import com.sun.org.apache.bcel.internal.generic.NEW;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 07.10.12
 * Time: 15:53
 * To change this template use File | Settings | File Templates.
 */
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
  public String mpRouteParam()
  {
    String tmp;
    tmp = GetAttributeValue("RouteParam");
    if(tmp.trim().equals("")){
      tmp = GetAttributeValue("RouteParams");
    }
    return tmp;
  }
  //Подсчеты разных свойств
  public double CalculateArea()
  {
    String   strData0;
  	String[] tmp;
    int N;
    int i;
    double[][]  dblCoords; // массив координат вершин полигона

    String strX;
    String strY;

    double s;

    //Найдем размер объекта в квадратных километрах

    //предполагаем, что Data0 содержит внешний контур полигона
    strData0 = GetAttributeValue("Data0");
    //Распарсим его.
    //Формат
    //(x1,y1),(x2,y2),(x3,y3), ...,(xN,yN)

    tmp = strData0.split("\\)\\,");

    N=tmp.length;
    dblCoords= new double[N+1][];


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


      /*System.out.print(dblCoords[i][0] );
      System.out.print(" " );
      System.out.println(dblCoords[i][1] );*/
    }

    //Убедимся, что полигон замкнутый
    if ((dblCoords[0][0]!= dblCoords[N-1][0]) || (dblCoords[0][1] != dblCoords[N-1][1])){
      //Если полигон не замкнутый(такое может быть по разным причинам), надо его замкнуть
      N = N + 1;
      dblCoords[N-1]=new double[2];
      dblCoords[N-1][0] = dblCoords[0][0];
      dblCoords[N-1][1] = dblCoords[0][1];
    }

    //Найдем площадь в квадратных градусах
    s = 0;
    for(i=0;i<N-1;i++){
      s = s + (dblCoords[i][0] - dblCoords[i + 1][0]) * (dblCoords[i][1] + dblCoords[i + 1][1]) / 2;
    }


   //Переведем площадь из квадратных градусов в км^2 (приближенно)
    s = s * l_grad * l_grad * Math.cos(dblCoords[0][0] * 3.141592653 / 180);

    //System.out.println(s);
    //System.out.println("");

    //Знак зависит от направления обхода, но площадь полигона так или иначе положительна
    return Math.abs(s);
  }
}
