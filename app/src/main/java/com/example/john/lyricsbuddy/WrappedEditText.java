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
    public static final int NO_UNDO_ID = -2;

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

    static void ensureUndoStack() {
        if (undo == null) {
            undo = new ArrayDeque<>(1000);
        }
    }

    static void ensureRedoStack() {
        if (redo == null) {
            redo = new ArrayDeque<>(1000);
        }
    }

    static void discardRedo() {
        if (redo != null) {
            redo.clear();
        }
    }

    /**
     * This method can be synchronized, if thread safety is need later on
     * @param textChange Item to push onto stack
     */
    static void pushUndo(TextChange textChange) {
        undo.push(textChange);
    }

    /**
     *
     * @return !undo.isEmpty() -> undo.peek().id; undo.isEmpty() -> {@link #NO_UNDO_ID}
     */
    static int peekUndoId() {
        ensureUndoStack();

        if (undo.isEmpty()) {
            return NO_UNDO_ID;
        } else {
            return undo.peek().id;
        }
    }

    /**
     *
     * @return !redo.isEmpty() -> redo.peek().id; redo.isEmpty() -> {@link #NO_UNDO_ID}
     */
    static int peekRedoId() {
        ensureRedoStack();

        if (redo.isEmpty()) {
            return NO_UNDO_ID;
        } else {
            return redo.peek().id;
        }
    }

    /**
     * Undoes the most recent text change, of which is largely determined by {@link TextChange#canStateMerge(TextChange)}
     * @param editText EditText that should correspond to id=undo.peek().id
     */
    static void undoTextChange(WrappedEditText editText) {
        if (editText == null) {
            return;
        }

        // Disable the textWatcher from editText by temporarily removing it
        editText.removeTextWatcher();

        TextChange tx = undo.pop();

        // Update the editText with TextChange
        editText.getEditableText().replace(tx.start, tx.start + tx.with.length(), tx.replace);

        // Push the undo TextChange back onto the redo stack
        ensureRedoStack();
        redo.push(tx);

        // Enable the textWatcher
        editText.addTextWatcher(tx.id);
    }

    /**
     * Redoes the most recent text change
     * @param editText EditText that should correspond to id=redo.peek().id
     * @see #undoTextChange(WrappedEditText)
     */
    static void redoTextChange(WrappedEditText editText) {
        if (editText == null) {
            return;
        }

        // Disable the textWatcher from editText by temporarily removing it
        editText.removeTextWatcher();

        TextChange tx = redo.pop();

        // Update the editText with TextChange
        editText.getEditableText().replace(tx.start, tx.start + tx.replace.length(), tx.with);

        // Push the redo TextChange back onto the undo stack
        ensureUndoStack();
        undo.push(tx);

        // Enable the textWatcher
        editText.addTextWatcher(tx.id);
    }

    private class UndoTextWatcher implements TextWatcher {
        final int id;
        TextChange textChange;

        UndoTextWatcher(int id) {
            this.id = id;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            textChange = new TextChange(id);
            textChange.start = start;
            textChange.replace = s.subSequence(start, start + count);

            // A change has occurred, invalidating redo stack
            discardRedo();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            textChange.with = s.subSequence(start, start + count);

            ensureUndoStack();
            if (!textChange.canStateMerge(undo.peek())) {
                pushUndo(textChange);
            }
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

        /**
         * <p>Purpose of this method is to chunk text changes into 'larger' undo states</p>
         * @param peek the TextChange on the top of the undo stack
         * @return if this can be merged with peek, then both peek is modified by the merge and
         * true is returned. Otherwise, false is returned and this still needs to be pushed onto the stack
         */
        boolean canStateMerge(TextChange peek) {
            if (peek == null || id != peek.id)
                return false;

            if (start == peek.start) {
                if (peek.replace.length() == 0 && // <- Avoid merging an 'add' with a 'delete' undo step
                        replace.toString().equals(peek.with.toString())) // <- Detect transitive text changes
                    {

                    // Merge characters into single words when adding words
                    peek.with = with;
                    return true;
                }
            } else if (start + 1 == peek.start &&
                    with.length() == 0 &&
                    replace.charAt(0) != ' ') {

                // Merge deleted characters in whole single words

                // Prepend peek's replace with the current replace text
                peek.replace = replace.toString() + peek.replace;

                peek.start = start;
                return true;
            }

            return false;
        }
    }
}