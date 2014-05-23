package ru.zkir.mp2mp.taskvalidator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.zkir.mp2mp.vb6.vb6;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 05.11.12
 * Time: 13:21
 * To change this template use File | Settings | File Templates.
 */
public class clsAddrRegistryTest {



  //Типы НП
  final String otCity = "0x0700"; //'-city
  final String otTown = "0x0a00"; //   '-town
  final String otVillage = "0x0C00";// '-village
  final String otHamlet = "0x0F00"; //'-hamlet

  //Коды ошибок
  final int steOK = 0;
  final int steOutsideCity = 1;
  final int steStreetNotSet = 2;
  final int steStreetNotFound = 3;
  final int steStreetNotRelatedToCity = 4;
  //final int steNumberRelatedToTerritory = 5;
  //final int steStreetNotRoutable = 6;

   class HouseInfo
  {
    String cityName;
    String streetName;
    String houseNumber;
    double lat;
    double lon;
    int errorCode;
  }
  class StreetInfo
  {
    String cityName;
    String regionName;
    String streetName;
    int routable;
    int hasHouses;
    double lat;
    double lon;
  }

  class CityInfo
  {
    String name;       //Название
    String addrCity;   //Название по геокодеру
    String addrRegion; //Регион по геокодеру
    long population;
    boolean populationMissing;
    double lat;
    double lon;
    int valid;//Наличие полигона place
    int valid2;//Совпадение названия по геокодеру.
    String origtype;
    boolean urban;
  }


  //Рабочие массивы
  ArrayList<HouseInfo> arrHouses;

  ArrayList<StreetInfo> arrStreets;
  HashMap<String,StreetInfo> mapStreetByNameAndCity;
  ArrayList<StreetInfo> arrStreetSegments;

  ArrayList<CityInfo> arrCityPolies;
  ArrayList<CityInfo> arrCities;

  HashMap<String,String> mapAddrTerritoryByNameAndCity;


  //Результаты
  ArrayList<CityInfo> arrCitiesWOPopulation;
  ArrayList<CityInfo> arrCitiesWOPlacePolygon;
  ArrayList<CityInfo> arrCitiesWOPlaceNode;

  ArrayList<HouseInfo> arrHousesErr;

  int intHousesOutsideCity;
  int intHousesStreetNotSet ;
  int intHousesStreetNotFound;
  int intHousesStreetNotRelatedToCity ;
 // int intHousesNumberRelatedToTerritory;
 // int intHousesStreetNotRoutable;
  int intNumberOfCities;
  int intNumberOfCitiesWrongGK;


  public clsAddrRegistryTest()
  {
    arrCityPolies=new ArrayList<CityInfo>();
    arrCities=new ArrayList<CityInfo>();

    arrStreets=new ArrayList<StreetInfo>();
    mapStreetByNameAndCity=new HashMap<String,StreetInfo>();
    arrStreetSegments=new ArrayList<StreetInfo>();

    mapAddrTerritoryByNameAndCity= new HashMap<String, String>();

    arrHouses= new ArrayList<HouseInfo>();

    intHousesOutsideCity=0;
    intHousesStreetNotSet=0 ;
    intHousesStreetNotFound=0;
    intHousesStreetNotRelatedToCity=0;
    //intHousesNumberRelatedToTerritory=0;
    //intHousesStreetNotRoutable=0;
    arrHousesErr=new ArrayList<HouseInfo>();
    arrCitiesWOPopulation=new ArrayList<CityInfo>();
    arrCitiesWOPlacePolygon=new ArrayList<CityInfo>();
    arrCitiesWOPlaceNode=new ArrayList<CityInfo>();
  }

  public void AddHouseToRegistry(String strHouseNumber,
                                 String strStreetDesc,
                                 String strCity, double lat, double lon)
  {
    HouseInfo theHouseInfo;
    theHouseInfo =new HouseInfo();

    theHouseInfo.cityName    =  strCity;
    theHouseInfo.streetName  =  strStreetDesc;
    theHouseInfo.houseNumber =  strHouseNumber;
    theHouseInfo.lat=lat;
    theHouseInfo.lon=lon;

    arrHouses.add(theHouseInfo);


  }

