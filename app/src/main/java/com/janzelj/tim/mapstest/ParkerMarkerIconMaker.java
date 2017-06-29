package com.janzelj.tim.mapstest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.ArrayList;

/**
 * Created by mitja on 6/29/17.
 */

class ParkerMarkerIconMaker {



    private Canvas canvas;
    private Bitmap bitmap;
    private Paint textPaint;


    private Bitmap houseBitmap;





    ParkerMarkerIconMaker(int iconSize, float textSize, Context context) {


        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));


        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        bitmap = Bitmap.createBitmap(iconSize,iconSize, bitmapConfig);

        canvas = new Canvas(bitmap);

        houseBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.parking_house);






    }

    BitmapDescriptor getNewIcon(int numberOfSpaces){

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(houseBitmap,0,0,null);
        canvas.drawText(String.valueOf(numberOfSpaces),canvas.getWidth()/2, (canvas.getHeight()/2)+45, textPaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}
