package com.softard.wow.screencapture;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.softard.wow.screencapture.QRCode.ScanQRActivity;
import com.softard.wow.screencapture.Utils.ScreenCapturer;
import com.softard.wow.screencapture.View.CameraRecordActivity;
import com.softard.wow.screencapture.View.RTSPlayerActivity;
import com.softard.wow.screencapture.View.ScreenRecordActivity;
import com.softard.wow.screencapture.View.ScreenRecordByCodecActivity;
import com.softard.wow.screencapture.View.SocketActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 100;
    private static MediaProjection sMediaProjection;
    @BindView(R.id.btn_capture) Button mBtnCapture;
    @BindView(R.id.btn_record) Button mBtnRecordScreen;
    @BindView(R.id.btn_record_codec) Button mBtnRecordByCodec;
    @BindView(R.id.btn_record_camera) Button mBtnRecordCamera;
    @BindView(R.id.btn_scan_qr) Button mBtnScanQR;
    @BindView(R.id.btn_rtsp_player) Button mBtnRtsp;
    @BindView(R.id.btn_socket_test) Button mBtnSocket;
    private MediaProjectionManager mProjectionManager;
    private ScreenCapturer mSC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mBtnCapture.setOnClickListener(this);
        mBtnRecordScreen.setOnClickListener(this);
        mBtnRecordByCodec.setOnClickListener(this);
        mBtnRecordCamera.setOnClickListener(this);
        mBtnScanQR.setOnClickListener(this);
        mBtnRtsp.setOnClickListener(this);
        mBtnSocket.setOnClickListener(this);

        mProjectionManager = (MediaProjectionManager) getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_capture:
//                Intent i = new Intent(MainActivity.this, CaptureActivity.class);
//                i.putExtra("path", "some path");
//                startActivity(i);
                startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                        REQUEST_CODE);
                break;
            case R.id.btn_record:
                startActivity(new Intent(MainActivity.this, ScreenRecordActivity.class));
                break;

            case R.id.btn_record_codec:
                startActivity(new Intent(MainActivity.this, ScreenRecordByCodecActivity.class));
                break;

            case R.id.btn_record_camera:
                startActivity(new Intent(MainActivity.this, CameraRecordActivity.class));
                break;

            case R.id.btn_scan_qr:
                startActivity(new Intent(MainActivity.this, ScanQRActivity.class));
                break;
            case R.id.btn_rtsp_player:
                startActivity(new Intent(MainActivity.this, RTSPlayerActivity.class));
                break;
            case R.id.btn_socket_test:
                startActivity(new Intent(MainActivity.this, SocketActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("WOW", "on result : requestCode = " + requestCode + " resultCode = " + resultCode);
        if (RESULT_OK == resultCode && REQUEST_CODE == requestCode) {
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            if (sMediaProjection != null) {
//                Intent i = new Intent(MainActivity.this, ScreenCaptureService.class);
//                i.putExtra(ScreenCaptureService.EXTRA_PATH, "some path");
//                i.putExtra(ScreenCaptureService.EXTRA_MEDIA_PROJECTION, () sMediaProjection);
                Log.d("WOW", "Start capturing...");
                new ScreenCapturer(this, sMediaProjection, "").startProjection();
            }
        }
    }


}
