package com.cameracountmodule.manager;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

public class GooglePlayApp {

    private static String PACKAGE_NAME = "";

    public static boolean isInstalledApp(Context context, String packageName) {
        PACKAGE_NAME = packageName;
        if (!hasAppInstalled(context)) {
            openAppInPlayStore(context);
            return false;
        }
        return true;
    }

    private static boolean hasAppInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static void openAppInPlayStore(Context context) {
        try {
            startViewUri(context, "market://details?id=" + PACKAGE_NAME);
        } catch (ActivityNotFoundException anfe) {
            startViewUri(context, "http://play.google.com/store/apps/details?id=" + PACKAGE_NAME);
        }
    }

    private static void startViewUri(Context context, String uri) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
    }
}
