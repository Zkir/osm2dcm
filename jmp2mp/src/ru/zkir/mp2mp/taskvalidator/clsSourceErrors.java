/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 03.11.12
 * Time: 19:41
 * To change this template use File | Settings | File Templates.
 */
package ru.zkir.mp2mp.taskvalidator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.*;

import ru.zkir.mp2mp.core.MPParseException;
import ru.zkir.mp2mp.vb6.vb6;



public class clsSourceErrors {

  ArrayList<LatLon> arrCoastLineBreaks;
  ArrayList<LatLon> arrDuplicateRoads;
  Pattern rd_pattern;
  Pattern cl_pattern;

  public clsSourceErrors()
  {
    arrCoastLineBreaks=new ArrayList<LatLon>();
    arrDuplicateRoads= new ArrayList<LatLon>();
    rd_pattern = Pattern.compile("; ERROR: Roads .+ duplicate segments near \\((-?[0-9.]+),(-?[0-9.]+)\\)");
    cl_pattern = Pattern.compile("; ERROR: Possible coastline break at \\((-?[0-9.]+),(-?[0-9.]+)\\) or \\((-?[0-9.]+),(-?[0-9.]+)\\)");
  }


  class LatLon
  {
   String lat;
   String lon;
  }

  public void ProcessComment(String strComment)  throws MPParseException
  {
    LatLon latlon;

    //Нас интересуют:
    //1-береговая линия.
    //; ERROR: Possible coastline break at (59.870581,30.1579854) or (59.8707318,30.1609809)
    if( vb6.Left(strComment, 36).equals("; ERROR: Possible coastline break at" ))
    {
      Matcher m = cl_pattern.matcher(strComment);
      if (m.matches())
      {
        latlon =new LatLon();
        latlon.lat =( m.group(1));
        latlon.lon =( m.group(2));
        arrCoastLineBreaks.add(latlon);

        latlon =new LatLon();
        latlon.lat =( m.group(3));
        latlon.lon =( m.group(4));
        arrCoastLineBreaks.add(latlon);
      }
      else
      {
        throw new MPParseException("Unable to parse coordinates");
      }
    }

    //2-Дупликаты рутинговых ребер.
    //; ERROR: Roads 91630559:0, 91630559:0, 91630559:0 have 1 duplicate segments near (52.6322258,38.4829784)
    if( vb6.Left(strComment, 14).equals("; ERROR: Roads") )
    {


      Matcher m = rd_pattern.matcher(strComment);
      if (m.matches()) {
        latlon =new LatLon();
        latlon.lat = m.group(1);
        latlon.lon = m.group(2);
        arrDuplicateRoads.add(latlon);
      }
      else
      {
        //Координаты не удалось вытащить
        System.out.println(rd_pattern.pattern());
        throw new MPParseException("Unable to parse coordinates");
      }



    }



  }

  public void PrintErrorsToXML(BufferedWriter oReportFile, boolean blnSummary)  throws IOException
  {
    int i;

    //1 - разрывы береговой линии.

    oReportFile.write( "<CoastLineTest>\r\n");
    oReportFile.write( "<Summary>\r\n");
    oReportFile.write( "  <NumberOfBreaks>" + Long.toString(arrCoastLineBreaks.size()) + "</NumberOfBreaks>\r\n");
    oReportFile.write( "</Summary>\r\n");

    if (!blnSummary)
    {
      oReportFile.write( "<BreakList>\r\n");
      for(i=0;i<arrCoastLineBreaks.size();i++)
      {
        oReportFile.write( "  <BreakPoint>\r\n");
        oReportFile.write( "    <Coord>\r\n");
        oReportFile.write( "      <Lat>" + arrCoastLineBreaks.get(i).lat  + "</Lat>\r\n");
        oReportFile.write( "      <Lon>" + arrCoastLineBreaks.get(i).lon  + "</Lon>\r\n");
        oReportFile.write( "    </Coord>\r\n");
        oReportFile.write( "  </BreakPoint>\r\n");
      }
      oReportFile.write("</BreakList>\r\n");
    }
    oReportFile.write("</CoastLineTest>\r\n");


    //2-Дупликаты рутинговых ребер.

    oReportFile.write( "<RoadDuplicatesTest>\r\n" );
    oReportFile.write( "<Summary>\r\n");
    oReportFile.write( "  <NumberOfDuplicates>" + Long.toString(arrDuplicateRoads.size()) + "</NumberOfDuplicates>\r\n");

    oReportFile.write( "</Summary>\r\n");
    if (!blnSummary)
    {
      oReportFile.write( "<DuplicateList>\r\n");
      for(i=0;i<arrDuplicateRoads.size();i++)
      {
        oReportFile.write( "  <DuplicatePoint>\r\n" );
        oReportFile.write( "    <Coord>\r\n" );
        oReportFile.write( "      <Lat>" + arrDuplicateRoads.get(i).lat + "</Lat>\r\n");
        oReportFile.write( "      <Lon>" + arrDuplicateRoads.get(i).lon + "</Lon>\r\n");
        oReportFile.write( "    </Coord>\r\n");
        oReportFile.write( "  </DuplicatePoint>\r\n");
      }
      oReportFile.write( "</DuplicateList>\r\n");
    }
    oReportFile.write( "</RoadDuplicatesTest>\r\n");

  }

}
