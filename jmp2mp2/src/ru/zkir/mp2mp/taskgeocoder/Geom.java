package ru.zkir.mp2mp.taskgeocoder;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: KBondarenko
 * Date: 15/01/13
 * Time: 16:23
 * To change this template use File | Settings | File Templates.
 */
class Point
{
   double lat;
   double lon;
   Point (double aLat, double aLon)
   {
     lat=aLat;
     lon=aLon;
   }
}
class BorderSegment
{
  Point p0;
  Point p1;
  BorderSegment (Point ap0, Point ap1)
  {
    p0=ap0;
    p1=ap1;
  }
}

//Замкнутый контур, первая точка должна быть равна последней.
// По правде сказать, нужно просто множество отрезков границы, не дырявое. остальное неважно.
class Polygon
{
  ArrayList<BorderSegment> borderSegments;
  final double DELTA=1e-10;
  BBox bbox;
  Polygon()
  {
    borderSegments=new ArrayList<BorderSegment>();
  }
  double min(double a, double b)
  {
    if (a< b)
      {return a;}
    else
    {return b;}
  }

  double max(double a, double b)
    {if (a> b)
    {return a;}
    else
    {return b;}}

  //Проверка того что точка находится внутри полигона,
  //проверяется число пересечений (вертикального) луча, выпущенного из точки, с границей полигона.
  boolean isInside(double lat, double lon)
  {
    //Проверка ббоккса
    if (bbox==null)
    {
      bbox=calculateBBox();
    }
    if(
         (lat<bbox.minLat)||(lon<bbox.minLon)||(lat>bbox.maxLat)||(lon>bbox.maxLon)
       )
    {
      //Точка, которая находится вне ббокса, не может принадлежать полигону
      return false;
    }


    //Проверка пересечений
    int i;
    int intrsectCount=0;
    for (i=0;i<borderSegments.size();i++)
    {
      //Найдем пересечения вертикального луча с данным ребром.
      //lat0<lat<=lat0, lon>lonx
      Point p0,p1;
      p0=borderSegments.get(i).p0; //Начало отрезка
      p1=borderSegments.get(i).p1;   //Начало конец

      if ((lon==p0.lon) || (lon==p1.lon))
      {
        //Попали на вершину, это П-Ц
        //Нужно чуть чуть сдвинуть точку
         lon=lon+DELTA;
      }


      if ((lon>=min(p0.lon,p1.lon) && (lon<=max(p0.lon,p1.lon))))
      {

          //Найдем точку пересечения.
          double latX;
          latX= p0.lat+  (p1.lat-p0.lat)/(p1.lon-p0.lon)* (lon-p0.lon);

          if (latX>=lat)
          {
            intrsectCount++;
          }

      }
      else
      {
        //Отрезок идет нафик, пересечение с ним невозможно.
      }


    }
    //System.out.println(intrsectCount);
    return (intrsectCount%2)==1;

  }
  //Вычисление bbox
  BBox calculateBBox()
  {
    BBox bbox=new BBox();

    bbox.minLat=borderSegments.get(0).p0.lat;
    bbox.minLon=borderSegments.get(0).p0.lon;
    bbox.maxLat=borderSegments.get(0).p0.lat;
    bbox.maxLon=borderSegments.get(0).p0.lon;

    for (int i=0;i<borderSegments.size();i++)
    {
      Point p0,p1;
      p0=borderSegments.get(i).p0; //Начало отрезка
      p1=borderSegments.get(i).p1;   //конец отрезка

      if (p0.lat<bbox.minLat)
        {bbox.minLat=p0.lat;}
      if (p0.lat>bbox.maxLat)
        {bbox.maxLat=p0.lat;}

      if (p0.lon<bbox.minLon)
          {bbox.minLon=p0.lon;}
      if (p0.lon>bbox.maxLon)
          {bbox.maxLon=p0.lon;}


          }
    return bbox;
  }
}
  class BBox
  {
    double minLat;
    double maxLat;
    double minLon;
    double maxLon;
  }
