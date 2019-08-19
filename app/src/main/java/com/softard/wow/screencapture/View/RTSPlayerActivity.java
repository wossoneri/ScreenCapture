package com.softard.wow.screencapture.View;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.VideoView;

import com.softard.wow.screencapture.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RTSPlayerActivity extends AppCompatActivity {

    @BindView(R.id.videoView) VideoView mVideoView;

    private String mURL = "rtsp://118.25.39.144/sample_h264_1mbit.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtsplayer);
        ButterKnife.bind(this);

        mVideoView.setVideoPath(mURL);
        mVideoView.requestFocus();
        mVideoView.start();
    }
}