  public void AddStreetToRegistry(String strStreetDesc, String strCity, String strRegion,
                                  boolean blnRoutable,double lat, double lon, boolean blnShouldBeInCity)
  {
    //Безымянные улицы нам не нужны
    if(strStreetDesc.equals("") )
    {
      return;
    }
    //Именованные улицы, к которым привязываются дома

    StreetInfo theStreetInfo;
    if (!mapStreetByNameAndCity.containsKey(strCity+"/"+strStreetDesc) )
    {
      theStreetInfo=new StreetInfo();
      theStreetInfo.cityName = strCity;
      theStreetInfo.regionName = strRegion;

      theStreetInfo.streetName  = strStreetDesc;
      theStreetInfo.routable  = 0;
      theStreetInfo.hasHouses  =0;
      theStreetInfo.lat=lat;
      theStreetInfo.lon=lon;


      arrStreets.add(theStreetInfo);
      mapStreetByNameAndCity.put(strCity+"/"+strStreetDesc, theStreetInfo);
    }
    else
    {
      theStreetInfo=mapStreetByNameAndCity.get(strCity+"/"+strStreetDesc);
    }

    //В одном НП достаточно одного сегмента рутиговой улицы.
    if (blnRoutable)
    {
      theStreetInfo.routable  = 1;
    }

    //Улицы за пределами населенных пунктов
    if (blnShouldBeInCity && blnRoutable && strCity.equals(""))
    {
      theStreetInfo=new StreetInfo();
      theStreetInfo.cityName = strCity;
      theStreetInfo.regionName = strRegion;
      theStreetInfo.streetName  = strStreetDesc;
      if (blnRoutable)
        {theStreetInfo.routable  =1;}
      else
        {theStreetInfo.routable  =0;}

      theStreetInfo.hasHouses  =0;
      theStreetInfo.lat=lat;
      theStreetInfo.lon=lon;
      arrStreetSegments.add(theStreetInfo);

    }

  }

  //Добавление города, как границ, так и точки
  public void AddCityToRegistry(String strCityName,
                                String strAddrCity,
                                String strAddrRegion,
                                double lat,
                                double lon,
                                long intPopulation,
                                boolean blnPolygon,
                                boolean blnUrban,
                                String strOrigType)
  {
    CityInfo theCityInfo;
    theCityInfo=new CityInfo();
    if (blnPolygon)
    {
      theCityInfo.name=strCityName;
      theCityInfo.urban=blnUrban;
      theCityInfo.lat=(lat);
      theCityInfo.lon=(lon);
      arrCityPolies.add(theCityInfo);
    }
    else
    {
      theCityInfo.name=strCityName;
      theCityInfo.addrCity=strAddrCity;
      theCityInfo.addrRegion=strAddrRegion;
      theCityInfo.population = intPopulation;
      theCityInfo.lat=(lat);
      theCityInfo.lon=(lon);
      theCityInfo.origtype =strOrigType;

      arrCities.add(theCityInfo);
    }

  }

  public void AddAddrTerritoryToRegistry(String strCityName, String strStreetName)
  {
    mapAddrTerritoryByNameAndCity.put(strCityName+"/"+strStreetName,"A");

  }

