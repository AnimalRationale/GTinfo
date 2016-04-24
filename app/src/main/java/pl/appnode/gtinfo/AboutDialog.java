package pl.appnode.gtinfo;

import android.app.Activity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import static pl.appnode.gtinfo.Constants.RATING_1_STAR;
import static pl.appnode.gtinfo.Constants.RATING_2_STARS;
import static pl.appnode.gtinfo.Constants.RATING_3_STARS;
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

    private static void showServersListInformation(Context context, View aboutDialog) {
        String serversOnList = context.getResources()
                .getString(R.string.dialog_about_servers_on_list)
                + sServersList.size();
        TextView textServersList = (TextView) aboutDialog.findViewById(R.id.aboutDialogServersList);
        textServersList.setText(serversOnList);
        int rating1 = 0, rating2 = 0, rating3 = 0;
        for (int i = 0; i < sServersList.size(); i++ ) {
            GameServerItem server = sServersList.get(i);
            switch (server.mRating) {
                case RATING_3_STARS:
                    rating3++;
                    break;
                case RATING_2_STARS:
                    rating2++;
                    break;
                case RATING_1_STAR:
                    rating1++;
                    break;
                default:
                    break;
            }
        }
        if (rating1 + rating2 + rating3 > 0) {
            View ratings = aboutDialog.findViewById(R.id.aboutDialogRatings);
            ratings.setVisibility(View.VISIBLE);
            int ratingIconColor = argbColor(ContextCompat
                    .getColor(context, R.color.icon_list_orange_light));
            if (rating1 > 0) {
                TextView rating1Text = (TextView) aboutDialog.findViewById(R.id.aboutDialogRating1Text);
                rating1Text.setText("" + rating1);
                ImageView image = (ImageView) aboutDialog.findViewById(R.id.aboutDialogRating1Image);
                final Drawable imageBackground = image.getBackground();
                imageBackground.setColorFilter(ratingIconColor, PorterDuff.Mode.SRC_IN);
            }
            if (rating2 > 0) {
                TextView rating2Text = (TextView) aboutDialog.findViewById(R.id.aboutDialogRating2Text);
                rating2Text.setText("" + rating2);
                ImageView image = (ImageView) aboutDialog.findViewById(R.id.aboutDialogRating2Image);
                final Drawable imageBackground = image.getBackground();
                imageBackground.setColorFilter(ratingIconColor, PorterDuff.Mode.SRC_IN);
            }
            if (rating3 > 0) {
                TextView rating3Text = (TextView) aboutDialog.findViewById(R.id.aboutDialogRating3Text);
                rating3Text.setText("" + rating3);
                ImageView image = (ImageView) aboutDialog.findViewById(R.id.aboutDialogRating3Image);
                final Drawable imageBackground = image.getBackground();
                imageBackground.setColorFilter(ratingIconColor, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    private static int argbColor(int colorResource) {
        return Color.argb(Color.alpha(colorResource),
                Color.red(colorResource),
                Color.green(colorResource),
                Color.blue(colorResource));
    }

    public static void showDialog(Activity callingActivity) {
        versionInfo(callingActivity);
        String aboutVersion = sVersionName + "." + sVersionCode;
        LayoutInflater layoutInflater = LayoutInflater.from(callingActivity);
        View aboutDialog = layoutInflater.inflate(R.layout.dialog_about, null) ;
        TextView textAbout = (TextView) aboutDialog.findViewById(R.id.aboutDialogInfo);
        textAbout.setText(aboutVersion);
        if (!sServersList.isEmpty()) {
            showServersListInformation(callingActivity, aboutDialog);
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
