package com.example.john.lyricsbuddy;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.ListUpdateCallback;
import android.util.Log;

/**
 * Created by john on 4/16/18.
 * ViewModel that holds LiveData that fetches song lyric items using Room
 */

public class SongLyricDetailItemViewModel extends ViewModel {
    private long oldId, newId;
    private boolean songLyricsDirty;
    private MediatorLiveData<LyricDatabaseHelper.SongLyrics> mSongLyrics;
    private LyricDatabaseHelper.SongLyricsDao mSongLyricsDao;

    public  static final long NO_ID     = -1;
    private static final long NEW_ID    = -2;

    public SongLyricDetailItemViewModel() {
        oldId = NO_ID;
        songLyricsDirty = false;
    }

    public void setId(long songId) {
        if (songId != LyricDatabaseHelper.SongLyrics.UNSET_ID)
            newId = songId;
    }

    @Nullable
    public LyricDatabaseHelper.SongLyrics getSongLyricsInstantly() {
        return mSongLyrics != null ? mSongLyrics.getValue() : null;
    }

    /**
     * Precondition: {@link #setId(long)}
     */
    public LiveData<LyricDatabaseHelper.SongLyrics> getSongLyrics() {
        //noinspection ConstantConditions
        return getSongLyrics(newId);
    }

    private LiveData<LyricDatabaseHelper.SongLyrics> getSongLyrics(long songId) {
        boolean needsToUpdate = false;
        newId = songId;

        if (mSongLyrics == null) {
            mSongLyrics = new MediatorLiveData<>();
            needsToUpdate = true;
        }
        needsToUpdate |= newId != oldId;

        if (needsToUpdate) {
            if (oldId != NO_ID) {
                updateDatabase();
            }

            //noinspection ConstantConditions
            if (newId == NO_ID) {
                // Create a blank song lyric
                mSongLyrics.setValue(new LyricDatabaseHelper.SongLyrics());
            } else if (mSongLyricsDao != null) {
                // Query for the song lyrics
                final LiveData<LyricDatabaseHelper.SongLyrics> result = mSongLyricsDao.fetchSongLyric(newId);
                mSongLyrics.addSource(result, new Observer<LyricDatabaseHelper.SongLyrics>() {
                    @Override
                    public void onChanged(@Nullable LyricDatabaseHelper.SongLyrics songLyrics2) {
                        mSongLyrics.setValue(songLyrics2);
                        mSongLyrics.removeSource(result);
                    }
                });
            }
        }
        oldId = newId;
        return mSongLyrics;
    }

    public void setSongLyricsDao(LyricDatabaseHelper.SongLyricsDao songLyricsDao) {
        if (mSongLyricsDao == null) {
            mSongLyricsDao = songLyricsDao;
        } else {
            Log.d("Database", "song lyric dao already set");
        }
    }

    /**
     *
     * @param songLyrics The updated song lyrics
     * @return true iff songLyrics is dirty
     */
    public boolean setSongLyrics(@NonNull LyricDatabaseHelper.SongLyrics songLyrics) {
        if (mSongLyrics != null &&
                !songLyrics.isBlankType1() &&
                !songLyrics.equals(mSongLyrics.getValue())) {

            mSongLyrics.setValue(songLyrics);
            songLyricsDirty = true;
            return true;
        }
        return false;
    }

    /**
     * <p>
     *     Precondition: {@link #setSongLyricsDao(LyricDatabaseHelper.SongLyricsDao)}
     * </p>
     * <br>
     * The Song Lyrics are dirty, have been changed,
     * implies UPDATE database with songLyrics in mSongLyrics.getValue()
     * <br>
     * Then in either case set a new blank song lyrics to the view members
     */
    public void newSongLyrics() {
        updateDatabase();
        if (mSongLyrics == null) {
            mSongLyrics = new MediatorLiveData<>();
        }
        mSongLyrics.setValue(new LyricDatabaseHelper.SongLyrics());
        // Indicate that songLyrics goes in a new record
        newId = oldId = NEW_ID;
    }

    private void updateDatabase() {
        updateDatabase(false);
    }

    private void updateDatabase(boolean immediate) {
        LyricDatabaseHelper.SongLyrics songLyrics = getSongLyricsInstantly();

        if (songLyricsDirty &&
                mSongLyricsDao != null &&
                songLyrics != null && !songLyrics.isBlankType1()) {

            if (!immediate) {
                LyricDatabaseHelper.SongLyricAsyncTask task;
                task = new LyricDatabaseHelper.SongLyricAsyncTask(mSongLyricsDao,
                        LyricDatabaseHelper.SongLyricAsyncTask.UPDATE);
                task.execute(songLyrics);
            } else {
                // Update on the main thread
                mSongLyricsDao.update(songLyrics);
            }

            // Update state
            songLyricsDirty = false;
        }
    }

    public void updateDatabaseImmediate() {
        updateDatabase(true);
    }
}