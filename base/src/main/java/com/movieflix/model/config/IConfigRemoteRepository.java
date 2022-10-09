package com.movieflix.model.config;

import android.support.annotation.NonNull;

import io.reactivex.Observable;

public interface IConfigRemoteRepository {

    @NonNull
    Observable<Config> getConfig(@NonNull String url);
}
