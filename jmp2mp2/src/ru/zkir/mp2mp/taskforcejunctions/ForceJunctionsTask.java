package ru.zkir.mp2mp.taskforcejunctions;

import ru.zkir.mp2mp.core.MPParseException;
import ru.zkir.mp2mp.core.MpData;
import ru.zkir.mp2mp.core.MpSection;
import ru.zkir.mp2mp.core.TaskInfo;
//Библиотеки rtree
import org.slf4j.*;
import com.infomatiq.jsi.*;
import gnu.trove.*;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.lang.Math;

import ru.zkir.mp2mp.taskvalidator.clsDeadEndTest;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 06.03.13
 * Time: 23:27
 * To change this template use File | Settings | File Templates.
 */
public class ForceJunctionsTask {

  // Очень простая таска.
  // Нужно сделать рутинговые ноды в местах пересечения дорог.
  // Это нужно для еврообзорки
  // Алгоритм
  // Будем перебирать дороги попарно, и сравнивать пересекаются они или нет
  // Сперва по ббоксам, потом по ребрам полилиний.
  // Если общая рутинговая нода уже есть, ничего не делаем
  // Если общей рутинговой ноды нет, но есть точка пересечения, добавляем точку и рутинговую ноду в обе полилинии.

  //Алгоритм, чего доброго, двухпроходный.
  //Потому что нужно знать номер последней рутинговой вершины.
  private DecimalFormat latLonFormat;

  public ForceJunctionsTask()
  {
    //latLonFormat = new DecimalFormat("0.#######");
    latLonFormat = new DecimalFormat("0.############");
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator('.');
    latLonFormat.setDecimalFormatSymbols(dfs);
  }

