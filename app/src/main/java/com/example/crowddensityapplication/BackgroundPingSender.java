package com.example.crowddensityapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BackgroundPingSender extends BroadcastReceiver {

    Context hostContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        hostContext = context;
        Toast.makeText(context,"data to be pinged",Toast.LENGTH_SHORT).show();
    }
}
