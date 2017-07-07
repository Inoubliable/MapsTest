package com.janzelj.tim.mapstest;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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

    private Circle popInCircle;
    private float popInCircleRadius;






    UserMarker(String databaseID, LatLng location, double timeOfCreation, float locationPrecision, GoogleMap map, BitmapDescriptor icon){

        this.databaseID = databaseID;
        this.location = location;

        age = calculateMarkerAge();
        this.timeOfCreation = timeOfCreation;

        this.locationPrecision = locationPrecision;


        MarkerOptions tempMarkerOpt = new MarkerOptions().position(location)
                                                        .title("Take Parking")
                                                        .icon(icon)
                                                        .anchor(0.5f,0.5f);
        this.marker = map.addMarker(tempMarkerOpt);




        CircleOptions tempLocationPrecision = new CircleOptions().center(location).radius(locationPrecision).visible(false).fillColor(Color.GREEN);
        locationPrecisionCircle = map.addCircle(tempLocationPrecision);

        popInCircleRadius = 0;
        CircleOptions tempPopInCircle = new CircleOptions().center(location).radius(0).fillColor(Color.YELLOW).visible(false).strokeColor(Color.TRANSPARENT);
        popInCircle = map.addCircle(tempPopInCircle);


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
        return (System.currentTimeMillis() - timeOfCreation)*(0.001d); // for seconds

    }

    double getAge() {
        return calculateMarkerAge();
    }

    void animatePopIn(){

        popInCircle.setVisible(true);

        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {


                popInCircleRadius+=0.6;

                popInCircle.setRadius(popInCircleRadius);


                if(popInCircleRadius<60){
                    // Post again 16ms later.
                    handler.postDelayed(this, 1);
                }else{

                    popInCircleRadius = 0;
                    popInCircle.setRadius(popInCircleRadius);
                    popInCircle.setVisible(false);

                }



            }
        });
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
