package com.movieflix.ui.share;

import android.support.annotation.NonNull;

import com.movieflix.mvp.Presenter;
import com.movieflix.mvp.ViewState;

import com.movieflix.model.share.IShareData;

public final class OnShowShareDataViewState extends ViewState<IShareView> {

    private IShareData shareData;

    public OnShowShareDataViewState(@NonNull Presenter<IShareView> presenter, @NonNull IShareData shareData) {
        super(presenter);
        this.shareData = shareData;
    }

    public OnShowShareDataViewState setShareData(@NonNull IShareData shareData) {
        this.shareData = shareData;
        return this;
    }

    @Override
    public void apply(@NonNull IShareView view) {
        view.onShowShareData(shareData);
    }
}
