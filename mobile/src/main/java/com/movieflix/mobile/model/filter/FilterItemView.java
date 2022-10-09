package com.movieflix.mobile.model.filter;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.movieflix.model.filter.IFilterItem;

public final class FilterItemView implements IFilterItem {

    private final int viewName;
    private final IFilterItem filterItem;

    public FilterItemView(@StringRes int viewName, @NonNull IFilterItem filterItem) {
        this.viewName = viewName;
        this.filterItem = filterItem;
    }

    @NonNull
    @Override
    public String getValue() {
        return filterItem.getValue();
    }

    @StringRes
    public int getViewName() {
        return viewName;
    }
}
