package pl.appnode.gtinfo;

import java.util.HashMap;
import java.util.Map;

/** Set of constants  */

public final class Constants {
    private Constants() {} /** Private constructor of final class to prevent instantiating. */

    public static final String GT_HTML_INFO_COMPONENT_BASE_URL = "http://cache.www.gametracker.com/components/html0/?host=";


    /** Dictionary of all colors 'themes' for GT HTML component */
    public static final Map<String, String> GT_HTML_COLORS = new HashMap<String, String>() {
        {
            put("original-bgColor", "373E28");
            put("original-fontColor", "D2E1B5");
            put("original-titleBgColor", "2E3225");
            put("original-titleColor", "FFFFFF");
            put("original-borderColor", "3E4433");
            put("original-linkColor", "889C63");
            put("original-borderLinkColor", "828E6B");
        }
    };


    public static final int GT_HTML_INFO_COMPONENT_WIDTH = 240;
}