  // Проверка городов на валидность
  //  * Наличие полигональных границ
  //  * Наличие population
  public void  ValidateCities()
  {
    int i;
    int j;
    intNumberOfCities=0;
    intNumberOfCitiesWrongGK=0;

    for(i=0;i<arrCities.size();i++ )
    {
      CityInfo theCityInfo;
      theCityInfo=arrCities.get(i);

      //Население и работа геокодера
      if((theCityInfo.origtype.equals(otCity)) || (theCityInfo.origtype.equals(otTown) ))
      {
        intNumberOfCities++;
        //Население
        if (theCityInfo.population==-1 )
        {
          theCityInfo.populationMissing=true;
        }
        else
        {
          theCityInfo.populationMissing=false;
        }
        //Название города и название присвоенное ему геокодером должно совпадать
        //Безымянные города не учитываются, это не проблема геокодера.
        if(theCityInfo.name.equals(theCityInfo.addrCity)||(theCityInfo.name.equals("")) )
        {
          theCityInfo.valid2=1;
        }
        else
        {
          theCityInfo.valid2=0;
          intNumberOfCitiesWrongGK++;
        }

        //Особый случай для швеции
        //Родительный падеж, который нельзя вычистить геокодером
        //if("SE".equals("SE")&&((theCityInfo.addrCity.equals(theCityInfo.name+" kommun") )||(theCityInfo.addrCity.equals(theCityInfo.name+"s kommun")) )
        //(Не сложилось)
      }


      //Полигональные границы.
      // - найдем соответствующие границы, по совпадению имен
      ArrayList<CityInfo> rs;
      rs= new ArrayList<CityInfo>();
      for(j=0;j<arrCityPolies.size();j++ )
      {
        if (arrCityPolies.get(j).name.trim().equals(theCityInfo.name.trim()))
        {
          rs.add(arrCityPolies.get(j));
          break;//Для ускорения
        }
      }
      if (rs.size()>0 )
      {
        theCityInfo.valid=1;
      }
      else
      {
        //'У НП нет полигональных границ
        if (! ((theCityInfo.origtype.equals(otHamlet) ) || (theCityInfo.origtype.equals(otVillage)) ))
        {
          theCityInfo.valid = 0;
        }
        else
        {
          theCityInfo.valid  = 1; // Для хамлетов требовать границы не будем.
          //Debug.Print rsCities(RS_CITY_NAME).Value, rsCities(RS_CITY_ORIGTYPE).Value
        }

      }

    }

    //rsCities.Sort = RS_CITY_NAME



    for(i=0;i<arrCities.size();i++ )
    {
      CityInfo theCityInfo;
      theCityInfo=arrCities.get(i);

      //rsCities.Filter = RS_CITY_POPULATION_MISSING & "=1"
      if (theCityInfo.populationMissing)
      {
        arrCitiesWOPopulation.add(theCityInfo);
      }

      //rsCities.Filter = RS_CITY_VALID & "=0"
      if (theCityInfo.valid==0)
      {
        arrCitiesWOPlacePolygon.add(theCityInfo);
      }

    }
  }

  //"Обратный" тест, поиск городов, для которых есть граница, но нет точки.
  public void  ValidateCitiesReverse()
  {

    int i;
    int j;

    for(i=0;i<arrCityPolies.size();i++ )
    {
      CityInfo theCityInfo;
      theCityInfo=arrCityPolies.get(i);

      //Полигональные границы.
      // - найдем соответствующие границы, по совпадению имен
      ArrayList<CityInfo> rs;
      rs= new ArrayList<CityInfo>();
      for(j=0;j<arrCities.size();j++ )
      {
        //TODO: почему-то vb cравнивал без учета регистра
        if (arrCities.get(j).name.trim().equalsIgnoreCase(theCityInfo.name.trim()) )
        {
          rs.add(arrCities.get(j));
          break;//Для ускорения
        }
      }
      if (rs.size()>0 )
      {
        theCityInfo.valid=1;
      }
      else
      {
        //'У НП нет точечного центра
        theCityInfo.valid = 0;
      }
    }



    for(i=0;i<arrCityPolies.size();i++ )
    {
      CityInfo theCityInfo;
      theCityInfo=arrCityPolies.get(i);

      //rsCities.Filter = RS_CITY_VALID & "=0"
      if (theCityInfo.valid==0)
      {
        arrCitiesWOPlaceNode.add(theCityInfo);
      }
    }

  }

