package com.example.lew.indoornavigation;

import java.util.ArrayList;

/**
 * Created by Lew on 10/4/2017.
 */
public class Utils {

    public static boolean isBetween(double a, double b, double c) {
        boolean result = b > a ? c > a && c < b : c > b && c <a;
        //System.out.println(c+" is in between "+a+" and "+b+": "+result);
        return result;
    }

    public static float[] rotateTriangle(int rotate,float x1,float y1,float x2,float y2,float x3,float y3,float centerX,float centerY){
        float angle = (float) Math.toRadians(rotate); // Angle to rotate
        // Rotating
        float x1r = (float) ((x1 - centerX) * Math.cos(angle) - (y1 - centerY) * Math.sin(angle) + centerX);
        float y1r = (float) ((x1 - centerX) * Math.sin(angle) + (y1 - centerY) * Math.cos(angle) + centerY);

        float x2r = (float) ((x2 - centerX) * Math.cos(angle) - (y2 - centerY) * Math.sin(angle) + centerX);
        float y2r = (float) ((x2 - centerX) * Math.sin(angle) + (y2 - centerY) * Math.cos(angle) + centerY);

        float x3r = (float) ((x3 - centerX) * Math.cos(angle) - (y3 - centerY) * Math.sin(angle) + centerX);
        float y3r = (float) ((x3 - centerX) * Math.sin(angle) + (y3 - centerY) * Math.cos(angle) + centerY);

        float[] results = {x1r,y1r,x2r,y2r,x3r,y3r};
        return results;
    }

    public double getMax(ArrayList<Double> list){
        double max = 0.0;
        int index = 0;
        for (double value:list){
            if (value > max) {
                max = value;
                index = list.indexOf(value);
            }
        }
        return max;
    }

    public void printMax(ArrayList<Double> list,double threshold){
        for (double value : list){
            if (value > threshold)
                System.out.println(value+" at "+list.indexOf(value));
        }
    }

}
