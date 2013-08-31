/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.zkir.mp2mp.taskextsimp;

/**
 *
 * @author freeExec
 */
public class Highway {
    // Consts

    //OSM highway main types
    public static final int HIGHWAY_MOTORWAY = 0;
    public static final int HIGHWAY_MOTORWAY_LINK = 1;
    public static final int HIGHWAY_TRUNK = 2;
    public static final int HIGHWAY_TRUNK_LINK = 3;
    public static final int HIGHWAY_PRIMARY = 4;
    public static final int HIGHWAY_PRIMARY_LINK = 5;
    public static final int HIGHWAY_SECONDARY = 6;
    public static final int HIGHWAY_SECONDARY_LINK = 7;
    public static final int HIGHWAY_TERTIARY = 8;
    public static final int HIGHWAY_TERTIARY_LINK = 9;

    public static final int HIGHWAY_FERRY = 10;
    //OSM highway minor types (should not occur)
    public static final int HIGHWAY_LIVING_STREET = 12;
    public static final int HIGHWAY_RESIDENTIAL = 14;
    public static final int HIGHWAY_UNCLASSIFIED = 16;
    public static final int HIGHWAY_SERVICE = 18;
    public static final int HIGHWAY_TRACK = 20;
    public static final int HIGHWAY_OTHER = 22;
    public static final int HIGHWAY_UNKNOWN = 24;
    public static final int HIGHWAY_UNSPECIFIED = 26;

    //Masks
    //all links
    public static final int HIGHWAY_MASK_LINK = 1;
    //get main type (removes _link)
    public static final int HIGHWAY_MASK_MAIN = 254;

    
        //Parse OSM highway class to our own constants
    public static byte getHighwayType(String text) {
        byte _rtn = 0;
        // предположительно trim лишний, т.к. еще обрезаеться до входа
        switch (text.trim().toLowerCase()) {
            case  "primary":
                _rtn = HIGHWAY_PRIMARY;
                break;
            case  "primary_link":
                _rtn = HIGHWAY_PRIMARY_LINK;
                break;
            case  "secondary":
                _rtn = HIGHWAY_SECONDARY;
                break;
            case  "secondary_link":
                _rtn = HIGHWAY_SECONDARY_LINK;
                break;
            case  "tertiary":
                _rtn = HIGHWAY_TERTIARY;
                break;
            case  "tertiary_link":
                _rtn = HIGHWAY_TERTIARY_LINK;
                break;
            case  "motorway":
                _rtn = HIGHWAY_MOTORWAY;
                break;
            case  "motorway_link":
                _rtn = HIGHWAY_MOTORWAY_LINK;
                break;
            case  "trunk":
                _rtn = HIGHWAY_TRUNK;
                break;
            case  "trunk_link":
                _rtn = HIGHWAY_TRUNK_LINK;
                break;
            case  "living_street":
                _rtn = HIGHWAY_LIVING_STREET;
                break;
            case  "residential":
                _rtn = HIGHWAY_RESIDENTIAL;
                break;
            case  "unclassified":
                _rtn = HIGHWAY_UNCLASSIFIED;
                break;
            case  "service":
                _rtn = HIGHWAY_SERVICE;
                break;
            case  "track":
                _rtn = HIGHWAY_TRACK;
                break;
            case  "road":
                _rtn = HIGHWAY_UNKNOWN;
                break;
            default:
                _rtn = HIGHWAY_OTHER;
                break;
        }
        return _rtn;
    }

    //Convert constants to polyline type
    public static int getType_by_Highway(int highwayType) {
        int _rtn = 0;
        switch (highwayType) {
            case  HIGHWAY_MOTORWAY:
                _rtn = 1;
                break;
            case  HIGHWAY_MOTORWAY_LINK:
                _rtn = 9;
                break;
            case  HIGHWAY_TRUNK:
                _rtn = 1;
                break;
            case  HIGHWAY_TRUNK_LINK:
                _rtn = 9;
                break;
            case  HIGHWAY_PRIMARY:
                _rtn = 2;
                break;
            case  HIGHWAY_PRIMARY_LINK:
                _rtn = 8;
                break;
            case  HIGHWAY_SECONDARY:
                _rtn = 3;
                break;
            case  HIGHWAY_SECONDARY_LINK:
                _rtn = 8;
                break;
            case  HIGHWAY_TERTIARY:
                _rtn = 3;
                break;
            case  HIGHWAY_TERTIARY_LINK:
                _rtn = 8;
                break;
            case  HIGHWAY_LIVING_STREET:
                _rtn = 6;
                break;
            case  HIGHWAY_RESIDENTIAL:
                _rtn = 6;
                break;
            case  HIGHWAY_UNCLASSIFIED:
                _rtn = 3;
                break;
            case  HIGHWAY_SERVICE:
                _rtn = 7;
                break;
            case  HIGHWAY_TRACK:
                _rtn = 10;
                break;
            case  HIGHWAY_FERRY:
                _rtn = 27;
                break;
            case  HIGHWAY_UNKNOWN:
                _rtn = 3;
                break;
            case  HIGHWAY_OTHER:
                _rtn = 3;
                break;

            default:
                _rtn = 3;
                break;
        }
        return _rtn;
    }