  // Проверка домов на валидность
  // Основное условие - наличие соответсвующей улицы
  public void  ValidateHouses()
  {
    int i;

    for(i=0;i<arrHouses.size();i++ )
    {
      HouseInfo theHouseInfo;
      theHouseInfo=arrHouses.get(i);

      theHouseInfo.errorCode= CheckStreet(theHouseInfo.streetName, theHouseInfo.cityName);

      if (theHouseInfo.errorCode==steOutsideCity)
      {
        arrHousesErr.add(theHouseInfo);
        intHousesOutsideCity++;
      }

      if (theHouseInfo.errorCode==steStreetNotSet )
      {
        arrHousesErr.add(theHouseInfo);
        intHousesStreetNotSet++;
      }

      if (theHouseInfo.errorCode==steStreetNotFound )
      {arrHousesErr.add(theHouseInfo);
       intHousesStreetNotFound++;
      }

     /* if (theHouseInfo.errorCode==steStreetNotRoutable  )
      {arrHousesErr.add(theHouseInfo);
        intHousesStreetNotRoutable++;
      }*/

      if (theHouseInfo.errorCode==steStreetNotRelatedToCity)
      {arrHousesErr.add(theHouseInfo);
        intHousesStreetNotRelatedToCity++;
      }

      /*if (theHouseInfo.errorCode==steNumberRelatedToTerritory)
      {arrHousesErr.add(theHouseInfo);
        intHousesNumberRelatedToTerritory++;
      } */
    }

    //Теперь надо список домов рассортировать
    //rsHouses.Sort = RS_ADDR_CITY & ", " & RS_ADDR_STREET & ", " & RS_ADDR_HOUSENUMER

    Collections.sort(arrHousesErr , new Comparator <HouseInfo>() {

      public int compare(HouseInfo o1, HouseInfo o2) {

        if (!o1.cityName.equalsIgnoreCase(o2.cityName) )
        {
          return (o1.cityName).compareToIgnoreCase(o2.cityName);
        }
        else
        {
          if (!o1.streetName.equalsIgnoreCase(o2.streetName) )
          {
            return (o1.streetName).compareToIgnoreCase(o2.streetName );
          }
          else
          {
            return (o1.houseNumber).compareToIgnoreCase(o2.houseNumber);
          }
        }
      }
    });


  }

  //Проверка улицы и нп.
  private int CheckStreet(String strStreetDesc, String strCity)
  {
    //Город не указан.
    //На самом деле это относительная ошибка, но пока мы на это не обращаем внимание.
    // Cейчас не до адресов типа 101 км М8
    if(strCity.equals(""))
    {
      return steOutsideCity;
    }

    //Улица не задана.
    // Это тоже относительная ошибка, но будем считать что что-то должно быть задано
    if (strStreetDesc.equals(""))
    {
      return steStreetNotSet;
    }


    //Теперь начинаем искать улицу, к которой относится дом.

    //надо найти хотя бы одну рутинговую улицу
    if(mapStreetByNameAndCity.containsKey(strCity+"/"+strStreetDesc) )
    {
      StreetInfo theStreetInfo;
      theStreetInfo=mapStreetByNameAndCity.get(strCity+"/"+strStreetDesc);

      //Улица найдена, это однозначный вывод
      if (theStreetInfo.routable != 0)
      {
        return steOK;
      }
      else
      {
        //return steStreetNotRoutable;
        //Non-routable streets are now included into address registry.
        return steOK;
      }
    }

    //Продолжаем искать дальше
    // Улица может номероваться в рамках населенного пункта или квартала.

    // Название улицы равно названию города
    if(strStreetDesc.equals(strCity))
    {
      //return steNumberRelatedToTerritory;
      return steOK;
    }

    if (strStreetDesc.equals("деревня " + strCity))
    {
      //return steNumberRelatedToTerritory;
      return steOK;
    }


     //Название "улицы" содержит слово "микрорайон" или поселок.
     //Эта проверка предшествует поиску в "негородских" улицах, потому что есть особое извращение присваивать
     // названия микрорайонов дворовым проездам. hw=service

    //Название улицы может содержать название района (suburb)
    String strStreetDescWoSuburb;
    strStreetDescWoSuburb=strStreetDesc;

    Pattern p = Pattern.compile("(.*) /(.*)/");
    Matcher m = p.matcher(strStreetDesc);
    if (m.matches()){
      strStreetDescWoSuburb=m.group(1);
    }

     if( vb6.InStr(strStreetDescWoSuburb.toLowerCase(), "микрорайон") != 0 ||
         vb6.InStr(strStreetDescWoSuburb.toLowerCase(), "мкрн.") != 0 ||
         vb6.InStr(strStreetDescWoSuburb.toLowerCase(), "квартал") != 0 ||
         vb6.InStr(strStreetDescWoSuburb.toLowerCase(), "поселок") != 0 ||
         vb6.InStr(strStreetDescWoSuburb.toLowerCase(), "садоводство") != 0 ||
         vb6.InStr(strStreetDescWoSuburb.toLowerCase(), "территория") != 0 ||
         vb6.Left(strStreetDescWoSuburb, 3).equals("СНТ") )
    {
       //return steNumberRelatedToTerritory;
      return steOK;
    }


    //Попытаемся найти нашу улицу в списке территорий.
    if(mapAddrTerritoryByNameAndCity.containsKey(strCity+"/"+strStreetDesc) )
    {
      //return steNumberRelatedToTerritory;
      return steOK;
    }


    // Последняя попытка
    //Улица может присутствовать, но быть не сопоставлена городу.

    //надо найти хотя бы одну  улицу
    if(mapStreetByNameAndCity.containsKey(""+"/"+strStreetDesc) )
    {
      StreetInfo theStreetInfo;
      theStreetInfo=mapStreetByNameAndCity.get(""+"/"+strStreetDesc);
      if (theStreetInfo.routable != 0)
      {
        return steStreetNotRelatedToCity;
      }
      else
      {
        //Это такой особый прикол osm2mp, что нерутинговым улицам город не присваивается.
        //Будем считать, что если улица с таким названием есть, и она нерутинговая, этого достаточно
        return steOK;
      }
    }

    //Искать больше негде
    return steStreetNotFound; // Такой улицы просто нет
  }

