package com.movieflix.mobile.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.player.MobilePlayerActivity;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import com.movieflix.base.analytics.Analytics;
import com.movieflix.base.database.tables.Downloads;
import com.movieflix.base.database.tables.History;
import com.movieflix.base.model.PlayerInfo;
import com.movieflix.base.model.WatchInfo;
import com.movieflix.base.torrent.client.ClientConnectionListener;
import com.movieflix.base.torrent.client.WatchClient;
import com.movieflix.base.torrent.watch.WatchException;
import com.movieflix.base.torrent.watch.WatchListener;
import com.movieflix.base.torrent.watch.WatchProgress;
import com.movieflix.base.torrent.watch.WatchState;
import com.movieflix.base.utils.Logger;
import com.movieflix.mobile.PopcornApplication;
import com.movieflix.mobile.R;
import com.movieflix.mobile.ui.VLCPlayerActivity;
import com.movieflix.mobile.ui.base.PlayerBaseActivity;
import com.movieflix.model.share.IShareUseCase;

public class WatchDialog extends DialogFragment implements WatchListener {

    private ImageView movieflix;
    private ViewGroup statusLayout;
    private ProgressBar statusProgress;
    private TextView statusInfo;
    private TextView statusPercent;
    private TextView statusSeedsPeers;

    private WatchInfo watchInfo;
    private WatchClient watchClient;
    private boolean prepared;
    private Animation movieflixAnimation;
    private Animation alertIconAnimation;

