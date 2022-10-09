package com.movieflix.ui.content;

import android.support.annotation.NonNull;

import com.movieflix.model.content.IContentProvider;
import com.movieflix.model.filter.IFilter;

public interface IContentProviderView {

    void onContentProvider(@NonNull IContentProvider[] contentProviders, @NonNull IContentProvider contentProvider);

    void onContentFilterChecked(@NonNull IFilter filter);
}
