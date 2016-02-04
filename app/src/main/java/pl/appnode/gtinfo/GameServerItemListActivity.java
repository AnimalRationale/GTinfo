package pl.appnode.gtinfo;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static pl.appnode.gtinfo.Constants.ADDED_SERVER_RATING;
import static pl.appnode.gtinfo.Constants.ADD_SERVER_INTENT_REQUEST;
import static pl.appnode.gtinfo.Constants.ADDED_SERVER_ADDRESS;
import static pl.appnode.gtinfo.Constants.ADDED_SERVER_NAME;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_ADDRESS;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_INTENT_REQUEST;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_LIST_POSITION;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_NAME;
import static pl.appnode.gtinfo.Constants.FRAGMENT_ARG_ITEM_ID;
import static pl.appnode.gtinfo.Constants.NO_ITEM;
import static pl.appnode.gtinfo.Constants.RATING_0_STARS;
import static pl.appnode.gtinfo.Constants.SELECTED_ITEM_POSITION;
import static pl.appnode.gtinfo.Constants.SERVERS_PREFS_FILE;
import static pl.appnode.gtinfo.GameServerItemListFragment.sServersAdapter;
import static pl.appnode.gtinfo.PreferencesSetupHelper.isDarkTheme;
import static pl.appnode.gtinfo.PreferencesSetupHelper.orientationSetup;
import static pl.appnode.gtinfo.PreferencesSetupHelper.themeSetup;


/**
 * An activity representing a list of GameIServersItems. This activity
 * has different presentations for handset in portrait orientation
 * and tablet-size devices. On handsets in portrait orientation, the activity
 * presents a list of items, which when touched,
 * lead to a {@link GameServerItemDetailActivity} representing
 * item details.
 * On tablets and handsets in landscape orientation (when landscape
 * is enabled in settings), activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes use of fragments. The list of items is a
 * {@link GameServerItemListFragment} and the item details
 * (if present) is a {@link GameServerItemDetailFragment}.
 * <p/>
 */

