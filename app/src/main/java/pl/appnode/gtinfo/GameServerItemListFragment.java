package pl.appnode.gtinfo;


import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static pl.appnode.gtinfo.Constants.NO_ITEM;
import static pl.appnode.gtinfo.Constants.SERVERS_PREFS_FILE;
import static pl.appnode.gtinfo.Constants.UNDO_TIME;

public class GameServerItemListFragment extends Fragment {

    private static final String TAG = "GSI-List-fragment";
    private static final String TAG_V = "GameServerItemListFragment";
    protected static List<GameServerItem> sServersList = new ArrayList<>();
    protected static GameServersAdapter sServersAdapter;
    protected LinearLayoutManager mLinearLayoutManager;
    private CoordinatorLayout fabCoordinator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initServerList();
        Log.d(TAG, "OnCreate finish.");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "OnCreateView start.");
        View rootView = inflater.inflate(R.layout.fragment_gameserveritem_list, container, false);
        rootView.setTag(TAG_V);
        if (GameServerItemListActivity.isTwoPaneMode()) {
            FloatingActionButton addServerFab = (FloatingActionButton) rootView
                    .findViewById(R.id.fab_add_server);
            addServerFab.setVisibility(View.GONE);
        }
        fabCoordinator = (CoordinatorLayout) rootView.findViewById(R.id.fab_coordinator);
        final RecyclerView recyclerServersList = (RecyclerView) rootView.findViewById(R.id.serversList);
        recyclerServersList.setItemAnimator(null);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerServersList.setLayoutManager(mLinearLayoutManager);
        if (!GameServerItemListActivity.isTwoPaneMode()) {
            RecyclerView.ItemDecoration itemDecoration =
                    new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
            recyclerServersList.addItemDecoration(itemDecoration);
        }
        recyclerServersList.setItemAnimator(new DefaultItemAnimator());
        sServersAdapter = new GameServersAdapter(getActivity());
        recyclerServersList.setHasFixedSize(true);
        recyclerServersList.setAdapter(sServersAdapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper
                .SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target ) {
                return true;
            }
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                removeGameServer(viewHolder.getAdapterPosition());
                clearView(recyclerServersList, viewHolder);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerServersList);
        Log.d(TAG, "OnCreateView finish.");
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume start.");
        if (GameServerItemListActivity.getScrollTo() != NO_ITEM) {
            mLinearLayoutManager.scrollToPositionWithOffset(GameServerItemListActivity.getScrollTo(), 0);
        }
        Log.d(TAG, "onResume finish.");
    }

    private void removeGameServer(final int position) {
        final boolean isFragmentUndo;
        final int selectedItem = GameServerItemListActivity.getSelectedItem();
        if (position == selectedItem && GameServerItemListActivity.isTwoPaneMode()) {
            isFragmentUndo = true;
            GameServerItemListActivity.setSelectedItem(NO_ITEM);
            Log.d(TAG, "Selected NO_ITEM : " + GameServerItemListActivity.getSelectedItem());
            Bundle arguments = new Bundle();
            arguments.putInt(GameServerItemDetailFragment.ARG_ITEM_ID, NO_ITEM);
            GameServerItemDetailFragment fragment = new GameServerItemDetailFragment();
            fragment.setArguments(arguments);
            FragmentActivity activity = getActivity();
            FragmentManager manager = activity.getSupportFragmentManager();
            manager.beginTransaction()
                    .add(R.id.gameserveritem_detail_container, fragment)
                    .commit();
        } else {isFragmentUndo = false;}
        if (position < selectedItem) {
            GameServerItemListActivity.setSelectedItem(selectedItem - 1);
            Log.d(TAG, "Position < selectedItem : " + GameServerItemListActivity.getSelectedItem());
        }
        final GameServerItem gameServer = sServersList.get(position);
        sServersList.remove(position);
        sServersAdapter.notifyItemRemoved(position);
        final SharedPreferences gameServersPrefs = AppContextHelper.getContext()
                .getSharedPreferences(SERVERS_PREFS_FILE, 0);
        final SharedPreferences.Editor editor = gameServersPrefs.edit();
        if (gameServersPrefs.contains(gameServer.mId)) {
            editor.remove(gameServer.mId);
            editor.apply();
        }

        SpannableStringBuilder confirmationRemoved = new SpannableStringBuilder();
        int boldStart = confirmationRemoved.length();
        confirmationRemoved.append(gameServer.mName);
        confirmationRemoved.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.light_green)),
                boldStart, confirmationRemoved.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        confirmationRemoved.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), boldStart,
                confirmationRemoved.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        confirmationRemoved.append(getActivity().getResources().getString(R.string.confirmation_server_removed));
        View snackView;
        if (GameServerItemListActivity.isTwoPaneMode()) {
            if (getActivity().getView().findViewById(R.id.fab_refresh_coordinator) != null) {
                snackView = getView().findViewById(R.id.fab_refresh_coordinator);
                Log.d(TAG, "FAB 2 coordinator");
            } else snackView = getView();
        } else snackView = fabCoordinator;
        //noinspection ResourceType
        Snackbar.make(snackView, confirmationRemoved, UNDO_TIME)
                .setAction(R.string.confirmation_server_removed_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editor.putString(gameServer.mId, gameServer.mName);
                        editor.apply();
                        GameServerItemListActivity.setSelectedItem(selectedItem);
                        sServersList.add(position, gameServer);
                        sServersAdapter.notifyItemInserted(position);
                        if (isFragmentUndo) {
                            Bundle arguments = new Bundle();
                            arguments.putInt(GameServerItemDetailFragment.ARG_ITEM_ID, selectedItem);
                            GameServerItemDetailFragment fragment = new GameServerItemDetailFragment();
                            fragment.setArguments(arguments);
                            FragmentActivity activity = getActivity();
                            FragmentManager manager = activity.getSupportFragmentManager();
                            manager.beginTransaction()
                                    .add(R.id.gameserveritem_detail_container, fragment)
                                    .commit();
                        }
                    }
                })
                .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.icon_orange))
                .show();
    }

    private void initServerList() {
        if (sServersList.isEmpty()) {
            SharedPreferences gameServersPrefs = AppContextHelper.getContext()
                    .getSharedPreferences(SERVERS_PREFS_FILE, 0);
            Map<String, ?> keys = gameServersPrefs.getAll();
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                GameServerItem gameServer = new GameServerItem();
                gameServer.mId = entry.getKey();
                gameServer.mName = entry.getValue().toString();
                sServersList.add(gameServer);
                Log.d(TAG, gameServer.mId + " " + gameServer.mName);
            }
        }
    }
}
