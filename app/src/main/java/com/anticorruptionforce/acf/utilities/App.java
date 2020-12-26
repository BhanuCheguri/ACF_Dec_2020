package com.anticorruptionforce.acf.utilities;


import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.anticorruptionforce.acf.sms_verification.AppSignatureHelper;

/**
 * Created on : May 21, 201Secure@1239
 * Author     : AndroidWave
 */
public class App extends Application {
    private static Context context;
    ConnectivityManager connectivityManager;
    boolean isConnected;


    public static Context getAppContext() {
        return App.context;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();

        AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this);
        appSignatureHelper.getAppSignatures();
   }

    public static boolean isNetworkAvailable() {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr != null) {
            NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

            if (activeNetworkInfo != null) { // connected to the internet
                // connected to the mobile provider's data plan
                if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    return true;
                } else return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        }
        return false;
    }
}
