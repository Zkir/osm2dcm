/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 03.11.12
 * Time: 16:13
 * To change this template use File | Settings | File Templates.
 */
package ru.zkir.mp2mp.taskvalidator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collections;

import ru.zkir.mp2mp.core.MPParseException;
import ru.zkir.mp2mp.core.MpSection;
import ru.zkir.mp2mp.vb6.vb6;

public class clsStatistic {

  //Статистика по типам секций
  private HashMap<String,clsSectionInfo> mapSectionInfo;
  private ArrayList<clsSectionInfo> arrSectionInfo;

 //  Показатели
 //Общая протяженность дорог
 //Протяженность дворовых проездов
 //Общее число ПОИ
 //Число ПОИ с адресной/контактной информацией

  double dblRoadLength;
  double dblServiceRoadLength ;
  long intCitiesNumber;
  long intTotalPoiNumber;
  long intPoiWithAddressNumber;
  
  //Константы
  final String mptCityOver10M = "0x0100";
  final String mptCity200_500K = "0x0600";
  final String mptCity100_200K = "0x0700";
  final String mptCity50_100K = "0x0800";

  final String mptSettlement2_5K = "0x0C00";
  final String mptSettlement500_1000 = "0x0E00";
  final String mptSettlement200_500 = "0x0F00";
  final String mptSettlementLess100 = "0x1100";

  private class clsSectionInfo
  {
    String Kind;
    String Type;
    long Count;
    long SizeInBytes;
    public clsSectionInfo(String strKind, String strType)
    {
      Kind=strKind;
      Type=strType;
      Count=0;
      SizeInBytes=0;
    }

  }


  public clsStatistic()
  {

    mapSectionInfo = new HashMap<String,clsSectionInfo>();
    arrSectionInfo = new ArrayList<clsSectionInfo>();

    dblRoadLength=0;
    dblServiceRoadLength=0;
    intCitiesNumber=0;
    intTotalPoiNumber=0;
    intPoiWithAddressNumber=0;
  }

public void ProcessSection(MpSection oMpSection) throws MPParseException
{
  Double dblRoadLen;

  //Самая общая статистика, об объектах мп по типу.
  ProcessSectionGen (oMpSection.SectionType, oMpSection.mpType(), oMpSection.SizeInBytes());

  //Специфические показатели
  //Пои
  if(oMpSection.SectionType.equals("[POI]"))
  {  
    if ((oMpSection.mpType().compareTo( mptCityOver10M)>=0) && (oMpSection.mpType().compareTo(mptSettlementLess100)<=0))
    {
      //Населенные пункты учитываются отдельно и в число ПОИ не включаются.
      intCitiesNumber = intCitiesNumber + 1;
    }
    else
    {
      //Собственно пои
      if (!oMpSection.mpType().equals("0x6100"))  //точечные дома учитывать не надо, они уже учтены в адресах
      {
        intTotalPoiNumber = intTotalPoiNumber + 1;
        if (
             !oMpSection.GetAttributeValue("HouseNumber").trim().equals("") && 
             !oMpSection.GetAttributeValue("StreetDesc").trim().equals("")
           )   
        {      
          intPoiWithAddressNumber = intPoiWithAddressNumber + 1;
        }
      }
    }
  }

  //Дороги
  if ( oMpSection.SectionType.equals("[POLYLINE]") )
  {
    if ( !oMpSection.mpRouteParam().equals("") )
    {
      if ( !oMpSection.GetOsmHighway().equals("service") )
      {
        dblRoadLen = oMpSection.CalculateLength();
        if ( oMpSection.isOneWay() )
        {
          dblRoadLen = dblRoadLen / 2;
        }
        dblRoadLength = dblRoadLength + dblRoadLen;
      }
      else
      {
        dblServiceRoadLength = dblServiceRoadLength + oMpSection.CalculateLength();
      }
    }  
    else
    {
      if ( oMpSection.mpType().equals("0x8849") )
      {
        //Если дворовые проезды не влючены, они получают тип 0x8849
        dblServiceRoadLength = dblServiceRoadLength + oMpSection.CalculateLength();
      }
    }
  }

}

//Самая общая статистика, об объектах мп по типу.
  private void ProcessSectionGen(String strKind, String strType, Long intSize)
  {
  String strKey;
  clsSectionInfo si;

  strKey = strKind + " " + strType.toLowerCase();

  if (mapSectionInfo.containsKey(strKey))
  {
    si=mapSectionInfo.get(strKey);
  }
  else
  {
    si = new clsSectionInfo(strKind,strType);
    mapSectionInfo.put(strKey, si);
    arrSectionInfo.add(si);
  }

  si.Count++;
  si.SizeInBytes=si.SizeInBytes+intSize;


  }

  public void PrintReportToXML(BufferedWriter oReportFile) throws IOException
  {
    oReportFile.write("<Statistics>\r\n");
    oReportFile.write("<Summary>\r\n");
    oReportFile.write("  <RoadLengthKm>" + Long.toString(vb6.Round(dblRoadLength)) + "</RoadLengthKm>\r\n");
    oReportFile.write("  <ServiceRoadLengthKm>" + Long.toString(vb6.Round(dblServiceRoadLength)) + "</ServiceRoadLengthKm>\r\n");
    oReportFile.write("  <CitiesNumber>" + Long.toString(intCitiesNumber) + "</CitiesNumber>\r\n");
    oReportFile.write("  <TotalPoiNumber>" + Long.toString(intTotalPoiNumber) + "</TotalPoiNumber>\r\n");
    oReportFile.write("  <PoiWithAddressNumber>" + Long.toString(intPoiWithAddressNumber) + "</PoiWithAddressNumber>\r\n");

    oReportFile.write("</Summary>\r\n");

    //Cтатистика по типам. Только она еще должна быть рассортирована ;)
    Collections.sort(arrSectionInfo, new Comparator <clsSectionInfo>() {

      public int compare(clsSectionInfo o1, clsSectionInfo o2) {

        if (!o1.Kind.equalsIgnoreCase(o2.Kind) )
          {return (o1.Kind).compareToIgnoreCase(o2.Kind);}
        else
          {return (o1.Type).compareToIgnoreCase(o2.Type) ;}
      }
    });

    Iterator<clsSectionInfo> it =  arrSectionInfo.iterator();
    clsSectionInfo si;
    oReportFile.write("<TypeList>\r\n");

    while(it.hasNext())
    {
      si=it.next();

      oReportFile.write("  <Type>\r\n");
      oReportFile.write("    <Kind>" + si.Kind + "</Kind>\r\n");
      oReportFile.write("    <Type>" + si.Type + "</Type>\r\n");
      oReportFile.write("    <Count>"+ si.Count  + "</Count>\r\n");
      oReportFile.write("    <Size>" + si.SizeInBytes  + "</Size>\r\n");
      oReportFile.write("  </Type>\r\n");
    }



    oReportFile.write("</TypeList>\r\n");
    oReportFile.write("</Statistics>\r\n");;
  }
/*

 rsMPStat.Filter = adFilterNone
 rsMPStat.Sort = RS_STAT_KIND & " asc ," & RS_STAT_TYPE & " asc"


  Print #FileNumber, "<TypeList>"
  Do While Not rsMPStat.EOF

    rsMPStat.MoveNext
  Loop
  Print #FileNumber, "</TypeList>"
  Print #FileNumber, "</Statistics>"


End Sub




   */

}
