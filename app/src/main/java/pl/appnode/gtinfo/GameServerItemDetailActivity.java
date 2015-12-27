package pl.appnode.gtinfo;

import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.transition.Fade;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import static pl.appnode.gtinfo.Constants.ADD_SERVER_INTENT_REQUEST;
import static pl.appnode.gtinfo.Constants.FRAGMENT_ARG_ITEM_ID;
import static pl.appnode.gtinfo.Constants.NO_ITEM;
import static pl.appnode.gtinfo.PreferencesSetupHelper.isDarkTheme;
import static pl.appnode.gtinfo.PreferencesSetupHelper.orientationSetup;
import static pl.appnode.gtinfo.PreferencesSetupHelper.themeSetup;

/**
 * An activity representing a single GameServerItem detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link GameServerItemListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing
 * {@link GameServerItemDetailFragment}.
 */
public class GameServerItemDetailActivity extends AppCompatActivity {

    private static boolean sThemeChangeFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeSetup(this);
        sThemeChangeFlag = isDarkTheme(this);
        setContentView(R.layout.activity_gameserveritem_detail);
        setupWindowAnimations();
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setIcon(R.mipmap.ic_launcher);
        }
        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            showDetailFragment(getIntent().getIntExtra(FRAGMENT_ARG_ITEM_ID, NO_ITEM));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        orientationSetup(this);
        checkThemeChange();
        showDetailFragment(GameServerItemListActivity.getSelectedItem());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (!GameServerItemListActivity.isTwoPaneMode()) {
            MenuItem searchView = menu.findItem(R.id.action_search);
            searchView.setVisible(false);
            MenuItem menuAddServer = menu.findItem(R.id.action_add_server);
            menuAddServer.setVisible(false);
            MenuItem menuClearList = menu.findItem(R.id.action_clear_list);
            menuClearList.setVisible(false);
            MenuItem menuAddServers = menu.findItem(R.id.action_populate);
            menuAddServers.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, GameServerItemListActivity.class));
            return true;
        }
        if (id == R.id.action_about) {
            AboutDialog.showDialog(GameServerItemDetailActivity.this);
        }
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, PreferencesActivity.class);
            this.startActivity(settingsIntent);
        }
        if (id == R.id.action_add_server) {
            Intent settingsIntent = new Intent(this, AddGameServerActivity.class);
            this.startActivityForResult(settingsIntent, ADD_SERVER_INTENT_REQUEST);
        }
        if (id == R.id.action_share_server) {
            shareServer();
        }
        return super.onOptionsItemSelected(item);
    }

    // Shows app chooser for sharing game server text data (server name and IP:port) in single pane mode
    private void shareServer() {
        int position = getIntent().getIntExtra(FRAGMENT_ARG_ITEM_ID, NO_ITEM);
        if (position != NO_ITEM) {
            GameServerItem gameServer = GameServerItemListActivity.sServersList.get(position);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, gameServer.mName + " " + gameServer.mId);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent,
                    getResources().getText(R.string.share_server_chooser_title)));
        }
    }

    private void setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Fade fade = new Fade();
            fade.setDuration(3000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {getWindow().setExitTransition(fade);}
        }
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

    /** Refreshes detail view fragment if any item on servers list was selected.
     *
     * @param fab clicked refresh floating action button
     */
    public void refreshWebView(View fab) {
        if (GameServerItemListActivity.getSelectedItem() != NO_ITEM) {
            showDetailFragment(GameServerItemListActivity.getSelectedItem());
        }
    }

    // Recreates detail view fragment for chosen list item
    private void showDetailFragment(int position) {
        Bundle arguments = new Bundle();
        arguments.putInt(FRAGMENT_ARG_ITEM_ID, position);
        GameServerItemDetailFragment fragment = new GameServerItemDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.gameserveritem_detail_container, fragment)
                .commitAllowingStateLoss();
                // commitAllowingStateLoss workaround for IllegalStateException
                // when getting back to activity (eg. from share app chooser)
    }
}
