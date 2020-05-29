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
    String orange = "C67700";
    String green = "21C600";
    MapView map;
    public void initialize()
    {
        org.osmdroid.config.IConfigurationProvider osmConf = org.osmdroid.config.Configuration.getInstance();
        File basePath = new File(getCacheDir().getAbsolutePath(), "osmdroid");
        osmConf.setOsmdroidBasePath(basePath);
        Configuration.getInstance().setUserAgentValue(getApplicationContext().getPackageName());
        File tileCache = new File(osmConf.getOsmdroidBasePath().getAbsolutePath(), "tile");
        osmConf.setOsmdroidTileCache(tileCache);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION_EXTERNAL);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
        setContentView(R.layout.activity_maps);


        map= findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        GeoPoint startPoint = new GeoPoint(13.040896, 80.198368);
        IMapController mapController = map.getController();
        mapController.setZoom(15);
        mapController.setCenter(startPoint);


        drawZoneRed(new GeoPoint(13.040896, 80.188368),
                new GeoPoint(13.040896, 80.198368),
                new GeoPoint(13.050896, 80.198368),
                new GeoPoint(13.050896, 80.188368));

        drawZoneGreen(new GeoPoint(13.070896, 80.168368),
                new GeoPoint(13.070896, 80.178368),
                new GeoPoint(13.080896, 80.178368),
                new GeoPoint(13.080896, 80.168368));

        drawZoneOrange(new GeoPoint(13.020896, 80.158368),
                new GeoPoint(13.020896, 80.148368),
                new GeoPoint(13.030896, 80.148368),
                new GeoPoint(13.030896, 80.158368));



        map.invalidate();
    }
    public void drawZoneRed(GeoPoint g1, GeoPoint g2, GeoPoint g3, GeoPoint g4)
    {
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(g1);
        geoPoints.add(g2);
        geoPoints.add(g3);
        geoPoints.add(g4);
        Polygon polygon = new Polygon();
        polygon.setPoints(geoPoints);
        polygon.getFillPaint().setColor(Color.parseColor(alpha+red));
        polygon.getOutlinePaint().setColor(polygon.getFillPaint().getColor());
        polygon.getOutlinePaint().setStrokeWidth(0f);
        //polygon.setStrokeColor(Color.RED);
        map.getOverlayManager().add(polygon);
    }
    public void drawZoneGreen(GeoPoint g1, GeoPoint g2, GeoPoint g3, GeoPoint g4)
    {
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(g1);
        geoPoints.add(g2);
        geoPoints.add(g3);
        geoPoints.add(g4);
        Polygon polygon = new Polygon();
        polygon.setPoints(geoPoints);
        polygon.getFillPaint().setColor(Color.parseColor(alpha+green));
        polygon.getOutlinePaint().setColor(polygon.getFillPaint().getColor());
        polygon.getOutlinePaint().setStrokeWidth(0f);
        //polygon.setStrokeColor(Color.RED);
        map.getOverlayManager().add(polygon);
    }
    public void drawZoneOrange(GeoPoint g1, GeoPoint g2, GeoPoint g3, GeoPoint g4)
    {
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(g1);
        geoPoints.add(g2);
        geoPoints.add(g3);
        geoPoints.add(g4);
        Polygon polygon = new Polygon();
        polygon.setPoints(geoPoints);
        polygon.getFillPaint().setColor(Color.parseColor(alpha+orange));
        polygon.getOutlinePaint().setColor(polygon.getFillPaint().getColor());
        polygon.getOutlinePaint().setStrokeWidth(0f);
        //polygon.setStrokeColor(Color.RED);
        map.getOverlayManager().add(polygon);
    }
}
