/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.zkir.mp2mp.taskextsimp;

/**
 *
 * @author freeExec
 */
public class XYZ {

    public double x, y, z;

    //Convert (lat,lon) to (x,y,z) on reference ellipsoid
    public static XYZ latLonToXYZ(double lat, double lon) {
        XYZ result = new XYZ();
        double r = 0;
        r = Bbox.DATUM_R_EQUAT * Math.cos(lat * Bbox.DEGTORAD);
        result.z = Bbox.DATUM_R_POLAR * Math.sin(lat * Bbox.DEGTORAD);
        result.x = r * Math.sin(lon * Bbox.DEGTORAD);
        result.y = r * Math.cos(lon * Bbox.DEGTORAD);
        return result;
    }

    public void Sub(XYZ b) {
        this.x -= b.x;
        this.y -= b.y;
        this.z -= b.z;
    }

    public static XYZ Sub(XYZ a, XYZ b) {
        a.x -= b.x;
        a.y -= b.y;
        a.z -= b.z;
        return a;
    }
}
