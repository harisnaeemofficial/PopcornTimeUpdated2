package com.movieflix.ui.settings;

import android.support.annotation.NonNull;

import com.movieflix.mvp.Presenter;
import com.movieflix.mvp.ViewState;

public final class OnAboutSiteViewState extends ViewState<ISettingsView> {

    private String siteUrl;

    public OnAboutSiteViewState(@NonNull Presenter<ISettingsView> presenter, @NonNull String siteUrl) {
        super(presenter);
        this.siteUrl = siteUrl;
    }

    public void apply(@NonNull String siteUrl) {
        this.siteUrl = siteUrl;
        apply();
    }

    @Override
    public void apply(@NonNull ISettingsView view) {
        view.onAboutSite(siteUrl);
    }
}
