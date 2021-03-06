package pl.appnode.gtinfo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static pl.appnode.gtinfo.Constants.KEY_SETTINGS_GT_SHOW_MAP;
import static pl.appnode.gtinfo.Constants.KEY_SETTINGS_GT_SHOW_TOP_PLAYERS;
import static pl.appnode.gtinfo.Constants.KEY_SETTINGS_THEME;

/**
 * Class with functions for reading and using application settings
 * from app's default shared preferences.
 */
class PreferencesSetupHelper {

    /**
     * Sets up proper (dark or light) system theme.
     *
     * @param context the context of calling activity
     */
    public static void themeSetup(Context context) {
        if (isDarkTheme(context)) {
           context.setTheme(R.style.AppThemeDark);
        } else {
            context.setTheme(R.style.AppTheme);
        }
    }

    /**
     * Controls ability to change app display orientation accordingly to device state.
     *
     * @param activity the activity which is to be allowed to be displayed in portrait/landscape or
     *                 limited to only portrait orientation
     */
    public static void orientationSetup(Activity activity) {
        if (isRotationOn(activity)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * Returns state of dark theme setting in app preferences, used to set proper system theme.
     *
     * @return true if dark theme is set in preferences
     */
    public static boolean isDarkTheme(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(KEY_SETTINGS_THEME, false);
    }

    /**
     * Returns state of preference setting allowing app display orientation change.
     *
     * @param context the context of calling activity
     *
     * @return true if display orientation change is allowed in preferences
     */
    public static boolean isRotationOn(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean("settings_checkbox_orientation", false);
    }

    /**
     * Checks if app is started first time after installation.
     *
     * @return true if application is started with no previously existing preferences
     */
    public static boolean isFirstRun(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean firstRun = settings.getBoolean("settings_first_run", true);
        settings.edit().putBoolean("settings_first_run", false).apply();
        return firstRun;
    }

    /**
     * Returns state of user preference setting controlling showing image
     * of current map in HTML game info component.
     *
     * @return true if in game server info component should be displayed image of current game map
     */
    public static boolean isShowMap(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(KEY_SETTINGS_GT_SHOW_MAP, false);
    }

    /**
     * Returns state of user preference setting controlling showing list of server top players
     * in HTML game info component.
     *
     * @return true if in game server info component should be displayed list
     *              of this server top players
     */
    public static boolean isShowTopPlayers(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(KEY_SETTINGS_GT_SHOW_TOP_PLAYERS, false);
    }
}
