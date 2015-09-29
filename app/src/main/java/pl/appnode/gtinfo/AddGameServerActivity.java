package pl.appnode.gtinfo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import static pl.appnode.gtinfo.PreferencesSetupHelper.themeSetup;
import static pl.appnode.gtinfo.Constants.SERVERS_PREFS_FILE;

public class AddGameServerActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "AddServer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeSetup(this); // Setting theme
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog_add_server);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = 0.4f;
        getWindow().setAttributes(layoutParams);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        this.setFinishOnTouchOutside(false);
        Button buttonOk = (Button) findViewById(R.id.okAddServer);
        buttonOk.setOnClickListener(this);
        Button buttonCancel = (Button) findViewById(R.id.cancelAddServer);
        buttonCancel.setOnClickListener(this);
    }

    private void pressedOk() {
        finish();
    }

    private void pressedCancel() {
        finish();
    }

    public static void saveAddedServer(String address, String name) {
        SharedPreferences serversPrefs = AppContextHelper.getContext()
                .getSharedPreferences(SERVERS_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = serversPrefs.edit();
        editor.putString(address, name);
        editor.apply();
        Log.d(TAG, "Saved server: " + address + " with name: " + name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.okAddServer:
                pressedOk();
                break;
            case R.id.cancelAddServer:
                pressedCancel();
                break;
        }
    }
}
