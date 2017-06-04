package com.janzelj.tim.mapstest;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    LocationManager locationManager;
    String provider;
    double lat;
    double lng;

    String txtJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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

        //To trenutno dobi json iz linka in v Toast.show() izpiše podatke ki jih je vrnu server
        new JsonTask().execute("https://peaceful-taiga-88033.herokuapp.com/users");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        drawMyMarker();
    }

    private void drawMyMarker() {
        //Toast.makeText(this, "Lat: " + lat + ", Lng: " + lng, Toast.LENGTH_LONG).show();

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


    //Class k iz podanega linka dobi JSON file iz serverja in v String zapiše podatke ki jih vrne server
    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();


        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            txtJson = result;

            //Zbriše vse truntne markerje
            mMap.clear();

            try {
                JSONArray tempArray = new JSONArray(result);//Paharsa string Json v Json array

                for(int i=0; i<tempArray.length();i+=1){


                    String innerArray = tempArray.getString(i);//ker je Json sestavljen iz arrayey morm dobit usak array posevi kot string

                    double tempLat = Double.parseDouble(innerArray.substring(innerArray.indexOf("lat")+5,innerArray.indexOf("lat")+7)); // najdem lat in za tem uzamem stevki in jih spremenim v double
                    double tempLng = Double.parseDouble(innerArray.substring(innerArray.indexOf("lng")+5,innerArray.indexOf("lng")+7)); // najdem lng in za tem uzamem stevki in jih spremenim v double



                    //v Maps dodam nov marker
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(tempLat, tempLng))
                            .title("Free Space")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    //Kamero pomaknem na ta marker
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(tempLat, tempLng), 3));

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }
}
