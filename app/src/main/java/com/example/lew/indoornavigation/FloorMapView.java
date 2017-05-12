package com.example.lew.indoornavigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.wifi.WifiManager;
import android.view.View;

/**
 * Created by Lew on 13/4/2017.
 */
public class FloorMapView extends View{

    private int[][][] allWalls;
    private int[][] waps;
    private int WIDTH_MAP;
    private int HEIGHT_MAP;
    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;
    private int basePositionX; //initial coordinates in cm
    private int basePositionY;
    private int iconSizecm;
    private float pixelTocm;

    private int triangle_side_length;
    private int triangle_centre_height;
    private Bitmap map;
    private boolean resized = false;
    private double[] currentPos;
    private Paint paint = null;
    private int[] triangle_pt1, triangle_pt2, triangle_pt3;

    public FloorMapView(Context context,int[][][] allWalls,int waps[][],int WIDTH_MAP,int HEIGHT_MAP,int basePositionX,int basePositionY,int iconSizecm,float pixelTocm,int drawable)
    {
        super(context);
        this.allWalls = allWalls;
        this.waps = waps;
        this.WIDTH_MAP = WIDTH_MAP;
        this.HEIGHT_MAP = HEIGHT_MAP;
        this.basePositionX = basePositionX;
        this.basePositionY = basePositionY;
        this.iconSizecm = iconSizecm;
        this.pixelTocm = pixelTocm;
        this.map = BitmapFactory.decodeResource(context.getResources(),drawable);

        paint = new Paint();
        currentPos = new double[2];
        currentPos[0] = 0.0;
        currentPos[1] = 0.0;
        triangle_pt1 = new int[2];
        triangle_pt2 = new int[2];
        triangle_pt3 = new int[2];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.RED);
        if (resized != true)
            rescaleAll();

        int pos_x = (int)currentPos[0];
        int pos_y = (int)currentPos[1];
        //System.out.println(pos_x+","+pos_y);

        triangle_pt1[0] = pos_x - triangle_side_length/2;
        triangle_pt1[1] = pos_y - triangle_centre_height/2;
        triangle_pt2[0] = pos_x + triangle_side_length/2;
        triangle_pt2[1] = pos_y + triangle_centre_height/2;
        triangle_pt3[0] = pos_x + triangle_side_length;
        triangle_pt3[1] = pos_y - triangle_centre_height/2;

        float[] newVertices = Utils.rotateTriangle(-(int)MainActivity.bearing, triangle_pt1[0], triangle_pt1[1], triangle_pt2[0], triangle_pt2[1], triangle_pt3[0], triangle_pt3[1],pos_x,pos_y);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(newVertices[0], SCREEN_HEIGHT - newVertices[1]);
        path.lineTo(newVertices[2], SCREEN_HEIGHT - newVertices[3]);
        path.lineTo(newVertices[4], SCREEN_HEIGHT - newVertices[5]);
        path.lineTo(newVertices[0], SCREEN_HEIGHT -  newVertices[1]);
        path.close();

        canvas.drawBitmap(map,0,0,paint);
        canvas.drawPath(path, paint);
        paint.setTextSize(70);
        canvas.drawText("RSSI:"+String.valueOf(MainActivity.rssi),200,200,paint);
        canvas.drawText("PRESSURE:"+String.valueOf(MainActivity.pressure),200,300,paint);

        invalidate();

    }

    private void rescaleAll(){
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

        basePositionX = (int) (basePositionX * 1/scaleX);
        basePositionY = SCREEN_HEIGHT - (int)(basePositionY * 1/scaleY);

        for (int[][] wall_corners:allWalls)
            for (int i=0;i<wall_corners.length;i++){
                int[] pt = wall_corners[i];
                wall_corners[i][0] = (int) (pt[0]*(1/scaleX));
                wall_corners[i][1] = (int) (SCREEN_HEIGHT - pt[1]*(1/scaleY));
                System.out.println("Wall scaled to "+wall_corners[i][0]+","+wall_corners[i][1]);
            }

        for (int i=0;i<waps.length;i++){
            int[] pt = waps[i];
            waps[i][0] = (int) (pt[0]*(1/scaleX));
            waps[i][1] = (int) (SCREEN_HEIGHT - pt[1]*(1/scaleY));
            System.out.println("WAP scaled to "+waps[i][0]+","+waps[i][1]);
        }

        currentPos[0] = basePositionX;
        currentPos[1] = basePositionY;
        //DataProcessing.getFinalDestination(wall_corners,1333.6004347395194,804.0482048229254,1404.9484043996279,777.2917621337291);
        //DataProcessing.getIntersection(wall_corners[3][0],wall_corners[3][1],wall_corners[4][0],wall_corners[4][1],1200,1100,1400,1300);
        resized = true;
    }

    public int[][][] getAllWalls(){
        return allWalls;
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
