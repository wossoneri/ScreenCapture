package com.softard.wow.screencapture.View;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.softard.wow.screencapture.R;
import com.softard.wow.screencapture.Utils.ScreenCapturer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static com.softard.wow.screencapture.Utils.SocketUtils.PORT;


/**
 * Created by wOw on 2019-05-21.
 * Email: wossoneri@163.com
 * Copyright (c) 2019 Softard. All rights reserved.
 */
public class SocketClientFragment extends Fragment implements ScreenCapturer.OnImageCaptureScreenListener {

    private static MediaProjection sMediaProjection;
    @BindView(R.id.client_text_ip) TextView clientTextIp;
    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x11) {
                Bundle bundle = msg.getData();
                clientTextIp.append("server:" + bundle.getString("msg") + "\n");
            }
        }

    };
    @BindView(R.id.client_edit_text_ip) EditText clientEditTextIp;
    @BindView(R.id.client_button_set_ip) Button clientButtonSetIp;
    @BindView(R.id.client_edit_text_message) EditText clientEditText;
    @BindView(R.id.client_button_send_msg) Button clientButton;
    StringBuilder sb;
    String ip;
    Unbinder unbinder;
    private MediaProjectionManager mProjectionManager;
    private ScreenCapturer mSC;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_socket_client, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        clientButtonSetIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip = clientEditTextIp.getText().toString();
                Toast.makeText(getActivity(), "Set ip: " + ip, Toast.LENGTH_LONG).show();
                clientButton.setEnabled(true);
            }
        });

        clientButton.setEnabled(false);
        clientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = clientEditText.getText().toString();

                if (!TextUtils.isEmpty(inputText)) {
                    clientTextIp.append("client:" + inputText + "\n");
                    //启动线程 向服务器发送和接收信息
                    new ClientThread(inputText, ip).start();
                } else {
                    startScreenCapture();// 尝试截屏
                }


            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void imageCaptured(byte[] image) {
        //截屏成功 发送图片
        Log.d("WOW", "image captured and send image " + image.length);
        new ClientThread(image, ip).start();
    }

    private void startScreenCapture() {
        Log.d("WOW", "start cap");
        mProjectionManager = (MediaProjectionManager) getActivity().getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                1000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("WOW", "on result code:" + resultCode + "  requestCode:" + requestCode);
        if (RESULT_OK == resultCode && 1000 == requestCode) {
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            if (sMediaProjection != null) {
                Log.d("WOW", "Start capturing...");
                new ScreenCapturer(getContext(), sMediaProjection, "")
                        .setListener(this).startProjection();
            }
        }
    }

    class ClientThread extends Thread {
        String ipAddr;
        byte[] sendOriginData;

        public ClientThread(String str, String ip) {
            ipAddr = ip;
            try {
                sendOriginData = str.getBytes("gbk");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        public ClientThread(byte[] img, String ip) {
            Log.d("WOW", "send image");
            ipAddr = ip;
            sendOriginData = img;
        }

        @Override
        public void run() {
            //定义消息
            Message msg = new Message();
            msg.what = 0x11;
            Bundle bundle = new Bundle();
            bundle.clear();
            try {
                //连接服务器 并设置连接超时为1秒
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ipAddr, PORT), 1000); //端口号为30000
                //获取输入输出流
                OutputStream ou = socket.getOutputStream();
                BufferedReader bff = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                //读取发来服务器信息
                String line = null;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bff.readLine()) != null) {
                    stringBuilder.append(line);
                }

                //向服务器发送信息  2048byte发一次
                int count = 0;
                byte[] sendBytes = new byte[2048];
                int oriLen = sendOriginData.length;
                while (oriLen > 0) {
                    if (oriLen > 2048) {
                        for (int i = 0; i < 2048; i++) {
                            sendBytes[i] = sendOriginData[i + count * 2048];
                        }
                    } else { // 最后一段
                        sendBytes = null;
                        sendBytes = new byte[oriLen];
                        for (int i = 0; i < oriLen; i++) {
                            sendBytes[i] = sendOriginData[i + count * 2048];
                        }
                    }
                    ou.write(sendBytes, 0, sendBytes.length);
                    ou.flush();
                    oriLen -= 2048;
                    count++;
                }

                bundle.putString("msg", stringBuilder.toString());
                msg.setData(bundle);
                //发送消息 修改UI线程中的组件
                myHandler.sendMessage(msg);
                //关闭各种输入输出流1
                bff.close();
                ou.close();
                socket.close();
            } catch (SocketTimeoutException aa) {
                //连接超时 在UI界面显示消息
                bundle.putString("msg", "服务器连接失败！请检查网络是否打开");
                msg.setData(bundle);
                //发送消息 修改UI线程中的组件
                myHandler.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
