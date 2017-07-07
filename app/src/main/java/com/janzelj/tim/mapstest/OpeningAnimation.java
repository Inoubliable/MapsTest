package com.janzelj.tim.mapstest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by mitja on 7/7/17.
 */

public class OpeningAnimation extends View{

    private boolean drawing;

    private Paint darkPaint;
    private Paint lightPaint;

    private Path darkPath;
    private Path lightPath;

    private float width;
    private float height;

    private boolean first;

    private float lightPX;
    private float darkPX;

    private double startTime;
    private final int FPS = 60;
    private final double FPS_IN_MILLI = (1/FPS)*1000;

    private boolean openAnim;

    private Paint textPaint;
    private int textX;
    private int textY;
    private float textSize;


    private Paint loadingTextPaint;
    private int loadingX;
    private int loadingY;
    private float loadingSize;
    private boolean loadingBool;


    public OpeningAnimation(Context context) {
        super(context);

        drawing = true;

        first = true;

        loadingBool = false;

        darkPaint = new Paint();
        darkPaint.setColor(Color.DKGRAY);
        darkPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        lightPaint = new Paint();
        lightPaint.setColor(Color.BLUE);
        lightPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        darkPath = new Path();
        lightPath = new Path();

        lightPX = 0;
        darkPX = 0;

        startTime = 0;

        openAnim = false;


    }
    @Override
    public void onDraw(Canvas canvas) {


        if(drawing) {

            if (System.currentTimeMillis() - startTime >= FPS_IN_MILLI) {

                //DRAW
                if (first) {
                    first = false;

                    width = this.getWidth();
                    height = this.getHeight();
                    Log.e("" + width, "" + height);

                    darkPath.reset();
                    darkPath.moveTo(-100, -100);
                    darkPath.lineTo(width * 0.3f, -100);
                    darkPath.lineTo(width * 0.8f, height + 100);
                    darkPath.lineTo(-100, height + 100);

                    lightPath.reset();
                    lightPath.moveTo(width + 100, -100);
                    lightPath.lineTo(width * 0.18f, -100);
                    lightPath.lineTo(width * 0.68f, height + 100);
                    lightPath.lineTo(width + 100, height + 100);

                    textSize = height * 0.4f;

                    textPaint = new Paint();
                    textPaint.setColor(Color.WHITE);
                    textPaint.setTextSize(textSize);
                    textPaint.setTextAlign(Paint.Align.CENTER);

                    textX = (int) (width / 2);
                    textY = (int) ((height / 2) + (textSize / 4));



                    loadingSize = height/10f;

                    loadingTextPaint = new Paint();
                    loadingTextPaint.setColor(Color.WHITE);
                    loadingTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    loadingTextPaint.setTextAlign(Paint.Align.CENTER);
                    loadingTextPaint.setTextSize(loadingSize);


                    loadingY = (int) ((textY)+(textSize/2)+(height/50));
                    loadingX = (int) (width/2);

                    loadingBool = true;

                }


                startTime = System.currentTimeMillis();

                if (openAnim) {
                    openScreen();
                }

                canvas.drawPath(lightPath, lightPaint);
                canvas.drawPath(darkPath, darkPaint);


                canvas.drawText("P", textX, textY, textPaint);

                if(loadingBool){
                    loadingTextPaint.setAlpha(loadingTextPaint.getAlpha()-3);
                    canvas.drawText("Loading...", loadingX, loadingY, loadingTextPaint);
                }



                this.invalidate();

            }
        }
    }

    private void openScreen(){

        lightPX +=35;
        darkPX -= 35;

        darkPath.offset(darkPX,0);
        lightPath.offset(lightPX,0);

        if(textPaint.getAlpha() > 0) {
            textPaint.setAlpha(textPaint.getAlpha() - 5);
        }else{
            textPaint.setAlpha(0);
        }

        if ((textPaint.getAlpha() <= 0)&&(lightPX > width)&&(darkPX < 0)){
            drawing = false;
            this.setVisibility(View.GONE);
        }


    }

    public void openApp(){
        openAnim = true;
        loadingBool = false;
    }


}