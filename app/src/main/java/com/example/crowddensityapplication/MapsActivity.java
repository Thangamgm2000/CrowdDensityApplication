package com.example.crowddensityapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSION_EXTERNAL=1;
    String alpha = "#55";
    String red = "FF0000";
    String orange = "FFA500";
    String yellow = "FFFF00";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        org.osmdroid.config.IConfigurationProvider osmConf = org.osmdroid.config.Configuration.getInstance();
        File basePath = new File(getCacheDir().getAbsolutePath(), "osmdroid");
        osmConf.setOsmdroidBasePath(basePath);
        Configuration.getInstance().setUserAgentValue(getApplicationContext().getPackageName());
        File tileCache = new File(osmConf.getOsmdroidBasePath().getAbsolutePath(), "tile");
        osmConf.setOsmdroidTileCache(tileCache);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION_EXTERNAL);
        }
        setContentView(R.layout.activity_maps);
        MapView map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        GeoPoint startPoint = new GeoPoint(13.041297, 80.195952);
        IMapController mapController = map.getController();
        mapController.setZoom(19);
        mapController.setCenter(startPoint);
//
        List<GeoPoint> geoPoints = new ArrayList<>();
        //add your points here
        geoPoints.add(new GeoPoint(13.041297, 80.195952));
        geoPoints.add(new GeoPoint(13.043262, 80.195158));
        geoPoints.add(new GeoPoint(13.042367, 80.197841));
        geoPoints.add(new GeoPoint(13.040896, 80.198368));
        Polygon polygon = new Polygon();    //see note below
        polygon.setPoints(geoPoints);
        //geoPoints.add(geoPoints.get(0));    //forces the loop to close(connect last point to first point)
        polygon.getFillPaint().setColor(Color.parseColor(alpha+red)); //set fill color
        //polygon.getOutlinePaint().setColor(polygon.getFillPaint().getColor());
        //polygon.getOutlinePaint().setStrokeWidth(0f);

        //polygon.setStrokeColor(Color.RED);
        polygon.setTitle("A sample polygon");


        map.getOverlayManager().add(polygon);
        map.invalidate();
    }
}
