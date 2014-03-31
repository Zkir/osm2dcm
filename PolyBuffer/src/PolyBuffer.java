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
import com.vividsolutions.jts.geom.util.*;


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
    }
    else
    {
      dblBufferSize=0.01;
    }
    if ((args.length>3) && (args[3]!=""))
    {
      dblSimplValue= Double.parseDouble(args[3]);
    }
    else
    {
      dblSimplValue=dblBufferSize/5;
    }

    Geometry g1,g2,g3;
    g1=ReadFromPoly(strSourceFile);
    if (dblBufferSize>0)
    {
      g2=g1.buffer(dblBufferSize,3);
      //GeometryComponentFilter gcfilter;
      //gcfilter =new LinearComponentExtracter( a,b ) ;
      //g2.apply(gcfilter);
    }
    else
    {
      g2=g1;
    }
    if (dblSimplValue>0)
    {
      dps= new DouglasPeuckerSimplifier(g2);
      dps.setDistanceTolerance(dblSimplValue);
      g3=dps.getResultGeometry();
    }
    else
    {
      g3=g2;
    }
    if (g3.getGeometryType().equals("Polygon"))
    {
      g3=((Polygon)g3).getExteriorRing();
    }
    if (g3.getGeometryType().equals("MultiPolygon"))
    {
      g3=((Polygon)((MultiPolygon)g3).getGeometryN(0)).getExteriorRing();
    }

    SaveToPoly(g3,strOutFile);

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
      //oOutFile.write("     "+ (float)coords[i].x+" "+(float)coords[i].y+"\r\n");
      oOutFile.write("   "+ coords[i].x+"    "+coords[i].y+" \r\n");
    }

    oOutFile.write("END\r\n");
    oOutFile.write("END\r\n");
    oOutFile.close();

  };
}