  public void execute(MpData mpData, TaskInfo taskInfo)   throws MPParseException
  {
    int i,j;
    MpSection  ms1,ms2;
    int MaxRNodeID;

    //Найдем максимальную ноду. Она нам понадобится для вставки новых нод пересечений
    MaxRNodeID=0;
    for (i=0;i<mpData.sections.size() ;i++ )
    {
      ms1=mpData.sections.get(i);
      int RNodeID=getMaxRnodeID(ms1);

      if (RNodeID>MaxRNodeID)
      {MaxRNodeID=RNodeID;}
    }
    System.out.println(" MaxRNodeID "+MaxRNodeID);


    //Определим список тупиков
    System.out.println("Finding dead ends");
    clsDeadEndTest oDeadEndTest=new clsDeadEndTest();
    for (i=0;i<mpData.sections.size() ;i++ )
    {
      if (i%1000==0)
      {
        System.out.println(" i "+i);
      }
      ms1=mpData.sections.get(i);
      if (ms1.SectionType.equals( "[POLYLINE]")){
        if(!ms1.mpRouteParam().equals("") ){
          AddRoadToDeadEndTest(ms1,oDeadEndTest,1);
       }
      }
    }

    //"Удлинним" дороги, оканчивающиеся тупиками
    System.out.println(" Extending roads ");
    double delta=0.01; // На сколько градусов удлинняем
    for (i=0;i<mpData.sections.size() ;i++ )
    {
      if (i%1000==0)
      {
        System.out.println(" i "+i);
      }
      ms1=mpData.sections.get(i);
      if(ms1.SectionType.equals("[POLYLINE]") && !ms1.mpType().toLowerCase().equals("0x1b"))
      {
      //нужно найти первый отрезок и последний отрезок полилинии
        double [][] coords= ms1.GetCoordArray(false);
        String [] RNodes=getRNodesArray(ms1,coords.length);

        boolean blnCoordsUpdated;
        double len;
        blnCoordsUpdated=false;

        if (oDeadEndTest.isDeadEnd(RNodes[0]) )
        {
          len=Math.pow(Math.pow(coords[0][0]-coords[1][0],2) + Math.pow(coords[0][1]-coords[1][1],2),0.5);
          coords[0][0]=coords[0][0]+delta*(coords[0][0]-coords[1][0])/len;
          coords[0][1]=coords[0][1]+delta*(coords[0][1]-coords[1][1])/len;
          blnCoordsUpdated=true;
        }

        if (oDeadEndTest.isDeadEnd(RNodes[coords.length-1]))
        {
          int nlastnode=coords.length-1;
          len=Math.pow(Math.pow(coords[nlastnode][0]-coords[nlastnode-1][0],2) + Math.pow(coords[nlastnode][1]-coords[nlastnode-1][1],2),0.5);
          coords[nlastnode][0]=coords[nlastnode][0]+delta*(coords[nlastnode][0]-coords[nlastnode-1][0])/len;
          coords[nlastnode][1]=coords[nlastnode][1]+delta*(coords[nlastnode][1]-coords[nlastnode-1][1])/len;
          blnCoordsUpdated=true;
        }
        if (blnCoordsUpdated)
        {
          //Координаты вершин изменились, обновим DataO
          String strCoords="";
          for (int k=0;k<coords.length;k++ )
          {
            if (!strCoords.isEmpty())
            {strCoords=strCoords+",";}
            strCoords=strCoords+"("+latLonFormat.format(coords[k][0])+","+latLonFormat.format(coords[k][1])+")";
          }
          ms1.SetAttributeValue("Data0",strCoords);
        }
      }
    }

    //создадим геоиндекс.
    SpatialIndex si = new RTree();
    si.init(null);

    for (i=0;i<mpData.sections.size() ;i++ )
    {
      ms1=mpData.sections.get(i);
      if(ms1.SectionType.equals("[POLYLINE]"))
      {
        double[] coords=ms1.CalculateBBOX();
        si.add(new Rectangle((float)coords[0],(float)coords[1],(float)coords[2],(float)coords[3]), i);
      }
    }

    System.out.println("Geoindex created, "+ si.size()+" element(s)");



    class SaveToListProcedure implements TIntProcedure {
      private ArrayList<Integer> ids = new ArrayList<Integer>();

      @Override
      public boolean execute(int id) {
        ids.add(id);
        return true;
      };

      private ArrayList<Integer> getIds() {
        return ids;
      }
    };


    //Поиск пересечений.
    for (i=0;i<mpData.sections.size() ;i++ )
    {
      if (i%1000==0)
      {
        System.out.println(" i "+i);
      }
      ms1=mpData.sections.get(i);
      if(ms1.SectionType.equals("[POLYLINE]") && !ms1.mpType().toLowerCase().equals("0x1b"))
      {
        SaveToListProcedure myProc = new SaveToListProcedure();
        double[] coords=ms1.CalculateBBOX();
        si.intersects(new Rectangle((float)coords[0],(float)coords[1],(float)coords[2],(float)coords[3]), myProc) ;

        ArrayList<Integer> ids = myProc.getIds();

        for (Integer id : ids)
        {
          //System.out.println(id + " was contained");
          if (id==i)
          {
            //Самопересечения искать НЕ надо
            continue;
          }
          ms2=mpData.sections.get(id);

          Intersection intersection_info=doesPolyLinesCross(ms1,ms2);
          if((intersection_info!=null)&& intersection_info.blnTheyIntersect )
          {
            MaxRNodeID=MaxRNodeID+1;
            insertRoutingNode(ms1,intersection_info.intSegNo1,intersection_info.intersection_x,intersection_info.intersection_y,MaxRNodeID);
            insertRoutingNode(ms2,intersection_info.intSegNo2,intersection_info.intersection_x,intersection_info.intersection_y,MaxRNodeID);
          }
        }
        //Дополнительная операция
        //Удалим атрибут односторонней дороги
        ms1.DeleteAttribute("DirIndicator");
        String rp=ms1.GetAttributeValue("RouteParam");
        rp=rp.substring(0,4)+"0,"+rp.substring(6);
        ms1.SetAttributeValue("RouteParam",rp);
      }
    }


    //После того как пересечения найдены, нетронутые тупики нужно укротить обратно.
    //Если тупиковый сегмент (в начале или конце полилинии) длиннее delta, значит с ним ничего не случилось,
    //и его можно безболезненно перенести обратно

    //==Безусловное удаление коротких отростков ==
    //Нужно укоротить на 2*delta, удалив, при необходимости короткие полилинии целиком.
    System.out.println("Finding dead ends");
    //Прогоним тест тупиков еще раз - могли появиться новые рутинговые ноды.
    oDeadEndTest=new clsDeadEndTest();
    for (i=0;i<mpData.sections.size() ;i++ )
    {
      if (i%1000==0)
      {
        System.out.println(" i "+i);
      }
      ms1=mpData.sections.get(i);
      if (ms1.SectionType.equals( "[POLYLINE]")){
        if(!ms1.mpRouteParam().equals("") ){
          AddRoadToDeadEndTest(ms1,oDeadEndTest,1);
        }
      }
    }


    System.out.println(" Shortening roads 1");
    for (i=0;i<mpData.sections.size() ;i++ )
    {
      if (i%1000==0)
      {
        System.out.println(" i "+i);
      }
      ms1=mpData.sections.get(i);
      if(ms1.SectionType.equals("[POLYLINE]") && !ms1.mpType().toLowerCase().equals("0x1b"))
      {
        //нужно найти первый отрезок и последний отрезок полилинии
        double [][] coords= ms1.GetCoordArray(false);
        String[]  RNodeList;
        RNodeList=getRNodesArray(ms1,coords.length);
        boolean blnCoordsUpdated;
        double len;
        blnCoordsUpdated=false;
        double epsilon=0.0001;
        //Первая вершина полилинии
        if (oDeadEndTest.isDeadEnd(RNodeList[0]))
        {
          len=Math.pow(Math.pow(coords[0][0]-coords[1][0],2) + Math.pow(coords[0][1]-coords[1][1],2),0.5);
          if (len-delta>epsilon)
          {
            coords[0][0]=coords[0][0]-delta*(coords[0][0]-coords[1][0])/len;
            coords[0][1]=coords[0][1]-delta*(coords[0][1]-coords[1][1])/len;
            blnCoordsUpdated=true;
          }
        }
        //Последняя вершина полилинии
        if (oDeadEndTest.isDeadEnd(RNodeList[coords.length-1]))
        {
          int nlastnode=coords.length-1;
          len=Math.pow(Math.pow(coords[nlastnode][0]-coords[nlastnode-1][0],2) + Math.pow(coords[nlastnode][1]-coords[nlastnode-1][1],2),0.5);
          if (len-delta>epsilon)
          {
            coords[nlastnode][0]=coords[nlastnode][0]-delta*(coords[nlastnode][0]-coords[nlastnode-1][0])/len;
            coords[nlastnode][1]=coords[nlastnode][1]-delta*(coords[nlastnode][1]-coords[nlastnode-1][1])/len;
            blnCoordsUpdated=true;
          }
        }
        if (blnCoordsUpdated)
        {
          //Координаты вершин изменились, обновим DataO
          String strCoords="";
          for (int k=0;k<coords.length;k++ )
          {
            if (!strCoords.isEmpty())
            {strCoords=strCoords+",";}
            strCoords=strCoords+"("+latLonFormat.format(coords[k][0])+","+latLonFormat.format(coords[k][1])+")";
          }
          ms1.SetAttributeValue("Data0",strCoords);
        }

      }
    }



    System.out.println(" Shortening roads 2");
    //"Укоротим" дороги, оканчивающиеся тупиками
    //Алгоритм.
    //1. Взять концевую вершину.
    //2. Убедиться что она тупиковая.
    //3. Найти предыдущую рутинговую вершину.
    //4. если расстояние меньше дельты, дропнуть все узлы до этой вершины.
    //   Если она сама тупиковая, удалить всю полилинию.
    for (i=0;i<mpData.sections.size() ;i++ )
    {
      if (i%1000==0)
      { System.out.println(" i "+i); }

      ms1=mpData.sections.get(i);
      if(ms1.SectionType.equals("[POLYLINE]") && !ms1.mpType().toLowerCase().equals("0x1b"))
      {
        //Узлы с координатами
        double [][] coords;
        //Номера рутинговых нод. Массив имеет тот же размер, что и координатный, с пропусками.
        String[] RNodeList;

        coords = ms1.GetCoordArray(false);
        RNodeList=getRNodesArray(ms1,coords.length);

        double len;
        int intNextNode;

        if (oDeadEndTest.isDeadEnd(RNodeList[0]))
        {
          //Теперь нужно найти следущую вершину
          intNextNode=0;
          for (j=1;j<coords.length;j++)
          {
            if (RNodeList[j]!=null)
            {
              intNextNode=j;
              break;
            }
          }
          if (intNextNode==0)
          {
            throw new MPParseException("Next node is not found");
          }

          len=Math.pow(Math.pow(coords[0][0]-coords[intNextNode][0],2) + Math.pow(coords[0][1]-coords[intNextNode][1],2),0.5);
          if (len<2*delta)
          {
            removeRoutingNode(ms1,0,intNextNode);
          }
        }
        //Перезачитаем список вешин, потому что он мог измениться.
        coords = ms1.GetCoordArray(false);
        RNodeList=getRNodesArray(ms1,coords.length);


        if (oDeadEndTest.isDeadEnd(RNodeList[coords.length-1]))
        {
          int intLastNode=coords.length-1;
          intNextNode=intLastNode;
          //Теперь нужно найти следущую вершину
          for (j=intLastNode-1;j>=0;j--)
          {
            if (RNodeList[j]!=null)
            {
              intNextNode=j;
              break;
            }
          }
          if (intNextNode==intLastNode)
          {
           // throw new MPParseException("Next node is not found");
           ms1.SetAttributeValue("FullyCollapsed","yes");
            ms1.DeleteAttribute("Data0");
           ms1.DeleteAttribute("Nod1");
           //Это значит, что полилиния убита полностью
           //это был изолированный отрезок (две вершины и обе тупиковые)
          }
          else
          {
            len=Math.pow(Math.pow(coords[intLastNode][0]-coords[intNextNode][0],2) + Math.pow(coords[intLastNode][1]-coords[intNextNode][1],2),0.5);
            if (len<2*delta)
            {
              removeRoutingNode(ms1,intLastNode,intNextNode);
            }
          }
        }

      }
    }

    //Удалим убитые ребра.

    for (i=mpData.sections.size()-1;i>=0 ;i-- )
    {

      ms1=mpData.sections.get(i);
      if(ms1.SectionType.equals("[POLYLINE]"))
      {
        if (ms1.GetAttributeValue("FullyCollapsed").equals("yes"))
        {
          mpData.sections.remove(i);
        }

      }
    }

  }
  //Максимальный номер рутинговой ноды в данной секции
  private int getMaxRnodeID(MpSection ms1)
  {
    int i;
    i=1;
    int MaxNodeNum=0;
    while(true)
    {
      String strRnode=ms1.GetAttributeValue("Nod"+i);
      if (!strRnode.isEmpty())
      {
        String[] rnode=strRnode.split(",");
        int currentNodeNum;
        currentNodeNum=Integer.parseInt(rnode[1]);
        if (MaxNodeNum<currentNodeNum)
        {MaxNodeNum=currentNodeNum;}

      }
      else
      {break;}
      i=i+1;
    }
    return MaxNodeNum;
  };

