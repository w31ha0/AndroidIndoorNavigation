package com.example.lew.indoornavigation;

/**
 * Created by Lew on 30/4/2017.
 */
public class MapTemplates{

     class Map1 extends IndoorMap{

         public Map1(){
             int[][] walls = {{660,662},{940,662},{940,73},{1156,73},{1156,606},{1237,606},{1237,704},{1154,704},{1154,802},{1111,802},{1043,869}
                 ,{603,869},{603,701},{660,701}};
             setWall_corners(walls);
             setWIDTH_MAP(1279);
             setHEIGHT_MAP(1010);
             setBasePositionX(1056);
             setBasePositionY(730);
             setIconSizecm(100);
             setPixelTocm(1000/247);
             setDrawable(R.drawable.map1);
             setBuilding_id(0);
             setFloor(0);
         }

     }

    class Map2 extends IndoorMap{

        public Map2(){
            int[][] walls = {{398,878},{398,913},{191,913},{440,913},{440,964},{132,964},
                    {132,730},{206,730},{206,518},{237,518},{237,809},{195,809},{195,878}};
            setWall_corners(walls);
            setWIDTH_MAP(1194);
            setHEIGHT_MAP(970);
            setBasePositionX(353);
            setBasePositionY(893);
            setIconSizecm(100);
            setPixelTocm(1000/247);
            setDrawable(R.drawable.map2);
            setBuilding_id(0);
            setFloor(1);
        }
    }

    public static IndoorMap getMapFromFloor(String UUID,int floor){
        if (UUID == "e6bf275e-0bb3-43e5-bf88-517f13a5a162"){
            switch(floor){
                case 0:
                    return new MapTemplates().new Map1();
                case 1:
                    return new MapTemplates().new Map2();
                default:
                    return null;
            }
        }
        return null;
    }

}
