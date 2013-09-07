/*

 mp_extsimp
 Generalization of complex junctions and two ways roads
 from OpenStreetMap data

 Copyright © 2012-2013 OverQuantum

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 Author contacts:
 http://overquantum.livejournal.com
 https://github.com/OverQuantum

 Project homepage:
 https://github.com/OverQuantum/mp_extsimp


 OpenStreetMap data licensed under the Open Data Commons Open Database License (ODbL).
 OpenStreetMap wiki licensed under the Creative Commons Attribution-ShareAlike 2.0 license (CC-BY-SA).
 Please refer to http://www.openstreetmap.org/copyright for details

 osm2mp licensed under GPL v2, please refer to http://code.google.com/p/osm2mp/

 mp file format (polish format) description from http://www.cgpsmapper.com/manual.htm




history
2012.10.08 - "challenge accepted", project started
2012.10.10 - added combining of edges
2012.10.11 - added checking of type and oneway on combining of edges
2012.10.12 - added collapsing of junctions
2012.10.15 - added comments to code, added handling of speedclass
2012.10.16 - added distance in metres by WGS 84
2012.10.16 - added joining directions (only duplicate edges and close edges forming V-form)
2012.10.17 - chg CollapseJunctions now iterative
2012.10.17 - added inserting near nodes to edges on joining direction (no moving of node)
2012.10.18 - adding JoinDirections2, closest edge founds, GoByTwoWays started
2012.10.18 - adding JoinDirections2, joining works, but some mess created...
2012.10.21 - finished JoinDirections2, handling of circles and deleted void edges
2012.10.22 - adding CollapseJunctions2, done main part, remain marking edges for cases of crossing if border-nodes >= 2 in the end
2012.10.22 - adding CollapseJunctions2, marking edges for cases of crossing if border-nodes >= 2 in the end, marking long oneways before
2012.10.23 - added limiting distance of ShrinkBorderNodes
2012.10.23 - added check for forward/backward coverage at ends of chains in JoinDirections2
2012.10.24 - added CheckShortLoop, does not help
2012.10.29 - added CheckShortLoop, forward/backward coverage in JoinDirections2 for cycles
2012.10.29 - added DouglasPeucker_total_split
2012.10.30 - added check lens in CosAngleBetweenEdges, skipping of RouteParamExt, LoopLimit in CJ2
2012.10.31 - fix forw/back check in JD2 (split to two cycles)
2012.11.01 - added JoinAcute (from JD), ProjectNode, CompareRoadtype. Looks like RC1 of algo
2012.11.01 - added Save_MP_2 (w/o rev-dir)
2012.11.06 - added JD3 - with cluster search
2012.11.07 - fix loop in D-P, optimized aiming and del in CJ2, added/modified status writing in form1.caption
2012.11.09 - fix saving2 on oneway=2
2012.11.12 - added keep of main road label
2012.11.13 - added TWback/TWforw and checking them in JD3 (unfinished)
2012.11.14 - added correct naming and speedclass in JD3
 RC3
2012.11.14 - hardcoded limits moved to func/sub parameters
2012.11.14 - unused functions commented
2012.11.15 - root code moved to module, removed comdlg32.bas
2012.11.19 - added keeping header of source file, removed writing "; roadtype="
2012.11.15-20 - adding explaining comments, small fixes
2012.11.20 - added license and references
2013.01.08 - added CollapseShortEdges (fix "too close nodes")
2013.01.28 - fixed deadlock in SaveChain in case of isolated road cycle

2013.02.03 - Completed porting to java (freeExec - https://github.com/freeExec/mp_extsimp)
2012.02.28 - added MaxLinkLen to Load_MP() include (2013.03.02, 2013.03.03)
 
TODO:
*? dump problems of OSM data (1: too long links (ready), 2: ?)
'? 180/-180 safety */
package ru.zkir.mp2mp.taskextsimp;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;


/**
 *
 * @author freeExec
 */
public class Mp_extsimp {

    // Fields

    public static String MPheader = "";
    
    //All nodes
    //private static node[] Nodes;
    private static ArrayList<Node> Nodes;
    //private static int NodesAlloc = 0;
    //private static int NodesNum = 0;
    
    //All road edges
    //private static edge[] Edges;
    private static ArrayList<Edge> Edges;
    //private static int EdgesAlloc = 0;
    //private static int EdgesNum = 0;
    
    //Array for building chain of indexes
    private static ArrayList<Node> Chain;
    /* private static int[] Chain;
    private static int ChainAlloc = 0;
    private static int ChainNum = 0;*/
    
    //Aim edges
    private static ArrayList<AimEdge> AimEdges;
    
    //Label statistics, for estimate labels of joined roads
    private static ArrayList<LabelStat> LabelStats;
    //private static int LabelStatsNum = 0;
    //private static int LabelStatsAlloc = 0;

    //Indexes of forward and backward ways during two ways joining
    private static ArrayList<Edge> TWforw;
    private static ArrayList<Edge> TWback;
    /*private static int[] TWforw;
    private static int[] TWback;
    private static int TWalloc = 0;
    private static int TWforwNum = 0;
    private static int TWbackNum = 0;*/

    //max found NodeID
    public static int NodeIDMax = 0;

    //edge, on which GoByChain() function have just passed from node to node
    public static Edge GoByChain_lastedge;

    //case of calc distance during last call of DistanceToSegment()
    public static int DistanceToSegment_last_case = 0;

    //histogramm of speed classes
    public static int[] SpeedHistogram;
    
//Cluster index
/*    //'min lat-border of clusters
    public static double ClustersLat0 = 0;
    //'min lon-border of clusters
    public static double ClustersLon0 = 0;
    //'index of first node of cluster (X*Y)
    public static int[] ClustersFirst;
    //'chain of nodes (NodesNum)
    public static int[] ClustersChain;
    //'num of cluster by lat = X
    public static int ClustersLatNum = 0;
    //'num of cluster by lon = Y
    public static int ClustersLonNum = 0;
    //'num of indexed nodes (for continuing BuildNodeClusterIndex)
    public static int ClustersIndexedNodes = 0;
    //'index of last node of cluster - for building index (X*Y)
    public static int[] ClustersLast;
    //last bbox
//    public static Bbox ClustersFindLastBbox;
    //'last index of cluster
//    public static int ClustersFindLastCluster = 0;
    //'
//    public static int ClustersFindLastNode = 0;
*/
    public static DecimalFormat numFormat;

    //speed of chain after last call of EstimateChain()
    public static int EstimateChain_speed = 0;
    //label of chain after last call of EstimateChain()
    public static String EstimateChain_label = "";
    
    /*public static void main(String[] args) {
        //Locale.setDefault(Locale.ENGLISH);
        System.out.println("Generalization of complex junctions and two ways roads from OpenStreetMap data\n" +
            "Copyright 2012-2013 OverQuantum\n" + 
            "Version: 0.2    Java porting freeExec\n");
        if (args.length != 1) {
            System.out.print("Usages: mp_extsimp filename.mp\n");
        } else {
            optimizeRouting(args[0]);
        }
    } */
    
    public void optimizeRouting(String inputFile, int intEdgeLengthLimit) {
        String outFile = "";
        //String outFile2 = "";
        long time1 = 0;

        //System.out.println(String.format("Nod1=0,%d,0\n", 8742));
        //System.out.println(String.format("%f\n", 8742.547820d));
        //DecimalFormat nf = new DecimalFormat("0.#######");
        //System.out.println(nf.format(37.6170351227685d));
        //nothing to do
        if (inputFile.isEmpty()) { return; }

        //output file
        outFile = inputFile + "_opt.mp";
        //outFile2 = inputFile + "_p.mp";  //output2 - for intermediate results
        
        //start measure time
        time1 = System.currentTimeMillis();
        
        //Init module (all arrays)
        initArrays();

        //Load data from file
        load_MP(inputFile, 1200);

        //Join nodes by NodeID
        joinNodesByID();

        //Join two way roads into bidirectional ways
        joinDirections3(70, -0.996, -0.95, 100, 2);
        //70 metres between directions (Ex: Universitetskii pr, Moscow - 68m)
        //-0.996 -> (175, 180) degrees for start contradirectional check
        //-0.95 -> (161.8, 180) degrees for further contradirectional checks
        //100 metres min two way road
        //2 metres for joining nodes into one

        filterVoidEdges();

        //Optimize all roads by (Ramer-)Douglas Peucker algorithm with limiting edge len
        douglasPeucker_total_split(5, 100);
        //Epsilon = 5 metres
        //Max edge - 100 metres

        collapseJunctions2(1000, 1200, 0.13);
        //Slide allowed up to 1000 metres
        //Max junction loop is 1200 metres
        //0.13 -> ~ 7.46 degress

        filterVoidEdges();

        //Optimize all roads by (Ramer)DouglasPeucker algorithm
        douglasPeucker_total(5);
        //Epsilon = 5 metres

        //Join edges with very acute angle into one
        joinAcute(100, 3);
        //100 metres for joining nodes
        //AcuteKoeff = 3 => 18.4 degrees

        //Optimize all roads by (Ramer)DouglasPeucker algorithm
        douglasPeucker_total(5);
        //Epsilon = 5 metres

        //Remove very short edges, they are errors, most probably
        CollapseShortEdges(intEdgeLengthLimit);
        
        //Save result
        save_MP_2(outFile);

        //display timing
        long time2 = System.currentTimeMillis();
        System.out.printf("Done %1$tT s",  time2 - time1);
    }

    //Init module (all arrays)
    private static void initArrays() {
        //NodesAlloc = 1000;
        //Nodes = new node[NodesAlloc];
        Nodes = new ArrayList<>(100000);
        //NodesNum = 0;   -> Nodes.size();

        //EdgesAlloc = 1000;
        //Edges = new edge[EdgesAlloc];
        Edges = new ArrayList<>(100000);
        //EdgesNum = 0;

        Chain = new ArrayList<>(100000);
        /* ChainAlloc = 1000;
        Chain = new int[ChainAlloc];
        ChainNum = 0;*/

        //AimEdges = new ArrayList<>();

        LabelStats = new ArrayList<>(100);

        /*TWalloc = 100;
        TWforw = new int[TWalloc];
        TWback = new int[TWalloc];
        TWbackNum = 0;
        TWforwNum = 0;*/
        TWforw = new ArrayList<>(10000);
        TWback = new ArrayList<>(10000);

        SpeedHistogram = new int[11];

        Clusters.init(Nodes);
    }

