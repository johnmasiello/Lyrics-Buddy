package com.example.john.lyricsbuddy;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

/**
 * Created by john on 4/16/18.
 * ViewModel that holds LiveData that fetches song lyric items using Room
 */

public class SongLyricDetailItemViewModel extends ViewModel {
    private long oldId, newId;
    private boolean songLyricsDirty;
    /**
     * A helper variable to store newly created Song Lyrics Items before setting it to
     * mSongLyrics
     */
    private LyricDatabaseHelper.SongLyrics mNewSongLyrics;
    private MediatorLiveData<LyricDatabaseHelper.SongLyrics> mSongLyrics;
    private LyricDatabaseHelper.SongLyricsDao mSongLyricsDao;
    private WeakReference<SongLyricsListViewModel> mListViewModel;
    private final WeakReference<SongLyricDetailItemViewModel> self;

    public  static final long NO_ID     = -1;
    private static final long NEW_ID    = -2;

    public SongLyricDetailItemViewModel() {
        oldId = NO_ID;
        songLyricsDirty = false;
        self = new WeakReference<>(this);
    }

    public void setId(long songId) {
        if (songId != LyricDatabaseHelper.SongLyrics.UNSET_ID) {
            newId = songId;
        }
    }

    @Nullable
    public LyricDatabaseHelper.SongLyrics getSongLyricsInstantly() {
        return mSongLyrics != null ? mSongLyrics.getValue() : null;
    }

    /**
     *
     * @param songLyricsReceived The songLyrics that is received by the observer
     * @return songLyricsReceiver != null and songLyricsReceived.getUid() equals the id set with
     * {@link #setId(long)}
     */
    public boolean songLyricsIsLoaded(@Nullable LyricDatabaseHelper.SongLyrics songLyricsReceived) {
        return songLyricsReceived != null && songLyricsReceived.getUid() == newId;
    }

    /**
     * Precondition: {@link #setId(long)}
     */
    public LiveData<LyricDatabaseHelper.SongLyrics> getSongLyrics() {
        //noinspection ConstantConditions
        return getSongLyrics(newId);
    }

