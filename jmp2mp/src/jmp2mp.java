/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 07.10.12
 * Time: 13:56
 * To change this template use File | Settings | File Templates.
 */
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class jmp2mp {
  //Параметры процесса
  private static Date dtProcessEnd,  dtProcessStart;
  //Основные параметры карты
  private static String strSource; //Входной файл
  private static String strTarget; //Выходной файл
  private static String strViewPoint; //Начальная точка карты

  // Классы, которые используются для тестов.
  private static clsConnectivityTest oConnectivityTest;
  private static clsConnectivityTest oConnectivityTest0; //Trunk
  private static clsConnectivityTest oConnectivityTest1; //Primary
  private static clsConnectivityTest oConnectivityTest2; //Secondary
  private static clsConnectivityTest oConnectivityTest3; //Tertiary

  //Статистка по польскому файлу
  private static clsStatistic oStatistic;


  //Точка входа
  public static void  main(String args[]) throws IOException,MPParseException
  {
    System.out.println(" --| jmp2mp (c) Zkir 2012");
    ParseCommandLine(args);

    if ((strSource != "") & (strTarget != "")) {
      System.out.println( "");
      System.out.println( "Postprocessor has been started");
      System.out.println( "Source file: " + strSource);
      System.out.println( "Target file: " + strTarget);
      System.out.println( "Viewpoint: " + strViewPoint);
      ProcessMP (strSource, strTarget, strViewPoint);
      System.out.println( "Postprocessor has been finished OK");
      System.out.println( "Time used: "+ Long.toString(dtProcessEnd.getTime()-dtProcessStart.getTime()) );
    }
    else
    {
      System.out.println( "Usage: mpPostProcessor <source mp file> <target mp file>");
    }

  }

  private static void ParseCommandLine(String args[])
  {
    //Инициализация
    strSource="";
    strTarget="";
    strViewPoint="";

   // strSource="d:/OSM/osm2dcm/_my/test/Test.pre.mp";
   // strTarget="d:/OSM/osm2dcm/_my/test/Test.java.mp";

     strSource="d:/OSM/osm2dcm/_my/RU-KGD/RU-KGD.pre.mp";
     strTarget="d:/OSM/osm2dcm/_my/RU-KGD/RU-KGD.java.mp";

    //strSource="d:/OSM/osm2dcm/_my/TH-FULL/TH-FULL.pre.mp";
    //strTarget="d:/OSM/osm2dcm/_my/TH-FULL/TH-FULL.java.mp";

  }
  private static void ProcessMP (String strSource, String strTarget, String strViewPoint) throws IOException, MPParseException
  {
    clsMpFile oSrcMp;
    clsMpFile oTgtMp;
    clsMpSection oMpSection;
    boolean blnSkipSection;

    oSrcMp= new clsMpFile(strSource,0);
    oTgtMp= new clsMpFile(strTarget,1);

    dtProcessStart= new Date();

    oConnectivityTest  = new clsConnectivityTest();
    oConnectivityTest0 = new clsConnectivityTest();
    oConnectivityTest1 = new clsConnectivityTest();
    oConnectivityTest2 = new clsConnectivityTest();
    oConnectivityTest3 = new clsConnectivityTest();

    oStatistic = new clsStatistic();

    while (oSrcMp.ReadNextSection()){ //цикл по секциям
      //Здесь различные операции над секцией
      oMpSection=oSrcMp.CurrentSection;
      blnSkipSection=false;

      //0. Расставим доп теги
      if (oSrcMp.CurrentSection.SectionType.equals("[IMG ID]")) {
        if (!strViewPoint.trim().equals("")){
          oSrcMp.CurrentSection.SetAttributeValue ("PointView", strViewPoint);
        }
      }

      //1. Убьем названия огородов (это очевидная бага конвертора )
      if (oSrcMp.CurrentSection.SectionType.equals("[POLYGON]")){
        if (oSrcMp.CurrentSection.GetAttributeValue("Type").equals("0x4e") & !oSrcMp.CurrentSection.GetAttributeValue("Label").equals("")) {
          oSrcMp.CurrentSection.SetAttributeValue("Label", "");
        }
      }

      //2. Переделаем pedestrian у которых нет названия в нерутинговые пешеходные дорожки.
      // (Операция устарела и закомментирована в vb) = Теперь это делается в osm2mp, более аккуратно

      //3. Переделаем нерутинговые улицы и проезды  в пешеходные улицы
      //  (По-моему, это просто не нужно )

      //4,5 . Убьем слово улица, ул. в адресах (StreetDesc) и названиях улиц(Label). Применим сокращения статусных частей улиц.
      NormalizeStreetLabelsAndAddresses(oSrcMp.CurrentSection);

      //6. 'Особый тип для грунтовых дорог.
      // (Операция устарела и закомментирована в vb) Теперь это делается в osm2mp, более аккуратно

      //7.  убьем нерутинговые паромные переправы
      //  (По-моему, это просто не нужно. Паромность - это теперь атрибут дороги )


      //8. Запреты проезда в СГ не поддерживаются. Вместо них поддерживается запрет транзитного рутинга.
      if(oMpSection.SectionType.equals("[POLYLINE]")){
        if (!oMpSection.mpRouteParam().equals("")) {
          if (oMpSection.mpRouteParam().split(",")[6].equals("1")){
            oMpSection.SetAttributeValue("RouteParamExt", "1");
          }
        }
      }

      //9. Классифицируем города по населению
      ClassifyCitiesByPopulation(oSrcMp.CurrentSection);

      //11. Классифицируем озера по размеру
      ClassifyLakesBySize(oSrcMp.CurrentSection);

      // 12.  Определим посты ДПС.
      if (oMpSection.SectionType.equals( "[POI]")){
        if(oMpSection.GetAttributeValue("Type").equals("0x3001")){
          String strLabel;
          strLabel=oMpSection.GetAttributeValue("Label");
          if (strLabel.contains("ДПС") || strLabel.contains("ГИБДД")){
            oMpSection.SetAttributeValue("Type","0xf202");
          }
        }
      }


      //13. Отфильтруем лишние запреты поворотов
      String strRestrParam;
      if(oMpSection.SectionType.equals("[Restrict]")){
        strRestrParam = oMpSection.GetAttributeValue("RestrParam");
        if(!strRestrParam.equals("")){
          if (strRestrParam.split(",")[2].equals("1")){
            // Это такой странный запрет поворота, который на автомобили не распространяется.
            blnSkipSection = true;
          }
        }
      }

      //16. Убьем CountryName, оно в СГ не используется
      RemoveCountryAttribute(oSrcMp.CurrentSection);

      //17. Тест связности.
      if (oMpSection.SectionType.equals( "[POLYLINE]")){
        if(!oMpSection.mpRouteParam().equals("") ){


          int Nnodes;
          String[] NodeList;
          int[]    NodeList2;
          double lat1, lon1,  lat2, lon2 ;



          int NN;
          int aNode;
          String strNodeAttr;

          NodeList=new String[100] ;
          NodeList2=new int[100];


          double[] bbox;

          bbox=oMpSection.CalculateBBOX();
          lat1=bbox[0];
          lon1=bbox[1];
          lat2=bbox[2];
          lon2=bbox[3];


          NN=0;
          while (true)
          {
            strNodeAttr = oMpSection.GetAttributeValue("Nod" + NN);
            if (strNodeAttr.equals("") ) break;

            NodeList[NN] =  strNodeAttr.split(",")[1];
            NodeList2[NN] = Integer.parseInt(strNodeAttr.split(",")[2]) ;

            NN++;
          }
          //Нужно передать список рутинговых нод, и bbox для данной области.
          //Все дороги кроме service
          if (OSMLevelByTag(oMpSection.GetOsmHighway()) <= 4)
            oConnectivityTest.AddRoad(NN, NodeList, NodeList2,  lat1, lon1,lat2,lon2);

          //Trunk
          if (OSMLevelByTag(oMpSection.GetOsmHighway()) <= 0)
            oConnectivityTest0.AddRoad(NN, NodeList, NodeList2,  lat1, lon1,lat2,lon2);

          //Primary
          if (OSMLevelByTag(oMpSection.GetOsmHighway()) <= 1)
            oConnectivityTest1.AddRoad(NN, NodeList, NodeList2,  lat1, lon1,lat2,lon2);

          //Secondary
          if (OSMLevelByTag(oMpSection.GetOsmHighway()) <= 2)
            oConnectivityTest2.AddRoad(NN, NodeList, NodeList2,  lat1, lon1,lat2,lon2);

          //Tertiary
          if (OSMLevelByTag(oMpSection.GetOsmHighway()) <= 3)
            oConnectivityTest3.AddRoad(NN, NodeList, NodeList2,  lat1, lon1,lat2,lon2);

        }
      }

      //Статистика по исходному файлу
      oStatistic.ProcessSection(oMpSection);

      //Записываем секцию, если не было велено ее выкинуть.
      if (!blnSkipSection){
        oTgtMp.WriteSection(oSrcMp.CurrentSection);
      }
    }
    oTgtMp.Close();

    dtProcessEnd=new Date();

    PrintReport(strTarget + "_addr.xml");
  }

  //====================================================================================================================
  //Операции над секцией
  //====================================================================================================================
  //4,5 . Убьем слово улица, ул. в адресах и названиях улиц. Применим сокращения статусных частей улиц.
  private static void NormalizeStreetLabelsAndAddresses(clsMpSection oMpSection)
  {
    String strLabel;
    String mpType;
    mpType=oMpSection.GetAttributeValue("Type");

    //Убьем слово улица, ул. в названиях улиц (Label)
    if(oMpSection.SectionType.equals("[POLYLINE]")){
      if(
           ((mpType.compareTo("0x01")>=0) & (mpType.compareTo( "0x0C")<=0)) |
           mpType.equals("0x0a") | mpType.equals("0x0b") | mpType.equals("0x0c") |
           mpType.equals("0x16") | mpType.equals("0x8849") | mpType.equals("0x880a")
        )

      {
        strLabel=oMpSection.GetAttributeValue("Label");
        //Это особенная бага osm2mp. StreetDesc нерутинговым улицам не присваевается.
        //Тем не менее, оригинальное название нам понадобится для адресного теста.
        if (!strLabel.equals("") && oMpSection.GetAttributeValue("StreetDesc").equals("") ){
          oMpSection.SetAttributeValue("StreetDesc", strLabel);
        }

        //Нормализуем Label
        if (!strLabel.equals("")){
          strLabel = NormalizeStreetName(strLabel, true);
          oMpSection.SetAttributeValue("Label", strLabel);
        }
      }
    }

    //Нормализация названий улиц в адресах (StreetDesc)
    strLabel = oMpSection.GetAttributeValue("StreetDesc");
    if (!strLabel.equals("")){
      strLabel = NormalizeStreetName(strLabel, false);
      oMpSection.SetAttributeValue("StreetDesc", strLabel);
    }
  }
  //9. Классифицируем города по населению
  private static void ClassifyCitiesByPopulation(clsMpSection oMpSection)
  {
    String strPopulation;
    int intPopulation=0;
    if (oMpSection.SectionType.equals("[POI]"))
    {
      if(oMpSection.GetAttributeValue("City").equals("Y")){

        //Добавим в адрес название города, если там пусто
        if (oMpSection.GetAttributeValue("CityName").equals("")){
          oMpSection.SetAttributeValue("CityName", oMpSection.GetAttributeValue("Label"));
        }

        //Получим население
        strPopulation = oMpSection.GetAttributeValue("Population");
        if (!strPopulation.equals("")){
          try{
            intPopulation=Integer.parseInt(strPopulation);
          }
          catch (NumberFormatException e){
            System.out.println("unparsed population value: " + strPopulation);
          }
        }

        /*
          'Добавим город в адресный реестр.
          'Возможно нужно проверять еще и тип.
          If blnDoTests Then
            oAddrRegisty.AddCityToRegistry oMpSection.mpLabel, oMpSection.GetCoords, intPopulation, False, False, oMpSection.mpType
          End If
        */


        if (intPopulation > 0) {
          if (intPopulation >= 10000000) { // Мегаполис, >10 млн
            oMpSection.SetAttributeValue("Type","0x0100");
          }
          else if (intPopulation >= 5000000){ // Мегаполис, 5-10 млн
            oMpSection.SetAttributeValue("Type", "0x0200");
          }
          else if (intPopulation >= 2000000){ // Крупный город, 2-5 млн
            oMpSection.SetAttributeValue("Type", "0x0300");
          }
          else if(intPopulation >= 1000000){ // Крупный город, 1-2 млн
            oMpSection.SetAttributeValue("Type", "0x0400") ;
          }
          else if(intPopulation >= 500000){ // Город 0.5-1 млн
            oMpSection.SetAttributeValue("Type", "0x0500") ;
          }
          else if(intPopulation >= 200000){ // Город 200 тыс - 500 тыс
            oMpSection.SetAttributeValue("Type", "0x0600");
          }
          else if(intPopulation >= 100000){ // Город 100 тыс - 200 тыс
            oMpSection.SetAttributeValue("Type", "0x0700");
          }
          else if(intPopulation >= 50000){ // Город 50 тыс - 100 тыс
            oMpSection.SetAttributeValue("Type", "0x0800");
          }
          else if(intPopulation >= 20000){ // Город 20 тыс - 50 тыс
            oMpSection.SetAttributeValue("Type", "0x0900");
          }
          else if(intPopulation >= 10000){ // Город 10 тыс - 20 тыс
            oMpSection.SetAttributeValue("Type", "0x0a00");
          }
          else if(intPopulation >= 5000){ // населенный пункт 5 тыс - 10 тыс
            oMpSection.SetAttributeValue("Type", "0x0b00");
          }
          else if(intPopulation >= 5000){ // населенный пункт 2 тыс - 5 тыс
            oMpSection.SetAttributeValue("Type", "0x0c00");
          }
          else if(intPopulation >= 5000){ // населенный пункт 1 тыс - 2 тыс
            oMpSection.SetAttributeValue("Type", "0x0d00");
          }
          else{
            oMpSection.SetAttributeValue("Type", "0x0e00");
          }
        }

      }
    }

  }

  //11. Классифицируем озера по размеру
  private static void ClassifyLakesBySize(clsMpSection oMpSection) //throws Exception
  {
    double dblSize;

    if (oMpSection.SectionType.equals("[POLYGON]")){
      if (oMpSection.GetAttributeValue("Type").equals("0x3f")){   //Medium Lake
        dblSize = oMpSection.CalculateArea();


        if (dblSize  < 0.25)  {
          //EndLevel = 3
          oMpSection.SetAttributeValue("Type","0x41");
          oMpSection.SetAttributeValue("EndLevel", "2");
        }
        else if(dblSize <= 11){

          oMpSection.SetAttributeValue ("Type",  "0x40");
        }
        else if(dblSize <= 25){
          oMpSection.SetAttributeValue ("Type",  "0x3f");
          oMpSection.SetAttributeValue ("EndLevel", "4");
        }
        else if(dblSize <= 75){
          oMpSection.SetAttributeValue ("Type", "0x3e");
          oMpSection.SetAttributeValue ("EndLevel", "4");
        }
        else if(dblSize <= 250){
          oMpSection.SetAttributeValue ("Type", "0x3d");
          oMpSection.SetAttributeValue ("EndLevel", "4");
        }
        else if(dblSize <= 600){
          oMpSection.SetAttributeValue ("Type",  "0x3c");
          oMpSection.SetAttributeValue ("EndLevel", "4");
        }
        else if(dblSize <= 1100){
          oMpSection.SetAttributeValue ("Type", "0x44");
          oMpSection.SetAttributeValue ("EndLevel", "4");
        }
        else if(dblSize <= 3300){
          oMpSection.SetAttributeValue ("Type", "0x43");
          oMpSection.SetAttributeValue ("EndLevel", "4");
        }
        else{
          oMpSection.SetAttributeValue ("Type",  "0x42");
          oMpSection.SetAttributeValue ("EndLevel", "4" );
        }

        //Убьем слово "озеро" в названиях озер
        String strLabel = " " +oMpSection.GetAttributeValue("Label")+" ";
        if (!strLabel.equals("  ")){

          strLabel = strLabel.replaceAll( " озеро ", "");
          strLabel = strLabel.replaceAll( " водохранилище ", " вдхр. ");

          oMpSection.SetAttributeValue("Label",strLabel.trim());
        }
      }
    }
  }


  //16. Убьем CountryName, оно в СГ не используется
  private static void RemoveCountryAttribute(clsMpSection oMpSection)
  {
    if( !oMpSection.GetAttributeValue("CountryName").equals(""))
      oMpSection.DeleteAttribute("CountryName");

  }

  //************************ Полезности ********************************************************************************
  private static String NormalizeStreetName(String  strStreetName, boolean blnKillUl)
  {
    strStreetName = strStreetName.trim();

    int l;

    //В краткой форме слово "улица" не нужно
    l = strStreetName.length();
    if(blnKillUl){

      if (vb6.Left(strStreetName, 5).equalsIgnoreCase("улица") )
        strStreetName = vb6.Right(strStreetName, l - 5);

      if (vb6.Left(strStreetName, 3).equalsIgnoreCase("ул."))
        strStreetName = vb6.Right(strStreetName, l - 3);

      if (vb6.Right(strStreetName, 5).equalsIgnoreCase("улица"))
        strStreetName = vb6.Left(strStreetName, l - 5);
      if (vb6.Right(strStreetName, 3).equalsIgnoreCase("ул."))
        strStreetName = vb6.Left(strStreetName, l - 3);

    }


    //Применим сокращения

    strStreetName = " " + strStreetName.trim() + " ";

    //названия типа "6-я набережная" не сокращаются, во избежание "6-я наб."
    if(
            ! ( strStreetName.matches("^ [0-9]+-я Набережная $") | strStreetName.equals(" Набережная ") |
                strStreetName.matches("^ [0-9]+-я Набережная улица $") | strStreetName.equals(" Набережная улица "))
       ){
      strStreetName = strStreetName.replaceAll(" набережная ", " наб. ");
    }


    strStreetName = strStreetName.replaceAll( " проспект ", " пр. ");
    strStreetName = strStreetName.replaceAll( " площадь ", " пл. ");

    strStreetName = strStreetName.replaceAll( " переулок ", " пер. ");
    strStreetName = strStreetName.replaceAll( " проезд ", " пр-д. ");
    strStreetName = strStreetName.replaceAll( " шоссе ", " ш. ");
    strStreetName = strStreetName.replaceAll( " [Уу]лица ", " ул. ");

    /*
      ' 'Убьем пробел перед номером
      'strStreetName = strStreetName.replaceAll(, " № ", " №", , , vbTextCompare)

      ''Номер СГ таки  не понимает.
      'strStreetName = strStreetName.replaceAll(, " № ", " №", , , vbTextCompare)
    */

    //Обтримливание на всякий случай
    strStreetName = strStreetName.trim();

      //статусная часть переносится в конец
    if(!blnKillUl){
        l = strStreetName.length();
        if(vb6.Left(strStreetName, 3).equalsIgnoreCase("ул."))
          strStreetName = vb6.Right(strStreetName, l - 3) + " ул.";
        //If LCase(Left(strStreetName, 3)) = "пр." Then strStreetName = Right(strStreetName, l - 3) + " пр."
    }

    strStreetName = strStreetName.trim();
    return strStreetName;
  }

  private static void PrintReport(String strFileName)   throws IOException
  {
    BufferedWriter oReportFile;
    oReportFile = new BufferedWriter(new FileWriter(strFileName));

    Date dtCurrentDate;

    dtCurrentDate=new Date();

    oReportFile.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
    oReportFile.write( "<QualityReport>\r\n");
    oReportFile.write( " <Date>" + FormatXMLDate(dtCurrentDate, false) + "</Date>\r\n");
    oReportFile.write( " <DateWithTime>" + FormatXMLDate(dtCurrentDate, true) + "</DateWithTime>\r\n");
    oReportFile.write( " <TimeUsed>" + FormatXMLTimeInterval(dtProcessStart,dtProcessEnd ) + "</TimeUsed>\r\n");




    oReportFile.write( "<RoutingTest>\r\n");
    oConnectivityTest.PrintRegistryToXML(oReportFile);
    oReportFile.write( "</RoutingTest>\r\n");

    oReportFile.write( "<RoutingTestByLevel>\r\n");
    oReportFile.write( "<Trunk>\r\n");
    oConnectivityTest0.PrintRegistryToXML(oReportFile);
    oReportFile.write( "</Trunk>\r\n");

    oReportFile.write( "<Primary>\r\n");
    oConnectivityTest1.PrintRegistryToXML(oReportFile);
    oReportFile.write("</Primary>\r\n");

    oReportFile.write( "<Secondary>\r\n");
    oConnectivityTest2.PrintRegistryToXML(oReportFile);
    oReportFile.write( "</Secondary>\r\n");

    oReportFile.write( "<Tertiary>\r\n");
    oConnectivityTest3.PrintRegistryToXML(oReportFile);
    oReportFile.write( "</Tertiary>\r\n");
    oReportFile.write( "</RoutingTestByLevel>\r\n");

    //oDanglingRoads.PrintErrorsToXML (oReportFile);
    //oSourceErrors.PrintErrorsToXML  (oReportFile);
    oStatistic.PrintReportToXML(oReportFile);

    oReportFile.write( "</QualityReport>\r\n");

    oReportFile.close();
   

  }
  private static String FormatXMLDate(Date dtDate,boolean blnWithTime)
  {
    DateFormat dateFormat;
    if (blnWithTime)
      {dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");}
    else
      {dateFormat = new SimpleDateFormat("yyyy-MM-dd");}

    return dateFormat.format(dtDate);
  }

  private static String FormatXMLTimeInterval(Date dtStart,Date dtEnd)
  {
    long HH,mm,ss;
    long t;
    t=(dtEnd.getTime() - dtStart.getTime())/1000;

    ss=t%60;
    t=t/60;
    mm=t%60;
    t=t/60;
    HH=t;


    return Long.toString(HH)+":"+Long.toString(mm)+":"+Long.toString(ss);
  }

  private static int OSMLevelByTag(String Tag)
  {
    int intLevel;

    // else if (Tag.equals()||Tag.equals()  )

    if (Tag.equals("trunk") || Tag.equals("trunk_link") || Tag.equals("motorway") || Tag.equals("motorway_link")  )
    {
      intLevel = 0;
    }
    else if (Tag.equals("primary") || Tag.equals("primary_link"))
    {
      intLevel = 1;
    }
    else if (Tag.equals("secondary")||Tag.equals("secondary_link") )
    {
      intLevel = 2;
    }
    else if (Tag.equals("tertiary")||Tag.equals("tertiary_link") )
    {
      intLevel = 3;
    }
    else if (Tag.equals("residential")||Tag.equals("unclassified")||Tag.equals("living_street")||Tag.equals("checked_construction") )
    {
      intLevel = 4;
    }
    else if (Tag.equals("service"))
    {
      intLevel = 5;
    }
    else
    {
      intLevel = 5;
    }
    return intLevel;
  }
}
