package com.example.crowddensityapplication;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.widget.Toast;



import java.io.UnsupportedEncodingException;
import java.util.Map;

public class BackgroundPingSender extends BroadcastReceiver {

    Context hostContext;
    SharedPreferences sh;
    @Override
    public void onReceive(Context context, Intent intent) {
        hostContext = context;
        Intent serviceIntent = new Intent(context, DeviceScannerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            hostContext.startForegroundService(serviceIntent);
        }
        else
        {
            hostContext.startService(serviceIntent);
        }
        Toast.makeText(hostContext,"Service started",Toast.LENGTH_SHORT).show();

    }

}
