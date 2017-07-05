package com.janzelj.tim.mapstest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;


//TODO(): implement time(from servers) for markers (now set value)
//TODO(): after certian time(10min) old markers shold be delited
//TODO(): Block spamming of free parking spaces
//TODO(): Corelate Circle diameter to Marker precision
//TODO(): Timer event to check for new Parking places

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener{





    /**********************************************GLOBAL VARIABLES****************************************************/

    Animation slideInAnim1;
    Animation slideInAnim2;
    Animation slideInAnim3;
    Animation slideInAnim4;
    Animation slideOutAnim;
    Animation gearAnimOut;
    Animation gearAnimIn;

    LinearLayout optionsMenu;
    Button openOptBtn;
    Button saveOptBtn;

    RelativeLayout optionsTitle;
    LinearLayout optionsDisplay;
    LinearLayout optionsMarker;

    Button claimParkBtn;

    CheckBox userCheck;
    CheckBox parkingCheck;
    SeekBar distanceSeek;
    SeekBar ageSeek;



    private GoogleMap googleMap;

    LocationManager locationManager;
    String provider;
    double globLATITUTE;
    double globLONGITUTE;
    float MAX_RADIUS;
    boolean SHOW_USER;
    boolean SHOW_PARKING;
    float MAX_AGE;


    ArrayList<UserMarker> userMarkersList;//stores ParkingMarkers for updating the oppacity of markers
    ArrayList<Circle> markersWithCircles;//stores circles for displaying precision

    ArrayList<ParkingHouseMarker> parkingMarkersList;

    Handler UPDATE_MARKERS_COLOR;//For thread events (setting a clocl trigerted fucntion for updating the oppacity of markers)
    Handler CHECK_FOR_SERVER_UPDATES;



    UserMarkerIconMaker userMarkerIconMaker;
    ParkerMarkerIconMaker parkerMarkerIconMaker;


    Marker thankYouMarker;

    Marker markerInFocus;




    /**************************************************END**************************************************************/





    /*************************************ANDROID ACTIVITY FUNCTIONS(onCreade, onPause, onResume)***************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // load the animation
        slideInAnim1 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in1);
        slideInAnim2 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in2);
        slideInAnim3 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in3);
        slideInAnim4 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in4);
        slideOutAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out);
        gearAnimOut = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.gear_out);
        gearAnimIn = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.gear_in);

        optionsMenu = (LinearLayout) findViewById(R.id.optionsMenu);
        openOptBtn = (Button) findViewById(R.id.openOptions);

        saveOptBtn = (Button) findViewById(R.id.saveOptBtn);

        optionsTitle = (RelativeLayout) findViewById(R.id.optionsTitle);
        optionsDisplay = (LinearLayout) findViewById(R.id.optionsDisplay);
        optionsMarker = (LinearLayout) findViewById(R.id.optionsMarker);

        claimParkBtn = (Button) findViewById(R.id.claimParkBtn);

        userCheck = (CheckBox) findViewById(R.id.userCheck);
        parkingCheck = (CheckBox) findViewById(R.id.parkingCheck);
        distanceSeek = (SeekBar) findViewById(R.id.distanceSeek);
        ageSeek = (SeekBar) findViewById(R.id.ageSeek);

        //TODO(): load from shared and set options;



        updateGlobalVariables();


        userMarkersList = new ArrayList<>(); //sotres ParkingMarkers for updating the oppacity of markers
        markersWithCircles = new ArrayList<>();

        parkingMarkersList = new ArrayList<>();


        userMarkerIconMaker = new UserMarkerIconMaker(100,40);

        parkerMarkerIconMaker = new ParkerMarkerIconMaker(150,60,this);


        //Code to get user location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        try {
            Location location = locationManager.getLastKnownLocation(provider);
            if(location != null) {
                globLATITUTE = location.getLatitude();
                globLONGITUTE = location.getLongitude();
            }
        } catch (SecurityException e) {}







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
        this.googleMap = googleMap;

        //TODO(): Better loading screan, than just Toast
        Toast.makeText(this, "Loading Free Parking Spaces", Toast.LENGTH_LONG).show();

        //TODO(IMPORTNAT): Canot add objects to Map here beacuse it is lockef by a seperate thread JsonTASK()

        //TODO(): unccoment GET_USER
        //Gets Json from server and adds markers on maps after(UserMarker and than ParkingHouses)
        if(SHOW_USER) {
            new GET_USER_SUBBMITED_PARKINGS(globLATITUTE, globLONGITUTE, MAX_RADIUS).execute();
        }
        if(SHOW_PARKING) {
            new GET_PARKING_HOUSES().execute();
        }


        final MarkerOptions thankYouOpt = new MarkerOptions().visible(false).position(new LatLng(0,0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.thank_you_icon));
        thankYouMarker = googleMap.addMarker(thankYouOpt);

        generateTestingMarkers();

        //Function onMarker Click (show the precision of the marker placement)
        this.googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {



                //Loop through all markers to see witch marker was clicked
                for(UserMarker userMarker : userMarkersList){
                    //make action on cliced marker
                    if(userMarker.getMarker().getId().compareTo(marker.getId()) == 0){

                        markerInFocus = marker;
                        claimParkBtn.setVisibility(View.VISIBLE);

                        //First check if marker already has active circle if not make one (if circles were added over each other they wouldn't be transperant)
                        if(!userMarker.isCircle()) {

                            CircleOptions circleOptions = new CircleOptions().center(userMarker.getLocation())
                                    .radius(50)
                                    .strokeColor(Color.GRAY)
                                    .fillColor(0x7F00FF00);

                            Circle tempCircle = MapsActivity.this.googleMap.addCircle(circleOptions);

                            markersWithCircles.add(tempCircle); //adds to list for later deleting
                            userMarker.setCircle(tempCircle); //add Circle to ParkingMarker (to later check "First check if marker already has active circle if not make one" on if)

                            marker.showInfoWindow(); //shows the infoWindows so it can ve clicked to remove the marker/parking spot

                            userMarker.animatePopIn();

                            return false;
                        }else{
                            //if same marker is cliced again remove the circle and infoWinow
                            userMarker.removePrecisionCircle(); //remove it from the map

                            return true; // to hide the infoWindow
                        }
                    }else{
                        claimParkBtn.setVisibility(View.GONE);
                    }
                }

                return false;
            }
        });


        //if unmarked part of map is clicked hide the circle around the markers and the infowindows
        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                claimParkBtn.setVisibility(View.GONE);

                //TODO(optimization): lahko bi meu posebi shranjnene circles v list pa sam une v list zbrisu
                //remove all infoWindows
                for(UserMarker parkingMarker : userMarkersList){
                    parkingMarker.getMarker().hideInfoWindow();
                    parkingMarker.removePrecisionCircle(); //sience we removed the circle its reference must be set to null as well( so if marker is clicked again, the circle will be redrawn)
                }



            }
        });

        //On long click on the map, place marker on that place and pass parking spot to server
        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //pass parking spot to server, LOACTION PRECISION(last parameter) is 0 becasue user did not use GPS to give location but clicked
                new POST_NEW_USER_PARKING(latLng.latitude, latLng.longitude, System.currentTimeMillis(),0).execute("");
                thankYouMarker.setPosition(latLng);
                thankYouMarker.setVisible(true);
                animateMarker(thankYouMarker, new LatLng(latLng.latitude + 0.0008, latLng.longitude), true);
            }
        });




        //TODO(): move camera to use location
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(46.054515, 14.504680), 15));


        //For marker animation
        UPDATE_MARKERS_COLOR = new Handler(); //to be handle thread events
        UPDATE_MARKERS_COLOR.postDelayed(UI_UPDTAE_RUNNABLE, 5000);//This is like a clock triger event that runs on a UI(main) thread

        CHECK_FOR_SERVER_UPDATES = new Handler();
        CHECK_FOR_SERVER_UPDATES.postDelayed(GET_DATA_FROM_SERVER,60000);

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
    //TODO(): somehow make it so that markers dont all get updated at the same time
    Runnable UI_UPDTAE_RUNNABLE = new Runnable() {

        @Override
        public void run() {


            UserMarker tempMarker;
            ArrayList<UserMarker> toDelete = new ArrayList<>();
            //add any new markers
            for(UserMarker marker : userMarkersList){

                tempMarker =  updatUserMarkerIconColor(marker);
                if(tempMarker != null){
                    toDelete.add(tempMarker);
                }
            }
            //delete markers that are older than 10min
            for(UserMarker deleteMarker : toDelete){
                userMarkersList.remove(deleteMarker);
            }


            //call the function agaon after 5seconds
            UPDATE_MARKERS_COLOR.postDelayed(UI_UPDTAE_RUNNABLE, 5000);
        }
    };





    /*********************************************************END***************************************************/



    Runnable GET_DATA_FROM_SERVER = new Runnable() {

        @Override
        public void run() {


            Log.e("GOT NEW DATA","HUJAA");

            if(SHOW_USER) {
                new GET_USER_SUBBMITED_PARKINGS(globLATITUTE, globLONGITUTE, MAX_RADIUS).execute();
            }
            if(SHOW_PARKING) {
                new GET_PARKING_HOUSES().execute();
            }




            //update number of spaces on Parkig House marker icon
            for(ParkingHouseMarker parkingHouseMarker : parkingMarkersList){
                parkingHouseMarker.getMarker().setIcon(parkerMarkerIconMaker.getNewIcon(parkingHouseMarker.getNumberOfSpaces()));
            }


            CHECK_FOR_SERVER_UPDATES.postDelayed(GET_DATA_FROM_SERVER, 60000);
        }
    };





    /*******************************************GET DATA FROM SERVER CODE + ADD MARKERS FOR RECIVED LOCATIONS*********************************************************/

    //Class k iz podanega linka dobi JSON file iz serverja in v String zapiše podatke ki jih vrne server
    private class GET_USER_SUBBMITED_PARKINGS extends AsyncTask<String, String, String> {

        double latitute;
        double longitute;
        float radius;

        GET_USER_SUBBMITED_PARKINGS(double latitute, double longitute, float radius){

            this.latitute = latitute;
            this.longitute = longitute;
            this.radius = radius;

        }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            StringBuilder buffer = new StringBuilder();

            //loop until connection succeds
            while (true) {
                try {
                    URL url = new URL("https://peaceful-taiga-88033.herokuapp.com/users?globLATITUTE=" + String.valueOf(latitute) + "&globLONGITUTE=" + String.valueOf(longitute) + "&r=" + String.valueOf(radius) + "&");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    InputStream stream = connection.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    connection.disconnect();

                    reader.close();

                    break;//connection succeeded brek out of the loop


                } catch (IOException e) {
                    e.printStackTrace();
                    continue; //connection failed retry
                }
            }

            return buffer.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Gson gson = new Gson();//a external libery object for reading HTTP JSON responses


            try {
                JSONArray tempArray = new JSONArray(result);//Paharsa string Json v Json array


                for(int i=0; i<tempArray.length();i+=1){


                    String innerArray = tempArray.getString(i);//ker je Json sestavljen iz arrayey morm dobit usak array posevi kot string

                    Type type = new TypeToken<Map<String, String>>(){}.getType(); //DA lagko pol v Map podam keksn tip je ker item
                    Map<String, String> myMap = gson.fromJson(innerArray, type); //Key-Value map k lagk pol vn uzamem globLATITUTE, globLONGITUTE, time


                    double tempLat = Double.parseDouble(myMap.get("lat")); // najdem globLATITUTE in za tem uzamem stevki in jih spremenim v double
                    double tempLng = Double.parseDouble(myMap.get("lng")); // najdem globLONGITUTE in za tem uzamem stevki in jih spremenim v double
                    float tempTime = Float.parseFloat(myMap.get("time"));
                    String tempName = myMap.get("id");



                    if((System.currentTimeMillis() - tempTime)*(0.001) < MAX_AGE*60){
                        //TODO(Tim): add precision of marker
                        addUserMarker(tempName, tempLat,tempLng, tempTime, 10f);
                    }

                }

                //TODO(): uncomment
                //I can not run get user markers and parking houses at the same time because one Locks googleMap and the other cand add markers at that time
                //new GET_PARKING_HOUSES().execute("https://peaceful-taiga-88033.herokuapp.com/parkings");

                //TODO(DELETE): test
                //addParkingHouse("BTC", 46.067878, 14.547504, 67);


            } catch (JSONException | NullPointerException e) {e.printStackTrace();}
        }


    }

    /**************************************************************END******************************************************/





    /****************************************************POST DATA TO SERVER**********************************************************/

    class POST_NEW_USER_PARKING extends AsyncTask<String, String, String> {

        String latitute, longitute;
        String id;
        double timeOfCreation;
        String locationPrecision;

        POST_NEW_USER_PARKING(double latitute, double longitute, double timeOfCreation, float locationPrecision){


            this.latitute = String.valueOf(latitute);
            this.longitute = String.valueOf(longitute);
            this.locationPrecision = String.valueOf(locationPrecision);
            this.timeOfCreation = timeOfCreation;


        }

        @Override
        protected String doInBackground(String... params) {

            String resultToDisplay = "";

            resultToDisplay = postNewParkingSpotToServer(latitute, longitute, locationPrecision);

            id = resultToDisplay;

            return resultToDisplay;
        }

        @Override
        protected void onPostExecute(String result) {

            addUserMarker(id, Double.parseDouble(latitute), Double.parseDouble(longitute), timeOfCreation, Float.parseFloat(locationPrecision));

        }

    }


    //TODO(Tim): add location precision to post
    //TODO(): get the new ID an return it out
    private String postNewParkingSpotToServer(String latitute, String longitute, String locationPrecision){

        String result = "Mitja error";

        //loops untily connection was successfull
        while(true) {
            try {

                URL url = new URL("https://peaceful-taiga-88033.herokuapp.com/login");


                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                String urlParameter = "lat=" + latitute + "&lng=" + longitute + "&" + 0X0D + 0X0A;

                connection.setRequestMethod("POST");
                double time = System.currentTimeMillis();

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

                String output = "Request URl " + url;
                output += System.getProperty("line.seperator") + "Request Parameters " + urlParameter;
                output += System.getProperty("line.seperator") + "Request Response Code " + responseCode;

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line = "";
                StringBuilder reponseOutput = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    reponseOutput.append(line);
                }
                br.close();


                String tempResponse = String.valueOf(reponseOutput);
                tempResponse = tempResponse.substring(7, tempResponse.length() - 2);
                Log.e("POST resposne", tempResponse + " " + String.valueOf(time));


                result = tempResponse;

                break;//connection was succeesfull break out of the loop


            } catch (IOException e) {
                e.printStackTrace();
                continue;//connecrion failed retry
            }

        }

        return result;

    }



    /**********************************************************END*******************************************************************/




    /*********************************************************GET PAYED PARKINGS FROM SERVER****************************************/

    private class GET_PARKING_HOUSES extends AsyncTask<String, String, String> {


        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            StringBuilder buffer = new StringBuilder();

            //If it fails to connect retry until it succeeds
            while (true) {
                try {
                    URL url = new URL("https://peaceful-taiga-88033.herokuapp.com/parkings");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    InputStream stream = connection.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    connection.disconnect();

                    reader.close();

                    break;//connection was succeesfull break out of the loop

                } catch (IOException e) {
                    e.printStackTrace();
                    continue;//connection failed retry
                }
            }
            return buffer.toString();//pass recived data to onPostExecute()
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Gson gson = new Gson();//a external libery object for reading HTTP JSON responses

            try {
                JSONArray tempArray = new JSONArray(result);//Paharsa string Json v Json array
                for(int i=0; i<tempArray.length();i+=1){

                    String innerArray = tempArray.getString(i);//ker je Json sestavljen iz arrayey morm dobit usak array posevi kot string

                    Type type = new TypeToken<Map<String, String>>(){}.getType(); //DA lagko pol v Map podam keksn tip je ker item
                    Map<String, String> myMap = gson.fromJson(innerArray, type); //Key-Value map k lagk pol vn uzamem globLATITUTE, globLONGITUTE, time

                    //TODO(): get parking houses names

                    double tempLat = Double.parseDouble(myMap.get("lat")); // najdem lat in za tem uzamem stevki in jih spremenim v double
                    double tempLng = Double.parseDouble(myMap.get("lng")); // najdem lng in za tem uzamem stevki in jih spremenim v double
                    int tempNumSpaces = Integer.parseInt(myMap.get("available"));
                    String tempName = myMap.get("name");


                    //TODO(Tim): add precision of marker
                    addParkingHouse(tempName, tempLat,tempLng, tempNumSpaces);

                }


            } catch (JSONException | NullPointerException e) {e.printStackTrace();}
        }


    }



    /***********************************************************END*****************************************************************/





    /*******************************************GET DATA FROM SERVER CODE + ADD MARKERS FOR RECIVED LOCATIONS*********************************************************/

    //Class k iz podanega linka dobi JSON file iz serverja in v String zapiše podatke ki jih vrne server
    private class DELETE_USERS_ID extends AsyncTask<String, String, String> {

        String id;

        DELETE_USERS_ID(String id){
            this.id = id;
        }


        protected String doInBackground(String... params) {

            //If it fails to connect retry until it succeeds
            while (true) {
                try {

                    URL url = new URL("https://peaceful-taiga-88033.herokuapp.com/users/" + id);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("DELETE");
                    int responseCode = connection.getResponseCode();

                    break;//connection was succeesfull break out of the loop

                } catch (IOException e) {
                    e.printStackTrace();
                    continue;//connection failed retry
                }
            }

            return null;
        }

    }

    /**************************************************************END******************************************************/




    //TODO(): add precision
    //Function to add a marker on maps
    private void addUserMarker(String id, double lat, double lng, double timeOfCreation, float locationPrecision){

        //First check if this marker already exists on the maps
        for(UserMarker marker : userMarkersList){
            //if it does then jump out of a function without placing the marker
            if(id.compareTo(marker.getDatabaseID()) == 0){
                return;
            }
        }


        //v Maps dodam nov marker in ga shranim v marker list
        userMarkersList.add( new UserMarker( id, new LatLng(lat,lng),timeOfCreation,locationPrecision, googleMap, userMarkerIconMaker.getNewIcon(0)));

        updatUserMarkerIconColor(userMarkersList.get(userMarkersList.size()-1));
    }

    //Just adds pop in animation
    private void popInUserMarker(String id, double lat, double lng, double timeOfCreation, float locationPrecision){

        //First check if this marker already exists on the maps
        for(UserMarker marker : userMarkersList){
            //if it does then jump out of a function without placing the marker
            if(id.compareTo(marker.getDatabaseID()) == 0){
                return;
            }
        }


        //v Maps dodam nov marker in ga shranim v marker list
        userMarkersList.add( new UserMarker( id, new LatLng(lat,lng),timeOfCreation,locationPrecision, googleMap, userMarkerIconMaker.getNewIcon(0)));

        updatUserMarkerIconColor(userMarkersList.get(userMarkersList.size()-1));

        userMarkersList.get(userMarkersList.size()-1).animatePopIn();//The only difference from add UserMarker
    }

    private UserMarker updatUserMarkerIconColor(UserMarker marker){

        marker.updateMarkerAge();
        if(marker.getAge() > MAX_AGE*60){
            marker.getMarker().remove();
            return marker;
        }else{
            marker.getMarker().setIcon(userMarkerIconMaker.getNewIcon(marker.getAge()));
            return null;
        }


    }



    private void addParkingHouse(String name, Double latitute, Double longitute, int numSpaces){

        //first check if this parking map already exists on the map
        for(ParkingHouseMarker parkingHouseMarker : parkingMarkersList){
            //TODO(): change comparison to some other id not name(2 parking houses could have the sam name)
            if(parkingHouseMarker.getDatabaseID().compareTo(name) == 0){

                parkingHouseMarker.setNumberOfSpaces(numSpaces);

                return;
            }
        }


        MarkerOptions tempOptions = new MarkerOptions().position(new LatLng(latitute, longitute))
                .icon(parkerMarkerIconMaker.getNewIcon(numSpaces))
                .title(name)
                .anchor(0.5f,0.5f);

        parkingMarkersList.add(new ParkingHouseMarker(name, new LatLng(latitute, longitute), numSpaces, googleMap.addMarker(tempOptions)));

    }





    public void claimParking(View view){

    }


    public void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = googleMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;
        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }


    //TODO(DELETE):test
    private void generateTestingMarkers(){

        double starost = System.currentTimeMillis();
        float lat = 46.056779f;
        float lng = 14.506324f;

        for(int i=0; i<10;i++){


            MarkerOptions tempopt = new MarkerOptions().position(new LatLng(lat,lng));
            userMarkersList.add(new UserMarker("hi",new LatLng(lat,lng),starost,0,googleMap,userMarkerIconMaker.getNewIcon(i*60)));


            starost -= 60000;
            lng += 0.0007199999;
        }



    }


    ////////////////////////////////////////////////BUTTON CLICKS//////////////////////////////////////////////////

    public void onOpenOpt(View view){

        openOptBtn.startAnimation(gearAnimOut);
        openOptBtn.setVisibility(View.GONE);

        optionsMenu.setVisibility(View.VISIBLE);

        optionsTitle.startAnimation(slideInAnim1);
        optionsDisplay.startAnimation(slideInAnim2);
        optionsMarker.startAnimation(slideInAnim3);
        saveOptBtn.startAnimation(slideInAnim4);

    }


    public void onCloseOpt(View v){

        optionsMenu.setVisibility(View.GONE);

        openOptBtn.setVisibility(View.VISIBLE);
        openOptBtn.startAnimation(gearAnimIn);

    }

    public void onClaimParking(View v){

        //cant delete marker mid for loop so i store it in here for deleting later
        UserMarker parkingMarkerToRemove = null;
        //Loop through all the marker to see witchs infowindow was clicked
        for(UserMarker parkingMarker : userMarkersList){
            //when marker whos window was clicked is found delete the maerker, circle, parking spot
            if(parkingMarker.getMarker().getId().compareTo(markerInFocus.getId()) == 0){

                new DELETE_USERS_ID(parkingMarker.getDatabaseID()).execute();

                markerInFocus.remove();
                parkingMarker.removePrecisionCircle();
                //TODO(): add function to remove free spot from database

                parkingMarkerToRemove = parkingMarker;//store the marker to delete it from the list later
            }

        }
        userMarkersList.remove(parkingMarkerToRemove);

        claimParkBtn.setVisibility(View.GONE);

    }


    ////////////////////////////////////////////////////END///////////////////////////////////////////////////////////



    public void onSaveOptions(View v){


        updateGlobalVariables();

        for(UserMarker marker : userMarkersList){

            marker.getMarker().remove();

        }
        userMarkersList.clear();


        for(ParkingHouseMarker marker : parkingMarkersList){

            marker.getMarker().remove();

        }
        parkingMarkersList.clear();


        if(SHOW_USER) {
            new GET_USER_SUBBMITED_PARKINGS(globLATITUTE, globLONGITUTE, MAX_RADIUS).execute();
        }
        if(SHOW_PARKING) {
            new GET_PARKING_HOUSES().execute();
        }

        optionsMenu.setVisibility(View.GONE);

        openOptBtn.setVisibility(View.VISIBLE);
        openOptBtn.startAnimation(gearAnimIn);

    }


    private void updateGlobalVariables(){

        //TODO(): to last location
        globLATITUTE = 46.054515;
        globLONGITUTE = 14.504680;

        SHOW_USER = userCheck.isChecked();
        SHOW_PARKING = parkingCheck.isChecked();
        MAX_RADIUS = distanceSeek.getProgress();
        MAX_AGE = ageSeek.getProgress();

        Log.e("CHECKS", SHOW_USER+" "+SHOW_PARKING);

    }



}






