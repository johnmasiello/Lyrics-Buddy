package com.example.john.lyricsbuddy;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.util.TypedValue;
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

import org.json.JSONArray;

import java.util.List;
import java.util.Random;

import static com.example.john.lyricsbuddy.LyricDatabaseHelper.SongLyrics;

/**
 * Created by john on 3/12/18.
 * Content Fragment that displays lyrics
 */
public class LyricFragment extends Fragment {

    public static final String DETAIL_FRAGMENT_TAG = "Detail Fragment Tag";
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
    private Random random;
    private int highlightPadding;

    // UI State
    private int mode = MODE_EDIT;
    private int paletteId = R.id.beach;
    private int colorRotation = DEFAULT_COLOR_ROTATION;

    // SongLyric State
    private SongLyricDetailItemViewModel songLyricsDetailViewModel;
    private Observer<SongLyrics> songLyricsObserver;
    private boolean ignoreChange = false;

    private static final int MODE_EDIT = 0;
    private static final int MODE_DISPLAY = 1;
    private static final int DEFAULT_COLOR_ROTATION = -1;
    private static final String MODE_KEY = "lyric mode";
    private static final String PALETTE_KEY = "palette";
    private static final String COLOR_ROTATION_KEY = "color rotation";

    public LyricFragment() {
        // Do Default initialization, independent of context here
        trackInfo = new SparseArray<>();
        random = new Random(System.currentTimeMillis());
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
        highlightPadding = getResources().getDimensionPixelSize(R.dimen.highlighted_lyrics_padding);

        if (savedInstanceState != null) {
            mode = savedInstanceState.getInt(MODE_KEY, MODE_EDIT);
            paletteId = savedInstanceState.getInt(PALETTE_KEY, R.id.beach);
            colorRotation = savedInstanceState.getInt(COLOR_ROTATION_KEY, DEFAULT_COLOR_ROTATION);
        }

        fetchColorsFromPalette();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        connectToSongLyricsItemViewModel();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MODE_KEY, mode);
        outState.putInt(PALETTE_KEY, paletteId);
        outState.putInt(COLOR_ROTATION_KEY, colorRotation);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
        // Force the lyrics to respect the color background padding
        lyrics.setShadowLayer(highlightPadding /* radius */, 0, 0, Color.TRANSPARENT);
        lyrics.setPadding(highlightPadding, 0, highlightPadding, highlightPadding);
        lyrics.setLineSpacing(highlightPadding, 1.0f);
        lyricsScroller = rootView.findViewById(R.id.lyrics_scroller);

