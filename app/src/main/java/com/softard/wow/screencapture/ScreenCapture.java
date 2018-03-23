package com.softard.wow.screencapture;

import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.view.Display;

/**
 * Created by wow on 3/23/18.
 */

public class ScreenCapture {
    private static final int REQUEST_CODE = 100;
    private static MediaProjection sMediaProjection;

    private MediaProjectionManager mProjectionManager;
    private int mDensity;
    private Display mDisplay;
    private int mWidth;
    private int mHeight;
    private ImageReader mImageReader;
    private VirtualDisplay mVirtualDisplay;
    private Handler mHandler;
    private String STORE_DIR;



}
