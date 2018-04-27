package com.example.john.lyricsbuddy;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.john.lyricsbuddy.LyricDatabaseHelper.SongLyricsDao;
import static com.example.john.lyricsbuddy.LyricDatabaseHelper.doesDatabaseExist;
import static com.example.john.lyricsbuddy.LyricDatabaseHelper.getSongLyricDatabase;
import static com.example.john.lyricsbuddy.LyricDatabaseHelper.writeInitialRecords;

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

        // Initialize action view for searching the web
        MenuItem searchWeb = menu.findItem(R.id.searchWeb);

        if (searchWeb != null) {
            View spinnerView = searchWeb.getActionView();

            if (spinnerView instanceof AppCompatSpinner) {
                AppCompatSpinner spinner = ((AppCompatSpinner) spinnerView);

                // Row data
                String[] items = getResources().getStringArray(R.array.song_lyric_site_names);
                final String[] response = items;

                BaseAdapter adapter =
                        new ArrayAdapter<String>(this,
                                R.layout.web_search_list_item_layout,
                                items)
                        {
                            private final ThemedSpinnerAdapter.Helper mDropDownHelper =
                                    new ThemedSpinnerAdapter.Helper(MainActivity.this);
                            private final View.OnClickListener mOnClick = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Object position_ = v.getTag();
                                    int position = position_ instanceof Integer ? ((Integer) position_) : 0;

                                    Toast.makeText(v.getContext(), response[position], Toast.LENGTH_SHORT).show();
                                }
                            };

                            @Override
                            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                View view;
                                if (convertView == null) {
                                    // Inflate the drop down using the helper's LayoutInflater
                                    LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                                    view = inflater.inflate(R.layout.support_simple_spinner_dropdown_item, parent, false);

                                    view.setOnClickListener(mOnClick);
                                } else {
                                    view = convertView;
                                }
                                view.setTag(position);
                                return view;
                            }

                            @Override
                            public void setDropDownViewTheme(@Nullable Resources.Theme theme) {
                                mDropDownHelper.setDropDownViewTheme(theme);
                            }

                            @Nullable
                            @Override
                            public Resources.Theme getDropDownViewTheme() {
                                return mDropDownHelper.getDropDownViewTheme();
                            }
                        };

                spinner.setAdapter(adapter);
            }
        }
        return super.onCreateOptionsMenu(menu);
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
                // Hide the keyboard
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
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