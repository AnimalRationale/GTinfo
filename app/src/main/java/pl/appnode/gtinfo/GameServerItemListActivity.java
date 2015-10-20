package pl.appnode.gtinfo;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import static pl.appnode.gtinfo.Constants.ADD_SERVER_INTENT_REQUEST;
import static pl.appnode.gtinfo.Constants.ADDED_SERVER_ADDRESS;
import static pl.appnode.gtinfo.Constants.ADDED_SERVER_NAME;
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

public class GameServerItemListActivity extends AppCompatActivity {

    private static final String TAG = "GameServerListAct";
    private static boolean mTwoPane;
    private static boolean sThemeChangeFlag;
    private static int sSelected = NO_ITEM;
    private static int sScrollTo = NO_ITEM;

    public static boolean isTwoPaneMode() {
        return mTwoPane;
    }

    public static int getsScrollTo() {return sScrollTo;}

    public static void setsScrollTo(int position) {
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
        } else setContentView(R.layout.activity_gameserveritem_list);
        if (isDarkTheme(this)) {
            getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.black));
        } else {getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.white));}
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.mipmap.ic_launcher);
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
            mTwoPane = true;
        } else {
            mTwoPane = false;
            sSelected = NO_ITEM;
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public void onResume() {
        super.onResume();
        orientationSetup(this);
        checkThemeChange();
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        if (isTwoPaneMode() && sSelected != NO_ITEM) {
            Log.d(TAG, "Restoring detail view.");
            restoreDetailPane(sSelected);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
        Intent settingsIntent = new Intent(this, AddGameServerActivity.class);
        this.startActivityForResult(settingsIntent, ADD_SERVER_INTENT_REQUEST);
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

            }
        };
        Log.d(TAG, "Created servers example list");
        SharedPreferences serversPrefs = getSharedPreferences(SERVERS_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = serversPrefs.edit();
        for (Map.Entry<String, String> entry : SERVERS_EXAMPLE.entrySet()) {
            GameServerItem gameServer = new GameServerItem();
            gameServer.mId = entry.getKey();
            gameServer.mName = entry.getValue();
            sServersList.add(gameServer);
            Log.d(TAG, "Added: " + entry.getKey() + "#" + entry.getValue());
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.apply();

        sServersAdapter.notifyDataSetChanged();
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
            Log.d(TAG, "Proper ResultIntent.");
            GameServerItem gameServer = new GameServerItem();
            gameServer.mId = resultIntent.getStringExtra(ADDED_SERVER_ADDRESS);
            gameServer.mName = resultIntent.getStringExtra(ADDED_SERVER_NAME);
            sServersList.add(gameServer);
            sServersAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM_POSITION, sSelected);
    }
}
