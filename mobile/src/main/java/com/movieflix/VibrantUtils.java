package com.movieflix;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

public final class VibrantUtils {

    private static int accentColor;

    private VibrantUtils() {
    }

    public static int getAccentColor() {
        return accentColor;
    }

    public static void setAccentColor(Bitmap bitmap, final int defaultColor, OnAccentColorReady listener) {
        retrieveAccentColor(bitmap, defaultColor, accentColor -> {
            VibrantUtils.accentColor = accentColor;
            listener.onReady(accentColor);
        });
    }

    public static void setAccentColor(int color) {
        accentColor = color;
    }

    public static void retrieveAccentColor(Bitmap bitmap, final int defaultColor, OnAccentColorReady listener) {
        Palette.from(bitmap).generate(palette -> {
            int accentColor = palette.getVibrantColor(defaultColor);
            if (accentColor == defaultColor) {
                accentColor = palette.getMutedColor(defaultColor);
            }
            listener.onReady(accentColor);
        });
    }

    public interface OnAccentColorReady {
        void onReady(int accentColor);
    }
}
