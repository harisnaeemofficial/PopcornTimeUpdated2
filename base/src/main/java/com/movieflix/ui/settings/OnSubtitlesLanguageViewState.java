package com.movieflix.ui.settings;

import android.support.annotation.NonNull;

import com.movieflix.mvp.Presenter;
import com.movieflix.mvp.ViewState;

public final class OnSubtitlesLanguageViewState extends ViewState<ISettingsView> {

    private String subtitlesLanguage;

    public OnSubtitlesLanguageViewState(@NonNull Presenter<ISettingsView> presenter, @NonNull String subtitlesLanguage) {
        super(presenter);
        this.subtitlesLanguage = subtitlesLanguage;
    }

    public void apply(@NonNull String subtitlesLanguage) {
        this.subtitlesLanguage = subtitlesLanguage;
        apply();
    }

    @Override
    public void apply(@NonNull ISettingsView view) {
        view.onSubtitlesLanguage(subtitlesLanguage);
    }
}
