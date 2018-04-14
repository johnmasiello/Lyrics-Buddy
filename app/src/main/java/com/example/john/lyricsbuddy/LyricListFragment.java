package com.example.john.lyricsbuddy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.example.john.lyricsbuddy.LyricDatabaseHelper.SongLyricsListItem;

/**
 * Created by john on 4/13/18.
 */

public class LyricListFragment extends Fragment {
    static class LyricsListFragmentAdapter extends ListAdapter<SongLyricsListItem,
            LyricsListFragmentAdapter.SongLyricViewHolder> {

        public LyricsListFragmentAdapter(@NonNull DiffUtil.ItemCallback<SongLyricsListItem> diffCallback) {
            super(diffCallback);
        }


        @NonNull
        @Override
        public SongLyricViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.lyric_list_item_layout, parent, false);

            return new SongLyricViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SongLyricViewHolder holder, int position) {
            // TODO bind data to the view holder
        }

        static class SongLyricViewHolder extends RecyclerView.ViewHolder {
            TextView artist, album, track;

            SongLyricViewHolder(View itemView) {
                super(itemView);
                artist = itemView.findViewById(R.id.artist);
                album = itemView.findViewById(R.id.album);
                track = itemView.findViewById(R.id.track);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lyrics_master_layout, container, false);
    }
}
