package com.movieflix.ui.share;

import com.movieflix.mvp.IPresenter;

public interface ISharePresenter extends IPresenter<IShareView> {

    void share();

    void cancel();
}
