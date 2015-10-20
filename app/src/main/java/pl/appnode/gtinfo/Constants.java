package pl.appnode.gtinfo;

import java.util.HashMap;
import java.util.Map;

/** Set of constants  */

public final class Constants {
    private Constants() {} /** Private constructor of final class to prevent instantiating. */

    /** Intent request code for add server activity  */
    public static final int ADD_SERVER_INTENT_REQUEST = 501;

    /** Extra keys for result intent in add server activity s*/
    public static final String ADDED_SERVER_ADDRESS = "AddedServerAddress";
    public static final String ADDED_SERVER_NAME = "AddedServerName";

    /** SharedPreferences file for storing serves IP and custom name */
    public static final String SERVERS_PREFS_FILE = "ServersPrefsFile";

    /** Key for saving value of selected server list item used in instance state save*/
    public static final String SELECTED_ITEM_POSITION = "mSelected";

    /** Value indicating no server list item selected or no position to scroll server list */
    public static final int NO_ITEM = -1;


    /** Servers list item default state, used to determine card colour */
    public static final int CARD_STATE_DEFAULT = 1;

    /** Servers list item selected state, used to determine card colour */
    public static final int CARD_STATE_SELECTED = 2;


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

    /** Scaling factor for two pane mode */
    public static final Double SCALING_FACTOR_TABLET = 1.8;

    /** Dictionary of all colors 'themes' for GT HTML component */
    public static final Map<String, String> GT_HTML_COLORS = new HashMap<String, String>() {
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
