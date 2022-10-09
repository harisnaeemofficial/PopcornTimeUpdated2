package com.movieflix.base.prefs;

import android.content.Context;

public class PopcornPrefs extends BasePrefs {

    public static final String UPDATE_APK_PATH = "update-apk-path";
    public static final String LAST_TORRENT = "last-torrent";

    public PopcornPrefs(Context context) {
        super(context);
    }

    @Override
    protected String getPrefsName() {
        return "MovieFlixPreferences";
    }
}