        return rootView;
    }

    private void connectToSongLyricsItemViewModel() {
        //noinspection ConstantConditions
        songLyricsDetailViewModel = ViewModelProviders.of(getActivity()).get(SongLyricDetailItemViewModel.class);

        songLyricsDetailViewModel.setSongLyricsDao(LyricDatabaseHelper
                .getSongLyricDatabase(getActivity())
                    .songLyricsDao());

        songLyricsObserver = new Observer<SongLyrics>() {
            @Override
            public void onChanged(@Nullable SongLyrics songLyrics) {
                if (songLyrics == null || ignoreChange) {
                    ignoreChange = false;
                    return;
                }
                setTextWatcherEnabled(false);
                trackInfo.get(R.id.title).setText(songLyrics.getTrackTitle());
                trackInfo.get(R.id.album).setText(songLyrics.getAlbum());
                trackInfo.get(R.id.artist).setText(songLyrics.getArtist());
                lyrics.setText(new SpannableString(songLyrics.getLyrics()), TextView.BufferType.EDITABLE);
                setTextWatcherEnabled(true);
            }
        };
        songLyricsDetailViewModel.getSongLyrics().observe(this, songLyricsObserver);

        WrappedEditText.ensureUndoStack();
        WrappedEditText.ensureRedoStack();

        // Get the scrollview to scroll to the topmost view in the layout
        EditText t2 = ((EditText) trackInfo.get(textViewIDs[0]));
        t2.requestFocus();
        t2.setSelection(0, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        ignoreChange = true;
        dumpLyricsIntoViewModel();
        // Persist the data
        songLyricsDetailViewModel.updateDatabase(false);
    }

    private void setTextWatcherEnabled(boolean enabled) {
        if (enabled) {
            lyrics.addTextWatcher(-1);

            TextView t;
            for (int i = 0; i < textViewIDs.length; i++) {
                t = trackInfo.get(textViewIDs[i]);

                if (t instanceof WrappedEditText) {
                    ((WrappedEditText) t).addTextWatcher(i);
                }
            }
        } else {
            lyrics.removeTextWatcher();

            TextView t;
            for (int textViewID : textViewIDs) {
                t = trackInfo.get(textViewID);

                if (t instanceof WrappedEditText) {
                    ((WrappedEditText) t).removeTextWatcher();
                }
            }
        }
    }

    /**
     * Has the side-effect of causing observers to reload the song lyrics from the view model.
     * Since the dump is triggered by the view controller and clicks, generally events
     * independent of the underlying live data, an infinite loop is thus avoided.
     */
    public void dumpLyricsIntoViewModel() {
        LiveData<SongLyrics> songLyricsLiveData = songLyricsDetailViewModel.getSongLyrics();
        SongLyrics songLyrics, songLyricsOutput;

        songLyrics = songLyricsLiveData.getValue();
        songLyricsOutput = new SongLyrics();

        if (songLyrics != null) {
            songLyricsOutput.setUid(songLyrics.getUid());
        }
        // Pull SongLyrics info from the EditTexts in the UI
        songLyricsOutput.setTrackTitle(trackInfo.get(R.id.title).getText().toString());
        songLyricsOutput.setAlbum(trackInfo.get(R.id.album).getText().toString());
        songLyricsOutput.setArtist(trackInfo.get(R.id.artist).getText().toString());
        songLyricsOutput.setLyrics(lyrics.getText().toString());

        // Update the SongLyrics in the ViewModel with the extracted SongLyrics
        songLyricsDetailViewModel.setSongLyrics(songLyricsOutput);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.lyric_content_options, menu);
        this.menu = menu;

        // Update the menu icons
        MenuItem item = menu.findItem(paletteId);
        if (item != null) {
            item.setIcon(menuItemIcon(paletteId, true));
        }

        MenuItem itemNew = menu.findItem(R.id.new_lyrics);
        if (itemNew != null) {
            itemNew.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        // Update the lyrics
        updateLyricsMode(mode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getGroupId()==R.id.palettes) {

            // Remove the previously checkedItem
            MenuItem oldItem = menu.findItem(paletteId);
            if (oldItem != null) {
                oldItem.setIcon(menuItemIcon(paletteId, false));
            }

            paletteId = item.getItemId();

            // Check this item
            item.setIcon(menuItemIcon(paletteId, true));

            fetchColorsFromPalette();
            colorRotation = DEFAULT_COLOR_ROTATION;
            applySpansToLyrics();
        } else {
            switch (item.getItemId()) {
                case R.id.edit_lyrics:
                    updateLyricsMode(MODE_EDIT);
                    break;

                case R.id.view_lyrics:
                    updateLyricsMode(MODE_DISPLAY);
                    break;

                case R.id.share_lyrics:
                    // Fetch song lyrics
                    dumpLyricsIntoViewModel();
                    SongLyrics songLyrics = songLyricsDetailViewModel.getSongLyricsInstantly();

                    if (songLyrics != null) {
                        LyricActionHelperKt.share(this, songLyrics);
                    } else {
                        LyricActionHelperKt.failShare(getContext(), R.string.share_intent_fail_message);
                    }
                    break;

                case R.id.shuffle_colors:
                    // Take the lower bits of the next random integer
                    colorRotation = random.nextInt() & 0xffff;
                    applySpansToLyrics();
                    break;

                case R.id.undo:
                    WrappedEditText.undoTextChange(
                            fetchEditTextFromUndoRedo(WrappedEditText.peekUndoId()));
                    break;

                case R.id.redo:
                    WrappedEditText.redoTextChange(
                            fetchEditTextFromUndoRedo(WrappedEditText.peekRedoId()));
                    break;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        return true;
    }

    WrappedEditText fetchEditTextFromUndoRedo(int id) {
        if (id == WrappedEditText.NO_UNDO_ID) {
            return null;
        } else if (id == -1) {
            return lyrics;
        } else {
            try {
                return ((WrappedEditText) trackInfo.get(textViewIDs[id]));
            } catch (Exception e) {
                throw new IllegalStateException("Undo operation: invalid id of EditText; id = "+id);
            }
        }
    }

    void updateLyricsMode(int mode) {
        TextView textView;

        this.mode = mode;
        switch (mode) {
            case MODE_EDIT:
                menu.findItem(R.id.edit_lyrics).setVisible(false);
                menu.findItem(R.id.shuffle_colors).setVisible(false);
                menu.findItem(R.id.palette).setVisible(false);
                menu.findItem(R.id.view_lyrics).setVisible(true);
                menu.findItem(R.id.undo).setVisible(true);
                menu.findItem(R.id.redo).setVisible(true);
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
                menu.findItem(R.id.shuffle_colors).setVisible(true);
                menu.findItem(R.id.palette).setVisible(true);
                menu.findItem(R.id.view_lyrics).setVisible(false);
                menu.findItem(R.id.undo).setVisible(false);
                menu.findItem(R.id.redo).setVisible(false);

                lyrics.setEditable(false);

                // Hide keyboard
                View view = getView();
                if (view != null && getActivity() != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert imm != null;
                    imm.hideSoftInputFromWindow(view.getRootView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

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

        // Remove the spans
        this.lyrics.removeSpansFromText();

        // Add new spans...
        Editable editable = this.lyrics.getEditableText();

        String lyrics = editable.toString();
        if (regions == null) {
            computeRegions(lyrics);
        }

        // Apply color spans, highlighting the structure of the lyrics
        int index, offset, nextOffset;

        offset = index = 0;
        nextOffset = lyrics.indexOf('\n', offset);

        while (nextOffset != -1) {
            if (lyricAnalyzer.containsWords(lyrics.substring(offset, nextOffset))) {
                setColorSpan(editable, index, offset, nextOffset);
            }
            index++;
            offset = ++nextOffset;
            nextOffset = lyrics.indexOf('\n', offset);
        }
        nextOffset = lyrics.length();
        if (offset != nextOffset && lyricAnalyzer.containsWords(lyrics.substring(offset, nextOffset))) {
            setColorSpan(editable, index, offset, nextOffset);
        }
    }

    private void setColorSpan(Editable editable, int regionIndex, int start, int end) {
        int colorIndex = regions[regionIndex];
        int foregroundColor, backgroundColor;

        if (colorIndex == 0) {
            foregroundColor = defaultLineColorDisplay;
        } else {
            backgroundColor = lyricSpanColors[(colorIndex + colorRotation) % lyricSpanColors.length];
            foregroundColor = ColorPerceptionHelper.getEquidistantGray(backgroundColor);

            editable.setSpan(
                    new PaddedBackgroundColorSpan(highlightPadding, backgroundColor),
                    start,
                    end,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            );
        }
        editable.setSpan(
                new ForegroundColorSpan(foregroundColor),
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

    private void fetchColorsFromPalette() {
        String[] colorRes = getResources().getStringArray(fetchPalette(paletteId));
        lyricSpanColors = new int[colorRes.length];

        for (int i = 0; i < colorRes.length; i++) {
            lyricSpanColors[i] = Color.parseColor(colorRes[i]);
        }
    }

    private @ArrayRes int fetchPalette(int menuItemId) {
        switch (menuItemId) {
            case R.id.beach:
                return R.array.colors_beach;
            case R.id.reggae:
                return R.array.colors_reggae;
            case R.id.rock:
                return R.array.colors_goth;
            case R.id.usa:
                return R.array.colors_usa;
            case R.id.spring:
                return R.array.colors_spring;
            case R.id.autumn:
                return R.array.colors_autumn;
            case R.id.winter:
                return R.array.colors_snowflake;
            default:
                return -1;
        }
    }

    private @DrawableRes int menuItemIcon(int menuItemId, boolean isChecked) {
        switch (menuItemId) {
            case R.id.beach:
                return isChecked ? R.drawable.palette_beach_check : R.drawable.palette_beach;
            case R.id.reggae:
                return isChecked ? R.drawable.palette_reggae_check : R.drawable.palette_reggae;
            case R.id.rock:
                return isChecked ? R.drawable.palette_rock_check : R.drawable.palette_rock;
            case R.id.usa:
                return isChecked ? R.drawable.palette_usa_check : R.drawable.palette_usa;
            case R.id.spring:
                return isChecked ? R.drawable.palette_spring_check : R.drawable.palette_spring;
            case R.id.autumn:
                return isChecked ? R.drawable.palette_autumn_check : R.drawable.palette_autumn;
            case R.id.winter:
                return isChecked ? R.drawable.palette_winter_check : R.drawable.palette_winter;
            default:
                return -1;
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
            Resources res = context.getResources();
            TypedValue val = new TypedValue();

            res.getValue(R.fraction.shadowDx, val, true);
            this.dx = val.getFloat();

            res.getValue(R.fraction.shadowDy, val, true);
            this.dy = 7;

            res.getValue(R.fraction.shadowRadius, val, true);
            this.radius = val.getFloat();
            this.color = res.getColor(R.color.trackInfoShadowColor);
        }
    }

    /**
     * A class that aids in computing an intensity of gray for text so its relative luminance
     * will have a medium level of contrast with respect to the background color in order to reduce
     * the strain on the eyes
     */
    static class ColorPerceptionHelper {
        /**
         * @param color color in the form argb with 1 byte for each channel. Only the rgb channels are used
         * @return Relative Luminance of RGB. Assumes no chroma compression. In that case, the formula is
         * Y = .2126R + .71522G + .0722B
         * https://en.wikipedia.org/wiki/Relative_luminance
         */
        static float computeRelativeLuminance(int color) {
            return .2126f * Color.red(color) + .71522f * Color.green(color) + .0722f * Color.blue(color);
        }

        static int getGray(float relativeLuminance) {
            int y = ((int) relativeLuminance);
            return Color.rgb(y, y, y);
        }

        /**
         * Makes a gray color by transforming the relative luminance in a map where the output is always exactly 128
         * distance from the input. The map is achieved by connecting the endpoints {0, 255} to form a circle,
         * then taking the output as the antipodal point on the circle.
         */
        static int getEquidistantGray(int color) {
            int y = ((int) computeRelativeLuminance(color));
            return getGray(y > 127 ? y - 128 : y + 128);
        }
    }
}
