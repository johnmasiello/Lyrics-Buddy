package com.example.john.lyricsbuddy;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import static com.example.john.lyricsbuddy.LyricDatabaseHelper.*;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Toast.makeText(MainActivity.this, getString(R.string.title_home), Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.new_lyrics:
                    Toast.makeText(MainActivity.this, getString(R.string.title_new_lyrics), Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.lyric_content_container,
                    LyricFragment.newInstance())
                    .commit();
        }

        // Get an app Database to manage all of the lyrics
        Context context = getApplicationContext();
        boolean databaseAlreadyExists = LyricDatabaseHelper.doesDatabaseExist(context);

        AppDatabase database = getAppDatabase(context);

        if (!databaseAlreadyExists) {
            LyricDatabaseHelper.writeInitialRecords(context);
            Log.d("Database", "Database Record No 1="+String.valueOf(database.songLyricsDao().getFirstUser()));
        } else {
            Log.d("Database", "Database already exists");
        }
    }
}
