package com.example.john.lyricsbuddy;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.method.KeyListener;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

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
        // Preserve the start of the selection offset
        if (getEditableText() != null) {
            selectionOffset = getSelectionStart();
        }

        Editable editable = getEditableText();
        Class[] classes = new Class[] {
                ForegroundColorSpan.class,
                BackgroundColorSpan.class
            };

        Object[] spans;
        int len = editable.length();

        for (Class clasz : classes) {
            spans = editable.getSpans(0, len, clasz);

            for (Object span : spans) {
                editable.removeSpan(span);
            }
        }

        // Set the cursor to the selection offset
        if (selectionOffset <= editable.length() && selectionOffset > -1) {
            setSelection(selectionOffset, selectionOffset);
        }
    }
}
