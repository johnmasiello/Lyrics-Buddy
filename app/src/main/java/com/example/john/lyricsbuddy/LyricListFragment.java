package com.example.john.lyricsbuddy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.example.john.lyricsbuddy.LyricDatabaseHelper.SongLyricsListItem;
import static com.example.john.lyricsbuddy.LyricDatabaseHelper.getSongLyricDatabase;

interface ListItemClickCallback extends ActionMode.Callback{
    void handleClick(SongLyricsListItem item);

    boolean isSelectMode();

    /**
     *
     * @return Action mode is set to selection mode and it is started
     */
    boolean startSelectionMode();
}

/**
 * A Fragment featuring recyclerView with an underlying listAdapter using Room, LiveData, ViewModel
 * architecture components to implement Reactive Paradigm Pattern
 * Created by john on 4/13/18.
 */
public class LyricListFragment extends Fragment {
    public static final String LYRIC_LIST_FRAGMENT_TAG = "Master Transaction";

    static class LyricsListAdapter extends ListAdapter<SongLyricsListItem,
            LyricsListAdapter.SongLyricViewHolder> {

        @SuppressLint("UseSparseArrays")
        private static final HashMap<Long, Boolean> mIsSelected = new HashMap<>(500);
        private static final String PAYLOAD_CHECKBOX = "Checkbox";
        private ListItemClickCallback mListItemClickCallback;
        /**
         * Does not change data or view state of a single list item
         */
        private View.OnLongClickListener mLongClickListener;

        public LyricsListAdapter(ListItemClickCallback listItemClickCallback) {
            super(DIFF_CALLBACK);
            setHasStableIds(true);
            mListItemClickCallback = listItemClickCallback;

            mLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return mListItemClickCallback != null &&
                            mListItemClickCallback.startSelectionMode();
                }
            };
        }

        private static synchronized void toggleSelected(long id) {
            Boolean isSelected = mIsSelected.get(id);
            mIsSelected.put(id, isSelected == null || !isSelected);
        }

        private static synchronized void setSelectionAll(boolean isSelected) {
            Set<Long> keys = mIsSelected.keySet();

            for (long key : keys) {
                mIsSelected.put(key, isSelected);
            }
        }

        /**
         * <p>Invariant: mIsSelected.get(id) == null implies mIsSelected.put(id, false)</p>
         * @param id from listAdapter.getId(), derived from list item. The adapter must use stable ids
         * @return mIsSelected.get(id) or vacuously false if null is returned
         */
        private static boolean getSelected(long id) {
            Boolean isSelected = mIsSelected.get(id);
            if (isSelected == null) {
                mIsSelected.put(id, false);
                return false;
            } else
                return isSelected;
        }

        /**
         * This method is static because it relies on a data source external to the adapter
         */
        public static long[] getSelectedIds(List<SongLyricsListItem> items) {
            List<Long> ids = new ArrayList<>();

            long id;
            for (SongLyricsListItem item : items) {
                if (getSelected(id = item.getUid())) {
                    ids.add(id);
                }
            }
            int length = ids.size();
            long[] results = new long[length];

            for (int i = 0; i < length; i++)
                results[i] = ids.get(i);

            return results;
        }

        public void selectAll() {
            setSelectionAll(true);
            invalidateSelection();
        }

        public void deselectAll(){
            setSelectionAll(false);
            invalidateSelection();
        }


        public void invalidateSelection() {
            int count = getItemCount();

            if (count > 0)
                notifyItemRangeChanged(0, count, PAYLOAD_CHECKBOX);
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

                    if (position != RecyclerView.NO_POSITION &&
                            mListItemClickCallback != null) {
                        if (mListItemClickCallback.isSelectMode()) {
                            toggleSelected(getItemId(position));
                            notifyItemChanged(position, PAYLOAD_CHECKBOX);
                        } else {
                            mListItemClickCallback.handleClick(getItem(position));
                        }
                    }
                }
            });
            view.setOnLongClickListener(mLongClickListener);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull SongLyricViewHolder holder, int position, @NonNull List<Object> payloads) {
            for (Object obj : payloads) {
                if (obj instanceof String && PAYLOAD_CHECKBOX.equals(obj)) {
                    holder.bindCheckBox(getSelected(getItemId(position)),
                            mListItemClickCallback != null &&
                                    mListItemClickCallback.isSelectMode());
                    return;
                }
            }
            super.onBindViewHolder(holder, position, payloads);
        }

        @Override
        public void onBindViewHolder(@NonNull SongLyricViewHolder holder, int position) {
            holder.bindTo(getItem(position),
                    getSelected(getItemId(position)),
                    isSelectionMode());
        }

        private boolean isSelectionMode() {
            return mListItemClickCallback != null && mListItemClickCallback.isSelectMode();
        }

        static class SongLyricViewHolder extends RecyclerView.ViewHolder {
            TextView artist, album, track;
            CheckBox checkBox;

            SongLyricViewHolder(View itemView) {
                super(itemView);
                artist = itemView.findViewById(R.id.artist);
                album = itemView.findViewById(R.id.album);
                track = itemView.findViewById(R.id.track);
                checkBox = itemView.findViewById(R.id.checkbox);
            }

            public void bindTo(SongLyricsListItem item, boolean isSelected, boolean isSelectable) {
                artist.setText(item.getArtist());
                album.setText(item.getAlbum());
                track.setText(item.getTrackTitle());
                bindCheckBox(isSelected, isSelectable);
            }

            public void bindCheckBox(boolean isSelected, boolean isSelectable) {
                checkBox.setChecked(isSelected);
                checkBox.setVisibility(isSelectable ? View.VISIBLE : View.GONE);
                itemView.setBackgroundColor(isSelectable && isSelected ?
                        itemView.getResources().getColor(R.color.lyricListItemSelectedColor) :
                        Color.TRANSPARENT);
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
    private LyricsListAdapter lyricListAdapter;
    /**
     * selectMode is the only action mode for the activity
     */
    private boolean mSelectMode = false;

    private static final String SELECT_MODE_KEY = "SELECT_MODE";
    private final ListItemClickCallback mListItemClickCallback = new ListItemClickCallback() {
        @Override
        public void handleClick(SongLyricsListItem item) {
            FragmentManager fragmentManager = getFragmentManager();

            if (fragmentManager != null && getActivity() != null) {
                SongLyricDetailItemViewModel detailViewModel =
                        ViewModelProviders.of(getActivity()).get(SongLyricDetailItemViewModel.class);

                // Give the view model the id of the item...
                // so it can fetch the item for the detail view
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

        @Override
        public boolean isSelectMode() {
            return mSelectMode;
        }

        // TODO update SongLyrics in Detail View Model and call Refresh in the List View Model, if SongLyrics was not already in db
        @Override
        public boolean startSelectionMode() {
            if (mSelectMode || lyricListAdapter == null)
                return false;

            Activity activity = getActivity();

            if (activity instanceof AppCompatActivity) {
                mSelectMode = true;
                lyricListAdapter.deselectAll();
                ((AppCompatActivity) getActivity()).startSupportActionMode(this);
            }
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.lyric_list_action_select, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            final int menuId = item.getItemId();

            switch (menuId) {
                case R.id.multiple_selectAll:
                    lyricListAdapter.selectAll();
                    item.setVisible(false);
                    {
                        MenuItem item2 = mode.getMenu().findItem(R.id.multiple_deselectAll);
                        if (item2 != null) {
                            item2.setVisible(true);
                        }
                    }
                    break;

                case R.id.multiple_deselectAll:
                    lyricListAdapter.deselectAll();
                    item.setVisible(false);
                    {
                        MenuItem item2 = mode.getMenu().findItem(R.id.multiple_selectAll);
                        if (item2 != null) {
                            item2.setVisible(true);
                        }
                    }
                    break;

                case R.id.multiple_share:
                case R.id.multiple_trash:
                    final FragmentActivity activity = getActivity();
                    final Fragment fragment = LyricListFragment.this;
                    if (activity != null) {

                        final SongLyricsListViewModel viewModel =
                                ViewModelProviders.of(activity)
                                        .get(SongLyricsListViewModel.class);

                        List<SongLyricsListItem> staticLiveItems =
                                viewModel.getLyricList().getValue();

                        long[] ids = LyricsListAdapter.getSelectedIds(staticLiveItems);
                        viewModel.getSongLyricsDao().fetchSongLyrics(ids)
                            .observe(menuId == R.id.multiple_share ? fragment : activity,
                                    new Observer<List<LyricDatabaseHelper.SongLyrics>>() {

                                // TODO Stop reentry into onChanged?
                                @Override
                                public void onChanged(@Nullable List<LyricDatabaseHelper.SongLyrics> list) {
                                    LyricDatabaseHelper.SongLyrics[] args = toArgs(list);

                                    if (menuId == R.id.multiple_share) {
                                        if (args != null) {
                                            LyricActionHelperKt.share(fragment, args);
                                        } else {
                                            LyricActionHelperKt.failShare(fragment.getContext(), R.string.share_intent_fail_message);
                                        }
                                    } else {
                                        if (args != null) {
                                            SongLyricDetailItemViewModel.RefreshListItemsOnUpdateCallback callback;
                                            callback = new SongLyricDetailItemViewModel.RefreshListItemsOnUpdateCallback(
                                                    new WeakReference<>(viewModel));

                                            new LyricDatabaseHelper.SongLyricAsyncTask(
                                                    viewModel.getSongLyricsDao(),
                                                    LyricDatabaseHelper.SongLyricAsyncTask.DELETE,
                                                    callback)
                                                            .execute(args);
                                        } else {
                                            LyricActionHelperKt.failShare(activity, R.string.share_trash_fail_message);
                                        }
                                    }

                                }
                            });
                    }
                    break;

                default:
                    return false;
            }
            return true;
        }

        private LyricDatabaseHelper.SongLyrics[] toArgs(@Nullable List<LyricDatabaseHelper.SongLyrics> list) {
            LyricDatabaseHelper.SongLyrics[] args = null;

            if (list != null && !list.isEmpty()) {
                args = list.toArray(new LyricDatabaseHelper.SongLyrics[list.size()]);
            }
            return args;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mSelectMode = false;
            lyricListAdapter.invalidateSelection();
        }
    };

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

        mSelectMode = savedInstanceState != null &&
                savedInstanceState.getBoolean(SELECT_MODE_KEY, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        lyricListAdapter = new LyricsListAdapter(mListItemClickCallback);
        initializeSongLyricsListItemViewModel();
        recyclerView.setAdapter(lyricListAdapter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SELECT_MODE_KEY, mSelectMode);
    }

    @SuppressWarnings("ConstantConditions")
    private void initializeSongLyricsListItemViewModel() {

        SongLyricDetailItemViewModel songLyricDetailItemViewModel =
                ViewModelProviders.of(getActivity()).get(SongLyricDetailItemViewModel.class);

        SongLyricsListViewModel songLyricsListViewModel =
                ViewModelProviders.of(getActivity()).get(SongLyricsListViewModel.class);

        songLyricsListViewModel.setSongLyricsDao(getSongLyricDatabase(getActivity()).songLyricsDao());
        songLyricsListViewModel.setSongLyricViewModel(songLyricDetailItemViewModel);
        songLyricDetailItemViewModel.setSongLyricsListViewModel(songLyricsListViewModel);
        // Add an observer register changes from LiveData to the adapter backing the recyclerView
        // to reflect changes in the UI
        songLyricsListViewModel
                .getLyricList()
                .observe(this, new Observer<List<SongLyricsListItem>>() {

                    @Override
                    public void onChanged(@Nullable List<SongLyricsListItem> songLyricsListItems) {
                        lyricListAdapter.submitList(songLyricsListItems);
                    }
                });
    }
}