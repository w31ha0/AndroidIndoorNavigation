package com.example.lew.indoornavigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.view.View;

/**
 * Created by Lew on 13/4/2017.
 */
public class FloorMapView extends View{

    Paint paint = null;
    public static Bitmap navIcon;
    int[] pt1,pt2,pt3;
    AsyncTask<Bitmap, Void, Bitmap> task;

    public FloorMapView(Context context)
    {
        super(context);
        paint = new Paint();
        navIcon = BitmapFactory.decodeResource(getResources(), R.drawable.nav);
        pt1 = new int[2];
        pt2 = new int[2];
        pt3 = new int[2];
        task = new RotateTask();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int initial_pos_x = width/2;
        int initial_pos_y = height - (int)((9.0*height)/10.0);
        int triangle_side_length = width/15;
        int triangle_centre_height = width/7;

        int pos_x = initial_pos_x + (int)MainActivity.currentPos[0];
        int pos_y =  initial_pos_y + (int)MainActivity.currentPos[1];
        int angleChange = (int)(MainActivity.bearing - MainActivity.prevBearing);
        //System.out.println(angleChange);

        pt1[0] = pos_x - triangle_side_length/2;
        pt1[1] = pos_y - triangle_centre_height/2;
        pt2[0] = pos_x + triangle_side_length/2;
        pt2[1] = pos_y + triangle_centre_height/2;
        pt3[0] = pos_x + triangle_side_length;
        pt3[1] = pos_y - triangle_centre_height/2;

        float[] newVertices = Utils.rotateTriangle(-(int)MainActivity.bearing,pt1[0],pt1[1],pt2[0],pt2[1],pt3[0],pt3[1],pos_x,pos_y);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(newVertices[0], height - newVertices[1]);
        path.lineTo(newVertices[2], height - newVertices[3]);
        path.lineTo(newVertices[4], height - newVertices[5]);
        path.lineTo(newVertices[0], height -  newVertices[1]);
        path.close();
        canvas.drawPath(path, paint);

        invalidate();

    }

}