    private String loadedSubtitlesPath;
    private boolean stopped;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("watch_info", watchInfo);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.FullscreenDialog);
        setCancelable(true);

        if (savedInstanceState != null) {
            watchInfo = savedInstanceState.getParcelable("watch_info");
        }

        prepared = false;
        movieflixAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.movieflix_prepare);
        alertIconAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.vpn_alert_icon_anim);

        watchClient = new WatchClient(getActivity());
        watchClient.setConnectionListener(new ClientConnectionListener() {
            @Override
            public void onClientConnected() {
                if (!prepared) {
                    watchClient.startWatch(watchInfo, WatchDialog.this);
                    Analytics.event(Analytics.Category.CONTENT, Analytics.Event.DOWNLOAD_START);
                }
            }

            @Override
            public void onClientDisconnected() {

            }
        });
        if (watchInfo != null) {
            ((PopcornApplication) getActivity().getApplication()).getShareUseCase().loadVideoShare(watchInfo.imdb);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && KeyEvent.ACTION_UP == event.getAction()) {
                    close();
                    return true;
                }
                return false;
            }
        });
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_watch, container, false);
        final PopcornApplication app = (PopcornApplication) getActivity().getApplication();

        movieflix = (ImageView) view.findViewById(R.id.prepare_movieflix);

        statusLayout = (ViewGroup) view.findViewById(R.id.prepare_status_layout);
        statusProgress = (ProgressBar) statusLayout.findViewById(R.id.prepare_status_progress);
        statusInfo = (TextView) statusLayout.findViewById(R.id.prepare_status_info);
        statusPercent = (TextView) statusLayout.findViewById(R.id.prepare_status_percent);
        statusSeedsPeers = (TextView) statusLayout.findViewById(R.id.prepare_status_seeds_peers);

        movieflix.setVisibility(View.GONE);
        updateStatus(0, 0, 0, 0, "0B/s");

        ImageButton closeBtn = (ImageButton) view.findViewById(R.id.prepare_close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ImageView image = (ImageView) view.findViewById(R.id.image);
        if (!TextUtils.isEmpty(watchInfo.posterUrl)) {
            Picasso.with(view.getContext()).load(watchInfo.posterUrl).into(image);
        } else {
            image.setImageResource(R.drawable.poster);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        watchClient.bind();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        watchClient.removeWatchListener(WatchDialog.this);
        watchClient.unbind();
    }

    @Override
    public void onError(WatchException exception) {
        if (exception != null) {
            if (WatchState.LOAD_METADATA == exception.getState()) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.error_metadata, Toast.LENGTH_SHORT).show();
                }
            }
            Logger.error("State: " + exception.getState() + ", error: " + exception.getMessage());
        }
        stopAnim();
        dismiss();
    }

    @Override
    public void onMetadataLoad() {
        startAnim();
        loadedSubtitlesPath = null;
    }

    @Override
    public void onSubtitlesLoaded(String subPath) {
        loadedSubtitlesPath = subPath;
        Logger.debug("WatchDialog<onSubtitlesLoaded>: " + subPath);
    }

    @Override
    public void onDownloadStarted(String torrent) {
        Logger.debug("WatchDialog<onDownloadStarted>: " + torrent);
        stopAnim();
        prepared = false;
        if (statusLayout != null) {
            updateStatus(0, 0, 0, 0, "0B/s");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 111) {
            final long length = data.getLongExtra(MobilePlayerActivity.EXTRA_LENGTH, 0);
            final long position = data.getLongExtra(MobilePlayerActivity.EXTRA_POSITION, 0);
            if (getActivity() != null) {
                final IShareUseCase shareUseCase = ((PopcornApplication) getActivity().getApplication()).getShareUseCase();
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (watchInfo != null) {
                            shareUseCase.share(watchInfo.imdb, length, position);
                        }
                    }
                }, 1000);
            }
            close();
        }
    }

    @Override
    public void onVideoPrepared(String filePath) {
        Logger.debug("WatchDialog<onPrepareWatchCompleted>: " + filePath);
        if (getActivity() == null || getActivity().getBaseContext() == null) {
            return;
        }
        if (watchInfo != null) {
            if (watchInfo.isDownloads()) {
                Downloads.readyToWatch(getContext(), watchInfo.downloadsId);
            }
            if (!History.isWatched(getContext(), watchInfo.imdb, watchInfo.episode, watchInfo.season)) {
                History.insert(getContext(), watchInfo);
            }
        }
        Analytics.event(Analytics.Category.CONTENT, Analytics.Event.WATCHING_START);
        prepared = true;
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.loadedSubtitlesPath = loadedSubtitlesPath;
        playerInfo.name = watchInfo.title;
        PlayerBaseActivity.startForResult(
                WatchDialog.this,
                new Intent(getActivity(), VLCPlayerActivity.class),
                111,
                Uri.parse("file://" + filePath),
                playerInfo
        );
    }

    @Override
    public void onUpdateProgress(WatchProgress progress) {
        updateStatus(progress.total, progress.value, progress.seeds, progress.peers, progress.speed);
    }

    @Override
    public void onDownloadFinished() {
        Logger.debug("WatchDialog<onDownloadFinished>");
    }

    @Override
    public void onBufferingFinished() {

    }

    public void show(FragmentManager fm, WatchInfo watchInfo, String tag) {
        if (!isAdded()) {
            this.watchInfo = watchInfo;
            super.show(fm, tag);
        }
    }

    @Override
    public void onDestroy() {
        //watchClient.removeWatchListener(WatchDialog.this);
        //watchClient.unbind();
        super.onDestroy();
    }

    public void close() {
        watchClient.stopWatch();
        dismiss();
    }

    private void startAnim() {
        statusLayout.setVisibility(View.GONE);
        if (movieflix != null) {
            movieflix.setVisibility(View.VISIBLE);
            if (movieflixAnimation != null) {
                movieflix.startAnimation(movieflixAnimation);
            }
        }
    }

    private void stopAnim() {
        if (movieflix != null) {
            movieflix.setVisibility(View.GONE);
            movieflix.clearAnimation();
        }
    }

    private void updateStatus(int max, int progress, int seeds, int peers, String info) {
        movieflix.setVisibility(View.GONE);
        statusLayout.setVisibility(View.VISIBLE);
        statusProgress.setMax(max);
        statusProgress.setProgress(progress);
        statusInfo.setText(info);
        statusPercent.setText(String.format(Locale.ENGLISH, "%d%%", max > 0 ? (100 * progress / max) : 0));
        statusSeedsPeers.setText(String.format(Locale.ENGLISH, "%d/%d", seeds, peers));
    }
}