package pl.appnode.gtinfo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static pl.appnode.gtinfo.Constants.GT_ORIGINAL_PLAYERS_LIST_HEIGHT;
import static pl.appnode.gtinfo.Constants.PLAYERS_LIST_HEIGHT_FACTOR_WITH_MAP_IMAGE_BIG;
import static pl.appnode.gtinfo.Constants.PLAYERS_LIST_HEIGHT_FACTOR_WITH_MAP_IMAGE_SMALL;
import static pl.appnode.gtinfo.Constants.PLAYERS_LIST_HEIGHT_FACTOR_WITH_TOP_PLAYERS;
import static pl.appnode.gtinfo.Constants.SCALING_FACTOR_PHONE;
import static pl.appnode.gtinfo.Constants.SCALING_FACTOR_PHONE_LANDSCAPE;
import static pl.appnode.gtinfo.Constants.SCALING_FACTOR_TABLET;
import static pl.appnode.gtinfo.Constants.GT_HTML_COLORS;
import static pl.appnode.gtinfo.Constants.GT_HTML_INFO_COMPONENT_BASE_URL;
import static pl.appnode.gtinfo.Constants.GT_HTML_INFO_COMPONENT_WIDTH;
import static pl.appnode.gtinfo.GameServerItemListFragment.sServersList;
import static pl.appnode.gtinfo.PreferencesSetupHelper.isDarkTheme;
import static pl.appnode.gtinfo.PreferencesSetupHelper.isShowMap;
import static pl.appnode.gtinfo.PreferencesSetupHelper.isShowTopPlayers;

/**
 * A fragment representing a single GameServerItem detail screen.
 * This fragment is either contained in a {@link GameServerItemListActivity}
 * in two-pane mode (on tablets) or a {@link GameServerItemDetailActivity}
 * on handsets.
 */
public class GameServerItemDetailFragment extends Fragment {

    public static final String TAG = "GameServerDetail";

    TextView mServerName = null;
    ProgressBar mProgressBar;

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * Content this fragment is presenting.
     */
    private GameServerItem mItem;

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
            mItem = sServersList.get(getArguments().getInt(ARG_ITEM_ID));
        } else {
            mItem.mId = "0";
            mItem.mName = "";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gameserveritem_detail, container, false);
        mServerName = (TextView) rootView.findViewById(R.id.detail_server_name);
        mProgressBar = (ProgressBar)rootView.findViewById(R.id.webview_progress_bar);
        LinearLayout detailBackground = (LinearLayout) rootView.findViewById(R.id.detail_background);

        if (!GameServerItemListActivity.isTwoPaneMode()) {
            mServerName.setText(mItem.mName);
            mServerName.setBackgroundColor(getResources().getColor(R.color.icon_orange));
        } else {
            mServerName.setVisibility(View.GONE);}
        String keyPrefix;
        if (isDarkTheme(getActivity())) {
            detailBackground.setBackgroundColor(getResources().getColor(R.color.black));
            keyPrefix = "dark-";
        } else {
            keyPrefix = "light-";
            detailBackground.setBackgroundColor(getResources().getColor(R.color.white));
        }

        if (mItem != null && !mItem.mId.equals("0")) {
            final WebView gameServerWebView = (WebView) rootView.findViewById(R.id.gameServerInfoWebview);
            gameServerWebView.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView view, String url) {
                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    mProgressBar.setVisibility(View.GONE);
                    gameServerWebView.loadUrl("about:blank");
                    Toast.makeText(getActivity(), description, Toast.LENGTH_LONG).show();
                    super.onReceivedError(view, errorCode, description, failingUrl);
                }
            });
            Double factor = SCALING_FACTOR_PHONE;
            if (!GameServerItemListActivity.isPhone() && GameServerItemListActivity.isTwoPaneMode()) {
                factor = SCALING_FACTOR_TABLET;
            } else if (GameServerItemListActivity.isTwoPaneMode() && GameServerItemListActivity.isPhone()) {
                factor = SCALING_FACTOR_PHONE_LANDSCAPE;
            }
            int currentPlayersListHeight = GT_ORIGINAL_PLAYERS_LIST_HEIGHT;
            if (GameServerItemListActivity.isTwoPaneMode()) {
                currentPlayersListHeight = getScaledCurrentPlayersListHeight();
            }
            gameServerWebView.setBackgroundColor(Color.TRANSPARENT);
            gameServerWebView.setInitialScale(getWebViewScale(factor));
            String showMap = "0";
            if (isShowMap(getActivity())) {
                showMap = "1";
            }
            String topPlayers = "0";
            if (isShowTopPlayers(getActivity())) {
                topPlayers = "1";
            }
            String url = GT_HTML_INFO_COMPONENT_BASE_URL
                    + mItem.mId
                    + "&bgColor=" + GT_HTML_COLORS.get(keyPrefix + "bgColor")
                    + "&fontColor=" + GT_HTML_COLORS.get(keyPrefix + "fontColor")
                    + "&titleBgColor=" + GT_HTML_COLORS.get(keyPrefix + "titleBgColor")
                    + "&titleColor=" + GT_HTML_COLORS.get(keyPrefix + "titleColor")
                    + "&borderColor=" + GT_HTML_COLORS.get(keyPrefix + "borderColor")
                    + "&linkColor=" + GT_HTML_COLORS.get(keyPrefix + "linkColor")
                    + "&borderLinkColor=" + GT_HTML_COLORS.get(keyPrefix + "borderLinkColor")
                    + "&showMap=" + showMap
                    + "&currentPlayersHeight=" + currentPlayersListHeight
                    + "&showCurrPlayers=1"
                    + "&showTopPlayers=" + topPlayers
                    + "&showBlogs=0"
                    + "&width=" + GT_HTML_INFO_COMPONENT_WIDTH;
            gameServerWebView.loadUrl(url);
        }
        return rootView;
    }

    private DisplayMetrics getDisplay() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics;
    }

    private int getScaledCurrentPlayersListHeight() {
        int height = getDisplay().heightPixels;
        if (isShowMap(getActivity())) {
            if (height <= 1300) {
                height = height - PLAYERS_LIST_HEIGHT_FACTOR_WITH_MAP_IMAGE_SMALL;
            } else height = height - PLAYERS_LIST_HEIGHT_FACTOR_WITH_MAP_IMAGE_BIG;
        }
        if (isShowTopPlayers(getActivity())) {
            height = height - PLAYERS_LIST_HEIGHT_FACTOR_WITH_TOP_PLAYERS;
        }
        int listFactor;
        if (height > 1300) {
            listFactor = 5;
        } else {
            if (height > 700) {
                listFactor = 3;
            } else listFactor = 2;
        }
        return (height / listFactor);
    }

    private int getWebViewScale(Double factor) {
        Double width = getDisplay().widthPixels / factor;
        int height = getDisplay().heightPixels;
        Log.d(TAG, "Metrics: width=" + width + " height=" + height);
        Double scale = width / GT_HTML_INFO_COMPONENT_WIDTH;
        scale = scale * 100d;
        return scale.intValue();
    }
}
