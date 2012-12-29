import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 07.10.12
 * Time: 15:53
 * To change this template use File | Settings | File Templates.
 */

class MPParseException extends Exception {
  public MPParseException(String ErrorMsg)
  {
    super(ErrorMsg);
  }
}


public class clsMpSection {

  public String SectionType;
  public String SectionEnding;
  public ArrayList<String> oComments;
  public ArrayList<String> oAttributes;
  private final double l_grad = 111.321322222222;

  public clsMpSection()
  {
    oComments =new ArrayList<String>();
    oAttributes=new ArrayList<String>();
  }
  //Добавление комментария
  public void AddCommentLine(String strComment)
  {
    oComments.add(strComment);
  }

  //Добавление атрибута
  public void AddAttributeLine(String strLine)
  {
    oAttributes.add(strLine);
  }

  public String GetAttributeValue(String strAttributeName)
  {
    String str;
    String strAttributeValue="";
    String[] strpp;
    int i;
    for(i=0;i<oAttributes.size();i++ )
    {
      str=oAttributes.get(i);
      strpp=str.split("=",2);
      if (strpp[0].equals(strAttributeName) )
      {
        strAttributeValue=strpp[1];
        break;
      }
    }
    return strAttributeValue;
  };

  public void SetAttributeValue(String strAttributeName, String strAttributeValue)
  {
    String str;

    String[] strpp;
    int i;
    int k=-1;
    for(i=0;i<oAttributes.size();i++ )
    {
      str=oAttributes.get(i);
      strpp=str.split("=",2);
      if (strpp[0].equals(strAttributeName) )
      {
        k=i;
        break;
      }
    }
    if (k==-1){
      oAttributes.add(strAttributeName+"="+strAttributeValue);
    }
    else{
      oAttributes.set(k,strAttributeName+"="+strAttributeValue);
    }

  };

  public void DeleteAttribute(String strAttributeName)
  {
    String str;

    String[] strpp;
    int i;
    for(i=0;i<oAttributes.size();i++ )
    {
      str=oAttributes.get(i);
      strpp=str.split("=",2);
      if (strpp[0].equals(strAttributeName) )
      {
        oAttributes.remove(i);
        break;
      }
    }

  }
  public String mpType()
  {
    return GetAttributeValue("Type");
  }
  public String mpRouteParam()
  {
    String tmp;
    tmp = GetAttributeValue("RouteParam");
    if(tmp.trim().equals("")){
      tmp = GetAttributeValue("RouteParams");
    }
    return tmp;
  }

  // массив координат вершин полигона (почему-то замкнутый)
  private double[][] GetCoordArray(boolean blnForceClosed)  throws MPParseException
  {
    double[][]  dblCoords; // массив координат вершин полигона

    String   strData0;
    String[] tmp;
    int N;
    String strX;
    String strY;
    int i;
    int intLevel;


    //предполагаем, что Data0 содержит внешний контур полигона
    strData0 = GetAttributeValue("Data0");
    if (strData0.equals(""))
    {

      for(intLevel=1;intLevel<6;intLevel++)
      {
         strData0 = GetAttributeValue("Data"+Integer.toString(intLevel));
         if(!strData0.equals(""))
         {
           break;
         }
      }
      if (strData0.equals(""))
      {
        throw new MPParseException("unable to find object coordinates(DataX attribute) ");
      }
    }



    //Распарсим его.
    //Формат
    //(x1,y1),(x2,y2),(x3,y3), ...,(xN,yN)

    tmp = strData0.split("\\)\\,");

    N=tmp.length;
    if (blnForceClosed)
      {
        dblCoords= new double[N+1][];
      }
    else
    {
      dblCoords= new double[N][];
    }


    for(i=0;i<N;i++){

      dblCoords[i]=new double[2];

      strX = tmp[i].split(",")[0].trim();  //Широта
      strY = tmp[i].split(",")[1].trim(); //Долгота

      //Широта
      dblCoords[i][0] = Double.parseDouble(vb6.Right(strX, strX.length() - 1)) ;


      //Долгота
      if (i == N-1){
        dblCoords[i][1] = Double.parseDouble(vb6.Left(strY, strY.length()-1));
      }
      else{
        dblCoords[i][1] = Double.parseDouble(strY);
      }

    }

    //Убедимся, что полигон замкнутый
    if (blnForceClosed)
    {
      if ((dblCoords[0][0]!= dblCoords[N-1][0]) || (dblCoords[0][1] != dblCoords[N-1][1])){
        //Если полигон не замкнутый(такое может быть по разным причинам), надо его замкнуть
        N = N + 1;
        dblCoords[N-1]=new double[2];
        dblCoords[N-1][0] = dblCoords[0][0];
        dblCoords[N-1][1] = dblCoords[0][1];
      }
    }

    return dblCoords;
  }

