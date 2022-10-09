package com.movieflix.ui.details;

import android.support.annotation.Nullable;

import com.movieflix.base.model.video.info.Episode;
import com.movieflix.base.model.video.info.Season;
import com.movieflix.base.model.video.info.TvShowsInfo;

public interface IDetailsTvShowView extends IDetailsView<TvShowsInfo> {

    void onSeasons(@Nullable Season[] seasons, int position);

    void onEpisodes(@Nullable Episode[] episodes, int position);
}
