package com.example.john.lyricsbuddy;

import android.arch.lifecycle.ViewModel;

/**
 * Created by john on 4/18/18.
 */

public class MainActivityViewModel extends ViewModel {
    private boolean isTwoPane;

    public boolean isTwoPane() {
        return isTwoPane;
    }

    public void setTwoPane(boolean twoPane) {
        isTwoPane = twoPane;
    }
}
