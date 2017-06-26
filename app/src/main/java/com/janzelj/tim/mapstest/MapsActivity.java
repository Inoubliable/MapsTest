package com.janzelj.tim.mapstest;

import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


//TODO(): implement time(from servers) for markers (now set value)
//TODO(): after certian time(10min) old markers shold be delited

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {





    /**********************************************GLOBAL VARIABLES****************************************************/

    private GoogleMap mMap;

    LocationManager locationManager;
    String provider;
    double lat;
    double lng;


    ArrayList<ParkingMarker> markersList;//stores ParkingMarkers for updating the oppacity of markers

    Handler UI_HANDLER;//For thread events (setting a clocl trigerted fucntion for updating the oppacity of markers)

    /**************************************************END**************************************************************/





    /*************************************ANDROID ACTIVITY FUNCTIONS(onCreade, onPause, onResume)***************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        markersList = new ArrayList<>(); //sotres ParkingMarkers for updating the oppacity of markers


        //Code to get user location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        try {
            Location location = locationManager.getLastKnownLocation(provider);
            if(location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();
            }
        } catch (SecurityException e) {}


        //Gets Json from server and adds markers on maps after
        new JsonTask().execute("https://peaceful-taiga-88033.herokuapp.com/users");


        //For marker animation
        UI_HANDLER = new Handler(); //to be handle thread events
        UI_HANDLER.postDelayed(UI_UPDTAE_RUNNABLE, 1000);//This is like a clock triger event that runs on a UI(main) thread



    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        } catch (SecurityException e) {

        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);
    }

    /**************************************************END*************************************************************/










    /*************************************GOOGLE MAPS FUNCTIONS(onMapReady, onLocationChanged)***************************************/
    //TODO(): Remove old code from Tim

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        drawMyMarker();
    }


    //This is old code from Tim using ustom shapes as marker
    private void drawMyMarker() {
        Toast.makeText(this, "Loading Free Parking Spaces", Toast.LENGTH_LONG).show();

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


    /*********************************************************END***********************************************************/









    /***************************************************MARKER ANIMATION*********************************************/

    //This is like a clock triger event that runs on a UI(main) thread
    //This is needed to update the oppacitiy of the markers, but they cannot be accest from another thread(no animation loop therad possible)
    //It gets called every second
    Runnable UI_UPDTAE_RUNNABLE = new Runnable() {

        @Override
        public void run() {

            for(ParkingMarker mark : markersList){

                mark.updateAlpha();

            }

            UI_HANDLER.postDelayed(UI_UPDTAE_RUNNABLE, 1000);
        }
    };

    /*********************************************************END***************************************************/








    /*******************************************GET DATA FROM SERVER CODE + ADD MARKERS FOR RECIVED LOCATIONS*********************************************************/

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

            Gson gson = new Gson();//a external libery object for reading HTTP JSON responses

            //Zbriše vse truntne markerje
            mMap.clear();

            try {
                JSONArray tempArray = new JSONArray(result);//Paharsa string Json v Json array
                Log.d("JSON arraqy : ",tempArray.toString());

                for(int i=0; i<tempArray.length();i+=1){

                    Log.d(i+"",tempArray.getString(i));

                    String innerArray = tempArray.getString(i);//ker je Json sestavljen iz arrayey morm dobit usak array posevi kot string

                    Type type = new TypeToken<Map<String, String>>(){}.getType(); //DA lagko pol v Map podam keksn tip je ker item
                    Map<String, String> myMap = gson.fromJson(innerArray, type); //Key-Value map k lagk pol vn uzamem lat, lng, time

                    Log.d("Map"+i,myMap.toString());

                    double tempLat = Double.parseDouble(myMap.get("lat")); // najdem lat in za tem uzamem stevki in jih spremenim v double
                    double tempLng = Double.parseDouble(myMap.get("lng")); // najdem lng in za tem uzamem stevki in jih spremenim v double
                    String tempName = myMap.get("id");

                    //options serve as propreties of marker(position, icon, name...)
                    MarkerOptions myMarkerOptions = new MarkerOptions().position(new LatLng(tempLat, tempLng))
                                                        .title(tempName)
                                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round))
                                                        .anchor(0.5f,0.5f);

                    //v Maps dodam nov marker in ga shranim v marker list
                    markersList.add( new ParkingMarker( tempName, tempLat, tempLng,100f,10f,mMap.addMarker(myMarkerOptions)));

                }






                //TODO(DELETE): just a test
                //added 1000 markers to see preformance
                MarkerOptions myMarkerOptions = new MarkerOptions().position(new LatLng(1, 1))
                        .title("")
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round))
                        .anchor(0.5f,0.5f);

                for(int k=0;k<1000;k++){
                    myMarkerOptions.position(new LatLng(1+k*0.1, 3f));
                    markersList.add( new ParkingMarker( "", 1+k*0.1, 3f,100f,10f,mMap.addMarker(myMarkerOptions)));
                }






                //TODO():Implement user location
                //moves camera to User Location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(1, 1), 3));


            } catch (JSONException e) {e.printStackTrace();}
        }


    }

    /**************************************************************END******************************************************/




}






