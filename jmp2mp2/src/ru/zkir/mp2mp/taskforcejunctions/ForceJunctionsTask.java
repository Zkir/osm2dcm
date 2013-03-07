package ru.zkir.mp2mp.taskforcejunctions;

import ru.zkir.mp2mp.core.MpData;
import ru.zkir.mp2mp.core.MpSection;
import ru.zkir.mp2mp.core.TaskInfo;



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


  public void execute(MpData mpData, TaskInfo taskInfo)
  {
    int i,j;
    MpSection  ms1,ms2;
    for (i=0;i<mpData.sections.size() ;i++ )
    {
      ms1=mpData.sections.get(i);
      for (j=i+1;i<mpData.sections.size();j++ )
      {
        ms2=mpData.sections.get(j);
        if(doesPolyLinesCross(ms1,ms2))
        {
          insertRoutingNode(ms1,1,0,0,77);
          insertRoutingNode(ms2,1,0,0,77);
        }
      }
    }

  }

  boolean doesPolyLinesCross(MpSection ms1,MpSection ms2)
  {
   return false;
  }

  //Вставка дополнительной рутинговой вершины.
  //Нужно знать номер сегмента полилинии, координату, и номер-ид рутинговой вершины.
  void insertRoutingNode(MpSection ms1,int SegmentNo, double lat,double lon, int rnnum)
  {

  }
  //Другая идея - нафигачить отрезков полилиний в какой-нибудь массив.
}
