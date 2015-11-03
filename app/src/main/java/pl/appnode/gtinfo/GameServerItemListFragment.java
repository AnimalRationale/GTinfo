package pl.appnode.gtinfo;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static pl.appnode.gtinfo.Constants.NO_ITEM;
import static pl.appnode.gtinfo.Constants.SERVERS_PREFS_FILE;

public class GameServerItemListFragment extends Fragment {

    private static final String TAG = "GSI-List-fragment";
    private static final String TAG_V = "GameServerItemListFragment";
    protected static List<GameServerItem> sServersList = new ArrayList<>();
    protected static GameServersAdapter sServersAdapter;
    protected LinearLayoutManager mLinearLayoutManager;

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
        RecyclerView recyclerServersList = (RecyclerView) rootView.findViewById(R.id.serversList);
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
        recyclerServersList.setAdapter(sServersAdapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper
                .SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target ) {
                return true;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                removeGameServer(viewHolder.getAdapterPosition());
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

    private void removeGameServer(int position) { //TODO: Undo
        int selectedItem = GameServerItemListActivity.getSelectedItem();
        if (position == selectedItem) {
            GameServerItemListActivity.setSelectedItem(NO_ITEM);
            Log.d(TAG, "Selected NO_ITEM : " + GameServerItemListActivity.getSelectedItem());
            Bundle arguments = new Bundle();
            arguments.putInt(GameServerItemDetailFragment.ARG_ITEM_ID, NO_ITEM);
            GameServerItemDetailFragment fragment = new GameServerItemDetailFragment();
            fragment.setArguments(arguments);
            FragmentActivity activity = getActivity();
            FragmentManager manager = activity.getSupportFragmentManager();
            Log.d(TAG, "Detail fragment transaction commit.");
            manager.beginTransaction()
                    .add(R.id.gameserveritem_detail_container, fragment)
                    .commit();
        }
        if (position < selectedItem) {
            GameServerItemListActivity.setSelectedItem(selectedItem - 1);
            Log.d(TAG, "Position < selectedItem : " + GameServerItemListActivity.getSelectedItem());
        }
        GameServerItem gameServer = sServersList.get(position);
        sServersList.remove(position);
        sServersAdapter.notifyDataSetChanged();
        SharedPreferences gameServersPrefs = AppContextHelper.getContext()
                .getSharedPreferences(SERVERS_PREFS_FILE, 0);
        if (gameServersPrefs.contains(gameServer.mId)) {
            SharedPreferences.Editor editor = gameServersPrefs.edit();
            editor.remove(gameServer.mId);
            editor.apply();
        }
        String confirmation = gameServer.mName + getActivity().getResources()
                .getString(R.string.confirmation_server_removed);
        Toast toast = Toast.makeText(getActivity(),
                confirmation, Toast.LENGTH_SHORT);
        toast.show();
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
