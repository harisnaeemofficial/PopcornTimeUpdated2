package com.movieflix.mobile.ui;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.sax.EndElementListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.transition.Transition;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterViewFlipper;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.movieflix.HeaderBehavior;
import com.movieflix.IUseCaseManager;
import com.movieflix.VibrantUtils;
import com.movieflix.base.IPopcornApplication;
import com.movieflix.base.database.tables.Downloads;
import com.movieflix.base.model.DownloadInfo;
import com.movieflix.base.model.WatchInfo;
import com.movieflix.base.model.video.Cinema;
import com.movieflix.base.model.video.info.Episode;
import com.movieflix.base.model.video.info.Season;
import com.movieflix.base.model.video.info.Torrent;
import com.movieflix.base.model.video.info.TvShowsInfo;
import com.movieflix.base.model.video.info.VideoInfo;
import com.movieflix.base.prefs.PopcornPrefs;
import com.movieflix.base.prefs.Prefs;
import com.movieflix.base.storage.StorageUtil;
import com.movieflix.base.subtitles.SubtitlesLanguage;
import com.movieflix.base.torrent.TorrentState;
import com.movieflix.base.torrent.client.DownloadsClient;
import com.movieflix.base.utils.Logger;
import com.movieflix.base.utils.NetworkUtil;
import com.movieflix.mobile.PopcornApplication;
import com.movieflix.mobile.R;
import com.movieflix.mobile.model.content.ContentRepository;
import com.movieflix.mobile.ui.adapter.PeopleAdapter;
import com.movieflix.mobile.ui.adapter.VideoPosterAdapter;
import com.movieflix.mobile.ui.dialog.OptionDialog;
import com.movieflix.mobile.ui.dialog.WatchDialog;
import com.movieflix.mobile.ui.widget.ItemSelectButton;
import com.movieflix.model.details.IDetailsUseCase;
import com.movieflix.model.subtitles.Subtitles;
import com.movieflix.mvp.IViewRouter;
import com.movieflix.ui.IBrowserView;
import com.movieflix.ui.details.IDetailsPresenter;
import com.movieflix.ui.details.IDetailsView;
import com.movieflix.utils.PermissionsUtils;
import com.player.dialog.FileChooserDialog;
import com.player.dialog.ListItemEntity;
import com.player.subtitles.SubtitlesRenderer;
import com.player.subtitles.SubtitlesUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.widget.Toast.LENGTH_SHORT;

public abstract class DetailsVideoFragment<T extends VideoInfo, V extends IDetailsView<T>, P extends IDetailsPresenter<T, V>> extends Fragment implements IDetailsView<T> {

    private static final String TAG_VPN_DIALOG = "vpn_dialog";

    private static final float RATING_MULTIPLIER = 5f / 10; // start count / max rating

    private static final int REQUEST_CODE_DOWNLOAD_EXTERNAL_STORAGE_PERMISSION = 100;
    private static final int REQUEST_CODE_WATCH_EXTERNAL_STORAGE_PERMISSION = 101;

    private P detailsPresenter;

    protected DownloadsClient downloadsClient;
    protected IDetailsUseCase detailsUseCase;

    protected ImageView poster;
    protected AdapterViewFlipper backdrops;
    protected RatingBar ratingbar;
    protected TextView ratingbarText;
    protected TextView title;
    protected TextView genre;
    protected TextView year;
    protected TextView duration;
    protected TextView description;

    protected FloatingActionButton playBtn;
    protected FloatingActionButton downloadBtn;

    protected ToggleButton watchedButton;
    protected Button imdbBtn;
    protected AppCompatButton trailerBtn;
    protected TextView additionalDescription;
    protected TextView additionalReleaseDate;
    protected View additionalControls;
    protected ItemSelectButton subtitlesBtn;
    protected ItemSelectButton dubbingBtn;
    protected ItemSelectButton torrentsBtn;
    private FrameLayout posterParent;

    private RecyclerView recyclerActors;
    private PeopleAdapter actorsAdapter;
    protected NestedScrollView nestedScrollView;
    View viewDetailsHeader;
    private boolean canEnableBtns = false;
    private boolean buttonsShown = false;
    private boolean transitioned = false;
    private AppBarLayout appBarLayout;