  //Добавляем секцию в тест тупиков
  private void AddRoadToDeadEndTest(MpSection ms1, clsDeadEndTest oDeadEndTest, int OsmRoutingLevel)
  {
    String[] NodeList;
    int[]    NodeList2;

    int NN;

    String strNodeAttr;

    NodeList=new String[100] ;
    NodeList2=new int[100];

    NN=0;
    while (true)
    {
      strNodeAttr = ms1.GetAttributeValue("Nod" + (NN+1));
      if (strNodeAttr.equals("") ) break;

      NodeList[NN] =  strNodeAttr.split(",")[1];
      NodeList2[NN] = Integer.parseInt(strNodeAttr.split(",")[2]) ;

      NN++;
    }
    oDeadEndTest.AddRoad(ms1.mpType(),1,NN, NodeList, NodeList2, 0, 0, 0, 0);

  }



  //Вставка дополнительной рутинговой вершины.
  //Нужно знать номер сегмента полилинии, координату, и номер-ид рутинговой вершины.
  void insertRoutingNode(MpSection ms1,int intSegmentNo, double lat, double lon, int rnnum) throws MPParseException
  {

     double [][] coords= ms1.GetCoordArray(false);
    //Вставим дополнительную вершину в список вершин полилинии ("Data0")
     String strCoords="";
     for (int i=0;i<coords.length;i++ )
     {
       if (!strCoords.isEmpty())
       {strCoords=strCoords+",";}
       strCoords=strCoords+"("+latLonFormat.format(coords[i][0])+","+latLonFormat.format(coords[i][1])+")";
       if ((i+1)==intSegmentNo)
       {
         strCoords=strCoords+",("+latLonFormat.format(lat) +","+latLonFormat.format(lon)+")";
       }
     }
    ms1.SetAttributeValue("Data0",strCoords);

    //Еще нужно подвинуть рутинговые ноды, чтобы они ссылались на правильные номера вершин.
    for(int i=coords.length;i>0;i-- )
    {
     String strRnode;
     strRnode=ms1.GetAttributeValue("Nod"+i);
     if (!strRnode.isEmpty())
     {
       String[] rnode=strRnode.split(",");
       int currentNodeNum;
       currentNodeNum=Integer.parseInt(rnode[0]);
       if(currentNodeNum<intSegmentNo)
       {
         //Добавим нашу новую вершину в список рутиновых нод
         //После первой вершины которую сдвигать не надо, это самое подходящее место
         ms1.SetAttributeValue("Nod"+(i+1), intSegmentNo+","+rnnum+",0");
         break;
       }
       ms1.DeleteAttribute("Nod"+(i+1));
       ms1.SetAttributeValue("Nod"+(i+1),(currentNodeNum+1)+","+rnode[1]+","+rnode[2]);

     }
    }
  }

