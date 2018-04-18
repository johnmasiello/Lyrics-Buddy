package com.example.john.lyricsbuddy;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.Update;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.List;

/**
 * Created by john on 3/21/18.
 *
 * Singleton to manage the database of lyrics
 */

public class LyricDatabaseHelper {

    private static final String DATABASE_NAME = "lyricDatabase";

    @Entity(indices = {@Index("album"), @Index("track_title"), @Index("artist")})
    public static class SongLyrics {
        @PrimaryKey(autoGenerate = true)
        private long uid;

        @ColumnInfo(name = "album")
        private String album;

        @ColumnInfo(name = "track_title")
        private String trackTitle;

        @ColumnInfo(name = "artist")
                private String artist;

        @ColumnInfo(name = "lyrics")
        private String lyrics;

        public SongLyrics() {
            // Primary key
            this.uid = 0; // This will be treated as not-set by the auto generator
        }

        SongLyrics(String trackTitle, String album, String artist, String lyrics) {
            // Primary key
            this.uid = 0; // This will be treated as not-set by the auto generator

            this.album = album;
            this.trackTitle = trackTitle;
            this.artist = artist;
            this.lyrics = lyrics;
        }

        boolean isBlankType1() {
            return (album == null || album.equals("")) &&
                    (artist == null || artist.equals("")) &&
                    (trackTitle == null || trackTitle.equals("")) &&
                    (lyrics == null || lyrics.equals(""));
        }

        boolean isBlankType2() {
            return uid == 0 && isBlankType1();
        }

        long getUid() {
            return uid;
        }

        void setUid(long uid) {
            this.uid = uid;
        }

        String getAlbum() {
            return getPrintableString(album);
        }

        void setAlbum(String album) {
            this.album = album;
        }

        String getTrackTitle() {
            return getPrintableString(trackTitle);
        }

        void setTrackTitle(String trackTitle) {
            this.trackTitle = trackTitle;
        }

        public String getArtist() {
            return getPrintableString(artist);
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public String getLyrics() {
            return getPrintableString(lyrics);
        }

        public void setLyrics(String lyrics) {
            this.lyrics = lyrics;
        }

        private String getPrintableString(@Nullable String str) {
            return str != null ? str : "";
        }

        @Override
        public String toString() {
            return String.valueOf(uid)      +
             "\t\t track: "+getTrackTitle() +
             "\t\t album: "+getAlbum()      +
             "\t\t artist: "+getArtist()    +
             "\t\t lyrics: "+getLyrics();
        }

        /**
         * An expensive equals that is especially useful for determining
         * whether an instance is 'dirty'
         */
        @Override
        public boolean equals(Object obj) {
            return obj instanceof SongLyrics &&
                getUid() == ((SongLyrics) obj).getUid() &&
                getAlbum().equals(((SongLyrics) obj).getAlbum()) &&
                getArtist().equals(((SongLyrics) obj).getArtist()) &&
                getTrackTitle().equals(((SongLyrics) obj).getTrackTitle()) &&
                getLyrics().equals(((SongLyrics) obj).getLyrics());
        }
    }

    public static class SongLyricsListItem {
        @ColumnInfo(name = "uid")
        private long uid;

        @ColumnInfo(name = "album")
        private String album;

        @ColumnInfo(name = "track_title")
        private String trackTitle;

        @ColumnInfo(name = "artist")
        private String artist;

        public long getUid() {
            return uid;
        }

        public void setUid(long uid) {
            this.uid = uid;
        }

        public String getAlbum() {
            return album;
        }

        public void setAlbum(String album) {
            this.album = album;
        }

        public String getTrackTitle() {
            return trackTitle;
        }

        public void setTrackTitle(String trackTitle) {
            this.trackTitle = trackTitle;
        }

        public String getArtist() {
            return artist;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object obj) {
            SongLyricsListItem l2 = (SongLyricsListItem) obj;
            if (album != null) {
                if (!album.equals(l2.album)) {
                    return false;
                }
            } else if (l2.album != null) {
                return false;
            }

            if (artist != null) {
                if (!artist.equals(l2.artist)) {
                    return false;
                }
            } else if (l2.artist != null) {
                return false;
            }

            if (trackTitle != null) {
                if (!trackTitle.equals(l2.trackTitle)) {
                    return false;
                }
            } else if (l2.trackTitle != null) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return String.valueOf(uid)      +
                    "\t\t track: "+String.valueOf(trackTitle) +
                    "\t\t album: "+String.valueOf(album)      +
                    "\t\t artist: "+String.valueOf(artist);
        }
    }

