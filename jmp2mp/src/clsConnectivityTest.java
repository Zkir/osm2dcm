import com.sun.org.apache.xpath.internal.NodeSet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 16.10.12
 * Time: 22:26
 * To change this template use File | Settings | File Templates.
 */


public class clsConnectivityTest {

  private class clsSubGraph{
    //Множество вершин входящих в данный подграф
    public HashSet<String> Nodes;

    //Свойства подграфа
    double lat1;
    double lon1;
    double lat2;
    double lon2;
    int RoadCount;
    boolean blnHasExternalNodes;

    public clsSubGraph()
    {
      Nodes= new HashSet<String>();
    }
  }

  ArrayList<clsSubGraph> lstAllSubGraphs;
  int NTotalRoads;
  int NRoutiningEdges;

  HashMap<String,clsSubGraph> SubGraphsByNodes;

  public clsConnectivityTest()
  {
    lstAllSubGraphs = new ArrayList<clsSubGraph>();
    SubGraphsByNodes = new  HashMap<String,clsSubGraph>();
  }


 /*=====================================================================================================================
  'Самая важная функция.
'Добавляется дорога.
'Передается ID дороги и список номеров ее вершин.
'И еще хорошо бы BBOX
'
'алгоритм прост как колобок.
'1. Начать с пустого списка подграфов.
'2. Взять следующую дорогу из списка
'3. Проверить, есть ли подграф (подграфы), в который уже входят  рутинговые ноды данной дороги.
'* Если нет, добавить в список новый подграф, дорогу и ее ноды пометить как относящиеся
'  к данному подграфу.
'* Если есть ровно один такой подграф, дорогу и ее ноды пометить как относящиеся к данному подграфу.
'* Если таких подграфов несколько (данная дорога входит в несколько из уже найденных подграфов,
'  являсь, таким образом, перемычкой ними)
'- оставить только первый подграф, остальные исключить из списка, относящиеся к ним ноды
'  и дороги пометить как относящиеся к первому подграфу
'4. Продолжать с пункта 2, пока есть необработанные ребра.
'5. Отобразить список найденных подграфов, в порядке убывания(или возрастания) числа ребер.
'В идеале должен быть один подграф. Практически может быть несколько (какие-нибудь карты островов).
' В любом случае подграфы с несколькими (до десятка) ребрами суть ошибки.
'
' NodeList() - список айдишников рутинговых вершин
' NodeExtrAttrList() - 0 для обычной вершины и 1 для внешней (которая соединяется с чем-то за пределами карты)
' Подграф, содержащий внешние вершины, не является вполне изолированным.
========================================================================================================================
*/

public void AddRoad(int Nnodes, String[] NodeList, int[] NodeExtrAttrList,
                    double lat1, double lon1, double lat2, double lon2 )
{
  int i;
  int  N;//  ' число найденных подграфов
  ArrayList<clsSubGraph>  WorkingSetOfSubGraphs; //'  найденные подграфы (1..N)
  boolean blnHasExternalNodes;

  //Проверим, есть ли подграф (подграфы), в который уже входят  рутинговые ноды данной дороги.
  // Т.е попросту те, которые передаются в функцию.


  blnHasExternalNodes = false;
  WorkingSetOfSubGraphs = new ArrayList<clsSubGraph>(); // Чистенький списочек найденных подграфов.


  //Перебираем вершины данной дороги, и смотрим, входят ли они в какие либо подграфы
  for( i=0;i<Nnodes;i++)
  {

    if( SubGraphsByNodes.containsKey(NodeList[i]) )
    { //Подграф найден
      if (!WorkingSetOfSubGraphs.contains(SubGraphsByNodes.get(NodeList[i])))
        WorkingSetOfSubGraphs.add(SubGraphsByNodes.get(NodeList[i]));
    };


    if (NodeExtrAttrList[i] == 1)
    {
      blnHasExternalNodes = true;
    }

  };

  //Если ни в какой не входят, надо создать новый.
  if (WorkingSetOfSubGraphs.size() == 0)
  {
    clsSubGraph theNewSubGraph;
    // Добавим новый подграф
    theNewSubGraph =new clsSubGraph();

    //intSubgraphCount = intSubgraphCount + 1

    theNewSubGraph.RoadCount=0;//  rsSubGraph(RS_SUBGRAPH_ROADCOUNT).Value = 0
    theNewSubGraph.lat1=lat1;//rsSubGraph(RS_SUBGRAPH_LAT1).Value = lat1;
    theNewSubGraph.lon1=lon1; //rsSubGraph(RS_SUBGRAPH_LON1).Value = lon1;
    theNewSubGraph.lat2=lat2;//rsSubGraph(RS_SUBGRAPH_LAT2).Value = lat2;
    theNewSubGraph.lon2=lon2;//rsSubGraph(RS_SUBGRAPH_LON2).Value = lon2;
    theNewSubGraph.blnHasExternalNodes=blnHasExternalNodes; //rsSubGraph(RS_SUBGRAPH_HAS_EXTNODES).Value = blnHasExternalNodes

    N = 1;
    WorkingSetOfSubGraphs.add(theNewSubGraph);
    lstAllSubGraphs.add(theNewSubGraph);
  }
  //Объединим найденные подграфы
  for(i = 1;i<WorkingSetOfSubGraphs.size() ;i++)
  {
    if(WorkingSetOfSubGraphs.get(0) != WorkingSetOfSubGraphs.get(i))
    {
      MergeSubGraphs( WorkingSetOfSubGraphs.get(0) , WorkingSetOfSubGraphs.get(i));

      //TODO:Второй подграф нужно удалить!!!!
      //Откуда только?
      lstAllSubGraphs.remove(WorkingSetOfSubGraphs.get(i)) ;
      //SubGraphs.remove(SubGraphs.get(i));
    }
  }

  //Добавим вершины нашей дороги в первый подграф
  for( i=0;i<Nnodes;i++)
  {
    AddNodeToSubGraph (WorkingSetOfSubGraphs.get(0), NodeList[i]);
  }
  //Найденные дороги объединились,
  //и мы имеем ОДИН объединенный подграф
  clsSubGraph CombinedSubGraph;
  CombinedSubGraph= WorkingSetOfSubGraphs.get(0);

  //Добавим дорогу в наш первый подграф.
  // * число ребер увеличивется на единицу
  // * Bbox расширяется


  CombinedSubGraph.RoadCount++; //  rsSubGraph(RS_SUBGRAPH_ROADCOUNT).Value = rsSubGraph(RS_SUBGRAPH_ROADCOUNT).Value + 1

  if (lat1<CombinedSubGraph.lat1) CombinedSubGraph.lat1=lat1;
  if (lon1<CombinedSubGraph.lon1) CombinedSubGraph.lon1=lon1;
  if (lat2>CombinedSubGraph.lat2) CombinedSubGraph.lat2=lat2;
  if (lon2>CombinedSubGraph.lon2) CombinedSubGraph.lon2=lon2;

  //Если в данной дороге есть внешняя нода, значит во всем подграфе есть внешняя нода.
  if (blnHasExternalNodes)
  {
    CombinedSubGraph.blnHasExternalNodes=true;
  }

  //Общее число дорог так же увеличивается.
  NTotalRoads = NTotalRoads + 1;

  //число рутинговых ребер в данной дороге равно количеству рутинговых вершин минус один.
  NRoutiningEdges = NRoutiningEdges + (Nnodes-1);

}

