package com.movieflix;

import android.support.annotation.NonNull;

import com.movieflix.model.content.IContentUseCase;
import com.movieflix.model.details.IDetailsUseCase;

public interface IUseCaseManager {

    @NonNull
    IContentUseCase getContentUseCase();

    @NonNull
    IDetailsUseCase getDetailsUseCase();
}
