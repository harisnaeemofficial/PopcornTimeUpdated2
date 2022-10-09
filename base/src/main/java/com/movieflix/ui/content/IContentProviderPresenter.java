package com.movieflix.ui.content;

import android.support.annotation.NonNull;

import com.movieflix.mvp.IPresenter;

import com.movieflix.model.content.IContentProvider;

public interface IContentProviderPresenter extends IPresenter<IContentProviderView> {

    void setContentProvider(@NonNull IContentProvider contentProvider);
}
