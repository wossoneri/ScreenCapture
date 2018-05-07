package com.softard.wow.screencapture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wow on 3/23/18.
 */

public class ScreenCapture {
    private static MediaProjection sMediaProjection;

    private int mDensity;
    private Display mDisplay;
    private int mWidth;
    private int mHeight;
    private ImageReader mImageReader;
    private VirtualDisplay mVirtualDisplay;
    private Handler mHandler;
    private String STORE_DIR;
    private Context mContext;


    public ScreenCapture(Context context, MediaProjection mediaProjection, String savePath) {
        sMediaProjection = mediaProjection;
        mContext = context;

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();


        if (TextUtils.isEmpty(savePath)) {
            File externalFilesDir = mContext.getExternalFilesDir(null);
            Log.d("WOW", "externalFilesDir:" + externalFilesDir.getAbsolutePath());
            if (externalFilesDir != null) {
                STORE_DIR = externalFilesDir.getAbsolutePath() + "/myScreenshots";
            } else {
                Toast.makeText(mContext, "No save path assigned!", Toast.LENGTH_SHORT);
            }
        } else {
            STORE_DIR = savePath;
        }
    }

    public void startProjection() {
        if (sMediaProjection != null) {
            File storeDir = new File(STORE_DIR);
            if (!storeDir.exists()) {
                boolean success = storeDir.mkdirs();
                if (!success) {
                    Log.d("WOW", "mkdir " + storeDir + "  failed");
                    return;
                } else {
                    Log.d("WOW", "mkdir " + storeDir + "  success");
                }
            } else {
                Log.d("WOW", " " + storeDir + "  exist");
            }

        } else {
            Log.d("WOW", "get mediaprojection failed");
        }

        WindowManager window = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mDisplay = window.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        // use getMetrics is 2030, use getRealMetrics is 2160, the diff is NavigationBar's height
        mDisplay.getRealMetrics(metrics);
        mDensity = metrics.densityDpi;
//            Log.d("WOW", "metrics.widthPixels is " + metrics.widthPixels);
//            Log.d("WOW", "metrics.heightPixels is " + metrics.heightPixels);
        mWidth = metrics.widthPixels;//size.x;
        mHeight = metrics.heightPixels;//size.y;

        //start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = sMediaProjection.createVirtualDisplay(
                "ScreenShot",
                mWidth,
                mHeight,
                mDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mImageReader.getSurface(),
                null,
                mHandler);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = null;
                FileOutputStream fos = null;
                Bitmap bitmap = null;

                try {
                    image = reader.acquireLatestImage();
                    if (image != null) {
                        Image.Plane[] planes = image.getPlanes();
                        ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride();
                        Log.d("WOW", "pixelStride is " + pixelStride);
                        int rowStride = planes[0].getRowStride();
                        Log.d("WOW", "row Stride is " + rowStride);
                        int rowPadding = rowStride - pixelStride * mWidth;

                        bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride,
                                mHeight, Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(buffer);

                        Date currentDate = new Date();
                        SimpleDateFormat date = new SimpleDateFormat("yyyyMMddhhmmss");
                        String fileName = STORE_DIR + "/myScreen_" + date.format(currentDate) + ".png";
                        fos = new FileOutputStream(fileName);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        Log.d("WOW", "End now!!!!!!");
                        Toast.makeText(mContext, "Screenshot saved in " + fileName, Toast.LENGTH_LONG);
                        stopProjection();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                    if (image != null) {
                        image.close();
                    }
                }

            }
        }, mHandler);
        sMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
    }

    public void stopProjection() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (sMediaProjection != null) {
                    sMediaProjection.stop();
                }
            }
        });
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) {
                        mVirtualDisplay.release();
                    }
                    if (mImageReader != null) {
                        mImageReader.setOnImageAvailableListener(null, null);
                    }
                    sMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
        }
    }
}
