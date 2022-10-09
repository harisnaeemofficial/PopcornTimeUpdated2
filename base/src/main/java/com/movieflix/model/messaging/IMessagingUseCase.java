package com.movieflix.model.messaging;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public interface IMessagingUseCase {

    interface Observer {

        void onShowMessagingDialog(@NonNull IMessagingData data);
    }

    interface NotificationObserver {

        void onShowMessagingNotification(@NonNull IMessagingNotificationData data);
    }

    IMessagingData getData();

    void subscribe(@NonNull Observer observer);

    void unsubscribe(@NonNull Observer observer);

    void subscribe(@NonNull NotificationObserver observer);

    void show(@Nullable IMessagingData data);
}
