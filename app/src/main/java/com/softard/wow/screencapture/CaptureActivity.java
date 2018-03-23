package com.softard.wow.screencapture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CaptureActivity extends Activity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_capture);

        String path = getIntent().getStringExtra("path");
        if (null == path) {
            File externalFilesDir = getExternalFilesDir(null);
            Log.d("WOW", "externalFilesDir:" + externalFilesDir.getAbsolutePath());
            if (externalFilesDir != null) {
                STORE_DIR = externalFilesDir.getAbsolutePath() + "/myScreenshots";
            } else {
                Toast.makeText(this, "No save path assigned!", Toast.LENGTH_SHORT);
            }
        } else {
            STORE_DIR = path;
        }

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startProjection();
    }

    private void startProjection() {
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private void stopProjection() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (sMediaProjection != null) {
                    sMediaProjection.stop();
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode && REQUEST_CODE == requestCode) {
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);

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

            mDisplay = getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            // use getMetrics is 2030, use getRealMetrics is 2160, the diff is NavigationBar's height
            mDisplay.getRealMetrics(metrics);
            mDensity = metrics.densityDpi;
//            Log.d("WOW", "metrics.widthPixels is " + metrics.widthPixels);
//            Log.d("WOW", "metrics.heightPixels is " + metrics.heightPixels);
            mWidth = metrics.widthPixels;//size.x;
            mHeight = metrics.heightPixels;//size.y;

            //start capture reader
            mImageReader = ImageReader.newInstance(mWidth, mHeight,
                    PixelFormat.RGBA_8888, 1);
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
                            int rowStride = planes[0].getRowStride();
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
                            Toast.makeText(CaptureActivity.this, "Screenshot saved in " + fileName, Toast.LENGTH_LONG);
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
            sMediaProjection.registerCallback(new CaptureActivity.MediaProjectionStopCallback(), mHandler);
        }
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
