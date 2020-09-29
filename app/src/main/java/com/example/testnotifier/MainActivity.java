package com.example.testnotifier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {

    private TextView tw1;
    private BroadcastReceiver updateUiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tw1 = findViewById(R.id.tw1);
        bindBroadcastReceiver();

        tw1.setText(getPreferences(Context.MODE_PRIVATE).getString("fcmt", "empty token, please reinstall"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindBroadcastReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindBroadcastReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindBroadcastReceiver();
    }

    private void bindBroadcastReceiver() {
        if (updateUiReceiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("NEW_FCM_TOKEN");
            updateUiReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String token = intent.getStringExtra("TOKEN");
                    tw1.setText(token);
                    getPreferences(Context.MODE_PRIVATE).edit().putString("fcmt", token).apply();
                }
            };
            registerReceiver(updateUiReceiver, filter);
        }
    }

    private void unbindBroadcastReceiver() {
        if (updateUiReceiver != null) {
            unregisterReceiver(updateUiReceiver);
            updateUiReceiver = null;
        }
    }
}
