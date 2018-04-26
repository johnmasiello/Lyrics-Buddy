package com.example.john.lyricsbuddy;

/**
 * Callback for when the async task finishes
 */
public interface SongLyricAsyncTaskCallback {
    void onSuccess(Object result);

    void onCancel(Object result);
}
