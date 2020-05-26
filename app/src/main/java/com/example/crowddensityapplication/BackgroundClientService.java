package com.example.crowddensityapplication;

import android.Manifest;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class BackgroundClientService extends IntentService implements LocationListener {

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_PING = "com.example.crowddensityapplication.action.ping";
    private static final String ACTION_RESET_NAME = "com.example.crowddensityapplication.action.reset_name";
    int REQUEST_ENABLE_BT = 1;
    LocationManager locationManager;

    ArrayList<String> arrayList;
    String latitude = "na";
    String longitude="na";


    public BackgroundClientService() {
        super("BacgroundClientService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    public static void startActionPing(Context context) {
        Intent intent = new Intent(context, BackgroundClientService.class);
        intent.setAction(ACTION_PING);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    public static void startActionResetName(Context context) {
        Intent intent = new Intent(context, BackgroundClientService.class);
        intent.setAction(ACTION_RESET_NAME);
        context.startService(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PING.equals(action)) {
                handleActionPing();
            } else if (ACTION_RESET_NAME.equals(action)) {
                handleActionResetName();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void handleActionPing() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
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
                    arrayList.add(result.getDevice().getName());
                    Log.d("a_device_found",result.getDevice().getName());
                }
            };
            final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.startScan(mScanCallback);
            //sendDeviceList();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    //Do something after 10000ms
                    bluetoothLeScanner.flushPendingScanResults(mScanCallback);
                    bluetoothLeScanner.stopScan(mScanCallback);
                    bluetoothAdapter.cancelDiscovery();
                    sendDeviceList();

                }
            }, 15000);

        }
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

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionResetName() {
        String devicename="temp";
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