  //Второй подграф присоединяется к первому.
  private void MergeSubGraphs(clsSubGraph SubGraph1, clsSubGraph SubGraph2)
  {
    int NRoads;
    double lat1,lon1,lat2,lon2;
    boolean blnHasExternalNodes;

    //Число дорог и bbox "прибавляются" к основному подграфу
    NRoads = SubGraph2.RoadCount; // rsSubGraph(RS_SUBGRAPH_ROADCOUNT).Value
    lat1 = SubGraph2.lat1;  //rsSubGraph(RS_SUBGRAPH_LAT1).Value
    lon1 = SubGraph2.lon1;  //rsSubGraph(RS_SUBGRAPH_LON1).Value
    lat2 = SubGraph2.lat2;  // rsSubGraph(RS_SUBGRAPH_LAT2).Value
    lon2 = SubGraph2.lon2;  // rsSubGraph(RS_SUBGRAPH_LON2).Value
    blnHasExternalNodes = SubGraph2.blnHasExternalNodes; // rsSubGraph(RS_SUBGRAPH_HAS_EXTNODES).Value

    //Запись будет(?) удалена. число дорог зануляется на всякий случай
    SubGraph2.RoadCount=0;

    SubGraph1.RoadCount=SubGraph1.RoadCount+ NRoads;

    if (lat1<SubGraph1.lat1) SubGraph1.lat1=lat1;
    if (lon1<SubGraph1.lon1) SubGraph1.lon1=lon1;
    if (lat2>SubGraph1.lat2) SubGraph1.lat2=lat2;
    if (lon2>SubGraph1.lon2) SubGraph1.lon2=lon2;

    if (blnHasExternalNodes)
    {
      SubGraph1.blnHasExternalNodes=true;
    }

    //Всем вершинам второго подграфа приписывается первый подграф

    Iterator<String> it =  SubGraph2.Nodes.iterator();

    while(it.hasNext())
    {
      AddNodeToSubGraph(SubGraph1,it.next());
    }

  }


