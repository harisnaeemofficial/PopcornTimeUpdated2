package com.movieflix.base;

import android.support.annotation.NonNull;

import com.movieflix.model.config.IConfigUseCase;
import com.movieflix.model.messaging.IMessagingUseCase;
import com.movieflix.model.settings.ISettingsUseCase;

public interface IPopcornApplication {

    @NonNull
    IMessagingUseCase getMessagingUseCase();

    @NonNull
    IConfigUseCase getConfigUseCase();

    @NonNull
    ISettingsUseCase getSettingsUseCase();
}