    //Load .mp file
    // Remove _link flags from polylines longer than MaxLinkLen
    //(loader is basic and rather stupid, uses relocation on file to read section info without internal buffering)
    public static void load_MP(String filename, double maxLinkLen) {
        //int logOptimization = 0;
        String sLine = "";
        double fLat = 0;
        double fLon = 0;
        //String sWay = "";
        //'0 - none, 1 - header, 2 - polyline, 3 - polygon
        int sectionType = 0;
        //'Phase of reading polyline: 0 - general part, 1 - scan for routeparam, 2 - scan for geometry, 3 - scan for routing (nodeid e.t.c)
        //int iPhase = 0;
        //long iStartLine = 0;
        long iPrevLine = 0;
        //long fileLen = 0;
        int lastPercent = 0;
        //String sPrefix = "";
        int dataLineNum = 0;
        int k, k2, k3;
        long p;
        //int i, j;
        int thisLineNodes = 0;
        int nodeID = 0;
        byte wayClass = 0;
        byte waySpeed = 0;
        byte wayOneway = 0;
        String[] routep;
        byte lastCommentHighway = 0;
        String label = "";
        
        double linkLen = 0;
        
        int originalLenLine = 0;

        //no nodeid yet
        NodeIDMax = -1;

        //Open(filename For Input As #1);
        //fileLen = LOF(1);
        BufferedReader br;
        FileInputStream fis;
        try {
            fis = new FileInputStream(filename);
            br = new BufferedReader(new InputStreamReader(fis, "CP1251"));
            
            //fileLen = fis.getChannel().size();
            
            sectionType = 0;
            wayClass = -1;
            waySpeed = -1;
            //iPhase = 0;
            label = "";
            //iStartLine = 0;
            iPrevLine = 0;
            MPheader = "";
            lastCommentHighway = Highway.HIGHWAY_UNSPECIFIED;
            
            // для однопроходной загрузки
            Node addedNode;// = new Node(-1);
            ArrayList<Node> addedNodes = new ArrayList<>();
            ArrayList<Edge> addedEdges = new ArrayList<>();
            
            // delete after fix
            int autoINCNodesNum = 0;

            while(br.ready()) {
    //*TODO:** label found: lNextLine:;
                //get current position in file            //iPrevLine = br.Seek(1);
                iPrevLine += originalLenLine; //fis.getChannel().position();
                //Line(Input #1, sLine);
                sLine = br.readLine();
                originalLenLine = sLine.length() + 2; // 2 = перевод строки
                sLine = sLine.trim();   // один раз отрезать все лишнее

                //check for section start
                if (sLine.startsWith("[IMG ID]")) {
                    //header section
                    sectionType = 1;
                    //iPhase = 0;
                }
                if (sLine.startsWith("[POLYGON]")) {
                    //polygon
                    sectionType = 3;
                    //*TODO:** goto found: GoTo lStartPoly;
                }
                if (sLine.startsWith("[POLYLINE]")) {
                    //polyline
                    sectionType = 2;
                }
    //*TODO:** label found: lStartPoly:;
                if (sLine.startsWith("[POLY") && (sectionType == 3 || sectionType == 2)) {
                    if ((iPrevLine / 1023) > lastPercent) {
                        //display progress
                        //Form1.Caption = "Load: " + CStr(iPrevLine) + " / " + CStr(fileLen): Form1.Refresh;
                        lastPercent = (int)(iPrevLine / 1023);
// TODO                        System.out.printf("Load: (%3$d%%) %1$d / %2$d\n", iPrevLine, fileLen, lastPercent);
                    }
                    dataLineNum = 0;
                    //if (iPhase == 0) {
                        //first pass of section? start scanning
                        wayClass = -1;
                        waySpeed = -1;
                        //iPhase = 1;
                        //remember current pos (where to go after ending pass)
                        //iStartLine = iPrevLine - originalLenLine;
                        //br.mark(40*1024);   // резерв буфера на 40 КБ
                        //System.out.printf("Mark: %1$d / %2$d\n", iPrevLine , originalLenLine);
                    //}
                    // Инициализирую объект свойства которого буду заполнять
                    //addedNode = new Node(-1);
                }

                if (sLine.startsWith("[END")) {
                    //section ended

                    if (sectionType == 1) {
                        //add ending of section into saved header
                        MPheader = MPheader + sLine + "\r\n";
                    }
                    if (waySpeed != -1 && addedNodes.size() > 0 && addedEdges.size() > 0) {
                        linkLen = 0;                        
                        boolean fixLink = false;
                        // замена параметров
                        for (Edge edgeI: addedEdges) {
                            edgeI.oneway = wayOneway;
                            edgeI.roadtype = wayClass;
                            edgeI.label = label;
                            if (waySpeed >= 0) {
                                //Edges[j].speed = waySpeed;
                                edgeI.speed = waySpeed;
                            }
                            else {
                                //were not specified
                                //56km/h
                                //Edges[j].speed = 3;
                                edgeI.speed = 3;
                            }
                            if ((wayClass & Highway.HIGHWAY_MASK_LINK) != 0 && !fixLink) {
                                linkLen += Node.distance(edgeI.node2, edgeI.node1);// Distance(NodesNum - 1, NodesNum)
                                if (linkLen > maxLinkLen) {
                                    wayClass &= Highway.HIGHWAY_MASK_MAIN;
                                    //edgeI.roadtype &= Highway.HIGHWAY_MASK_MAIN;
                                    // исправляем тип всем предыдущим и отменяем проверку для последующих
                                    fixLink = true;
                                    for (Edge edgeI2: addedEdges) {
                                        edgeI.roadtype = wayClass;
                                        if (edgeI2.equals(edgeI)) break;
                                    }
                                    //for (Node nodeI: Chain)
                                    //    Edges(Chain(i)).roadtype = WayClass
                                    //'Debug.Print LastWayID; " - "; CStr(LinkLen) 'uncomment to log list of ways
                                    //numDelinked++;
                                } else {
                                    //Call AddChain(j)
                                }
                                
                            }
                        }
                        
                        
                        Nodes.addAll(addedNodes);
                        Edges.addAll(addedEdges);
                    } else {
                // delete after fix
autoINCNodesNum -= addedNodes.size();
                    }
                    //if (iPhase > 0 && iPhase < 3) {
                        //not last pass of section -> goto start of it
                        //relocate in file

                        //Seek(1, iStartLine);
                        //fis.getChannel().position(iStartLine);
                        //System.out.printf("Reset: %1$d / %2$d\n", iPrevLine , originalLenLine);
                        //iPrevLine = iStartLine;
                        //br.reset();
                        //iPhase = iPhase + 1;
                    

                    addedNodes.clear();
                    addedEdges.clear();                        
                    //}
                    //no routing params found in 1st pass - skip way completely
                    //if (iPhase == 1  && (waySpeed == -1)) { 
                    //    iPhase = 0;
                    //}


                    //if no osm2mp info yet found
                    lastCommentHighway = Highway.HIGHWAY_UNSPECIFIED;
                    label = "";
                    //iPhase = 0;
                    sectionType = 0;
                    //numDelinked = 0;

    //*TODO:** goto found: GoTo lNextLine;
                    continue;
                }

                //switch (iPhase) {
                //    case  0:
                        if (sLine.startsWith("; highway")) {
                            //comment, produced by osm2mp
                            lastCommentHighway = Highway.getHighwayType(sLine.substring(12).trim());
                        }
                        if (sectionType == 1) {
                            //line of header section
                            MPheader = MPheader + sLine + "\r\n";
                        }
                //        break;
                    //scan for routing param
                //    case  1:
                        if (sLine.trim().equals("Type=0x1b") ) {
                          lastCommentHighway =Highway.HIGHWAY_FERRY;
                        }
                        if (sLine.startsWith("RouteParam")) {
                            //'skip ext
                            if (sLine.startsWith("RouteParamExt")) { continue; } // break; } //{ goto: GoTo lNoData; // skipp ext}
                            k2 = sLine.indexOf("=") + 1;
                            //split by "," delimiter
                            routep = sLine.substring(k2).split(",");
                            //direct copy of speed
                            waySpeed = Byte.parseByte(routep[0]);
                            //and oneway
                            wayOneway = Byte.parseByte(routep[2]);
                            if (lastCommentHighway == Highway.HIGHWAY_UNSPECIFIED) {
                                //default class
                                //wayClass = 3;
                                wayClass = Highway.HIGHWAY_SECONDARY;
                                //TODO: should be detected by Type and WayClass
                            }
                            else {
                                //get class from osm2mp comment
                                wayClass = lastCommentHighway;
                            }
                        }
                        if (sLine.startsWith("Label")) {
                            //label
                            k2 = sLine.indexOf("=") + 1;
                            label = sLine.substring(k2).trim();
                            if (label.startsWith("~[0x")) {
                                //use only special codes
                                k2 = label.indexOf("]") + 1;
                                label = label.substring(k2).trim();
                            }
                            else {
                                //ignore others
                                label = "";
                            }

                        }
                //        break;
                //    case  3:
                        //scan for node routing info:
                        if (sLine.startsWith("Nod")) {
                            //Nod

                            k2 = sLine.indexOf("=") + 1;
                            if (k2 <= 0) { continue; }// break; } //*TODO:** goto found: GoTo lSkipRoadNode; }
                            routep = sLine.substring(k2).split(",");
                            k = Integer.parseInt(routep[0]);

                            if (k > addedNodes.size()) { //NodesAlloc) {
                                //error: too big node index: " + sLine
                                //*TODO:** goto found: GoTo lSkipRoadNode;
                                System.out.println("error: too big node index: " + sLine);
                                continue; //break;
                            }

                            //k3 = sLine.indexOf(",", k2);
                            //if (k3 < 0) {
                            if (routep.length < 2) {
                                //error: bad NodeID
                                //*TODO:** goto found: GoTo lSkipRoadNode;
                                System.out.println("error: bad NodeID");
                                continue; //break;
                            }
                            nodeID = Integer.parseInt(routep[1]);

                            //update max NodeID
                            if (nodeID > NodeIDMax) { NodeIDMax = nodeID; }

                            //store nodeid
                            //Nodes[NodesNum - thisLineNodes + k].nodeID = nodeID;
                            //Nodes.add(new Node(nodeID));
                            addedNodes.get(k).nodeID = nodeID;
                            
//*TODO:** label found: lSkipRoadNode:;
                        }
                //        break;
                //    case  2:
                        if (sLine.startsWith("Data")) {
                            //geometry
                            // вроде как дубль пред. условия - выкинуть
                            //k = sLine.indexOf("Data");
                            //if (k < 0) { break; } //*TODO:** goto found: GoTo lNoData; }

                            //sWay = "";
                            //"Data" + next char + "="
                            //sPrefix = sLine.substring(0, k + 4) + "=";

                            dataLineNum = dataLineNum + 1;

                            thisLineNodes = 0;

//*TODO:** label found: lNextPoint:;
                            k3 = 0;
                            while (true) {
                                //Node addedNode = new Node(-1);    **
                                //get lat-lon coords from line
                                k = sLine.indexOf("(", k3);//k.toLowerCase().indexOf(sLine.toLowerCase());
                                k2 = sLine.indexOf(",", k);
                                k3 = sLine.indexOf(")", k2);
                                if (k < 0 || k2 < 0 || k3 < 0) { break; } //*TODO:** goto found: GoTo lEndData; }
                                fLat = Double.parseDouble(sLine.substring(k + 1, k2));

                                //k3 = sLine.indexOf(",", k2);
                                //if (k3 < 0) { break; }//*TODO:** goto found: GoTo lEndData; }
                                fLon = Double.parseDouble(sLine.substring(k2 + 1, k3));

                                //fill node info
                                /*
                                Nodes[NodesNum].lat = fLat;
                                Nodes[NodesNum].lon = fLon;
                                //Nodes[NodesNum].Edges = 0;
                                Nodes[NodesNum].nodeID = -1;*/
                                addedNode = new Node(-1);
                                
                                addedNode.lat = fLat;
                                addedNode.lon = fLon;
                                //addedNode.nodeID = -1;
                                                // delete after fix
                addedNode.VBNum = autoINCNodesNum++;
                                addedNodes.add(addedNode);

                                if (thisLineNodes > 0) {
                                    //not the first node of way -> create edge
                                    //Edge jEdge = joinByEdge(Nodes.size() - 2, Nodes.size() - 1);
                                    //Edge jEdge = joinByEdge(Nodes.get(Nodes.size() - 2), addedNode);
                                    Edge jEdge = Edge.joinByEdge(addedNodes.get(addedNodes.size() - 2), addedNode);
                                    //oneway edges is always -> by geometry
                                    /*Edges[j].oneway = wayOneway;
                                    Edges[j].roadtype = wayClass;
                                    Edges[j].label = label;*/

                                    /*jEdge.oneway = wayOneway;
                                    jEdge.roadtype = wayClass;
                                    jEdge.label = label;
                                    if (waySpeed >= 0) {
                                        //Edges[j].speed = waySpeed;
                                        jEdge.speed = waySpeed;
                                    }
                                    else {
                                        //were not specified
                                        //56km/h
                                        //Edges[j].speed = 3;
                                        jEdge.speed = 3;
                                    }*/
                                    addedEdges.add(jEdge);
                                }
                                thisLineNodes ++;

                                //'finish node creation
                                //addNode();
                                
                                //k = k3;
                                //*TODO:** goto found: GoTo lNextPoint;
                            }
//*TODO:** label found: lEndData:;

                            p = 0;

//*TODO:** label found: lNoData:;
                        }
                //        break;
                //}
                //if (!EOF(1)) { //*TODO:** goto found: GoTo lNextLine; }
            }
            //Close(#1);
            br.close();
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getLocalizedMessage() + " : " + filename);
        } catch (IOException ex) {
            System.err.println(ex.getLocalizedMessage());
        }

    }
   
    //Join two nodes by new edge
    //node1 - start node, node2 - end node
    //return: index of new edge
    public static Edge joinByEdge(Node node1, Node node2) {
        Edge result = Edge.joinByEdge(node1, node2);
        Edges.add(result);
        return result;
    }
    
    /*
    // Использую списки, провке размера не нужна
    //Add one edge to dynamic array
    //Assumed, Edges(EdgesNum) filled with required data prior to call
    public static void addEdge() {
        if (EdgesNum >= EdgesAlloc) {
            //realloc if needed
            EdgesAlloc = EdgesAlloc * 2;
            G.redimPreserve(Edges, EdgesAlloc);
        }
        EdgesNum = EdgesNum + 1;
    } */

    //Merge loaded nodes from diffrent ways by NodeID
    private static void joinNodesByID() {
        int i, j;
        int k;
        int mapNum;
        int[] iDmap;
        int[] nodeMap;

        int NodesNum = Nodes.size();

        //if NodeID indexes are too big, we could not use direct mapping
        //max number for direct mapping should be selected with respect to available RAM
        //need more than 40M
        if (NodeIDMax < 10000000L) {

            //SOFT WAY, via direct map from NodeID  (~ O(n) )

            //IDmap(NodeID) = index in Nodes array
            //G.redim(iDmap, NodeIDMax);
            iDmap = new int[NodeIDMax+1];

            for (i = 0; i < NodeIDMax+1; i++) {  // <= ?
                iDmap[i] = -1;
            }

            for (i = 0; i < NodesNum ; i++) {   // <= - 1?
                k = Nodes.get(i).nodeID;

                //without NodeID - not mergable
                if (k >= 0) { //if (k < 0) *TODO:** goto found: GoTo lSkip; }

                    if (iDmap[k] < 0) {
                        //first occurence of NodeID
                        iDmap[k] = i;
                    }
                    else {
                        //hould join
                        Node.mergeNodes(Nodes.get(iDmap[k]), Nodes.get(i));
                    }
                }
//*TODO:** label found: lSkip:;

                if ((i & 8191) == 0) {  //8191
                    //display progress
                    //Form1.Caption = "Join soft " + CStr(i) + " / " + CStr(NodesNum): Form1.Refresh;
                    System.out.printf("Join soft %1$d / %2$d\n", i, Nodes.size());
                }

            }
        //*TODO:** goto found: GoTo lExit;

//*TODO:** label found: lHardWay:;
        } else {
            //HARD WAY, via bubble search (~ O(n^2))

            // IDmap(a) = NodeID
            //G.redim(iDmap, NodesNum);
            iDmap = new int[NodesNum];
            // NodeMap(a) = index of node in Nodes() array
            //G.redim(nodeMap, NodesNum);
            nodeMap = new int[NodesNum];
            mapNum = 0;

            for (i = 0; i <= NodesNum - 1; i++) {
                k = Nodes.get(i).nodeID;
                if (k >= 0) {
                    for (j = 0; j <= i - 1; j++) {
                        if (iDmap[j] == k) {
                            //found - not first occurence - should join
                            Node.mergeNodes(Nodes.get(nodeMap[j]), Nodes.get(i));
                            //*TODO:** goto found: GoTo lFound;
                            break;
                        }
                    }
                    if (j > i - 1) {        // добавил из-за брейка в цикле
                        //not found - first occurence of NodeID
                        nodeMap[mapNum] = i;
                        iDmap[mapNum] = k;
                        mapNum = mapNum + 1;
                    }
                }
//*TODO:** label found: lFound:;

                if ((i & 8191) == 0) {
                    //display progress
                    //Form1.Caption = "Join soft " + CStr(i) + " / " + CStr(NodesNum): Form1.Refresh;
                    System.out.printf("Join hard %1$d / %2$d\n", i, NodesNum);
                }
            }
        }
//*TODO:** label found: lExit:;
    }

    //Join two directions of road way
    //MaxCosine - cosine of max angle between start edges, -0.996 means (175,180) degrees - contradirectional edges or close
    //MaxCosine2 - cosine of max angle between other edges, during going-by-two-ways
    //MinChainLen - length of min two-way road to join
    public static void joinDirections3(double joinDistance, double maxCosine, double maxCosine2, double minChainLen, double combineDistance) {
        int i,  j;
        Node clusterNode;
        boolean mode1;
        Node eNode, dNode;
        int e, d;
        Edge q;
        double dist1;//, dist2;
        Bbox bbox_edge;
        double angl;
        double min_dist;
        Edge min_dist_edge;
        int roadtype;
        int speednew;

        //chain of forward edges
        //int[] edgesForw;
        //ArrayList<Edge> edgesForw;
        Edge[] edgesForw;
        //chain of backward edges
        //int[] edgesBack;
        //ArrayList<Edge> edgesBack;
        Edge[] edgesBack;
        //1 if road is circled
        int loopChain = 0;
        //len of half of road
        int halfChain = 0;

        //Algorithm will check all non-link oneway edges for presence of contradirectional edge in the vicinity
        //All found pairs of edges will be checked in both directions by GoByTwoWays function
        //for presence of continuous road of one type
        //During this check will be created new chain of nodes, which is projection of joining nodes into middle line
        //Then both found ways will be joined into one bidirectional way, consist from new nodes
        //All related roads will reconnected to new way and old edges were deleted

        //int NodesNum = Nodes.size();
        //mark all nodes as not checked - default = null
        /*for (i = 0; i <= NodesNum - 1; i++) {
            //not moved
            Nodes.get(i).mark = null;
        }*/
        //int EdgesNum = Edges.size();
        for (i = 0; i < Edges.size(); i++) {
            Edge edgeI = Edges.get(i);
            edgeI.mark = 1;
            
            //skip deleted and 2-ways edges
            if (edgeI.node1 == null || edgeI.oneway == 0) {
                //*TODO:** goto found: GoTo lFinMarkEdge;
                continue;
            }
            //skip links
            if ((edgeI.roadtype & Highway.HIGHWAY_MASK_LINK) != 0) {
                //*TODO:** goto found: GoTo lFinMarkEdge;
                continue;
            }
            //skip edges between complex connections
            if (edgeI.node1.edgeL.size() != 2  && edgeI.node2.edgeL.size() != 2) { 
                //*TODO:** goto found: GoTo lFinMarkEdge;
                continue;
            }
            edgeI.mark = 0;
//*TODO:** label found: lFinMarkEdge:;
        }

        //rebuild cluster-index from 0
        Clusters.buildNodeClusterIndex(false);

        for (i = 0; i < Edges.size(); i++) {
            Edge edgeI = Edges.get(i);
            //skip marked edge or deleted
            if (edgeI.mark > 0 || edgeI.node1 == null) {
        //*TODO:** goto found: GoTo lSkipEdge;
                continue;
            }
            // так как половина времени циклы возравщаются по continue
            if ((i & 8191) == 0) {
                //show progress
                //Form1.Caption = "JD3: " + CStr(i) + " / " + CStr(EdgesNum): Form1.Refresh;
                System.out.printf("JDS: %1$d / %2$d\r\n", i, Edges.size());
            }

            //get bbox
            //bbox_edge = getEdgeBbox(i);
            bbox_edge = Edge.getEdgeBbox(edgeI.node1, edgeI.node2);
            //'expand it
            Bbox.expandBbox(bbox_edge, joinDistance);
            min_dist = joinDistance;
            min_dist_edge = null;

            //first
            mode1 = false;

            while (true) {
//*TODO:** label found: lSkipNode2:;
                clusterNode = Clusters.getNodeInBboxByCluster(bbox_edge, mode1);
                //if (clusterNode != null) System.out.println("Node(" + clusterNode.VBNum + ").ID = " + clusterNode.nodeID);
                // System.out.println("k = " + clusterNode.VBNum + " ID=" + clusterNode.nodeID);
                //next (next time)
                mode1 = true;
                //no more nodes
                if (clusterNode == null) {
                    //*TODO:** goto found: GoTo lAllNodes;
                    break;
                }

                //skip nodes of same edge, deleted and complex nodes
                if (clusterNode == edgeI.node1  || clusterNode == edgeI.node2  || 
                        clusterNode.nodeID == Mark.MARK_NODEID_DELETED  || clusterNode.edgeL.size() != 2) {
                    //*TODO:** goto found: GoTo lSkipNode2;
                    continue;
                }

                //calc dist from found node to our edge
                dist1 = Node.distanceToSegment(edgeI.node1, edgeI.node2, clusterNode);
                //DistanceToSegment_last_case = Node.DistanceToSegment_last_case;
                //too far, skip
                if (dist1 > min_dist) {
                    //*TODO:** goto found: GoTo lSkipNode2;
                    continue;
                }

                //node is on join distance, check all (2) edges
                for (int d1 = 0; d1 < 2; d1++) {
                    q = clusterNode.edgeL.get(d1);
                    //deleted or 2-way edge or other road class
                    if (q.node1 == null || q.oneway == 0 || q.roadtype != edgeI.roadtype) {
                    //*TODO:** goto found: GoTo lSkipEdge2;
                        continue;
                    }
                    angl = Edge.cosAngleBetweenEdges(q, edgeI);
                    if (angl < maxCosine) {
                        //contradirectional edge or close

                        dist1 = Edge.distanceBetweenSegments(edgeI, q);
                        //found edge close enough
                        if (dist1 < min_dist) {
                            min_dist = dist1;
                            min_dist_edge = q;
                        }
                    }
    //*TODO:** label found: lSkipEdge2:;
                }
            }

            //*TODO:** goto found: GoTo lSkipNode2:;

//*TODO:** label found: lAllNodes:;
            //all nodes in bbox check

            if (min_dist_edge != null) {
                //found edge close enough
                //now - trace two ways in both directions
                //in the process we will fill Chain array with all nodes of joining ways
                //sequence of nodes in Chain will correspond to sequence of nodes on combined way
                //index of new node, where old node should join, will in .mark field of old nodes
                //also will be created two lists of deleting edges separated to two directions - arrays TWforw and TWback

                roadtype = edgeI.roadtype;
                loopChain = 0;
                //ChainNum = 0;
                //TWforwNum = 0;
                //TWbackNum = 0;
                Chain.clear();
                TWforw.clear();
                TWback.clear();

                //first pass, in direction of edge i
                goByTwoWays(edgeI, min_dist_edge, joinDistance, combineDistance, maxCosine2, false);

                //reverse of TWforw and TWback removed, as order of edges have no major effect

                //reverse Chain
                reverseArray(Chain);

                //second pass, in direction of min_dist_edge
                goByTwoWays(min_dist_edge, edgeI, joinDistance, combineDistance, maxCosine2, true);

                //first and last nodes coincide - this is loop road
                if (Chain.get(0).equals(Chain.get(Chain.size() - 1))) { loopChain = 1; }

                //half len of road in nodes
                halfChain = Chain.size() / 2;
                //will "kill" halfchain limit for very short loops
                // TODO: возможно +1 не нужен
                if (halfChain < 10) { halfChain = Chain.size(); }

                //call metric length of found road
                dist1 = 0;
                for (j = 1; j < Chain.size(); j++) {
                    // TODO: марк вроде бы как маркер, а тут как индекс ?
                    dist1 += Node.distance(Chain.get(j - 1).markNode, Chain.get(j).markNode);
                }

                if (dist1 < minChainLen) {
                    //road is too short -> unmark all edges and not delete anything
                    /*for (j = 0; j < Chain.size(); j++) {
                        for (clusterNode = 0; clusterNode < Chain.get(j).edgeL.size(); clusterNode++) {
                            if (Edges.get(Chain.get(j).edgeL[clusterNode]).mark == 2) {
                                Edges.get(Chain.get(j).edgeL[clusterNode]).mark = 1;
                            }
                        }
                    }*/
                    for(Node jChain: Chain) {
                        for(Edge kEdge: jChain.edgeL) {
                            if (kEdge.mark == 2) { kEdge.mark = 1; }
                        }
                    }
                    //*TODO:** goto found: GoTo lSkipDel;
                } else {

                    //process both directions edges list
                    //to build index of edges, which joins between each pair of nodes in Chain

                    //note: is some rare cases chain of nodes have pleats, where nodes of one directions
                    //in Chain swaps position due to non uniform projecting of nodes to middle-line
                    //In this cases one or more edges joins to bidirectional road in backward direction
                    //to the original direction of this one-way line
                    //These edges could be ignored during combining parameter of bidirectional road
                    //(as they are usually very short)
                    //also at least two other edges will overlap in at least one interval between nodes
                    //only one of them will be counted during combining parameter (last in TW* array)
                    //this is considired acceptable, as they are near edges of very same road

                    //G.redim(edgesForw, ChainNum);
                    //edgesForw = new int[ChainNum];
                    //G.redim(edgesBack, ChainNum);
                    //edgesBack = new int[ChainNum];
                    // TODO: не уверен что не будет расширение элементов, но буду надеяться
                    //edgesForw = new ArrayList<Edge>(Chain.size());
                    //edgesBack = new ArrayList<Edge>(Chain.size());
                    edgesForw = new Edge[Chain.size()];
                    edgesBack = new Edge[Chain.size()];
                    // TODO: думаю не актуально
                    /*
                    for (j = 0; j < Chain.size(); j++) {
                        edgesForw.set(j, null);    // -1;
                        edgesBack.set(j, null);    // -1;
                    }
                    */
                    //process forward direction
                    for (j = 0; j < TWforw.size(); j++) {
                        eNode = TWforw.get(j).node1;
                        dNode = TWforw.get(j).node2;
                        //get indexes of nodes inside Chain
                        e = Chain.indexOf(eNode);
                        d = Chain.indexOf(dNode);
                        if (e == -1 || d == -1) {
                            //(should not happen)
                            //edge with nodes not in chain - skip
                    //*TODO:** goto found: GoTo lSkip1;
                            continue;
                        }
//                        System.out.println("indexOf: " + e + ", " + d);

                        if (e < d) {
                            //normal forward edge (or pleat crossing 0 of chain)
                            // ... e ---> d .....
                            //skip too long edges on loop chains as it could be wrong (i.e. pleat edge which cross 0 of chain)
                            if (loopChain == 1  && (d - e) > halfChain) {
                    //*TODO:** goto found: GoTo lSkip1:;
                                continue;
                            }
                            for (int q1 = e; q1 < d; q1++) {
                                //in forward direction between q and q+1 node is edge TWforw(j)
                                //edgesForw.set(q1, TWforw.get(j));
                                edgesForw[q1] = TWforw.get(j);
                            }
                        }
                        else {
                            //pleat edge (or normal crossing 0 of chain)
                            // ---.---> d .... ... .... e --->
                            //'on straight chains forward edge could not go backward without pleat
                            if (loopChain == 0) {
                    //*TODO:** goto found: GoTo lSkip1;
                                continue;
                            }
                            if ((e - d) > halfChain) {
                                //e and d is close to ends of chain
                                //-> this is really forward edge crossing 0 of chain in a loop road
                                for (int q1 = 0; q1 < d; q1++) {
                                    //edgesForw.set(q1, TWforw.get(j));
                                    edgesForw[q1] = TWforw.get(j);
                                }
                                for (int q1 = e; q1 < Chain.size(); q1++) {
                                    //edgesForw.set(q1, TWforw.get(j));
                                    edgesForw[q1] = TWforw.get(j);
                                }
                            }
                        }
    //*TODO:** label found: lSkip1:;
                    }

                    //process backward direction
                    for (j = 0; j < TWback.size(); j++) {
                        eNode = TWback.get(j).node1;
                        dNode = TWback.get(j).node2;
                        //get indexes of nodes inside Chain
                        e = Chain.indexOf(eNode);
                        d = Chain.indexOf(dNode);
                        if (e == -1  || d == -1) {
                            //(should not happen)
                            //edge with nodes not in chain - skip
                //*TODO:** goto found: GoTo lSkip2;
                            continue;
                        }

                        if (d < e) {
                            //normal backward edge (or pleat crossing 0 of chain)
                            // ... d <--- e .....
                            //skip too long edges on loop chains as it could be wrong (i.e. pleat edge which cross 0 of chain)
                            if (loopChain == 1  && (e - d) > halfChain) {
                                //*TODO:** label found: //*TODO:** goto found: GoTo lSkip2:;
                                continue;
                            }
                            for (int q1 = d; q1 < e; q1++) {
                                //edgesBack.set(q1, TWback.get(j));
                                edgesBack[q1] = TWback.get(j);
                            }
                        }
                        else {
                            //pleat edge (or normal crossing 0 of chain)
                            // <-.-- e ... ... .... ... d <--.---.---
                            //on straight chains backward edge could not go forward without pleat
                            if (loopChain == 0) {
                                //*TODO:** goto found: GoTo lSkip2;
                                continue;
                            }
                            if ((d - e) > halfChain) {
                                //e and d is close to ends of chain
                                //-> this is really backward edge crossing 0 of chain in a loop road
                                for (int q1 = 0; q1 < e; q1++) {
                                    edgesBack[q1] = TWback.get(j);
                                }
                                for (int q1 = d; q1 < Chain.size(); q1++) {
                                    edgesBack[q1] = TWback.get(j);
                                }
                            }
                        }
    //*TODO:** label found: lSkip2:;
                    }

                    for (j = 1; j < Chain.size(); j++) {
                        Node chainJ_1 = Chain.get(j - 1);
                        Node chainJ = Chain.get(j);
                        Edge edgesForwJ_1 = edgesForw[j - 1];
                        Edge edgesBackJ_1 = edgesBack[j - 1];
                        dNode = chainJ_1.markNode;
                        eNode = chainJ.markNode;
                        if (dNode != eNode) {
                            /*k = joinByEdge(Nodes.get(Chain[j - 1]).mark, Nodes.get(Chain[j]).mark);
                            Edges.get(k).roadtype = roadtype;
                            Edges.get(k).oneway = 0;
                            Edges.get(k).mark = 1;
                            */
                            Edge edg = joinByEdge(dNode, eNode);
                            // TODO: не будет ли потери при байте
                            edg.roadtype = (byte)roadtype;
                            edg.oneway = 0;
                            edg.mark = 1;

                            if ((edgesForwJ_1 == null) && (edgesBackJ_1 == null)) {
                                //no edges for this interval between nodes
                                //(should never happens)
                                //default value
                                /*Edges.get(k).speed = 3;
                                Edges.get(k).label = "";*/
                                edg.speed = 3;
                                edg.label = "";
                            }
                            else {
                                //get minimal speed class of both edges
                                speednew = 10;
                                resetLabelStats();
                                if (edgesForwJ_1 != null) {
                                    //forward edge present
                                    speednew = edgesForwJ_1.speed;
                                    addLabelStat0(edgesForwJ_1.label);
                                }
                                if (edgesBackJ_1 != null) {
                                    //backward edge present
                                    if (speednew > edgesBackJ_1.speed) { speednew = edgesBackJ_1.speed; }
                                    addLabelStat0(edgesBackJ_1.label);
                                }
                                /*Edges.get(k).speed = speednew;
                                Edges.get(k).label = getLabelByStats(0);*/
                                // TODO (byte)
                                edg.speed = (byte)speednew;
                                edg.label = getLabelByStats(0);
                            }


                            //ends of chain could be oneway if only one edge (or even part is joining there
                            //ex:     * ------> * --------> * ----------> *
                            //             * <-------- * <--------- * <---------- *
                            //joins into:
                            //        *--->*----*------*----*-------*-----*<------*

                            if (edgesBackJ_1 == null) {
                                //no backward edge - result in one-way
                                //Edges.get(k).oneway = 1;
                                edg.oneway = 1;
                            }
                            else if (edgesForwJ_1 == null) {
                                //no forward edge - result in one-way, backward to other road
                                /*Edges.get(k).oneway = 1;
                                Edges.get(k).node1 = Nodes[Chain[j]]..mark;
                                Edges.get(k).node2 = Nodes[Chain[j - 1]]..mark;*/
                                edg.oneway = 1;
                                edg.node1 = eNode;
                                edg.node2 = dNode;
                            }
                        }
                    }

                    //delete all old edges
                    /*for (j = 0; j < TWforw.size(); j++) {
                        TWforw.get(j).delEdge();
                    }
                    for (j = 0; j < TWback.size(); j++) {
                        delEdge(TWback.get(j));
                    }
                    */
                    for(Iterator<Edge> iEdge = TWforw.iterator(); iEdge.hasNext();) {
                        iEdge.next().delEdge();
                    }
                    for(Iterator<Edge> iEdge = TWback.iterator(); iEdge.hasNext();) {
                        iEdge.next().delEdge();
                    }

                    //merge all old nodes into new ones
                    /*for (j = 0; j <= ChainNum - 1; j++) {
                        mergeNodes(Nodes.get(Chain[j]).mark, Chain[j], 1);
                    }*/
                    for(Iterator<Node> iNode = Chain.iterator(); iNode.hasNext();) {
                        Node iNodeN = iNode.next();
                        Node.mergeNodes(iNodeN.markNode, iNodeN, true);
                    }

                    //update cluster index to include only newly created nodes (i.e. nodes of joined road)
                    Clusters.buildNodeClusterIndex(true);

    //*TODO:** label found: lSkipDel:;
                }
            }
//System.out.println("i = " + i);
            //mark edge as checked
            edgeI.mark = 1;

//*TODO:** label found: lSkipEdge:;
        }
    }


    //Find edges of two way road
    //Algorithm goes by finding next edge on side, which is not leading
    //Found new node (end of found edge) is projected to local middle line
    //Array Chain is filled by found nodes
    //Arrays TWforw and TWback is filled by found edges
    //
    //edge1,edge2 - start edges
    //JoinDistance - distance limit between two ways
    //CombineDistance - distance to join two nodes into one (on middle line)
    //MaxCosine2 - angle limit between edges
    //Params: 0 - first pass (chain empty, go by edge1 direction)
    //        1 - second pass (chain contains all 4 nodes of edges at the end, go by edge2 direction)
    //public static void goByTwoWays(int edge1, int edge2, double joinDistance, double combineDistance, double maxCosine2, int params) {
    public static void goByTwoWays(Edge edge1, Edge edge2, double joinDistance, double combineDistance, double maxCosine2, boolean params) {
        //int i;

        //arrow-head edges
        //int edge_side1 = 0;
        //int edge_side2 = 0;
        Edge edge_side1;
        Edge edge_side2;

        //arrow-head nodes
        Node side1i, side1j;
        Node side2i, side2j;
        //flags of circle on each side
        int side1circled;
        int side2circled;

        Node[] side = new Node[4];
        double[] dist = new double[4];
        double dist_t;
        double dx;
        double dy;
        double px, py;
        double dd;
        int roadtype;
        double angl;
        int calc_side;
        double angl_min;
        Edge angl_min_edge;
        int checkchain;
        int passNumber;

        //keep road type for comparing
        roadtype = edge1.roadtype;

        //mark edges as participating in joining
        edge1.mark = 2;
        edge2.mark = 2;

        //arrow-head of finding chains
        edge_side1 = edge1;
        edge_side2 = edge2;

        //i node is back, j is front of arrow-head - on both sides
        side1i = edge1.node1;
        side1j = edge1.node2;
        side2i = edge2.node2;
        side2j = edge2.node1;

        //circles not yet found
        side1circled = 0;
        side2circled = 0;

        passNumber = 0;
        if (params) { passNumber = 1; }

        if (passNumber == 1) {
            //second pass
            //skip initial part, as it is already done in first pass
   //*TODO:** goto found: GoTo lKeepGoing;
        } else {

            //middle line projection vector
            //TODO: fix (not safe to 180/-180 edge)
            //sum of two edges
            dx = (side1j.lat - side1i.lat) + (side2j.lat - side2i.lat);
            dy = (side1j.lon - side1i.lon) + (side2j.lon - side2i.lon);
            //start point - average of two starts
            px = (side1i.lat + side2i.lat) * 0.5;
            py = (side1i.lon + side2i.lon) * 0.5;

            side[0] = side1i;
            side[1] = side1j;
            side[2] = side2i;
            side[3] = side2j;

            //calc relative positions of projections of all 4 noes to edge1
            dd = 1 / (dx * dx + dy * dy);
            for (int i = 0; i <= 3; i++) {
                dist[i] = (side[i].lat - px) * dx + (side[i].lon - py) * dy;
            }

            //Sort dist() and side() by dist() by bubble sort
            for (int i = 0; i <= 3; i++) {
                for (int j = i + 1; j <= 3; j++) {
                    if (dist[j] < dist[i]) {
                        dist_t = dist[j]; dist[j] = dist[i]; dist[i] = dist_t;
                        Node k = side[j]; side[j] = side[i]; side[i] = k;
                    }
                }
            }

            //Add nodes to chain in sorted order
            for (int i = 0; i <= 3; i++) {
                //addChain(side[i]);
                Chain.add(side[i]);
                /*
                Nodes.get(Nodes.size().Edges = 0;
                Nodes.get(Nodes.size().nodeID = -1;
                Nodes.get(Nodes.size().mark = -1;
                //'info that old node will collapse to this new one
                Nodes.get([side[i]).mark = NodesNum;
                //'projected coordinates
                Nodes.get(Nodes.size().lat = px + dist[i] * dx * dd;
                Nodes.get(Nodes.size().lon = py + dist[i] * dy * dd;
                addNode(); */
                Node addedNode = new Node(-1);
                //addedNode.mark = null;
                addedNode.lat = px + dist[i] * dx * dd;
                addedNode.lon = py + dist[i] * dy * dd;
                
                // delete after fix
                addedNode.VBNum = Nodes.size();
                Nodes.add(addedNode);
                side[i].markNode = addedNode;
            }
        }

//*TODO:** label found: lKeepGoing:;
        while (true) {

            angl_min = maxCosine2; angl_min_edge = null;

            if (Chain.get(Chain.size()-1) == side1j) {
                //side1 is leading, side2 should be prolonged
                calc_side = 2;
            }
            else {
                //side2 is leading, side1 should be prolonged
                calc_side = 1;
            }

            if (calc_side == 2) {
                //search edge from side2j which is most opposite to edge_side1
                for (Iterator<Edge> iEdge = side2j.edgeL.iterator(); iEdge.hasNext();) {
                //for (i = 0; i < side2j.Edges - 1; i++) {
                    Edge jEdge = iEdge.next();//Nodes.get(side2j).edgeL[i];
                    // TODO: возможно что-то не так, было j == edge_side2 (индексы)
                    if (jEdge.equals(edge_side2)  || jEdge.node1 == null || jEdge.oneway == 0
                            || jEdge.roadtype != roadtype  || jEdge.node2 != side2j) {
                //*TODO:** goto found: GoTo lSkipEdgeSide2;
                        continue;
                    }
                    //skip same edge_side2, deleted, 2-ways, other road types and directed from this node outside
                    //dist_t = distanceBetweenSegments(j, edge_side1);
                    dist_t = Edge.distanceBetweenSegments(jEdge, edge_side1);
                    //skip too far edges
                    if (dist_t > joinDistance) {
                //*TODO:** goto found: GoTo lSkipEdgeSide2;
                        continue;
                    }
                    //angl = cosAngleBetweenEdges(j, edge_side1);
                    angl = Edge.cosAngleBetweenEdges(jEdge, edge_side1);
                    //remember edge with min angle
                    if (angl < angl_min) { angl_min = angl; angl_min_edge = jEdge; }
    //*TODO:** label found: lSkipEdgeSide2:;
                }

                //mark edge as participating in joining
                edge_side2.mark = 2;
                //add edge to chain (depending on pass number)
                addTW(edge_side2, passNumber);

                if (angl_min_edge == null) {
                    //no edge found - end of chain
                    //mark last edge of side1
                    edge_side1.mark = 2;
                    //and add it to chain
                    addTW(edge_side1, 1 - passNumber);
            //*TODO:** goto found: GoTo lChainEnds;
                    break;
                }

                edge_side2 = angl_min_edge;
                //update i and j nodes of side
                side2i = side2j;
                side2j = edge_side2.node1;

                if (edge_side2.mark == 2) {
                    //found marked edge, this means that we found cycle
                    side2circled = 1;
                }

                if (side2j == side1j) {
                    //found joining of two directions, should end chain
                    //mark both last edges as participating in joining
                    edge_side2.mark = 2;
                    edge_side1.mark = 2;
                    //add them to chains
                    addTW(edge_side2, passNumber);
                    addTW(edge_side1, 1 - passNumber);
            //*TODO:** goto found: GoTo lChainEnds;
                    break;
                }

            }
            else {
                //search edge from side1j which is most opposite to edge_side2
                for (Iterator<Edge> iEdge = side1j.edgeL.iterator(); iEdge.hasNext();) {
                //for (i = 0; i <= Nodes.get(side1j).Edges - 1; i++) {
                    //Edge jEdge = iEdge.next();  //Nodes.get(side1j).edgeL[i];
                    Edge edgeJ = iEdge.next();  //Edges.get(j);
                    if (edgeJ.equals(edge_side1) || edgeJ.oneway == 0 || edgeJ.roadtype != roadtype  || edgeJ.node1 != side1j) {
                //*TODO:** goto found: GoTo lSkipEdgeSide1;
                        continue;
                    }
                    //skip same edge_side1, 2-ways, other road types and directed from this node outside
                    dist_t = Edge.distanceBetweenSegments(edgeJ, edge_side2);
                    //skip too far edges
                    if (dist_t > joinDistance) {
                //*TODO:** goto found: GoTo lSkipEdgeSide1;
                        continue;
                    }
                    angl = Edge.cosAngleBetweenEdges(edgeJ, edge_side2);
                    //remember edge with min angle
                    if (angl < angl_min) { angl_min = angl; angl_min_edge = edgeJ; }
                //*TODO:** label found: lSkipEdgeSide1:;
                }

                //mark edge as participating in joining
                edge_side1.mark = 2;
                //add edge to chain (depending on pass number)
                addTW(edge_side1, 1 - passNumber);

                if (angl_min_edge == null) {
                    //no edge found - end of chain
                    //mark last edge of side2
                    edge_side2.mark = 2;
                    //and add it to chain
                    addTW(edge_side2, passNumber);
                //*TODO:** goto found: GoTo lChainEnds;
                    break;
                }

                edge_side1 = angl_min_edge;
                //update i and j nodes of side
                side1i = side1j;
                side1j = edge_side1.node2;

                if (edge_side1.mark == 2) {
                    //found marked edge, means, that we found cycle
                    side1circled = 1;
                }

                if (side2j == side1j) {
                    //found marked edge, this means that we found cycle
                    //mark both last edges as participating in joining
                    edge_side2.mark = 2;
                    edge_side1.mark = 2;
                    //add them to chains
                    addTW(edge_side2, passNumber);
                    addTW(edge_side1, 1 - passNumber);
            //*TODO:** goto found: GoTo lChainEnds;
                    break;
                }
            }

            //middle line projection vector
            //TODO: fix (not safe to 180/-180 edge)
            dx = side1j.lat - side1i.lat + side2j.lat - side2i.lat;
            dy = side1j.lon - side1i.lon + side2j.lon - side2i.lon;
            px = (side1i.lat + side2i.lat) * 0.5;
            py = (side1i.lon + side2i.lon) * 0.5;
            dd = 1 / (dx * dx + dy * dy);

            //remember current chain len
            checkchain = Chain.size();

            //create new node
            // создаю чуть раньше, т.к. ссылка не него используется в след. блоке
            Node createNode = new Node(-1);

            if (calc_side == 2) {
                //project j node from side2 to middle line
                dist_t = (side2j.lat - px) * dx + (side2j.lon - py) * dy;
                //addChain(side2j);
                Chain.add(side2j);
                //old node will collapse to this new one
                side2j.markNode = createNode;   //Nodes.get(Nodes.size()-1);
            }
            else {
                //project j node from side1 to middle line
                dist_t = (side1j.lat - px) * dx + (side1j.lon - py) * dy;
                Chain.add(side1j);
                //old node will collapse to this new one
                side1j.markNode = createNode;   //Nodes.get(Nodes.size()-1);
            }

            /*Nodes[NodesNum].Edges = 0;
            Nodes[NodesNum]..nodeID = -1;
            Nodes[NodesNum]..mark = -1;
            Nodes[NodesNum]..lat = px + dist_t * dx * dd;
            Nodes[NodesNum]..lon = py + dist_t * dy * dd;
            */
            createNode.lat = px + dist_t * dx * dd;
            createNode.lon = py + dist_t * dy * dd;

            //reproject prev node into current middle line ("ChainNum - 2" because ChainNum were updated above by AddChain)
            //j = Nodes[Chain[ChainNum - 2]]..mark;
            Node jNode = Chain.get(Chain.size() - 2).markNode;
            dist_t = (jNode.lat - px) * dx + (jNode.lon - py) * dy;
            jNode.lat = px + dist_t * dx * dd;
            jNode.lon = py + dist_t * dy * dd;

            if (Node.distance(jNode, createNode /*Nodes.get(Nodes.size()-1)*/) < combineDistance) {
                //Distance from new node to prev-one is too small, collapse node with prev-one
                //TODO(?): averaging coordinates?
                if (calc_side == 2) {
                    side2j.markNode = jNode;
                }
                else {
                    side1j.markNode = jNode;
                }
                //do not call AddNode -> new node will die
            }
            else {
                //addNode();
                                // delete after fix
                createNode.VBNum = Nodes.size();
                
                Nodes.add(createNode);
                //fix order of nodes in chain
                fixChainOrder(checkchain);
            }

            //both sides circled - whole road is a loop
            if (side1circled > 0 && side2circled > 0) {
        //*TODO:** goto found: GoTo lFoundCycle;

//*TODO:** label found: lFoundCycle:;
                //handle cycle road

                //find all nodes from end of chain which is present in chain two times
                //remove all of them, except last one
                //in good cases last node should be same as first node
                //TODO: what if not?
                //for (int i = Chain.size(); i >= 0; i--) {
                int i = Chain.size()-1;
                while(true) {
                    Node iChain = Chain.get(i);
                    int j = 0;
                    for (; j < i; j++) {
                        if (iChain.equals(Chain.get(j))) {
            //*TODO:** goto found: GoTo lFound;
                            i--;
                            break;
                        }
                    }
                    if (j == i) {
                        for (j = Chain.size() - 1; j > i + 1; j--) {
                            Chain.remove(j);
                        }
                        break;
                    }
                    //not found
                    //keep this node (which is one time in chain) and next one (which is two times)
                    // TODO: не понятка как это толком энтерпретировать. Переделал два фора, в один фор и ваил, где регулирование индекса идет в форе
                    //ChainNum = i + 2;
                    //*TODO:** goto found: GoTo lExit;
        //*TODO:** label found: lFound:;
                }
            }

            //proceed to searching next edge
        //*TODO:** goto found: GoTo lKeepGoing;
        }

//*TODO:** label found: lChainEnds:;

        //Node there is chance, that circular way will be not closed from one of sides
        //Algorithm does not handle this case, it should collapse during juctions collapsing

//*TODO:** label found: lExit:;
    }
