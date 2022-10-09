package com.movieflix.ui.settings;

import android.support.annotation.NonNull;

import com.movieflix.mvp.Presenter;
import com.movieflix.mvp.ViewState;

public final class OnSubtitlesFontSizeViewState extends ViewState<ISettingsView> {

    private Float subtitlesFontSize;

    public OnSubtitlesFontSizeViewState(@NonNull Presenter<ISettingsView> presenter, @NonNull Float subtitlesFontSize) {
        super(presenter);
        this.subtitlesFontSize = subtitlesFontSize;
    }

    public void apply(@NonNull Float subtitlesFontSize) {
        this.subtitlesFontSize = subtitlesFontSize;
        apply();
    }

    @Override
    public void apply(@NonNull ISettingsView view) {
        view.onSubtitlesFontSize(subtitlesFontSize);
    }
}
