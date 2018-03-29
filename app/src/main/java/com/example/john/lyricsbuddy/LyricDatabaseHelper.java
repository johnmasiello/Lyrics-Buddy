package com.example.john.lyricsbuddy;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
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

    @Entity
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

        SongLyrics(String trackTitle, String album, String artist, String lyrics) {
            // Primary key
            this.uid = 0; // This will be treated as not-set by the auto generator

            this.album = album;
            this.trackTitle = trackTitle;
            this.artist = artist;
            this.lyrics = lyrics;
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


    }

    @Dao
    public interface SongLyricsDao {
        @Query("SELECT * FROM SongLyrics")
        List<SongLyrics> getAll();

        /**
         *
         * @return The first user in the database. Used to determine if database has any records
         */
        @Query("SELECT * FROM SongLyrics LIMIT 1 OFFSET 0")
        SongLyrics getFirstUser();

        @Query("SELECT * FROM SongLyrics WHERE uid IN (:songLyricsIds)")
        List<SongLyrics> loadAllByIds(int[] songLyricsIds);

        @Query("SELECT * FROM SongLyrics WHERE artist LIKE :artist")
        List<SongLyrics> findByArtist(String artist);

        @Query("SELECT * FROM SongLyrics WHERE album LIKE :album")
        List<SongLyrics> findByAlbum(String album);

        @Query("SELECT * FROM SongLyrics WHERE artist LIKE :artist AND "
                + "track_title LIKE :track LIMIT 1")
        SongLyrics findByTrackTitle(String artist, String track);

        @Insert
        void insertAll(SongLyrics... songLyrics);

        @Delete
        void delete(SongLyrics songLyrics);
    }

    @Database(entities = {SongLyrics.class}, version = 5)
    public abstract static class AppDatabase extends RoomDatabase {
        public abstract SongLyricsDao songLyricsDao();
    }

    private static AppDatabase appDatabase;

    static AppDatabase getAppDatabase(final Context context) {
        if (appDatabase == null) {

            appDatabase = Room.databaseBuilder(context,
                    AppDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return appDatabase;
    }

    static boolean doesDatabaseExist(Context context) {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        return dbFile.exists();
    }

    static void writeInitialRecords(final Context context) {
        if (appDatabase != null && context != null) {

            // Populate the first-time data
            appDatabase.songLyricsDao().insertAll(
                    new SongLyrics(context.getString(R.string.ballgame_title),
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
    }
}
