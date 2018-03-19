package com.example.john.lyricsbuddy;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.SpannableString;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by john on 3/15/18.
 * Customized in order to make more convenient removing selections, clicks, and styling-spans
 */

public class WrappedEditText extends AppCompatEditText {

    private KeyListener lyricsEditorListener;
    private int selectionOffset = 0;

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
            setLongClickable(false);
        }
    }

    public void removeSpansFromText() {
        setText(new SpannableString(getText().toString()),
                TextView.BufferType.SPANNABLE);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        // Preserve the start of the selection offset
        try
        {
            selectionOffset = getSelectionStart();
        } catch (Exception ignore) {
            // A getText() is called, which throws a ClassCastException, when called before
            // setText(). This behavior could change upon changes to the framework
        }

        super.setText(text, type);

        // Set the cursor to the selection offset
        if (selectionOffset <= text.length()) {
            setSelection(selectionOffset, selectionOffset);
        }
    }
}