/*
    //Reverse array into backward direction
    public static void reverseArray(int[] arr, int num) { // TODO: Use of ByRef founded
        int i = 0;
        int j = 0;
        int t = 0;
        //half of len
        j = num / 2;
        for (i = 0; i <= j - 1; i++) {
            //swap elements from first and second halfs
            t = arr[i];
            arr[i] = arr[num - 1 - i];
            arr[num - 1 - i] = t;
        }
    }
*/
    //Add edge into one of TW arrays
    //side: 0 - into TWforw, 1 - into TWback
    public static void addTW(Edge edge1, int side) {
        if (side == 1) {
            TWback.add(edge1);
            /*TWback[TWbackNum] = edge1;
            TWbackNum = TWbackNum + 1;
            if (TWbackNum >= TWalloc) {
                //*TODO:** goto found: GoTo lRealloc;
            }*/
        } else {
            TWforw.add(edge1);
            /*TWforw[TWforwNum] = edge1;
            TWforwNum = TWforwNum + 1;
            if (TWforwNum >= TWalloc) {
//*TODO:** label found: lRealloc:;
                //realloc if needed
                TWalloc = TWalloc * 2;
                G.redimPreserve(TWforw, TWalloc);
                G.redimPreserve(TWback, TWalloc);
            }*/
        }
    }
    /*
    //Find index of node1 in Chain() array, return -1 if not present
    public static int findInChain(Node node1) {
        /*int i = 0;
        
        for (i = 0; i < Chain.size(); i++) {
            if (Chain[i] == node1) { return 0; }
        }*//*
        if (Chain.contains(node1)) { return Chain.indexOf(node1); }
        return -1;
    }
    */

    //Fix order of nodes in Chain
    //Fixing is needed when last node is not new arrow-head of GoByTwoWays algorithm (ex. several short edges of one side, but long edge of other side)
    public static void fixChainOrder(int checkindex) {

        Node i2, i1, i0;
        int k;
        double p;
        //2 or less nodes in chain, nothing to fix
        if (checkindex < 2) { return; }

        //last new node
        i2 = Chain.get(checkindex).markNode;
        //exit in case of probles
        if (i2 == null) { return; }
        //prev new node
        i1 = Chain.get(checkindex - 1).markNode;
        if (i1 == null) { return; }
        //prev-prev new node
        i0 = Chain.get(checkindex - 2).markNode;
        if (i0 == null) { return; }

        k = 3;
        //if prev-prev new nodes is combined with prev new node - find diffent node backward
        while (i0.equals(i1)) {
            //reach Chain(0)
            if (checkindex < k) { return; }
            i0 = Chain.get(checkindex - k).markNode;
            k = k + 1;
        }

        //Scalar multiplication of vectors i0->i1 and i1->i2
        p = (i2.lat - i1.lat) * (i1.lat - i0.lat) + (i2.lon - i1.lon) * (i1.lon - i0.lon);

        if (p < 0) {
            //vectors are contradirectional -> swap
            i0 = Chain.get(checkindex);
            Chain.set(checkindex, Chain.get(checkindex - 1));
            Chain.set(checkindex - 1, i0);
            //check last new node on new place
            fixChainOrder(checkindex - 1);
        }
    }

    public static String getLabelByStats(int flags) {
        //GetLabelByStats = GetLabelByStats1(Text) majoritary version
        //combinatory version
        // TODO: флаг пока не использовался
        return LabelStat.getLabelByStats2(LabelStats);
    }

    //Add label to label stats
    public static void addLabelStat0(String text) {
        //Call AddLabelStat1(Text) majoritary version
        //combinatory version
        LabelStat.addLabelStat2(text, LabelStats);
    }

    //Remove all labels stats from memory
    public static void resetLabelStats() {
        LabelStats.clear();
    }

    //Reverse array into backward direction
    public static void reverseArray(ArrayList<Node> arr) { // TODO: Use of ByRef founded
        int i;
        int j;
        Node t;
        int num = arr.size();
        //half of len
        j = num / 2;
        for (i = 0; i < j; i++) {
            //swap elements from first and second halfs
            t = arr.get(i);
            arr.set(i, arr.get(num - 1 - i));
            arr.set(num - 1 - i, t);
        }
    }

    //Delete edges which connect node with itself
    public static void filterVoidEdges() {
        int i = 0;
        for (i = 0; i < Edges.size(); i++) {
            Edge edgeI = Edges.get(i);
            if ((edgeI.node1 != null) && (edgeI.node1 == edgeI.node2)) {
                edgeI.delEdge();
                // TODO возможно стоит добавить удаление сомого edgeI
            }
        }
    }

    //Save geometry to .mp file with joining chains into polylines
    public static void save_MP_2(String filename) {
        /*int i = 0;
        int k1, k2;
        int typ = 0;*/

        //Open(filename For Output As #2);
        BufferedWriter bw;
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(filename);
            bw = new BufferedWriter(new OutputStreamWriter(fos, "CP1251"));

            bw.write("; Generated by mp_extsimp (java)\r\n");
            bw.newLine();
            bw.write(MPheader);
            bw.newLine();

            for(Iterator<Edge> kEdge = Edges.iterator(); kEdge.hasNext();) {
                Edge kEdgeN = kEdge.next();
                if (kEdgeN.node1 == null) {
                    //deleted edge
                    //mark to ignore
                    kEdgeN.mark = 1;
                } else {
                    //mark to save
                    kEdgeN.mark = 0;
                }
            }
            numFormat = new DecimalFormat("0.#######");
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            numFormat.setDecimalFormatSymbols(dfs);

            int count = 0;
            for(Iterator<Edge> kEdge = Edges.iterator(); kEdge.hasNext();) {
                Edge kEdgeN = kEdge.next();
                if (kEdgeN.mark == 0) {
                    //all marked to save - find chain and save
                    bw.write(saveChain(kEdgeN));
                }
                if (((count++) & 8191) == 0) {
                    //show progress
                    //Form1.Caption = "JD3: " + CStr(i) + " / " + CStr(EdgesNum): Form1.Refresh;
                    System.out.printf("Save: %1$d / %2$d\r\n", count, Edges.size());
                }                
            }

            //file finalization flag
            bw.write("; Completed\r\n");

            bw.close();
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getLocalizedMessage() + " : " + filename);
        } catch (IOException ex) {
            System.err.println(ex.getLocalizedMessage());
        }
    }

    //Save chain of edges into mp file (already opened as #2)
    public static String saveChain(Edge edge1) {

        String result = "";
        int m;
        boolean chainEnd = false;
        Edge nextChainEdge = null;

        Node startNode;

        Edge refEdge;

        //Algorithm go from specified edge into one direction by chain of nodes
        //(nodes connected one by one, without junctions) until end (or junction) is reached
        //After that algorithm will go from final edge into opposite direction and will compare edges
        //and add nodes into Chain array
        //On findind different edge (or reaching other end of chain) algorithm will save found (sub)chain into mp file
        //Then rest of chain (if it exits) will be processed in similar way

        //1) go by chain to the one end - to node with !=2 edges

        //start node
        Node nodeI = edge1.node1;
        Node nodeJ = edge1.node2;
        Node nodeK;
        startNode = nodeJ;
        
        if (nodeI.edgeL.size() != 2) {
            //i is end of chain
            //ChainNum = 0;
            Chain.clear(); //= new ArrayList<Node>();
            //addChain(i);
            //addChain(j);
            Chain.add(nodeI);
            Chain.add(nodeJ);
            refEdge = new Edge(edge1);
            
            //saved
            edge1.mark = 1;

            if (nodeJ.edgeL.size() != 2) {
                //that's all
                chainEnd = true;
                result += saveChainInString(refEdge);
        //*TODO:** goto found: GoTo lBreak;
            }
            else {
                nodeJ = Chain.get(0);
                nodeI = Chain.get(1);
        //*TODO:** goto found: GoTo lGoNext2;
            }
        } else {

    //*TODO:** label found: lGoNext:;
            while (true) {
                //go by chain
                nodeK = goByChain(nodeI, nodeJ);
                //if still 2 edges and we have not found loop - proceed
                if (nodeK.edgeL.size() == 2 && nodeK != startNode) {
                    nodeJ = nodeI;
                    nodeI = nodeK;
            //GoTo lGoNext;
                } else { break; }
            }

            //   *-----*-----*-----*---...
            //   k     i     j

            //OK, we found end of chain
            nodeJ = nodeK;

            //   *---------*-----*-----*---...
            //  k=j        i

            //2) go revert - from found end to another one and saving all nodes into Chain() array

            //ChainNum = 0;
            Chain.clear();// = new ArrayList<Node>();
            //addChain(k);
            //addChain(i);
            Chain.add(nodeK);
            Chain.add(nodeI);
            startNode = nodeK;

            //keep info about first edge in chain
            refEdge = new Edge(GoByChain_lastedge);
            GoByChain_lastedge.mark = 1;
            //reversed oneway
            if (refEdge.node1 != Chain.get(0) && refEdge.oneway == 1) { refEdge.oneway = 2; }
        }
//*TODO:** label found: lGoNext2:;

        while (!chainEnd) {
            nodeK = goByChain(nodeI, nodeJ);

            //   *-------------*-----*-----*---...
            //  j              i     k

            //check oneway
            m = GoByChain_lastedge.oneway;
            if (m > 0 && GoByChain_lastedge.node1 != nodeI) { m = 2; }

            //if oneway flag is differnt or road type, speed or label is changed - break chain
            if (m != refEdge.oneway) {
                nextChainEdge = GoByChain_lastedge;
        //GoTo lBreak;
            }
            if (GoByChain_lastedge.roadtype != refEdge.roadtype) {
                nextChainEdge = GoByChain_lastedge;
        //GoTo lBreak;
            }
            if (GoByChain_lastedge.speed != refEdge.speed) {
                nextChainEdge = GoByChain_lastedge;
        //GoTo lBreak;
            }
            if (!(GoByChain_lastedge.label.equals(refEdge.label))) {
                nextChainEdge = GoByChain_lastedge;
        //GoTo lBreak;
            }

            if (nextChainEdge != GoByChain_lastedge) {
                //saved
                GoByChain_lastedge.mark = 1;

                //addChain(k);
                Chain.add(nodeK);

                if (nodeK.edgeL.size() == 2 && nodeK != startNode) {
                    //still 2 edges - still chain
                    nodeJ = nodeI;
                    nodeI = nodeK;
                    continue;
            //*TODO:** goto found: GoTo lGoNext2;
                } else {
                    chainEnd = true;
                }
            }

    //*TODO:** label found: lBreak:;
            //3) save chain to file

            result += saveChainInString(refEdge);

            if (!chainEnd) {
                //continue with this chain, as it is not ended

                //   *================*--------------------*-----------*-----*---...
                //                        NextChainEdge

                //new reference info
                refEdge = new Edge(nextChainEdge);
                if (refEdge.node1 == Chain.get(Chain.size()-1)) {
                    nodeI = refEdge.node2;
                    nodeJ = refEdge.node1;
                }
                else {
                    if (refEdge.oneway == 1) { refEdge.oneway = 2; }
                    nodeI = refEdge.node1;
                    nodeJ = refEdge.node2;
                }

                //   *================*--------------------*-----------*-----*---...
                //                    j                    i

                nextChainEdge.mark = 1;

                //add both nodes of last edge
                //ChainNum = 0;
                //addChain(j);
                //addChain(i);
                Chain.clear();  // = new ArrayList<Node>();
                Chain.add(nodeJ);
                Chain.add(nodeI);
                if (nodeI.edgeL.size() != 2) {
                    //chain from one edge
                    chainEnd = true;
                    result += saveChainInString(refEdge);
                    continue; //break;  // без разницы флаг конца установлен

        //*TODO:** goto found: GoTo lBreak;
                } else {

                    nextChainEdge = null;
                    continue;
                }
                //continue with chain
        //*TODO:** goto found: GoTo lGoNext2;
            }
        }

        return result;
    }

    //Go by chain from node1 in some direction, but not to Node0
    //(assumed, that node1 have two edges, not 1, not 3 or more, otherwise - UB)
    //Usage: GoByChain(x,x) goes by first edge, z=GoByChain(x,y)->u=GoByChain(z,x)->... allows to travel by chain node by node
    public static Node goByChain(Node node1, Node node0) {
        //check first edge
        Edge i = node1.edgeL.get(0);
        Node k = i.node1;
        if (k == node1) { k = i.node2; }
        GoByChain_lastedge = i;
        if (k == node0) {
            //node0 -> check second edge
            i = node1.edgeL.get(1);
            k = i.node1;
            if (k == node1) { k = i.node2; }
            GoByChain_lastedge = i;
        }
        return k;
    }

    private static String saveChainInString(Edge refEdge) {
        String result = "";
        //Print #2, "; roadtype=" + CStr(refedge.roadtype) 'debug info about road type
        //Print #2, "[POLYLINE]";
        result += "[POLYLINE]\r\n";
        //object type - from road type
        int typ = Highway.getType_by_Highway(refEdge.roadtype);
        //Print #2, "Type=0x"; Hex(typ);
        result += String.format("Type=0x%1$X\r\n", typ);
        if (refEdge.label.length() > 0) {
            //labels - into special codes fro labelization
            //Print #2, "Label=~[0x05]" + refEdge.label;
            //Print #2, "StreetDesc=~[0x05]" + refEdge.label;
            result += "Label=~[0x05]" + refEdge.label + "\r\n";
            result += "StreetDesc=~[0x05]" + refEdge.label + "\r\n";
        }
        //oneway indicator
        if (refEdge.oneway > 0) {
            //Print #2, "DirIndicator=1";
            result += "DirIndicator=1\r\n";
        }
        //top level of visibility - from road type
        //Print #2, "EndLevel=" + CStr(GetTopLevel_by_Highway(refEdge.roadtype));
        //Print #2, "RouteParam=";
        result += "EndLevel=" + Highway.getTopLevel_by_Highway(refEdge.roadtype) + "\r\n";
        result += "RouteParam=";
        //speed class
        //Print #2, CStr(refEdge.speed); ",";
        result += refEdge.speed + ",";
        //road class - from road type
        //Print #2, CStr(GetClass_by_Highway(refEdge.roadtype)); ",";
        result += Highway.getClass_by_Highway(refEdge.roadtype) + ",";
        if (refEdge.oneway > 0) {
            //one_way
            //Print #2, "1,";
            result += "1,";
        } else {
            //Print #2, "0,";
            result += "0,";
        }
        //other params are not handled
        //Print #2, "0,0,0,0,0,0,0,0,0";
        result += "0,0,0,0,0,0,0,0,0\r\n";
        //Print #2, "Data0=";
        result += "Data0=";
        if (refEdge.oneway == 2) {
            //reverted oneway, save in backward sequence
            /*for (i = ChainNum - 1; i <= 0; i--) {
                if (i != ChainNum - 1) { Print #2, ","; }
                Print #2, "("; CStr(Nodes(Chain(i)).lat); ","; CStr(Nodes(Chain(i)).lon); ")";
            }*/
            for(int i = Chain.size() - 1; i >= 0; i--) {
                Node iChain = Chain.get(i);
                if (i != Chain.size() - 1) { result += ","; }
                result += "(" + numFormat.format(iChain.lat) + "," + numFormat.format(iChain.lon) +")";
            }
            //Print #2,;
            result += "\r\n";
            //Print #2, "Nod1=0,"; CStr(Chain(ChainNum - 1)); ",0";
            //Print #2, "Nod2=" + CStr(ChainNum - 1) + ","; CStr(Chain(0)); ",0";
            result += String.format("Nod1=0,%d,0\r\n", Nodes.indexOf(Chain.get(Chain.size()-1)));
            result += String.format("Nod2=%d,%d,0\r\n", Chain.size()-1, Nodes.indexOf(Chain.get(0)));
        }
        else {
            //forward oneway or twoway, save in direct sequence
            /*for (i = 0; i <= ChainNum - 1; i++) {
                if (i != 0) { Print #2, ","; }
                Print #2, "("; CStr(Nodes(Chain(i)).lat); ","; CStr(Nodes(Chain(i)).lon); ")";
            }
            Print #2,;
            Print #2, "Nod1=0,"; CStr(Chain(0)); ",0";
            Print #2, "Nod2=" + CStr(ChainNum - 1) + ","; CStr(Chain(ChainNum - 1)); ",0";
            */

            for(int i = 0; i < Chain.size(); i++) {
                Node iChain = Chain.get(i);
                if (i != 0) { result += ","; }
                //result += String.format("(%.7f,%.7f)", iChain.lat, iChain.lon);
                result += "(" + numFormat.format(iChain.lat) + "," + numFormat.format(iChain.lon) +")";
            }
            result += "\r\n";
            result += String.format("Nod1=0,%d,0\r\n", Nodes.indexOf(Chain.get(0)));
            result += String.format("Nod2=%d,%d,0\r\n", Chain.size()-1, Nodes.indexOf(Chain.get(Chain.size()-1)));
        }
        //Print #2, "[END]";
        //Print #2, "";
        result += "[END]\r\n";
        result += "\r\n";

        return result;
    }

    //find and optimize all chains by Douglas-Peucker with Epsilon (in metres) and limiting max edge (in metres)
    public static void douglasPeucker_total_split(double epsilon, double maxEdge) {
        for (Node nodeI : Nodes) {
            //mark all nodes as not passed
            nodeI.mark = 0;
        }
        for (Node nodeI : Nodes) {
            if (nodeI.nodeID == Mark.MARK_NODEID_DELETED  || nodeI.edgeL.size() != 2  || nodeI.mark == 1) { // nodeI.mark == 1
                //*TODO:** goto found: GoTo lSkip;
            } else {
            //node: not deleted, not yet passed and with 2 edges -> should be checked for chain
                douglasPeucker_chain_split(nodeI, epsilon, maxEdge);
            }
//*TODO:** label found: lSkip:;
            if ((nodeI.VBNum & 8191) == 0) {
                //show progress
                System.out.print("Doug-Pek sp " + nodeI.VBNum + " / " + Nodes.size() + "\r");
            }
        }
    }

    //find one chain (starting from node1) and optimize it by Douglas-Peucker with Epsilon (in metres) and limiting edge len by MaxEdge
    // Ну ооочень похоже на SaveChain
    public static void douglasPeucker_chain_split(Node node1, double epsilon, double maxEdge) {
        Node nodeI, nodeJ, nodeK;
        Edge refEdge;
        boolean chainEnd = false;
        Edge nextChainEdge = null;
        int m;

        //Algorithm works as DouglasPeucker_chain above
        //difference is only inside OptimizeByDouglasPeucker_One_split

        //1) go by chain to the one end - to node with !=2 edges

        //start node
        nodeI = node1;
        nodeJ = node1;
//*TODO:** label found: lGoNext:;
        while (true) {
            //go by chain
            nodeK = goByChain(nodeI, nodeJ);
            //'if still 2 edges - proceed
            if (nodeK != node1 && nodeK.edgeL.size() == 2) {
                nodeJ = nodeI; nodeI = nodeK;
                //GoTo lGoNext;
            } else { break; }
        }
        //   *-----*-----*-----*---...
        //   k     i     j

        //OK, we found end of chain
        nodeJ = nodeK;

        //   *---------*-----*-----*---...
        //  k=j        i

        //2) go revert - from found end to another one and saving all nodes into Chain() array

        //ChainNum = 0;
        Chain.clear();  // = new ArrayList<Node>();
        //addChain(k);
        //addChain(i);
        Chain.add(nodeK);
        Chain.add(nodeI);

        //keep info about first edge in chain
        refEdge = new Edge(GoByChain_lastedge);
        //reversed oneway
        if (refEdge.node1 != Chain.get(0) && refEdge.oneway == 1) { refEdge.oneway = 2; }

//*TODO:** label found: lGoNext2:;
        while (!chainEnd) {
            nodeK = goByChain(nodeI, nodeJ);

            //   *-------------*-----*-----*---...
            //  j              i     k

            //check oneway
            m = GoByChain_lastedge.oneway;
            if (m > 0 && GoByChain_lastedge.node1 != nodeI) { m = 2; }

            //if oneway flag is differnt or road type is changed - break chain
            if (m != refEdge.oneway) {
                nextChainEdge = GoByChain_lastedge;
        //GoTo lBreak;
            }
            if (GoByChain_lastedge.roadtype != refEdge.roadtype) {
                nextChainEdge = GoByChain_lastedge;
        //GoTo lBreak;
            }

            if (nextChainEdge != GoByChain_lastedge) {
                Chain.add(nodeK);

                if (nodeK != Chain.get(0) && nodeK.edgeL.size() == 2) {
                    //still 2 edges - still chain
                    nodeK.mark = 1;
                    nodeJ = nodeI;
                    nodeI = nodeK;
                    continue;
            //*TODO:** goto found: GoTo lGoNext2;
                } else {
                    chainEnd = true;
                }
            }

//*TODO:** label found: lBreak:;

            //3) optimize found chain by D-P
            //optimizeByDouglasPeucker_One_split(0, ChainNum - 1, epsilon, refEdge, maxEdge);
            // передача объектов некатит когда есть повторения объектов в Chain
            optimizeByDouglasPeucker_One_split(0, Chain.size() - 1, epsilon, refEdge, maxEdge);

            if (!chainEnd) {
                //continue with this chain, as it is not ended

                //   *================*--------------------*-----------*-----*---...
                //                        NextChainEdge

                //new reference info
                refEdge = new Edge(nextChainEdge);
                if (refEdge.node1 == Chain.get(Chain.size()-1)) {
                    nodeI = refEdge.node2;
                    nodeJ = refEdge.node1;
                }
                else {
                    if (refEdge.oneway == 1) { refEdge.oneway = 2; }
                    nodeI = refEdge.node1;
                    nodeJ = refEdge.node2;
                }

                //   *================*--------------------*-----------*-----*---...
                //                    j                    i

                //chain from one edge - nothing to optimize by D-P
                if (nodeI.edgeL.size() != 2) { return; }

                //add both nodes of last edge
                Chain.clear();  // = new ArrayList<Node>();
                Chain.add(nodeJ);
                Chain.add(nodeI);

                nextChainEdge = null;

                //continue with chain
        //*TODO:** goto found: GoTo lGoNext2;
            }
        }
    }

    //Recursive check to optimize chain/subchain by Douglas-Peucker with Epsilon (in metres) and limiting edge len by MaxEdge
    //subchain is defined by IndexStart,IndexLast
    //refEdge - road parameters of chain (for create new edge in case of optimization)
    //(180/-180 safe)
    private static void optimizeByDouglasPeucker_One_split(int indexStart, int indexLast, double epsilon, Edge refEdge, double maxEdge) {
        int i = 0;
        int farestNode = -1;
        double farestDist = 0;
        double dist = 0;
        double k = 0;
        //double scalarMult = 0;
        int newspeed = 0;
        String newlabel = "";
        //int indexStart = Chain.indexOf(nodeStart);
        //int indexLast = Chain.indexOf(nodeLast);
        Node nodeStart = Chain.get(indexStart);
        Node nodeLast = Chain.get(indexLast);

        //one edge (or less) -> nothing to do
        if (((indexStart + 1) >= indexLast)) { return; }

        //distance between subchain edge
        k = Node.distance(nodeStart, nodeLast);

        //find node, farest from line first-last node (farer than Epsilon)
        //start max len - Epsilon
        farestDist = epsilon;
        //nothing yet found
        for (i = indexStart + 1; i < indexLast; i++) {
            Node nodeI = Chain.get(i);
            if (k == 0) {
                //circled subchain
                dist = Node.distance(nodeI, nodeStart);
            }
            else {
                dist = Node.distanceToSegment(nodeStart, nodeLast, nodeI);
            }
            if (dist > farestDist) {
                farestDist = dist; farestNode = i; //nodeI;
            }

            if (Node.distance(nodeI, nodeStart) > maxEdge) {
                //distance from start to this node is more than limit -> we should keep this node
                farestNode = i; //nodeI;
    //*TODO:** goto found: GoTo lKeepFar;
                // т.к. farestNode != null след. условие пропускается и переходим как раз на нужную метку
                break;
            }
        }

        if (farestNode == -1) {
            //farest node not found -> all distances less than Epsilon -> remove all internal nodes

            //calc speed and label from all subchain edges
            estimateChain(indexStart, indexLast);
            newspeed = EstimateChain_speed;
            newlabel = EstimateChain_label;

            for (i = indexStart + 1; i < indexLast; i++) {
                //kill with edges
                Chain.get(i).delNode();
            }
            Edge edgeI;
            //join first and last nodes by new edge
            if (refEdge.oneway == 2) {
                //reversed oneway
                edgeI = joinByEdge(nodeLast, nodeStart);
                edgeI.oneway = 1;
            }
            else {
                edgeI = joinByEdge(nodeStart, nodeLast);
                edgeI.oneway = refEdge.oneway;
            }
            edgeI.roadtype = refEdge.roadtype;
            edgeI.speed = (byte)newspeed;
            edgeI.label = newlabel;

            return;
        }

//*TODO:** label found: lKeepFar:;
        //farest point found - keep it
        //call Douglas-Peucker for two new subchains
        //Douglas-Peucker for two new subchains
        optimizeByDouglasPeucker_One_split(indexStart, farestNode, epsilon, refEdge, maxEdge);
        optimizeByDouglasPeucker_One_split(farestNode, indexLast, epsilon, refEdge, maxEdge);

    }

    //Calc speedclass and label of combined subchain of edges
    public static void estimateChain(int indexStart, int indexLast) {

        for (int i = 0; i <= 10; i++) {
            SpeedHistogram[i] = 0;
        }

        EstimateChain_label = "";
        EstimateChain_speed = 0;
        resetLabelStats();

        for (int i = indexStart; i < indexLast; i++) {
            Edge edgeJ = Edge.getEdgeBetween(Chain.get(i), Chain.get(i + 1));
            if (edgeJ != null) {
                //add label of edge into stats
                addLabelStat0(edgeJ.label);
                //add speed into histogram
                SpeedHistogram[edgeJ.speed]++;
            }
        }

        //estimate speed
        EstimateChain_speed = Highway.estimateSpeedByHistogram(SpeedHistogram);
        //calc resulting label
        EstimateChain_label = getLabelByStats(0);
    }

    //Collapse all junctions to nodes
    //junctions detected as clouds of _links and short loops
    //SlideMaxDist - max distance allowed to slide by border-nodes
    //LoopLimit - max loop considered as junction
    //AngleLimit - min angle between aiming edges to use precise calc of centroid
    public static void collapseJunctions2(double slideMaxDist, double loopLimit, double angleLimit) {

        int joinGroups = 0;
        //controids
        //int[] joinedNode;
        int joiningNodes = 0;
        int passNumber = 0;
        int borderNodes;
        //Bbox[] joinGroupBox;

        passNumber = 1;

        //Algorithm marks _link edges as junctions
        //then all edges checked for participating of short loops, if short loop found all its edges also marked as junctions
        //then all junction edges tries to collapse with keeping connection points (border nodes) on other road
        //border nodes can slide on other edges while construction of junction edges tries to shrink like stretched rubber
        //siding also marks passed edges as part of collapsing junctions
        //final constructions from collapsing edges is separated from each other
        //then centroids of this constructions found, it is done by finding point which minimizes sum of squares
        //of distances to aiming lines, aiming lines is build from all edges connecting to collapsing junction and
        //all not _link edges of this junctions
        //then all collapsing edges deleted, all nodes - joined into centroid
        //then algorithm reiterates from start till no junction were found

// label found: lIteration:;
        while (true) {
            //mark all as not-yet-joining
            for (Node nodeI: Nodes) {
                //no wave
                nodeI.mark = 0;
                //no wave
                nodeI.temp_dist = 0;
            }

            //1) Mark all potential parts of junctions
            for (Edge edgeI: Edges) {
                //not checked
                edgeI.mark = 0;
                if (edgeI.node1 != null) {
                    if ((edgeI.roadtype & Highway.HIGHWAY_MASK_LINK) != 0) {
                        //all links are parts of junctions
                        edgeI.mark = Mark.MARK_JUNCTION;
                    }
                }
            }
            int k = 0;
            for (Edge edgeI: Edges) {
                if (edgeI.node1 != null) {
                    //check all edges (not links) for short loops
                    //all short loops also marked as part of junctions

                    if ((edgeI.roadtype & Highway.HIGHWAY_MASK_LINK) == 0  && (edgeI.mark & Mark.MARK_JUNCTION) == 0) {
                        //not _link, not yet marked junction
                        //not marked distcheck -> should check it
                        if ((edgeI.mark & Mark.MARK_DISTCHECK) == 0) { checkShortLoop2(edgeI, loopLimit); }
                    }
                }
                if ((k++ & 65535) == 0) {
                    //display progress
                    System.out.println("Collapse " + passNumber + ", Shorts " + k + " / " + Edges.size());
                }
            }

            for (Node nodeI: Nodes) {
                //not in join group
                nodeI.mark = -1;
            }

            //2) mark edges by trying to shrink junction
            joinGroups = 0;
            for (Node nodeI: Nodes) {
                if (nodeI.nodeID != Mark.MARK_NODEID_DELETED  && nodeI.mark == -1) {
                    //not deleted node, not marked yet -> should check

                    //start new group
                    //ChainNum = 0;
                    Chain.clear();  // = new ArrayList<Node>();
                    borderNodes = 0; //nodeI;    // далее он ссылается на Chain(borderNode), а я его только что обнулил и добавляю
                    //Без изменений он ссылается на 0-й элемент
                    //add this node to a chain
                    Chain.add(nodeI);

                    //check all edges of node for collapsing
                    checkForCollapeByChain2(Chain.get(0));

                    //node is border-node
                    /*if (Chain.get(0).mark == Mark.MARK_NODE_BORDER) {
                        if (Chain.size() > 1 ) {
                            borderNodes = Chain.get(1);
                        } else {
                            System.out.println("Chain size < 2");
                        }
                    }*/

                    int j = 1;
                    if (Chain.size() > 1) {
                        //node is border-node
                        // перенес сюда, т.к. Chain(1) может и не существовать, но если так, то borderNodes далее не используется
                        if (Chain.get(0).mark == Mark.MARK_NODE_BORDER) { borderNodes = 1; /*Chain.get(1); */}
                        //at least 2 nodes found to collapse

    // label found: lRecheckAgain:;
                        while (true) {
                            //continue to check all edges of added nodes by chain
                            while (j < Chain.size()) {
                                Node nodeJ = Chain.get(j);
                                checkForCollapeByChain2(nodeJ);

                                if (nodeJ.mark == Mark.MARK_NODE_BORDER) {
                                    //border-node found - move it to start of chain (by swap)
                                    Node nodeK = Chain.get(borderNodes);
                                    Chain.set(borderNodes, nodeJ);
                                    Chain.set(j, nodeK);

                                    borderNodes++; //= Chain.get(Chain.indexOf(borderNodes) + 1);
                                }
                                j++;
                            }

                            if (borderNodes > 1) {
                                //if border-nodes found - shrink whole construction by sliding border-nodes by geometry
                                shrinkBorderNodes(borderNodes, slideMaxDist);
                                borderNodes = 0; //Chain.get(0);
                    // goto found: GoTo lRecheckAgain;
                            } else { break; }
                        }
                        //mark all found nodes
                        for (Node chainJ: Chain) {
                            chainJ.mark = Mark.MARK_NODE_OF_JUNCTION;
                        }
                    }
                }
                if ((nodeI.VBNum & 8191) == 0) {
                    //show progress
                    System.out.println("Collapse " + passNumber + ", Shrink " + nodeI.VBNum + " / " + Nodes.size());
                }
            }

            //3) group edges to separate junctions
            joinGroups = 0;
            for (Node nodeI: Nodes) {
                if (nodeI.mark == Mark.MARK_NODE_OF_JUNCTION) {
                    Chain.clear();  // = new ArrayList<Node>();
                    //add this node to a chain
                    Chain.add(nodeI);
                    int j = 0;
                    while (j < Chain.size()) {
                        //check all edges and add their other ends if they are collapsing
                        groupCollapse(Chain.get(j));
                        j = j + 1;
                    }

                    if (Chain.size() > 1) {
                        //2 or more found - new group
                        for (Node chainJ: Chain) {
                            chainJ.mark = joinGroups;
                        }
                        joinGroups = joinGroups + 1;
                    }
                }
            }

            //4) calculate coordinates of centroid for collapsed junction

            //G.redim(joinedNode, joinGroups);
            //G.redim(joinGroupBox, joinGroups);
            ArrayList<Node> joinedNode = new ArrayList<>(joinGroups);
            ArrayList<Bbox> joinGroupBox = new ArrayList<>(joinGroups);

            //Create nodes for all found join-groups
            for (int i = 0; i <= joinGroups; i++) {
                Node addedNode = new Node(-1);
                addedNode.mark = -1;
                //fake coords, so cluster-index algo will not get (0,0) coords
                addedNode.lat = Nodes.get(0).lat;
                addedNode.lon = Nodes.get(0).lon;
                addedNode.VBNum = Nodes.size();
                Nodes.add(addedNode);
                joinedNode.add(addedNode);
                Bbox addedBox = new Bbox(360, -360, 360, -360);
                joinGroupBox.add(addedBox);
            }

            //calc bboxes for all join groups
            for (Node nodeJ: Nodes) {
                if (nodeJ.mark >= 0) {
                    Bbox jGBi = joinGroupBox.get(nodeJ.mark);

                    //update bbox for group
                    if (nodeJ.lat < jGBi.lat_min) { jGBi.lat_min = nodeJ.lat; }
                    if (nodeJ.lat > jGBi.lat_max) { jGBi.lat_max = nodeJ.lat; }
                    if (nodeJ.lon < jGBi.lon_min) { jGBi.lon_min = nodeJ.lon; }
                    if (nodeJ.lon > jGBi.lon_max) { jGBi.lon_max = nodeJ.lon; }
                }
            }

            //rebuild cluster-index from zero
            Clusters.buildNodeClusterIndex(false);

            for (int i = 0; i < joinGroups; i++) {
                joiningNodes = 0;
                //AimEdgesNum = 0;
                AimEdges = new ArrayList<>(200);

                //first
                boolean mode1 = false;
    // label found: lNextNode:;
                while (true) {
                    //get node from bbox by cluster-index
                    //without cluster-index search have complexety ~O(n^2)
                    Node nodeJ = Clusters.getNodeInBboxByCluster(joinGroupBox.get(i), mode1);
                    //"next" next time
                    mode1 = true;

                    if (nodeJ != null) {
                        if (nodeJ.mark == i) {
                            //this joining group
                            joiningNodes++;
                            for (Edge edgeE : nodeJ.edgeL) {
                                //skip already marked as aiming
                                if ((edgeE.mark & Mark.MARK_AIMING) > 0) {
                        // goto found: GoTo lSkipEdge;
                                    continue;
                                }
                                //skip all collapsing junctions
                                if ((edgeE.mark & Mark.MARK_COLLAPSING) > 0 && (edgeE.mark & Mark.MARK_JUNCTION) > 0) {
                        // goto found: GoTo lSkipEdge;
                                    continue;
                                }

                                //remain: all edges which will survive from all nodes of this join group
                                //plus all collapsing edges of main roads (this needed for removing noise of last edges near junctions)
                                AimEdge addedAE = new AimEdge();
                                addedAE.lat1 = nodeJ.lat;
                                addedAE.lon1 = nodeJ.lon;
                                if (edgeE.node1 == nodeJ) {
                                    //lat1-lon1 is always a node which will collapse (and so points to junction)
                                    addedAE.lat2 = edgeE.node2.lat;
                                    addedAE.lon2 = edgeE.node2.lon;
                                }
                                else {
                                    addedAE.lat2 = edgeE.node1.lat;
                                    addedAE.lon2 = edgeE.node1.lon;
                                }
                                if ((edgeE.mark & Mark.MARK_COLLAPSING) > 0) {
                                    //mark as aiming, for skip it next time
                                    edgeE.mark |= Mark.MARK_AIMING;
                                }
                                AimEdges.add(addedAE);
        // label found: lSkipEdge:;
                            }
                        }
                // goto found: GoTo lNextNode;
                    } else { break; }
                }

                if (joiningNodes > 0) {
                    if ((joiningNodes & 127) == 0) {
                        //display progress
                        //Form1.Caption = "Collapse " + CStr(passNumber) + ", Aim " + CStr(i) + " / " + CStr(joinGroups): Form1.Refresh;
                    }
                    //find centroid of junction
                    /*joinedNode.set(i, findAiming(joinedNode.get(i), angleLimit));
                    // т.к. я создаю новый объект, чтобы не менять входной объект, нужно его так же заменить в Nodes
                    // хотя в оригинале именно так
                    Nodes.set(joinedNode.get(i).VBNum, joinedNode.get(i));
                    */
                    // пробую как оригинале, параметр in/out
                    findAiming(joinedNode.get(i), angleLimit);
                }
            }

            //5) delete all collapsing edges
            for (Edge edgeI : Edges) {
                if ((edgeI.mark & Mark.MARK_COLLAPSING) > 0) {
                    edgeI.delEdge();
                }
            }

            //6) Collapse nodes to junctions single nodes
            for (Node nodeJ : Nodes) {
                if (nodeJ.mark >= 0) {
                    int i = nodeJ.mark;
                    for (Edge edgeM : nodeJ.edgeL) {
                        //reconnect edge to centroid
                        if (edgeM.node1 == nodeJ) {
                            edgeM.node1 = joinedNode.get(i);    // TODO: переделать под Node.markNode
                        }
                        else {
                            edgeM.node2 = joinedNode.get(i);
                        }
                        //joinedNode.get(i).edgeL.add(new Edge(edgeM)); // тут был косяк с клонированием
                        joinedNode.get(i).edgeL.add(edgeM);
                        //k = joinedNode.get(i).Edges;
                        //Nodes[joinedNode[i]]..edge(k) = Nodes[j]..edge(m);
                        //Nodes[joinedNode[i]].Edges = k + 1;
                    }
                    //all edges were reconnected
                    nodeJ.edgeL.clear();
                    //kill
                    nodeJ.delNode();
                }
                if ((nodeJ.VBNum & 8191) == 0) {
                    System.out.println("Collapse " + passNumber + ", Del " + nodeJ.VBNum + " / " + Nodes.size());
                }
            }

            if (joinGroups > 0) {
                //unless no junction detected - relaunch algo
                passNumber = passNumber + 1;
                //DoEvents;
        // goto found: GoTo lIteration;
            } else { break; }
        }
    }

    //Check edge for participating in short loop (shorted than MaxDist)
    //Launch two waves for propagation - one from each end of edge
    //if waves collide, they are part of short loop
    //waves are limited by length and MARK_DISTCHECK
    //if no short loop found, this edge marked with MARK_DISTCHECK (means, no short loop passing this edge)
    public static void checkShortLoop2(Edge edge1, double maxDist) {
        Node node1;
        Node node2;
        double dist0, Dist1;

        //wave starts
        node1 = edge1.node1;
        node2 = edge1.node2;
        //half of edge len - start point is center of edge
        dist0 = 0.5 * Node.distance(node1, node2);
        node1.mark = 1;
        node2.mark = -1;
        edge1.mark |= Mark.MARK_WAVEPASSED;
        Chain.clear();  // = new ArrayList<>();
        Chain.add(node1);
        Chain.add(node2);
        node1.temp_dist = dist0;
        node2.temp_dist = dist0;

        //propagate waves
        int j = 0;
        int k, k2;
        Node d = null;
        while (j < Chain.size()) {
            k = Chain.get(j).mark;
            if (k > 0) {
                k2 = k + 1;
            }
            else {
                k2 = k - 1;
            }

            dist0 = Chain.get(j).temp_dist;
            for (Edge q: Chain.get(j).edgeL) {
                //wave already passed this edge
                if ((q.mark & Mark.MARK_WAVEPASSED) != 0) {
        //goto found: GoTo lSkipEdge;
                    continue;
                }
                //no short loop here - no need to pass thru this edge
                if ((q.mark & Mark.MARK_DISTCHECK) != 0) {
        //goto found: GoTo lSkipEdge;
                    continue;
                }
                q.mark |= Mark.MARK_WAVEPASSED;
                d = q.node1;
                if (d == Chain.get(j)) { d = q.node2; }
                if (d.mark != 0) {
                    if ((d.mark < 0 && k > 0) || (d.mark > 0 && k < 0)) {
                        //loop found
                        //update by len of this edge
                        Dist1 = dist0 + Node.distance(d, Chain.get(j));
                        //len of second part of wave
                        Dist1 += d.temp_dist;
                        //loop is too long
                        if (Dist1 > maxDist) { 
            //goto found: GoTo lSkipEdge;
                            continue;
                        }
                        //short loop found
            //goto found: GoTo lShortLoop;
//*TODO:** label found: lShortLoop:;
                        //short loop found
                        //mark final edge
                        q.mark |= Mark.MARK_JUNCTION;

                        //mark both loop half by moving backward from collision edge to start
                        Node.markLoopHalf(d);
                        Node.markLoopHalf(Chain.get(j));

                        //mark start edge
                        edge1.mark |= Mark.MARK_JUNCTION;

            //*TODO:** label found: lClearTemp:;
                        for (Node chainJ: Chain) {
                            chainJ.clearTemp();
                        }
                        return;
                    }
            //goto found: GoTo lSkipEdge;
                    continue;
                }
                //update by len of this edge
                Dist1 = dist0 + Node.distance(d, Chain.get(j));
                //set passed len
                d.temp_dist = Dist1;
                d.mark = k2;
                //add to chain, but only if distance from start is not too long
                if (Dist1 < maxDist) { Chain.add(d); }
//label found: lSkipEdge:;
            }
            j++;
        }
        //short loop not found

        //no short passing this edge
        edge1.mark |= Mark.MARK_DISTCHECK;
    //goto found: GoTo lClearTemp;


//label found: lClearTemp:;
        //clear all temp marks
        for (Node chainJ: Chain) {
            chainJ.clearTemp();
        }
    }

    //Check all edges of node for junction marker and add them into collapsing constuction
    public static void checkForCollapeByChain2(Node node1) {
        boolean borderNode = false;

        for (Edge edge: node1.edgeL) {
            if ((edge.mark & Mark.MARK_JUNCTION) > 0) {
                //edge is marked as junction
                //mark it as collapsing
                edge.mark |= Mark.MARK_COLLAPSING;

                //add other end of edge into collapsing constuction
                if (!Chain.contains(edge.node1)) {
                    //node1 is not in chain
                    Chain.add(edge.node1);
                }
                if (!Chain.contains(edge.node2)) {
                    //node2 is not in chain
                    Chain.add(edge.node2);
                }
        //goto found: GoTo lNext;
            } else {
            //at lease one non-junction edge found
                borderNode = true;
                // TODO: добавил сам
                //break;
                // не понятно зачем добавил, бордер да не меняется, но возможно след отрезки должны попасть в IF
            }
//label found: lNext:;
        }

        //node is border-node
        if (borderNode) { node1.mark = Mark.MARK_NODE_BORDER; }
    }

    //Shrink group of border-nodes to minimum-distance (point or segment or more complex)
    //BorderNum - numbed of border nodes (must be in the start of Chain array)
    //shrink is achived by moving border-nodes along geometry while minimizing sum length
    //all edges, passed by border-node marked as part of junctions
    //MaxShift limit max length allowed to pass by each border-node
    //if two border-nodes reach each other, they joins
    //if reaching 1 border-node is not possible, then near edges are checked for internal-points minimizing sum len
    //this edges also marked as part of junctions (requires than all edges were not very long)
    public static void shrinkBorderNodes(int borderNumI, double maxShift) {
        //current node index of border-node
        ArrayList<Node> borderNodes;
        //distance, covered by border-node while moving on edges
        double[] borderShifts;

        int moving = 0;
        int moved = 0;
        double dist, dist0;
        double dist1 = 0;
        double dist_min;
        Node node_dist_min;
        Edge edge_dist_min;
        //int borderNumI = Chain.indexOf(borderNum);
        //indexes of border-nodes
        //G.redim(borderNodes, borderNum);
        borderNodes = new ArrayList<>(borderNumI);
        //len, passed by border-nodes
        //G.redim(borderShifts, borderNum);
        borderShifts = new double[borderNumI];

        //get border nodes
        for (int i = 0; i < borderNumI; i++) {
            //borderNodes[i] = Chain.get(i);
            borderNodes.add(Chain.get(i));
            //zero len passed
            borderShifts[i] = 0;
        }
        do {
//label found: lRestartCycle:;
            moving = 0;
            moved = 0;

            while (moving < borderNumI) {
    //label found: lRestartMoving:;
                boolean lRestartMovingFlag = false;
                //calc current sum of distances from Moving border-node to all others
                dist0 = 0;
                for (int j = 0; j < borderNumI; j++) {
                    if (j != moving) {
                        dist1 = Node.distance(borderNodes.get(moving), borderNodes.get(j));
                        dist0 = dist0 + dist1;
                    }
                }

                //finding node, where this border-node can move to minimize distance
                //need sum-distance less than current
                dist_min = dist0;
                //not yet found
                node_dist_min = null; edge_dist_min = null;
                for (Edge e: borderNodes.get(moving).edgeL) {
                    if ((e.mark & Mark.MARK_JUNCTION) == 0) {
                        //not junction edge
                        Node p = e.node1;
                        //get len of this edge
                        dist1 = Node.distance(p, e.node2);
                        //moving will exceed MaxShift
                        if (borderShifts[moving] + dist1 > maxShift) {
                //goto found: GoTo lSkipAsLong;
                            continue;
                        }
                        //get other end of edge
                        if (p == borderNodes.get(moving)) { p = e.node2; }

                        //calc new sum of distances from this border-node to all others
                        dist = 0;
                        for (int j = 0; j < borderNumI; j++) {
                            if (j != moving) {
                                dist1 = Node.distance(p, borderNodes.get(j));
                                dist = dist + dist1;
                            }
                        }
                        //minimizing found
                        if (dist < dist_min) { dist_min = dist; node_dist_min = p; edge_dist_min = e; }
//label found: lSkipAsLong:;
                    }
                }

                if (node_dist_min != null) {
                    //found node, more close to other border-nodes
                    //mark for collapse
                    edge_dist_min.mark |= Mark.MARK_COLLAPSING;
                    //add found node to chain of junction nodes
                    if (!Chain.contains(node_dist_min)) { Chain.add(node_dist_min); }
                    //at least 1 border-node moved
                    moved = 1;

                    //update passed distance
                    borderShifts[moving] += Node.distance(edge_dist_min.node1, edge_dist_min.node2);

                    for (int i = 0; i < borderNumI; i++) {
                        if (node_dist_min == borderNodes.get(i)) {
                            //after moving border-node joined with another one - remove
                            if (borderShifts[i] < borderShifts[moving]) {
                                //joined with node with smaller moves - keep smallest
                                borderShifts[i] = borderShifts[moving];
                            }

                            //join
                            borderNodes.set(moving, borderNodes.get(borderNumI - 1));
                            borderShifts[moving] = borderShifts[borderNumI - 1];
                            borderNumI--;
                            //only 1 border-node left
                            if (borderNumI == 1) {
                    //goto found: GoTo lReachOne;
                                return;
                            }
                            //back to moving this node
                //goto found: GoTo lRestartMoving;
                            lRestartMovingFlag = true;
                            break;
                        }
                    }

                    //border-node not joined, just moved
                    if (!lRestartMovingFlag) {
                        borderNodes.set(moving, node_dist_min);
                        lRestartMovingFlag = true;
                    }
            //goto found: GoTo lRestartMoving;
                }
                //not found - proceeding to next node

                if (!lRestartMovingFlag) { moving++; }
            }

            //some border-nodes were moved, repeat cycle
//            if (moved == 1) {
//        //goto found: GoTo lRestartCycle;
//            }
        } while (moved == 1);
        //no border-nodes moved during whole cycle - looks like shrinking reached minimum

        if (borderNumI > 1) {
            //2 or more border nodes remains

            //all border-nodes
            for (int i = 0; i < borderNumI; i++) {
                //all edges of it
                for (Edge e :borderNodes.get(i).edgeL) {
                    if ((e.mark & Mark.MARK_JUNCTION) == 0) {
                        //not junction edge -> edge of main road
                        for (int j = 0; j < borderNumI; j++) {
                            //not the same border-node
                            if (j != i) {
                                dist = Node.distanceToSegment(e.node1, e.node2, borderNodes.get(j));
                                if (Node.DistanceToSegment_last_case == 3) {    //костыль, т.к. статичная в классе, а надо как возврат значения
                                    //3rd case distance - interval internal point is closer to border-node j than both ends of interval
                                    //-> mark edge and both nodes for collapsing
                                    //mark for definite collapse
                                    e.mark |= Mark.MARK_COLLAPSING;
                                    if (!Chain.contains(e.node1)) { Chain.add(e.node1); }
                                    if (!Chain.contains(e.node2)) { Chain.add(e.node2); }
                                }
                            }
                        }
                    }
                }
            }
        }

//*TODO:** label found: lReachOne:;
        //one border-node reach - end
    }

