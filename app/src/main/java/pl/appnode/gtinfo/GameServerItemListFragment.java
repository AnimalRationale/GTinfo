package pl.appnode.gtinfo;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static pl.appnode.gtinfo.Constants.SERVERS_PREFS_FILE;

public class GameServerItemListFragment extends Fragment {

    private static final String TAG = "GSI-List-fragment";
    private static final String TAG_V = "GameServerItemListFragment";
    protected static List<GameServerItem> sServersList = new ArrayList<>();
    protected static GameServersAdapter sServersAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initServerList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gameserveritem_list, container, false);
        rootView.setTag(TAG_V);
        RecyclerView recyclerServersList = (RecyclerView) rootView.findViewById(R.id.serversList);
        // recyclerServersList.setItemAnimator(null);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerServersList.setLayoutManager(llm);
        sServersAdapter = new GameServersAdapter(getActivity());
        recyclerServersList.setAdapter(sServersAdapter);
        return rootView;
    }

    private void initServerList() {
        if (sServersList.isEmpty()) {
            SharedPreferences gameServersPrefs = AppContextHelper.getContext()
                    .getSharedPreferences(SERVERS_PREFS_FILE, 0);
            Map<String, ?> keys = gameServersPrefs.getAll();
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                GameServerItem gameServer = new GameServerItem();
                gameServer.mId = entry.getKey();
                gameServer.mName = entry.getValue().toString();
                sServersList.add(gameServer);
            }
        }
    }
}
