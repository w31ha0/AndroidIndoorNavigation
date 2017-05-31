package com.example.lew.indoornavigation;

/**
 * Created by Lew on 30/4/2017.
 */
public class MapTemplates{

     class Map0 extends IndoorMap{

         public Map0(){
             int[][] walls = {{660,662},{940,662},{940,73},{1156,73},{1156,606},{1237,606},{1237,704},{1154,704},{1154,802},{1111,802},{1043,869}
                 ,{603,869},{603,701},{660,701}};
             int[][][] allWalls = {walls};
             int[][] waps = {{324,174},{768,375},{407,632}};
             setAllWalls(allWalls);
             setWaps(waps);
             setWIDTH_MAP(1279);
             setHEIGHT_MAP(1010);
             setBasePositionX(1056);
             setBasePositionY(730);
             setIconSizecm(100);
             setPixelTocm(1000/247);
             setDrawable(R.drawable.map0);
             setBuilding_id(0);
             setFloor(0);
             setInitialBearing(0);
         }

     }

    class Map1 extends IndoorMap{

        public Map1(){
            int[][] walls = {{398,878},{398,913},{191,913},{440,913},{440,964},{132,964},
                    {132,730},{206,730},{206,518},{237,518},{237,809},{195,809},{195,878}};
            int[][][] allWalls = {walls};
            int[][] waps = {{146,753},{292,741},{397,604},{277,502}};
            setAllWalls(allWalls);
            setWaps(waps);
            setWIDTH_MAP(1194);
            setHEIGHT_MAP(970);
            setBasePositionX(353);
            setBasePositionY(893);
            setIconSizecm(100);
            setPixelTocm(1000/247);
            setDrawable(R.drawable.map1);
            setBuilding_id(0);
            setFloor(1);
            setInitialBearing(0);
        }
    }

    class Map2 extends IndoorMap{

        public Map2(){
            int[][] walls = {{226,967},{226,171},{1017,171},{1017,211},{276,211},{276,967}};
            int[][][] allWalls = {walls};
            int[][] waps = {{458,195}};
            setAllWalls(allWalls);
            setWaps(waps);
            setWIDTH_MAP(1318);
            setHEIGHT_MAP(1021);
            setBasePositionX(250);
            setBasePositionY(598);
            setIconSizecm(100);
            setPixelTocm(1000/247);
            setDrawable(R.drawable.map2);
            setBuilding_id(0);
            setFloor(2);
            setInitialBearing(0);
        }
    }

    class Map4 extends IndoorMap{

        public Map4(){
            int[][] walls = {{140,908},{140,146},{649,146},{557,146},{557,97},{650,97},{650,44},{726,44},{726,176},{176,176},{176,787},{215,787},{215,908}};
            int[][][] allWalls = {walls};
            int[][] waps = {{176,866},{160,629},{160,278},{350,155}};
            setAllWalls(allWalls);
            setWaps(waps);
            setWIDTH_MAP(1010);
            setHEIGHT_MAP(938);
            setBasePositionX(593);
            setBasePositionY(121);
            setIconSizecm(75);
            setPixelTocm(1000/247);
            setDrawable(R.drawable.map4);
            setBuilding_id(0);
            setFloor(4);
            setInitialBearing(180);
        }
    }

    class Map5 extends IndoorMap{

        public Map5(){
            int[][] walls = {{142,976},{142,175},{777,175},{777,777},{868,777},{868,976}};
            int [][] walls2 = {{216,942},{216,820},{180,820},{180,211},{745,211},{745,815},{830,815},{830,942}};
            int[][][] allWalls = {walls,walls2};
            int[][] waps = {{160,757},{160,347},{414,190},{758,253},{758,595},{845,912},{605,955}};
            setAllWalls(allWalls);
            setWaps(waps);
            setWIDTH_MAP(1011);
            setHEIGHT_MAP(1010);
            setBasePositionX(193);
            setBasePositionY(888);
            setIconSizecm(75);
            setPixelTocm(1000/247);
            setDrawable(R.drawable.map5);
            setBuilding_id(0);
            setFloor(5);
            setInitialBearing(270);
        }
    }

    public static IndoorMap getMapFromFloor(String UUID,int floor){
        if (UUID == "e6bf275e-0bb3-43e5-bf88-517f13a5a162"){
            switch(floor){
                case 0:
                    return new MapTemplates().new Map0();
                case 1:
                    return new MapTemplates().new Map1();
                default:
                    return null;
            }
        }
        return null;
    }

}
