package pl.appnode.gtinfo;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import static pl.appnode.gtinfo.Constants.CARD_STATE_DEFAULT;
import static pl.appnode.gtinfo.Constants.CARD_STATE_SELECTED;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_ADDRESS;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_INTENT_REQUEST;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_LIST_POSITION;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_NAME;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_RATING;
import static pl.appnode.gtinfo.Constants.FRAGMENT_ARG_ITEM_ID;
import static pl.appnode.gtinfo.Constants.NO_ITEM;
import static pl.appnode.gtinfo.Constants.RATING_0_STARS;
import static pl.appnode.gtinfo.Constants.RATING_1_STAR;
import static pl.appnode.gtinfo.Constants.RATING_2_STARS;
import static pl.appnode.gtinfo.Constants.RATING_3_STARS;
import static pl.appnode.gtinfo.GameServerItemListActivity.sServersList;
import static pl.appnode.gtinfo.PreferencesSetupHelper.isDarkTheme;

/**
 * Adapts items from data set into views grouped in list.
 */
public class GameServersAdapter extends RecyclerView.Adapter<GameServersAdapter.ServerViewHolder>{

    private static final String TAG = "GameServersAdapter";
    private final static int CARD_DEFAULT_DARK_TWO_PANE_BACKGROUND = ContextCompat
            .getColor(AppContextHelper.getContext(), R.color.dark_grey);
    private final static int CARD_DEFAULT_LIGHT_TWO_PANE_BACKGROUND = ContextCompat
            .getColor(AppContextHelper.getContext(), R.color.light_grey);
    private final static int CARD_SELECTED_DARK_TWO_PANE_BACKGROUND = ContextCompat
            .getColor(AppContextHelper.getContext(), R.color.black);
    private final static int CARD_SELECTED_LIGHT_TWO_PANE_BACKGROUND = ContextCompat
            .getColor(AppContextHelper.getContext(), R.color.white);
    private final static int CARD_DEFAULT_DARK_SINGLE_PANE_BACKGROUND = ContextCompat
            .getColor(AppContextHelper.getContext(), R.color.black);
    private final static int CARD_DEFAULT_LIGHT_SINGLE_PANE_BACKGROUND = ContextCompat
            .getColor(AppContextHelper.getContext(), R.color.white);
    private final Context mContext;
    private final static Drawable CARD_RATING_1_STAR_IMAGE = ContextCompat
            .getDrawable(AppContextHelper.getContext(), R.drawable.ic_star_border_grey_48px);
    private final static Drawable CARD_RATING_2_STARS_IMAGE = ContextCompat
            .getDrawable(AppContextHelper.getContext(), R.drawable.ic_star_half_grey_48px);
    private final static Drawable CARD_RATING_3_STARS_IMAGE = ContextCompat
            .getDrawable(AppContextHelper.getContext(), R.drawable.ic_star_full_grey_48px);

