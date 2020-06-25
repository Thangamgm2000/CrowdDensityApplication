package com.example.crowddensityapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;

public class SplashScreen extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        check_for_permissions();
    }

    private void schedulePingBackground(long parseLong) {
        Intent pingIntent = new Intent( this, BackgroundPingSender.class ) ;
        //notificationIntent.putExtra("Alarmtask",true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast ( this, 0 , pingIntent , PendingIntent. FLAG_UPDATE_CURRENT ) ;
        long futureInMillis = SystemClock. elapsedRealtime () + 1000;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context. ALARM_SERVICE ) ;
        assert alarmManager != null;
        alarmManager.setInexactRepeating(AlarmManager. RTC_WAKEUP , futureInMillis, parseLong, pendingIntent);
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
        this.finish();
    }

    private void check_for_permissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        }
        else
        {
            schedulePingBackground(300000);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        check_for_permissions();
    }
}