public class GameServerItemListActivity extends AppCompatActivity
        implements ConfirmationDialogFragment.ConfirmationDialogListener,
        SearchView.OnQueryTextListener {

    private static final String LOGTAG = "GameServerListAct";
    private static boolean sTwoPane; // Flag for using two pane mode
    private static boolean sPhone; // Flag indicating handset (portrait: list, landscape 2 panes)
    private static boolean sThemeChangeFlag;
    private static int sSelected = NO_ITEM; // Last selected item from list, NO_ITEM if not available
    private static int sScrollTo = NO_ITEM; // desired position of list, NO_ITEM if not available
    static List<GameServerItem> sServersList = new ArrayList<>();
    static String sSearchQuery = "";
    static List<String> sQueryHistory = new ArrayList<>(); // List of queries entered in search widget
    static List<GameServerItem> sFilteredServersList = new ArrayList<>(); // Helper collection for keeping search results
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
        // Check for handset in landscape
        Configuration configuration = getResources().getConfiguration();
        if (configuration.smallestScreenWidthDp < 600
                && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_gameserveritem_list_landscape);
            sPhone = true;
        } else setContentView(R.layout.activity_gameserveritem_list);
        setupWindowAnimations();
        // Setting window background color accordingly to settings
        if (isDarkTheme(this)) {
            getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.black));
        } else {getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.white));}
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayShowHomeEnabled(true);
            mActionBar.setIcon(R.mipmap.ic_launcher);
        }
        if (savedInstanceState == null) {
            // Check if local dataset version is the same as app's data current schema, run migrations if necessary
            runDatasetMigrationsToVersion(GameServerItem.DATASET_VERSION);
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
        if (sSearchQuery.equals("")) {initServerList();}

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public void onResume() {
        super.onResume();
        orientationSetup(this);
        checkThemeChange();
        Log.d(LOGTAG, "sSelected: " + sSelected + " / ScrollTo: " + sScrollTo);
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        // In two pane mode restore detail view if available
        if (isTwoPaneMode() && sSelected != NO_ITEM && sSelected < sServersList.size()) {
            restoreDetailPane(sSelected);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        // Init for showing and handling search widget in action bar
        mSearchView = (SearchView) MenuItemCompat.getActionView(item);
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search),
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        sFilteredServersList.clear();
                        sSearchQuery = "";
                        sServersList.clear();
                        initServerList();
                        sServersAdapter.notifyDataSetChanged();
                        hideKeyboard();
                        mSearchView.setBackgroundColor(ContextCompat
                                .getColor(AppContextHelper.getContext(), R.color.dark_action_bar));
                        mSearchView.setQuery(sSearchQuery, false);
                        Log.d(LOGTAG, "Closing search widget.");
                        mActionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat
                                .getColor(AppContextHelper.getContext(), R.color.dark_action_bar)));
                        sScrollTo = NO_ITEM;
                        sSelected = NO_ITEM;
                        return true;
                    }
        });
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mSearchView.setSubmitButtonEnabled(true);
        if (!sSearchQuery.equals("")) {
            // Configuring search widget if it is currently in use (filtered list)
            mSearchView.setIconified(false);
            MenuItemCompat.expandActionView(item);
            mSearchView.setQuery(sSearchQuery, false);
            mSearchView.clearFocus();
            mSearchView.setBackgroundColor(ContextCompat.getColor(this, R.color.filtered_list));
            mActionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat
                    .getColor(this, R.color.filtered_list)));
        } else {
            mSearchView.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_action_bar));
            mActionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat
                    .getColor(this, R.color.dark_action_bar)));
        }
        MenuItem menuShareServer = menu.findItem(R.id.action_share_server);
        if (!sTwoPane) {
            // Hiding action button for adding server in portrait mode (using FAB instead)
            MenuItem menuAddServer = menu.findItem(R.id.action_add_server);
            menuAddServer.setVisible(false);
            // Hiding action button for sharing server in one pane mode
            menuShareServer.setVisible(false);
        }
        return true;
    }

    /**
     *  Handles use of close button in active search widget.
     */
    @Override
    public boolean onQueryTextChange(String query) {
        ImageView searchCloseButton = (ImageView) mSearchView.findViewById(R.id.search_close_btn);
        if (searchCloseButton != null) {
            searchCloseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sFilteredServersList.clear();
                    sSearchQuery = "";
                    sServersList.clear();
                    initServerList();
                    sServersAdapter.notifyDataSetChanged();
                    mSearchView.setBackgroundColor(ContextCompat
                            .getColor(AppContextHelper.getContext(), R.color.dark_action_bar));
                    mSearchView.setQuery(sSearchQuery, false);
                    mActionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat
                            .getColor(AppContextHelper.getContext(), R.color.dark_action_bar)));
                    sScrollTo = NO_ITEM;
                    mSearchView.requestFocus();
                }
            });
        }
        return false;
    }

    /**
     * Handles success and fail of search for entered string in servers list.
     */
    // TODO: search queries history indicator, getting back in search with up arrow?
    @Override
    public boolean onQueryTextSubmit(String query) {
        hideKeyboard();
        int j = 0; // Matching results counter
        String[] queryWords = query.split(" ");
        sFilteredServersList.clear();
        sSearchQuery = query;
        for (int i = 0; i < sServersList.size(); i++) {
            GameServerItem gameServer = sServersList.get(i);
            for (int q = 0; q < queryWords.length; q++) {
                if (!gameServer.mName.toLowerCase().contains(queryWords[q].toLowerCase())) {
                        q = queryWords.length;
                } else
                    if (q == queryWords.length - 1) {
                        sFilteredServersList.add(gameServer);
                        j++;
                    }
            }
        }
        if (j > 0) {
            sServersList.clear();
            sServersList.addAll(sFilteredServersList);
            sServersAdapter.notifyDataSetChanged();
            String info = getResources().getString(R.string.search_action_positive) + j;
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
            mSearchView.setBackgroundColor(ContextCompat.getColor(this, R.color.filtered_list));
            mActionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat
                    .getColor(this, R.color.filtered_list)));
            sQueryHistory.add(query);
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
        // Debug tool option for zeroing local dataset version
        if (id == R.id.action_reset_dataset_version) {
            setLocalDatasetVersion(0);
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
        if (id == R.id.action_share_server) {
            shareServer();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupWindowAnimations() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Fade fade = new Fade();
            fade.setDuration(3000);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {getWindow().setExitTransition(fade);}
        }
    }

    private void checkThemeChange() { // Restarts activity if user changed theme
        if (sThemeChangeFlag != isDarkTheme(this)) {
            finish();
            Intent intent = new Intent(this, GameServerItemListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    /**
     *  Handles main menu action for adding servers to list.
     *
     *  @param fab clicked add server floating action button (visible on handsets in portrait mode)
     */
    public void addServer(View fab) {
        showAddServerDialog();
    }

    private void showAddServerDialog() {
        Intent settingsIntent = new Intent(this, AddGameServerActivity.class);
        this.startActivityForResult(settingsIntent, ADD_SERVER_INTENT_REQUEST);
    }

    /**
     * Refreshes detail view pane.
     *
     * @param fab clicked floating action button for refresh HTML server info component (visible in
     *            any two pane mode)
     */
    public void refreshWebView(View fab) {
        if (sSelected != NO_ITEM && !sServersList.isEmpty()) {
            restoreDetailPane(sSelected);
        }
    }

    // Shows app chooser for sharing game server text data (server name and IP:port) in 2 pane mode
    private void shareServer() {
        if (isTwoPaneMode() && sSelected != NO_ITEM && sSelected < sServersList.size()) {
            GameServerItem gameServer = sServersList.get(sSelected);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, gameServer.mName + " " + gameServer.mId);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent,
                    getResources().getText(R.string.share_server_chooser_title)));
        }
    }

    // Helper/debug tool to populate app data set with example content
    private void populateServerList() {
        final Map<String, String> SERVERS_EXAMPLE = new ArrayMap<String, String>() {
            {
                put("1.2.3.4:2000", "Test");
                put ("185.49.14.11:27015", "Skillownia CS:GO PL");
                put("5.39.72.122:2302", "ATD Exile");
                put ("94.23.247.102:2502", "XG Exile Altis");
                put("109.230.249.148:2302", "PvE NL/UK Exile Altis");
                put("37.152.48.105:2302", "DMR PvE");
                put("94.250.209.13:2302", "Z_PvE Der Rentner Exile Altis");
                put("109.236.89.182:2402", "3_PvE Cranky Exile Altis");
                put("31.186.251.213:2302", "2_Hostile Takeover EU#1");
                put("100.200.300.400:5555","1_Test 2");
                put("184.88.43.167:2302", "Alpha1Alpha PvE Exile Altis");
                put("185.38.151.161:2442", "UK CiC PvE Exile Altis");
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

    // Erases app's data set
    private void clearServersList() {
       int range = sServersList.size();
       sServersList.clear();
       sServersAdapter.notifyItemRangeRemoved(0, range);
       sSelected = NO_ITEM;
       sScrollTo = NO_ITEM;
       SharedPreferences gameServersPrefs = getSharedPreferences(SERVERS_PREFS_FILE, 0);
       SharedPreferences.Editor editor = gameServersPrefs.edit();
       editor.clear();
       editor.apply();
       if (isTwoPaneMode() ) {restoreDetailPane(NO_ITEM);}
    }

    private void checkDatasetVersio() {

    }

    private void runDatasetMigrationsToVersion(int currentDatasetVersion) {
        int localDatasetVersion = getLocalDatasetVersion();
        if (localDatasetVersion >= 0 && localDatasetVersion < currentDatasetVersion) {
            Log.d(LOGTAG, "Running migrations from version " + localDatasetVersion +" to version " + currentDatasetVersion);
            for (int i = localDatasetVersion; i < currentDatasetVersion; i++) {
                switch (i) {
                    case 0:
                        Log.d(LOGTAG, "Migration 0 -> 1");
                        setLocalDatasetVersion(i + 1);
                        break;
                    case 1:
                        Log.d(LOGTAG, "Migration 1 -> 2");
                        setLocalDatasetVersion(i + 1);
                        break;
                    case 2:
                        Log.d(LOGTAG, "Migration 2 -> 3");
                        setLocalDatasetVersion(i + 1);
                        break;
                    case 3:
                        Log.d(LOGTAG, "Migration 3 -> 4");
                        setLocalDatasetVersion(i + 1);
                        break;
                    case 4:
                        Log.d(LOGTAG, "Migration 4 -> 5");
                        setLocalDatasetVersion(i + 1);
                        break;
                    case 5:
                        Log.d(LOGTAG, "Migration 5 -> 6");
                        setLocalDatasetVersion(i + 1);
                        break;
                    default:
                        Log.d(LOGTAG, "No migrations needed -- i=" + i);
                }
            }
        } else {
            Log.d(LOGTAG, "No migration, local=" + localDatasetVersion + " current=" + currentDatasetVersion);
        }
    }

    // Gets version of local dataset
    private int getLocalDatasetVersion() {
        int localDatasetVersion = -1;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.contains("settings_dataset_version")) {
            localDatasetVersion = settings.getInt("settings_dataset_version", -1);
            Log.d(LOGTAG, "Key exists, local dataset version: " + localDatasetVersion);
            if (localDatasetVersion == -1) {
                localDatasetVersion = 0;
                settings.edit().putInt("settings_dataset_version", localDatasetVersion).apply();
                Log.d(LOGTAG, "Key exists, but had improper value, set to: " + localDatasetVersion);
            }
        } else {
            localDatasetVersion = 0;
            settings.edit().putInt("settings_dataset_version", localDatasetVersion).apply();
            Log.d(LOGTAG, "Key was created, local dataset version: " + localDatasetVersion);
        }
        return localDatasetVersion;
    }

    //Sets version of local dataset
    private void setLocalDatasetVersion(int version) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.edit().putInt("settings_dataset_version", version).apply();
        Log.d(LOGTAG, "Local dataset version set to: " + version);
    }

    private void showConfirmationDialog() {
        DialogFragment dialog = ConfirmationDialogFragment.newInstance(sServersList.size());
        dialog.show(getSupportFragmentManager(), "ConfirmationDialogFragment");
    }

    /**
     * Handles click of positive button in confirmation dialog.
     */
    @Override
    public void onConfirmationDialogPositiveClick(DialogFragment dialog) {
        clearServersList();
        Log.d(LOGTAG, "List cleared.");
    }

    @Override
    public void onConfirmationDialogNegativeClick(DialogFragment dialog) {
    }

    // Sets up arguments and starts detail fragment
    private void restoreDetailPane(int position) {
        Bundle arguments = new Bundle();
        arguments.putInt(FRAGMENT_ARG_ITEM_ID, position);
        GameServerItemDetailFragment fragment = new GameServerItemDetailFragment();
        fragment.setArguments(arguments);
        FragmentManager manager = this.getSupportFragmentManager();
        manager.beginTransaction()
                .add(R.id.gameserveritem_detail_container, fragment)
                .commit();
    }

    /**
     * Handles results from AddGameServerActivity - initiates adding server to data set
     * or editing current data.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (requestCode == ADD_SERVER_INTENT_REQUEST && resultCode == RESULT_OK
                && resultIntent.getExtras() != null) {
            String serverAddress = resultIntent.getStringExtra(ADDED_SERVER_ADDRESS);
            String serverName = resultIntent.getStringExtra(ADDED_SERVER_NAME);
            String serverRating = resultIntent.getStringExtra(ADDED_SERVER_RATING);
            saveServerData(serverAddress, serverName, serverRating, NO_ITEM);
        } else if (requestCode == EDIT_SERVER_INTENT_REQUEST && resultCode == RESULT_OK
                && resultIntent.getExtras() != null) {
            String serverAddress = resultIntent.getStringExtra(EDIT_SERVER_ADDRESS);
            String serverName = resultIntent.getStringExtra(EDIT_SERVER_NAME);
            String serverRating = resultIntent.getStringExtra(ADDED_SERVER_RATING);
            int position = resultIntent.getIntExtra(EDIT_SERVER_LIST_POSITION, NO_ITEM);
            if (position != NO_ITEM) {saveServerData(serverAddress, serverName, serverRating, position);}
        }
    }

    // Saves new item in data set or modifies existing data (if server address is edited
    // then removes old item and saves new one; server IP:PORT address is ID key for data set)
    private void saveServerData(String address, String name, String rating, int position) {
        SharedPreferences serversPrefs = AppContextHelper.getContext().getSharedPreferences(SERVERS_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = serversPrefs.edit();
        editor.putString(address, name);
        if (position == NO_ITEM) {
            GameServerItem gameServer = new GameServerItem();
            gameServer.mId = address;
            gameServer.mName = name;
            gameServer.mRating = rating;
            sServersList.add(gameServer);
            sServersAdapter.notifyDataSetChanged();
            Log.d(LOGTAG, "Saved server: " + address + " with name: " + name);
        } else {
            GameServerItem gameServer = sServersList.get(position);
            if (!gameServer.mId.equals(address)) {
                Log.d(LOGTAG, "Editing server address - old: " + gameServer.mId + " / new: " + address);
                if (serversPrefs.contains(gameServer.mId)) {
                    editor.remove(gameServer.mId);
                }
                gameServer.mId = address;
            }
            gameServer.mName = name;
            gameServer.mRating = rating;
            sServersAdapter.notifyItemChanged(position);
            Log.d(LOGTAG, "Edited server: " + address + " with name: " + name);
        }
        editor.apply();
    }

    // Initialises list with servers data from persistent storage (shared preferences)
    private void initServerList() {
        if (sServersList.isEmpty()) {
            SharedPreferences gameServersPrefs = AppContextHelper.getContext()
                    .getSharedPreferences(SERVERS_PREFS_FILE, 0);
            Map<String, ?> keys = gameServersPrefs.getAll();
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                GameServerItem gameServer = new GameServerItem();
                gameServer.mId = entry.getKey();
                gameServer.mName = entry.getValue().toString();
                gameServer.mRating = RATING_0_STARS;
                sServersList.add(gameServer);
                Log.d(LOGTAG, gameServer.mId + " " + gameServer.mName);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_ITEM_POSITION, sSelected);
        super.onSaveInstanceState(outState);
    }
}
