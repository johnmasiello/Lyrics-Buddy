package com.example.john.lyricsbuddy;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import static com.example.john.lyricsbuddy.LyricDatabaseHelper.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firstInitializationOfDatabaseEvent();

        setContentView(R.layout.activity_main);
        handleIntent();

        setUpViewFragments();
    }

    private void handleIntent() {
        Intent intent   = getIntent();
        String action   = intent.getAction();
        String type     = intent.getType();
        Uri uri = intent.getData();
        String scheme = intent.getScheme();

        if (Intent.ACTION_VIEW.equals(action) &&
                type != null && type.startsWith(getString(R.string.mimeTypePrefix)) &&
                (ContentResolver.SCHEME_CONTENT.equals(scheme) ||
                    ContentResolver.SCHEME_FILE.equals(scheme))) {

            SongLyricsListViewModel lvm =
                    ViewModelProviders.of(MainActivity.this)
                    .get(SongLyricsListViewModel.class);

            // Ensure lvm has access to the SongLyricsDao
            SongLyricsDao songLyricsDao = getSongLyricDatabase(this).songLyricsDao();
            lvm.setSongLyricsDao(songLyricsDao);

            new ActionImportContentTask(this, lvm).execute(uri);
        }
    }

    private void setUpViewFragments() {
        MainActivityViewModel model = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        model.setTwoPane(findViewById(R.id.lyric_detail_container) != null);
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (model.isTwoPane()) {
            // Two pane layout does not have up navigation
            removeHomeAsUp();
            Fragment fragment = fragmentManager.findFragmentById(R.id.lyric_master_container);

            if (fragment instanceof LyricFragment) {
                fragmentManager.beginTransaction()
                        .replace(R.id.lyric_master_container, new LyricListFragment(),
                                LyricListFragment.LYRIC_LIST_FRAGMENT_TAG)
                        .add(R.id.lyric_detail_container, fragment,
                                LyricFragment.DETAIL_FRAGMENT_TAG)
                        .commit();
            } else if (fragment == null) {
                fragmentManager.beginTransaction()
                        .replace(R.id.lyric_master_container, new LyricListFragment(),
                                LyricListFragment.LYRIC_LIST_FRAGMENT_TAG)
                        .add(R.id.lyric_detail_container, new LyricFragment(),
                                LyricFragment.DETAIL_FRAGMENT_TAG)
                        .commit();
            } else if (fragmentManager.findFragmentById(R.id.lyric_detail_container) == null) {
                fragmentManager.beginTransaction()
                        .add(R.id.lyric_detail_container, new LyricFragment(),
                                LyricFragment.DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            Fragment fragment = fragmentManager.findFragmentById(R.id.lyric_detail_container);

            if (fragment != null) {
                addHomeAsUp();
                fragmentManager.beginTransaction()
                        .remove(fragment)
                        .replace(R.id.lyric_master_container, new LyricFragment(),
                                LyricFragment.DETAIL_FRAGMENT_TAG)
                        .commit();
            } else {
                fragment = fragmentManager.findFragmentById(R.id.lyric_master_container);

                if (fragment instanceof LyricFragment) {
                    addHomeAsUp();
                } else if (fragment == null) {
                    fragmentManager.beginTransaction()
                            .add(R.id.lyric_master_container, new LyricListFragment(),
                                    LyricListFragment.LYRIC_LIST_FRAGMENT_TAG)
                            .commit();
                }
            }
        }
    }

    /**
     * CAUTION: This must be called before any other access to the database, which
     * will bypass detection of whether the database already existed
     */
    private void firstInitializationOfDatabaseEvent() {
        if ( !doesDatabaseExist(this) ) {
              writeInitialRecords(this
              );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                removeHomeAsUp();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.lyric_master_container, new LyricListFragment(),
                                LyricListFragment.LYRIC_LIST_FRAGMENT_TAG)
                        .commit();
                return true;

            case R.id.new_lyrics: {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment detail = fragmentManager.findFragmentByTag(LyricFragment.DETAIL_FRAGMENT_TAG);

                if (detail instanceof LyricFragment) {
                    // Pull the data from the old lyrics item from the UI,
                    // to be updated in the repository
                    ((LyricFragment) detail).dumpLyricsIntoViewModel();
                }

                // Update the view model to own a blank song lyrics object
                ViewModelProviders.of(MainActivity.this)
                        .get(SongLyricDetailItemViewModel.class)
                        .newSongLyrics();

                // Show the detail fragment, or list/detail layout
                MainActivityViewModel activityViewModel = ViewModelProviders.of(this)
                        .get(MainActivityViewModel.class);

                if (activityViewModel.isTwoPane()) {
                    if (fragmentManager.findFragmentById(R.id.lyric_detail_container) == null) {
                        fragmentManager.beginTransaction()
                                .add(R.id.lyric_detail_container, new LyricFragment(),
                                        LyricFragment.DETAIL_FRAGMENT_TAG)
                                .commit();
                    }
                } else {
                    fragmentManager.beginTransaction()
                            .replace(R.id.lyric_master_container, new LyricFragment(),
                                    LyricFragment.DETAIL_FRAGMENT_TAG)
                            .commit();

                    // Show the Up Navigation button in the action bar
                    addHomeAsUp();
                }
                return true;
            }
            // TODO LyricListFragment allow sorts on list > using queries

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void removeHomeAsUp() {
        // Hide the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    private void addHomeAsUp() {
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

}