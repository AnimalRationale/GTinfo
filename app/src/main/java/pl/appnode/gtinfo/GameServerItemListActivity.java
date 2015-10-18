package pl.appnode.gtinfo;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import static pl.appnode.gtinfo.Constants.ADD_SERVER_INTENT_REQUEST;
import static pl.appnode.gtinfo.Constants.ADDED_SERVER_ADDRESS;
import static pl.appnode.gtinfo.Constants.ADDED_SERVER_NAME;
import static pl.appnode.gtinfo.Constants.SELECTED_ITEM_POSITION;
import static pl.appnode.gtinfo.GameServerItemListFragment.sServersAdapter;
import static pl.appnode.gtinfo.GameServerItemListFragment.sServersList;
import static pl.appnode.gtinfo.PreferencesSetupHelper.isDarkTheme;
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
    private static int mSelected = -1;

    public static boolean isTwoPaneMode() {
        return mTwoPane;
    }

    public static int getSelectedItem() {return mSelected;}

    public static void setSelectedItem(int position) {
        mSelected = position;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSelected = savedInstanceState.getInt(SELECTED_ITEM_POSITION);
        }
        themeSetup(this);
        sThemeChangeFlag = isDarkTheme(this);
        setContentView(R.layout.activity_gameserveritem_list);
        if (isDarkTheme(this)) {
            getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.black));
        } else {getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.white));}
        ActionBar actionBar = getActionBar();
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
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public void onResume() {
        super.onResume();
        checkThemeChange();
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
        if (id == R.id.action_add_server) {
            Intent settingsIntent = new Intent(this, AddGameServerActivity.class);
            this.startActivityForResult(settingsIntent, ADD_SERVER_INTENT_REQUEST);
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
        outState.putInt(SELECTED_ITEM_POSITION, mSelected);
    }
}
