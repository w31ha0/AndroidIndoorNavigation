package com.example.lew.indoornavigation;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by Lew on 13/4/2017.
 */
public class RotateTask extends AsyncTask<Bitmap, Void, Bitmap> {
    private WeakReference<ImageView> imgInputView;
    private WeakReference<Bitmap> rotateBitmap;

    public RotateTask(){
    }

    @Override
    protected Bitmap doInBackground(Bitmap... params) {
        Matrix matrix = new Matrix();
        int angleChanged = (int)(MainActivity.bearing-MainActivity.prevBearing);
        System.out.println(angleChanged);
        matrix.postRotate(45);
        rotateBitmap = new WeakReference<Bitmap>(Bitmap.createBitmap(params[0], 0, 0,params[0].getWidth(), params[0].getHeight(), matrix, true));
        return rotateBitmap.get();
    }

    @Override
    protected void onPreExecute() {
        //if you want to show progress dialog
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        //dismiss progress dialog
        FloorMapView.navIcon = result;
    }
}