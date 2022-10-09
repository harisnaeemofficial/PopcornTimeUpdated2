package com.movieflix.mvp;

import android.support.annotation.NonNull;

public interface IPresenter<T> {

    void attach(@NonNull T view);

    void detach(@NonNull T view);
}
