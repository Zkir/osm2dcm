package ru.zkir.mp2mp.taskgeocoder;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 02.02.14
 * Time: 13:12
 * To change this template use File | Settings | File Templates.
 */
public class OsmRelation
{
  public String id;
  public java.util.HashMap<String,String> tags;
  ArrayList<String> members;
  public OsmRelation()
  {
    tags=new java.util.HashMap<String,String>();
    members=new ArrayList <String>();
  }

  public void addTag(String strKey, String strValue)
  {
    tags.put(strKey,strValue);
  }
  public String getTag(String strKey)
  { String strTag;
    strTag=tags.get(strKey);
    if (strTag!=null)
      {return strTag;}
    else
      {return "";}
  }
  public void addMember(String strRef,String strRole)
  {
    members.add(strRef);
  }
}
