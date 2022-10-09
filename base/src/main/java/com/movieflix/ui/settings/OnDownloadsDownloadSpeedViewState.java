package com.movieflix.ui.settings;

import android.support.annotation.NonNull;

import com.movieflix.mvp.Presenter;
import com.movieflix.mvp.ViewState;

public final class OnDownloadsDownloadSpeedViewState extends ViewState<ISettingsView> {

    private Integer downloadsDownloadSpeed;

    public OnDownloadsDownloadSpeedViewState(@NonNull Presenter<ISettingsView> presenter, @NonNull Integer downloadsDownloadSpeed) {
        super(presenter);
        this.downloadsDownloadSpeed = downloadsDownloadSpeed;
    }

    public void apply(@NonNull Integer downloadsDownloadSpeed) {
        this.downloadsDownloadSpeed = downloadsDownloadSpeed;
        apply();
    }

    @Override
    public void apply(@NonNull ISettingsView view) {
        view.onDownloadsDownloadSpeed(downloadsDownloadSpeed);
    }
}
