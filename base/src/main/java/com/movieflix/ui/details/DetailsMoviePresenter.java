package com.movieflix.ui.details;

import android.support.annotation.NonNull;

import com.movieflix.base.model.video.info.MoviesInfo;
import com.movieflix.model.content.IContentUseCase;
import com.movieflix.model.details.IDetailsUseCase;

public final class DetailsMoviePresenter extends DetailsPresenter<MoviesInfo, IDetailsMovieView> implements IDetailsMoviePresenter {

    public DetailsMoviePresenter(@NonNull IContentUseCase contentUseCase, @NonNull IDetailsUseCase detailsUseCase) {
        super(contentUseCase, detailsUseCase);
    }
}