  //Получение bbox объекта
  public double[] CalculateBBOX() throws MPParseException
  {
    double[] bbox;
    double[][] Coords;
    double lat,lon;
    double lat1,lon1,lat2,lon2;
    int i;

    Coords=GetCoordArray(false);

    //Начнем с первой точки
    lat1 = Coords[0][0];
    lon1 = Coords[0][1];
    lat2 = Coords[0][0];
    lon2 = Coords[0][1];

    for(i=1;i<Coords.length;i++)
    {
      lat = Coords[i][0];
      lon = Coords[i][1];

      if (lat < lat1)  lat1 = lat;
      if (lat > lat2)  lat2 = lat;

      if (lon < lon1)  lon1 = lon;
      if (lon > lon2)  lon2 = lon;
    }

    bbox=new double[4];
    bbox[0]=lat1;
    bbox[1]=lon1;
    bbox[2]=lat2;
    bbox[3]=lon2;

    return bbox;
  }

  //Получение координат первой и последней точки
  public double[] CalculateFirstLast() throws MPParseException
  {
    double[] bbox;
    double[][] Coords;
    double lat1,lon1,lat2,lon2;

    Coords=GetCoordArray(true);

    //первая точка
    lat1 = Coords[0][0];
    lon1 = Coords[0][1];

    //Последняя точка.   Coords - c какого-то хрена замкнутый массив, поэтому length-2
    lat2 = Coords[Coords.length-2][0];
    lon2 = Coords[Coords.length-2][1];

    bbox=new double[4];
    bbox[0]=lat1;
    bbox[1]=lon1;
    bbox[2]=lat2;
    bbox[3]=lon2;

    return bbox;
  }

  //Получение координат первой точки
  public double[] GetCoord() throws MPParseException
  {
    double[] bbox;
    double[][] Coords;
    double lat1,lon1,lat2,lon2;

    Coords=GetCoordArray(true);

    //первая точка
    lat1 = Coords[0][0];
    lon1 = Coords[0][1];

    bbox=new double[2];
    bbox[0]=lat1;
    bbox[1]=lon1;

    return bbox;
  }



  //Подсчеты разных свойств
  public double CalculateArea() throws MPParseException
  {

    int i;
    double[][]  dblCoords; // массив координат вершин полигона


    double s;

    //Найдем размер объекта в квадратных километрах


    dblCoords=GetCoordArray(true);

    //Найдем площадь в квадратных градусах
    s = 0;
    for(i=0;i<dblCoords.length-1-1;i++){
      s = s + (dblCoords[i][0] - dblCoords[i + 1][0]) * (dblCoords[i][1] + dblCoords[i + 1][1]) / 2;
    }


   //Переведем площадь из квадратных градусов в км^2 (приближенно)
    s = s * l_grad * l_grad * Math.cos(dblCoords[0][0] * 3.141592653 / 180);

    //System.out.println(s);
    //System.out.println("");

    //Знак зависит от направления обхода, но площадь полигона так или иначе положительна
    return Math.abs(s);
  }

  //Найдем длинну линии в километрах
  public double CalculateLength() throws MPParseException
  {
    final double coeff=111.1;// длинна одного градуса дуги в км

    int i;
    double dblLen;
    double deltaLat,AvgLat,deltaLon;

    double[][]  dblCoords; // массив координат вершин полигона
    //Получим массив координат
    dblCoords=GetCoordArray(true);

    //Сложим длинну сегментов
    dblLen = 0;
    for(i=0;i<dblCoords.length-1-1;i++){

      deltaLat = dblCoords[i][0] - dblCoords[i + 1][0];
      deltaLon = dblCoords[i][1] - dblCoords[i + 1][1];
      AvgLat =  (dblCoords[i][0] + dblCoords[i + 1][0]) / 2;

      double x,y;
      x=(coeff * deltaLat);
      y=(coeff * Math.cos(AvgLat * 3.141592653 / 180) * deltaLon);


      dblLen = dblLen + Math.sqrt(x*x+y*y) ;

    }

    return dblLen;

  }

  //Значение осм-тега, оно сидит в комментариях
  public String GetOsmHighway() throws MPParseException
  {
    int i,j;


    String strCommentLine;
    String strValue;
    //Dim j, i As Integer

    for (j=0;j<oComments.size(); j++ )
    {
      strCommentLine = oComments.get(j);

      i= strCommentLine.indexOf("highway =");  //  i = InStr(strCommentLine, )
      if(i > 0)
      { // Найдено
        strCommentLine = strCommentLine.substring(i);

        strValue = strCommentLine.split("=")[1].trim();
        return strValue;
      }

    }
    throw new MPParseException("Unable to find osm highway tag");
  }

  public boolean isOneWay()
  {

    return !mpRouteParam().split(",")[2].equals("0") ;

  }

  public long SizeInBytes()
  {
    int i;
    int intSize;
    intSize=0;

    for(i=0;i<oAttributes.size();i++ )
    {
      intSize=intSize+ oAttributes.get(i).length();
    }
    return intSize;
  }
}
