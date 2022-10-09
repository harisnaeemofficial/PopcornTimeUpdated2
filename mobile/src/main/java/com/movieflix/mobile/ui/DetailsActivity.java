package com.movieflix.mobile.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.InterstitialAd;
import com.movieflix.IUseCaseManager;
import com.movieflix.UIUtils;
import com.movieflix.base.database.tables.Favorites;
import com.movieflix.base.model.video.Anime;
import com.movieflix.base.model.video.Cinema;
import com.movieflix.base.model.video.info.VideoInfo;
import com.movieflix.base.utils.Logger;
import com.movieflix.mobile.R;

public final class DetailsActivity extends LocaleActivity {

    private static final String TAG_FRAGMENT_DETAILS = "fragment_details";

    private boolean clear = false;

    private VideoInfo videoInfo;

    protected boolean showAnimationsDirectly = false;

    private MenuItem favoritesMenuItem;
    private DetailsVideoFragment fragment;

    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("rotated")) {
                showAnimationsDirectly = true;
                Logger.debug("showAnimationsDirectly");
            }
        }
        MainActivity.mInterstitialAd.show();


        if (Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation && !showAnimationsDirectly) {
            supportPostponeEnterTransition();
        }
        UIUtils.transparentStatusBar(this);
        videoInfo = ((IUseCaseManager) getApplication()).getDetailsUseCase().getVideoInfoProperty().getValue();
        if (videoInfo != null) {
            onVideoInfo(videoInfo);
        } else {
            finish();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clear) {
            ((IUseCaseManager) getApplication()).getDetailsUseCase().getVideoInfoProperty().setValue(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details, menu);
        favoritesMenuItem = menu.findItem(R.id.favorites);
        DrawableCompat.setTint(favoritesMenuItem.getIcon(), Favorites.isFavorite(this, videoInfo) ? Color.RED : Color.WHITE);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.favorites:
                if (Favorites.isFavorite(this, videoInfo)) {
                    DrawableCompat.setTint(favoritesMenuItem.getIcon(), Favorites.delete(this, videoInfo) == 0 ? Color.RED : Color.WHITE);
                } else {
                    DrawableCompat.setTint(favoritesMenuItem.getIcon(), Favorites.insert(this, videoInfo) != null ? Color.RED : Color.WHITE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        if (getIntent().getBooleanExtra("normal", false)) {
            fragment.enterAnimationsFinished();
        }
    }

    @Override
    public void onBackPressed() {
        fragment.finish(DetailsActivity.super::onBackPressed);
        clear = true;
    }

    private void onVideoInfo(@NonNull VideoInfo videoInfo) {
        switch (videoInfo.getType()) {
            case Cinema.TYPE_MOVIES:
            case Anime.TYPE_MOVIES:
                addFragment(DetailsMovieFragment.class);
                break;
            case Cinema.TYPE_TV_SHOWS:
            case Anime.TYPE_TV_SHOWS:
                addFragment(DetailsTvShowFragment.class);
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("rotated", true);
        super.onSaveInstanceState(outState);
    }

    private void addFragment(@NonNull Class<? extends Fragment> clazz) {
        fragment = (DetailsVideoFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_DETAILS);
        if (fragment == null) {
            try {
                fragment = (DetailsVideoFragment) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (fragment.isAdded()) {
            return;
        }
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment, TAG_FRAGMENT_DETAILS).commit();
    }

    public static void start(@NonNull Context context, @NonNull VideoInfo videoInfo) {
        ((IUseCaseManager) context.getApplicationContext()).getDetailsUseCase().getVideoInfoProperty().setValue(videoInfo);
        Intent start = new Intent(context, DetailsActivity.class);
        start.putExtra("normal", true);
        context.startActivity(start);
    }


}
