package pl.appnode.gtinfo;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pl.appnode.gtinfo.Constants.ADDED_SERVER_ADDRESS;
import static pl.appnode.gtinfo.Constants.ADDED_SERVER_NAME;
import static pl.appnode.gtinfo.Constants.ADDED_SERVER_RATING;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_ADDRESS;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_ADDRESS_EDITTEXT_ID;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_LIST_POSITION;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_NAME;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_NAME_EDITTEXT_ID;
import static pl.appnode.gtinfo.Constants.EDIT_SERVER_RATING;
import static pl.appnode.gtinfo.Constants.IP_ADDRESS_PORT_PATTERN;
import static pl.appnode.gtinfo.Constants.NO_ITEM;
import static pl.appnode.gtinfo.Constants.RATING_0_STARS;
import static pl.appnode.gtinfo.Constants.RATING_1_STAR;
import static pl.appnode.gtinfo.Constants.RATING_2_STARS;
import static pl.appnode.gtinfo.Constants.RATING_3_STARS;
import static pl.appnode.gtinfo.PreferencesSetupHelper.isDarkTheme;
import static pl.appnode.gtinfo.PreferencesSetupHelper.themeSetup;

/**
 * Activity shows dialog for adding new server to data set
 * or editing data of existing in data set server (IP_address:port and server name),
 * validates entered IP address and returns result intent with entered data.
 */
public class AddGameServerDialogActivity extends Activity implements View.OnClickListener {

    private static final String LOGTAG = "AddServer";
    private EditText mEditServerAddress;
    private EditText mEditServerName;
    private RatingBar mEditServerRating;
    private boolean mIsEdit = false;
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeSetup(this); // Setting theme
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog_add_server);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (isDarkTheme(this)) {
            getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.dark_grey));
        }
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
        if (isDarkTheme(this)) {
            mEditServerAddress.setTextColor(ContextCompat.getColor(this, R.color.white));
            mEditServerName.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
        mEditServerRating = (RatingBar) findViewById(R.id.serverRatingBar);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            LayerDrawable ratingStar = (LayerDrawable) mEditServerRating.getProgressDrawable();
            DrawableCompat.setTint(DrawableCompat.wrap(ratingStar.getDrawable(0)),
                    ContextCompat.getColor(this, R.color.light_grey));  // Empty star
            DrawableCompat.setTint(DrawableCompat.wrap(ratingStar.getDrawable(2)),
                    ContextCompat.getColor(this, R.color.icon_list_orange_light));  // Full star
        }
        getIntentData(getIntent());
    }

    // Checks required action, sets up view and flag indicating edit mode if needed
    private void getIntentData(Intent intent) {
        if (intent.getExtras() != null && intent.hasExtra(EDIT_SERVER_ADDRESS)) {
            mEditServerAddress.setText(intent.getStringExtra(EDIT_SERVER_ADDRESS));
            mEditServerName.setText(intent.getStringExtra(EDIT_SERVER_NAME));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                // Sets long click listeners to avoid ActionBarContextView IllegalStateException
                // on devices with Android version prior to M,
                // long click copies EditText content (address) to clipboard
                mEditServerAddress.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        if (!mEditServerAddress.getText().toString().equals("")) {
                            longClickCopyToClipboard(mEditServerAddress.getText().toString(),
                                    EDIT_SERVER_ADDRESS_EDITTEXT_ID);
                        }
                        return true;
                    }
                });
                mEditServerName.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        if (!mEditServerName.getText().toString().equals("")) {
                            longClickCopyToClipboard(mEditServerName.getText().toString(),
                                    EDIT_SERVER_NAME_EDITTEXT_ID);
                        }
                        return true;
                    }
                });
            }
            switch(intent.getStringExtra(EDIT_SERVER_RATING)) {
                case RATING_3_STARS:
                    mEditServerRating.setRating(3.0f);
                    break;
                case RATING_2_STARS:
                    mEditServerRating.setRating(2.0f);
                    break;
                case RATING_1_STAR:
                    mEditServerRating.setRating(1.0f);
                    break;
                default:
                    mEditServerRating.setRating(0.0f);
            }
            mPosition = intent.getIntExtra(EDIT_SERVER_LIST_POSITION, NO_ITEM);
            TextView editServerTitle = (TextView) findViewById(R.id.serverEditTitle);
            editServerTitle.setVisibility(View.VISIBLE);
            mIsEdit = true;
        }
    }

    private void longClickCopyToClipboard(String text, int editText) {
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(AddGameServerDialogActivity.this.CLIPBOARD_SERVICE);
        String description;
        String info;
        if (editText == EDIT_SERVER_ADDRESS_EDITTEXT_ID) {
            description = "Server IP:port";
            info = getResources().getString(R.string.server_edit_copy_address);
        } else {
            description = "Server name";
            info = getResources().getString(R.string.server_edit_copy_name);
        }
        ClipData clip = ClipData.newPlainText(description, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(AddGameServerDialogActivity.this, info, Toast.LENGTH_SHORT).show();
    }

    // Handles pressing of positive button and calls validation on entered server IP:port
    private void pressedOk() {
        String address = mEditServerAddress.getText().toString();
        String name = mEditServerName.getText().toString();
        String rating;
        int ratingValue = (int) mEditServerRating.getRating();
        switch (ratingValue) {
            case 1:
                rating = RATING_1_STAR;
                break;
            case 2:
                rating = RATING_2_STARS;
                break;
            case 3:
                rating = RATING_3_STARS;
                break;
            default:
                rating = RATING_0_STARS;
        }
        if (!address.equals("") && !name.equals("")) {
            if (validateServerAddress(address)) {
                resultOk(address, name, rating);
            }
        }
    }

    private void pressedCancel() {
        resultCancel();
    }

    // Validates entered server IP:port, using regular expression from Constants
    private boolean validateServerAddress(String address) {
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

    // Prepares and executes intent with positive result of performed action
    private void resultOk(String address, String name, String rating) {
        if (!mIsEdit) {
            Intent resultIntent = getIntent();
            resultIntent.putExtra(ADDED_SERVER_ADDRESS, address);
            resultIntent.putExtra(ADDED_SERVER_NAME, name);
            resultIntent.putExtra(ADDED_SERVER_RATING, rating);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Intent resultIntent = getIntent();
            resultIntent.putExtra(EDIT_SERVER_ADDRESS, address);
            resultIntent.putExtra(EDIT_SERVER_NAME, name);
            resultIntent.putExtra(ADDED_SERVER_RATING, rating);
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
