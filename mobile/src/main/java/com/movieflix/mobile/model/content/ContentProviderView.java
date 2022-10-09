package com.movieflix.mobile.model.content;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import com.movieflix.base.model.video.info.VideoInfo;
import com.movieflix.model.content.IContentProvider;
import com.movieflix.model.content.IDetailsProvider;
import com.movieflix.model.content.ISubtitlesProvider;
import com.movieflix.model.filter.IFilter;

public final class ContentProviderView implements IContentProvider {

    private final int viewCategoryIcon;
    private final int viewCategoryName;
    private final int viewName;
    private final IContentProvider contentProvider;

    public ContentProviderView(@DrawableRes int viewCategoryIcon,
                               @StringRes int viewCategoryName,
                               @StringRes int viewName,
                               @NonNull IContentProvider contentProvider) {
        this.viewCategoryIcon = viewCategoryIcon;
        this.viewCategoryName = viewCategoryName;
        this.viewName = viewName;
        this.contentProvider = contentProvider;
    }

    @DrawableRes
    public int getViewCategoryIcon() {
        return viewCategoryIcon;
    }

    @StringRes
    public int getViewCategoryName() {
        return viewCategoryName;
    }

    @StringRes
    public int getViewName() {
        return viewName;
    }

    @NonNull
    @Override
    public String getType() {
        return contentProvider.getType();
    }

    @NonNull
    @Override
    public IFilter[] getFilters() {
        return contentProvider.getFilters();
    }

    @NonNull
    @Override
    public Iterator<Observable<List<? extends VideoInfo>>> getContentIterator(@Nullable String keywords) {
        return contentProvider.getContentIterator(keywords);
    }

    @Nullable
    @Override
    public IDetailsProvider[] getDetailsProviders() {
        return contentProvider.getDetailsProviders();
    }

    @Nullable
    @Override
    public ISubtitlesProvider getSubtitlesProvider() {
        return contentProvider.getSubtitlesProvider();
    }
}
