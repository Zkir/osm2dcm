/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 26.03.13
 * Time: 20:57
 * To change this template use File | Settings | File Templates.
 */
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.ParseException;


import java.io.*;
public class PolyBuffer {

  public static void main(String[] args)    throws IOException,ParseException
  {
    DouglasPeuckerSimplifier  dps;
    System.out.println("PolyBuffer, (c) Zkir 2013");
    String strSourceFile;
    String strOutFile;
    double dblBufferSize;
    double dblSimplValue;
    strSourceFile=args[0];
    strOutFile=args[1];
    if (args[2]!="")
    {
      dblBufferSize= Double.parseDouble(args[2]);
      dblSimplValue=dblBufferSize/5;

    }
    else
    {
      dblBufferSize=0.01;
      dblSimplValue= 0.002;
    }

    Geometry g1=ReadFromPoly(strSourceFile);

    Geometry g2=g1.buffer(dblBufferSize,3);

    dps= new DouglasPeuckerSimplifier(g2);
    dps.setDistanceTolerance(dblSimplValue);

    SaveToPoly(dps.getResultGeometry(),strOutFile);



  }
  static Geometry ReadFromPoly(String strFileName) throws IOException,ParseException
  {
    BufferedReader oInFile;
    oInFile = new BufferedReader(new InputStreamReader(new FileInputStream(strFileName), "windows-1251"));
    String strLine = null;
    String strCoordLine="";
    double lat, lon;
    oInFile.readLine();//Header, we are not interested in it
    strLine =oInFile.readLine();
    if (strLine.equals("") )
      oInFile.readLine();
    while( (strLine = oInFile.readLine()) != null) {
      strLine = strLine.trim();
      if (strLine.equalsIgnoreCase("end") )
      {
        break;
      }
      String[] cr=strLine.split(" ",2);

      //lat=Double.parseDouble(cr[0]);
      //lon=Double.parseDouble(cr[1]);
      if (strCoordLine.equals(""))
      {strCoordLine=cr[0].trim()+" "+cr[1].trim();}
      else
      {strCoordLine=strCoordLine+", "+cr[0].trim()+" "+cr[1].trim();}


    }


    Geometry g1 = new WKTReader().read("POLYGON (("+strCoordLine+"))");
    //Geometry g1;

    /*
    Coordinate[] coordinates = new Coordinate[] {
            new Coordinate(0, 0),
            new Coordinate(0, 10),
            new Coordinate(20, 20),
            new Coordinate(10, 0),
            new Coordinate(0, 0),
    };

    g1 = new GeometryFactory().createPolygon(coordinates) ;
    g1 = new GeometryFactory().createPolygon()
    */

    return g1;
  }


  static void SaveToPoly(Geometry g1,String strFileName) throws IOException
  {
    BufferedWriter oOutFile;
    oOutFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(strFileName), "windows-1251"));

    Coordinate[] coords=g1.getCoordinates();
    oOutFile.write("TEST"+"\r\n");
    oOutFile.write("1\r\n");
    for (int i=0;i<coords.length;i++)
    {
      oOutFile.write("     "+ (float)coords[i].x+" "+(float)coords[i].y+"\r\n");
    }

    oOutFile.write("END\r\n");
    oOutFile.write("END\r\n");
    oOutFile.close();

  };
}