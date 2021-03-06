package ru.zkir.mp2mp.taskgeocoder;

import java.util.ArrayList;
import java.util.Date;
import net.sf.junidecode.Junidecode;
import ru.zkir.mp2mp.core.*;


/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 15.01.13
 * Time: 21:03
 * To change this template use File | Settings | File Templates.
 */
public class GeocoderTask {
  /*Что нужно сделать
    1.Подготовить геокодер
    1.1 Прочитать границы из осм файла. Это конечно специально обработанный осм файл, содержащий только границы
    1.2 Превратить их в полигоны, снабженные должной атрибутивной инфой
    2 собственно геокодировать.
    2.2 Объекты, снабженные признаками адреса (дома, улицы и пои), прогонять через геокодер и получать "Город"

  */

  public void execute(MpData mpData, TaskInfo taskInfo) throws MPParseException
  {
    String strSrcFileName;
    String strCountryCode;

    strSrcFileName=taskInfo.parameters.get("src");
    strCountryCode=taskInfo.parameters.get("mapcode").substring(0,2);

    Date dtProcessEnd,  dtProcessStart;

    System.out.println("Task: geocoder");
    System.out.println(" source file: " + strSrcFileName);
    System.out.println(" country code: " + strCountryCode);

    GeocoderConfig geocoderConfig=new GeocoderConfig(strCountryCode);


    if (geocoderConfig.levelsForCity.length==0 )
    {
      System.out.println(" No rules are defined for country "+strCountryCode);
      return;
    }

    Geocoder geocoder;
    geocoder=new Geocoder();

    geocoder.loadAddressRegionsFromOsmFile(strSrcFileName,
                                           geocoderConfig.levelsForCity,
                                           geocoderConfig.levelsForRegion,
                                           geocoderConfig.language,
                                           geocoderConfig.blnHamletsExcluded);

    System.out.println("Address regions loaded");
    System.out.println("");

    dtProcessStart =new Date();
    /*
    //System.out.println(geocoder.getCityName(47.858771, 15.3129245));
    System.out.println(geocoder.getCityName(47.830619659869505, 15.348480274071381));
    */

    MpSection ms;
    int intObjectsWithAddress=0;
    int intCitySet=0;
    int intRegionSet=0;
    mpData.moveFirst();
    while (!mpData.eof() )
    {
      ms=mpData.getCurrentSection();
      if(
             /* ms.GetAttributeValue("CityName").equals("")&&  */
             ((!ms.GetAttributeValue("HouseNumber").equals(""))||
               !ms.GetAttributeValue("StreetDesc").equals("")||
               !ms.GetAttributeValue("CityName").equals("")
             ||ms.SectionType.equals("[POI]") )
        )
      {
        intObjectsWithAddress++;
        String strCityName;
        String strRegionName;
        //Здесь мы получаем координаты объекта.
        // TODO: Для линий еще неплохо бы проверять точки на 20% и 80%, а не на крайних.
        double[] bbox;
        double lat, lon;
        bbox=ms.CalculateFirstLast();

        lat=bbox[0];
        lon=bbox[1];
        //Тут грязный хак. Подразумевается, что osm2mp уже проставил города из полигональных place
        if (geocoderConfig.levelsForCity[0].equals("CITY_POLYGON") && (!ms.GetAttributeValue("CityName").equals("")) )
        {
          strCityName = ms.GetAttributeValue("CityName");
        }
        else
        {
          //Город ("обычно это муниципалитет")
          strCityName= geocoder.getCityName(lat,lon, geocoderConfig.levelsForCity);
        }
        strCityName=normalizeName(strCityName,geocoderConfig);

        if (!strCityName.equals(""))
        {
          ms.SetAttributeValue("CityName",strCityName);
          intCitySet++;
        }

        //Регион ("провинция")
        strRegionName= geocoder.getCityName(lat,lon,geocoderConfig.levelsForRegion);
        strRegionName=normalizeName(strRegionName,geocoderConfig);

        if (!strRegionName.equals(""))
        {
          ms.SetAttributeValue("RegionName",strRegionName);
          intRegionSet++;
        }

      }
      mpData.moveNext();
    }
    dtProcessEnd=new Date();
    System.out.println("Объектов, для которых нужен адрес: "+ intObjectsWithAddress);
    System.out.println("Из них обработано (проставлен город)  : "+ intCitySet );
    System.out.println("Из них обработано (проставлен 'регион') : "+ intRegionSet );
    System.out.println( "Time used: "+ Long.toString((dtProcessEnd.getTime()-dtProcessStart.getTime())/1000)+ " s" );
  }