  private String[] getRNodesArray(MpSection ms1,int intLength)
  {
    String[] NodeList;
    int NN;
    String strNodeAttr;
    NodeList=new String[intLength] ;
    NN=0;
    while (true)
    {
      strNodeAttr = ms1.GetAttributeValue("Nod" + (NN+1));
      if (strNodeAttr.equals("") ) break;
      NodeList[Integer.parseInt(strNodeAttr.split(",")[0])] =  strNodeAttr.split(",")[1];
      NN++;
    }
    return NodeList;
  }
  //Удаление вершины (первой или последней)
  private void removeRoutingNode(MpSection ms1,int intFirstNo,int intNextNo) throws MPParseException
  {
    int k=0;
    //Координаты
    double [][] coords= ms1.GetCoordArray(false);
    //Рутинговые вершины
    String[] NodeList=getRNodesArray(ms1,coords.length);
    //Запишем обратно, пропуская лишнюю
    //if (intFirstNo==0)

    //удаляем лишнюю вершину из списка вершин полилинии ("Data0")
    String strCoords="";
    for (int i=0;i<coords.length;i++ )
    {
      if ( ((i>=intNextNo)&&(intFirstNo<intNextNo ))||((i<=intNextNo)&&(intFirstNo>intNextNo))  )
      {
        if (!strCoords.isEmpty())
          {strCoords=strCoords+",";}
        strCoords=strCoords+"("+latLonFormat.format(coords[i][0])+","+latLonFormat.format(coords[i][1])+")";
      }
    }
    ms1.SetAttributeValue("Data0",strCoords);


    for(int i=0;i<coords.length;i++ )
    {
      if ((((i>=intNextNo)&&(intFirstNo<intNextNo ))||((i<=intNextNo)&&(intFirstNo>intNextNo)))&&(NodeList[i]!=null))
      {
        int intNewIndex;
        if(intFirstNo<intNextNo)
        {
          intNewIndex=i-(intNextNo-intFirstNo);
        }
        else
        {
          intNewIndex=i;
        }
        ms1.SetAttributeValue("Nod"+(k+1),(intNewIndex)+","+NodeList[i] +",0");
        k++;
      }
    }
    ms1.DeleteAttribute("Nod"+(k+1));

  }
  //Проверка - пересекаются ли полилинии
  Intersection doesPolyLinesCross(MpSection ms1,MpSection ms2)  throws MPParseException
  {
    //Для начала сравним bbox'ы
    double[] bbox1= ms1.CalculateBBOX();
    double[] bbox2= ms2.CalculateBBOX();

    if (bbox1[0]>=bbox2[2]) return null;
    if (bbox1[1]>=bbox2[3]) return null;

    if (bbox1[2]<=bbox1[0]) return null;
    if (bbox1[3]<=bbox1[1]) return null;
    //System.out.println(" bboxes cross");

    double[][] coords1=ms1.GetCoordArray(false);
    double[][] coords2=ms2.GetCoordArray(false);

    //Todo: Проверить, есть ли общая рутинговая вершина.

    //Цикл по отрезкам полилиний
    for (int i=0;i<coords1.length-1;i++)
    {
      for (int j=0;j<coords2.length-1;j++)
      {
        Intersection intersection_info=new Intersection(coords1[i][0],coords1[i][1],coords1[i+1][0],coords1[i+1][1],
                                                        coords2[j][0],coords2[j][1],coords2[j+1][0],coords2[j+1][1]     );
        if (intersection_info.blnTheyIntersect)
        {
          intersection_info.intSegNo1=i+1;
          intersection_info.intSegNo2=j+1;
          return intersection_info;
        }
      }
    }

    return null;
  }


}
class Intersection{
  boolean blnTheyIntersect;
  double intersection_x;
  double intersection_y;
  int intSegNo1;
  int intSegNo2;

