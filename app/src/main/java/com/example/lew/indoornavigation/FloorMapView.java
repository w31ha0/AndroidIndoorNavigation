package com.example.lew.indoornavigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * Created by Lew on 13/4/2017.
 */
public class FloorMapView extends View{

    private final int WIDTH_MAP = 1279;
    private final int HEIGHT_MAP = 1010;
    private final float ASPECT_RATIO = (float)HEIGHT_MAP/(float)WIDTH_MAP;
    private final int iconSizecm = 100;

    private float pixelTocm =1000/247;
    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;
    private int initial_pos_x = 4250; //initial coordinates in cm
    private int initial_pos_y = 2000;
    private int triangle_side_length;
    private int triangle_centre_height;
    private Bitmap map;
    private boolean resized = false;

    Paint paint = null;
    int[] pt1,pt2,pt3;

    public FloorMapView(Context context)
    {
        super(context);
        paint = new Paint();
        pt1 = new int[2];
        pt2 = new int[2];
        pt3 = new int[2];

        map = BitmapFactory.decodeResource(context.getResources(),
            R.drawable.map);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.RED);
        if (resized != true) {
            SCREEN_WIDTH = getWidth();
            SCREEN_HEIGHT = getHeight();
            float scale = (float)WIDTH_MAP/(float)SCREEN_WIDTH;
            pixelTocm = pixelTocm *scale;
            map = Bitmap.createScaledBitmap(map, SCREEN_WIDTH, SCREEN_HEIGHT, false);

            initial_pos_x = (int) ((1/pixelTocm) * initial_pos_x);
            initial_pos_y = (int) ((1/pixelTocm) * initial_pos_y);
            triangle_side_length = (int) ((1/pixelTocm) * iconSizecm);
            triangle_centre_height = triangle_side_length*3;

            resized = true;
        }

        int pos_x = initial_pos_x + (int)MainActivity.currentPos[0];
        int pos_y =  initial_pos_y + (int)MainActivity.currentPos[1];

        pt1[0] = pos_x - triangle_side_length/2;
        pt1[1] = pos_y - triangle_centre_height/2;
        pt2[0] = pos_x + triangle_side_length/2;
        pt2[1] = pos_y + triangle_centre_height/2;
        pt3[0] = pos_x + triangle_side_length;
        pt3[1] = pos_y - triangle_centre_height/2;

        float[] newVertices = Utils.rotateTriangle(-(int)MainActivity.bearing,pt1[0],pt1[1],pt2[0],pt2[1],pt3[0],pt3[1],pos_x,pos_y);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(newVertices[0], SCREEN_HEIGHT - newVertices[1]);
        path.lineTo(newVertices[2], SCREEN_HEIGHT - newVertices[3]);
        path.lineTo(newVertices[4], SCREEN_HEIGHT - newVertices[5]);
        path.lineTo(newVertices[0], SCREEN_HEIGHT -  newVertices[1]);
        path.close();

        canvas.drawBitmap(map,0,0,paint);
        canvas.drawPath(path, paint);

        invalidate();

    }

}
