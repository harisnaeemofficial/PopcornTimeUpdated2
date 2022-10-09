package com.movieflix.mvp;

import android.support.annotation.NonNull;

public interface IViewState<T> {

    void apply(@NonNull T view);
}