  //Добавляем ноду в граф
  private void AddNodeToSubGraph(clsSubGraph SubGraph, String strNodeID)
  {
    //Добавим ноду в список верши данного графа.
    SubGraph.Nodes.add(strNodeID);
    //Добавим в "индекс"  вершина-граф
    SubGraphsByNodes.put(strNodeID,SubGraph);
  }

  public void PrintRegistryToXML(BufferedWriter oReportFile) throws IOException
  {
    int i;
    ArrayList<clsSubGraph> lstResultSet;
    lstResultSet=new ArrayList<clsSubGraph>();

    // выводим подграфы без внешних нод и известные исключения
    for(i=0;i<lstAllSubGraphs.size();i++ )
    {
      if (!lstAllSubGraphs.get(i).blnHasExternalNodes)
      {
        lstResultSet.add(lstAllSubGraphs.get(i));
      }
    }


    // В порядке убывания числа вершин
    //rsSubGraph.Sort = RS_SUBGRAPH_ROADCOUNT & " desc"

    oReportFile.write("<Summary>\r\n");
    oReportFile.write( "  <NumberOfSubgraphs>" + lstResultSet.size() + "</NumberOfSubgraphs>\r\n");
    oReportFile.write( "  <NumberOfRoads>" + Integer.toString( NTotalRoads) + "</NumberOfRoads> \r\n");
    oReportFile.write( "  <NumberOfRoutingEdges>" + Integer.toString(NRoutiningEdges) + "</NumberOfRoutingEdges>\r\n");
    oReportFile.write( "</Summary>\r\n");

    //Найденные подграфы

    oReportFile.write(  "<SubgraphList>\r\n");


    for(i=0;i<lstResultSet.size();i++ )
    {
      oReportFile.write( "  <Subgraph>\r\n");
      oReportFile.write( "    <NumberOfRoads>" + Integer.toString (lstResultSet.get(i).RoadCount) + "</NumberOfRoads>\r\n");
      oReportFile.write( "    <Bbox>\r\n");
      oReportFile.write( "      <Lat1>" + Double.toString(lstResultSet.get(i).lat1)  + "</Lat1>\r\n");
      oReportFile.write( "      <Lon1>" + Double.toString(lstResultSet.get(i).lon1) + "</Lon1>\r\n");
      oReportFile.write( "      <Lat2>" + Double.toString(lstResultSet.get(i).lat2) + "</Lat2>\r\n");
      oReportFile.write( "      <Lon2>" + Double.toString(lstResultSet.get(i).lon2) + "</Lon2>\r\n");
      oReportFile.write( "    </Bbox>\r\n");

      oReportFile.write( "  </Subgraph>\r\n");
    }
   oReportFile.write( "</SubgraphList>\r\n");


  }
}
