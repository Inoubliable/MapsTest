package com.janzelj.tim.mapstest;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by mitja on 6/28/17.
 */

public class ParkingHouseMarker {



    private Marker marker;
    private String databaseID;
    private LatLng location;
    private int numberOfSpaces;


    ParkingHouseMarker(String databaseID, LatLng location, int numberOfSpaces ,Marker marker){

        this.databaseID = databaseID;
        this.location = location;
        this.marker = marker;
        this.numberOfSpaces = numberOfSpaces;

    }


    void UpdateNUmberOfSpaces(int number , BitmapDescriptor icon){
        numberOfSpaces = number;
        updateMarkerIcon(icon);
    }

    private void updateMarkerIcon(BitmapDescriptor icon){
        marker.setIcon(icon);
    }


}
