package com.example.john.lyricsbuddy;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import static com.example.john.lyricsbuddy.LyricDatabaseHelper.SongLyricsListItem;
import static com.example.john.lyricsbuddy.LyricDatabaseHelper.getAppDatabase;

/**
 * A Fragment featuring recyclerView with an underlying listAdapter using Room, LiveData, ViewModel
 * architecture components to implement Reactive Paradigm Pattern
 * Created by john on 4/13/18.
 */

public class LyricListFragment extends Fragment {
    static class LyricsListFragmentAdapter extends ListAdapter<SongLyricsListItem,
            LyricsListFragmentAdapter.SongLyricViewHolder> {

        public LyricsListFragmentAdapter() {
            super(DIFF_CALLBACK);
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getUid(); // The underlying database creates stable ids
        }

        @NonNull
        @Override
        public SongLyricViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.lyric_list_item_layout, parent, false);

            final SongLyricViewHolder viewHolder = new SongLyricViewHolder(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = viewHolder.getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        Log.d("DataListItemClick", "UID="+getItemId(position));
                    }
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull SongLyricViewHolder holder, int position) {
            holder.bindTo(getItem(position));
        }

        static class SongLyricViewHolder extends RecyclerView.ViewHolder {
            TextView artist, album, track;

            SongLyricViewHolder(View itemView) {
                super(itemView);
                artist = itemView.findViewById(R.id.artist);
                album = itemView.findViewById(R.id.album);
                track = itemView.findViewById(R.id.track);
            }

            public void bindTo(SongLyricsListItem item) {
                artist.setText(item.getArtist());
                album.setText(item.getAlbum());
                track.setText(item.getTrackTitle());
            }
        }

        public final static DiffUtil.ItemCallback<SongLyricsListItem> DIFF_CALLBACK =
                new DiffUtil.ItemCallback<SongLyricsListItem>() {
                    @Override
                    public boolean areItemsTheSame(SongLyricsListItem oldItem, SongLyricsListItem newItem) {
                        return oldItem.getUid() == newItem.getUid();
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull SongLyricsListItem oldItem,
                                                      @NonNull SongLyricsListItem newItem) {
                        return oldItem.equals(newItem);
                    }
                };
    }

    private RecyclerView recyclerView;

    public LyricListFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.lyrics_master_layout, container, false);

        // Add and observer to observe changes on the view model for lyric list items to the
        // recyclerView's adapter
        recyclerView = view.findViewById(R.id.lyricList);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        LyricsListFragmentAdapter adapter = new LyricsListFragmentAdapter();
        initializeSongLyricsListItemViewModel(adapter);
        recyclerView.setAdapter(adapter);
    }

    private void initializeSongLyricsListItemViewModel(final ListAdapter<SongLyricsListItem,
            LyricsListFragmentAdapter.SongLyricViewHolder> listAdapter) {

        SongLyricsListViewModel songLyricsListViewModel =
                ViewModelProviders.of(this).get(SongLyricsListViewModel.class);

        // Add an observer to the recyclerView UI
        songLyricsListViewModel
                .fetchSongLyricsListViewModel(getAppDatabase(getActivity()).songLyricsDao())
                .observe(this, new Observer<List<SongLyricsListItem>>() {

                    @Override
                    public void onChanged(@Nullable List<SongLyricsListItem> songLyricsListItems) {
                        listAdapter.submitList(songLyricsListItems);
                    }
                });
    }
}
