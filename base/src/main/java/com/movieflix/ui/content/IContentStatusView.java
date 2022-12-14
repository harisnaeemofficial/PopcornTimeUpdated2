package com.movieflix.ui.content;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.movieflix.model.content.IContentStatus;

public interface IContentStatusView {

    void onKeywords(@Nullable String keywords);

    void onContentStatus(@NonNull IContentStatus contentStatus);
}
