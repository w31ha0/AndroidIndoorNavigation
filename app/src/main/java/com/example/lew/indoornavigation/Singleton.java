package com.example.lew.indoornavigation;

/**
 * Created by Lew on 4/5/2017.
 */
public class Singleton {
    private static Singleton instance = null;
    private Boolean mapLoaded = false;

    protected Singleton() {
        // Exists only to defeat instantiation.
    }
    public static Singleton getInstance() {
        if(instance == null) {
            instance = new Singleton();
        }
        return instance;
    }

    public Boolean getMapLoaded(){
        return mapLoaded;
    }

    public void setMapLoaded(Boolean value){
        this.mapLoaded = value;
    }
}

