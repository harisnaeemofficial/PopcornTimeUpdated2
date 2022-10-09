package com.player.subtitles.loader;

import android.support.annotation.NonNull;

import com.player.subtitles.SubtitlesException;
import com.player.subtitles.format.SRTFormat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class UrlSubtitlesLoader extends SubtitlesLoader<URL> {

    @Override
    public String load(@NonNull URL url, @NonNull File saveFile, String langName) throws IOException, SubtitlesException, InterruptedException {
        String fileName = "";

        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(5000);
        connection.connect();

        if (Thread.interrupted()) {
            throw new InterruptedException("Subtitles loading was interrupted");
        }

        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(connection.getInputStream()));
        ZipEntry zipEntry;
        boolean isSaved = false;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            int index = zipEntry.getName().lastIndexOf('.') + 1;
            String ext = zipEntry.getName().substring(index).toLowerCase();
            fileName = zipEntry.getName().substring(0, index - 1);
            if (SRTFormat.EXTENSION.equals(ext)) {
                if (langName != null) {
                    new File(saveFile.getParent() + "/subs-"+langName).mkdir();
                    File path = new File(saveFile.getParent() + "/subs-"+langName+"/" + fileName+"."+SRTFormat.EXTENSION);
                    save(zipInputStream, path, new SRTFormat());
                } else {
                    save(zipInputStream, saveFile, new SRTFormat());
                }
                isSaved = true;
                break;
            }
        }
        zipInputStream.closeEntry();
        zipInputStream.close();

        if (!isSaved) {
            throw new SubtitlesException("Subtitles not loaded from: " + url);
        }
        return fileName;
    }
}