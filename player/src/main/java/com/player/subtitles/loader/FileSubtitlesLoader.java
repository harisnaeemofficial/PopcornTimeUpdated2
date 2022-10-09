package com.player.subtitles.loader;

import android.support.annotation.NonNull;

import com.player.subtitles.SubtitlesException;
import com.player.subtitles.format.SRTFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class FileSubtitlesLoader extends SubtitlesLoader<File> {

    @Override
    public String load(@NonNull File file, @NonNull File saveFile, String dummy) throws IOException, SubtitlesException, InterruptedException {
        int index = file.getAbsolutePath().lastIndexOf('.') + 1;
        String ext = file.getAbsolutePath().substring(index).toLowerCase();
        if (SRTFormat.EXTENSION.equals(ext)) {
            InputStream inputStream = new FileInputStream(file);
            save(inputStream, saveFile, new SRTFormat());
            inputStream.close();
        } else {
            throw new SubtitlesException("Not supported subtitles file: " + file.getAbsolutePath());
        }
        return file.getAbsolutePath().substring(0, index - 1);
    }
}