package pl.appnode.gtinfo;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.TextView;

import static pl.appnode.gtinfo.Constants.CARD_STATE_DEFAULT;
import static pl.appnode.gtinfo.Constants.CARD_STATE_SELECTED;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_ADDRESS;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_INTENT_REQUEST;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_LIST_POSITION;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_NAME;
import static pl.appnode.gtinfo.Constants.NO_ITEM;
import static pl.appnode.gtinfo.GameServerItemListFragment.sServersList;
import static pl.appnode.gtinfo.PreferencesSetupHelper.isDarkTheme;

public class GameServersAdapter extends RecyclerView.Adapter<GameServersAdapter.ServerViewHolder>{

    private static final String TAG = "GameServersAdapter";
    private Context mContext;


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
    }

    private int setCardColor(int position) {

        int state = CARD_STATE_DEFAULT;
        if (GameServerItemListActivity.getSelectedItem() == position) {state = CARD_STATE_SELECTED;}

        if (state == CARD_STATE_DEFAULT && GameServerItemListActivity.sFilteredServersList.contains(position)) {
            return ContextCompat.getColor(mContext, R.color.filtered_list);
        }

        if (isDarkTheme(mContext)) {
            if (GameServerItemListActivity.isTwoPaneMode()) {
                switch (state) {
                    case CARD_STATE_DEFAULT:
                        return ContextCompat.getColor(mContext, R.color.dark_gray);
                    case CARD_STATE_SELECTED:
                        return ContextCompat.getColor(mContext, R.color.black);
                }
            } else {
                return ContextCompat.getColor(mContext, R.color.black);
            }
        }

        if (!isDarkTheme(mContext)) {
            if (GameServerItemListActivity.isTwoPaneMode()) {
                switch (state) {
                    case CARD_STATE_DEFAULT:
                        return ContextCompat.getColor(mContext, R.color.light_gray);
                    case CARD_STATE_SELECTED:
                        return ContextCompat.getColor(mContext, R.color.white);
                }
            } else {
                return ContextCompat.getColor(mContext, R.color.white);
            }
        }
        return ContextCompat.getColor(mContext, R.color.white);
    }

    @Override
    public ServerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        int cardLayout;
        Configuration configuration = mContext.getResources().getConfiguration();
        if (GameServerItemListActivity.isPhone()
                && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE ) {
            cardLayout = R.layout.card_layout_landscape;
        } else cardLayout = R.layout.card_layout;
        final View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(cardLayout, viewGroup, false);
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
                    detailIntent.putExtra(GameServerItemDetailFragment.ARG_ITEM_ID,
                            position);
                    Log.d(TAG, "Address for detail activity: " + position);
                    mContext.startActivity(detailIntent);
                } else {
                    Bundle arguments = new Bundle();
                    arguments.putInt(GameServerItemDetailFragment.ARG_ITEM_ID, position);
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
                settingsIntent.putExtra(EDIT_SERVER_LIST_POSITION, position);
                ((GameServerItemListActivity)mContext).startActivityForResult(settingsIntent, EDIT_SERVER_INTENT_REQUEST);
            }
        });

        CardView card = (CardView) itemView;
        if (isDarkTheme(mContext)) {
            card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.dark_gray));
        } else card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.light_gray));
        return viewHolder;
    }

    static public class ServerViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        protected IViewHolderOnClicks mClickListener;
        protected IViewHolderOnClicks mLongClickListener;
        protected int vPosition;
        protected TextView vName;
        protected TextView vAddress;

        public ServerViewHolder(View itemCardView, IViewHolderOnClicks listener) {
            super(itemCardView);
            mClickListener = listener;
            mLongClickListener = listener;
            vName = (TextView) itemCardView.findViewById(R.id.server_name);
            vAddress = (TextView) itemCardView.findViewById(R.id.server_address);
            itemCardView.setOnClickListener(this);
            itemCardView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.server_name:
                    mClickListener.onCardClick(v, vPosition);
                    Log.d(TAG, "Server name clicked.");
                    break;
                default:
                    mClickListener.onCardClick(v, vPosition);
                    Log.d(TAG, "Card clicked: " + vPosition);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            mLongClickListener.onCardLongClick(v, vPosition);
            Log.d(TAG, "Card longclicked: " + vPosition);
            return true;
        }

        public interface IViewHolderOnClicks {
            void onCardClick(View caller, int position);
            void onCardLongClick(View caller, int position);
        }
    }
}