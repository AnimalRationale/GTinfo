package pl.appnode.gtinfo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
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

import static pl.appnode.gtinfo.Constants.BLANK_ITEM_ID;
import static pl.appnode.gtinfo.Constants.BLANK_ITEM_NAME;
import static pl.appnode.gtinfo.Constants.FRAGMENT_ARG_ITEM_ID;
import static pl.appnode.gtinfo.Constants.GT_ORIGINAL_PLAYERS_LIST_HEIGHT;
import static pl.appnode.gtinfo.Constants.NO_ITEM;
import static pl.appnode.gtinfo.Constants.PLAYERS_LIST_HEIGHT_FACTOR_WITH_MAP_IMAGE_BIG;
import static pl.appnode.gtinfo.Constants.PLAYERS_LIST_HEIGHT_FACTOR_WITH_MAP_IMAGE_SMALL;
import static pl.appnode.gtinfo.Constants.PLAYERS_LIST_HEIGHT_FACTOR_WITH_TOP_PLAYERS;
import static pl.appnode.gtinfo.Constants.SCALING_FACTOR_PHONE;
import static pl.appnode.gtinfo.Constants.SCALING_FACTOR_PHONE_LANDSCAPE;
import static pl.appnode.gtinfo.Constants.SCALING_FACTOR_TABLET;
import static pl.appnode.gtinfo.Constants.GT_HTML_COLORS;
import static pl.appnode.gtinfo.Constants.GT_HTML_INFO_COMPONENT_BASE_URL;
import static pl.appnode.gtinfo.Constants.GT_HTML_INFO_COMPONENT_WIDTH;
import static pl.appnode.gtinfo.GameServerItemListActivity.sServersList;
import static pl.appnode.gtinfo.PreferencesSetupHelper.isDarkTheme;
import static pl.appnode.gtinfo.PreferencesSetupHelper.isShowMap;
import static pl.appnode.gtinfo.PreferencesSetupHelper.isShowTopPlayers;

/**
 * A fragment representing a single GameServerItem detail screen.
 * This fragment is either contained in a {@link GameServerItemListActivity}
 * in two-pane mode (devices in landscape orientation) or a {@link GameServerItemDetailActivity}
 * on handsets (in portrait orientation).
 */
public class GameServerItemDetailFragment extends Fragment {

    public static final String LOGTAG = "GameServerDetail";

    private TextView mErrorInfoText;
    private ProgressBar mProgressBar;
    private String mKeyPrefix;
    private WebView mGameServerWebView;
    private FloatingActionButton mRefreshButton;
    private boolean mWebError = false;

