package com.example.crowddensityapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class BluetoothCommunicator extends AppCompatActivity {
    ConnectionLifecycleCallback connectionLifecycleCallback;
    PayloadCallback payloadCallback;
    ArrayList<String> devicelist;
    ArrayList<String> endid;
    String toaddname,nick;
    TextView msgrec;
    SharedPreferences sh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_communicator);
        msgrec= findViewById(R.id.msgrec);
        devicelist=new ArrayList<>();
        endid = new ArrayList<>();
        sh = getSharedPreferences("active_endpoint_list",MODE_PRIVATE);
        connectionLifecycleCallback=new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo connectionInfo) {
                final String id=s;
                Toast.makeText(getApplicationContext(),"Connection Initiated with "+connectionInfo.getEndpointName(),Toast.LENGTH_SHORT).show();
                Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(id,payloadCallback);
                toaddname=connectionInfo.getEndpointName();
            }





            @Override
            public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
//                Toast.makeText(MainActivity.this,"Connection established/rejected "+s,Toast.LENGTH_SHORT).show();
//                connectionResolution.getStatus().getStatusMessage();
                Status status=connectionResolution.getStatus();
                byte[] message={1};
                if(status.getStatusCode()== ConnectionsStatusCodes.STATUS_OK)
                {
                    Toast.makeText(getApplicationContext(),"Connection established",Toast.LENGTH_SHORT).show();
//                        devicelist.add(toaddname);
//                        endid.add(s);
                    sendmessage("hello friend!");

                }
                else if(status.getStatusCode()==ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED)
                    Toast.makeText(getApplicationContext(),"Connection was rejected",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(),status.getStatusMessage(),Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onDisconnected(@NonNull String s) {
                Toast.makeText(getApplicationContext(),"Connection disconnected with "+s,Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sh.edit();
                editor.remove(s);
                editor.apply();

            }
        };
        payloadCallback=new PayloadCallback() {
            @Override
            public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                byte[] receivedBytes = payload.asBytes();
                String string="ini";
                try {
                    string = new String(receivedBytes, "UTF-8");

                    // qqToast.makeText(MainActivity.this,string,Toast.LENGTH_SHORT).show();

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Date currentTime = Calendar.getInstance().getTime();
                //broadcastMessage(string);
                msgrec.setText(string);
            }

            @Override
            public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

            }
        };
        startAdvertising();
        startDiscovery();
    }

    public void startAdvertising() {
        nick="";
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
        Nearby.getConnectionsClient(getApplicationContext()).startAdvertising(nick,"com.example.crowddensityapplication",connectionLifecycleCallback,new AdvertisingOptions(Strategy.P2P_CLUSTER))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"On Success",Toast.LENGTH_SHORT).show();
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"On Failure",Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(
                        String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                    SharedPreferences.Editor editor = sh.edit();
                    editor.putString(endpointId,"a");
                    editor.apply();
                    establishconnection(endpointId);
                    Toast.makeText(getApplicationContext(),"onEndpointFound",Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // A previously discovered endpoint has gone away.
                    Toast.makeText(getApplicationContext(),"onEndpointLost",Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = sh.edit();
                    editor.remove(endpointId);
                    editor.apply();
                }
            };

    public void startDiscovery() {
        Nearby.getConnectionsClient(getApplicationContext()).startDiscovery(
                "com.example.crowddensityapplication",
                mEndpointDiscoveryCallback,new DiscoveryOptions(Strategy.P2P_CLUSTER))
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                // We're discovering!
                                Toast.makeText(getApplicationContext(),"Discovery Started!",Toast.LENGTH_SHORT).show();
                                schedulePingBackground(15000);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // We were unable to start discovering.
                                Toast.makeText(getApplicationContext(),"Discovery failed!",Toast.LENGTH_SHORT).show();
                            }
                        });

    }

    public void establishconnection(String endpointId)
    {
        //int pos=devicelist.indexOf(endpointName);
        //String endpointId=endid.get(pos);
        Nearby.getConnectionsClient(getApplicationContext()).requestConnection(
                nick,
                endpointId,
                connectionLifecycleCallback)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                // We successfully requested a connection. Now both sides
                                // must accept before the connection is established.

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Nearby Connections failed to request the connection.
                            }
                        });

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
            Nearby.getConnectionsClient(getApplicationContext()).sendPayload(endpointId,bytesPayload);
        }

    }

    private void schedulePingBackground(long parseLong) {
        Intent notificationIntent = new Intent( this, BackgroundPingSender. class ) ;
        //notificationIntent.putExtra("Alarmtask",true);
        PendingIntent pendingIntent = PendingIntent. getBroadcast ( this, 0 , notificationIntent , PendingIntent. FLAG_UPDATE_CURRENT ) ;
        long futureInMillis = SystemClock. elapsedRealtime () + 1000;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context. ALARM_SERVICE ) ;
        assert alarmManager != null;
        alarmManager.setInexactRepeating(AlarmManager. ELAPSED_REALTIME_WAKEUP , futureInMillis, parseLong, pendingIntent);
    }

    public void stopPingBackground(View v)
    {
        Intent notificationIntent = new Intent( this, BackgroundPingSender. class ) ;
        //notificationIntent.putExtra("Alarmtask",true);
        PendingIntent pendingIntent = PendingIntent. getBroadcast ( this, 0 , notificationIntent , PendingIntent. FLAG_UPDATE_CURRENT ) ;
        long futureInMillis = SystemClock. elapsedRealtime () + 1000;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context. ALARM_SERVICE ) ;
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);
    }

}
