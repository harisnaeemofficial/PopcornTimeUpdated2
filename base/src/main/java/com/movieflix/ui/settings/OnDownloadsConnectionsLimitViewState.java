package com.movieflix.ui.settings;

import android.support.annotation.NonNull;

import com.movieflix.mvp.Presenter;
import com.movieflix.mvp.ViewState;

public final class OnDownloadsConnectionsLimitViewState extends ViewState<ISettingsView> {

    private Integer downloadsConnectionsLimit;

    public OnDownloadsConnectionsLimitViewState(@NonNull Presenter<ISettingsView> presenter, @NonNull Integer downloadsConnectionsLimit) {
        super(presenter);
        this.downloadsConnectionsLimit = downloadsConnectionsLimit;
    }

    public void apply(@NonNull Integer downloadsConnectionsLimit) {
        this.downloadsConnectionsLimit = downloadsConnectionsLimit;
        apply();
    }

    @Override
    public void apply(@NonNull ISettingsView view) {
        view.onDownloadsConnectionsLimit(downloadsConnectionsLimit);
    }
}
