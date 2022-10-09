package com.movieflix.model.content;

import java.util.List;

import com.movieflix.base.model.video.info.VideoInfo;

public interface IContentStatus {

    boolean isLoading();

    Throwable getError();

    List<VideoInfo> getList();
}
