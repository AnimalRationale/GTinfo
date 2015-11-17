package pl.appnode.gtinfo;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.appnode.gtinfo.Constants.ADD_SERVER_INTENT_REQUEST;
import static pl.appnode.gtinfo.Constants.ADDED_SERVER_ADDRESS;
import static pl.appnode.gtinfo.Constants.ADDED_SERVER_NAME;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_ADDRESS;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_INTENT_REQUEST;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_LIST_POSITION;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_NAME;
import static pl.appnode.gtinfo.Constants.NO_ITEM;
import static pl.appnode.gtinfo.Constants.SELECTED_ITEM_POSITION;
import static pl.appnode.gtinfo.Constants.SERVERS_PREFS_FILE;
import static pl.appnode.gtinfo.GameServerItemListFragment.sServersAdapter;
import static pl.appnode.gtinfo.GameServerItemListFragment.sServersList;
import static pl.appnode.gtinfo.PreferencesSetupHelper.isDarkTheme;
import static pl.appnode.gtinfo.PreferencesSetupHelper.orientationSetup;
import static pl.appnode.gtinfo.PreferencesSetupHelper.themeSetup;


/**
 * An activity representing a list of GameIServersItems. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link GameServerItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link GameServerItemListFragment} and the item details
 * (if present) is a {@link GameServerItemDetailFragment}.
 * <p/>
 */

