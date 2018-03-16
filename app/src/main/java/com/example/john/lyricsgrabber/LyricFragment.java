package com.example.john.lyricsgrabber;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by john on 3/12/18.
 * Content Fragment that displays lyrics
 */
// Menu
// TODO: Put in an intent for the share menu item
// https://stackoverflow.com/questions/13941093/how-to-share-entire-android-app-with-share-intent
// TODO: Make a color palette with submenus to choose a color palette. Use a color icon for each submenu item
// TODO: Make an option to save lyrics locally to the phone
// Bottom Navigation
// TODO: Home [icon only] | '+' [new project]
public class LyricFragment extends Fragment {

    // Valid reference until next call to OnCreateOptionsMenu
    private Menu menu;

    // Handles to UI
    final int[] textViewIDs = new int[] {
            R.id.title,
            R.id.album,
            R.id.by,
            R.id.artist
    };
    private SparseArray<TextView> trackInfo;
    private WrappedEditText lyrics;
    private ScrollView lyricsScroller;

    // Lyric color logic
    private LyricAnalyzer lyricAnalyzer;
    private int defaultLineColorEdit;
    private int defaultLineColorDisplay;
    private ShadowLayer shadowLayer;
    private int[] lyricSpanColors;
    private int[] regions;

    // State
    private int mode = MODE_EDIT;

    private static final int MODE_EDIT = 0;
    private static final int MODE_DISPLAY = 1;
    private static final String MODE_KEY = "lyric mode";

    public LyricFragment() {
        // Do Default initialization, independent of context here
        trackInfo = new SparseArray<>();
    }

    // Call when making the first instance of fragment
    public static LyricFragment newInstance() {
        return new LyricFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Initialize color logic helper
        lyricAnalyzer = new LyricAnalyzer();

        // Initialize colors to span the lyrics
        defaultLineColorEdit = getResources().getColor(R.color.default_line_color_plain);
        defaultLineColorDisplay = getResources().getColor(R.color.default_line_color);

        String[] colorRes = getResources().getStringArray(R.array.colors_beach);
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


        trackInfo.clear();

        for (int id : textViewIDs) {
            trackInfo.append(id, ((TextView) rootView.findViewById(id)));
        }
        // Get the default ShadowLayer properties
        TextView t1 = trackInfo.get(textViewIDs[0]);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            shadowLayer = new ShadowLayer(t1.getShadowRadius(), t1.getShadowDx(), t1.getShadowDy(), t1.getShadowColor());
        } else {
            shadowLayer = new ShadowLayer(inflater.getContext());
        }

        lyrics = rootView.findViewById(R.id.lyrics_body);
        lyricsScroller = rootView.findViewById(R.id.lyrics_scroller);

        if (savedInstanceState != null) {
            mode = savedInstanceState.getInt(MODE_KEY, MODE_EDIT);
        }

        // TODO: control data entry point for lyrics
        trackInfo.get(R.id.title).setText(R.string.track_title);
        trackInfo.get(R.id.album).setText(getString(R.string.track_album));
        trackInfo.get(R.id.artist).setText(getString(R.string.track_artist));
        lyrics.setText(new SpannableString(getString(R.string.lyrics)), TextView.BufferType.SPANNABLE);

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
        TextView textView;

        this.mode = mode;
        switch (mode) {
            case MODE_EDIT:
                menu.findItem(R.id.edit_lyrics).setVisible(false);
                menu.findItem(R.id.view_lyrics).setVisible(true);
                lyrics.setEditable(true);

                // Update Visual changes
                for (int id : textViewIDs) {
                    textView = trackInfo.get(id);
                    applyDefaultTextColor(textView, MODE_EDIT);
                    setVisibility(textView, MODE_EDIT);

                    if (textView instanceof WrappedEditText) {
                        ((WrappedEditText) textView).setEditable(true);
                    }
                }
                lyrics.removeSpansFromText();
                regions = null;
                lyricsScroller.setBackgroundColor(getResources().getColor(R.color.editBackground));
                break;

            case MODE_DISPLAY:
                menu.findItem(R.id.edit_lyrics).setVisible(true);
                menu.findItem(R.id.view_lyrics).setVisible(false);

                lyrics.setEditable(false);

                // Hide keyboard
                View view = getView();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert imm != null;
                    imm.hideSoftInputFromWindow(view.getRootView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                lyricsScroller.scrollTo(0, 0);

                // Update Visual changes
                for (int id : textViewIDs) {
                    textView = trackInfo.get(id);
                    applyDefaultTextColor(textView, MODE_DISPLAY);
                    setVisibility(textView, MODE_DISPLAY);

                    if (textView instanceof WrappedEditText) {
                        ((WrappedEditText) textView).setEditable(false);
                    }
                }
                applySpansToLyrics();
                lyricsScroller.setBackgroundColor(getResources().getColor(R.color.showBackground));
                break;
        }
    }

