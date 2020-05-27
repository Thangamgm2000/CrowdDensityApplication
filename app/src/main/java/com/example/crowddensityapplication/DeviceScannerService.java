package com.example.crowddensityapplication;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceScannerService extends Service implements LocationListener {

    LocationManager locationManager;

    ArrayList<String> arrayList;
    String latitude = "na";
    String longitude="na";
    String CHANNEL_ID="Bluetooth Scanning";
    int Notification_id=1;

    @Override
    public void onCreate() {
        createNotificationChannel();
        Notification notification = getNotification();
        startForeground(Notification_id,notification);
        super.onCreate();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Scanning for bluetooth devices";
            String description = "Periodical scans for bluetooth devices around you.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager =  getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification getNotification()
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Scanning for bluetooth devices")
                .setContentText("Periodical scans for bluetooth devices around you.")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true);
        return builder.build();
    }

    public DeviceScannerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = getNotification();
        startForeground(Notification_id,notification);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return super.onStartCommand(intent, flags, startId);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter!=null)
        {
            bluetoothAdapter.startDiscovery();
            arrayList=new ArrayList<>();
            arrayList.clear();
            arrayList.add("123");

            final ScanCallback mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if(result!=null)
                    {
                        if (result.getDevice()!=null)
                        {
                            if(result.getDevice().getName()!=null) {
                                arrayList.add(result.getDevice().getName());
                                Log.d("a_device_found", result.getDevice().getName());
                            }
                        }
                    }

                }
            };
            final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.startScan(mScanCallback);
            //sendDeviceList();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothLeScanner.flushPendingScanResults(mScanCallback);
                    bluetoothLeScanner.stopScan(mScanCallback);
                    bluetoothAdapter.cancelDiscovery();
                    sendDeviceList();
                }
            },15000);
            /*new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    //Do something after 10000ms
                    bluetoothLeScanner.flushPendingScanResults(mScanCallback);
                    bluetoothLeScanner.stopScan(mScanCallback);
                    bluetoothAdapter.cancelDiscovery();
                    sendDeviceList();

                }
            }, 15000);*/

        }

        return super.onStartCommand(intent, flags, startId);

    }

    private void sendDeviceList() {
        final JSONArray deviceListData= new JSONArray(arrayList);
        String url = getString(R.string.server)+"/pingDeviceList";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        stopSelf();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("devicesList",deviceListData.toString());
                params.put("latitude",latitude);
                params.put("longitude",longitude);
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                params.put("timestamp",ts);
                Log.d("deviceList",deviceListData.toString());

                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(postRequest);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null)
        {
            latitude = location.getLatitude()+"";
            longitude = location.getLongitude()+"";
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
