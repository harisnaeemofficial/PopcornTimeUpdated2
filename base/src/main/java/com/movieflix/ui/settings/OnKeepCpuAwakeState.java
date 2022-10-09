package com.movieflix.ui.settings;

import android.support.annotation.NonNull;

import com.movieflix.mvp.Presenter;
import com.movieflix.mvp.ViewState;

public final class OnKeepCpuAwakeState extends ViewState<ISettingsView> {

    private Boolean keepCpuAwake;

    public OnKeepCpuAwakeState(@NonNull Presenter<ISettingsView> presenter, @NonNull Boolean keepCpuAwake) {
        super(presenter);
        this.keepCpuAwake = keepCpuAwake;
    }

    public void apply(@NonNull Boolean downloadsClearCacheFolder) {
        this.keepCpuAwake = downloadsClearCacheFolder;
        apply();
    }

    @Override
    public void apply(@NonNull ISettingsView view) {
        view.onKeepCPUawake(keepCpuAwake);

    }
}
