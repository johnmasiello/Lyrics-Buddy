package com.example.john.lyricsbuddy;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import static com.example.john.lyricsbuddy.LyricDatabaseHelper.SongLyricsListItem;
import static com.example.john.lyricsbuddy.LyricDatabaseHelper.getAppDatabase;

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
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.lyric_content_container,
//                    )
//                    .commit();
        }

        // TODO If container for detail view is not null and FragmentManager does not contain detail view, add detail view in transaction
    }

    private void initializeSongLyricsDatabase() {
        // Get an app Database to manage all of the lyrics
        boolean databaseAlreadyExists = LyricDatabaseHelper.doesDatabaseExist(this);

        // Create a singleton instance of the app database that persists for the app's lifecycle
        getAppDatabase(this);

        if (!databaseAlreadyExists) {
            LyricDatabaseHelper.writeInitialRecords(this);
        }
    }

    private void initializeSongLyricsListItemViewModel() {
        SongLyricsListViewModel songLyricsListViewModel =
                ViewModelProviders.of(this).get(SongLyricsListViewModel.class);

        // Add an observer to the recyclerView UI
        songLyricsListViewModel
                .fetchSongLyricsListViewModel(getAppDatabase(this).songLyricsDao())
                .observe(this, new Observer<List<SongLyricsListItem>>() {

                    @Override
                    public void onChanged(@Nullable List<SongLyricsListItem> songLyricsListItems) {
                        // TODO submit list to the recycler view list adapter
                    }
                });
    }
}
