package com.example.john.lyricsbuddy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IllegalFormatException;

/**
 * Created by john on 3/15/18.
 * Customized in order to make more convenient removing selections, clicks, and styling-spans
 */

public class WrappedEditText extends AppCompatEditText {

    private KeyListener lyricsEditorListener;
    private TextWatcher textListener;
    private int selectionOffset = 0;

    private static Deque<TextChange> undo, redo;

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

    /**
     * Adds a TextWatcher as on TextChangedListener
     * @param id To refer the Editable view to which the TextWatcher is added
     */
    void addTextWatcher(int id) {
        if (textListener == null) {
            textListener = new UndoTextWatcher(id);
        }
        addTextChangedListener(textListener);
    }

    void removeTextWatcher() {
        removeTextChangedListener(textListener);
    }

    /**
     * Resets and both the undo and redo stacks
     */
    static void resetUndoStack() {
        if (undo == null) {
            undo = new ArrayDeque<>(1000);
        } else {
            undo.clear();
        }
        if (redo == null) {
            redo = new ArrayDeque<>(1000);
        } else {
            redo.clear();
        }
    }

    static void discardRedo() {
        if (redo != null) {
            redo.clear();
        }
    }

    private class UndoTextWatcher implements TextWatcher {
        final int id;
        TextChange textChange;

        UndoTextWatcher(int id) {
            this.id = id;
            textChange = new TextChange(id);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            textChange.start = start;
            textChange.replace = s.subSequence(start, start + count);

            // A change has occurred, invalidating redo stack
            discardRedo();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            textChange.with = s.subSequence(start, start + count);
            Log.d("Texxt", ""+textChange);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    static class TextChange {
        final int id;
        int start;
        CharSequence replace, with;

        TextChange(int id) {
            this.id = id;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public String toString() {
            try {
                return String.format("id: %d text: \"%s\"     with: \"%s\"     start=%d",
                id, replace.toString(), with.toString(), start);
            } catch (IllegalFormatException e) {
                return "Text Change with id: "+ id + " not fully initialized";
            }
        }

        // Todo: write convenience methods for textChange

        /**
         *
         * @param peek the TextChange on the top of the undo stack
         * @return if this can be merged with peek, then both peek is modified by the merge and
         * true is returned. Otherwise, false is returned and this still needs to be pushed onto the stack
         */
        boolean canStateMerge(TextChange peek) {
            if (peek == null || id != peek.id)
                return false;

            if (start == peek.start) {
                if (replace.length() == peek.with.length() &&
                        replace.toString().equals(peek.with.toString())) {

                    peek.with = with;
                    return true;
                }
            } else if (start + 1 == peek.start);

            return false;
        }
    }
}
