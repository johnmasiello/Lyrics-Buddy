package com.example.john.lyricsbuddy;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.widget.TextView;

import java.util.Arrays;

/**
 * Created by john on 4/27/18.
 * Class that support themes for AppCompatSpinner
 */

public class WebSearchAdapter<T> extends ArrayAdapter<T> implements ThemedSpinnerAdapter {
    private final ThemedSpinnerAdapter.Helper mDropDownHelper;
    private final T[] items;

    public WebSearchAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull T[] objects) {
        super(context, resource, 0, objects);
        items = objects;
        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            // Inflate the drop down using the helper's LayoutInflater
            LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
            view = inflater.inflate(R.layout.web_search_drop_down_item_layout, parent, false);
        } else {
            view = convertView;
        }
        TextView text1 = view.findViewById(R.id.web_search_title_text);
        text1.setText(String.valueOf(items[position]));
        return view;
    }

    @Override
    public void setDropDownViewTheme(@Nullable Resources.Theme theme) {
        mDropDownHelper.setDropDownViewTheme(theme);
    }

    @Nullable
    @Override
    public Resources.Theme getDropDownViewTheme() {
        return mDropDownHelper.getDropDownViewTheme();
    }


}
