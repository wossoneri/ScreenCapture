package com.softard.wow.screencapture.Utils;

import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.softard.wow.screencapture.MainApplication;

/**
 * Created by wow on 5/8/18.
 */

public class ScreenUtils {

    public static final String VIDEO_PATH = Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_DOWNLOADS) + "/video.mp4";

    public static float getScreenDensity() {
        Context context = MainApplication.sAppContext;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.density;
    }


}
