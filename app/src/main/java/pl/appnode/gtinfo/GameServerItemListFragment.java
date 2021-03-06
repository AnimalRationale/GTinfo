package pl.appnode.gtinfo;

import android.content.SharedPreferences;
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

import static pl.appnode.gtinfo.Constants.FRAGMENT_ARG_ITEM_ID;
import static pl.appnode.gtinfo.Constants.HINT_TIME;
import static pl.appnode.gtinfo.Constants.NO_ITEM;
import static pl.appnode.gtinfo.Constants.SERVERS_PREFS_FILE;
import static pl.appnode.gtinfo.Constants.UNDO_TIME;
import static pl.appnode.gtinfo.GameServerItemListActivity.sServersList;

/**
 * A fragment representing list of all GameServersItems.
 * This fragment is contained in a {@link GameServerItemListActivity}.
 */
public class GameServerItemListFragment extends Fragment {

    private static final String LOGTAG = "GSI-List-fragment";
    private static final String TAG_V = "GameServerItemListFragment";

    static GameServersAdapter sServersAdapter;
    private static RecyclerView recyclerServersList;
    private LinearLayoutManager mLinearLayoutManager;
    private CoordinatorLayout mFabCoordinator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gameserveritem_list, container, false);
        rootView.setTag(TAG_V);
        if (GameServerItemListActivity.isTwoPaneMode()) {
            FloatingActionButton addServerFab = (FloatingActionButton) rootView
                    .findViewById(R.id.fab_add_server);
            addServerFab.setVisibility(View.GONE);
        }
        mFabCoordinator = (CoordinatorLayout) rootView.findViewById(R.id.fab_coordinator);
        recyclerServersList = (RecyclerView) rootView.findViewById(R.id.serversList);
        recyclerServersList.setItemAnimator(null);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerServersList.setLayoutManager(mLinearLayoutManager);
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
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Scrolls list to last clicked card if available
        setListPosition(GameServerItemListActivity.getScrollTo());
        if (sServersList.isEmpty()) showEmptyListHint();
    }

    @Override
    public void onPause() {
        super.onPause();
        int lastFirstVisiblePosition = ((LinearLayoutManager) recyclerServersList.getLayoutManager())
                .findFirstVisibleItemPosition();
        // Stores last visible servers list item position
        GameServerItemListActivity.setScrollTo(lastFirstVisiblePosition);
    }

    // Removes position from list and persistent storage, handles undo action
    private void removeGameServer(final int position) {
        final boolean isFragmentUndo;
        final int selectedItem = GameServerItemListActivity.getSelectedItem();
        if (position == selectedItem && GameServerItemListActivity.isTwoPaneMode()) {
            isFragmentUndo = true;
            GameServerItemListActivity.setSelectedItem(NO_ITEM);
            showDetailFragment(NO_ITEM);
        } else {
            isFragmentUndo = false;
        }
        if (position < selectedItem) {
            GameServerItemListActivity.setSelectedItem(selectedItem - 1);
        }
        final GameServerItem gameServer = sServersList.get(position);
        sServersList.remove(position);
        sServersAdapter.notifyItemRemoved(position);
        sServersAdapter.notifyItemRangeChanged(position, sServersList.size());
        final SharedPreferences gameServersPrefs = AppContextHelper.getContext()
                .getSharedPreferences(SERVERS_PREFS_FILE, 0);
        final SharedPreferences.Editor editor = gameServersPrefs.edit();
        if (gameServersPrefs.contains(gameServer.mId)) {
            editor.remove(gameServer.mId);
            editor.apply();
        }
        // Undo action - snack bar with information and undo button visible for limited time
        SpannableStringBuilder confirmationRemoved = new SpannableStringBuilder();
        int boldStart = confirmationRemoved.length();
        confirmationRemoved.append(gameServer.mName);
        confirmationRemoved.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(),
                R.color.light_green)), boldStart, confirmationRemoved.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        confirmationRemoved.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), boldStart,
                confirmationRemoved.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        confirmationRemoved.append(getActivity().getResources().getString(R.string.confirmation_server_removed));
        View snackView;
        if (GameServerItemListActivity.isTwoPaneMode()) {
            snackView = getView();
        } else snackView = mFabCoordinator;

        if (snackView != null) {
            //noinspection ResourceType
            Snackbar.make(snackView, confirmationRemoved, UNDO_TIME)
            // Workaround for using custom int values in duration without false warnings
                    .setAction(R.string.confirmation_server_removed_undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                editor.putString(gameServer.mId, gameServer.mRating + " " + gameServer.mName);
                                editor.apply();
                                GameServerItemListActivity.setSelectedItem(selectedItem);
                                sServersList.add(position, gameServer);
                                sServersAdapter.notifyItemInserted(position);
                                if (isFragmentUndo) {
                                    showDetailFragment(selectedItem);
                                }
                            }
                        })
                    .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.icon_orange))
                    .show();
        }
    }

    // Displays detail fragment with information about given game server
    private void showDetailFragment(int position) {
        Bundle arguments = new Bundle();
        arguments.putInt(FRAGMENT_ARG_ITEM_ID, position);
        GameServerItemDetailFragment fragment = new GameServerItemDetailFragment();
        fragment.setArguments(arguments);
        FragmentActivity activity = getActivity();
        FragmentManager manager = activity.getSupportFragmentManager();
        manager.beginTransaction()
                .add(R.id.gameserveritem_detail_container, fragment)
                .commit();
    }

    // Scrolls list to previous first visible position
    private void setListPosition(int scrollTo) {
        mLinearLayoutManager.scrollToPosition(scrollTo);
    }


    // Shows snack bar with hint for adding server to list if list is empty
    private void showEmptyListHint() {
        View snackView;
        String hintText;
        if (GameServerItemListActivity.isTwoPaneMode()) {
            snackView = getView();
            hintText = getActivity().getResources().getString(R.string.hint_add_servers_menu);
        } else {
            snackView = mFabCoordinator;
            hintText = getActivity().getResources().getString(R.string.hint_add_servers_fab);
        }
        if (snackView != null) {
            //noinspection ResourceType
            Snackbar.make(snackView, hintText, HINT_TIME)
                    // Workaround for using custom int values in duration without false warnings
                    .setAction(R.string.hint_add_servers_action, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    })
                    .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.light_green))
                    .show();
        }
    }

}
