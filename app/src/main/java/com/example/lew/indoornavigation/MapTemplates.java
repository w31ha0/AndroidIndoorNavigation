package com.example.lew.indoornavigation;

/**
 * Created by Lew on 30/4/2017.
 */
public class MapTemplates {

     class Map1{
         private final int[][] wall_corners_ = {{660,662},{940,662},{940,73},{1156,73},{1156,606},{1237,606},{1237,704},{1154,704},{1154,802},{1111,802},{1043,869}
                 ,{603,869},{603,701},{660,701}};
         private final int WIDTH_MAP_ = 1279;
         private final int HEIGHT_MAP_ = 1010;
         private final int basePositionX_ = 1056; //initial coordinates in cm
         private final int basePositionY_ = 730;
         private final int iconSizecm = 100;
         private float pixelTocm = 1000/247;
         private int drawable = R.drawable.map1;

         public int[][] getWall_corners_() {
             return wall_corners_;
         }

         public int getWIDTH_MAP_() {
             return WIDTH_MAP_;
         }

         public int getHEIGHT_MAP_() {
             return HEIGHT_MAP_;
         }

         public int getBasePositionX_() {
             return basePositionX_;
         }

         public int getBasePositionY_() {
             return basePositionY_;
         }

         public int getIconSizecm() {
             return iconSizecm;
         }

         public float getPixelTocm() {
             return pixelTocm;
         }

         public int getDrawable() {
             return drawable;
         }

     }

}
