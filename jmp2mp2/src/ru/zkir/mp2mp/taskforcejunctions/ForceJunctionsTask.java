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
import java.util.ArrayList;

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


  public void execute(MpData mpData, TaskInfo taskInfo)   throws MPParseException
  {
    int i,j;
    MpSection  ms1,ms2;
    int MaxRNodeID;

    //Найдем максимальную ноду. Она нам понадобиться для вставки новых нод пересечений
    MaxRNodeID=0;
    for (i=0;i<mpData.sections.size() ;i++ )
    {
      ms1=mpData.sections.get(i);
      int RNodeID=getMaxRnodeID(ms1);

      if (RNodeID>MaxRNodeID)
      {MaxRNodeID=RNodeID;}
    }
    System.out.println(" MaxRNodeID "+MaxRNodeID);


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
      if(ms1.SectionType.equals("[POLYLINE]"))
      {
        SaveToListProcedure myProc = new SaveToListProcedure();
        double[] coords=ms1.CalculateBBOX();
        si.intersects(new Rectangle((float)coords[0],(float)coords[1],(float)coords[2],(float)coords[3]), myProc) ;

        ArrayList<Integer> ids = myProc.getIds();

        for (Integer id : ids)
        {
          //System.out.println(id + " was contained");
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


  //Вставка дополнительной рутинговой вершины.
  //Нужно знать номер сегмента полилинии, координату, и номер-ид рутинговой вершины.
  void insertRoutingNode(MpSection ms1,int intSegmentNo, double lat, double lon, int rnnum) throws MPParseException
  {
     double [][] coords= ms1.GetCoordArray(false);
     String strCoords="";
     for (int i=0;i<coords.length;i++ )
     {
       if (!strCoords.isEmpty())
       {strCoords=strCoords+",";}
       strCoords=strCoords+"("+coords[i][0]+","+coords[i][1]+")";
       if ((i+1)==intSegmentNo)
       {
         strCoords=strCoords+",("+lat+","+lon+")";
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
  //Другая идея - нафигачить отрезков полилиний в какой-нибудь массив.
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