    @Dao
    public interface SongLyricsDao {
        /*
        Data Adapters
         */

        @Query("SELECT uid, album, track_title, artist FROM SongLyrics " +
                "ORDER BY uid ASC")
        LiveData<List<SongLyricsListItem>> fetchListItems_NaturalOrder();

        @Query("SELECT uid, album, track_title, artist FROM SongLyrics " +
                "ORDER BY artist ASC")
        LiveData<List<SongLyricsListItem>> fetchListItems_Artist();

        @Query("SELECT uid, album, track_title, artist FROM SongLyrics " +
                "ORDER BY album ASC")
        LiveData<List<SongLyricsListItem>> fetchListItems_Album();

        @Query("SELECT uid, album, track_title, artist FROM SongLyrics " +
                "ORDER BY track_title ASC")
        LiveData<List<SongLyricsListItem>> fetchListItems_Track();

        /*
        Data detail view
         */
        @Query("SELECT * FROM SongLyrics WHERE uid LIKE :uid LIMIT 1")
        LiveData<SongLyrics> fetchSongLyric(Long uid);

        /*
         Searches
         */
        @Query("SELECT uid, album, track_title, artist FROM SongLyrics WHERE artist LIKE :artist")
        LiveData<List<SongLyricsListItem>> findByArtist(String artist);

        @Query("SELECT uid, album, track_title, artist FROM SongLyrics WHERE album LIKE :album")
        LiveData<List<SongLyricsListItem>> findByAlbum(String album);

        @Query("SELECT uid, album, track_title, artist FROM SongLyrics WHERE track_title LIKE :track")
        LiveData<List<SongLyricsListItem>> findByTrackTitle(String track);

        /*
        Population
         */
        @Insert
        void insertAll(SongLyrics... songLyrics);

        @Delete
        void delete(SongLyrics songLyrics);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void update(SongLyrics songLyrics);
    }

    @Database(entities = {SongLyrics.class}, version = 6)
    public abstract static class SongLyricDatabase extends RoomDatabase {
        public abstract SongLyricsDao songLyricsDao();
    }

    private static SongLyricDatabase songLyricDatabase;

    static SongLyricDatabase getSongLyricDatabase(final Context context) {
        if (songLyricDatabase == null) {
            songLyricDatabase = Room.databaseBuilder(context,
                    SongLyricDatabase.class, DATABASE_NAME)
                    .build();
        }
        return songLyricDatabase;
    }

    static boolean doesDatabaseExist(Context context) {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        return dbFile.exists();
    }

    static void writeInitialRecords(final Context context) {
        // Populate the first-time data
        SongLyricAsyncTask task = new SongLyricAsyncTask(
                getSongLyricDatabase(context).songLyricsDao(),
                SongLyricAsyncTask.INSERT_ALL);
        task.execute(new SongLyrics(context.getString(R.string.ballgame_title),
                        null,
                        context.getString(R.string.ballgame_artist),
                        context.getString(R.string.ballgame_lyrics)),

                new SongLyrics(context.getString(R.string.jellyRollBlues_title),
                        context.getString(R.string.jellyRollBlues_album),
                        context.getString(R.string.jellyRollBlues_artist),
                        context.getString(R.string.jellyRollBlues_lyrics)),

                new SongLyrics(context.getString(R.string.sweetChariot_title),
                        null,
                        context.getString(R.string.sweetChariot_artist),
                        context.getString(R.string.sweetChariot_lyrics))
        );
    }

    static class SongLyricAsyncTask extends AsyncTask<SongLyrics, Void, Void> {
        private SongLyricsDao mSongLyricDao;
        private final int mCommand;

        public static final int INSERT_ALL = 0;
        public static final int DELETE     = 1;
        public static final int UPDATE     = 2;

        public SongLyricAsyncTask(SongLyricsDao songLyricsDao, int command) {
            mSongLyricDao = songLyricsDao;
            mCommand = command;
        }

        @Override
        protected Void doInBackground(SongLyrics... songLyrics) {
            if (mSongLyricDao != null) {
                switch (mCommand) {
                    case INSERT_ALL:
                        mSongLyricDao.insertAll(songLyrics);
                        break;

                    case DELETE:
                        mSongLyricDao.delete(songLyrics[0]);
                        break;

                    case UPDATE:
                        mSongLyricDao.update(songLyrics[0]);
                        break;
                }
            }
            return null;
        }
    }
}
