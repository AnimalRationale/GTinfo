package pl.appnode.gtinfo;

import java.util.HashMap;
import java.util.Map;

/** Set of constants  */

public final class Constants {
    private Constants() {} /** Private constructor of final class to prevent instantiating. */

    public static final String SERVERS_PREFS_FILE = "ServersPrefsFile";

    public static final String GT_HTML_INFO_COMPONENT_BASE_URL = "http://cache.www.gametracker.com/components/html0/?host=";


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

    public static final int GT_HTML_INFO_COMPONENT_WIDTH = 240;
}
