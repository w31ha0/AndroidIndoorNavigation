package com.example.lew.indoornavigation;

/**
 * Created by Lew on 2/5/2017.
 */
public class IndoorMap {

    private int[][] wall_corners;
    private int WIDTH_MAP;
    private int HEIGHT_MAP;
    private int basePositionX;
    private int basePositionY;
    private int iconSizecm;
    private float pixelTocm;
    private int drawable;
    private int building_id;
    private int floor;

    public int getWIDTH_MAP() {
        return WIDTH_MAP;
    }

    public void setWIDTH_MAP(int WIDTH_MAP) {
        this.WIDTH_MAP = WIDTH_MAP;
    }

    public int[][] getWall_corners() {
        return wall_corners;
    }

    public void setWall_corners(int[][] wall_corners) {
        this.wall_corners = wall_corners;
    }

    public int getHEIGHT_MAP() {
        return HEIGHT_MAP;
    }

    public void setHEIGHT_MAP(int HEIGHT_MAP) {
        this.HEIGHT_MAP = HEIGHT_MAP;
    }

    public int getBasePositionX() {
        return basePositionX;
    }

    public void setBasePositionX(int basePositionX) {
        this.basePositionX = basePositionX;
    }

    public int getBasePositionY() {
        return basePositionY;
    }

    public void setBasePositionY(int basePositionY) {
        this.basePositionY = basePositionY;
    }

    public int getIconSizecm() {
        return iconSizecm;
    }

    public void setIconSizecm(int iconSizecm) {
        this.iconSizecm = iconSizecm;
    }

    public float getPixelTocm() {
        return pixelTocm;
    }

    public void setPixelTocm(float pixelTocm) {
        this.pixelTocm = pixelTocm;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public int getBuilding_id() {
        return building_id;
    }

    public void setBuilding_id(int building_id) {
        this.building_id = building_id;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

}
