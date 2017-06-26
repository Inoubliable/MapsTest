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
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


//TODO(): implement time(from servers) for markers (now set value)
//TODO(): after certian time(10min) old markers shold be delited
//TODO(): Block spamming of free parking spaces
//TODO(): Corelate Circle diameter to Marker precision
//TODO(): Timer event to check for new Parking places

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener{





    /**********************************************GLOBAL VARIABLES****************************************************/

    private GoogleMap mMap;

    LocationManager locationManager;
    String provider;
    double lat;
    double lng;


    ArrayList<ParkingMarker> markersList;//stores ParkingMarkers for updating the oppacity of markers
    ArrayList<Circle> circleList;//stores circles for displaying precision

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
        circleList = new ArrayList<>();


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
        new JsonTask().execute("https://peaceful-taiga-88033.herokuapp.com/users?lat="+String.valueOf(46.054515)+"&lng="+String.valueOf(14.504680)+"&r=500&");


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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Toast.makeText(this, "Loading Free Parking Spaces", Toast.LENGTH_LONG).show();





        //Function onMarker Click (show the precision of the marker placement)
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                //Loop through all markers to see witch marker was clicked
                for(ParkingMarker parkingMarker : markersList){
                    //make action on cliced marker
                    if(parkingMarker.getMarker().getId().compareTo(marker.getId()) == 0){
                        //First check if marker already has active circle if not make one (if circles were added over each other they wouldn't be transperant)
                        if(!parkingMarker.isCircle()) {

                            CircleOptions circleOptions = new CircleOptions().center(new LatLng(parkingMarker.getLat(), parkingMarker.getLng()))
                                    .radius(50)
                                    .strokeColor(Color.GRAY)
                                    .fillColor(0x7F00FF00);

                            Circle tempCircle = mMap.addCircle(circleOptions);

                            circleList.add(tempCircle); //adds to list for later deleting
                            parkingMarker.setCircle(tempCircle); //add Circle to ParkingMarker (to later check "First check if marker already has active circle if not make one" on if)

                            marker.showInfoWindow(); //shows the infoWindows so it can ve clicked to remove the marker/parking spot

                            return false;
                        }else{
                            //if same marker is cliced again remove the circle and infoWinow
                            parkingMarker.getCircle().remove(); //remove it from the map
                            parkingMarker.setCircle(null); //set reference to null

                            return true; // to hide the infoWindow
                        }
                    }
                }



                return false;
            }
        });


        //if unmarked part of map is clicked hide the circle around the markers and the infowindows
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //remove all circles
                for(Circle circle : circleList){
                    circle.remove();
                }
                //remove all infoWindows
                for(ParkingMarker parkingMarker : markersList){
                    parkingMarker.getMarker().hideInfoWindow();
                    parkingMarker.setCircle(null); //sience we removed the circle its reference must be set to null as well( so if marker is clicked again, the circle will be redrawn)
                }
            }
        });

        //On long click on the map, place marker on that place and pass parking spot to server
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                //pass parking spot to server
                new PostSpot(latLng.latitude, latLng.longitude).execute("");
                //add the spot to the map
                addParkingMarker("Mitja Test"+markersList.size(), latLng.latitude, latLng.longitude, 1f);
            }
        });

        //If infoWinow above marker is clicked, delete this maerker/parking spot
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                //cant delete marker mid for loop so i store it in here for deleting later
                ParkingMarker parkingMarkerToRemove = null;
                //Loop through all the marker to see witchs infowindow was clicked
                for(ParkingMarker parkingMarker : markersList){
                    //when marker whos window was clicked is found delete the maerker, circle, parking spot
                    if(parkingMarker.getMarker().getId().compareTo(marker.getId()) == 0){

                        marker.remove();
                        parkingMarker.getCircle().remove();
                        //TODO(): add function to remove free spot from database

                        parkingMarkerToRemove = parkingMarker;//store the marker to delete it from the list later
                    }

                }
                markersList.remove(parkingMarkerToRemove);
            }
        });

        //TODO(): move camera to use location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(46.054515, 14.504680), 15));

    }



    @Override
    public void onLocationChanged(Location location) {



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

                for(int i=0; i<tempArray.length();i+=1){


                    String innerArray = tempArray.getString(i);//ker je Json sestavljen iz arrayey morm dobit usak array posevi kot string

                    Type type = new TypeToken<Map<String, String>>(){}.getType(); //DA lagko pol v Map podam keksn tip je ker item
                    Map<String, String> myMap = gson.fromJson(innerArray, type); //Key-Value map k lagk pol vn uzamem lat, lng, time


                    double tempLat = Double.parseDouble(myMap.get("lat")); // najdem lat in za tem uzamem stevki in jih spremenim v double
                    double tempLng = Double.parseDouble(myMap.get("lng")); // najdem lng in za tem uzamem stevki in jih spremenim v double
                    String tempName = myMap.get("id");

                    addParkingMarker(tempName, tempLat, tempLng, 1f);

                }

                //TODO():Implement user location
                //moves camera to User Location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(46.054515, 14.504680), 15));


            } catch (JSONException | NullPointerException e) {e.printStackTrace();}
        }


    }

    /**************************************************************END******************************************************/





    /****************************************************POST DATA TO SERVER**********************************************************/

    public class PostSpot extends AsyncTask<String, String, String> {

        String latitute, longitute;

        public PostSpot(double latitute, double longitute){


            this.latitute = String.valueOf(latitute);
            this.longitute = String.valueOf(longitute);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {

            String resultToDisplay = "";


            resultToDisplay = addNewParkingSpot(latitute, longitute);

            return resultToDisplay;



        }


        @Override
        protected void onPostExecute(String result) {
            Log.d("Post Request:", result);

            if(result != "Mitja error"){

            }
        }

    }



    //TODO(): get the new ID an return it out
    private String addNewParkingSpot(String latitute, String longitute){

        String result = "Mitja error";

        try {

            URL url = new URL("https://peaceful-taiga-88033.herokuapp.com/login");


            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            String urlParameter = "lat="+latitute+"&lng="+longitute+"&";

            connection.setRequestMethod("POST");

            connection.setDoOutput(true);

            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());

            dStream.writeBytes(urlParameter);
            dStream.flush();
            dStream.close();

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK)
                Log.d("Mitja ERROR", String.valueOf(responseCode));
            else
                Log.d("Mitja WORKS", String.valueOf(responseCode));

            String output = "Request URl "+ url;
            output += System.getProperty("line.seperator")+"Request Parameters "+ urlParameter;
            output += System.getProperty("line.seperator")+"Request Response Code "+ responseCode;

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line = "";
            StringBuilder reponseOutput = new StringBuilder();

            while ((line = br.readLine()) != null){
                reponseOutput.append(line);
            }
            br.close();

            output += System.getProperty("line.seperator")+ reponseOutput.toString();

            result = output;



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }



    /**********************************************************END*******************************************************************/

    //TODO(): add precision
    //Function to add a marker on maps
    private void addParkingMarker(String id, double lat, double lng, float time){

        //First check if this marker already exists on the maps
        for(ParkingMarker marker : markersList){
            //if it does then jump out of a function without placing the marker
            if(id.compareTo(marker.getID()) == 0){
                return;
            }
        }

        //options serve as propreties of marker(position, icon, name...)
        MarkerOptions myMarkerOptions = new MarkerOptions().position(new LatLng(lat, lng))
                .title("Take Parking")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round))
                .anchor(0.5f,0.5f);

        //v Maps dodam nov marker in ga shranim v marker list
        markersList.add( new ParkingMarker( id, lat, lng,100f,10f,mMap.addMarker(myMarkerOptions)));

    }

}






