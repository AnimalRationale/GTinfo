package pl.appnode.gtinfo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        ServerViewHolder.vName.setText(gameServer.mName);
        ServerViewHolder.vAddress.setText(gameServer.mId);
        ServerViewHolder.vPosition = position;
    }

    @Override
    public ServerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);
        GameServersAdapter.ServerViewHolder viewHolder = new ServerViewHolder
                (itemView, new GameServersAdapter.ServerViewHolder.IViewHolderOnClicks() {
            public void onCardClick(View caller) {
                Intent detailIntent = new Intent(mContext, GameServerItemDetailActivity.class);
                detailIntent.putExtra(GameServerItemDetailFragment.ARG_ITEM_ID, ServerViewHolder.vPosition);
                mContext.startActivity(detailIntent);
            }
                });
        CardView card = (CardView) itemView;
        if (isDarkTheme(mContext)) {
            card.setCardBackgroundColor(Color.BLACK);
        } else card.setCardBackgroundColor(Color.WHITE);
        return viewHolder;
    }

    public static class ServerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected IViewHolderOnClicks mClickListener;
        protected static int vPosition;
        protected static TextView vName;
        protected static TextView vAddress;

        public ServerViewHolder(View v, IViewHolderOnClicks listener) {
            super(v);
            vName = (TextView) v.findViewById(R.id.server_name);
            vAddress = (TextView) v.findViewById(R.id.server_address);
            vName.setOnClickListener(this);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof TextView) {
                mClickListener.onCardClick(v);
            } else {
                mClickListener.onCardClick(v);
            }
        }

        public interface IViewHolderOnClicks {
            void onCardClick(View caller);
        }
    }
}