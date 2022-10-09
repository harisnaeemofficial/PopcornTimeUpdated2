package com.movieflix.mvp;

import android.support.annotation.NonNull;

public interface IViewRouter {

    boolean onShowView(@NonNull Class<?> view, Object... args);
}
