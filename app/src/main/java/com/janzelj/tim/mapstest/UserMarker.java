package com.janzelj.tim.mapstest;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mitja on 6/28/17.
 */

class UserMarker {


    private Marker marker;

    private String databaseID;

    private LatLng location;
    private float timeOfCreation;
    float locationPrecision;


    private float alpha;
    private int[] markerColor;
    private float age;


    private Circle locationPrecisionCircle;



    UserMarker(String databaseID, LatLng location, float timeOfCreation, float locationPrecision, Marker marker){

        this.databaseID = databaseID;
        this.location = location;

        age = calculateMarkerAge();
        this.timeOfCreation = timeOfCreation;
        markerColor = calculateColorFromAge();

        this.locationPrecision = locationPrecision;


        this.marker = marker;

        alpha = 1f;




    }



    void removeMarker(){

    }


    /************************ Code to display and delete precision of marker placement*********/
    LatLng getLocation() {
        return location;
    }

    boolean isCircle(){
        if(locationPrecisionCircle == null){
            return false;
        }else{
            return true;
        }
    }

    void setCircle(Circle circle){
        locationPrecisionCircle = circle;
    }

    void removePrecisionCircle(){
        if(locationPrecisionCircle != null){
            locationPrecisionCircle.remove();
            locationPrecisionCircle = null;
        }
    }
    /***************************************End*************************************************/



    //calculates marker age + updates its color and time signiture
    void updateMarkerAge(){
        age = calculateMarkerAge();
        markerColor = calculateColorFromAge();
        //Log.d("Marker Age", String.valueOf(age*6000000));
        //Log.d("Marker Age", String.valueOf(age*6000000));
    }

    private int[] calculateColorFromAge(){
        //TODO(): Actual formula for calculating color dependant on time
        calculateMarkerAge();
        int red = (int) (age * 0.00425f);
        int blue = 255-red;
        //Log.d("Marker Red",String.valueOf(red));
        //Log.d("Marker Blue",String.valueOf(blue));
        //[red, blue]
        return new int[]{255,blue};
    }


    //returnes age in minutes
    private float calculateMarkerAge(){
        return (float) ((System.currentTimeMillis() - timeOfCreation)*(1.66667*Math.pow(10,-5)));
    }

    float getAge() {

        /*
        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date resultdate = new Date(yourmilliseconds);
        System.out.println(sdf.format(resultdate));
        */

        return age;
    }

    void animatePopIn(){

    }

    void animateFadeOut(){

    }

    void animateTranslate(float lat, float lng){

    }

    Marker getMarker() {
        return marker;
    }

    String getDatabaseID(){
        return databaseID;
    }

    int[] getMarkerColor(){
        return markerColor;
    }
}