  public void PrintErrorsToXML(BufferedWriter oReportFile, boolean blnSummary)  throws IOException
  {
    int i;

    int intTotalHouses=arrHouses.size();
    int intTotalStreets=arrStreets.size() ;
    int intUnmatchedHouses= arrHousesErr.size();

    int intStreetsOutsideCities=0;
    int intStreetsWithoutRegion=0;
    for(i=0;i<arrStreetSegments.size();i++ )
    {
      if (arrStreetSegments.get(i).cityName.equals("")  )
      {
        intStreetsOutsideCities=intStreetsOutsideCities+1;
      }
    }

    for(i=0;i<arrStreets.size();i++ )
    {
      //todo: correct condition when CG learn to search non-routable streets
      if (arrStreets.get(i).regionName.equals("")  && (arrStreets.get(i).routable!=0) )
      {
        intStreetsWithoutRegion=intStreetsWithoutRegion+1;
        //System.out.println(arrStreets.get(i).streetName+ " -- "+ arrStreets.get(i).cityName );

      }
    }

    double dblErrorRate=(double)intUnmatchedHouses/(double)intTotalHouses ;


    //Сводка по НП
    //Отсортируем массив городов по населению.
    Collections.sort(arrCities,  new Comparator<CityInfo>() {
      public int compare(CityInfo sp1, CityInfo sp2) {
        return (sp1.population > sp2.population ) ? -1: (sp1.population < sp2.population) ? 1:0 ;
      }
    });

    //Найдем особый показатель, индекс правильности городов. Это доля городов с адресным поиском до первой ошибки.
    //Например, ищутся 5 первых городов =(6-5)/6=0.16
    Double dblNumberOfCitiesWrongGKMark;
    int intCityCounter=0;
    double dblCityErrors=0.0;
    for (i=0;i<arrCities.size();i++ )
    {
      CityInfo theCityInfo;
      theCityInfo=arrCities.get(i);
      if((theCityInfo.origtype.equals(otCity)) || (theCityInfo.origtype.equals(otTown) ))
      {
        intCityCounter++;
        if (theCityInfo.valid2==0)
        {
          dblCityErrors=1.0;
          break;
        }
      }
    }
    dblNumberOfCitiesWrongGKMark=dblCityErrors/intCityCounter;


    oReportFile.write( "<AddressTest>\r\n");

    oReportFile.write( "<Summary>\r\n");
    oReportFile.write( " <TotalHouses>" + Integer.toString( intTotalHouses) + "</TotalHouses>\r\n");
    oReportFile.write( " <TotalStreets>" + Integer.toString( intTotalStreets)+ "</TotalStreets>\r\n");
    oReportFile.write( " <UnmatchedHouses>" + Integer.toString( intUnmatchedHouses) + "</UnmatchedHouses>\r\n");
    oReportFile.write( " <HousesWOCities>" +  Integer.toString(intHousesOutsideCity ) + "</HousesWOCities>\r\n");
    oReportFile.write( " <HousesStreetNotSet>" + Integer.toString(intHousesStreetNotSet) + "</HousesStreetNotSet>\r\n");
    oReportFile.write( " <HousesStreetNotFound>" + Integer.toString( intHousesStreetNotFound) + "</HousesStreetNotFound>\r\n");
    oReportFile.write( " <HousesStreetNotRelatedToCity>" + Integer.toString(intHousesStreetNotRelatedToCity) + "</HousesStreetNotRelatedToCity>\r\n");
    //oReportFile.write( " <HousesStreetNotRoutable>"  + Integer.toString(intHousesStreetNotRoutable) + "</HousesStreetNotRoutable>\r\n");
    //oReportFile.write( " <HousesNumberRelatedToTerritory>" + Integer.toString(intHousesNumberRelatedToTerritory) + "</HousesNumberRelatedToTerritory>\r\n");
    oReportFile.write( " <TotalCities>" + Integer.toString( intNumberOfCities )+ "</TotalCities>\r\n");
    oReportFile.write( " <CitiesWrongGK>" + Integer.toString( intNumberOfCitiesWrongGK )+ "</CitiesWrongGK>\r\n");
    oReportFile.write( " <CitiesWrongGKMark>" + Double.toString( dblNumberOfCitiesWrongGKMark )+ "</CitiesWrongGKMark>\r\n");
    oReportFile.write( " <CitiesWithoutPopulation>" + Integer.toString( arrCitiesWOPopulation.size() )+ "</CitiesWithoutPopulation>\r\n");

    oReportFile.write( " <CitiesWithoutPlacePolygon>"  + Integer.toString(arrCitiesWOPlacePolygon.size()) + "</CitiesWithoutPlacePolygon>\r\n");
    oReportFile.write( " <CitiesWithoutPlaceNode>" +  Integer.toString(arrCitiesWOPlaceNode.size())+ "</CitiesWithoutPlaceNode>\r\n");

    oReportFile.write( " <StreetsOutsideCities>" + Integer.toString( intStreetsOutsideCities) + "</StreetsOutsideCities>\r\n");
    oReportFile.write( " <StreetsWithoutRegion>" + Integer.toString( intStreetsWithoutRegion) + "</StreetsWithoutRegion>\r\n");

    oReportFile.write( " <ErrorRate>" +Double.toString(dblErrorRate) + "</ErrorRate>\r\n");

    oReportFile.write( "</Summary>\r\n");



    oReportFile.write( "<CitiesSummary>\r\n");
    for (i=0;i<arrCities.size();i++ )
    {
      CityInfo theCityInfo;
      theCityInfo=arrCities.get(i);
      if((theCityInfo.origtype.equals(otCity)) || (theCityInfo.origtype.equals(otTown) ))
      {
      oReportFile.write( "<City>\r\n");
      oReportFile.write( " <Name>" + MakeXmlString(theCityInfo.name) + "</Name>\r\n");
      oReportFile.write( " <AddrCity>" + MakeXmlString(theCityInfo.addrCity) + "</AddrCity>\r\n");
      oReportFile.write( " <AddrRegion>" + MakeXmlString(theCityInfo.addrRegion) + "</AddrRegion>\r\n");
      oReportFile.write( " <Population>" + theCityInfo.population  + "</Population>\r\n");
      oReportFile.write( " <Valid>" + theCityInfo.valid2  + "</Valid>\r\n");
      oReportFile.write( " <Coord>\r\n");
      oReportFile.write( "   <lat>" + theCityInfo.lat + "</lat>\r\n");
      oReportFile.write( "   <lon>" + theCityInfo.lon + "</lon>\r\n");
      oReportFile.write( " </Coord>\r\n");
      oReportFile.write( "</City>\r\n");
      }
    }
    oReportFile.write( "</CitiesSummary>\r\n");

    //Города без населения

    oReportFile.write( "<CitiesWithoutPopulation>\r\n");
    for (i=0;i<arrCitiesWOPopulation.size();i++ )
    {
      CityInfo theCityInfo;
      theCityInfo=arrCitiesWOPopulation.get(i);
      oReportFile.write( "<City>\r\n");
      oReportFile.write( " <City>" + MakeXmlString(theCityInfo.name) + "</City>\r\n");
      oReportFile.write( " <Coord>\r\n");
      oReportFile.write( "   <lat>" + theCityInfo.lat + "</lat>\r\n");
      oReportFile.write( "   <lon>" + theCityInfo.lon + "</lon>\r\n");
      oReportFile.write( " </Coord>\r\n");
      oReportFile.write( "</City>\r\n");
    }
    oReportFile.write( "</CitiesWithoutPopulation>\r\n");


    //Города без полигона place
    oReportFile.write( "<CitiesWithoutPlacePolygon>\r\n");

    for (i=0;i<arrCitiesWOPlacePolygon.size();i++ )
    {
      CityInfo theCityInfo;
      theCityInfo=arrCitiesWOPlacePolygon.get(i);
      oReportFile.write( "<City>\r\n");
      oReportFile.write( " <City>" + MakeXmlString(theCityInfo.name) + "</City>\r\n");
      oReportFile.write( " <Coord>\r\n");
      oReportFile.write( "   <lat>" + theCityInfo.lat + "</lat>\r\n");
      oReportFile.write( "   <lon>" + theCityInfo.lon + "</lon>\r\n");
      oReportFile.write( " </Coord>\r\n");
      oReportFile.write( "</City>\r\n");
    }
    oReportFile.write( "</CitiesWithoutPlacePolygon>\r\n");

    //'Города c полигоном place, но без точки
      oReportFile.write( "<CitiesWithoutPlaceNode>\r\n");
      for (i=0;i<arrCitiesWOPlaceNode.size();i++ )
      {
        CityInfo theCityInfo;
        theCityInfo=arrCitiesWOPlaceNode.get(i);
        oReportFile.write( "<City>\r\n");
        oReportFile.write( " <City>" + MakeXmlString(theCityInfo.name) + "</City>\r\n");
        oReportFile.write( " <Coord>\r\n");
        oReportFile.write( "   <lat>" + theCityInfo.lat + "</lat>\r\n");
        oReportFile.write( "   <lon>" + theCityInfo.lon + "</lon>\r\n");
        oReportFile.write( " </Coord>\r\n");
        oReportFile.write( "</City>\r\n");
      }
      oReportFile.write( "</CitiesWithoutPlaceNode>\r\n");

      // Битая адреска

      if (!blnSummary)
      {
        oReportFile.write( "<AddressErrorList>\r\n");
        for(i=0;i<arrHousesErr.size();i++ )
        {

          HouseInfo theHouseInfo;
          theHouseInfo= arrHousesErr.get(i);

          oReportFile.write( "<House>\r\n");
          oReportFile.write( " <ErrType>" + MakeXmlString(theHouseInfo.errorCode) + "</ErrType>\r\n");
          oReportFile.write( " <City>" + MakeXmlString(theHouseInfo.cityName) + "</City>\r\n");
          oReportFile.write( " <Street>" + MakeXmlString(theHouseInfo.streetName) + "</Street>\r\n");
          oReportFile.write( " <HouseNumber>" + MakeXmlString(theHouseInfo.houseNumber) + "</HouseNumber>\r\n");
          oReportFile.write( " <Coord>\r\n");
          oReportFile.write( "   <lat>" + theHouseInfo.lat + "</lat>\r\n");
          oReportFile.write( "   <lon>" + theHouseInfo.lon + "</lon>\r\n");
          oReportFile.write( " </Coord>\r\n");
          oReportFile.write( "</House>\r\n");

        }

        oReportFile.write( "</AddressErrorList>\r\n");



        /*

        rsHouses.Filter = adFilterNone
        rsStreets.Filter = adFilterNone
        rsHouses.Sort = RS_ADDR_CITY & ", " & RS_ADDR_STREET & ", " & RS_ADDR_HOUSENUMER
        intTotalHouses = rsHouses.RecordCount
        intTotalStreets = rsStreets.RecordCount

        'Фильтр по домам без городов
        rsHouses.Filter = RS_ADDR_CITY & " = ''"
        intHousesWOCities = rsHouses.RecordCount

        rsHouses.Filter = RS_ADDR_ERROR & " = " & steStreetNotSet
        intHousesStreetNotSet = rsHouses.RecordCount

        rsHouses.Filter = RS_ADDR_ERROR & " = " & steStreetNotFound
        intHousesStreetNotFound = rsHouses.RecordCount

        rsHouses.Filter = RS_ADDR_ERROR & " = " & steStreetNotRelatedToCity
        intHousesStreetNotRelatedToCity = rsHouses.RecordCount

        rsHouses.Filter = RS_ADDR_ERROR & " = " & steNumberRelatedToTerritory
        intHousesNumberRelatedToTerritory = rsHouses.RecordCount

        rsHouses.Filter = RS_ADDR_ERROR & " = " & steStreetNotRoutable
        intHousesStreetNotRoutableCG = rsHouses.RecordCount

        'Фильтр по "несопоставленным домам"
        rsHouses.Filter = RS_ADDR_ERROR & " <> 0"
        intUnmatchedHouses = rsHouses.RecordCount
        If intTotalHouses <> 0 Then
          dblErrorRate = intUnmatchedHouses / intTotalHouses
        Else
          dblErrorRate = 0
        End If

        'Число городов без населения
        rsCities.Filter = RS_CITY_POPULATION_MISSING & "=1"
        intCitiesWOPopulation = rsCities.RecordCount

        'Число городов без полигональных границ
        rsCities.Filter = RS_CITY_VALID & "=0"
        intCitiesWOBounds = rsCities.RecordCount

        'Число городов без точек
        rsCityPolies.Filter = RS_CITY_VALID & "=0"
        intCitiesWONodes = rsCityPolies.RecordCount

        'Число улиц за пределами НП
        rsStreetSegments.Filter = RS_ADDR_CITY & "= '' "
        intStreetsOutsideCities = rsStreetSegments.RecordCount



        */



        //Улицы, оказавшиеся почему-то за пределами НП
        oReportFile.write( "<StreetsOutsideCities>\r\n");

        //rsStreetSegments.Filter = RS_ADDR_CITY & "= '' "
        for(i=0;i<arrStreetSegments.size();i++ )
        {
          StreetInfo theStreetInfo;
          theStreetInfo=arrStreetSegments.get(i);
          if (theStreetInfo.cityName.equals("")  )
          {
            oReportFile.write( "<Street>\r\n");
            oReportFile.write( " <Street>" + MakeXmlString(theStreetInfo.streetName) + "</Street>\r\n");
            oReportFile.write( " <Coord>\r\n");
            oReportFile.write( "   <Lat>" + theStreetInfo.lat + "</Lat>\r\n");
            oReportFile.write( "   <Lon>" + theStreetInfo.lon + "</Lon>\r\n");
            oReportFile.write( " </Coord>\r\n" );
            oReportFile.write( "</Street>\r\n" );
          }
        }
        oReportFile.write( "</StreetsOutsideCities>\r\n");
      }
    oReportFile.write("</AddressTest>\r\n");
  }

  private String MakeXmlString(String str)
  {
    str = str.replaceAll("&", "&amp;");
    return str;
  }

  private String MakeXmlString(int i)
  {
    return Integer.toString(i) ;
  }

}
