package com.softard.wow.screencapture;

import android.app.Application;
import android.content.Context;

/**
 * Created by wow on 5/8/18.
 */

public class MainApplication extends Application {
    public static Context sAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = this;
    }
}
