package ru.zkir.mp2mp.taskgeocoder;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 02.02.14
 * Time: 13:13
 * To change this template use File | Settings | File Templates.
 */
public class OsmNode{
  String id;
  double lat;
  double lon;
  java.util.HashMap<String,String> tags;

  OsmNode(String aRef, String aLat,String aLon)
  {
    id=aRef;
    lat=Double.parseDouble(aLat);
    lon=Double.parseDouble(aLon);
    tags=new java.util.HashMap<String,String>();
  }
  public void addTag(String strKey, String strValue)
  {
    tags.put(strKey,strValue);
  }
}
