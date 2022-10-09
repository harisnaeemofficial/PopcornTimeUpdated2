package com.movieflix.base.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.movieflix.base.IPopcornApplication;

public class NetworkUtil {

    public static boolean hasAvailableConnection(Context context) {
        if (((IPopcornApplication) context.getApplicationContext()).getSettingsUseCase().isDownloadsWifiOnly()) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting() && ConnectivityManager.TYPE_WIFI == activeNetwork.getType();
        }
        return true;
    }
}