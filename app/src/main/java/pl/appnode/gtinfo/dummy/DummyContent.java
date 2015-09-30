package pl.appnode.gtinfo.dummy;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.appnode.gtinfo.AppContextHelper;


import static pl.appnode.gtinfo.Constants.SERVERS_PREFS_FILE;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    static {
        SharedPreferences gameServersPrefs = AppContextHelper.getContext()
                .getSharedPreferences(SERVERS_PREFS_FILE, 0);
        Map<String, ?> keys = gameServersPrefs.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
           addItem(new DummyItem(entry.getKey(), entry.getValue().toString()));
        }
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String id;
        public String content;

        public DummyItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
