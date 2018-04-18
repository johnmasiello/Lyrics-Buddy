package com.example.john.lyricsbuddy;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home: {
                    // TODO: Support 2-pane layout when flowing to detail view
                    // If 2-pane then do nothing
                    // Otherwise...
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if (isDetailFragmentInBackStack()) {
                        removeHomeAsUp();
                        fragmentManager.popBackStack(LyricFragment.DETAIL_BACK_STACK_TAG,
                                FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                    else if (fragmentManager.findFragmentByTag(LyricListFragment.LYRIC_LIST_FRAGMENT_TAG) == null) {
                        fragmentManager.beginTransaction().replace(R.id.lyric_master_container,
                                new LyricListFragment(), LyricListFragment.LYRIC_LIST_FRAGMENT_TAG)
                                .commit();
                    }
                    return true;
                }

                case R.id.new_lyrics:
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    Fragment detailFragment = fragmentManager
                            .findFragmentByTag(LyricFragment.DETAIL_FRAGMENT_TAG);

                    if (!(detailFragment instanceof LyricFragment)) {
                        // TODO: Support 2-pane layout when flowing to detail view
                        fragmentManager.beginTransaction()
                                .replace(R.id.lyric_master_container, new LyricFragment(),
                                        LyricFragment.DETAIL_FRAGMENT_TAG)
                                .commit();
                    } else {
                        removeHomeAsUp();
                        // Persist the state of the current song lyrics in the detail fragment
                        ((LyricFragment) detailFragment).dumpLyricsIntoViewModel();
                        // TODO Update the Live data in Lyric List View model to reflect changes, for example a new item in the list
                    }
                    ViewModelProviders.of(MainActivity.this)
                            .get(SongLyricDetailItemViewModel.class)
                                    .newSongLyrics();
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

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getFragments().isEmpty()) {
            fragmentManager.beginTransaction()
                    .add(R.id.lyric_master_container,
                            new LyricListFragment(),
                            LyricListFragment.LYRIC_LIST_FRAGMENT_TAG)
                    .commit();
        }
        // TODO determine layout configuration: 1 or 2 panes
        boolean isSinglePane = true;
        if (isSinglePane && isDetailFragmentInBackStack()) {
            // Show up navigation on configuration change
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void doDatabaseInitializeEvent() {
        if ( !LyricDatabaseHelper.doesDatabaseExist(this) ) {
              LyricDatabaseHelper.writeInitialRecords(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                if (fm.findFragmentByTag(LyricFragment.DETAIL_FRAGMENT_TAG) != null) {
                    removeHomeAsUp();
                    fm.popBackStack(LyricFragment.DETAIL_BACK_STACK_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

    private boolean isDetailFragmentInBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        int count = fm.getBackStackEntryCount();

        for (int i = 0; i < count; i++) {
            if (String.valueOf(fm.getBackStackEntryAt(i).getName())
                    .equals(LyricFragment.DETAIL_BACK_STACK_TAG)) {
                return true;
            }
        }
        return false;
    }
}