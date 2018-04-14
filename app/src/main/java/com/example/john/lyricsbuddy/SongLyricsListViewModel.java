package com.example.john.lyricsbuddy;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import static com.example.john.lyricsbuddy.LyricDatabaseHelper.*;

/**
 * Created by john on 4/14/18.
 * ViewModel that holds LiveData that it fetches using Room
 */

public class SongLyricsListViewModel extends ViewModel {
    private LiveData<List<SongLyricsListItem>> songLyricListItems;
    private MutableLiveData<Integer> sortOrder;

    public static final int ORDER_NATURAL   = 0;
    public static final int ORDER_ARTIST    = 1;
    public static final int ORDER_ALBUM     = 2;
    public static final int ORDER_TRACK     = 3;

    public SongLyricsListViewModel() {
        sortOrder = new MutableLiveData<>();
        sortOrder.setValue(ORDER_NATURAL);
    }

    public LiveData<List<SongLyricsListItem>> fetchSongLyricsListViewModel(SongLyricsDao songLyricsDao) {
        return fetchSongLyricsListViewModel(songLyricsDao, sortOrder.getValue());
    }

    public LiveData<List<SongLyricsListItem>> fetchSongLyricsListViewModel(SongLyricsDao songLyricsDao,
                                   Integer sortOrder) {

        if (this.sortOrder.getValue() != null &&
            this.sortOrder.getValue().equals(sortOrder)) {
            if (songLyricListItems != null) {
                return songLyricListItems;
            }
        } else {
            this.sortOrder.setValue(sortOrder);
        }
        switch (sortOrder) {
            case ORDER_ARTIST:
                this.songLyricListItems = songLyricsDao.fetchListItems_Artist();
                break;

            case ORDER_ALBUM:
                this.songLyricListItems = songLyricsDao.fetchListItems_Album();
                break;

            case ORDER_TRACK:
                this.songLyricListItems = songLyricsDao.fetchListItems_Track();
                break;

            case ORDER_NATURAL:
            default:
                this.songLyricListItems = songLyricsDao.fetchListItems_NaturalOrder();
        }
        return songLyricListItems;
    }
}
