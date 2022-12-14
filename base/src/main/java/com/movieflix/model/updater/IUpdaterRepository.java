package com.movieflix.model.updater;

import android.support.annotation.NonNull;

import io.reactivex.Observable;

public interface IUpdaterRepository {

    @NonNull
    Observable<Update> getUpdate(@NonNull String url);
}
