package ru.zkir.mp2mp.getboundarytask;

import ru.zkir.mp2mp.core.MPParseException;
import ru.zkir.mp2mp.core.MpData;
import ru.zkir.mp2mp.core.TaskInfo;
import ru.zkir.mp2mp.taskgeocoder.*;
import java.io.IOException;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 02.02.14
 * Time: 13:05
 * To change this template use File | Settings | File Templates.
 */
public class GetBoundaryTask {
  public void execute(MpData mpData, TaskInfo taskInfo) throws MPParseException,IOException,InterruptedException
  {
    String strSrcFileName;
    String strOutFileName;
    String strCountryCode;

    strSrcFileName=taskInfo.parameters.get("src");
    strCountryCode=taskInfo.parameters.get("mapcode");
    strOutFileName=taskInfo.parameters.get("outfile");

    Date dtProcessEnd,  dtProcessStart;

    System.out.println("Task: GetBoundary");
    System.out.println(" source file: " + strSrcFileName);
    System.out.println(" country code: " + strCountryCode);
    System.out.println(" poly file: " + strOutFileName);

    //найти и загрузить нужное отношение границы (в какую-то структуру данных)
    loadBoundaryRegionFromOsmFile(strSrcFileName, strCountryCode);
    //сохранить его в файл
  }

  void loadBoundaryRegionFromOsmFile(String strSrcFileName, String strMapCode)    throws IOException,InterruptedException
  {
    OsmParser osmParser;
    osmParser=new OsmParser(strSrcFileName);
    OsmRelation currentRelation;

    String strCountryCode;
    String strRegionCode;
    String strNutsCode;
    strCountryCode=strMapCode.substring(0,2);
    strRegionCode=strMapCode.substring(3,strMapCode.length());
    //AddressRegion addressRegion;
    int k;
    for ( k=0;k<osmParser.myParser.relations.size();k++)
      rel:{
        //TODO Цикл по релейшенам
        currentRelation=osmParser.myParser.relations.get(k);
        // System.out.println("admin_level="+currentRelation.tags.get("admin_level") );
        // System.out.println("name="+currentRelation.tags.get("name") );

        boolean blnNeedForCitySearch;
        blnNeedForCitySearch=false;
        if (strRegionCode.equals("FULL")||strRegionCode.equals("OVRV"))
          //ISO3166-1,ISO3166-1:alpha2
          {
            blnNeedForCitySearch=blnNeedForCitySearch || currentRelation.getTag("ISO3166-1").equals(strCountryCode);
            blnNeedForCitySearch=blnNeedForCitySearch || currentRelation.getTag("iso3166-1").equals(strCountryCode);
            blnNeedForCitySearch=blnNeedForCitySearch || currentRelation.getTag("cg_ref").equalsIgnoreCase(strCountryCode);
          }
        else
          //iso3166-2,ISO3166-2,ISO3166-2:XX
          {
            blnNeedForCitySearch=blnNeedForCitySearch || currentRelation.getTag("ISO3166-2").equals(strMapCode);
            blnNeedForCitySearch=blnNeedForCitySearch || currentRelation.getTag("iso3166-2").equals(strMapCode);
            blnNeedForCitySearch=blnNeedForCitySearch || currentRelation.getTag("cg_ref").equalsIgnoreCase(strMapCode);
            if (strCountryCode.equals("FI"))
            {
              blnNeedForCitySearch=blnNeedForCitySearch || currentRelation.getTag("ISO3166-2:old").equals(strMapCode);
              blnNeedForCitySearch=blnNeedForCitySearch || currentRelation.getTag("iso3166-2:old").equals(strMapCode);
            }

            //ref:nuts
            //ref:nuts без дефиса, например CZ02
            strNutsCode=strCountryCode+strRegionCode;
            if (strCountryCode.equals("GB"))
            {
              if (strRegionCode.equals("EN1")){strNutsCode="UKC";}
              if (strRegionCode.equals("EN2")){strNutsCode="UKD";}
              if (strRegionCode.equals("EN3")){strNutsCode="UKE";}
              if (strRegionCode.equals("EN4")){strNutsCode="UKF";}
              if (strRegionCode.equals("EN5")){strNutsCode="UKG";}
              if (strRegionCode.equals("EN6")){strNutsCode="UKH";}
              if (strRegionCode.equals("EN7")){strNutsCode="UKI";}
              if (strRegionCode.equals("EN8")){strNutsCode="UKJ";}
              if (strRegionCode.equals("EN9")){strNutsCode="UKK";}
            }

            blnNeedForCitySearch=blnNeedForCitySearch || currentRelation.getTag("ref:nuts").equalsIgnoreCase(strNutsCode) ;
            blnNeedForCitySearch=blnNeedForCitySearch || currentRelation.getTag("ref:nuts:1").equalsIgnoreCase(strNutsCode) ;


          }

        if (!blnNeedForCitySearch )
        {
          //System.out.println("Отношение "+currentRelation.tags.get("name") + " нах не годится, у него уровень "+ currentRelation.tags.get("admin_level")  );
          break rel;
        }

        System.out.println("Отношение "+currentRelation.tags.get("name") +"("+ currentRelation.id +")  годится, у него уровень "+ currentRelation.tags.get("admin_level")  );
        GetBoundary(strMapCode,currentRelation.id);
        //break;
        /*
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

          OsmWay aWay;
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
        */
      }

    System.out.println(k+ " relation(s) processed");
    //System.out.println(addressRegions.size()+ " relation(s) loaded");

  }

  void GetBoundary(String MapCode,String strID)   throws IOException,InterruptedException
  {
    System.out.println("Starting getbound.pl");
    ProcessBuilder pb = new ProcessBuilder
            ("getbound.bat",
                    MapCode,
                    strID
                  );

    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
    pb.redirectError(ProcessBuilder.Redirect.INHERIT);

    long startMark = System.currentTimeMillis();

    Process process = pb.start();

    int result = process.waitFor();
    System.out.println("getbound.pl completed "+result);

  }

}
