package pl.appnode.gtinfo;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import static pl.appnode.gtinfo.Constants.SERVERS_PREFS_FILE;
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
        serverViewHolder.vName.setText(gameServer.mName);
        serverViewHolder.vAddress.setText(gameServer.mId);
        serverViewHolder.vPosition = position;
        serverViewHolder.vPositionDisplay.setText(position + 1 + "");
    }

    @Override
    public ServerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);
        GameServersAdapter.ServerViewHolder viewHolder = new ServerViewHolder
                (itemView, new GameServersAdapter.ServerViewHolder.IViewHolderOnClicks() {
            public void onCardClick(View caller, int position) {
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

            public void onRemoveButtonClick(View caller, int position) {
                removeItem(position);
                Log.d(TAG, "Remove item: " + position);
            }

                });
        CardView card = (CardView) itemView;
        if (isDarkTheme(mContext)) {
            card.setCardBackgroundColor(Color.BLACK);
            Button removeButton = (Button)itemView.findViewById(R.id.button_remove_server);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                removeButton.setBackground(mContext.getResources()
                        .getDrawable(R.drawable.ic_remove_circle_outline_white_24dp, mContext.getTheme()));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                removeButton.setBackground(mContext.getResources()
                        .getDrawable(R.drawable.ic_remove_circle_outline_white_24dp));
            } else {
                removeButton.setBackgroundDrawable(mContext.getResources()
                        .getDrawable(R.drawable.ic_remove_circle_outline_white_24dp));
            }
        } else card.setCardBackgroundColor(Color.WHITE);
        return viewHolder;
    }

    private void removeItem(int position) {
        GameServerItem gameServer = sServersList.get(position);
        SharedPreferences serversPrefs = AppContextHelper.getContext()
                .getSharedPreferences(SERVERS_PREFS_FILE, mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = serversPrefs.edit();
        editor.remove(gameServer.mId);
        editor.apply();
        sServersList.remove(position);
        notifyDataSetChanged();
    }

    static public class ServerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected IViewHolderOnClicks mClickListener;
        protected int vPosition;
        protected TextView vPositionDisplay;
        protected TextView vName;
        protected TextView vAddress;
        protected Button vRemoveButton;

        public ServerViewHolder(View itemCardView, IViewHolderOnClicks listener) {
            super(itemCardView);
            mClickListener = listener;
            vName = (TextView) itemCardView.findViewById(R.id.server_name);
            vAddress = (TextView) itemCardView.findViewById(R.id.server_address);
            vPositionDisplay = (TextView) itemCardView.findViewById(R.id.item_position);
            vRemoveButton = (Button) itemCardView.findViewById(R.id.button_remove_server);
            vName.setOnClickListener(this);
            vRemoveButton.setOnClickListener(this);
            itemCardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_remove_server:
                    mClickListener.onRemoveButtonClick(v, vPosition);
                    break;
                case R.id.server_name:
                    mClickListener.onCardClick(v, vPosition);
                    Log.d(TAG, "Server name clicked.");
                    break;
                default:
                    mClickListener.onCardClick(v, vPosition);
            }
        }

        public interface IViewHolderOnClicks {
            void onCardClick(View caller, int position);
            void onRemoveButtonClick(View caller, int position);
        }
    }
}