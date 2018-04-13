package com.example.john.lyricsbuddy;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

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

        // Create a singleton instance of the app database that persists for the app's lifecycle
        getAppDatabase(context);

        if (!databaseAlreadyExists) {
            LyricDatabaseHelper.writeInitialRecords(context);
        }

        adapterTest();
    }

    private void adapterTest() {
        AppDatabase database = getAppDatabase(getApplicationContext());

        // Item Count
        Log.d("Database", "Count = " + database.songLyricsDao().count());

        // Items
        List<SongLyricsListItem> listItems = database.songLyricsDao()
                .fetchListItems_NaturalOrder(0, 2);

        for (int i = 0; i < listItems.size(); i++) {
            Log.d("Database","Item "+ i + " uid=" + listItems.get(i).getUid());
        }
    }
}
