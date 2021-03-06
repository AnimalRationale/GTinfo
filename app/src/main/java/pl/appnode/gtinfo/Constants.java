package pl.appnode.gtinfo;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;

import java.util.Map;

/**
 *  Set of constants.
 */

final class Constants {
    private Constants() {} /** Private constructor of final class to prevent instantiating. */

    /** Intent request code for add server activity  */
    public static final int ADD_SERVER_INTENT_REQUEST = 501;

    /** Intent request code for edit server activity  */
    public static final int EDIT_SERVER_INTENT_REQUEST = 502;

    /** Extra keys for result intent in add server activity */
    public static final String ADDED_SERVER_ADDRESS = "AddedServerAddress";
    public static final String ADDED_SERVER_NAME = "AddedServerName";
    public static final String ADDED_SERVER_RATING = "AddedServerRating";

    /** Extra keys for edit server activity intent */
    public static final String EDIT_SERVER_ADDRESS = "ServerAddress";
    public static final String EDIT_SERVER_NAME = "ServerName";
    public static final String EDIT_SERVER_RATING = "ServerRating";
    public static final String EDIT_SERVER_LIST_POSITION = "ServerPosition";

    /** Server ratings */
    public static final String RATING_0_STARS = "00";
    public static final String RATING_1_STAR = "01";
    public static final String RATING_2_STARS = "02";
    public static final String RATING_3_STARS = "03";

    /** IDs for EditText widgets in add/edit server dialog activity, used to identify copy action **/
    public static final int EDIT_SERVER_ADDRESS_EDITTEXT_ID = 0;
    public static final int EDIT_SERVER_NAME_EDITTEXT_ID = 1;

    /** Drawables representing server rating status */
    public final static Drawable CARD_RATING_1_STAR_IMAGE = ContextCompat
            .getDrawable(AppContextHelper.getContext(), R.drawable.ic_star_border_grey_48px);
    public final static Drawable CARD_RATING_2_STARS_IMAGE = ContextCompat
            .getDrawable(AppContextHelper.getContext(), R.drawable.ic_star_half_grey_48px);
    public final static Drawable CARD_RATING_3_STARS_IMAGE = ContextCompat
            .getDrawable(AppContextHelper.getContext(), R.drawable.ic_star_full_grey_48px);

    /** SharedPreferences file for storing serves IP and custom name */
    public static final String SERVERS_PREFS_FILE = "ServersPrefsFile";

    /** Key for saving theme settings in app preferences */
    public static final String KEY_SETTINGS_THEME = "settings_checkbox_theme";

    /** Key for saving GT show map options in app preferences */
    public static final String KEY_SETTINGS_GT_SHOW_MAP = "settings_checkbox_show_map";

    /** Key for saving GT show server top players options in app preferences */
    public static final String KEY_SETTINGS_GT_SHOW_TOP_PLAYERS = "settings_checkbox_show_top_players";

    /** Argument for detail view fragment representing the item ID that this fragment displays. */
    public static final String FRAGMENT_ARG_ITEM_ID = "item_id";

    /** Argument for list delete confirmation dialog with number of servers on this list. */
    public static final String SERVERS_ON_LIST = "serversOnList";

    /** Key for saving value of selected server list item used in instance state save*/
    public static final String SELECTED_ITEM_POSITION = "mSelected";

    /** Value indicating no server list item selected or no position to scroll server list */
    public static final int NO_ITEM = -1;

    /** Values for creating empty game server item for blank detail view (when no item is selected in two pane mode) */
    public static final String BLANK_ITEM_ID = "";
    public static final String BLANK_ITEM_NAME = "";

    /** Servers list item default state, used to determine card colour */
    public static final int CARD_STATE_DEFAULT = 1;

    /** Servers list item selected state, used to determine card colour */
    public static final int CARD_STATE_SELECTED = 2;

    /** Swipe to dismiss confirmation and undo snackbar display time in milliseconds */
    public static final int UNDO_TIME = 5000;

    /** Swipe to dismiss confirmation and undo snackbar display time in milliseconds */
    public static final int HINT_TIME = 5000;

    /** Regexp pattern for server IP:port validation
     * (allowing addresses starting with 0 and port value in range 0..99999) */
    public static final String IP_ADDRESS_PORT_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\:"
            + "(\\d{1,5})$";

    /** Base URL for GT HTML component with game server information */
    public static final String GT_HTML_INFO_COMPONENT_BASE_URL
            = "http://cache.www.gametracker.com/components/html0/?host=";

    /** Scaling factor for phones */
    public static final Double SCALING_FACTOR_PHONE = 1.0;

    /** Scaling factor for phones in landscape */
    public static final Double SCALING_FACTOR_PHONE_LANDSCAPE = 2.0;

    /** Scaling factor for two pane mode */
    public static final Double SCALING_FACTOR_TABLET = 1.8;

    /** GT HTML component original height of players list */
    public static final int GT_ORIGINAL_PLAYERS_LIST_HEIGHT = 200;

    /** Factor for scaling players list height when map image is shown on smaller devices */
    public static final int PLAYERS_LIST_HEIGHT_FACTOR_WITH_MAP_IMAGE_SMALL = 300;

    /** Factor for scaling players list height when map image is shown on bigger devices */
    public static final int PLAYERS_LIST_HEIGHT_FACTOR_WITH_MAP_IMAGE_BIG = 500;

    /** Factor for scaling players list height when top players list is shown */
    public static final int PLAYERS_LIST_HEIGHT_FACTOR_WITH_TOP_PLAYERS = 400;

    /** Dictionary of all colors 'themes' for GT HTML component */
    public static final Map<String, String> GT_HTML_COLORS = new ArrayMap<String, String>() {
        {
            // original GT colors
            put("original-bgColor", "373E28");
            put("original-fontColor", "D2E1B5");
            put("original-titleBgColor", "2E3225");
            put("original-titleColor", "FFFFFF");
            put("original-borderColor", "3E4433");
            put("original-linkColor", "889C63");
            put("original-borderLinkColor", "828E6B");

            // dark theme colors
            put("dark-bgColor", "000000");
            put("dark-fontColor", "FFFFEE");
            put("dark-titleBgColor", "000000");
            put("dark-titleColor", "FF6600");
            put("dark-borderColor", "000000");
            put("dark-linkColor", "889C63");
            put("dark-borderLinkColor", "CC0052");

            // light theme colors
            put("light-bgColor", "FFFFFF");
            put("light-fontColor", "0F0F00");
            put("light-titleBgColor", "FFFFFF");
            put("light-titleColor", "FF6600");
            put("light-borderColor", "FFFFFF");
            put("light-linkColor", "3366FF");
            put("light-borderLinkColor", "FF0066");
        }
    };

    /** Width of requested GT HTML component */
    public static final int GT_HTML_INFO_COMPONENT_WIDTH = 240;
}
