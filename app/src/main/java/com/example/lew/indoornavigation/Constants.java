package com.example.lew.indoornavigation;

/**
 * Created by Lew on 10/4/2017.
 */
public class Constants {

    public static final float DATA_SAMPLING_PERIOD = 0.01f * 1000000000f;
    public static final float DATA_PROCESSING_PERIOD = 1f * 1000000000f;
    public static final int MAX_SIZE_LIST = 3000;
    public static final float DATA_SAMPLING_PERIOD_SEC = DATA_SAMPLING_PERIOD/1000000000f;
    public static final String HEIGHT_PREFERENCES = "height";
    public static final String HEIGHT_STRING = "height";
    public static final String WARNING = "Please enter a value for your height!";
    public static final float FLOOR_PRESSURE_DIFFERENCE = 0.3f;
    public static final int PRESSURE_LIST_SIZE = 40;
    public static final int WAP_MAX_RSSI = -45;
    public static final double BEARING_ADJUSTMENT_TOLERANCE = 70;
    public static final int ZOOM_SCALE = 2;

}
