package com.joinacf.acf.utilities;


import android.app.Application;
import android.content.Context;

import com.joinacf.acf.sms_verification.AppSignatureHelper;

/**
 * Created on : May 21, 201Secure@1239
 * Author     : AndroidWave
 */
public class App extends Application {
    private static Context context;
    public static Context getAppContext() {
        return App.context;
    }

    @Override
  public void onCreate() {
    super.onCreate();
    AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this);
    appSignatureHelper.getAppSignatures();
  }
}
