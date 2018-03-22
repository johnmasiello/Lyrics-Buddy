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

import java.util.List;

/**
 * Created by john on 3/21/18.
 *
 * Singleton to manage the database of lyrics
 */

public class LyricDatabaseHelper {

    @Entity
    public static class User {
        @PrimaryKey
        private int uid;

        @ColumnInfo(name = "first_name")
        private String firstName;

        @ColumnInfo(name = "last_name")
        private String lastName;

        int getUid() {
            return uid;
        }

        void setUid(int uid) {
            this.uid = uid;
        }

        String getFirstName() {
            return firstName;
        }

        void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        String getLastName() {
            return lastName;
        }

        void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }

    @Dao
    public interface UserDao {
        @Query("SELECT * FROM user")
        List<User> getAll();

        @Query("SELECT * FROM user WHERE uid IN (:userIds)")
        List<User> loadAllByIds(int[] userIds);

        @Query("SELECT * FROM user WHERE first_name LIKE :first AND "
                + "last_name LIKE :last LIMIT 1")
        User findByName(String first, String last);

        @Insert
        void insertAll(User... users);

        @Delete
        void delete(User user);
    }

    @Database(entities = {User.class}, version = 1)
    public abstract static class AppDatabase extends RoomDatabase {
        public abstract UserDao userDao();
    }

    private static AppDatabase appDatabase;

    static AppDatabase getAppDatabase(Context context) {
        if (appDatabase == null) {
            appDatabase = Room.databaseBuilder(context,
                    AppDatabase.class, "database-name")
                    .allowMainThreadQueries()
                    .build();
        }
        return appDatabase;
    }
}