    private AdView mAdView;
    private AdView mAdView2;
    private AdView mAdView3;
    private AdView mAdView4;
    private AdView mAdView5;
    private AdView mAdView6;
    private AdView mAdView7;
    private AdView mAdView8;
    private AdView mAdView9;
    private AdView mAdView10;
    private AdView mAdView11;
    private AdView mAdView12;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        downloadsClient = new DownloadsClient(getContext());
        final IUseCaseManager useCaseManager = (IUseCaseManager) getActivity().getApplication();
        detailsPresenter = onCreateDetailsPresenter(useCaseManager);
        detailsUseCase = useCaseManager.getDetailsUseCase();


    }



    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(view.findViewById(R.id.toolbar));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        nestedScrollView = view.findViewById(R.id.nested_scroll);
        posterParent = view.findViewById(R.id.poster_parent);
        poster = view.findViewById(R.id.poster);
        backdrops = view.findViewById(R.id.backdrops);
        ratingbar = view.findViewById(R.id.ratingbar);
        ratingbarText = view.findViewById(R.id.ratingbar_text);
        title = view.findViewById(R.id.title);
        genre = view.findViewById(R.id.genre);
        year = view.findViewById(R.id.year);
        duration = view.findViewById(R.id.duration);
        description = view.findViewById(R.id.description);
        playBtn = view.findViewById(R.id.play);
        recyclerActors = view.findViewById(R.id.recyclerActors);
        viewDetailsHeader = view.findViewById(R.id.view_details_header);

        mAdView = view.findViewById(R.id.adView);
        mAdView2 = view.findViewById(R.id.adView2);
        mAdView3 = view.findViewById(R.id.adView3);
        mAdView4 = view.findViewById(R.id.adView4);
        mAdView5 = view.findViewById(R.id.adView5);
        mAdView6 = view.findViewById(R.id.adView6);
        mAdView7 = view.findViewById(R.id.adView7);
        mAdView8 = view.findViewById(R.id.adView8);
        mAdView9 = view.findViewById(R.id.adView9);
        mAdView10 = view.findViewById(R.id.adView10);
        mAdView11 = view.findViewById(R.id.adView11);
        mAdView12 = view.findViewById(R.id.adView12);

        playBtn.setOnClickListener(v -> {
        //    MainActivity.mInterstitialAd.show();
            MainActivity.rewardedVideoAd.show();
            /*MainActivity.mInterstitialAd.setAdListener(new AdListener() {
                public void onAdClosed() {
                    final Torrent torrent = detailsUseCase.getTorrentChoiceProperty().getItem();
                    if (torrent != null) {
                        watch(torrent);
                    }
                }
            });*/
            MainActivity.rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener(){
                @Override
                public void onRewardedVideoAdLoaded() {
                }
                @Override
                public void onRewardedVideoAdOpened() {
                }
                @Override
                public void onRewardedVideoStarted() {
                }
                @Override
                public void onRewardedVideoAdClosed() {
                }
                @Override
                public void onRewarded(RewardItem rewardItem) {
                    final Torrent torrent = detailsUseCase.getTorrentChoiceProperty().getItem();
                    if (torrent != null) {
                        watch(torrent);
                    }
                }
                @Override
                public void onRewardedVideoAdLeftApplication() {
                }
                @Override
                public void onRewardedVideoAdFailedToLoad(int i) {
                }
                @Override
                public void onRewardedVideoCompleted() {
                }
            });
        });
        downloadBtn = view.findViewById(R.id.download);

        final View additionalDetails = view.findViewById(R.id.additional_details);
        ((HeaderBehavior) ((CoordinatorLayout.LayoutParams) view.findViewById(R.id.appbar).getLayoutParams()).getBehavior()).setBottomContent(additionalDetails);

        watchedButton = additionalDetails.findViewById(R.id.watched);
        imdbBtn = additionalDetails.findViewById(R.id.imdb);
        trailerBtn = additionalDetails.findViewById(R.id.trailer);
        additionalDescription = additionalDetails.findViewById(R.id.additional_description);
        additionalReleaseDate = additionalDetails.findViewById(R.id.additional_release_date);
        additionalControls = additionalDetails.findViewById(R.id.additional_controls);
        subtitlesBtn = additionalDetails.findViewById(R.id.subtitles);
        subtitlesBtn.setFragmentManager(getActivity().getSupportFragmentManager());
        subtitlesBtn.setPrompt(R.string.subtitles);
        dubbingBtn = additionalDetails.findViewById(R.id.dubbing);
        dubbingBtn.setFragmentManager(getActivity().getSupportFragmentManager());
        dubbingBtn.setPrompt(R.string.dubbing);
        torrentsBtn = additionalDetails.findViewById(R.id.torrents);
        torrentsBtn.setFragmentManager(getActivity().getSupportFragmentManager());
        torrentsBtn.setPrompt(R.string.torrents);
        appBarLayout = view.findViewById(R.id.appbar);

        LayerDrawable stars = (LayerDrawable) ratingbar.getProgressDrawable();
        DrawableCompat.setTint(DrawableCompat.wrap(stars.getDrawable(0)), Color.WHITE);
        DrawableCompat.setTint(DrawableCompat.wrap(stars.getDrawable(1)), VibrantUtils.getAccentColor());
        DrawableCompat.setTint(DrawableCompat.wrap(stars.getDrawable(2)), VibrantUtils.getAccentColor());

        final ColorStateList list = ColorStateList.valueOf(VibrantUtils.getAccentColor());
        ratingbarText.setTextColor(list);
        trailerBtn.setSupportBackgroundTintList(list);
        playBtn.setBackgroundTintList(list);
        downloadBtn.setBackgroundTintList(list);
        AppCompatButton showImdbInfo = view.findViewById(R.id.show_imdb_info);
        showImdbInfo.setSupportBackgroundTintList(list);

        nestedScrollView.setFillViewport(true);

        View imdbInfo = view.findViewById(R.id.imdb_info);
        showImdbInfo.setOnClickListener(v -> {
            if (imdbInfo.getVisibility() == View.GONE) {
                imdbInfo.setVisibility(View.VISIBLE);
                showImdbInfo.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_arrow_up), null, null, null);
            } else {
                imdbInfo.setVisibility(View.GONE);
                showImdbInfo.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_arrow_down), null, null, null);
            }
        });

        if (!getActivity().getIntent().getBooleanExtra("normal", false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActivity().getWindow().getEnterTransition().addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {

                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {
                        enterAnimationsFinished();
                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                });
            }
        }
        if (((DetailsActivity)getActivity()).showAnimationsDirectly) {
            Logger.debug("showAnimationsDirectly fragment");
            enterAnimationsFinished();
        }


        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        adRequest = new AdRequest.Builder().build();
        mAdView2.loadAd(adRequest);

        adRequest = new AdRequest.Builder().build();
        mAdView3.loadAd(adRequest);

        adRequest = new AdRequest.Builder().build();
        mAdView4.loadAd(adRequest);

        adRequest = new AdRequest.Builder().build();
        mAdView5.loadAd(adRequest);

        adRequest = new AdRequest.Builder().build();
        mAdView6.loadAd(adRequest);

        adRequest = new AdRequest.Builder().build();
        mAdView7.loadAd(adRequest);

        adRequest = new AdRequest.Builder().build();
        mAdView8.loadAd(adRequest);

        adRequest = new AdRequest.Builder().build();
        mAdView9.loadAd(adRequest);

        adRequest = new AdRequest.Builder().build();
        mAdView10.loadAd(adRequest);

        adRequest = new AdRequest.Builder().build();
        mAdView11.loadAd(adRequest);

        adRequest = new AdRequest.Builder().build();
        mAdView12.loadAd(adRequest);

    }



    @Override
    public void onStart() {
        super.onStart();
        downloadsClient.bind();
    }

    public void onResume() {
        super.onResume();
        getDetailsPresenter().attach((V) this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getDetailsPresenter().detach((V) this);
        if (backdrops != null) {
            backdrops.stopFlipping();
        }
    }

    void finish(EndElementListener endListener) {
        appBarLayout.setExpanded(true, true);
        circularHideCard(nestedScrollView, nestedScrollView.getMeasuredWidth()/2, 0, null);
        circularHideCard(downloadBtn, downloadBtn.getMeasuredWidth() / 2, downloadBtn.getMeasuredHeight() / 2, null);
        circularHideCard(playBtn, playBtn.getMeasuredWidth() / 2, playBtn.getMeasuredHeight() / 2, null);
        circularHideCard(viewDetailsHeader, viewDetailsHeader.getMeasuredWidth()/2, 0, endListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        downloadsClient.unbind();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_CODE_DOWNLOAD_EXTERNAL_STORAGE_PERMISSION == requestCode || REQUEST_CODE_WATCH_EXTERNAL_STORAGE_PERMISSION == requestCode) {
            if (PermissionsUtils.isPermissionsGranted(permissions, grantResults)) {
                if (StorageUtil.getCacheDir() == null) {
                    StorageUtil.init(getContext(), ((PopcornApplication) getActivity().getApplication()).getSettingsUseCase());
                }
                if (REQUEST_CODE_DOWNLOAD_EXTERNAL_STORAGE_PERMISSION == requestCode) {
                    download(detailsUseCase.getTorrentChoiceProperty().getItem());
                } else {
                    watch(detailsUseCase.getTorrentChoiceProperty().getItem());
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onVideoInfo(@NonNull T videoInfo) {
        if (Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation && poster != null) {
            Picasso.with(getContext()).load(videoInfo.getPosterBig()).placeholder(android.R.color.black).into(poster, new Callback() {
                @Override
                public void onSuccess() {
                    Drawable foreDrawable;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        foreDrawable = new RippleDrawable(ColorStateList.valueOf(VibrantUtils.getAccentColor()), null, null);
                    } else {
                        foreDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ripple_image);
                    }
                    posterParent.setForeground(foreDrawable);
                    getActivity().supportStartPostponedEnterTransition();
                }

                @Override
                public void onError() {
                    getActivity().supportStartPostponedEnterTransition();
                }
            });
        }
        if (backdrops != null) {
            final String[] urls = videoInfo.getBackdrops() != null ? videoInfo.getBackdrops() : new String[]{videoInfo.getPosterBig()};
            if (backdrops.getAdapter() == null) {
                backdrops.setAdapter(new VideoPosterAdapter(urls));
                backdrops.setFlipInterval(5000);
                backdrops.setInAnimation(getActivity(), android.R.animator.fade_in);
                backdrops.setOutAnimation(getActivity(), android.R.animator.fade_out);
            } else {
                ((VideoPosterAdapter) backdrops.getAdapter()).setUrls(urls);
            }
            if (backdrops.getCount() > 1) {
                backdrops.startFlipping();
            } else {
                backdrops.stopFlipping();
            }
        }

        ratingbar.setRating(RATING_MULTIPLIER * videoInfo.getRating());
        final SpannableString rating = new SpannableString(String.format(Locale.ENGLISH, "%.1f/10", videoInfo.getRating()));
        rating.setSpan(new RelativeSizeSpan(0.67f), 3, rating.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        ratingbarText.setText(rating);
        title.setText(Html.fromHtml(videoInfo.getTitle()));
        final int genreRes = videoInfo.getGenres() != null && videoInfo.getGenres().length > 0 ? getGenreRes(videoInfo.getGenres()[0]) : 0;
        if (genreRes != 0) {
            genre.setText(genreRes);
            genre.setVisibility(View.VISIBLE);
        } else {
            genre.setVisibility(View.GONE);
        }
        year.setText(String.format(Locale.ENGLISH, "%d", videoInfo.getYear()));
        duration.setText(String.format(Locale.ENGLISH, "%dm", videoInfo.getDurationMinutes()));
        description.setText(Html.fromHtml(videoInfo.getDescription()));
        if (!isCinema(videoInfo) || TextUtils.isEmpty(videoInfo.getImdb())) {
            imdbBtn.setVisibility(View.GONE);
        } else {
            imdbBtn.setVisibility(View.VISIBLE);
            imdbBtn.setOnClickListener(new OnImdbClickListener(videoInfo.getImdb()));
        }
        if (TextUtils.isEmpty(videoInfo.getTrailer())) {
            trailerBtn.setVisibility(View.GONE);
        } else {
            trailerBtn.setVisibility(View.VISIBLE);
            trailerBtn.setOnClickListener(new OnTrailerClickListener(videoInfo.getTrailer()));
        }
        if (videoInfo.getCast() != null && actorsAdapter == null) {
            actorsAdapter = new PeopleAdapter(videoInfo.getCast());
            recyclerActors.setAdapter(actorsAdapter);
            recyclerActors.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        }
    }

    @Override
    public void onDubbing(@Nullable String[] languages, int position) {
        if (languages != null && languages.length > 0) {
            final List<ListItemEntity> items = new ArrayList<>();
            for (String language : languages) {
                ListItemEntity.addItemToList(items, new ListItemEntity<String>(language) {

                    @Override
                    public String getName() {
                        switch (getValue()) {
                            case "":
                                return "No dubbing";
                            case "it":
                                return getString(R.string.lang_italian);
                            case "ru":
                                return getString(R.string.lang_russian);
                            case "es":
                                return getString(R.string.lang_spanish);
                            case "fr":
                                return getString(R.string.lang_french);
                            case "pb":
                            case "br":
                                return getString(R.string.lang_brazilian_portuguese);
                            default:
                                return getValue();
                        }
                    }

                    @Override
                    public CharSequence getControlText() {
                        switch (getValue()) {
                            case "":
                                return "Select dubbing";
                            default:
                                return super.getControlText();
                        }
                    }

                    @Override
                    public void onItemChosen() {
                        detailsUseCase.getDubbingChoiceProperty().setPosition(getPosition());
                    }
                });
            }
            dubbingBtn.setItems(items, position);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onTorrents(@Nullable Torrent[] torrents, int position) {
        if (torrents != null && torrents.length > 0) {
            final List<ListItemEntity> items = new ArrayList<>();
            for (Torrent torrent : torrents) {
                ListItemEntity.addItemToList(items, new ListItemEntity<Torrent>(torrent) {

                    @Override
                    public String getName() {
                        return String.format(
                                Locale.ENGLISH, "%s (%s)  %s %s,  %s %s, %s %s",
                                getValue().getQuality(),
                                StorageUtil.getSizeText(getValue().getSize()),
                                getValue().getSeeds(), getString(R.string.seeds),
                                getValue().getPeers(), getString(R.string.peers),
                                getValue().getFile().substring(getValue().getFile().lastIndexOf('.')+1).toUpperCase(), getString(R.string.file)
                        );
                    }

                    @Override
                    public void onItemChosen() {
                        detailsUseCase.getTorrentChoiceProperty().setPosition(getPosition());
                    }
                });
            }
            torrentsBtn.setItems(items, position);
            final Torrent t = torrents[position];
            if (t != null) {
                final Season season = detailsUseCase.getSeasonChoiceProperty().getItem();
                final Episode episode = detailsUseCase.getEpisodeChoiceProperty().getItem();
                if (Downloads.isDownloads(getContext(), t, season != null ? season.getNumber() : -1, episode != null ? episode.getNumber() : -1)) {
                    showOpenBtn(t);
                } else {
                    showDownloadBtn(t);
                }
                if (transitioned) {
                    if (!buttonsShown) {
                        buttonsShown = true;
                        circularRevealCard(downloadBtn, downloadBtn.getMeasuredWidth() / 2, downloadBtn.getMeasuredHeight() / 2);
                        circularRevealCard(playBtn, playBtn.getMeasuredWidth() / 2, playBtn.getMeasuredHeight() / 2);
                    } else {
                        playBtn.setVisibility(View.VISIBLE);
                        downloadBtn.setVisibility(View.VISIBLE);
                    }
                }
                canEnableBtns = true;
                additionalControls.setVisibility(View.VISIBLE);
                return;
            }
        }
        playBtn.setVisibility(View.GONE);
        downloadBtn.setVisibility(View.GONE);
        additionalControls.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLangSubtitles(@Nullable String[] languages, int position) {
        final List<ListItemEntity> items = new ArrayList<>();
        ListItemEntity.addItemToList(items, new ListItemEntity<String>(SubtitlesUtils.WITHOUT_SUBTITLES) {

            @Override
            public String getName() {
                return getString(R.string.without_subtitle);
            }

            @Override
            public void onItemChosen() {
                detailsUseCase.getCustomSubtitlesProperty().setValue(null);
                detailsUseCase.getLangSubtitlesChoiceProperty().setPosition(-1);
            }
        });
        ListItemEntity.addItemToList(items, new ListItemEntity<String>(SubtitlesUtils.CUSTOM_SUBTITLES) {

            @Override
            public String getName() {
                return getString(R.string.custom_subtitle);
            }

            @Override
            public void onItemChosen() {
                final String tag = "custom_subs_dialog";
                FileChooserDialog dialog = (FileChooserDialog) getFragmentManager().findFragmentByTag(tag);
                if (dialog == null) {
                    dialog = new FileChooserDialog();
                }
                if (!dialog.isAdded()) {
                    dialog.setTitle(R.string.select_subtitle);
                    dialog.setChooserListener(new FileChooserDialog.OnChooserListener() {

                        @Override
                        public void onChooserSelected(File file) {
                            final Subtitles sub = new Subtitles();
                            sub.setUrl(Uri.fromFile(file).toString());
                            detailsUseCase.getCustomSubtitlesProperty().setValue(sub);
                            subtitlesBtn.showSelectedItem(1);
                        }

                        @Override
                        public void onChooserCancel() {
                            detailsUseCase.getCustomSubtitlesProperty().setValue(null);
                        }
                    });
                    dialog.setAcceptExtensions(SubtitlesRenderer.SUPPORTED_EXTENSIONS);
                    dialog.setDenyFolderNames(new String[]{
                            StorageUtil.ROOT_FOLDER_NAME
                    });
                    dialog.show(getFragmentManager(), StorageUtil.getSDCardFolder(getActivity()));
                }
            }
        });
        if (languages != null) {
            for (String lang : languages) {
                ListItemEntity.addItemToList(items, new ListItemEntity<String>(lang) {

                    @Override
                    public String getName() {
                        return SubtitlesLanguage.subtitlesNameToNative(SubtitlesLanguage.subtitlesIsoToName(getValue()));
                    }

                    @Override
                    public void onItemChosen() {
                        detailsUseCase.getCustomSubtitlesProperty().setValue(null);
                        detailsUseCase.getLangSubtitlesChoiceProperty().setPosition(getPosition() - 2);
                    }
                });
            }
        }
        if (detailsUseCase.getCustomSubtitlesProperty().getValue() != null) {
            position = -2;
        }
        subtitlesBtn.setItems(items, position >= 0 ? position + 2 : (position == -2 ? 1 : 0));
    }

    @NonNull
    protected final P getDetailsPresenter() {
        return detailsPresenter;
    }

    private void download(@NonNull Torrent torrent) {
        if (!isReadyToAction(torrent, REQUEST_CODE_DOWNLOAD_EXTERNAL_STORAGE_PERMISSION)) {
            return;
        }

        addDownload(torrent);
    }

    private void open(@NonNull Torrent torrent) {
        startActivity(new Intent(getActivity(), DownloadsActivity.class).putExtra(DownloadsActivity.VIDEO_URL, torrent.getUrl()));
    }

    private void watch(@NonNull Torrent torrent) {
        if (!isReadyToAction(torrent, REQUEST_CODE_WATCH_EXTERNAL_STORAGE_PERMISSION)) {
            return;
        }
        addWatch(torrent);
    }

    @NonNull
    protected DownloadInfo buildDownloadInfo(@NonNull Torrent torrent) {
        DownloadInfo info = new DownloadInfo();
        info.type = detailsUseCase.getVideoInfoProperty().getValue().getType();
        info.imdb = detailsUseCase.getVideoInfoProperty().getValue().getImdb();
        info.torrentUrl = torrent.getUrl();
        info.torrentMagnet = torrent.getMagnet();
        info.fileName = torrent.getFile();
        info.posterUrl = detailsUseCase.getVideoInfoProperty().getValue().getPoster();
        info.title = detailsUseCase.getVideoInfoProperty().getValue().getTitle();
        info.state = TorrentState.DOWNLOADING;
        info.size = torrent.getSize();
        info.torrentHash = torrent.getHash();
        return info;
    }

    @NonNull
    private WatchInfo buildWatchInfo(@NonNull Torrent torrent) {
        WatchInfo watchInfo = new WatchInfo();
        watchInfo.imdb = detailsUseCase.getVideoInfoProperty().getValue().getImdb();
        watchInfo.type = detailsUseCase.getVideoInfoProperty().getValue().getType();
        watchInfo.title = detailsUseCase.getVideoInfoProperty().getValue().getTitle();
        watchInfo.watchDir = StorageUtil.getCacheDirPath();
        watchInfo.torrentUrl = torrent.getUrl();
        watchInfo.torrentMagnet = torrent.getMagnet();
        watchInfo.fileName = torrent.getFile();
        watchInfo.posterUrl = detailsUseCase.getVideoInfoProperty().getValue().getPosterBig();
        if (detailsUseCase.getVideoInfoProperty().getValue() instanceof TvShowsInfo) {
            watchInfo.season = detailsUseCase.getSeasonChoiceProperty().getItem().getNumber();
            watchInfo.episode = detailsUseCase.getEpisodeChoiceProperty().getItem().getNumber();
        }
//        watchInfo.subtitles = subtitles;
        return watchInfo;
    }

    private boolean isReadyToAction(@NonNull Torrent torrent, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PermissionsUtils.requestPermissions(this, requestCode, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return false;
        }
        if (StorageUtil.getDownloadsDir() == null) {
            Toast.makeText(getActivity(), R.string.cache_folder_not_selected, LENGTH_SHORT).show();
            return false;
        }
        if (!NetworkUtil.hasAvailableConnection(getActivity())) {
            final String tag = "option_dialog";
            OptionDialog fragment = (OptionDialog) getFragmentManager().findFragmentByTag(tag);
            if (fragment == null) {
                fragment = new OptionDialog();
            }
            if (!fragment.isAdded()) {
                fragment.setArguments(OptionDialog.createArguments(getString(R.string.download), getString(R.string.disable_wifi_message)));
                fragment.setListener(new OptionDialog.SimpleOptionListener() {

                    @Override
                    public boolean positiveShow() {
                        return true;
                    }

                    @Override
                    public String positiveButtonText() {
                        return getString(android.R.string.ok);
                    }
                });
                fragment.show(getFragmentManager(), tag);
            }
            return false;
        }
        if (StorageUtil.getAvailableSpaceInBytes(StorageUtil.getDownloadsDirPath()) < torrent.getSize()) {
            final String tag = "option_dialog";
            OptionDialog fragment = (OptionDialog) getFragmentManager().findFragmentByTag(tag);
            if (fragment == null) {
                fragment = new OptionDialog();
            }
            if (!fragment.isAdded()) {
                fragment.setArguments(OptionDialog.createArguments(getString(R.string.application_name), getString(R.string.no_free_space)));
                fragment.setListener(new OptionDialog.SimpleOptionListener() {

                    @Override
                    public boolean positiveShow() {
                        return true;
                    }

                    @Override
                    public String positiveButtonText() {
                        return getString(android.R.string.ok);
                    }

                    @Override
                    public void positiveAction() {
                        StorageUtil.clearCacheDir();
                    }
                });
                fragment.show(getFragmentManager(), tag);
            }
            return false;
        }
        return true;
    }



    private void addDownload(@NonNull Torrent torrent) {
        final DownloadInfo info = buildDownloadInfo(torrent);
        String uuid = info.title + "-" + UUID.randomUUID().toString();
        String directoryPath = StorageUtil.getDownloadsDirPath() + "/" + uuid;
        info.directory = new File(directoryPath);
        if (info.directory.exists()) {
            StorageUtil.clearDir(info.directory);
        } else {
            if (!info.directory.mkdirs()) {
                Logger.error("VideoBaseFragment: Cannot create dir - " + info.directory.getAbsolutePath());
                return;
            }
        }
        downloadsClient.downloadsAdd(info);
        showOpenBtn(torrent);
    }

    private void addWatch(@NonNull Torrent torrent) {
        final String lastTorrent = Prefs.getPopcornPrefs().get(PopcornPrefs.LAST_TORRENT, "");
        if (!TextUtils.isEmpty(lastTorrent) && !(lastTorrent.equals(torrent.getUrl()) || lastTorrent.equals(torrent.getMagnet()))) {
            downloadsClient.removeTorrent(lastTorrent);
            Prefs.getPopcornPrefs().put(PopcornPrefs.LAST_TORRENT, "");
            StorageUtil.clearCacheDir();
        }
        showWatchDialog(buildWatchInfo(torrent));
    }

    private void showWatchDialog(@NonNull WatchInfo watchInfo) {
        final String tag = "watch_view";
        WatchDialog dialog = (WatchDialog) getFragmentManager().findFragmentByTag(tag);
        if (dialog == null) {
            dialog = new WatchDialog();
        }
        if (!dialog.isAdded()) {
            dialog.show(getFragmentManager(), watchInfo, tag);
        }
    }

    private void showDownloadBtn(@NonNull final Torrent torrent) {
        downloadBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_item_download_resume));
        downloadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity.mInterstitialAd.show();
                download(torrent);
            }
        });
    }

    private void showOpenBtn(@NonNull final Torrent torrent) {
        downloadBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_folder));
        downloadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity.mInterstitialAd.show();
                open(torrent);
            }
        });
    }

    @NonNull
    protected abstract P onCreateDetailsPresenter(@NonNull IUseCaseManager useCaseManager);

    @StringRes
    private int getGenreRes(@NonNull String genre) {
        switch (genre) {
            case ContentRepository.GENRE_NONE:
            case ContentRepository.GENRE_POPULAR:
                return R.string.popular;
            case ContentRepository.GENRE_ACTION:
                return R.string.action;
            case ContentRepository.GENRE_ADVENTURE:
                return R.string.adventure;
            case ContentRepository.GENRE_ANIMATION:
                return R.string.animation;
            case ContentRepository.GENRE_BIOGRAPHY:
                return R.string.biography;
            case ContentRepository.GENRE_COMEDY:
                return R.string.comedy;
            case ContentRepository.GENRE_CRIME:
                return R.string.crime;
            case ContentRepository.GENRE_DOCUMENTARY:
                return R.string.documentary;
            case ContentRepository.GENRE_DRAMA:
                return R.string.drama;
            case ContentRepository.GENRE_FAMILY:
                return R.string.family;
            case ContentRepository.GENRE_FANTASY:
                return R.string.fantasy;
            case ContentRepository.GENRE_FILM_NOIR:
                return R.string.film_noir;
            case ContentRepository.GENRE_HISTORY:
                return R.string.history;
            case ContentRepository.GENRE_HORROR:
                return R.string.horror;
            case ContentRepository.GENRE_MUSIC:
                return R.string.music;
            case ContentRepository.GENRE_MUSICAL:
                return R.string.musical;
            case ContentRepository.GENRE_MYSTERY:
                return R.string.mystery;
            case ContentRepository.GENRE_ROMANCE:
                return R.string.romance;
            case ContentRepository.GENRE_SCI_FI:
                return R.string.sci_fi;
            case ContentRepository.GENRE_SHORT:
                return R.string.short_;
            case ContentRepository.GENRE_SPORT:
                return R.string.sport;
            case ContentRepository.GENRE_THRILLER:
                return R.string.thriller;
            case ContentRepository.GENRE_WAR:
                return R.string.war;
            case ContentRepository.GENRE_WESTERN:
                return R.string.western;
            default:
                return 0;
        }
    }

    private boolean isCinema(@NonNull T videoInfo) {
        return Cinema.TYPE_MOVIES.equals(videoInfo.getType()) || Cinema.TYPE_TV_SHOWS.equals(videoInfo.getType());
    }

    public void enterAnimationsFinished() {
        if (nestedScrollView.getWindowToken() != null) {
            enterAnimations();
        } else {
            nestedScrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    new Handler().postDelayed(() -> enterAnimations(), 400);
                }
            });
        }
    }

    public void enterAnimations() {
        Logger.debug("enterAnimationsFinished");
        circularRevealCard(nestedScrollView, nestedScrollView.getMeasuredWidth()/2, 0);
        transitioned = true;
        if (canEnableBtns) {
            if (!buttonsShown) {
                buttonsShown = true;
                circularRevealCard(downloadBtn, downloadBtn.getMeasuredWidth() / 2, downloadBtn.getMeasuredHeight() / 2);
                circularRevealCard(playBtn, playBtn.getMeasuredWidth() / 2, playBtn.getMeasuredHeight() / 2);
            }
        }
        circularRevealCard(viewDetailsHeader, viewDetailsHeader.getMeasuredWidth()/2, 0);
    }

    private final class OnImdbClickListener implements View.OnClickListener {

        private final String imdb;

        private OnImdbClickListener(@NonNull String imdb) {
            this.imdb = imdb;
        }

        @Override
        public void onClick(View v) {
            final String url = ((IPopcornApplication) getActivity().getApplication()).getConfigUseCase().getConfig().getImdbUrl();
            if (!TextUtils.isEmpty(url)) {
                ((IViewRouter) getActivity()).onShowView(IBrowserView.class, url + imdb);
            }
        }
    }

    private final class OnTrailerClickListener implements View.OnClickListener {

        private final String trailer;

        private OnTrailerClickListener(@NonNull String trailer) {
            this.trailer = trailer;
        }

        @Override
        public void onClick(View v) {
            TrailerActivity.start(v.getContext(), trailer);
        }
    }
    // The collapse points is where the animation collapses into
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void circularHideCard(final View view, int collapsePointX, int collapsePointY, EndElementListener endListener) {

        if (view == null) {return;}
        // Radius is whichever dimension is the longest on our screen
        float finalRadius = Math.max(view.getWidth(), view.getHeight());

        // Start circular animation
        Animator animator = ViewAnimationUtils.createCircularReveal(view, collapsePointX, collapsePointY, finalRadius, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(500);
        animator.start();

        // Listen for completion and hide view
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
                if (endListener != null) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            endListener.end();
                        }
                    }, 200);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    // The expansion points is where the animation starts
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void circularRevealCard(
            View view, int expansionPointX, int expansionPointY) {
        if (view == null) {return;}
        // Radius is whichever dimension is the longest on our screen
        float finalRadius = Math.max(view.getWidth(), view.getHeight());

        // Start circular animation
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(view, expansionPointX, expansionPointY, 0, finalRadius * 1.1f);
        circularReveal.setDuration(500);

        // make the view visible and start the animation
        view.setVisibility(View.VISIBLE);
        circularReveal.start();
    }

}
