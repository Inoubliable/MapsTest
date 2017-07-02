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
    private double timeOfCreation;
    private float locationPrecision;


    private float alpha;
    private int[] markerColor;
    private double age;


    private Circle locationPrecisionCircle;



    UserMarker(String databaseID, LatLng location, double timeOfCreation, float locationPrecision, Marker marker){

        this.databaseID = databaseID;
        this.location = location;

        age = calculateMarkerAge();
        this.timeOfCreation = timeOfCreation;

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

    }




    //returnes age in seconds
    private double calculateMarkerAge(){
        //return (float) ((System.currentTimeMillis() - timeOfCreation)*(1.66667*Math.pow(10,-5))); //For minutes
        return (System.currentTimeMillis() - timeOfCreation)*(0.001); // for seconds

    }

    double getAge() {
        Log.d("MarkerAge", ""+calculateMarkerAge()/60);
        return calculateMarkerAge();
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