  private String normalizeName(String strName, GeocoderConfig geocoderConfig)
  {

    //Удаление лишних слов
    int i;
    strName=strName.trim();
    for (i=0;i<geocoderConfig.redundantWords.length;i++)
    {
      if (strName.toLowerCase().startsWith(geocoderConfig.redundantWords[i].toLowerCase()+" ") )
      {
        strName=strName.substring(geocoderConfig.redundantWords[i].length());
      }

      if (strName.toLowerCase().endsWith(" "+geocoderConfig.redundantWords[i].toLowerCase()) )
      {
        strName=strName.substring(0,strName.length()-geocoderConfig.redundantWords[i].length());
      }
    }

    //Перевод в 26 букв латинского алфавита
    if (geocoderConfig.blnPerformTransliteration )
    {
      strName = Junidecode.unidecode(strName);
    }
    return strName.trim();
  }

}

//Геокодер - в сущности, это множество полинонов границ, снабженных названиями.
//Нас интересуют только "города"
class Geocoder{
  ArrayList<AddressRegion> addressRegions;
  ArrayList<CityPoint> cityPoints;
  Geocoder()
  {
    addressRegions=new ArrayList<AddressRegion>();
    cityPoints=new   ArrayList<CityPoint>();
  }

  //Единственная нужная на данный момент функция геокодера
  //определение города
  double sqr (double x)
  {return x*x;}
  double dist(double lat1, double lon1, double lat2,  double lon2)
  {
    return sqr(lat1-lat2)+sqr(lon1-lon2);
  }
  String getCityName(double lat, double lon, String[] levelsForCity)
  {
    String strName="";

    for (int k=0;k<levelsForCity.length;k++ )
    {
      if (!levelsForCity[k].equals("NEAREST_CITY_POINT"))
      {
        //Ищем в списке полигональных адресных регионов
        //Простой линейный поиск
        for (int i=0;i<addressRegions.size();i++ )
        {
          AddressRegion  currRegion;
          currRegion=addressRegions.get(i);
          if(currRegion.addrLevel.equals(levelsForCity[k])  && currRegion.polygon.isInside(lat,lon) )
          {
            strName=currRegion.name;
            return strName;
          }
        }
      }else
      {
        //Ищем в списке точечных городов
        if(cityPoints.size()>0)
        {
          double r2min;
          double r2;
          r2min=dist(lat, lon, cityPoints.get(0).lat,cityPoints.get(0).lon);
          strName=cityPoints.get(0).name;

          //А сейчас поищем точечные города.
          for(int i=1;i<cityPoints.size();i++)
          {
            r2=dist(lat,lon,cityPoints.get(i).lat,cityPoints.get(i).lon);
            if (r2<r2min)
            {
              r2min=r2;
              strName=cityPoints.get(i).name;
            }
          }
        }
        if(!strName.equals(""))
        { return strName; }
      }
    }

    return strName;
  }

