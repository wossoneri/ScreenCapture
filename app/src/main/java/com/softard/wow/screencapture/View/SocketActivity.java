package com.softard.wow.screencapture.View;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.softard.wow.screencapture.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SocketActivity extends AppCompatActivity {

    @BindView(R.id.CS_switch) Switch CSSwitch;
    SocketClientFragment socketClientFragment;
    SocketServerFragment socketServerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        ButterKnife.bind(this);

        socketClientFragment = new SocketClientFragment();
        socketServerFragment = new SocketServerFragment();

        CSSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setServer();
                } else {
                    setClient();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setClient();
    }

    private void setClient() {
        CSSwitch.setText(R.string.socket_client);
        getSupportFragmentManager().beginTransaction().replace(
                R.id.socket_fragment_container,
                socketClientFragment).commit();
    }

    private void setServer() {
        CSSwitch.setText(R.string.socket_server);
        getSupportFragmentManager().beginTransaction().replace(
                R.id.socket_fragment_container,
                socketServerFragment).commit();
    }
}
