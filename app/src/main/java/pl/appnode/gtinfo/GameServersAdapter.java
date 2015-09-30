package pl.appnode.gtinfo;

import android.content.Context;
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
    }

    @Override
    public ServerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);
        CardView card = (CardView) itemView;
        if (isDarkTheme(mContext)) {
            card.setCardBackgroundColor(Color.BLACK);
        } else card.setCardBackgroundColor(Color.WHITE);
        return new ServerViewHolder(itemView);
    }

    public static class ServerViewHolder extends RecyclerView.ViewHolder {

        protected static TextView vName;

        public ServerViewHolder(View v) {
            super(v);
            vName = (TextView) v.findViewById(R.id.server_name);
        }
    }
}