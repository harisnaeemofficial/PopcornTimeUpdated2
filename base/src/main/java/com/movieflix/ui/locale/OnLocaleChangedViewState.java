package com.movieflix.ui.locale;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.movieflix.mvp.Presenter;
import com.movieflix.mvp.ViewState;

public final class OnLocaleChangedViewState extends ViewState<ILocaleView> {

    private String language;

    public OnLocaleChangedViewState(@NonNull Presenter<ILocaleView> presenter, @Nullable String language) {
        super(presenter);
        this.language = language;
    }

    public OnLocaleChangedViewState setLanguage(@Nullable String language) {
        this.language = language;
        return this;
    }

    @Override
    public void apply(@NonNull ILocaleView view) {
        view.onLocaleChanged(language);
    }
}