//Check all edges of node for collapsing marker and add their other ends into chain
    public static void groupCollapse(Node node1) {

        for (Edge edge: node1.edgeL) {
            if ((edge.mark & Mark.MARK_COLLAPSING) > 0) {
                Node k = edge.node1;
                //get other end of edge
                if (k == node1) { k = edge.node2; }

                //if other end is marked as node-of-junction -> add it to current chain
                if ((k.mark == Mark.MARK_NODE_OF_JUNCTION) && !Chain.contains(k)) { Chain.add(k); }
            }
        }
    }

//Find centroid of junction by aiming-edges
    //Will find location, equally distant from all lines (defined by AimEdges)
    //Iterative search by 5 points, combines moving into direction of minimizing sum of squares of distances
    //and bisection method to clarify centroid position
    public static void findAiming(Node node, double angleLimit) { // TODO: Use of ByRef founded
        double px = 0;
        double py = 0;
        double dx = 0;
        double dy = 0;
        double q = 0;
        double v, v1, v2, v3, v4;
        double dvx, dvy;
        double estStepX = 0;
        double estStepY = 0;
        //Dim phase As Long
        double maxAngle = 0;

        px = 0;
        py = 0;
        //Node result = new Node(node);

        if (AimEdges.isEmpty()) { return; }

        //calculate equation elements of all aimedges
        //equation: Distance of (x,y) = a * x + b * y + c
        //q[i] = 1 / sqrt(((y2[i]-y1[i])^2+(x2[i]-x1[i])^2)
        //a[i] = (y2[i]-y1[i])*q[i]
        //b[i] = (x1[i]-x2[i])*q[i]
        //c[i] = (y1[i]*x2[i]-x1[i]*y2[i])*q[i]
        //also calc default centroid as average of all aimedges lat1-lot1 coords

        for (AimEdge AimI: AimEdges) {
            //TODO: fix (not safe to 180/-180 edge)
            px = px + AimI.lat1;
            py = py + AimI.lon1;
            dx = AimI.lat2 - AimI.lat1;
            dy = AimI.lon2 - AimI.lon1;
            q = Math.sqrt(dx * dx + dy * dy);
            AimI.d = q;
            if (q != 0) {
                q = 1 / q;
            }
            //a and b is normalized normal to edge
            AimI.a = dy * q;
            AimI.b = -dx * q;
            AimI.c = (AimI.lon1 * AimI.lat2 - AimI.lon2 * AimI.lat1) * q;
        }
        px = px / AimEdges.size();
        py = py / AimEdges.size();

        //check max angle between aimedges
        //angle is checked in lat/lon grid, so not exactly angle in real world
        maxAngle = 0;
        for (int i = 1; i < AimEdges.size(); i++) {
            for (int j = 0; j < i; j++) {
                //vector product of normals, result is sin(angle)
                //angle is smallest of (a,180-a)
                q = Math.abs(AimEdges.get(i).a * AimEdges.get(j).b - AimEdges.get(i).b * AimEdges.get(j).a);
                if (q > maxAngle) { maxAngle = q; }
            }
        }

        //angle is too small, iterative aiming will make big error along roads and should not be used
        if (maxAngle < angleLimit) {
            //goto found: GoTo lResult;
            node.lat = px;
            node.lon = py;
            return;
        }


        //OK, angle is good => lets start iterative search

        //initial steps
        estStepX = 0.0001;
        estStepY = 0.0001;
        int t = 0;
        //px and py is start location

        do {
//label found: lNextStep:;
            t++;
            v = 0;
            v1 = 0;
            v2 = 0;
            v3 = 0;
            v4 = 0;

            //calc distance in 5 points - current guess and 4 points on ends of "+"-cross
            for (AimEdge AimI: AimEdges) {
                //sum of module distances, not good
                //        v = v + Abs(px * AimEdges(i).a + py * AimEdges(i).b + AimEdges(i).c)
                //        v1 = v1 + Abs((px + EstStepX) * AimEdges(i).a + py * AimEdges(i).b + AimEdges(i).c)
                //        v2 = v2 + Abs((px - EstStepX) * AimEdges(i).a + py * AimEdges(i).b + AimEdges(i).c)
                //        v3 = v3 + Abs(px * AimEdges(i).a + (py + EstStepY) * AimEdges(i).b + AimEdges(i).c)
                //        v4 = v4 + Abs(px * AimEdges(i).a + (py - EstStepY) * AimEdges(i).b + AimEdges(i).c)

                //sum of square distances - better
                v = v + Math.pow(px * AimI.a + py * AimI.b + AimI.c, 2);
                v1 = v1 + Math.pow((px + estStepX) * AimI.a + py * AimI.b + AimI.c, 2);
                v2 = v2 + Math.pow((px - estStepX) * AimI.a + py * AimI.b + AimI.c, 2);
                v3 = v3 + Math.pow(px * AimI.a + (py + estStepY) * AimI.b + AimI.c, 2);
                v4 = v4 + Math.pow(px * AimI.a + (py - estStepY) * AimI.b + AimI.c, 2);
            }

            if (v > v1 || v > v2 || v > v3 || v > v4) {
                //v is not smallest => centroid location is not in covered by our cross (px+-EstStepX,py+-EstStepY)
                //=> we need to shift
                if (v > v1 || v > v2) {
                    //shift by X (by half of quad) in direction to minimize v
                    if (v1 < v2) { px += estStepX * 0.5; } else { px -= estStepX * 0.5; }
                }
                if (v > v3 || v > v4) {
                    //shift by Y (by half of quad) in direction to minimize v
                    if (v3 < v4) { py += estStepY * 0.5; } else { py -= estStepY * 0.5; }
                }
        //goto found: GoTo lNextStep;
                continue;
            }
            else {
                //v is smallest => centroid location IS covered by our cross (px+-EstStepX,py+-EstStepY)
                //we need to select sub-rectangle to clarify position

                //find q as max of v1-v4
                q = v1;
                int i = 1;
                if (v2 > q) { q = v2; i = 2; }
                if (v3 > q) { q = v3; i = 3; }
                if (v4 > q) { q = v4; i = 4; }
                switch (i) {
                    case  4:
                        //v4 is max, select half with v3
                        py = py + estStepY * 0.5;
                        estStepY = estStepY * 0.5;
                        break;
                    case  3:
                        //v3 is max, select half with v4
                        py = py - estStepY * 0.5;
                        estStepY = estStepY * 0.5;
                        break;
                    case  2:
                        //v2 is max, select half with v1
                        px = px + estStepX * 0.5;
                        estStepX = estStepX * 0.5;
                        break;
                    case  1:
                        //v1 is max, select half with v2
                        px = px - estStepX * 0.5;
                        estStepX = estStepX * 0.5;
                        break;
                }

                //if required accuracy not yet reached - continue
                //exit if 100k iteration does not help
                //if (t < 100000 && estStepX > 0.0000001 || estStepY > 0.0000001) { //*TODO:** goto found: GoTo lNextStep; }
            }
        //if required accuracy not yet reached - continue
        //exit if 100k iteration does not help
        } while (t < 100000 && estStepX > 0.0000001 || estStepY > 0.0000001);

        //OK, found

//label found: lResult:;
        node.lat = px;
        node.lon = py;
    }
    
    public static void checkIntegrity() {
        System.out.println("=============== checkIntegrity ===============");
        int count = 0;
        for (Edge edgeTest: Edges) {
            if (count == 223) {
                count = 223;
            }
            if (edgeTest.validLinks() < 0) {
                System.out.printf("%d: %d, %d\n", count, edgeTest.node1.VBNum, edgeTest.node2.VBNum);
            }
            count++;
        }
        System.out.println("==================== END =====================");
    }

    //Find and optimize all chains by Douglas-Peucker with Epsilon (in metres)
    public static void douglasPeucker_total(double epsilon) {
        for (Node nodeI: Nodes) {
            //mark all nodes as not passed
            nodeI.mark = 0;
        }

        for (Node nodeI: Nodes) {
            if (nodeI.nodeID == Mark.MARK_NODEID_DELETED || nodeI.edgeL.size() != 2
                    || nodeI.mark == 1) {
                //goto found: GoTo lSkip;
                continue;
            }
            //node: not deleted, not yet passed and with 2 edges -> should be checked for chain
            douglasPeucker_chain(nodeI, epsilon);
//label found: lSkip:;
            if ((nodeI.VBNum & 8191) == 0) {
                //show progress
                System.out.println("Doug-Pek " + nodeI.VBNum + " / " + Nodes.size());
            }
        }
    }

    //Find one chain (starting from node1) and optimize it by Douglas-Peucker with Epsilon (in metres)
    public static void douglasPeucker_chain(Node node1, double epsilon) {
        Edge refEdge;
        int chainEnd = 0;

        Edge nextChainEdge = null;
        chainEnd = 0;

        //Algorithm go from specified node into one direction by chain of nodes
        //(nodes connected one by one, without junctions) until end (or junction) is reached
        //After that algorithm will go from final edge into opposite direction and will compare edges
        //and add nodes into Chain array
        //On findind different edge (or reaching other end of chain) algorithm will pass found (sub)chain
        //into OptimizeByDouglasPeucker_One recursive function for optimization
        //Then rest of chain (if it exits) will be processed in similar way

        //1) go by chain to the one end - to node with !=2 edges

        //start node
        Node nodeI = node1;
        Node nodeJ = node1;
        Node nodeK = null;

        while (true) {
//label found: lGoNext:;
            //go by chain
            nodeK = goByChain(nodeI, nodeJ);
            //if still 2 edges - proceed
            if (nodeK != node1  && nodeK.edgeL.size() == 2) {
                nodeJ = nodeI;
                nodeI = nodeK;
                //GoTo lGoNext;
                continue;
            }
            break;
        }

        //   *-----*-----*-----*---...
        //   k     i     j

        //OK, we found end of chain
        nodeJ = nodeK;

        //   *---------*-----*-----*---...
        //  k=j        i

        //2) go revert - from found end to another one and saving all nodes into Chain() array

        //ChainNum = 0;
        //addChain(k);
        //addChain(i);
        Chain.clear();  // = new ArrayList<>();
        Chain.add(nodeK);
        Chain.add(nodeI);

        //keep info about first edge in chain
        // TODO: по идее как и внизу объект клонируется
        refEdge = new Edge(GoByChain_lastedge);
        //reversed oneway
        if (refEdge.node1 != Chain.get(0) && refEdge.oneway == 1) { refEdge.oneway = 2; }

        while(true) {
            while(true) {
    //*TODO:** label found: lGoNext2:;

                nodeK = goByChain(nodeI, nodeJ);

                //   *-------------*-----*-----*---...
                //  j              i     k

                //check oneway
                int m = GoByChain_lastedge.oneway;
                if (m > 0 && GoByChain_lastedge.node1 != nodeI) { m = 2; }

                //if oneway flag is differnt or road type is changed - break chain
                if (m != refEdge.oneway) {
                    nextChainEdge = GoByChain_lastedge;
                    //GoTo lBreak;
                    break;
                }

                if (GoByChain_lastedge.roadtype != refEdge.roadtype) {
                    nextChainEdge = GoByChain_lastedge;
                    //GoTo lBreak;
                    break;
                }

                Chain.add(nodeK);

                if (nodeK != Chain.get(0) && nodeK.edgeL.size() == 2) {
                    //still 2 edges - still chain
                    nodeK.mark = 1;
                    nodeJ = nodeI;
                    nodeI = nodeK;
                    //goto found: GoTo lGoNext2;
                    continue;
                }

                chainEnd = 1;
                break;
            }
//label found: lBreak:;

            //3) optimize found chain by D-P
            optimizeByDouglasPeucker_One(Chain.get(0), Chain.get(Chain.size() - 1), epsilon, refEdge);

            if (chainEnd == 0) {
                //continue with this chain, as it is not ended

                //   *================*--------------------*-----------*-----*---...
                //                        NextChainEdge

                //new reference info (ссылка на новый объект - клон)
                refEdge = new Edge(nextChainEdge);
                if (refEdge.node1 == Chain.get(Chain.size() -1)) {
                    nodeI = refEdge.node2;
                    nodeJ = refEdge.node1;
                } else {
                    if (refEdge.oneway == 1) { refEdge.oneway = 2; }
                    nodeI = refEdge.node1;
                    nodeJ = refEdge.node2;
                }

                //   *================*--------------------*-----------*-----*---...
                //                    j                    i

                //chain from one edge - nothing to optimize by D-P
                if (nodeI.edgeL.size() != 2) { return; }

                //add both nodes of last edge
                Chain.clear();  // = new ArrayList<>();
                Chain.add(nodeJ);
                Chain.add(nodeI);

                nextChainEdge = null;
                //continue with chain
                //goto found: GoTo lGoNext2;
                continue;
            }
            break;
        }
    }

    //Recursive check to optimize chain/subchain by Douglas-Peucker with Epsilon (in metres)
    //subchain is defined by IndexStart,IndexLast
    //refedge - road parameters of chain (for create new edge in case of optimization)
    //(180/-180 safe)
    private static void optimizeByDouglasPeucker_One(Node nodeStart, Node nodeLast, double epsilon, Edge refEdge) {
        int i = 0;
        Node farestIndex;
        double farestDist = 0;
        double dist = 0;
        double k = 0;
        //double scalarMult = 0;
        int newspeed = 0;
        String newlabel;

        int indexStart = Chain.indexOf(nodeStart);
        int indexLast = Chain.indexOf(nodeLast);

        //one edge (or less) -> nothing to do
        if (((indexStart + 1) >= indexLast)) { return; }

        //distance between subchain edge
        k = Node.distance(nodeStart, nodeLast);

        //find node, farest from line first-last node (farer than Epsilon)
        //start max len - Epsilon
        farestDist = epsilon;
        //nothing yet found
        farestIndex = null;
        for (i = indexStart + 1; i < indexLast; i++) {
            if (k == 0) {
                //circled subchain
                dist = Node.distance(Chain.get(i), nodeStart);
            } else {
                dist = Node.distanceToSegment(nodeStart, nodeLast, Chain.get(i));
            }
            if (dist > farestDist) {
                farestDist = dist;
                farestIndex = Chain.get(i);
            }
        }

        if (farestIndex == null) {
            //farest node not found -> all distances less than Epsilon -> remove all internal nodes

            //calc speed and label from all subchain edges
            estimateChain(indexStart, indexLast);
            newspeed = EstimateChain_speed;
            newlabel = EstimateChain_label;

            for (i = indexStart + 1; i < indexLast; i++) {
                //kill all nodes with edges
                Chain.get(i).delNode();
            }
            
            Edge edgeI;
            //join first and last nodes by new edge
            if (refEdge.oneway == 2) {
                //reversed oneway
                edgeI = joinByEdge(nodeLast, nodeStart);
                edgeI.oneway = 1;
            } else {
                edgeI = joinByEdge(nodeStart, nodeLast);
                edgeI.oneway = refEdge.oneway;
            }
            edgeI.roadtype = refEdge.roadtype;
            edgeI.speed = (byte)newspeed;
            edgeI.label = newlabel;

            return;
        }

        //farest point found - keep it
        //call Douglas-Peucker for two new subchains
        optimizeByDouglasPeucker_One(nodeStart, farestIndex, epsilon, refEdge);
        optimizeByDouglasPeucker_One(farestIndex, nodeLast, epsilon, refEdge);
    }

    //Join edges with very acute angle into one
    //1) distance between edges ends < JoinDistance
    //2) angle between edges lesser than limit
    //AcuteKoeff: 1/tan() of limit angle  (3 =>18.4 degrees)
    public static void joinAcute(double joinDistance, double acuteKoeff) {
        int q = 0;
        double dist = 0;
        int merged = 0;
        int passNumber = 0;

        for (Node nodeI: Nodes) {
            if (nodeI.nodeID != Mark.MARK_NODEID_DELETED  && nodeI.edgeL.size() > 1) {
                //mark to check, not deleted with 2+ edges
                nodeI.mark = 1;
            }
            else {
                //mark to skip
                nodeI.mark = 0;
            }
        }

        passNumber = 1;

//*TODO:** label found: lIteration:;
        while (true) {
            merged = 0;
            int j = 0;
            for (Node nodeI: Nodes) {

                if (nodeI.mark == 1) {

                    //check for edges connecting same nodes several times
                    //made by filling Chain array with other ends of edges
                    Chain.clear();
                    j = 0;
                    while (j < nodeI.edgeL.size()) {
                        Edge edgeJ = nodeI.edgeL.get(j);
                        Node nodeK = edgeJ.node1;
                        //get other end
                        if (nodeK == nodeI) { nodeK = edgeJ.node2; }
                        if (Chain.indexOf(nodeK) == -1) {
                            //first occurence in chain
                            Chain.add(nodeK);
                        }
                        else {
                            //not first - should join
                            Edge edgeM = Edge.getEdgeBetween(nodeI, nodeK);
                            //combining succeed, we should check j-th edge once again
                            if (Edge.combineEdges(edgeM, edgeJ, nodeI)) {
                //goto found: GoTo lAgain;
                                continue;
                            }
                        }
                        j++;
    //label found: lAgain:;
                    }

                    //node is processed, mark to skip
                    nodeI.mark = 0;

                    for (Node nodeJ: Chain) {
                        //skip removed nodes
                        if (nodeJ == null) {
                //goto found: GoTo lSkipJ;
                            continue;
                        }
                        //skip deleted nodes
                        if (nodeJ.nodeID == Mark.MARK_NODEID_DELETED) {
                //goto found: GoTo lSkipJ;
                            continue;
                        }
                        for (Node nodeK: Chain) {
                            //skip same and removed nodes
                            if (nodeJ == nodeK  || nodeK == null) {
                //goto found: GoTo lSkipK;
                                continue;
                            }
                            //skip deleted nodes
                            if (nodeK.nodeID == Mark.MARK_NODEID_DELETED) {
                //goto found: GoTo lSkipK;
                                continue;
                            }
                            //distance from Chain(k) to interval i-Chain(j)
                            dist = Node.distanceToSegment(nodeI, nodeJ, nodeK);
                            if (dist < joinDistance) {
                                //node Chain(k) is close to edge i->Chain(j)
                                if (Node.distance(nodeJ, nodeK) < joinDistance) {
                                    //Chain(k) is close to Chain(j), they should be combined
                                    //edge i-Chain(j)
                                    Edge edgeM = Edge.getEdgeBetween(nodeI, nodeJ);
                                    //edge i-Chain(k)
                                    Edge edgeN = Edge.getEdgeBetween(nodeI, nodeK);

                                    if (edgeM != null && edgeN != null) {
    //label found: lCheckEdge:;
                                        while(true) {
                                            //remove any edges from Chain(j) to Chain(k)
                                            Edge edgeP = Edge.getEdgeBetween(nodeJ, nodeK);
                                            if (edgeP != null) {
                                                edgeP.delEdge();
                            //goto found: GoTo lCheckEdge;
                                                continue;
                                            }
                                            break;
                                        }

                                        //at least one change made
                                        merged++;
                                        //mark node to check again
                                        nodeI.mark = 1;

                                        q = Highway.compareRoadtype(edgeM.roadtype, edgeN.roadtype);
                                        if (q == -1) {
                                            //edge n have higher priority
                                            //combine edge m into n
                                            Edge.combineEdges(edgeN, edgeM, nodeI);
                                            //combine node Chain(j) into Chain(k) w/o moving Chain(k)
                                            Node.mergeNodes(nodeK, nodeJ, true);
                                            //mark node to check once again
                                            nodeK.mark = 1;
                                            //remove Chain(j) from chain
                                            //Chain[j] = -1;
                                            Chain.set(Chain.indexOf(nodeJ), null);
                                            //proceed to next j
                        //goto found: GoTo lSkipJ;
                                            break;
                                        } else {
                                            //edge m have higher priority or edges are equal
                                            //combine edge n into m
                                            Edge.combineEdges(edgeM, edgeN, nodeI);
                                            if (q == 0) {
                                                //edges are equal
                                                //combine with averaging coordinates
                                                Node.mergeNodes(nodeJ, nodeK, false);
                                            } else {
                                                //edge m have higher priority
                                                //combine w/o moving Chain(j)
                                                Node.mergeNodes(nodeJ, nodeK, true);
                                            }
                                            //mark node to check once again
                                            nodeJ.mark = 1;
                                            //remove Chain(k) from chain
                                            //Chain[k] = -1;
                                            Chain.set(Chain.indexOf(nodeK), null);
                                            //proceed to next k
                        //goto found: GoTo lSkipK;
                                            continue;
                                        }
                                    }
                                }
                                else if (Node.distance(nodeI, nodeK) > dist * acuteKoeff) {
                                    //distance from i to chain(k) is higher than distance from Chain(k) to interval i-Chain(j) in AcuteKoeff times
                                    //=> angle Chain(k)-i-Chain(j) < limit angle
                                    //=>
                                    //Chain(k) should be inserted into edge i-Chain(j)
                                    //edge i-Chain(j) became Chain(k)-Chain(j) and keeps all params
                                    //edge i-Chain(k) became joined by params
                                    //edge i-Chain(j) - long edge
                                    Edge edgeM = Edge.getEdgeBetween(nodeI, nodeJ);
                                    //edge i-Chain(k) - short edge
                                    Edge edgeN = Edge.getEdgeBetween(nodeI, nodeK);

                                    if (edgeM != null && edgeN != null) {
                                        if (Highway.compareRoadtype(edgeM.roadtype, edgeN.roadtype) == -1) {
                                            //edge n have higher priority
                                        }
                                        else {
                                            //edge m have higher priority or equal
                                            //move Chain(k) to line i-Chain(j)
                                            nodeK.projectNode(nodeI, nodeJ);
                                        }
                                        //combine params from m into n
                                        Edge.combineEdgeParams(edgeN, edgeM, nodeI);
                                        //edge m is now Chain(k)-Chain(j)
                                        Edge.reconnectEdge(edgeM, nodeI, nodeK);
                                        //mark nodes as needed to check once again
                                        nodeJ.mark = 1;
                                        nodeK.mark = 1;
                                        nodeI.mark = 1;
                                        //at least one change made
                                        merged = merged + 1;
                                        //remove Chain(j) from chain, as it is not connected to node i
                                        //Chain[j] = -1;
                                        Chain.set(Chain.indexOf(nodeJ), null);
                                        //proceed to next j
                            //goto found: GoTo lSkipJ;
                                        break;
                                    }
                                }
                            }
    //label found: lSkipK:;
                        }
    //label found: lSkipJ:;
                    }
                }

                if ((nodeI.VBNum & 8191) == 0) {
                    //show progress
                    System.out.println("JA #" + passNumber + " : " + nodeI.VBNum + " / " + Nodes.size());
                }
            }

            if (merged > 0) {
                //at least one change made - relaunch algorithm
                passNumber++;
                //show progress
                //Form1.Caption = "JoinAcute " + CStr(passNumber) + ": " + CStr(merged);
                System.out.println("JoinAcute " + passNumber + ": " + merged);
        //*TODO:** goto found: GoTo lIteration;
                continue;
            }
            break;
        }
    }

    // Collapse all edges shorter than CollapseDistance (also will kill void edges)
    // Will collapse edges one by one, so should be called somewhere in the end of optimization
    public static void CollapseShortEdges(double collapseDistance) {
        int somedeleted, i;
        double edgeLen;

    //lIteration:
        do {
            i = somedeleted = 0;
            for(Edge edgeI: Edges) {
                if (edgeI.node1 != null) {
                    edgeLen = Node.distance(edgeI.node1, edgeI.node2);
                    if (edgeLen < collapseDistance) {
                        Node nodeJ = edgeI.node1;
                        Node nodeK = edgeI.node2;
                        edgeI.delEdge();    //del this edge
                        if (nodeJ !=  nodeK) {
                            Node.mergeNodes(nodeJ, nodeK, false); //merge nodes, only if they are different
                        }
                        somedeleted = 1;
                    }
                }
                if ((i++ & 8191) == 0) {
                    //show progress
                    System.out.println("CSE " + i + " / " + Edges.size());
                }
            }
        }
        while(somedeleted > 0);
    }
}