    //Convert constants to top level for visibility
    public static int getTopLevel_by_Highway(int highwayType) {
        int _rtn = 0;
        switch (highwayType) {
            case  HIGHWAY_MOTORWAY:
                _rtn = 6;
                break;
            case  HIGHWAY_MOTORWAY_LINK:
                _rtn = 2;
                break;
            case  HIGHWAY_TRUNK:
                _rtn = 6;
                break;
            case  HIGHWAY_TRUNK_LINK:
                _rtn = 2;
                break;
            case  HIGHWAY_PRIMARY:
                _rtn = 5;
                break;
            case  HIGHWAY_PRIMARY_LINK:
                _rtn = 2;
                break;
            case  HIGHWAY_SECONDARY:
                _rtn = 4;
                break;
            case  HIGHWAY_SECONDARY_LINK:
                _rtn = 2;
                break;
            case  HIGHWAY_TERTIARY:
                _rtn = 3;
                break;
            case  HIGHWAY_TERTIARY_LINK:
                _rtn = 2;
                break;
            case  HIGHWAY_LIVING_STREET:
                _rtn = 2;
                break;
            case  HIGHWAY_RESIDENTIAL:
                _rtn = 2;
                break;
            case  HIGHWAY_UNCLASSIFIED:
                _rtn = 2;
                break;
            case  HIGHWAY_SERVICE:
                _rtn = 2;
                break;
            case  HIGHWAY_TRACK:
                _rtn = 2;
                break;
            case  HIGHWAY_UNKNOWN:
                _rtn = 2;
                break;
            case  HIGHWAY_OTHER:
                _rtn = 2;
                break;
            default:
                _rtn = 2;
                break;
        }
        return _rtn;
    }

    //Convert constants to road class
    public static int getClass_by_Highway(int highwayType) {
        int _rtn = 0;
        switch (highwayType) {
            case  HIGHWAY_MOTORWAY:
                _rtn = 4;
                break;
            case  HIGHWAY_MOTORWAY_LINK:
                _rtn = 4;
                break;
            case  HIGHWAY_TRUNK:
                _rtn = 4;
                break;
            case  HIGHWAY_TRUNK_LINK:
                _rtn = 4;
                break;
            case  HIGHWAY_PRIMARY:
                _rtn = 3;
                break;
            case  HIGHWAY_PRIMARY_LINK:
                _rtn = 3;
                break;
            case  HIGHWAY_SECONDARY:
                _rtn = 2;
                break;
            case  HIGHWAY_SECONDARY_LINK:
                _rtn = 2;
                break;
            case  HIGHWAY_TERTIARY:
                _rtn = 1;
                break;
            case  HIGHWAY_TERTIARY_LINK:
                _rtn = 1;
                break;
            case  HIGHWAY_LIVING_STREET:
                _rtn = 0;
                break;
            case  HIGHWAY_RESIDENTIAL:
                _rtn = 0;
                break;
            case  HIGHWAY_UNCLASSIFIED:
                _rtn = 1;
                break;
            case  HIGHWAY_SERVICE:
                _rtn = 0;
                break;
            case  HIGHWAY_TRACK:
                _rtn = 0;
                break;
            case  HIGHWAY_UNKNOWN:
                _rtn = 0;
                break;
            case  HIGHWAY_OTHER:
                _rtn = 0;
                break;
            default:
                _rtn = 0;
                break;
        }
        return _rtn;
    }

    //Estimate speed class of road by histogram
    public static int estimateSpeedByHistogram(int[] SpeedHistogram) {
        int total = 0;
        int _ret;
        //call total sum
        for (int shI: SpeedHistogram) {
            total += shI;
        }

        //should never happens
        if (total == 0) { return 3; }        //default speedclass

        //find speedclass with 90% coverage
        _ret = 0;
        for (int shI: SpeedHistogram) {
            if (shI > total * 0.9) {
                //90% of chain have this speedclass
                return _ret;
            }
            _ret++;
        }

        //no 90%

        //find minimum speedclass with 40% coverage
        _ret = 0;
        for (int shI: SpeedHistogram) {
            if (shI > total * 0.4) {
                //40% of chain have this speedclass
                return _ret;
            }
            _ret++;
        }

        //no 40% (very much alike will not happens)

        //find minimum speedclass with 10% coverage
        _ret = 0;
        for (int shI: SpeedHistogram) {
            if (shI > total * 0.1) {
                //10% of chain have this speedclass
                return _ret;
            }
            _ret++;
        }

        //no 10% (almost impossible)

        //find minimum speedclass with >0 coverage
        _ret = 0;
        for (int shI: SpeedHistogram) {
            if (shI > 0) {
                return _ret;
            }
            _ret++;
        }

        return 3;
    }

    //Compare roadtype-s
    //return: 1 - type1 have higher priority, -1 - type2 have higher priority, 0 - equal
    public static int compareRoadtype(int type1, int type2) {
        int _rtn = 0;
        if (type1 == type2) {
            //just equal
            _rtn = 0;
        }
        else if ((type1 & HIGHWAY_MASK_LINK) != 0  && (type2 & HIGHWAY_MASK_LINK) == 0) {
            //type1 is link, type2 is not
            _rtn = -1;
        }
        else if ((type1 & HIGHWAY_MASK_LINK) == 0  && (type2 & HIGHWAY_MASK_LINK) != 0) {
            //type2 is link, type1 is not
            _rtn = 1;
        }
        else if ((type1 & HIGHWAY_MASK_MAIN) < (type2 & HIGHWAY_MASK_MAIN)) {
            //type1 is less numerically - higher
            _rtn = 1;
        }
        else if ((type1 & HIGHWAY_MASK_MAIN) > (type2 & HIGHWAY_MASK_MAIN)) {
            //type1 is higher numerically - less
            _rtn = -1;
        }
        else {
            //should not happen
            _rtn = 0;
        }
        return _rtn;
    }

}
