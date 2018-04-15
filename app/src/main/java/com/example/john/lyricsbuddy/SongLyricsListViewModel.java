package com.example.john.lyricsbuddy;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import static com.example.john.lyricsbuddy.LyricDatabaseHelper.*;

/**
 * Created by john on 4/14/18.
 * ViewModel that holds LiveData that fetches using Room
 */

public class SongLyricsListViewModel extends ViewModel {
    private MutableLiveData<SongLyricsDao> mSongLyricsDao;
    private MediatorLiveData<List<SongLyricsListItem>> mSongLyricListItems;
    private MutableLiveData<Integer> mSortOrder;

    public static final int ORDER_NATURAL   = 0;
    public static final int ORDER_ARTIST    = 1;
    public static final int ORDER_ALBUM     = 2;
    public static final int ORDER_TRACK     = 3;

    public SongLyricsListViewModel() {
        if (mSortOrder == null) {
            mSortOrder = new MutableLiveData<>();
            mSortOrder.setValue(ORDER_NATURAL);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public LiveData<List<SongLyricsListItem>> getLyricList() {
        return getLyricList(mSortOrder.getValue());
    }

    @SuppressWarnings("ConstantConditions")
    public LiveData<List<SongLyricsListItem>> getLyricList(int sortOrder) {
        boolean needsToFetchItems = false;

        if (mSongLyricListItems == null) {
            mSongLyricListItems = new MediatorLiveData<>();
            needsToFetchItems = true;
        }
        if (!mSortOrder.getValue().equals(sortOrder)) {
            mSortOrder.setValue(sortOrder);
            needsToFetchItems = true;
        }
        if (needsToFetchItems) {
            Log.d("Database", "Need to fetch LyricListItems as LiveData");
            loadSongLyricListItems();
        } else {
            Log.d("Database", "Song lyric list items already populated");
        }
        return mSongLyricListItems;
    }

    public void setSongLyricsDao(SongLyricsDao songLyricsDao) {
        if (mSongLyricsDao == null) {
            mSongLyricsDao = new MutableLiveData<>();
            mSongLyricsDao.setValue(songLyricsDao);
        } else {
            Log.d("Database", "song lyric dao already set");
        }
    }

    /**
     * Asynchronously load {@literal List<SongLyricListItems>} as LiveData stream
     */
    @SuppressWarnings("ConstantConditions")
    private void loadSongLyricListItems() {
        SongLyricsDao songLyricsDao = mSongLyricsDao.getValue();

        if (songLyricsDao == null) {
            Log.d("Database", "Unable to fetch data from database; song lyrics dao unset");
            throw new IllegalStateException("SongLyricDao unset");
        }
        LiveData<List<SongLyricsListItem>> query;
        switch (mSortOrder.getValue()) {
            case ORDER_ARTIST:
                query = songLyricsDao.fetchListItems_Artist();
                break;

            case ORDER_ALBUM:
                query = songLyricsDao.fetchListItems_Album();
                break;

            case ORDER_TRACK:
                query = songLyricsDao.fetchListItems_Track();
                break;

            case ORDER_NATURAL:
            default:
                query = songLyricsDao.fetchListItems_NaturalOrder();
        }

        final LiveData<List<SongLyricsListItem>> query_sorted =
                query;
        mSongLyricListItems.addSource(query_sorted,
                new Observer<List<SongLyricsListItem>>() {
                    @Override
                    public void onChanged(@Nullable List<SongLyricsListItem> songLyricsListItems) {
                        mSongLyricListItems.removeSource(query_sorted);
                        mSongLyricListItems.setValue(songLyricsListItems);

                        List<SongLyricsListItem> list = mSongLyricListItems.getValue();

                        if (list != null) {
                            for (SongLyricsListItem item : list) {
                                Log.d("Database", String.valueOf(item));
                            }
                        }
                    }
                });
    }
}
