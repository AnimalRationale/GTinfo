package pl.appnode.gtinfo;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import static pl.appnode.gtinfo.Constants.SERVERS_ON_LIST;
import static pl.appnode.gtinfo.PreferencesSetupHelper.isDarkTheme;


/** Shows dialog with confirmation for deleting whole data set */
public class ConfirmationDialogFragment extends DialogFragment {

    public interface ConfirmationDialogListener {
        void onConfirmationDialogPositiveClick(DialogFragment dialog);
        void onConfirmationDialogNegativeClick(DialogFragment dialog);
    }

    ConfirmationDialogListener mListener;

    /** Allows passing arguments (items list size) to confirmation dialog */
    public static ConfirmationDialogFragment newInstance(int serversOnList) {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putInt(SERVERS_ON_LIST, serversOnList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ConfirmationDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ConfirmationDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int serversOnList = getArguments().getInt(SERVERS_ON_LIST);
        int stringResourceId;
        if (serversOnList > 1) {
            stringResourceId = R.string.dialog_confirmation_title_02a;
        } else stringResourceId = R.string.dialog_confirmation_title_02b;
        String title = getActivity().getResources().getString(R.string.dialog_confirmation_title_01)
                + serversOnList
                + getActivity().getResources().getString(stringResourceId);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), setTheme());
        builder.setTitle(title)
                .setMessage(R.string.dialog_confirmation_message)
                .setPositiveButton(R.string.dialog_confirmation_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onConfirmationDialogPositiveClick(ConfirmationDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.dialog_confirmation_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onConfirmationDialogNegativeClick(ConfirmationDialogFragment.this);
                    }
                });
        return builder.create();
    }

    // Sets dialog theme accordingly to user settings for app
    private int setTheme() {
        if (isDarkTheme(getActivity())) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return android.R.style.Theme_Holo_Dialog_NoActionBar;
            } else {
                return android.R.style.Theme_Material_Dialog_NoActionBar;
            }
        } else
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return android.R.style.Theme_Holo_Light_Dialog_NoActionBar;
            } else {
            return android.R.style.Theme_Material_Light_Dialog_NoActionBar;
            }
    }
}
