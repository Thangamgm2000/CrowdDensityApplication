package com.example.crowddensityapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSION_EXTERNAL=1;
    String alpha = "#55";
    String red = "FF0000";
    String orange = "C67700";
    String green = "21C600";
    public MapView map;
    public Projection p;
    MapNetworking mapNetworking;
    public void initialize()
    {
        org.osmdroid.config.IConfigurationProvider osmConf = org.osmdroid.config.Configuration.getInstance();
        File basePath = new File(getCacheDir().getAbsolutePath(), "osmdroid");
        osmConf.setOsmdroidBasePath(basePath);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        File tileCache = new File(osmConf.getOsmdroidBasePath().getAbsolutePath(), "tile");
        osmConf.setOsmdroidTileCache(tileCache);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION_EXTERNAL);
        }
        //getSupportActionBar().hide();
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
        map.setTileSource(
                new XYTileSource("HttpMapnik",
                        0, 19, 256, ".png", new String[] {
                        "http://a.tile.openstreetmap.org/",
                        "http://b.tile.openstreetmap.org/",
                        "http://c.tile.openstreetmap.org/" },
                        "© OpenStreetMap contributors")
        );

       GeoPoint startPoint = new GeoPoint(11.017553, 76.969353);
        IMapController mapController = map.getController();
        mapController.setZoom(18);
        mapController.setCenter(startPoint);



      mapNetworking = new MapNetworking(this,map);
       //mapNetworking.drawCurrentCenter();
        mapNetworking.getZoneDensity(11.017553,76.969353);
        //mapNetworking.getAreaZone();
        map.invalidate();

//       map.getMapCenter().getLatitude();
//       map.getMapCenter().getLongitude();
//       map.addMapListener(new MapListener() {
//           @Override
//           public boolean onScroll(ScrollEvent event) {
//               return false;
//           }
//
//           @Override
//           public boolean onZoom(ZoomEvent event) {
//               return false;
//           }
//       });

       //MapNetworking mapNetworking = new MapNetworking(this,map);
       //mapNetworking.getZoneDensity(11.017553,76.969353);
        map.invalidate();

        map.addMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                if(event!=null){
                    int max_x=map.getWidth();
                    int max_y=map.getHeight();

                    Projection projection = map.getProjection();
                    GeoPoint geoPointTopLeft = (GeoPoint) projection.fromPixels(max_x/2, max_y/2);
                    Point topLeftPoint = new Point();
                    // Get the top left Point (includes osmdroid offsets)
                    projection.toPixels(geoPointTopLeft, topLeftPoint);
                    // get the GeoPoint of any point on screen
                    GeoPoint iGeoPoint = (GeoPoint) projection.fromPixels(max_x/2, max_y/2);
                    //Toast.makeText(MapsActivity.this,String.valueOf(iGeoPoint.getLatitude())+","+String.valueOf(iGeoPoint.getLongitude()),Toast.LENGTH_LONG).show();

                    //To be uncommented for street level density
                    mapNetworking.getZoneDensity(iGeoPoint.getLatitude(),iGeoPoint.getLongitude());

                }
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        }));
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