  Intersection(double start1_x,  double start1_y,
               double end1_x,    double end1_y,
               double start2_x,  double start2_y,
               double end2_x,    double end2_y)
  {

    //Возможна ситуация, когда концы отрезков совпадают (отрезки образуют угол)
    //В этом случае будем считать что пересечения нет.
    if ((start1_x==start2_x) && (start1_y==start2_y) )
    {
      blnTheyIntersect=false;
      return;
    }
    if ((end1_x==end2_x) && (end1_y==end2_y) )
    {
      blnTheyIntersect=false;
      return;
    }
    if ((start1_x==end2_x) && (start1_y==end2_y) )
    {
      blnTheyIntersect=false;
      return;
    }
    if ((end1_x==start2_x) && (end1_y==start2_y) )
    {
      blnTheyIntersect=false;
      return;
    }

    //Найдем пересечение.
    double  dir1_x = end1_x - start1_x;
    double  dir1_y = end1_y - start1_y;

    double  dir2_x = end2_x - start2_x;
    double  dir2_y = end2_y - start2_y;

    //считаем уравнения прямых проходящих через отрезки
    double a1 = -dir1_y;
    double b1 = +dir1_x;
    double d1 = -(a1*start1_x + b1*start1_y);

    double a2 = -dir2_y;
    double b2 = +dir2_x;
    double d2 = -(a2*start2_x + b2*start2_y);

    //подставляем концы отрезков, для выяснения в каких полуплоскотях они
    double seg1_line2_start = a2*start1_x + b2*start1_y + d2;
    double seg1_line2_end =   a2*end1_x +   b2*end1_y + d2;

    double seg2_line1_start = a1*start2_x + b1*start2_y + d1;
    double seg2_line1_end = a1*end2_x + b1*end2_y + d1;

    //если концы одного отрезка имеют один знак, значит он в одной полуплоскости и пересечения нет.
    if (seg1_line2_start * seg1_line2_end >= 0 || seg2_line1_start * seg2_line1_end >= 0)
    {
      blnTheyIntersect=false;
      return;
    }

    double u = seg1_line2_start / (seg1_line2_start - seg1_line2_end);
    intersection_x =  start1_x + u*dir1_x;
    intersection_y =  start1_y + u*dir1_y;

    blnTheyIntersect=true;
    return;
  }
}