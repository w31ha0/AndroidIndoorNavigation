package com.example.lew.indoornavigation;

import java.util.ArrayList;

/**
 * Created by Lew on 10/4/2017.
 */
public class Utils {

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
