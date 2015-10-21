package pl.appnode.gtinfo;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import static pl.appnode.gtinfo.PreferencesSetupHelper.orientationSetup;
import static pl.appnode.gtinfo.PreferencesSetupHelper.themeSetup;

public class PreferencesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {


    public static final String KEY_PREF_THEME = "settings_checkbox_theme";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        themeSetup(this);
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GTinfoPreferenceFragment()).commit();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(KEY_PREF_THEME)) {

        }
    }

    public static class GTinfoPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.activity_preferences);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        orientationSetup(this);
    }
}
