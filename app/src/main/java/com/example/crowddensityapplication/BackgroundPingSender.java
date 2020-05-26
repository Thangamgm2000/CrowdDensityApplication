package com.example.crowddensityapplication;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class BackgroundPingSender extends BroadcastReceiver {

    Context hostContext;
    SharedPreferences sh;
    @Override
    public void onReceive(Context context, Intent intent) {
        hostContext = context;
        Intent serviceIntent = new Intent(context, DeviceScannerService.class);
        hostContext.startService(serviceIntent);
        Toast.makeText(hostContext,"Service started",Toast.LENGTH_SHORT).show();

    }
    public void sendmessage(String msg)
    {
        byte[] message = new byte[0];
        try {
            message= msg.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); }
        Payload bytesPayload = Payload.fromBytes(message);
        Map<String, ?> allEntries = sh.getAll();
        for(String endpointId: allEntries.keySet())
        {
            Nearby.getConnectionsClient(hostContext).sendPayload(endpointId,bytesPayload);
        }

    }
}
