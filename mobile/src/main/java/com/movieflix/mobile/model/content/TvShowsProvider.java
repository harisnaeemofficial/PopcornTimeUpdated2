package com.movieflix.mobile.model.content;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import com.movieflix.base.model.video.info.TvShowsInfo;
import com.movieflix.base.model.video.info.VideoInfo;
import com.movieflix.model.content.IDetailsProvider;
import com.movieflix.model.content.ISubtitlesProvider;
import com.movieflix.model.filter.IFilter;

public final class TvShowsProvider<T extends TvShowsInfo> extends ContentProvider<T> {

    public TvShowsProvider(@NonNull ContentType<T> type,
                           @NonNull ContentRepository repository,
                           @NonNull IFilter[] filters,
                           @Nullable IDetailsProvider[] detailsProviders,
                           @Nullable ISubtitlesProvider subtitlesProvider) {
        super(type, repository, filters, detailsProviders, subtitlesProvider);
    }

    @NonNull
    @Override
    public Iterator<Observable<List<? extends VideoInfo>>> getContentIterator(@Nullable String keywords) {
        return new TvShowsIterator(keywords);
    }

    private final class TvShowsIterator extends ContentIterator {

        private TvShowsIterator(@Nullable String keywords) {
            super(keywords);
        }

        @NonNull
        @Override
        protected Observable<List<? extends VideoInfo>> getVideos(@Nullable String keywords, int page) {
            return repository.getTVShows(filters, keywords, page, type.getTypeToken()).map(new Function<List<T>, List<? extends VideoInfo>>() {

                @Override
                public List<? extends VideoInfo> apply(@io.reactivex.annotations.NonNull List<T> tvShows) throws Exception {
                    return tvShows;
                }
            });
        }
    }
}
