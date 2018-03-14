package com.example.john.lyricsgrabber;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.KeyListener;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by john on 3/12/18.
 * Content Fragment that displays lyrics
 */
// Edit Text
// TODO: allow the user to enter meta data + lyrics in one edit text; print in multiple edit texts OR utilize spannable texts
// TODO: Put in color logic on the lyrics, for display mode
// Menu
// TODO: Put in an intent for the share menu item
// TODO: Make a color palette with submenus to choose a color palette. Use a color icon for each submenu item
// TODO: Make an option to save lyrics locally to the phone
// Bottom Navigation
// TODO: Home [icon only] | '+' [new project]
public class LyricFragment extends Fragment {

    // Valid reference until next call to OnCreateOptionsMenu
    private Menu menu;

    private EditText lyrics;
    private int[] lyricSpanColors;
    private SpannableString spannableString;
    private KeyListener lyricsEditorListener;
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

        // Initialize colors to span the lyrics
        String[] colorRes = getResources().getStringArray(R.array.beach_colors);
        lyricSpanColors = new int[colorRes.length];

        for (int i = 0; i < colorRes.length; i++) {
            lyricSpanColors[i] = Color.parseColor(colorRes[i]);
        }
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

        lyricsScroller = rootView.findViewById(R.id.lyrics_scroller);

        if (savedInstanceState != null) {
            mode = savedInstanceState.getInt(MODE_KEY, MODE_EDIT);
        }

        // TODO: control data entry point for lyrics
        String lyricData = getString(R.string.lyrics);
        spannableString = new SpannableString(lyricData);
        lyrics.setText(spannableString, TextView.BufferType.SPANNABLE);

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
                removeSpansFromLyrics();
                lyricsScroller.setBackgroundColor(getResources().getColor(R.color.editBackground));
                break;

            case MODE_DISPLAY:
                menu.findItem(R.id.edit_lyrics).setVisible(true);
                menu.findItem(R.id.view_lyrics).setVisible(false);

                // Edit the lyrics view, by setting a nonnull key listener
                lyrics.setKeyListener(null);
                lyrics.setSelection(0);
                lyrics.setLongClickable(false);

                // Hide keyboard
                View view = getView();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert imm != null;
                    imm.hideSoftInputFromWindow(view.getRootView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                lyrics.clearFocus();
                lyricsScroller.scrollTo(0, 0);

                // Update Visual changes
                applySpansToLyrics();
                lyricsScroller.setBackgroundColor(getResources().getColor(R.color.showBackground));
                break;
        }
    }

    /**
     * Preserves the edits
     */
    private void applySpansToLyrics() {
        // Fetch the previously edited text
        spannableString = new SpannableString(lyrics.getText());

        // Rotate the color spans alternating on characters of editable

        int spanSize = lyricSpanColors.length + 1; // Add the default color
        int charIndex = 0;
        int len = (spannableString.length() >> 1) - 1;

        while (charIndex < len) {
            if ( charIndex % spanSize != lyricSpanColors.length) {
                spannableString.setSpan(new ForegroundColorSpan(lyricSpanColors[charIndex % spanSize]),
                        (charIndex << 1) + 1, (++charIndex) << 1,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            } else {
                charIndex++;
            }
        }
        lyrics.setText(spannableString, TextView.BufferType.SPANNABLE);
    }

    /**
     * Does not preserve the edits
     */
    private void removeSpansFromLyrics() {
        spannableString = new SpannableString(lyrics.getText().toString());
        lyrics.setText(spannableString, TextView.BufferType.SPANNABLE);
    }
}
