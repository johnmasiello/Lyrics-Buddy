package com.example.john.lyricsgrabber;

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
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by john on 3/12/18.
 * Content Fragment that displays lyrics
 */
public class LyricFragment extends Fragment {

    // Valid reference until next call to OnCreateOptionsMenu
    private Menu menu;

    private EditText lyrics;
    private KeyListener lyricsEditorListener;


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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.lyric_content_layout, container, false);

        lyrics = rootView.findViewById(R.id.lyrics_body);
        lyricsEditorListener = lyrics.getKeyListener();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.lyric_content_options, menu);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_lyrics:
                item.setVisible(false);
                menu.findItem(R.id.view_lyrics).setVisible(true);

                // Edit the lyrics view, by setting a nonnull key listener
                lyrics.setKeyListener(lyricsEditorListener);
                break;

            case R.id.view_lyrics:
                item.setVisible(false);
                menu.findItem(R.id.edit_lyrics).setVisible(true);

                // Present the lyrics view as un-editable
                lyrics.setKeyListener(null);
                break;

            case R.id.share_lyrics:
                Toast.makeText(getActivity(), "Share Stella Lyrics", Toast.LENGTH_SHORT).show();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
