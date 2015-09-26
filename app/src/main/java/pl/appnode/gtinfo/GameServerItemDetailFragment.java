package pl.appnode.gtinfo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;

import pl.appnode.gtinfo.dummy.DummyContent;

import static pl.appnode.gtinfo.Constants.GT_HTML_COLORS;
import static pl.appnode.gtinfo.Constants.GT_HTML_INFO_COMPONENT_BASE_URL;
import static pl.appnode.gtinfo.Constants.GT_HTML_INFO_COMPONENT_WIDTH;

/**
 * A fragment representing a single GameServerItem detail screen.
 * This fragment is either contained in a {@link GameServerItemListActivity}
 * in two-pane mode (on tablets) or a {@link GameServerItemDetailActivity}
 * on handsets.
 */
public class GameServerItemDetailFragment extends Fragment {

    public static final String TAG = "GameServerDetail";

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GameServerItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gameserveritem_detail, container, false);

        String keyPrefix = "light-";

        if (mItem != null) {
            WebView gameServerWebView = (WebView) rootView.findViewById(R.id.gameServerInfoWebview);
            gameServerWebView.setInitialScale(getWebviewScale());
            String url = GT_HTML_INFO_COMPONENT_BASE_URL
                    + mItem.id
                    + "&bgColor=" + GT_HTML_COLORS.get(keyPrefix + "bgColor")
                    + "&fontColor=" + GT_HTML_COLORS.get(keyPrefix + "fontColor")
                    + "&titleBgColor=" + GT_HTML_COLORS.get(keyPrefix + "titleBgColor")
                    + "&titleColor=" + GT_HTML_COLORS.get(keyPrefix + "titleColor")
                    + "&borderColor=" + GT_HTML_COLORS.get(keyPrefix + "borderColor")
                    + "&linkColor=" + GT_HTML_COLORS.get(keyPrefix + "linkColor")
                    + "&borderLinkColor=" + GT_HTML_COLORS.get(keyPrefix + "borderLinkColor")
                    + "&showMap=0"
                    + "&currentPlayersHeight=200"
                    + "&showCurrPlayers=1"
                    + "&showTopPlayers=0"
                    + "&showBlogs=0"
                    + "&width=" + GT_HTML_INFO_COMPONENT_WIDTH;
            gameServerWebView.loadUrl(url);
        }
        return rootView;
    }

    private int getWebviewScale() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        Log.d(TAG, "Metrics: width=" + width + " height=" + height);
        Double scale = new Double(width)/new Double(GT_HTML_INFO_COMPONENT_WIDTH);
        scale = scale * 100d;
        return scale.intValue();
    }
}