    public GameServersAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return sServersList.size();
    }

    @Override
    public void onBindViewHolder(final ServerViewHolder serverViewHolder, final int position) {
        final GameServerItem gameServer = sServersList.get(position);
        serverViewHolder.vPosition = position;
        serverViewHolder.vName.setText(gameServer.mName);
        serverViewHolder.vAddress.setText(gameServer.mId);
        ((CardView)serverViewHolder.itemView)
                    .setCardBackgroundColor(setCardColor(position));
        serverViewHolder.vImage.setImageDrawable(setCardImage(serverViewHolder, gameServer.mRating,
                gameServer.mId));
        if (gameServer.mRating.equals(RATING_0_STARS)) {
            serverViewHolder.vFirstLetter.setText(gameServer.mName.substring(0, 1));
        } else serverViewHolder.vFirstLetter.setText("");
    }

    // Returns proper background color, depending on user theme settings
    private int setCardColor(int position) {
        int state = CARD_STATE_DEFAULT;
        if (GameServerItemListActivity.getSelectedItem() == position) {state = CARD_STATE_SELECTED;}

        if (isDarkTheme(mContext)) {
            if (GameServerItemListActivity.isTwoPaneMode()) {
                switch (state) {
                    case CARD_STATE_DEFAULT:
                        return CARD_DEFAULT_DARK_TWO_PANE_BACKGROUND;
                    case CARD_STATE_SELECTED:
                        return CARD_SELECTED_DARK_TWO_PANE_BACKGROUND;
                }
            } else {
                return CARD_DEFAULT_DARK_SINGLE_PANE_BACKGROUND;
            }
        }

        if (!isDarkTheme(mContext)) {
            if (GameServerItemListActivity.isTwoPaneMode()) {
                switch (state) {
                    case CARD_STATE_DEFAULT:
                        return CARD_DEFAULT_LIGHT_TWO_PANE_BACKGROUND;
                    case CARD_STATE_SELECTED:
                        return CARD_SELECTED_LIGHT_TWO_PANE_BACKGROUND;
                }
            } else {
                return CARD_DEFAULT_LIGHT_SINGLE_PANE_BACKGROUND;
            }
        }
        return ContextCompat.getColor(mContext, R.color.white);
    }

    private Drawable setCardImage(ServerViewHolder serverViewHolder, String rating, String address) {
        int listRatingIconColor;
        int listDefaultIconColor = setColorFromServerAddress(address);
        View image = serverViewHolder.vImage;
        final GradientDrawable imageBackground = (GradientDrawable) image.getBackground();
        imageBackground.setColor(listDefaultIconColor);
        if (isDarkTheme(mContext)) {
            listRatingIconColor = argbColor(ContextCompat
                    .getColor(mContext, R.color.icon_list_orange_light));
        } else {
            listRatingIconColor = argbColor(ContextCompat
                .getColor(mContext, R.color.icon_list_orange_light));}
        switch (rating) {
            case RATING_1_STAR:
                CARD_RATING_1_STAR_IMAGE.setColorFilter(listRatingIconColor, PorterDuff.Mode.SRC_IN);
                return CARD_RATING_1_STAR_IMAGE;
            case RATING_2_STARS:
                CARD_RATING_2_STARS_IMAGE.setColorFilter(listRatingIconColor, PorterDuff.Mode.SRC_IN);
                return CARD_RATING_2_STARS_IMAGE;
            case RATING_3_STARS:
                CARD_RATING_3_STARS_IMAGE.setColorFilter(listRatingIconColor, PorterDuff.Mode.SRC_IN);
                return CARD_RATING_3_STARS_IMAGE;
            default:
                return null;
        }
    }

    private int argbColor(int colorResource) {
        int color = Color.argb(Color.alpha(colorResource),
                Color.red(colorResource),
                Color.green(colorResource),
                Color.blue(colorResource));
        return color;
    }

    private int setColorFromServerAddress(String address) {
        String[] parts = address.split("\\.");
        int r = Integer.parseInt(parts[0]);
        int g = Integer.parseInt(parts[1]);
        int b = Integer.parseInt(parts[2]);
        return Color.rgb(r, g, b);
    }

    @Override
    public ServerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        int cardLayout;
        // Sets up layout accordingly for device and orientation
        Configuration configuration = mContext.getResources().getConfiguration();
        if (GameServerItemListActivity.isPhone()
                && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE ) {
            cardLayout = R.layout.card_layout_landscape;
        } else cardLayout = R.layout.card_layout;
        final View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(cardLayout, viewGroup, false);
        // Creates view holder for items, handles clicks and long clicks on each view
        final GameServersAdapter.ServerViewHolder viewHolder = new ServerViewHolder
                (itemView, new GameServersAdapter.ServerViewHolder.IViewHolderOnClicks(){
            public void onCardClick(View caller, int position) {
                GameServerItem gameServer = sServersList.get(position);
                Log.d(TAG, "Clicked: " + gameServer.mName + " position: " + position);
                int oldSelected = GameServerItemListActivity.getSelectedItem();
                GameServerItemListActivity.setSelectedItem(position);
                GameServerItemListActivity.setScrollTo(position);
                if (oldSelected != NO_ITEM) {
                    notifyItemChanged(oldSelected);
                }
                notifyItemChanged(position);
                if (!GameServerItemListActivity.isTwoPaneMode()) {
                    Intent detailIntent = new Intent(mContext, GameServerItemDetailActivity.class);
                    detailIntent.putExtra(FRAGMENT_ARG_ITEM_ID,
                            position);
                    Log.d(TAG, "Address for detail activity: " + position);
                    mContext.startActivity(detailIntent);
                } else {
                    Bundle arguments = new Bundle();
                    arguments.putInt(FRAGMENT_ARG_ITEM_ID, position);
                    GameServerItemDetailFragment fragment = new GameServerItemDetailFragment();
                    fragment.setArguments(arguments);
                    FragmentActivity activity = (FragmentActivity) mContext;
                    FragmentManager manager = activity.getSupportFragmentManager();
                    manager.beginTransaction()
                            .add(R.id.gameserveritem_detail_container, fragment)
                            .commit();
                }
            }
            public void onCardLongClick(View caller, int position) {
                GameServerItem gameServer = sServersList.get(position);
                Intent settingsIntent = new Intent(mContext, AddGameServerActivity.class);
                settingsIntent.putExtra(EDIT_SERVER_ADDRESS, gameServer.mId);
                settingsIntent.putExtra(EDIT_SERVER_NAME, gameServer.mName);
                settingsIntent.putExtra(EDIT_SERVER_RATING, gameServer.mRating);
                settingsIntent.putExtra(EDIT_SERVER_LIST_POSITION, position);
                ((GameServerItemListActivity)mContext).startActivityForResult(settingsIntent, EDIT_SERVER_INTENT_REQUEST);
            }
        });
        return viewHolder;
    }

    static public class ServerViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        IViewHolderOnClicks mClickListener;
        IViewHolderOnClicks mLongClickListener;
        int vPosition;
        TextView vName;
        TextView vAddress;
        ImageView vImage;
        TextView vFirstLetter;

        public ServerViewHolder(View itemCardView, IViewHolderOnClicks listener) {
            super(itemCardView);
            mClickListener = listener;
            mLongClickListener = listener;
            vName = (TextView) itemCardView.findViewById(R.id.server_name);
            vAddress = (TextView) itemCardView.findViewById(R.id.server_address);
            vImage = (ImageView) itemCardView.findViewById(R.id.image_rating);
            vFirstLetter = (TextView) itemCardView.findViewById(R.id.server_first_letter);
            itemCardView.setOnClickListener(this);
            itemCardView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.server_name:
                    mClickListener.onCardClick(v, vPosition);
                    break;
                default:
                    mClickListener.onCardClick(v, vPosition);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            mLongClickListener.onCardLongClick(v, vPosition);
            return true;
        }

        public interface IViewHolderOnClicks {
            void onCardClick(View caller, int position);
            void onCardLongClick(View caller, int position);
        }
    }
}