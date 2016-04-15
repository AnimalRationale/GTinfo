package pl.appnode.gtinfo;

import android.app.Activity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import static pl.appnode.gtinfo.GameServerItemListActivity.sServersList;

/**
 * Shows information dialog with application's icon, name, version and code version
 */
class AboutDialog {

    private static String sVersionName;
    private static String sVersionCode;

    private static void versionInfo(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            sVersionName = info.versionName;
            sVersionCode = String.valueOf(info.versionCode);
        }
        catch (PackageManager.NameNotFoundException ex) {
            sVersionName = context.getResources().getString(R.string.dialog_about_version_name_error);
            sVersionCode = context.getResources().getString(R.string.dialog_about_version_code_error);
        }
    }

    public static void showDialog(Activity callingActivity) {
        versionInfo(callingActivity);
        String aboutVersion = sVersionName + "." + sVersionCode;
        LayoutInflater layoutInflater = LayoutInflater.from(callingActivity);
        View aboutDialog = layoutInflater.inflate(R.layout.dialog_about, null) ;
        TextView textAbout = (TextView) aboutDialog.findViewById(R.id.aboutDialogInfo);
        textAbout.setText(aboutVersion);
        if (!GameServerItemListActivity.sServersList.isEmpty()) {
            String serversOnList = callingActivity.getResources()
                    .getString(R.string.dialog_about_servers_on_list)
                    + GameServerItemListActivity.sServersList.size();
            TextView textServersList = (TextView) aboutDialog.findViewById(R.id.aboutDialogServersList);
            textServersList.setText(serversOnList);
        }
        new AlertDialog.Builder(callingActivity)
                .setTitle(callingActivity.getResources().getString(R.string.dialog_about_title)
                        + callingActivity.getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(true)
                .setPositiveButton(callingActivity.getResources().getString(R.string.dialog_about_ok), null)
                .setView(aboutDialog)
                .show();
    }
}
