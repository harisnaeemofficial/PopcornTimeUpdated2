package com.movieflix.ui.details;

import com.movieflix.mvp.IPresenter;

import com.movieflix.base.model.video.info.VideoInfo;

public interface IDetailsPresenter<T extends VideoInfo, V extends IDetailsView<T>> extends IPresenter<V> {
}
