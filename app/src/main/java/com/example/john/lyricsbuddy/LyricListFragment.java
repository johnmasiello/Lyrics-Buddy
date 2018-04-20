package com.example.john.lyricsbuddy;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import static com.example.john.lyricsbuddy.LyricDatabaseHelper.SongLyricsListItem;
import static com.example.john.lyricsbuddy.LyricDatabaseHelper.getSongLyricDatabase;

interface ListItemClickCallback {
    void handleClick(SongLyricsListItem item);
}

/**
 * A Fragment featuring recyclerView with an underlying listAdapter using Room, LiveData, ViewModel
 * architecture components to implement Reactive Paradigm Pattern
 * Created by john on 4/13/18.
 */
public class LyricListFragment extends Fragment implements ListItemClickCallback {
    public static final String LYRIC_LIST_FRAGMENT_TAG = "Master Transaction";

    static class LyricsListFragmentAdapter extends ListAdapter<SongLyricsListItem,
            LyricsListFragmentAdapter.SongLyricViewHolder> {

        private ListItemClickCallback mListItemClickCallback;

        public LyricsListFragmentAdapter() {
            this(null);
        }

        public LyricsListFragmentAdapter(ListItemClickCallback listItemClickCallback) {
            super(DIFF_CALLBACK);
            setHasStableIds(true);
            mListItemClickCallback = listItemClickCallback;
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

                    if (position != RecyclerView.NO_POSITION && mListItemClickCallback != null) {
                        mListItemClickCallback.handleClick(getItem(position));
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

        recyclerView = view.findViewById(R.id.lyricList);
        Context context = recyclerView.getContext();
        DividerItemDecoration itemDecor = new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL);
        itemDecor.setDrawable(getResources().getDrawable(R.drawable.list_item_divider));
        recyclerView.addItemDecoration(itemDecor);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        LyricsListFragmentAdapter adapter = new LyricsListFragmentAdapter(this);
        initializeSongLyricsListItemViewModel(adapter);
        recyclerView.setAdapter(adapter);
    }


    @SuppressWarnings("ConstantConditions")
    private void initializeSongLyricsListItemViewModel(final ListAdapter<SongLyricsListItem,
            LyricsListFragmentAdapter.SongLyricViewHolder> listAdapter) {

        SongLyricDetailItemViewModel songLyricDetailItemViewModel =
                ViewModelProviders.of(getActivity()).get(SongLyricDetailItemViewModel.class);

        SongLyricsListViewModel songLyricsListViewModel =
                ViewModelProviders.of(this).get(SongLyricsListViewModel.class);

        songLyricsListViewModel.setSongLyricsDao(getSongLyricDatabase(getActivity()).songLyricsDao());
        songLyricsListViewModel.setSongLyricViewModel(songLyricDetailItemViewModel);
        // Add an observer register changes from LiveData to the adapter backing the recyclerView
        // to reflect changes in the UI
        songLyricsListViewModel
                .getLyricList()
                .observe(this, new Observer<List<SongLyricsListItem>>() {

                    @Override
                    public void onChanged(@Nullable List<SongLyricsListItem> songLyricsListItems) {
                        listAdapter.submitList(songLyricsListItems);
                    }
                });
    }

    @Override
    public void handleClick(SongLyricsListItem item) {
        FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager != null && getActivity() != null) {
            SongLyricDetailItemViewModel detailViewModel =
                    ViewModelProviders.of(getActivity()).get(SongLyricDetailItemViewModel.class);

            detailViewModel.setId(item.getUid());

            MainActivityViewModel activityViewModel = ViewModelProviders.of(getActivity())
                    .get(MainActivityViewModel.class);

            if (activityViewModel.isTwoPane()) {
                if (fragmentManager.findFragmentById(R.id.lyric_detail_container) == null) {
                    fragmentManager.beginTransaction()
                            .add(R.id.lyric_detail_container, new LyricFragment(),
                                    LyricFragment.DETAIL_FRAGMENT_TAG)
                            .commit();
                }
            } else {
                fragmentManager.beginTransaction()
                        .replace(R.id.lyric_master_container, new LyricFragment(),
                                LyricFragment.DETAIL_FRAGMENT_TAG)
                        .commit();

                // Show the Up button in the action bar.
                Activity activity = getActivity();
                if (activity instanceof AppCompatActivity) {
                    ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setDisplayHomeAsUpEnabled(true);
                    }
                }
            }
        }
    }
}
