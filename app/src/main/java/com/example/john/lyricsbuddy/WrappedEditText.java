package com.example.john.lyricsbuddy;

import android.content.Context;
import android.text.SpannableString;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatEditText;
import android.widget.TextView;

/**
 * Created by john on 3/15/18.
 * Customized in order to make more convenient removing selections, clicks, and styling-spans
 */

public class WrappedEditText extends AppCompatEditText {

    private KeyListener lyricsEditorListener;

    public WrappedEditText(Context context) {
        super(context);
        init();
    }

    public WrappedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WrappedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {
        lyricsEditorListener = getKeyListener();
    }

    public void setEditable(boolean editable) {
        if (editable) {
            // Enable edits, by setting a nonnull key listener
            setKeyListener(lyricsEditorListener);
        } else {
            // Disable edits, including long-clicks; remove selections
            setKeyListener(null);
            setSelection(0);
            setLongClickable(false);
        }
    }

    public void removeSpansFromText() {
        setText(new SpannableString(getText().toString()),
                TextView.BufferType.SPANNABLE);
    }
}
