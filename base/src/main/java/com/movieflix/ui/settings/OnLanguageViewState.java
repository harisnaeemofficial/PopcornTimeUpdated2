package com.movieflix.ui.settings;

import android.support.annotation.NonNull;

import com.movieflix.mvp.Presenter;
import com.movieflix.mvp.ViewState;

public final class OnLanguageViewState extends ViewState<ISettingsView> {

    private String language;

    public OnLanguageViewState(@NonNull Presenter<ISettingsView> presenter, @NonNull String language) {
        super(presenter);
        this.language = language;
    }

    public void onLanguage(@NonNull String language) {
        this.language = language;
        apply();
    }

    @Override
    public void apply(@NonNull ISettingsView view) {
        view.onLanguage(language);
    }
}