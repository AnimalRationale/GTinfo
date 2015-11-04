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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static pl.appnode.gtinfo.Constants.CARD_STATE_DEFAULT;
import static pl.appnode.gtinfo.Constants.CARD_STATE_DELETED;
import static pl.appnode.gtinfo.Constants.CARD_STATE_SELECTED;
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
        if (gameServer.mAlive) {
            serverViewHolder.vCardDismissUndo.setVisibility(View.GONE);
            serverViewHolder.vCardNormal.setVisibility(View.VISIBLE);
            serverViewHolder.vName.setText(gameServer.mName);
            serverViewHolder.vAddress.setText(gameServer.mId);
        } else {
            serverViewHolder.vCardNormal.setVisibility(View.GONE);
            serverViewHolder.vCardDismissUndo.setGravity(View.VISIBLE);
            serverViewHolder.vDeletedServer.setText(gameServer.mName);
        }
        serverViewHolder.vPosition = position;
        ((CardView)serverViewHolder.itemView)
                    .setCardBackgroundColor(setCardColor(position, gameServer.mAlive));
    }

    private int setCardColor(int position, boolean alive) {

        int state = CARD_STATE_DEFAULT;
        if (GameServerItemListActivity.getSelectedItem() == position) {state = CARD_STATE_SELECTED;}
        if (!alive) {state = CARD_STATE_DELETED;}

        if (isDarkTheme(mContext)) {
            if (GameServerItemListActivity.isTwoPaneMode()) {
                switch (state) {
                    case CARD_STATE_DEFAULT:
                        return ContextCompat.getColor(mContext, R.color.dark_gray);
                    case CARD_STATE_SELECTED:
                        return ContextCompat.getColor(mContext, R.color.black);
                    case CARD_STATE_DELETED:
                        return ContextCompat.getColor(mContext, R.color.dark_green);
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
                    case CARD_STATE_DELETED:
                        return ContextCompat.getColor(mContext, R.color.light_green);
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
        GameServersAdapter.ServerViewHolder viewHolder = new ServerViewHolder
                (itemView, new GameServersAdapter.ServerViewHolder.IViewHolderOnClicks() {
            public void onCardClick(View caller, int position) {
                GameServerItem gameServer = sServersList.get(position);
                Log.d(TAG, "Clicked: " + gameServer.mName + " position: " + position);
                if (!gameServer.mAlive) {
                    gameServer.mAlive = true;
                    Log.d(TAG, "UNDO: " + gameServer.mName + " position: " + position);
                    notifyItemChanged(position);
                    notifyDataSetChanged();
                } else {
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
            }
        });
        CardView card = (CardView) itemView;
        if (isDarkTheme(mContext)) {
            card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.dark_gray));
        } else card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.light_gray));
        return viewHolder;
    }

    static public class ServerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected IViewHolderOnClicks mClickListener;
        protected int vPosition;
        protected TextView vName;
        protected TextView vAddress;
        protected TextView vDeletedServer;
        protected ProgressBar vDismissTimer;
        protected RelativeLayout vCardNormal;
        protected RelativeLayout vCardDismissUndo;

        public ServerViewHolder(View itemCardView, IViewHolderOnClicks listener) {
            super(itemCardView);
            mClickListener = listener;
            vName = (TextView) itemCardView.findViewById(R.id.server_name);
            vAddress = (TextView) itemCardView.findViewById(R.id.server_address);
            vDeletedServer = (TextView) itemCardView.findViewById(R.id.deleted_server);
            vCardNormal = (RelativeLayout) itemCardView.findViewById(R.id.card_view_normal);
            vCardDismissUndo = (RelativeLayout) itemCardView.findViewById(R.id.card_view_undo);
            vDismissTimer = (ProgressBar) itemCardView.findViewById(R.id.undo_progress_bar);
            itemCardView.setOnClickListener(this);
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

        public interface IViewHolderOnClicks {
            void onCardClick(View caller, int position);
        }
    }
}