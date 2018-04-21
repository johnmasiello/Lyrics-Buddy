package com.example.john.lyricsbuddy;

import android.arch.lifecycle.ViewModelProviders;
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

import java.lang.ref.WeakReference;

import static com.example.john.lyricsbuddy.LyricDatabaseHelper.getSongLyricDatabase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpViewFragments();

        doDatabaseInitializeEvent();
        handleIntent();
    }

    private void handleIntent() {
        Intent intent   = getIntent();
        String action   = intent.getAction();
        String type     = intent.getType();

        if (Intent.ACTION_VIEW.equals(action)) {
            if (type != null && type.startsWith(getString(R.string.mimeTypePrefix))) {
                String jsonString = intent.getStringExtra(Intent.EXTRA_TEXT);

                if (jsonString != null) {
                    // TODO parse the jsonString
                    Log.d("Intent", jsonString);
                } else {
                    Uri uri = intent.getData();
                    Log.d("Intent", "Uri is null = "+(uri==null));
                }
            }
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

    private void doDatabaseInitializeEvent() {
        if ( !LyricDatabaseHelper.doesDatabaseExist(this) ) {
              LyricDatabaseHelper.writeInitialRecords(this,
                      new EventPopulateSongLyricDatabaseHelper(this));
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
                    // Pull the lyrics from the UI
                    ((LyricFragment) detail).dumpLyricsIntoViewModel();
                }

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

                // Update the view model to own a blank song lyrics object
                ViewModelProviders.of(MainActivity.this)
                        .get(SongLyricDetailItemViewModel.class)
                        .newSongLyrics();
                return true;
            }

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

    private static class EventPopulateSongLyricDatabaseHelper
            implements SongLyricAsyncTaskCallback {

        final private WeakReference<MainActivity> mActivityWeakReference;

        public EventPopulateSongLyricDatabaseHelper(MainActivity mainActivity) {
            mActivityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void onSuccess() {
            MainActivity mainActivity = mActivityWeakReference.get();

            if (mainActivity != null) {
                SongLyricsListViewModel viewModel =
                    ViewModelProviders.of(mainActivity).get(SongLyricsListViewModel.class);

                viewModel.setSongLyricsDao(getSongLyricDatabase(mainActivity).songLyricsDao());
                viewModel.getLyricList(true);
            }
        }
    }
}