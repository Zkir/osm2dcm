package ru.zkir.mp2mp.taskvalidator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 03.11.12
 * Time: 21:56
 * To change this template use File | Settings | File Templates.
 */

/*
  ***************************************************************************
  Тест висячих вершин
  ***************************************************************************

  Идея очень простая.
  Важные дороги (trunk,primary,secondary) не могут просто обрываться,
   а должны переходить в менее важные.
*/

public class clsDeadEndTest {

  ArrayList<NodeInfo> arrDanglingNodesPre;
  ArrayList<NodeInfo> arrDanglingNodesOut;

  HashMap<String,NodeInfo> mapAllNodes;

  class NodeInfo
  {
    String id;
    String lat;
    String lon;
    int count;

  }
  public clsDeadEndTest()
  {
    arrDanglingNodesOut=new ArrayList<NodeInfo>();
    arrDanglingNodesPre=new ArrayList<NodeInfo>();
    mapAllNodes=new HashMap<String,NodeInfo>();

  }


  // Добавление концов дороги в список.
  // NodeList() - список айдишников рутинговых вершин
  //NodeExtrAttrList() - 0 для обычной вершины и 1 для внешней (которая соединяется с чем-то за пределами карты)
  public void AddRoad(String strMpType,int intRoadLevel, int NN, String[] NodeList, int[] NodeExtrAttrList,
                      double lat1, double lon1, double lat2, double lon2)
  {
     int i;
    //Все вершины добавляются в список вершин.
    for(i=0;i<NN; i++)
    { 
      NodeInfo nodeInfo;
      if(mapAllNodes.containsKey(NodeList[i]))
      {
        nodeInfo=mapAllNodes.get(NodeList[i]);
        nodeInfo.count++;  
      }
      else
      { //Надо добавить вершину 
        nodeInfo=new NodeInfo(); 
        nodeInfo.id=NodeList[i];
        nodeInfo.count=1;
        mapAllNodes.put(NodeList[i],nodeInfo);
      }
    }
    
    //Концевые вершины добавляются в список концевых, т.е. потенциально висячих.
    //При этом внешние вершины добавлять НЕ надо. Они не являются висячими, потому что с чем-то соединяются.
    //тупики развязкок тоже не добавляются
    if( (intRoadLevel <= 2) && !((strMpType.equals("0x08")) || (strMpType.equals("0x0B"))) )
    {
      if( NodeExtrAttrList[0] != 1 )
      {
        NodeInfo nodeInfo;
        nodeInfo=new NodeInfo();

        nodeInfo.id= NodeList[0];
        nodeInfo.lat=Double.toString(lat1);
        nodeInfo.lon=Double.toString(lon1);
        //rsDanglingNodes(RS_DANGLING_NODES_LEVEL).Value = intRoadLevel

        arrDanglingNodesPre.add(nodeInfo);

      }

      if( NodeExtrAttrList[NN-1] != 1 )
      {
        NodeInfo nodeInfo;
        nodeInfo=new NodeInfo();

        nodeInfo.id= NodeList[NN-1];

        nodeInfo.lat=Double.toString(lat2);
        nodeInfo.lon=Double.toString(lon2);
        //rsDanglingNodes(RS_DANGLING_NODES_LEVEL).Value = intRoadLevel

        arrDanglingNodesPre.add(nodeInfo);
      }
    }

  }


  //Проверим, какие вершины висячие. Собственно, для каждой вершины с концов рутингового вея, нужно проверить число вхождений
  //в дороги. Если концевая вершина входит только в одну дорогу - она висячая
  public void Validate()
  {
    int i;
    for (i=0;i<arrDanglingNodesPre.size();i++ )
    {
      NodeInfo nodeInfo;
      nodeInfo=arrDanglingNodesPre.get(i);


      if( mapAllNodes.get(nodeInfo.id).count==1 )
      {
        arrDanglingNodesOut.add(nodeInfo);
      }
    }

  }

  public void PrintErrorsToXML(BufferedWriter oReportFile, boolean blnSummary)  throws IOException
  {
     int i;
     // rsDanglingNodes.Filter = RS_DANGLING_NODES_HWLINK & "=" & 0
     oReportFile.write( "<DeadEndsTest>\r\n");
     oReportFile.write( "<Summary>\r\n");
     oReportFile.write( "  <NumberOfDeadEnds>" +Long.toString(arrDanglingNodesOut.size()) + "</NumberOfDeadEnds>\r\n");
     oReportFile.write( "</Summary>\r\n");
     if (!blnSummary)
     {
       oReportFile.write( "  <DeadEndList>\r\n");
       for(i=0;i<arrDanglingNodesOut.size();i++)
       {
          oReportFile.write( "  <DeadEnd>\r\n");
          //oReportFile.write( "    <rn_id>" + arrDanglingNodesOut.get(i).id + "</rn_id>\r\n" );
          oReportFile.write( "    <Coord>\r\n");
          oReportFile.write( "      <Lat>" + arrDanglingNodesOut.get(i).lat + "</Lat>\r\n" );
          oReportFile.write( "      <Lon>" + arrDanglingNodesOut.get(i).lon + "</Lon>\r\n" );
          oReportFile.write( "    </Coord>\r\n");
          oReportFile.write( "  </DeadEnd>\r\n");
        }
        oReportFile.write( "  </DeadEndList>\r\n");
     }
     oReportFile.write( "</DeadEndsTest>\r\n" );
  }

}
