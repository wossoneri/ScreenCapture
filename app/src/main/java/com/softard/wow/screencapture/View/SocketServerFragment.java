package com.softard.wow.screencapture.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.softard.wow.screencapture.R;
import com.softard.wow.screencapture.Utils.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.softard.wow.screencapture.Utils.SocketUtils.PORT;


/**
 * Created by wOw on 2019-05-21.
 * Email: wossoneri@163.com
 * Copyright (c) 2019 Softard. All rights reserved.
 */
public class SocketServerFragment extends Fragment {


    public ServerSocket serverSocket = null;
    @BindView(R.id.server_text_ip) TextView textServerIp;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0x11) {
//                Bundle bundle = msg.getData();
                textServerIp.append("client says:" + /*bundle.getString("msg") + */"1111\n");
            }
        }
    };
    Unbinder unbinder;
    ServerThread serverThread;
    StringBuilder strBuilder = new StringBuilder();
    private String IP = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_socket_server, container, false);
        unbinder = ButterKnife.bind(this, v);
        serverThread = new ServerThread();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        IP = getlocalip();
        textServerIp.setText("IP addresss:" + IP + "\n");
        startServerThread();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void startServerThread() {
        if (!serverThread.isAlive()) {
            serverThread.stop = false;
            serverThread.start();
        }
    }

    private void stopServerThread() {
        if (serverThread.isAlive()) {
            serverThread.stop = true;
            serverThread.interrupt();
        }
    }

    private String getlocalip() {
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        //  Log.d(Tag, "int ip "+ipAddress);
        if (ipAddress == 0) return null;

        Log.d("WOW", "Origin ip:" + ipAddress);
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "." + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }

    class ServerThread extends Thread {
        volatile boolean stop = false;

        @Override
        public void run() {
            Bundle bundle = new Bundle();
            bundle.clear();
            OutputStream output;
            String str = "hello hehe";
            try {
                serverSocket = new ServerSocket(PORT);
                while (!stop) {
                    Log.d("WOW", "LOOP");
                    Message msg = new Message();
                    msg.what = 0x11;
                    try {
                        Socket socket = serverSocket.accept();
                        output = socket.getOutputStream();
                        output.write(str.getBytes("gbk"));
                        output.flush();
                        socket.shutdownOutput();
                        Log.d("WOW", "LOOP1111");

                        DataInputStream in = new DataInputStream(socket.getInputStream());
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                        byte[] inputByte = new byte[2048];
                        byte[] finalByte = new byte[0];
                        int length = 0;
                        Log.d("WOW", "start to receive");
                        while ((length = in.read(inputByte, 0, inputByte.length)) > 0) {
//                            outputStream.write(inputByte, 0, length);
                            byte[] tmp = new byte[finalByte.length + length];
                            System.arraycopy(finalByte, 0, tmp, 0, finalByte.length);
                            System.arraycopy(inputByte, 0, tmp, finalByte.length, length);
                            finalByte = tmp;
                            Log.d("WOW", "final byte[] length:" + finalByte.length);
                        }

                        Log.d("WOW", "receiveddddddd");
                        Bitmap bmp = ImageUtils.byte2bitmap(finalByte);

                        // save bmp
                        Log.d("WOW", "get bmp:" + (bmp != null));
                        SimpleDateFormat date = new SimpleDateFormat("yyyyMMddhhmmss");
                        String fileName = getContext().getExternalFilesDir(null).getAbsolutePath()
                                + "/myScreenshots" + "/received_" + date.format(new Date()) + ".png";
                        Log.d("WOW", "save to " + fileName);
                        FileOutputStream fos = new FileOutputStream(fileName);
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);

                        in.close();
                        outputStream.close();


                        mHandler.sendMessage(msg);
//                        bff.close();
                        output.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }
}
