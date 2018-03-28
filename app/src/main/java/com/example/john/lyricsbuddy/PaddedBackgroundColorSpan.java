package com.example.john.lyricsbuddy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.style.LineBackgroundSpan;

/**
 * Created by john on 3/27/18.
 * Adds padding to backgroundColorSpan
 * https://medium.com/@tokudu/android-adding-padding-to-backgroundcolorspan-179ab4fae187
 */

class PaddedBackgroundColorSpan implements LineBackgroundSpan {
    private int mPadding = 0;
    private int mBackgroundColor = Color.WHITE;
    private Rect mBgRect;

    PaddedBackgroundColorSpan(int padding, int backgroundColor) {
        this.mPadding = padding;
        this.mBackgroundColor = backgroundColor;

        mBgRect = new Rect();
    }

    @Override
    public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
        final int textWidth = Math.round(p.measureText(text, start, end));
        final int paintColor = p.getColor();
        // Draw the background
        mBgRect.set(left - mPadding,
                top - mPadding,
                right + mPadding,
                bottom + mPadding);
        p.setColor(mBackgroundColor);
        c.drawRect(mBgRect, p);
        p.setColor(paintColor);
    }
}
