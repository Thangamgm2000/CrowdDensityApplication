package com.example.crowddensityapplication;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MapNetworking {
    private Context hostContext;
    private String alpha = "#55";
    String red = "FF0000";
    private String orange = "C67700";
    String green = "21C600";
    private MapView map;
    double base_lat = 11.0;
    double base_long = 76.9;
    ArrayList<BoundaryBox> boundaryBoxes;
    MapNetworking(Context context, MapView map)
    {
        this.hostContext = context;
        this.map = map;
        boundaryBoxes = new ArrayList<BoundaryBox>();
    }

    boolean isCovered(double lat,double lon,int n)
    {
        for(int i=0;i<n;i++)
        {
            BoundaryBox curr = boundaryBoxes.get(i);
            if(lat>=curr.latMin && lat<=curr.latMax && lon>=curr.longMin && lon<=curr.longMax)
            {
                return true;
            }
        }
        return false;
    }

    void getZoneDensity(final double latitude, final double longitude)
    {
        if(isCovered(latitude,longitude,boundaryBoxes.size()))
        {
            return;
        }
        boundaryBoxes.add(new BoundaryBox(latitude-0.005,longitude-0.005,latitude+0.005,longitude+0.005));
        String URLstring=  hostContext.getString(R.string.server)+"/getZoneCount";

        //showSimpleProgressDialog(this, "Loading...","Fetching the contents",false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLstring,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d("strrrrr", ">>" + response);
                        try {

                            JSONObject obj = new JSONObject(response);
                            JSONArray zoneArray  = obj.getJSONArray("zones_data");
                            for(int i =0;i<zoneArray.length();i++)
                            {
                                JSONObject dataobj = zoneArray.getJSONObject(i);
                                String zone = dataobj.getString("zone");
                                String count = dataobj.getString("count");
                                if(!count.equals("nan"))
                                {
                                    double density = Double.parseDouble(count);

                                    String lat_val = zone.split("lat")[1].split("long")[0];
                                    String long_val = zone.split("lat")[1].split("long")[1];
                                    double lat1 = base_lat + Integer.parseInt(lat_val)*0.001;
                                    double long1 = base_long + Integer.parseInt(long_val)*0.001;

                                    if(density<10)
                                    {
                                        drawZone(new GeoPoint(lat1,long1),new GeoPoint(lat1,long1+0.001),new GeoPoint(lat1+0.001,long1+0.001),new GeoPoint(lat1+0.001,long1),alpha+green);
                                    }
                                    else if(density<20)
                                    {
                                        drawZone(new GeoPoint(lat1,long1),new GeoPoint(lat1,long1+0.001),new GeoPoint(lat1+0.001,long1+0.001),new GeoPoint(lat1+0.001,long1),alpha+orange);
                                    }
                                    else
                                    {
                                        drawZone(new GeoPoint(lat1,long1),new GeoPoint(lat1,long1+0.001),new GeoPoint(lat1+0.001,long1+0.001),new GeoPoint(lat1+0.001,long1),alpha+red);
                                    }
                                }

                            }



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //displaying the error in toast if occurrs
                        Log.d("Error.Response", error.toString());
                        Toast.makeText(hostContext, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("latitude", ""+latitude);
                params.put("longitude", ""+longitude);

                return params;
            }
        };
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 500;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        // request queue
        RequestQueue requestQueue = Volley.newRequestQueue(hostContext);
        requestQueue.add(stringRequest);
    }

    public void drawZone(GeoPoint g1, GeoPoint g2, GeoPoint g3, GeoPoint g4, String color)
    {
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(g1);
        geoPoints.add(g2);
        geoPoints.add(g3);
        geoPoints.add(g4);
        Polygon polygon = new Polygon();
        polygon.setPoints(geoPoints);
        polygon.getFillPaint().setColor(Color.parseColor(color));
        polygon.getOutlinePaint().setColor(polygon.getFillPaint().getColor());
        polygon.getOutlinePaint().setStrokeWidth(0f);
        //polygon.setStrokeColor(Color.RED);
        map.getOverlayManager().add(polygon);
        map.invalidate();
    }

    void getAreaZone()
    {
        String URLstring=  hostContext.getString(R.string.server)+"/getAreaZone";

        //showSimpleProgressDialog(this, "Loading...","Fetching the contents",false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLstring,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d("strrrrr", ">>" + response);
                        try {

                            JSONObject obj = new JSONObject(response);
                            JSONArray zoneArray  = obj.getJSONArray("zones_data");
                            for(int i =0;i<zoneArray.length();i++)
                            {
                                JSONObject dataobj = zoneArray.getJSONObject(i);
                                String zone = dataobj.getString("zone");
                                String count = dataobj.getString("count");
                                if(!count.equals("nan"))
                                {
                                    double density = Double.parseDouble(count);

                                    String lat_val = zone.split("lat")[1].split("long")[0];
                                    String long_val = zone.split("lat")[1].split("long")[1];
                                    double lat1 = base_lat + Integer.parseInt(lat_val)*0.01;
                                    double long1 = base_long + Integer.parseInt(long_val)*0.01;
                                    if(isCovered(lat1,long1,boundaryBoxes.size()-1))
                                    {
                                        continue;
                                    }
                                    if(density<10)
                                    {
                                        drawZone(new GeoPoint(lat1,long1),new GeoPoint(lat1,long1+0.01),new GeoPoint(lat1+0.01,long1+0.01),new GeoPoint(lat1+0.01,long1),alpha+green);
                                    }
                                    else if(density<20)
                                    {
                                        drawZone(new GeoPoint(lat1,long1),new GeoPoint(lat1,long1+0.01),new GeoPoint(lat1+0.01,long1+0.01),new GeoPoint(lat1+0.01,long1),alpha+orange);
                                    }
                                    else
                                    {
                                        drawZone(new GeoPoint(lat1,long1),new GeoPoint(lat1,long1+0.01),new GeoPoint(lat1+0.01,long1+0.01),new GeoPoint(lat1+0.01,long1),alpha+red);
                                    }
                                }

                            }



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //displaying the error in toast if occurrs
                        Log.d("Error.Response", error.toString());
                        Toast.makeText(hostContext, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
        };
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 500;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        // request queue
        RequestQueue requestQueue = Volley.newRequestQueue(hostContext);
        requestQueue.add(stringRequest);
    }

}

class BoundaryBox
{
    double latMin,latMax,longMin,longMax;
    public BoundaryBox(double latMin,double longMin,double latMax, double longMax)
    {
        this.latMin=latMin;
        this.latMax=latMax;
        this.longMax = longMax;
        this.longMin = longMin;
    }
}
