package ru.zkir.mp2mp.taskgeocoder;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 02.02.14
 * Time: 13:12
 * To change this template use File | Settings | File Templates.
 */
public class  OsmWay{
  String id;
  ArrayList<String> nodeRefs;
  OsmWay()
  {

    nodeRefs=new ArrayList<String>();
  }
}