public class GameServerItemListActivity extends AppCompatActivity
        implements ConfirmationDialogFragment.ConfirmationDialogListener,
        SearchView.OnQueryTextListener {

    private static final String TAG = "GameServerListAct";
    private static boolean sTwoPane;
    private static boolean sPhone;
    private static boolean sThemeChangeFlag;
    private static int sSelected = NO_ITEM;
    private static int sScrollTo = NO_ITEM;
    static List sFilteredServersList = new ArrayList();
    private ActionBar mActionBar;
    private SearchView mSearchView;

    public static boolean isTwoPaneMode() {
        return sTwoPane;
    }

    public static boolean isPhone() {return sPhone;}

    public static int getScrollTo() {return sScrollTo;}

    public static void setScrollTo(int position) {
        sScrollTo = position;
    }

    public static int getSelectedItem() {return sSelected;}

    public static void setSelectedItem(int position) {
        sSelected = position;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            sSelected = savedInstanceState.getInt(SELECTED_ITEM_POSITION);
        }
        themeSetup(this);
        sThemeChangeFlag = isDarkTheme(this);
        Configuration configuration = getResources().getConfiguration();
        if (configuration.smallestScreenWidthDp < 600
                && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_gameserveritem_list_landscape);
            sPhone = true;
        } else setContentView(R.layout.activity_gameserveritem_list);
        if (isDarkTheme(this)) {
            getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.black));
        } else {getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.white));}
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayShowHomeEnabled(true);
            mActionBar.setIcon(R.mipmap.ic_launcher);
        }
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            GameServerItemListFragment fragment = new GameServerItemListFragment();
            transaction.replace(R.id.placeholder_fragment, fragment);
            transaction.commit();
        }
        if (findViewById(R.id.gameserveritem_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            sTwoPane = true;
        } else {
            sTwoPane = false;
            sSelected = NO_ITEM;
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public void onResume() {
        super.onResume();
        orientationSetup(this);
        checkThemeChange();
        Log.d(TAG, "sSelected: " + sSelected + " / ScrollTo: " + sScrollTo);
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        if (isTwoPaneMode() && sSelected != NO_ITEM && sServersList.size() >= sSelected) {
            restoreDetailPane(sSelected);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(item);
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                sFilteredServersList.clear();
                sServersAdapter.notifyDataSetChanged();
                hideKeyboard();
                mSearchView.setBackgroundColor(ContextCompat
                        .getColor(AppContextHelper.getContext(), R.color.dark_action_bar));
                mSearchView.setQuery("", false);
                Log.d(TAG, "Closing search widget.");
                mActionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat
                        .getColor(AppContextHelper.getContext(), R.color.dark_action_bar)));
                sScrollTo = NO_ITEM;
                return true;
            }
        });
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mSearchView.setSubmitButtonEnabled(true);
        if (!sFilteredServersList.isEmpty()) {
            mSearchView.setIconified(false);
            MenuItemCompat.expandActionView(item);
            mSearchView.setQuery(sFilteredServersList.get(0).toString(), false);
            mSearchView.clearFocus();
            mSearchView.setBackgroundColor(ContextCompat.getColor(this, R.color.filtered_list));
            mActionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat
                    .getColor(this, R.color.filtered_list)));
        } else {
            mSearchView.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_action_bar));
            mActionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat
                    .getColor(this, R.color.dark_action_bar)));
        }
        if (!sTwoPane) {
            MenuItem menuAddServer = menu.findItem(R.id.action_add_server);
            menuAddServer.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        ImageView searchCloseButton = (ImageView) mSearchView.findViewById(R.id.search_close_btn);
        if (searchCloseButton != null) {
            searchCloseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sFilteredServersList.clear();
                    sServersAdapter.notifyDataSetChanged();
                    mSearchView.setBackgroundColor(ContextCompat
                            .getColor(AppContextHelper.getContext(), R.color.dark_action_bar));
                    mSearchView.setQuery("", false);
                    mActionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat
                            .getColor(AppContextHelper.getContext(), R.color.dark_action_bar)));
                    sScrollTo = NO_ITEM;
                    mSearchView.requestFocus();
                }
            });
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        hideKeyboard();
        int j = 0;
        sFilteredServersList.add(0, query);
        for (int i = 0; i < sServersList.size(); i++) {
            GameServerItem gameServer = sServersList.get(i);
            if (gameServer.mName.toLowerCase().contains(query.toLowerCase())) {
                sFilteredServersList.add(i);
                if (j == 0) {sScrollTo = i;}
                j++;
            }
        }
        if (j > 0) {
            sServersAdapter.notifyDataSetChanged();
            String info = getResources().getString(R.string.search_action_positive) + j;
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
            mSearchView.setBackgroundColor(ContextCompat.getColor(this, R.color.filtered_list));
            mActionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat
                    .getColor(this, R.color.filtered_list)));
            return true;
        }
        sFilteredServersList.clear();
        Toast.makeText(this, R.string.search_action_negative, Toast.LENGTH_SHORT).show();
        return true;
    }

    private void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            AboutDialog.showDialog(GameServerItemListActivity.this);
        }
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, PreferencesActivity.class);
            this.startActivity(settingsIntent);
        }
        if (id == R.id.action_populate) {
            populateServerList();
        }
        if (id == R.id.action_add_server) {
            showAddServerDialog();
        }
        if (id == R.id.action_clear_list) {
            if (!sServersList.isEmpty()) {
                showConfirmationDialog();
            } else {
                Toast.makeText(this, getResources()
                        .getString(R.string.menu_action_clear_list_empty_info), Toast.LENGTH_SHORT)
                        .show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkThemeChange() {
        if (sThemeChangeFlag != isDarkTheme(this)) {
            finish();
            Intent intent = new Intent(this, GameServerItemListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public void addServer(View fab) {
        showAddServerDialog();
    }

    private void showAddServerDialog() {
        Intent settingsIntent = new Intent(this, AddGameServerActivity.class);
        this.startActivityForResult(settingsIntent, ADD_SERVER_INTENT_REQUEST);
    }

    public void refreshWebView(View fab) {
        if (sSelected != NO_ITEM && !sServersList.isEmpty()) {
            restoreDetailPane(sSelected);
        }
    }

    private void populateServerList() {
        final Map<String, String> SERVERS_EXAMPLE = new HashMap<String, String>() {
            {
                put("1.2.3.4:2000", "Test");
                put ("185.49.14.11:27015", "Skillownia CS:GO PL");
                put("5.39.72.122:2302", "ATD Exile");
                put ("94.23.247.102:2502", "XG Exile Altis");
                put("109.230.249.148:2302", "PvE NL/UK Exile Altis");
                put("37.152.48.105:2302", "DMR PvE");
                put("94.250.209.13:2302", "PvE Der Rentner Exile Altis");
                put("109.236.89.182:2402", "PvE Cranky Exile Altis");
                put("31.186.251.213:2302", "Hostile Takeover EU#1");
                put("100.200.300.400:5555","Test 2");
                put("184.88.43.167:2302", "Alpha1Alpha PvE Exile Altis");

            }
        };
        SharedPreferences serversPrefs = getSharedPreferences(SERVERS_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = serversPrefs.edit();
        for (Map.Entry<String, String> entry : SERVERS_EXAMPLE.entrySet()) {
            GameServerItem gameServer = new GameServerItem();
            gameServer.mId = entry.getKey();
            gameServer.mName = entry.getValue();
            sServersList.add(gameServer);
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.apply();
        sServersAdapter.notifyDataSetChanged();
    }

   private void clearServersList() {
       int range = sServersList.size();
       sServersList.clear();
       sServersAdapter.notifyItemRangeRemoved(0, range);
       sSelected = NO_ITEM;
       sScrollTo= NO_ITEM;
       SharedPreferences gameServersPrefs = getSharedPreferences(SERVERS_PREFS_FILE, 0);
       SharedPreferences.Editor editor = gameServersPrefs.edit();
       editor.clear();
       editor.apply();
       if (isTwoPaneMode() ) {restoreDetailPane(NO_ITEM);}
   }

    private void showConfirmationDialog() {
        DialogFragment dialog = ConfirmationDialogFragment.newInstance(sServersList.size());
        dialog.show(getSupportFragmentManager(), "ConfirmationDialogFragment");
    }

    @Override
    public void onConfirmationDialogPositiveClick(DialogFragment dialog) {
        clearServersList();
        Log.d(TAG, "Cleared list.");
    }

    @Override
    public void onConfirmationDialogNegativeClick(DialogFragment dialog) {
    }

    private void restoreDetailPane(int position) {
        Bundle arguments = new Bundle();
        arguments.putInt(GameServerItemDetailFragment.ARG_ITEM_ID, position);
        GameServerItemDetailFragment fragment = new GameServerItemDetailFragment();
        fragment.setArguments(arguments);
        FragmentManager manager = this.getSupportFragmentManager();
        manager.beginTransaction()
                .add(R.id.gameserveritem_detail_container, fragment)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (requestCode == ADD_SERVER_INTENT_REQUEST && resultCode == RESULT_OK
                && resultIntent.getExtras() != null) {
            String serverAddress = resultIntent.getStringExtra(ADDED_SERVER_ADDRESS);
            String serverName = resultIntent.getStringExtra(ADDED_SERVER_NAME);
            saveServerData(serverAddress, serverName, NO_ITEM);
        } else if (requestCode == EDIT_SERVER_INTENT_REQUEST && resultCode == RESULT_OK
                && resultIntent.getExtras() != null) {
            String serverAddress = resultIntent.getStringExtra(EDIT_SERVER_ADDRESS);
            String serverName = resultIntent.getStringExtra(EDIT_SERVER_NAME);
            int position = resultIntent.getIntExtra(EDIT_SERVER_LIST_POSITION, NO_ITEM);
            if (position != NO_ITEM) {saveServerData(serverAddress, serverName, position);}
        }
    }

    private void saveServerData(String address, String name, int position) {
        SharedPreferences serversPrefs = AppContextHelper.getContext().getSharedPreferences(SERVERS_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = serversPrefs.edit();
        editor.putString(address, name);
        if (position == NO_ITEM) {
            GameServerItem gameServer = new GameServerItem();
            gameServer.mId = address;
            gameServer.mName = name;
            sServersList.add(gameServer);
            sServersAdapter.notifyDataSetChanged();
            Log.d(TAG, "Saved server: " + address + " with name: " + name);
        } else {
            GameServerItem gameServer = sServersList.get(position);
            if (!gameServer.mId.equals(address)) {
                Log.d(TAG, "Editing server address - old: " + gameServer.mId + " / new: " + address);
                if (serversPrefs.contains(gameServer.mId)) {
                    editor.remove(gameServer.mId);
                }
                gameServer.mId = address;
            }
            gameServer.mName = name;
            sServersAdapter.notifyItemChanged(position);
            Log.d(TAG, "Edited server: " + address + " with name: " + name);
        }
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_ITEM_POSITION, sSelected);
        super.onSaveInstanceState(outState);
    }
}
