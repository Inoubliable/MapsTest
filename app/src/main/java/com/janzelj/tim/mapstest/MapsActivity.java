package com.janzelj.tim.mapstest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    LocationManager locationManager;
    String provider;
    double lat;
    double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toast.makeText(this, "Mitja Change", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        provider = locationManager.getBestProvider(new Criteria(), false);

        try {
            Location location = locationManager.getLastKnownLocation(provider);

            if(location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();
            }
        } catch (SecurityException e) {

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        drawMyMarker();
    }

    private void drawMyMarker() {
        Toast.makeText(this, "Lat: " + lat + ", Lng: " + lng, Toast.LENGTH_LONG).show();

        mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(lat, lng),
                        new LatLng(lat + 0.001, lng + 0.001),
                        new LatLng(lat + 0.0015, lng),
                        new LatLng(lat + 0.001, lng - 0.001),
                        new LatLng(lat, lng)
                )
                .strokeWidth(2)
                .fillColor(Color.GREEN));

        mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(lat + 0.00045, lng))
                .add(new LatLng(lat + 0.0011, lng))
                .add(new LatLng(lat + 0.0008, lng - 0.0003))
                .width(9)
        );

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));
    }

    @Override
    public void onLocationChanged(Location location) {

        mMap.clear();

        lat = location.getLatitude();
        lng = location.getLongitude();

        drawMyMarker();

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

    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        } catch (SecurityException e) {

        }
    }
}
