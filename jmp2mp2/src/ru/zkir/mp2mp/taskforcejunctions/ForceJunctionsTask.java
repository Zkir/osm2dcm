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
      if (i%500==0)
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
      if (i%500==0)
      {
        System.out.println(" i "+i);
      }
      ms1=mpData.sections.get(i);
      if(ms1.SectionType.equals("[POLYLINE]") && !ms1.mpType().toLowerCase().equals("0x1b"))
      {
      //нужно найти первый отрезок и последний отрезок полилинии
        double [][] coords= ms1.GetCoordArray(false);

        boolean blnCoordsUpdated;
        double len;
        blnCoordsUpdated=false;

        if (isNodeDeadEnd(ms1,0,oDeadEndTest))
        {
          len=Math.pow(Math.pow(coords[0][0]-coords[1][0],2) + Math.pow(coords[0][1]-coords[1][1],2),0.5);
          coords[0][0]=coords[0][0]+delta*(coords[0][0]-coords[1][0])/len;
          coords[0][1]=coords[0][1]+delta*(coords[0][1]-coords[1][1])/len;
          blnCoordsUpdated=true;
        }

        if (isNodeDeadEnd(ms1,1,oDeadEndTest))
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
      if (i%500==0)
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
            //TODO:Restore
            insertRoutingNode(ms1,intersection_info.intSegNo1,intersection_info.intersection_x,intersection_info.intersection_y,MaxRNodeID);
            insertRoutingNode(ms2,intersection_info.intSegNo2,intersection_info.intersection_x,intersection_info.intersection_y,MaxRNodeID);
          }
        }
        //Дополнительная операция
        //Удалим атрибут односторонней дороги
        //TODO:Restore
        ms1.DeleteAttribute("DirIndicator");
        String rp=ms1.GetAttributeValue("RouteParam");
        rp=rp.substring(0,4)+"0,"+rp.substring(6);
        ms1.SetAttributeValue("RouteParam",rp);
      }
    }

    //Теперь укоротим тупики обратно.
    //Рассуждать будем так:
    //Если тупиковый сегмент (в начале или конце полилинии) длиннее delta, значит с ним ничего не случилось,
    //и его можно безболезненно перенести обратно
    //Если короче delta - это значит что на удлинненом отрезке было найдено пересечение и последний сегмент нужно просто удалить.

    //Прогоним тест тупиков еще раз - могли появиться новые рутинговые ноды.
    System.out.println("Finding dead ends");
    oDeadEndTest=new clsDeadEndTest();
    for (i=0;i<mpData.sections.size() ;i++ )
    {
      if (i%500==0)
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


    System.out.println(" Shortening roads ");
    //"Укоротим" дороги, оканчивающиеся тупиками
    for (i=0;i<mpData.sections.size() ;i++ )
    {
      if (i%500==0)
      {
        System.out.println(" i "+i);
      }
      ms1=mpData.sections.get(i);
      if(ms1.SectionType.equals("[POLYLINE]") && !ms1.mpType().toLowerCase().equals("0x1b"))
      {
        //нужно найти первый отрезок и последний отрезок полилинии
        double [][] coords= ms1.GetCoordArray(false);

        boolean blnCoordsUpdated;
        boolean blnRemoveFirstNode;
        boolean blnRemoveLastNode;
        double len;
        blnCoordsUpdated=false;
        blnRemoveFirstNode=false;
        blnRemoveLastNode=false;
        double epsilon=0.0001;

        if (isNodeDeadEnd(ms1,0,oDeadEndTest))
        {
          len=Math.pow(Math.pow(coords[0][0]-coords[1][0],2) + Math.pow(coords[0][1]-coords[1][1],2),0.5);
          if (len-delta>epsilon)
          {
            coords[0][0]=coords[0][0]-delta*(coords[0][0]-coords[1][0])/len;
            coords[0][1]=coords[0][1]-delta*(coords[0][1]-coords[1][1])/len;
            blnCoordsUpdated=true;
          }
          else
          {
            blnRemoveFirstNode=true;
          }
        }

        if (isNodeDeadEnd(ms1,1,oDeadEndTest))
        {
          int nlastnode=coords.length-1;
          len=Math.pow(Math.pow(coords[nlastnode][0]-coords[nlastnode-1][0],2) + Math.pow(coords[nlastnode][1]-coords[nlastnode-1][1],2),0.5);
          if (len-delta>epsilon)
          {
            coords[nlastnode][0]=coords[nlastnode][0]-delta*(coords[nlastnode][0]-coords[nlastnode-1][0])/len;
            coords[nlastnode][1]=coords[nlastnode][1]-delta*(coords[nlastnode][1]-coords[nlastnode-1][1])/len;
            blnCoordsUpdated=true;
          }
          else
          {
            blnRemoveLastNode=true;
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
        if (blnRemoveFirstNode)
        {
          removeRoutingNode(ms1,0);
        }
        if (blnRemoveLastNode)
        {
          removeRoutingNode(ms1,coords.length-1);
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

  //Проверка, является ли первая (node=0) или последняя(node=1) вершина тупиковой,
  //на основе теста тупиков
  private boolean isNodeDeadEnd(MpSection ms1,int node, clsDeadEndTest oDeadEndTest)
  {
    String[] NodeList;
    int NN;
    String strNodeAttr;
    NodeList=new String[100] ;
    //Первое что нужно сделать - найти НОМЕР первой и последней вершины в списке рутинговых вершин данной секции
    NN=0;
    while (true)
    {
      strNodeAttr = ms1.GetAttributeValue("Nod" + (NN+1));
      if (strNodeAttr.equals("") ) break;
      NodeList[NN] =  strNodeAttr.split(",")[1];
      NN++;
    }

    //Второе, что нужно сделать - проверить, есть ли данная вершина в списке тупиков.
    if (node!=0)
    {
      node=NN-1;
    }

    return oDeadEndTest.isDeadEnd(NodeList[node]);
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
  //Удаление вершины (первой или последней)
  private void removeRoutingNode(MpSection ms1,int intNodeNo) throws MPParseException
  {

    double [][] coords= ms1.GetCoordArray(false);
    //Удаляем либо первую, либо последнюю
    if(intNodeNo!=0)
    {
      intNodeNo=coords.length-1;
    }
    //удаляем лишнюю вершину из списка вершин полилинии ("Data0")
    String strCoords="";
    for (int i=0;i<coords.length;i++ )
    {
      if (i!=intNodeNo)
      {
        if (!strCoords.isEmpty())
          {strCoords=strCoords+",";}
        strCoords=strCoords+"("+latLonFormat.format(coords[i][0])+","+latLonFormat.format(coords[i][1])+")";
      }
    }
    ms1.SetAttributeValue("Data0",strCoords);

    //Еще нужно подвинуть рутинговые ноды, чтобы они ссылались на правильные номера вершин.
    //Если это первая вершина, то ее нужно скипнуть при записи.

    //Зачитаем рутинговые ноды в массив
    String[] NodeList;
    int NN;
    String strNodeAttr;
    NodeList=new String[coords.length] ;
    NN=0;
    while (true)
    {
      strNodeAttr = ms1.GetAttributeValue("Nod" + (NN+1));
      if (strNodeAttr.equals("") ) break;
      NodeList[Integer.parseInt(strNodeAttr.split(",")[0])] =  strNodeAttr.split(",")[1];
      NN++;
    }
    //Запишем обратно, пропуская лишнюю
    if (intNodeNo==0)
    {
      int k=0;
      for(int i=0;i<coords.length;i++ )
      {
        if ((i!=0)&&(NodeList[i]!=null))
        {
          ms1.SetAttributeValue("Nod"+(k+1),(i-1)+","+NodeList[i] +",0");
          k++;
        }
      }
      ms1.DeleteAttribute("Nod"+(k+1));
    }
    else
    {
      int k=0;
      for(int i=0;i<coords.length;i++ )
      {
        if ((i!=(coords.length-1))&&(NodeList[i]!=null))
        {
          ms1.SetAttributeValue("Nod"+(k+1),i+","+NodeList[i] +",0");
          k++;
        }
      }
      ms1.DeleteAttribute("Nod"+(k+1));
    }


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