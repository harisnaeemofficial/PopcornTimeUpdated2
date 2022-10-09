package com.movieflix.ui.content;

import android.support.annotation.NonNull;

import com.movieflix.mvp.Presenter;
import com.movieflix.mvp.ViewState;

import com.movieflix.model.content.IContentStatus;

public final class ContentStatusViewState extends ViewState<IContentStatusView> {

    private IContentStatus contentStatus;

    public ContentStatusViewState(@NonNull Presenter<IContentStatusView> presenter, @NonNull IContentStatus contentStatus) {
        super(presenter);
        this.contentStatus = contentStatus;
    }

    public void apply(@NonNull IContentStatus contentState) {
        this.contentStatus = contentState;
        apply();
    }

    @Override
    public void apply(@NonNull IContentStatusView view) {
        view.onContentStatus(contentStatus);
    }
}