    /**
     * Apply foreground color spans to the lyrics while preserving the edits
     */
    private void applySpansToLyrics() {

        String lyrics = this.lyrics.getText().toString();
        SpannableString spannableString = new SpannableString(lyrics);

        if (regions == null) {
            computeRegions(lyrics);
        }

        // Apply color spans, highlighting the structure of the lyrics
        int index, offset, nextOffset;

        offset = index = 0;
        nextOffset = lyrics.indexOf('\n', offset);

        while (nextOffset != -1) {
            if (lyricAnalyzer.containsWords(lyrics.substring(offset, nextOffset))) {
                setColorSpan(spannableString, index, offset, nextOffset);
            }
            index++;
            offset = ++nextOffset;
            nextOffset = lyrics.indexOf('\n', offset);
        }
        nextOffset = lyrics.length();
        if (offset != nextOffset && lyricAnalyzer.containsWords(lyrics.substring(offset, nextOffset))) {
            setColorSpan(spannableString, index, offset, nextOffset);
        }
        this.lyrics.setText(spannableString, TextView.BufferType.SPANNABLE);
    }

    private void setColorSpan(SpannableString spannable, int regionIndex, int start, int end) {
        int colorIndex = regions[regionIndex];
        spannable.setSpan(
                new ForegroundColorSpan(colorIndex != 0 ? lyricSpanColors[colorIndex - 1] :
                        defaultLineColorDisplay),
                start,
                end,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
    }

    /**
     * Should be called each time the text in lyrics has or may have been changed.
     * It is unnecessary, however, using this method when the color palette has been changed only.
     */
    private void computeRegions(String lyrics) {
        String[] lyricLines = lyricAnalyzer.delimitLines(lyrics);
        List<List<Integer>> matchingLineNumbers = lyricAnalyzer.findEquivalentLines(lyricLines);
        regions = lyricAnalyzer.findColorRegions(matchingLineNumbers);
    }

    private void applyDefaultTextColor(TextView textView, int mode) {
        switch (mode) {
            case MODE_EDIT:
                textView.setTextColor(defaultLineColorEdit);
                break;

            case MODE_DISPLAY:
                textView.setTextColor(defaultLineColorDisplay);
                break;
        }
    }

    /**
     *
     * @param mode mode==MODE_EDIT implies make the textView visible; false implies make the view
     *                    gone if the view contains no text, but make the view visible
     */
    private void setVisibility(TextView textView, int mode) {

        int visibility = mode==MODE_EDIT || lyricAnalyzer.containsWords(textView.getText().toString()) ?
                View.VISIBLE :
                View.GONE;

        textView.setVisibility(visibility);

        if (mode==MODE_EDIT) {
            textView.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        } else {
            textView.setShadowLayer(shadowLayer.radius, shadowLayer.dx, shadowLayer.dy,
                    shadowLayer.color);
        }

        // A co-invariant
        if (textView.getId()==R.id.artist) {
            trackInfo.get(R.id.by).setVisibility(visibility);
        }
    }

    static class ShadowLayer {
        final float radius, dx, dy;
        final int color;

        ShadowLayer(float radius, float dx, float dy, int color) {
            this.radius = radius;
            this.dx = dx;
            this.dy = dy;
            this.color = color;
        }

        ShadowLayer(Context context) {
            this.radius = 5;
            this.dx = 7;
            this.dy = 7;
            this.color = context.getResources().getColor(R.color.trackInfoShadowColor);
        }
    }
}
