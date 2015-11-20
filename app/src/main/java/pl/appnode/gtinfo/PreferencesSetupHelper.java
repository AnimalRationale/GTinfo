package pl.appnode.gtinfo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Build;

import static pl.appnode.gtinfo.Constants.KEY_SETTINGS_GT_SHOW_MAP;
import static pl.appnode.gtinfo.Constants.KEY_SETTINGS_GT_SHOW_TOP_PLAYERS;
import static pl.appnode.gtinfo.Constants.KEY_SETTINGS_THEME;

/**
 * Class with functions for reading and using application settings
 */
public class PreferencesSetupHelper {

    /**
     * Sets up proper (dark or light) system theme
     */
    public static void themeSetup(Context context) {

        if (isDarkTheme(context)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                context.setTheme(android.R.style.Theme_Holo);
            } else {
                context.setTheme(android.R.style.Theme_Material);
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            context.setTheme(android.R.style.Theme_Holo_Light);
        } else {
            context.setTheme(android.R.style.Theme_Material_Light);
        }
    }

    /**
     * Controls ability to change app display orientation accordingly to device state
     */
    public static void orientationSetup(Activity activity) {
        if (isRotationOn(activity)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * Returns state of dark theme setting in app preferences, used to set proper system theme
     */
    public static boolean isDarkTheme(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(KEY_SETTINGS_THEME, false);
    }

    /**
     * Returns state of preference setting allowing app display orientation change
     */
    public static boolean isRotationOn(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean("settings_checkbox_orientation", false);
    }

    /**
     * Checks if app is started first time after installation
     */
    public static boolean isFirstRun(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean firstRun = settings.getBoolean("settings_first_run", true);
        settings.edit().putBoolean("settings_first_run", false).apply();
        return firstRun;
    }

    /**
     * Returns state of user preference setting controlling showing image
     * of current map in HTML game info component
     */
    public static boolean isShowMap(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(KEY_SETTINGS_GT_SHOW_MAP, false);
    }

    /**
     * Returns state of user preference setting controlling showing list of server top players
     * in HTML game info component
     */
    public static boolean isShowTopPlayers(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(KEY_SETTINGS_GT_SHOW_TOP_PLAYERS, false);
    }
}
