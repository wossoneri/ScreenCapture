package com.softard.wow.screencapture.Utils;

import android.content.Context;
import android.util.DisplayMetrics;

import com.softard.wow.screencapture.MainApplication;

/**
 * Created by wow on 5/8/18.
 */

public class ScreenUtils {

    public static float getScreenDensity() {
        Context context = MainApplication.sAppContext;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.density;
    }
}
