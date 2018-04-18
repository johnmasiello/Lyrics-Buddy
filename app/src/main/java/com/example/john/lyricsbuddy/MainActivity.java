package com.example.john.lyricsbuddy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import static com.example.john.lyricsbuddy.LyricDatabaseHelper.getSongLyricDatabase;

public class MainActivity extends AppCompatActivity {
    private final String MASTER_TRANSACTION_TAG = "Master Transaction";

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

        doDatabaseInitializeEvent();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.lyric_master_container,
                        new LyricListFragment(),
                            MASTER_TRANSACTION_TAG)
                    .commit();
        }
        // TODO determine layout configuration: 1 or 2 panes
        boolean isSinglePane = true;
        if (isSinglePane && getSupportFragmentManager()
                .findFragmentByTag(LyricFragment.DETAIL_FRAGMENT_TAG) != null) {
            // Show up navigation on configuration change
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void doDatabaseInitializeEvent() {
        if ( !LyricDatabaseHelper.doesDatabaseExist(this) ) {
              LyricDatabaseHelper.writeInitialRecords(getApplicationContext());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                if (fm.findFragmentByTag(LyricFragment.DETAIL_FRAGMENT_TAG) != null) {
                    removeHomeAsUp();
                    fm.popBackStack(LyricListFragment.DETAIL_BACKSTACK_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    return super.onOptionsItemSelected(item);
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager()
                .findFragmentByTag(LyricFragment.DETAIL_FRAGMENT_TAG) == null) {
            removeHomeAsUp();
        }
    }

    private void removeHomeAsUp() {
        // Hide the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }
}