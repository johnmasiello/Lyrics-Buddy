package com.example.john.lyricsbuddy;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 4/22/18.
 * <p>Class used to resolve content on ACTION_VIEW by loading into app</p>
 * <br>
 * In order to simplify my development of this app, I avoided implementing a contentProvider,
 * <p> as I do not intend for other apps to modify the content of this app.
 * Instead, the content takes the form of a JSONArray containing JSONObjects each
 * backing a SongLyrics Object</p>
 * <br>
 * <p>Avoids leaks by using a WeakReference to the View Model and Activity</p>
 */
class ActionImportContentTask extends
        AsyncTask<Uri, Void, LyricDatabaseHelper.SongLyrics[]> {

    private final WeakReference<MainActivity> mMainActivity;
    private final WeakReference<SongLyricsListViewModel> mListViewModel;

    public ActionImportContentTask(MainActivity main,
                                   SongLyricsListViewModel listViewModel) {
        mMainActivity = new WeakReference<>(main);
        mListViewModel = new WeakReference<>(listViewModel);
    }

    @Override
    protected LyricDatabaseHelper.SongLyrics[] doInBackground(Uri... uris) {
        JsonReader jsonReader = null;
        List<LyricDatabaseHelper.SongLyrics> result = new ArrayList<>();
        {
            MainActivity act = mMainActivity.get();
            if (act != null) {
                try {
                    InputStream in = act.getContentResolver().openInputStream(uris[0]);

                    if (in == null) {
                        Log.e("Import", "Error fetching inputStream: " +
                                "content not resolved");
                        return null;
                    }
                    InputStreamReader reader = new InputStreamReader(in);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    jsonReader = new JsonReader(bufferedReader);
                } catch (FileNotFoundException e) {
                    Log.e("Import", e.getMessage());
                    return null;
                }
            }
        }
        if (jsonReader == null) {
            Log.e("Import", "Error fetching inputStream");
            return null;
        }
        try {
            String[] fields = new String[4];

            jsonReader.beginArray();

            while (jsonReader.hasNext()) {
                jsonReader.beginObject();
                jsonReader.nextName();
                fields[0] = jsonReader.nextString();
                jsonReader.nextName();
                fields[1] = jsonReader.nextString();
                jsonReader.nextName();
                fields[2] = jsonReader.nextString();
                jsonReader.nextName();
                fields[3] = jsonReader.nextString();
                jsonReader.endObject();

                result.add(new LyricDatabaseHelper.SongLyrics(
                        fields[0],
                        fields[1],
                        fields[2],
                        fields[3]));

                if (isCancelled()) {
                    break;
                }
            }
            jsonReader.endArray();
        } catch (IOException e) {
            Log.e("Import", "Parsing Error: " +
                    e.getMessage());
            Log.e("Import", "" + result.size() + " total objects read");
        } finally {
            try {
                jsonReader.close();
            } catch (IOException e) {
                Log.e("Import", "Closing inputStream: " + e.getMessage());
            }
        }
        return result.toArray(new LyricDatabaseHelper.SongLyrics[result.size()]);
    }

    @Override
    protected void onPostExecute(LyricDatabaseHelper.SongLyrics[] songLyrics) {
        if (songLyrics == null || songLyrics.length == 0) {
            return;
        }
        SongLyricsListViewModel viewModel = mListViewModel.get();

        if (viewModel != null) {
            LyricDatabaseHelper.SongLyricsDao dao = viewModel.getSongLyricsDao();

            // Persist the imported content, using another asyncTask
            if (dao != null) {
                LyricDatabaseHelper.SongLyricAsyncTask task =
                        new LyricDatabaseHelper.SongLyricAsyncTask(dao,
                                LyricDatabaseHelper.SongLyricAsyncTask.INSERT_ALL,
                                new SongLyricDetailItemViewModel
                                        .RefreshListItemsOnUpdateCallback(mListViewModel));
                task.execute(songLyrics);
            }
        }
    }
}
