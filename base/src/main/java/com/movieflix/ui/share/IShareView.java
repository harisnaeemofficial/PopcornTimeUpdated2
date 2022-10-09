package com.movieflix.ui.share;

import android.support.annotation.NonNull;

import com.movieflix.model.share.IShareData;

public interface IShareView {

    void onShowShareData(@NonNull IShareData shareData);
}
