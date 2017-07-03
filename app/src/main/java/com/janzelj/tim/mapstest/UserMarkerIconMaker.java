package com.janzelj.tim.mapstest;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by mitja on 6/28/17.
 */

class UserMarkerIconMaker{

    private final float NUMBER_OF_SEGMENTS = 120;

    private Canvas canvas;
    private Bitmap bitmap;
    private Paint textPaint;



    private float iconSize;
    private float textSize;


    private Paint bluePaint;
    private Paint redPaint;



    private ArrayList<Path> pathArrayList;

    private ArrayList<double[]>  vertecies;

    UserMarkerIconMaker(int iconSize, float textSize) {

        this.iconSize = iconSize;
        this.textSize = textSize;

        bluePaint = new Paint();
        bluePaint.setColor(Color.BLUE);
        bluePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        redPaint = new Paint();
        redPaint.setColor(Color.RED);
        redPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));


        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        bitmap = Bitmap.createBitmap(iconSize,iconSize, bitmapConfig);

        canvas = new Canvas(bitmap);





        pathArrayList = new ArrayList<>();

        vertecies = new ArrayList<>();
        caclulateVerticies();
        makePath();

    }


    BitmapDescriptor getNewIcon(double age){

        int i = calculateAgeCount(age);


        canvas.drawCircle(iconSize/2,iconSize/2,iconSize/2,redPaint);
        while (i < pathArrayList.size()){
            canvas.drawPath(pathArrayList.get(i),bluePaint);
            i++;
        }

        canvas.drawText("P",iconSize/2,(iconSize/2)+(textSize/2)-3,textPaint);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private int calculateAgeCount(double age){

        //TODO(): calulate kazalec related to time

        if(age < 600){ //if age less than 10 min
            return (int) (age/5);
        }else {
            return 120;
        }



    }

    private void caclulateVerticies(){

        double[] vertex = new double[]{0,-iconSize/2};

        double[] translateVector = new double[]{iconSize/2,iconSize/2}; // doubles as circle center

        double[] roatitonMatrix = Maths.getCWmatrix(360/NUMBER_OF_SEGMENTS);

        float temp = NUMBER_OF_SEGMENTS;
        while (temp >= 0){
            temp--;

            vertecies.add(translateVector);
            vertecies.add(Maths.sumVectorVector(vertex,translateVector));
            vertex = Maths.mulMatrixVector(roatitonMatrix, vertex);
            vertecies.add(Maths.sumVectorVector(vertex,translateVector));
            vertecies.add(translateVector);
        }
    }

    private void makePath(){

        int i = 0;
        Path tempPath;

        while(i < vertecies.size()-4){
            i+=4;

            tempPath = new Path();
            tempPath.reset();
            tempPath.moveTo((float) vertecies.get(i)[0], (float) vertecies.get(i)[1]);
            tempPath.lineTo((float) vertecies.get(i+1)[0], (float) vertecies.get(i+1)[1]);
            tempPath.lineTo((float) vertecies.get(i+2)[0], (float) vertecies.get(i+2)[1]);
            tempPath.lineTo((float) vertecies.get(i+3)[0], (float) vertecies.get(i+3)[1]);
            pathArrayList.add(tempPath);

        }

    }



}