    /**
     * Content this fragment is displaying.
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
        if (getArguments().containsKey(FRAGMENT_ARG_ITEM_ID)) {
            int position = getArguments().getInt(FRAGMENT_ARG_ITEM_ID);
            // If there is any chosen item, and if item list is not empty,
            // and item index is in current list size then get item data to display
            if (position > NO_ITEM && !sServersList.isEmpty() && position < sServersList.size()) {
                mItem = sServersList.get(getArguments().getInt(FRAGMENT_ARG_ITEM_ID));
            } else {
                // If no item is selected or list of items is empty display blank fragment
                mItem = new GameServerItem();
                mItem.mId = BLANK_ITEM_ID;
                mItem.mName = BLANK_ITEM_NAME;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Prepares structure for detail fragment view
        View rootView = inflater.inflate(R.layout.fragment_gameserveritem_detail, container, false);
        TextView serverName = (TextView) rootView.findViewById(R.id.detail_server_name);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.webview_progress_bar);
        mRefreshButton = (FloatingActionButton) rootView.findViewById(R.id.fab_refresh_webview);
        mRefreshButton.setVisibility(View.GONE);
        mErrorInfoText = (TextView) rootView.findViewById(R.id.detail_error_information);
        mErrorInfoText.setVisibility(View.GONE);
        LinearLayout detailBackground = (LinearLayout) rootView.findViewById(R.id.detail_background);
        // Prepares data for detail view, depending on device configuration and user settings
        if (!GameServerItemListActivity.isTwoPaneMode()) {
            serverName.setText(mItem.mName);
            serverName.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.icon_orange));
        } else {
            serverName.setVisibility(View.GONE);
        }
        if (isDarkTheme(getActivity())) {
            detailBackground.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
            mKeyPrefix = "dark-";
        } else {
            mKeyPrefix = "light-";
            detailBackground.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
        }
        mGameServerWebView = (WebView) rootView.findViewById(R.id.gameServerInfoWebview);
        showServerInfo();
        return rootView;
    }

    /**
     * Shows webview with item details, styled accordingly to user defined settings,
     * handles connection errors.
     */
    private boolean showServerInfo() {
        // Checks network connection
        if (!isConnection()) {
            mProgressBar.setVisibility(View.GONE);
            mErrorInfoText.setText(R.string.error_network_access_info);
            mErrorInfoText.setVisibility(View.VISIBLE);
            return false;
        }
        // If no details available to display (eg. landscape mode without selected item) hides view elements
        if (mItem.mId.equals(BLANK_ITEM_ID)) {
            mProgressBar.setVisibility(View.GONE);
            mGameServerWebView.setVisibility(View.GONE);
            return false;
        }
        if (mItem != null) {
            // Shows webview, redirects links to system browser, handles http problems
            mGameServerWebView.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView view, String url) {
                    mProgressBar.setVisibility(View.GONE);
                    mRefreshButton.setVisibility(View.VISIBLE);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }

                // TODO: more robust error handling with new API 23+ method
                // onReceivedError is deprecated in API 23; but new method will be called
                // on every resource (web page element), implementation of new method require more attention
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    mProgressBar.setVisibility(View.GONE);
                    mErrorInfoText.setVisibility(View.VISIBLE);
                    mWebError = true;
                    String errorToastInfo = getActivity().getResources().getString(R.string.error_webview_default_info);
                    if (description != null) {errorToastInfo = description;}
                    mGameServerWebView.loadUrl("about:blank");
                    Toast.makeText(AppContextHelper.getContext(), errorToastInfo, Toast.LENGTH_LONG).show();
                    super.onReceivedError(view, errorCode, description, failingUrl);
                }
            });
            // Scales webview content display to current screen size
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
            mGameServerWebView.setBackgroundColor(Color.TRANSPARENT);
            mGameServerWebView.setInitialScale(getWebViewScale(factor));
            // Prepares request for HTML component, depending on user defined settings
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
                    + "&bgColor=" + GT_HTML_COLORS.get(mKeyPrefix + "bgColor")
                    + "&fontColor=" + GT_HTML_COLORS.get(mKeyPrefix + "fontColor")
                    + "&titleBgColor=" + GT_HTML_COLORS.get(mKeyPrefix + "titleBgColor")
                    + "&titleColor=" + GT_HTML_COLORS.get(mKeyPrefix + "titleColor")
                    + "&borderColor=" + GT_HTML_COLORS.get(mKeyPrefix + "borderColor")
                    + "&linkColor=" + GT_HTML_COLORS.get(mKeyPrefix + "linkColor")
                    + "&borderLinkColor=" + GT_HTML_COLORS.get(mKeyPrefix + "borderLinkColor")
                    + "&showMap=" + showMap
                    + "&currentPlayersHeight=" + currentPlayersListHeight
                    + "&showCurrPlayers=1"
                    + "&showTopPlayers=" + topPlayers
                    + "&showBlogs=0"
                    + "&width=" + GT_HTML_INFO_COMPONENT_WIDTH;
            mGameServerWebView.loadUrl(url);
            return true;
        } else {return false;}
    }

    /**
     * Returns current screen metrics used for proper webview content scaling.
     *
     * @return object of DisplayMetrics class with display height and width in pixels
     */
    private DisplayMetrics getDisplay() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics;
    }

    /**
     * Returns scaled height for main list of HTML component.
     *
     * @return height for list of players in HTML game server component
     */
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
        // Factor for scaling list accordingly to display resolution
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

    /**
     * Calculates and returns scale for whole webview, based on current display metrics.
     *
     * @return scale for proper webview content display on given viewport
     */
    private int getWebViewScale(Double factor) {
        Double width = getDisplay().widthPixels / factor;
        Double scale = width / GT_HTML_INFO_COMPONENT_WIDTH;
        scale = scale * 100d;
        return scale.intValue();
    }

    /**
     * Checks if device has available network connection.
     *
     * @return true if device has available network connection
     */
    private boolean isConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if ((networkInfo == null) || (!networkInfo.isConnected())) {
            Toast toast = Toast.makeText(getActivity(),
                    R.string.error_network_access, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        return true;
    }
}