    private LiveData<LyricDatabaseHelper.SongLyrics> getSongLyrics(long songId) {
        boolean needsToFetch = false;
        newId = songId;

        if (mSongLyrics == null) {
            mSongLyrics = new MediatorLiveData<>();
            needsToFetch = true;
        }
        needsToFetch |= newId != oldId;

        if (needsToFetch) {
            WrappedEditText.resetUndoStack();
            if (oldId != NO_ID) {
                updateDatabase(true);
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

    public void setSongLyricsListViewModel(SongLyricsListViewModel lyricsListViewModel) {
        if (mListViewModel == null) {
            mListViewModel = new WeakReference<>(lyricsListViewModel);
        }
    }

    public void setSongLyricsDao(LyricDatabaseHelper.SongLyricsDao songLyricsDao) {
        if (mSongLyricsDao == null) {
            mSongLyricsDao = songLyricsDao;
        }
    }

    /**
     *
     * @param songLyrics The updated song lyrics
     * @return true iff songLyrics is dirty
     */
    public boolean setSongLyrics(@NonNull LyricDatabaseHelper.SongLyrics songLyrics) {
        if (mSongLyrics != null &&
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
        // Reset the undo stack so the content in the editTexts will be synchronized with the undo stack
        WrappedEditText.resetUndoStack();
        updateDatabase(false);
        if (mSongLyrics == null) {
            mSongLyrics = new MediatorLiveData<>();
        }
        // Create new song lyrics; do not put it into LiveData yet
        mNewSongLyrics = new LyricDatabaseHelper.SongLyrics();
        updateDatabaseWithNewSong();
    }

    /**
     *
     * ******************************************************************
     * Updates the SongLyrics entity in the database with the matching id
     * ******************************************************************
     */
    public void updateDatabase(boolean refreshListItems) {
        updateDatabase(refreshListItems, null);
    }

    public void updateDatabase(boolean refreshListItems, @Nullable Executor executor) {
        LyricDatabaseHelper.SongLyrics songLyrics = getSongLyricsInstantly();

        if (songLyricsDirty &&
                mSongLyricsDao != null &&
                songLyrics != null) {

            LyricDatabaseHelper.SongLyricAsyncTask task;

            task = new LyricDatabaseHelper.SongLyricAsyncTask(mSongLyricsDao,
                    LyricDatabaseHelper.SongLyricAsyncTask.UPDATE,
                    refreshListItems ? new RefreshListItemsOnUpdateCallback(mListViewModel) :
                            null);
            if (executor != null) {
                task.executeOnExecutor(executor, songLyrics);
            } else {
                task.execute(songLyrics);
            }

            // Update state
            songLyricsDirty = false;
        }
    }

    /**
     * <p>Precondition: mNewSongLyrics contains the new song lyrics object, with its id unset</p>
     * <br>
     * <p>Creates updates SongLyric Database with a new SongLyrics entity, which upon success
     * will have its id set</p>
     */
    private void updateDatabaseWithNewSong() {
        if (mSongLyricsDao != null) {

            LyricDatabaseHelper.SongLyricAsyncTask task;

            task = new LyricDatabaseHelper.SongLyricAsyncTask(mSongLyricsDao,
                    LyricDatabaseHelper.SongLyricAsyncTask.UPDATE,
                    new RefreshListItemsAndRefreshItemIdCallback(mListViewModel, self));
            task.execute(mNewSongLyrics);

            // Update state
            songLyricsDirty = false;
        }
    }

    static class RefreshListItemsOnUpdateCallback implements
            SongLyricAsyncTaskCallback {
        private final WeakReference<SongLyricsListViewModel> mListViewModel;

        public RefreshListItemsOnUpdateCallback(WeakReference<SongLyricsListViewModel> listViewModel) {
            mListViewModel = listViewModel;
        }

        @Override
        public void onSuccess(Object result) {
            SongLyricsListViewModel listViewModel = mListViewModel.get();

            if (listViewModel != null) {
                listViewModel.getLyricList(true);
            }
        }

        @Override
        public void onCancel(Object result) {
        }
    }

    /**
     * Used for creating new items and re-fetching the items list
     */
    private static class RefreshListItemsAndRefreshItemIdCallback extends
            RefreshListItemsOnUpdateCallback {

        private final WeakReference<SongLyricDetailItemViewModel> mDetailViewModel;

        public RefreshListItemsAndRefreshItemIdCallback(WeakReference<SongLyricsListViewModel> listViewModel,
                                                        WeakReference<SongLyricDetailItemViewModel> mDetailViewModel) {
            super(listViewModel);
            this.mDetailViewModel = mDetailViewModel;
        }

        @Override
        public void onSuccess(Object result) {
            SongLyricDetailItemViewModel itemViewModel = mDetailViewModel.get();

            if (result instanceof Long && itemViewModel != null) {
                itemViewModel.oldId = itemViewModel.newId = (Long) result;
                itemViewModel.mNewSongLyrics.setUid((Long)result);

                // Set the live data, to push through the changes
                itemViewModel.mSongLyrics.setValue(itemViewModel.mNewSongLyrics);
            }

            super.onSuccess(result);
        }

        /**
         * This will call itemViewModel.mSongLyrics.setValue, which will trigger the observers
         * to refresh the UI with a blank Song Lyrics Item, to maintain consistency despite
         * the case the new entry was not created in the database
         */
        @Override
        public void onCancel(Object result) {
            SongLyricDetailItemViewModel itemViewModel = mDetailViewModel.get();

            if (itemViewModel != null) {
                itemViewModel.oldId = itemViewModel.newId = SongLyricDetailItemViewModel.NEW_ID;

                // Set the live data, to push through the changes
                itemViewModel.mSongLyrics.setValue(itemViewModel.mNewSongLyrics);
            }
        }
    }
}