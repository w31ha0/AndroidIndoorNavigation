package com.example.lew.indoornavigation;

/**
 * Created by Lew on 2/5/2017.
 */
public class BluetoothDatabase {

    public static IndoorMap getMapFromUUIDAndFloor(String UUID, int floor){
        switch (UUID){
            case "e6bf275e-0bb3-43e5-bf88-517f13a5a162":
                if (floor == 0)
                    return new MapTemplates().new Map1();
                else if (floor == 1)
                    return new MapTemplates().new Map2();
            default:
                return null;
        }
    }

}
