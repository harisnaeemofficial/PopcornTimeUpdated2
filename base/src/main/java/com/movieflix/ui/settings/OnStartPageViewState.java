package com.movieflix.ui.settings;

import android.support.annotation.NonNull;

import com.movieflix.mvp.Presenter;
import com.movieflix.mvp.ViewState;

public final class OnStartPageViewState extends ViewState<ISettingsView> {

    private Integer startPage;

    public OnStartPageViewState(@NonNull Presenter<ISettingsView> presenter, @NonNull Integer startPage) {
        super(presenter);
        this.startPage = startPage;
    }

    public void onStartPage(@NonNull Integer startPage) {
        this.startPage = startPage;
        apply();
    }

    @Override
    public void apply(@NonNull ISettingsView view) {
        view.onStartPage(startPage);
    }
}
