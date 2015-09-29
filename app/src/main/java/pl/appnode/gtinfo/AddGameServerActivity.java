package pl.appnode.gtinfo;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import static pl.appnode.gtinfo.PreferencesSetupHelper.themeSetup;

public class AddGameServerActivity extends Activity {

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
    }
}
