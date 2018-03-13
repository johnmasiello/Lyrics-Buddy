package com.example.john.lyricsgrabber;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

/**
 * Created by john on 3/12/18.
 * Content Fragment that displays lyrics
 */
// Edit Text
// TODO: clear the line at the bottom of the input text
// TODO: allow the user to enter meta data + lyrics in one edit text; print in multiple edit texts OR utilize spannable texts
// TODO: remove long presses in non-edit mode
// TODO: Put in color logic on the lyrics, for display mode
// Menu
// TODO: Put in an icon for share menu item
// TODO: Put in an intent for the share menu item
public class LyricFragment extends Fragment {

    // Valid reference until next call to OnCreateOptionsMenu
    private Menu menu;

    private EditText lyrics;
    private KeyListener lyricsEditorListener;
    private ViewGroup lyricsBackground;
    private ScrollView lyricsScroller;
    private int mode = MODE_EDIT;

    private static final int MODE_EDIT = 0;
    private static final int MODE_DISPLAY = 1;
    private static final String MODE_KEY = "lyric mode";

    public LyricFragment() {
        // Do Default initialization, independent of context here
    }

    // Call when making the first instance of fragment
    public static LyricFragment newInstance() {
        return new LyricFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MODE_KEY, mode);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.lyric_content_layout, container, false);

        lyrics = rootView.findViewById(R.id.lyrics_body);
        lyricsEditorListener = lyrics.getKeyListener();

        lyricsBackground = rootView.findViewById(R.id.lyrics_background);
        lyricsScroller = rootView.findViewById(R.id.lyrics_scroller);

        if (savedInstanceState != null) {
            mode = savedInstanceState.getInt(MODE_KEY, MODE_EDIT);
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.lyric_content_options, menu);
        this.menu = menu;
        updateLyricsMode(mode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_lyrics:
                updateLyricsMode(MODE_EDIT);
                break;

            case R.id.view_lyrics:
                updateLyricsMode(MODE_DISPLAY);
                break;

            case R.id.share_lyrics:
                Toast.makeText(getActivity(), "Share Stella Lyrics", Toast.LENGTH_SHORT).show();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    void updateLyricsMode(int mode) {
        this.mode = mode;
        switch (mode) {
            case MODE_EDIT:
                menu.findItem(R.id.edit_lyrics).setVisible(false);
                menu.findItem(R.id.view_lyrics).setVisible(true);

                // Edit the lyrics view, by setting a nonnull key listener
                lyrics.setKeyListener(lyricsEditorListener);

                // Update Visual changes
                lyricsBackground.setBackgroundColor(getResources().getColor(R.color.editBackground));
                break;

            case MODE_DISPLAY:
                menu.findItem(R.id.edit_lyrics).setVisible(true);
                menu.findItem(R.id.view_lyrics).setVisible(false);

                // Edit the lyrics view, by setting a nonnull key listener
                lyrics.setKeyListener(null);
                lyrics.clearFocus();

                // Hide keyboard
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert imm != null;
                    imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                lyricsScroller.scrollTo(0, 0);

                // Update Visual changes
                lyricsBackground.setBackgroundColor(getResources().getColor(R.color.showBackground));
                break;
        }
    }
}
