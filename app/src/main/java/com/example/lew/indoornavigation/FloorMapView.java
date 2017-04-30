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
    public int[][] getWall_corners(){
        return wall_corners;
    }

    int[][] wall_corners = {{660,662},{940,662},{940,73},{1156,73},{1156,606},{1237,606},{1237,704},{1154,704},{1154,802},{1111,802},{1043,869}
            ,{603,869},{603,701},{660,701}};

    private final int WIDTH_MAP = 1279;
    private final int HEIGHT_MAP = 1010;
    private final float ASPECT_RATIO = (float)HEIGHT_MAP/(float)WIDTH_MAP;
    private final int iconSizecm = 100;

    private float pixelTocm =1000/247;
    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;

    private int basePositionX = 4250; //initial coordinates in cm
    private int basePositionY = 2000;
    private int triangle_side_length;
    private int triangle_centre_height;
    private Bitmap map;
    private boolean resized = false;

    private double[] currentPos;
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

        currentPos = new double[2];
        currentPos[0] = 0.0;
        currentPos[1] = 0.0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.RED);
        if (resized != true) {
            SCREEN_WIDTH = getWidth();
            SCREEN_HEIGHT = getHeight();
            float scaleX = (float)WIDTH_MAP/(float)SCREEN_WIDTH;
            float scaleY = (float)HEIGHT_MAP/(float)SCREEN_HEIGHT;
            System.out.println("scaleX: "+scaleX+",scaleY: "+scaleY);
            System.out.println("SCREEN WIDTH:"+SCREEN_WIDTH+",SCREEN HEIGHT:"+SCREEN_HEIGHT);
            pixelTocm = pixelTocm *scaleX;
            map = Bitmap.createScaledBitmap(map, SCREEN_WIDTH, SCREEN_HEIGHT, false);

            triangle_side_length = (int) ((1/pixelTocm) * iconSizecm);
            triangle_centre_height = triangle_side_length*3;

            for (int i=0;i<wall_corners.length;i++){
                int[] pt = wall_corners[i];
                wall_corners[i][0] = (int) (pt[0]*(1/scaleX));
                wall_corners[i][1] = (int) (SCREEN_HEIGHT - pt[1]*(1/scaleY));
                System.out.println("Wall scaled to "+wall_corners[i][0]+","+wall_corners[i][1]);
            }
            basePositionX = (int) 1184.703472348348;
            basePositionY = 775;
            //basePositionX = (int) ((1/pixelTocm) * basePositionX);
            //basePositionY = (int) ((1/pixelTocm) * basePositionY);
            currentPos[0] = basePositionX;
            currentPos[1] = basePositionY;
            //DataProcessing.getFinalDestination(wall_corners,1333.6004347395194,804.0482048229254,1404.9484043996279,777.2917621337291);
            //DataProcessing.getIntersection(wall_corners[3][0],wall_corners[3][1],wall_corners[4][0],wall_corners[4][1],1200,1100,1400,1300);
            resized = true;
        }

        int pos_x = (int)currentPos[0];
        int pos_y = (int)currentPos[1];
        //System.out.println(pos_x+","+pos_y);

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
        paint.setTextSize(200);
        canvas.drawText(String.valueOf((int) MainActivity.bearing),100,300,paint);

        invalidate();

    }

    public double[] getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(double[] currentPos) {
        this.currentPos = currentPos;
    }

    public int getBasePositionX() {
        return basePositionX;
    }

    public int getBasePositionY() {
        return basePositionY;
    }

    public void setBasePositionX(int basePositionX) {
        this.basePositionX = basePositionX;
    }

    public void setBasePositionY(int basePositionY) {
        this.basePositionY = basePositionY;
    }

}
