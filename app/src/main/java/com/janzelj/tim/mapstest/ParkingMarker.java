package com.janzelj.tim.mapstest;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by mitja on 6/24/17.
 *
 *
 * This is a custom Marker class(basicly just has a Google Maps Marker class + time and id(from the database)+ added ability to handle touch events)
 *
 *
 *
 *
 */

public class ParkingMarker {


    private Marker marker; //Google Maps api class for drawing markers on maps
    double lat;
    double lng;
    private String id; //from the databse(to check the http return string for new parking spots)
    private float precision; //if user uses GPS to annouce a free spot, we give(upon clicking the marker) a range in witch the viehcle is parked(GPS is not 100% accurate)
    private float time; //then the spot was annouced free
    private float alpha; //to graphicali show how recent is the free parking spot



    public ParkingMarker(String id, double lat, double lng, float time, float precision, Marker marker){
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.time = time;
        this.precision = precision;

        this.marker = marker; // marker is returned upon creating a marker with map.addMarker()
        alpha = marker.getAlpha(); // could also just set it to 1
    }



    public void touch(){

    }

    public void updateAlpha(){
        time += 1000; // potekla 1 sec
        alpha = calculateAlphaForTime(time); //recalculate the oppacitiy of the marker- correspongind to time
        marker.setAlpha(alpha); //set new oppacitiy
    }

    private float calculateAlphaForTime(float a){
        //TODO(): update mathematical function
        return alpha-0.01f; //this should be a more coplicated function involving TIME
    }


    public String getID(){
        return id;
    }


}
