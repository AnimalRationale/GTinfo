package pl.appnode.gtinfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pl.appnode.gtinfo.Constants.ADDED_SERVER_ADDRESS;
import static pl.appnode.gtinfo.Constants.ADDED_SERVER_NAME;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_ADDRESS;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_LIST_POSITION;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_NAME;
import static pl.appnode.gtinfo.Constants.IP_ADDRESS_PORT_PATTERN;
import static pl.appnode.gtinfo.Constants.NO_ITEM;
import static pl.appnode.gtinfo.PreferencesSetupHelper.themeSetup;

/** Activity shows dialog for adding new server to data set
 * or editing data of existing in data set server (IPaddress:port and server name)*/
public class AddGameServerActivity extends Activity implements View.OnClickListener {

    private static final String LOGTAG = "AddServer";
    private EditText mEditServerAddress;
    private EditText mEditServerName;
    private boolean mIsEdit = false;
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeSetup(this); // Setting theme
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog_add_server);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        this.setFinishOnTouchOutside(false);
        Button buttonOk = (Button) findViewById(R.id.okAddServer);
        buttonOk.setOnClickListener(this);
        Button buttonCancel = (Button) findViewById(R.id.cancelAddServer);
        buttonCancel.setOnClickListener(this);
        mEditServerAddress = (EditText) findViewById(R.id.serverAddress);
        mEditServerName = (EditText) findViewById(R.id.serverNameText);
        getIntentData(getIntent());
    }

    private void getIntentData(Intent intent) {
        // Checks required action, sets up view and flag for edit if needed
        if (intent.getExtras() != null && intent.hasExtra(EDIT_SERVER_ADDRESS)) {
            mEditServerAddress.setText(intent.getStringExtra(EDIT_SERVER_ADDRESS));
            mEditServerName.setText(intent.getStringExtra(EDIT_SERVER_NAME));
            mPosition = intent.getIntExtra(EDIT_SERVER_LIST_POSITION, NO_ITEM);
            TextView editServerTitle = (TextView) findViewById(R.id.serverEditTitle);
            editServerTitle.setVisibility(View.VISIBLE);
            mIsEdit = true;
        }
    }

    private void pressedOk() {
        // Handles pressing of positive button and calls validation on entered server IP:port
        String address = mEditServerAddress.getText().toString();
        String name = mEditServerName.getText().toString();
        if (!address.equals("")) {
            if (validateServerAddress(address)) {
                resultOk(address, name);
            }
        }
    }

    private void pressedCancel() {
        resultCancel();
    }

    private boolean validateServerAddress(String address) {
        // Validates entered server IP:port, using regular expression from Constants
        Pattern validationPattern = Pattern.compile(IP_ADDRESS_PORT_PATTERN);
        String errorMessage = this.getResources().getString(R.string.server_add_invalid_address);
        Matcher matcher = validationPattern.matcher(address);
        if (matcher.matches()) {
            int port = Integer.parseInt(matcher.group(5));
            if (port > 1023 && port <= 65535) {
                return true;
            } else errorMessage = this.getResources().getString(R.string.server_add_invalid_port);
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void resultOk(String address, String name) {
        // Prepares and executes intent with positive result of performed action
        if (!mIsEdit) {
            Intent resultIntent = getIntent();
            resultIntent.putExtra(ADDED_SERVER_ADDRESS, address);
            resultIntent.putExtra(ADDED_SERVER_NAME, name);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Intent resultIntent = getIntent();
            resultIntent.putExtra(EDIT_SERVER_ADDRESS, address);
            resultIntent.putExtra(EDIT_SERVER_NAME, name);
            resultIntent.putExtra(EDIT_SERVER_LIST_POSITION, mPosition);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private void resultCancel() {
        Intent resultIntent = getIntent();
        setResult(RESULT_CANCELED, resultIntent);
        finish();
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
    }}
