package com.movieflix.base.prefs;

import android.content.Context;

public final class Prefs {

    private final static Prefs INSTANCE = new Prefs();

    private PopcornPrefs movieflixPrefs;

    private Prefs() {

    }

    public static void init(Context context) {
        INSTANCE.movieflixPrefs = new PopcornPrefs(context);
    }

    public static PopcornPrefs getPopcornPrefs() {
        return INSTANCE.movieflixPrefs;
    }
}