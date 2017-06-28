package com.janzelj.tim.mapstest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
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

    private GoogleMap googleMap;

    LocationManager locationManager;
    String provider;
    double lat;
    double lng;


    ArrayList<UserMarker> markersList;//stores ParkingMarkers for updating the oppacity of markers
    ArrayList<Circle> markersWithCircles;//stores circles for displaying precision

    Handler UPDATE_MARKERS_COLOR;//For thread events (setting a clocl trigerted fucntion for updating the oppacity of markers)



    Paint userMarkerPaint, userMarkerPaintText;
    Paint parkingHousePaint, parkingHousePaintText;
    Paint mitjaMarkerPaint, mitjaMarkerPaintText;

    Bitmap.Config bitmapConfigUserMarker;
    Bitmap bitmapForUserMarker;

    Canvas canvasUserMarker;



    Bitmap.Config bitmapConfigeParkingHouseMarker;
    Bitmap bitmapForParkingHouseMarker;
    Paint parkingMarkerPaintText;

    Canvas canvasParkingHouseMarker;





    /**************************************************END**************************************************************/





    /*************************************ANDROID ACTIVITY FUNCTIONS(onCreade, onPause, onResume)***************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        markersList = new ArrayList<>(); //sotres ParkingMarkers for updating the oppacity of markers
        markersWithCircles = new ArrayList<>();

        //setup Bitmap and cavas used to create Icons for Markers
        bitmapConfigUserMarker = Bitmap.Config.ARGB_8888;
        bitmapForUserMarker = Bitmap.createBitmap(200,200, bitmapConfigUserMarker);
        canvasUserMarker = new Canvas(bitmapForUserMarker);
        //setup Paints for canvas
        userMarkerPaint = new Paint();
        userMarkerPaint.setColor(Color.RED);
        userMarkerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        userMarkerPaintText = new Paint();
        userMarkerPaintText.setColor(Color.WHITE);
        userMarkerPaintText.setStyle(Paint.Style.FILL_AND_STROKE);
        userMarkerPaintText.setTextAlign(Paint.Align.CENTER);
        userMarkerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        userMarkerPaintText.setTextSize(40);

        //This has to be so when the markers are created they bitmap is not empty
        //TODO(DELETE): test
        canvasUserMarker.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasUserMarker.drawCircle(100,100,50,userMarkerPaint);
        canvasUserMarker.drawText("NEW", canvasUserMarker.getWidth() / 2, (canvasUserMarker.getHeight()/2)+15, userMarkerPaintText);


        //TODO(DELETE): test
        //ParkingHouse Marskers paint canvas and bitmapDescriptor
        bitmapConfigeParkingHouseMarker = Bitmap.Config.ARGB_8888;
        bitmapForParkingHouseMarker = Bitmap.createBitmap(150,150, bitmapConfigeParkingHouseMarker);
        parkingHousePaintText = new Paint();
        parkingHousePaintText.setColor(Color.WHITE);
        parkingHousePaintText.setStyle(Paint.Style.FILL_AND_STROKE);
        parkingHousePaintText.setTextAlign(Paint.Align.CENTER);
        parkingHousePaintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        parkingHousePaintText.setTextSize(60);

        canvasParkingHouseMarker = new Canvas(bitmapForParkingHouseMarker);
        canvasParkingHouseMarker.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Bitmap temp = BitmapFactory.decodeResource(getResources(),R.mipmap.parking_house);
        canvasParkingHouseMarker.drawBitmap(temp,0,0,null);
        canvasParkingHouseMarker.drawText("36",canvasParkingHouseMarker.getWidth()- 40, (canvasUserMarker.getHeight()/2)+10, parkingHousePaintText);





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



        //For marker animation
        UPDATE_MARKERS_COLOR = new Handler(); //to be handle thread events
        UPDATE_MARKERS_COLOR.postDelayed(UI_UPDTAE_RUNNABLE, 5000);//This is like a clock triger event that runs on a UI(main) thread



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

        //Gets Json from server and adds markers on maps after(UserMarker and than ParkingHouses)
        new GET_USER_SUBBMITED_PARKINGS().execute("https://peaceful-taiga-88033.herokuapp.com/users?lat="+String.valueOf(46.054515)+"&lng="+String.valueOf(14.504680)+"&r=500&");





        //Function onMarker Click (show the precision of the marker placement)
        this.googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                //Loop through all markers to see witch marker was clicked
                for(UserMarker userMarker : markersList){
                    //make action on cliced marker
                    if(userMarker.getMarker().getId().compareTo(marker.getId()) == 0){
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

                            return false;
                        }else{
                            //if same marker is cliced again remove the circle and infoWinow
                            userMarker.removePrecisionCircle(); //remove it from the map

                            return true; // to hide the infoWindow
                        }
                    }
                }

                return false;
            }
        });


        //if unmarked part of map is clicked hide the circle around the markers and the infowindows
        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //TODO(optimization): lahko bi meu posebi shranjnene circles v list pa sam une v list zbrisu
                //remove all infoWindows
                for(UserMarker parkingMarker : markersList){
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

            }
        });

        //TODO(): Check if user is close enough to the marker to delete(to acually take the parking spot)
        //If infoWinow above marker is clicked, delete this maerker/parking spot
        this.googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                //cant delete marker mid for loop so i store it in here for deleting later
                UserMarker parkingMarkerToRemove = null;
                //Loop through all the marker to see witchs infowindow was clicked
                for(UserMarker parkingMarker : markersList){
                    //when marker whos window was clicked is found delete the maerker, circle, parking spot
                    if(parkingMarker.getMarker().getId().compareTo(marker.getId()) == 0){

                        marker.remove();
                        parkingMarker.removePrecisionCircle();
                        //TODO(): add function to remove free spot from database

                        parkingMarkerToRemove = parkingMarker;//store the marker to delete it from the list later
                    }

                }
                markersList.remove(parkingMarkerToRemove);
            }
        });



        //TODO(): move camera to use location
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(46.054515, 14.504680), 15));

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


            for(UserMarker marker : markersList){



                updatUserMarkerIconColor(marker);

            }

            //TODO(): update for other type of markers as well

            UPDATE_MARKERS_COLOR.postDelayed(UI_UPDTAE_RUNNABLE, 5000);
        }
    };



    /*********************************************************END***************************************************/








    /*******************************************GET DATA FROM SERVER CODE + ADD MARKERS FOR RECIVED LOCATIONS*********************************************************/

    //Class k iz podanega linka dobi JSON file iz serverja in v String zapiše podatke ki jih vrne server
    private class GET_USER_SUBBMITED_PARKINGS extends AsyncTask<String, String, String> {

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
            googleMap.clear();

            try {
                JSONArray tempArray = new JSONArray(result);//Paharsa string Json v Json array

                for(int i=0; i<tempArray.length();i+=1){


                    String innerArray = tempArray.getString(i);//ker je Json sestavljen iz arrayey morm dobit usak array posevi kot string

                    Type type = new TypeToken<Map<String, String>>(){}.getType(); //DA lagko pol v Map podam keksn tip je ker item
                    Map<String, String> myMap = gson.fromJson(innerArray, type); //Key-Value map k lagk pol vn uzamem lat, lng, time


                    double tempLat = Double.parseDouble(myMap.get("lat")); // najdem lat in za tem uzamem stevki in jih spremenim v double
                    double tempLng = Double.parseDouble(myMap.get("lng")); // najdem lng in za tem uzamem stevki in jih spremenim v double
                    float tempTime = Float.parseFloat(myMap.get("time"));
                    String tempName = myMap.get("id");


                    //TODO(Tim): add precision of marker
                    addUserMarker(tempName, tempLat,tempLng, tempTime, 10f);

                }

                //TODO():Implement user location
                //moves camera to User Location
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(46.054515, 14.504680), 15));

                //TODO(): uncomment
                //I can not run get user markers and parking houses at the same time because one Locks googleMap and the other cand add markers at that time
                //new GET_PARKING_HOUSES().execute("https://peaceful-taiga-88033.herokuapp.com/parkings");

                addParkingHouse("BTC", 46.067878, 14.547504, 67);


            } catch (JSONException | NullPointerException e) {e.printStackTrace();}
        }


    }

    /**************************************************************END******************************************************/





    /****************************************************POST DATA TO SERVER**********************************************************/

    class POST_NEW_USER_PARKING extends AsyncTask<String, String, String> {

        String latitute, longitute;
        String id;
        float timeOfCreation;
        String locationPrecision;

        POST_NEW_USER_PARKING(double latitute, double longitute, float timeOfCreation, float locationPrecision){


            this.latitute = String.valueOf(latitute);
            this.longitute = String.valueOf(longitute);
            this.locationPrecision = String.valueOf(locationPrecision);
            this.timeOfCreation = timeOfCreation;

            Log.d("POST STARTED", "");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {

            String resultToDisplay = "";

            Log.d("POST MID", "");
            resultToDisplay = postNewParkingSpotToServer(latitute, longitute, locationPrecision);

            id = resultToDisplay;
            Log.d("Server Response", id);

            return resultToDisplay;



        }


        @Override
        protected void onPostExecute(String result) {
            Log.d("Post Request:", result);

            if(result != "Mitja error"){

            }

            Log.d("POST END", "");

            addUserMarker(id, Double.parseDouble(latitute), Double.parseDouble(longitute), timeOfCreation, Float.parseFloat(locationPrecision));

        }

    }


    //TODO(Tim): add location precision to post
    //TODO(): get the new ID an return it out
    private String postNewParkingSpotToServer(String latitute, String longitute, String locationPrecision){

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



    /*********************************************************GET PAYED PARKINGS FROM SERVER****************************************/

    private class GET_PARKING_HOUSES extends AsyncTask<String, String, String> {

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
            googleMap.clear();

            try {
                JSONArray tempArray = new JSONArray(result);//Paharsa string Json v Json array

                for(int i=0; i<tempArray.length();i+=1){


                    String innerArray = tempArray.getString(i);//ker je Json sestavljen iz arrayey morm dobit usak array posevi kot string

                    Type type = new TypeToken<Map<String, String>>(){}.getType(); //DA lagko pol v Map podam keksn tip je ker item
                    Map<String, String> myMap = gson.fromJson(innerArray, type); //Key-Value map k lagk pol vn uzamem lat, lng, time

                    //TODO(): get parking houses names

                    double tempLat = Double.parseDouble(myMap.get("lat")); // najdem lat in za tem uzamem stevki in jih spremenim v double
                    double tempLng = Double.parseDouble(myMap.get("lng")); // najdem lng in za tem uzamem stevki in jih spremenim v double
                    int tempNumSpaces = Integer.parseInt(myMap.get("available"));
                    String tempName = "Parking House";


                    //TODO(Tim): add precision of marker
                    addParkingHouse(tempName, tempLat,tempLng, tempNumSpaces);

                }

                //TODO():Implement user location
                //moves camera to User Location
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(46.054515, 14.504680), 15));



            } catch (JSONException | NullPointerException e) {e.printStackTrace();}
        }


    }



    /***********************************************************END*****************************************************************/

    //TODO(): add precision
    //Function to add a marker on maps
    private void addUserMarker(String id, double lat, double lng, float timeOfCreation, float locationPrecision){

        //First check if this marker already exists on the maps
        for(UserMarker marker : markersList){
            //if it does then jump out of a function without placing the marker
            if(id.compareTo(marker.getDatabaseID()) == 0){
                return;
            }
        }

        //options serve as propreties of marker(position, icon, name...)
        MarkerOptions myMarkerOptions = new MarkerOptions().position(new LatLng(lat, lng))
                .title("Take Parking")
                .icon(BitmapDescriptorFactory.fromBitmap(bitmapForUserMarker))
                .anchor(0.5f,0.5f);

        //v Maps dodam nov marker in ga shranim v marker list
        markersList.add( new UserMarker( id, new LatLng(lat,lng),timeOfCreation,locationPrecision, googleMap.addMarker(myMarkerOptions)));

        updatUserMarkerIconColor(markersList.get(markersList.size()-1));

    }

    void updatUserMarkerIconColor(UserMarker marker){

        marker.updateMarkerAge();



        userMarkerPaint.setColor(Color.rgb(marker.getMarkerColor()[0],0,marker.getMarkerColor()[1]));
        //TODO(DELETE): non fixed values for drawing
        canvasUserMarker.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasUserMarker.drawCircle(100,100,50,userMarkerPaint);
        canvasUserMarker.drawText("P", canvasUserMarker.getWidth() / 2, (canvasUserMarker.getHeight()/2)+15, userMarkerPaintText);

        marker.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(bitmapForUserMarker));

    }



    private void addParkingHouse(String name, Double latitute, Double longitute, int numSpaces){


        canvasParkingHouseMarker.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Bitmap temp = BitmapFactory.decodeResource(getResources(),R.mipmap.parking_house);
        canvasParkingHouseMarker.drawBitmap(temp,0,0,null);
        canvasParkingHouseMarker.drawText(String.valueOf(numSpaces),canvasParkingHouseMarker.getWidth()- 40, (canvasUserMarker.getHeight()/2)+10, parkingHousePaintText);


        MarkerOptions tempOptions = new MarkerOptions().position(new LatLng(latitute, longitute))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmapForParkingHouseMarker))
                .title(name)
                .anchor(0.5f,0.5f);

        googleMap.addMarker(tempOptions);

    }


}