  void loadAddressRegionsFromOsmFile(String strSrcFileName,String[] levelsForCity, String[] levelsForRegion, String strLanCode, boolean blnHamletsExcluded)
  {
    OsmParser osmParser;
    osmParser=new OsmParser(strSrcFileName);
    OsmRelation currentRelation;
    AddressRegion addressRegion;
    int k;
    for ( k=0;k<osmParser.myParser.relations.size();k++)
    rel:{
    //TODO Цикл по релейшенам
    currentRelation=osmParser.myParser.relations.get(k);
    // System.out.println("admin_level="+currentRelation.tags.get("admin_level") );
    // System.out.println("name="+currentRelation.tags.get("name") );

    boolean blnNeedForCitySearch;
    blnNeedForCitySearch=false;
    for(int i=0;i<levelsForCity.length;i++)
      {
        blnNeedForCitySearch=blnNeedForCitySearch || currentRelation.tags.get("admin_level").equals(levelsForCity[i]);
      }
    for(int i=0;i<levelsForRegion.length;i++)
      {
        blnNeedForCitySearch=blnNeedForCitySearch || currentRelation.tags.get("admin_level").equals(levelsForRegion[i]);
      }

    if (!blnNeedForCitySearch )
    {
        //System.out.println("Отношение "+currentRelation.tags.get("name") + " нах не годится, у него уровень "+ currentRelation.tags.get("admin_level")  );
        break rel;
    }

    addressRegion=new AddressRegion();
    addressRegion.name="";
    if (!strLanCode.equals("") )
    {
      addressRegion.name=currentRelation.tags.get("name"+":"+strLanCode);
    }
    if(addressRegion.name==null||addressRegion.name.equals("")) //Если все еще пусто
    {
      addressRegion.name=currentRelation.tags.get("name");
    }

    if (addressRegion.name==null)
    {
      System.out.println("relation without valid name " + currentRelation.id );
      addressRegion.name="";
    }
    addressRegion.addrLevel=  currentRelation.tags.get("admin_level");

    for(int i=0; i<currentRelation.members.size();i++ )
    {
      String wayRef=currentRelation.members.get(i);
      //System.out.println(wayRef );

      OsmWay  aWay;
      if (!osmParser.myParser.ways.containsKey(wayRef) )
      {
        //System.out.println("Отношение "+currentRelation.tags.get("name") + " нах не годится, нет вея "+wayRef  );
        break rel;
      }
      aWay=osmParser.myParser.ways.get(wayRef);
      //System.out.println("way "+aWay.id+ " "+aWay.nodeRefs.get(0) );
      for (int j=1; j<aWay.nodeRefs.size();j++ )
      {
        OsmNode aNode0;
        OsmNode aNode1;

        aNode0=osmParser.myParser.nodes.get(aWay.nodeRefs.get(j-1));
        aNode1=osmParser.myParser.nodes.get(aWay.nodeRefs.get(j));
        if (aNode0==null )
        {
          //System.out.println("вей "+wayRef + " нах не годится, нет ноды "+aWay.nodeRefs.get(j-1)  );
          break rel;
        }
        if (aNode1==null )
        {
          //System.out.println("вей "+wayRef + " нах не годится, нет ноды "+aWay.nodeRefs.get(j)  );
          break rel;
        }

        //System.out.println(" node "+aNode.id+" "+aNode.lat + " "+aNode.lon  );
        BorderSegment bs;
        bs=new BorderSegment(new Point(aNode0.lat,aNode0.lon),new Point(aNode1.lat,aNode1.lon));
        addressRegion.polygon.borderSegments.add(bs );

      }
    }

    if (addressRegion.polygon.borderSegments.size()==0)
    {
      System.out.println("Warning! Relation without valid members: "+addressRegion.name);
    }
    else
    {
      addressRegions.add(addressRegion);
    }
    }
    System.out.println(k+ " relation(s) processed");
    System.out.println(addressRegions.size()+ " relation(s) loaded");

    //Извлечем точечные НП
    java.util.Iterator nodes=osmParser.myParser.nodes.values().iterator();
    while (nodes.hasNext())
    {
      OsmNode theNode;
      theNode=(OsmNode)nodes.next();
      if (theNode.tags.containsKey("place")&&
          (theNode.tags.get("place").equals("city")||theNode.tags.get("place").equals("town")||
           theNode.tags.get("place").equals("village")||theNode.tags.get("place").equals("hamlet")))
      {
        if (!theNode.tags.containsKey("name") || theNode.tags.get("name").equals(""))
        {
          System.out.println("place node without name: "+ theNode.id );
          continue;
        }
        if (theNode.tags.get("place").equals("hamlet") && blnHamletsExcluded)
        {
          continue;
        }
        CityPoint cityPoint=new CityPoint();
        cityPoint.lat = theNode.lat;
        cityPoint.lon = theNode.lon;
        if (!strLanCode.equals(""))
        {
          cityPoint.name= theNode.tags.get("name"+":"+strLanCode);
        }
        if (cityPoint.name==null||cityPoint.name.equals(""))
        {
          cityPoint.name= theNode.tags.get("name");
        }
        cityPoint.placeTag =theNode.tags.get("place");
       /* if (!(cityPoint.placeTag.equals("city")||cityPoint.placeTag.equals("town")||
                cityPoint.placeTag.equals("village")||cityPoint.placeTag.equals("hamlet")))
          System.out.println(theNode.tags.get("place"));*/

        cityPoints.add(cityPoint);

      }
    }
    System.out.println(cityPoints.size() + " place points loaded");
  }




}

class AddressRegion{
  String name;
  Polygon polygon;
  String addrLevel;
  AddressRegion()
  {name="";
   polygon=new Polygon();}
}

class CityPoint
{
  String name;
  String placeTag;
  double lat;
  double lon;

}
