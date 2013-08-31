/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.zkir.mp2mp.taskextsimp;

/**
 *
 * @author freeExec
 */
public class Bbox {

    // Conts

    //Conversion degrees to radians and back
    public static final double DEGTORAD = 1.74532925199433E-02;
    public static final double RADTODEG = 57.2957795130823;
    //WGS84 datum
    public static final int DATUM_R_EQUAT = 6378137;
    public static final double DATUM_R_POLAR = 6356752.3142;
    //'for expanding bbox
    public static final int DATUM_R_OVER = 6380000;

    public double lat_min;
    public double lat_max;
    public double lon_min;
    public double lon_max;

    public Bbox() {

    }

    public Bbox(double lat_min, double lat_max, double lon_min, double lon_max) {
        this.lat_min = lat_min;
        this.lat_max = lat_max;
        this.lon_min = lon_min;
        this.lon_max = lon_max;
    }

    //Expand bounding box by distance in metres
    public static Bbox expandBbox(Bbox bbox1, double dist) { // TODO: Use of ByRef founded
        double cos1 = 0;
        double cos2 = 0;
        double dist_angle = 0;
        //distance in degrees of latitude
        dist_angle = RADTODEG * dist / DATUM_R_OVER;
        bbox1.lat_min -= dist_angle;
        bbox1.lat_max += dist_angle;

        cos1 = Math.cos(bbox1.lat_min * DEGTORAD);
        cos2 = Math.cos(bbox1.lat_max * DEGTORAD);
        //'smallest cos() - further from equator
        if (cos2 < cos1) { cos1 = cos2; }
        //'distance in degrees of longtitue
        dist_angle = dist_angle / cos1;
        bbox1.lon_min -= dist_angle;
        bbox1.lon_max += dist_angle;
        //TODO: fix (not safe to 180/-180 edge)

        return bbox1;
    }
}
