package com.example.john.lyricsbuddy;

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
        AppDatabase database = getAppDatabase(getApplicationContext());

        UserDao userDao = database.userDao();

        List<LyricDatabaseHelper.User> users = userDao.getAll();

        if (users.isEmpty()) {
            // Populate the data
            userDao.insertAll(
                    new User(getString(R.string.ballgame_title), null),
                    new User(getString(R.string.jellyRollBlues_title), null),
                    new User(getString(R.string.sweetChariot_title), null)
            );
        } else {
            for (User user: users) {
                Log.d("User", user.toString());

                // Uncomment to remove ALL records
//                userDao.delete(user);
            }
        }
    }
}
