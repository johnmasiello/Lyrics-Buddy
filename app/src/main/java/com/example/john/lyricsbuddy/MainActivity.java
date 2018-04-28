package com.example.john.lyricsbuddy;

import android.app.Dialog;
import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.zip.Inflater;

import static com.example.john.lyricsbuddy.LyricDatabaseHelper.SongLyricsDao;
import static com.example.john.lyricsbuddy.LyricDatabaseHelper.doesDatabaseExist;
import static com.example.john.lyricsbuddy.LyricDatabaseHelper.getSongLyricDatabase;
import static com.example.john.lyricsbuddy.LyricDatabaseHelper.writeInitialRecords;

public class MainActivity extends AppCompatActivity {

    private static final String WEB_SEARCH_DIALOG_TAG = "Web Search Dialog";

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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                removeHomeAsUp();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.lyric_master_container, new LyricListFragment(),
                                LyricListFragment.LYRIC_LIST_FRAGMENT_TAG)
                        .commit();
                // Hide the keyboard
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                return true;
            }

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

            case R.id.searchWeb: {
//                if (getSupportFragmentManager().findFragmentByTag(WEB_SEARCH_DIALOG_TAG) == null) {
                    DialogFragment searchWeb = new WebDialogFragment();
                    searchWeb.show(getSupportFragmentManager(), WEB_SEARCH_DIALOG_TAG);
//                }
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

    public static class WebDialogFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getContext();

            LayoutInflater inflater = ((LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE));
            assert inflater != null;

            final String[] webHostNames, webUrls;

            // Create a custom ListView, to set to the dialog
            View root = inflater.inflate(R.layout.web_search_layout, null);

            webHostNames = getResources().getStringArray(R.array.song_lyric_site_names);
            webUrls = getResources().getStringArray(R.array.secure_song_lyrics_site_urls);

            final View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = ((WebAdapter.ViewHolder) v.getTag()).position;
                    String url = webUrls[position];

                    Context context1 = v.getContext();
                    Toast.makeText(context1, url, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY, url);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context1.startActivity(intent);

                    // Dismiss the dialog
                    dismiss();
                }
            };
            ArrayAdapter<String> adapter = new WebAdapter(context,
                    R.layout.web_search_list_item_layout,
                    R.id.web_search_title_text,
                    webHostNames,
                    onClickListener
            );

            ListView listView = root.findViewById(R.id.listView);
            assert listView != null;
            listView.setAdapter(adapter);

            return new AlertDialog.Builder(context, android.support.v7.appcompat.R.style.Theme_AppCompat_Light_DialogWhenLarge)
                    .setView(root)
                    .setTitle(R.string.dialog_title_lyrics_on_web)
                    .create();
        }

        private static class WebAdapter extends ArrayAdapter<String> {
            private final View.OnClickListener mOnClickListener;
            private final String[] mItems;

            WebAdapter(@NonNull Context context, int resource, int textViewResourceId,
                       @NonNull String[] objects, View.OnClickListener onClickListener) {
                super(context, resource, textViewResourceId, objects);
                mItems = objects;
                mOnClickListener = onClickListener;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView;

                if (convertView == null) {
                    textView = view.findViewById(R.id.web_search_title_text);
                    view.setTag(new ViewHolder(textView, position));
                    view.setOnClickListener(mOnClickListener);
                } else {
                    textView = ((ViewHolder) view.getTag()).textView;
                }
                assert textView != null;
                textView.setText(mItems[position]);

                return view;
            }

            public static class ViewHolder {
                public TextView textView;
                public int position;

                public ViewHolder(TextView textView, int position) {
                    this.textView = textView;
                    this.position = position;
                }
            }
        }
    }
}