package com.example.john.lyricsbuddy;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by john on 4/16/18.
 * ViewModel that holds LiveData that fetches song lyric items using Room
 */

public class SongLyricDetailItemViewModel extends ViewModel {
    private long oldId, newId;
    private MediatorLiveData<LyricDatabaseHelper.SongLyrics> songLyrics;
    private MutableLiveData<LyricDatabaseHelper.SongLyricsDao> mSongLyricsDao;

    private static final long NO_ID = -1;

    public SongLyricDetailItemViewModel() {
        oldId = NO_ID;
    }

    public void setId(long songId) {
        newId = songId;
    }

    public LiveData<LyricDatabaseHelper.SongLyrics> getSongLyrics() {
        //noinspection ConstantConditions
        return getSongLyrics(newId);
    }

    public LiveData<LyricDatabaseHelper.SongLyrics> getSongLyrics(long songId) {
        boolean needsToQuery = false;
        newId = songId;

        if (songLyrics == null) {
            songLyrics = new MediatorLiveData<>();
            needsToQuery = true;
        }
        if (newId != oldId) {
            oldId = newId;
            needsToQuery = true;
        }

        if (needsToQuery) {
            //noinspection ConstantConditions
            if (newId == NO_ID) {
                songLyrics.setValue(LyricDatabaseHelper.SongLyrics.BLANK);
            } else {
                @SuppressWarnings("ConstantConditions")
                // Query for the song lyrics
                final LiveData<LyricDatabaseHelper.SongLyrics> result = mSongLyricsDao.getValue().fetchSongLyric(newId);
                songLyrics.addSource(result, new Observer<LyricDatabaseHelper.SongLyrics>() {
                    @Override
                    public void onChanged(@Nullable LyricDatabaseHelper.SongLyrics songLyrics2) {
                        songLyrics.setValue(songLyrics2);
                        songLyrics.removeSource(result);
                    }
                });
            }
        }
        return songLyrics;
    }

    public void setSongLyricsDao(LyricDatabaseHelper.SongLyricsDao songLyricsDao) {
        if (mSongLyricsDao == null) {
            mSongLyricsDao = new MutableLiveData<>();
            mSongLyricsDao.setValue(songLyricsDao);
        } else {
            Log.d("Database", "song lyric dao already set");
        }
    }
}
