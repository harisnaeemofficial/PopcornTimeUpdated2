package com.movieflix.ui.content;

import android.support.annotation.Nullable;

import com.movieflix.mvp.IPresenter;

public interface IContentStatusPresenter extends IPresenter<IContentStatusView> {

    void setKeywords(@Nullable String keywords);

    void getContent(boolean reset);
}
