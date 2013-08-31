/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.zkir.mp2mp.taskextsimp;

import java.util.ArrayList;

/**
 *
 * @author freeExec
 */
public class Clusters {

// Consts
    //Cluster size in degrees for ClusterIndex build and search
    public static final double CLUSTER_SIZE = 0.05;

// Fields
    public static ArrayList<Node> extLinkNodes;
    //min lat-border of clusters
    public static double ClustersLat0 = 0;
    //min lon-border of clusters
    public static double ClustersLon0 = 0;
    //index of first node of cluster (X*Y)
    public static int[] ClustersFirst;
    //chain of nodes (NodesNum)
    public static int[] ClustersChain;
    //num of cluster by lat = X
    public static int ClustersLatNum = 0;
    //num of cluster by lon = Y
    public static int ClustersLonNum = 0;
    //num of indexed nodes (for continuing BuildNodeClusterIndex)
    public static int ClustersIndexedNodes = 0;
    //index of last node of cluster - for building index (X*Y)
    public static int[] ClustersLast;
    //last bbox
    public static Bbox ClustersFindLastBbox;
    //last index of cluster
    public static int ClustersFindLastCluster = 0;

    public static int ClustersFindLastNode = 0;

    public static void init(ArrayList<Node> nodes) {
        extLinkNodes = nodes;
    }


    //Build cluster index
    //Cluster index allow to quickly find nodes in specific bbox
    //Cluster index is collections of nodes chains, where starts can be selected from coordinates
    //and continuation - by indexes in chains
    //Flags: 1 - only update from ClustersIndexedNodes to NodesNum (0 - full re/build)
    public static void buildNodeClusterIndex(boolean flags) {
        int i, j, k;
        int x;
        int y;

        if (flags) {
            //Only update
            //TODO(?): remove chain from deleted nodes
            //G.redimPreserve(ClustersChain, NodesNum);
            //TODO: хз, что тут делать, пока
            int[] newClustersChain = new int[extLinkNodes.size()];
            System.arraycopy(ClustersChain, 0, newClustersChain, 0, ClustersChain.length);
            ClustersChain = newClustersChain;//new int[Nodes.size()];

            //*TODO:** goto found: GoTo lClustering;
        } else {

            //calc overall bbox
            Bbox wholeBbox = new Bbox();
            wholeBbox.lat_max = -360;
            wholeBbox.lat_min = 360;
            wholeBbox.lon_max = -360;
            wholeBbox.lon_min = 360;
            for (Node iNode: extLinkNodes) {
                //skip deleted nodes
                if (iNode.nodeID != Mark.MARK_NODEID_DELETED) {
                    if (iNode.lat < wholeBbox.lat_min) { wholeBbox.lat_min = iNode.lat; }
                    if (iNode.lat > wholeBbox.lat_max) { wholeBbox.lat_max = iNode.lat; }
                    if (iNode.lon < wholeBbox.lon_min) { wholeBbox.lon_min = iNode.lon; }
                    if (iNode.lon > wholeBbox.lon_max) { wholeBbox.lon_max = iNode.lon; }
                }
            }

            ClustersIndexedNodes = 0;
            //no nodes at all or something wrong
            if (wholeBbox.lat_max < wholeBbox.lat_min || wholeBbox.lon_max < wholeBbox.lon_min) { return; }

            //calc number of clusters
            ClustersLatNum = (int)Math.round(1 + (wholeBbox.lat_max - wholeBbox.lat_min) / CLUSTER_SIZE);
            ClustersLonNum = (int)Math.round(1 + (wholeBbox.lon_max - wholeBbox.lon_min) / CLUSTER_SIZE);

            /*
            //starts of chains
            G.redim(ClustersFirst, ClustersLatNum * ClustersLonNum);
            //ends of chains (for updating)
            G.redim(ClustersLast, ClustersLatNum * ClustersLonNum);
            //whole chain
            G.redim(ClustersChain, NodesNum);
            */
            ClustersChain = new int[extLinkNodes.size()];
            ClustersFirst = new int[ClustersLatNum * ClustersLonNum];
            ClustersLast = new int[ClustersLatNum * ClustersLonNum];

            //edge of overall bbox
            ClustersLat0 = wholeBbox.lat_min;
            ClustersLon0 = wholeBbox.lon_min;

            for (i = 0; i < ClustersLatNum * ClustersLonNum; i++) {
                //'no nodes in cluster yet
                ClustersFirst[i] = -1;
                ClustersLast[i] = -1;
            }

            ClustersIndexedNodes = 0;
        }
//*TODO:** label found: lClustering:;
        for (i = ClustersIndexedNodes; i < extLinkNodes.size(); i++) {
            Node iNode = extLinkNodes.get(i);
            if (iNode.nodeID != Mark.MARK_NODEID_DELETED) {
                //get cluster from lat/lon
                x = (int)Math.round((iNode.lat - ClustersLat0) / CLUSTER_SIZE);
                y = (int)Math.round((iNode.lon - ClustersLon0) / CLUSTER_SIZE);
                j = x + y * ClustersLatNum;

                k = ClustersLast[j];
                if (k == -1) {
                    //first index in chain of this cluster
                    ClustersFirst[j] = i;
                }
                else {
                    //continuing chain
                    ClustersChain[k] = i;
                }
                //this is last node in chain
                ClustersChain[i] = -1;
                ClustersLast[j] = i;
            }
        }

        //last node in cluster index
        ClustersIndexedNodes = extLinkNodes.size();
    }

    //Find node in bbox by using cluster index
    //Flags: 1 - next (0 - first)
    // TODO: перелопачена здорого, могут быть БАГИ
    public static Node getNodeInBboxByCluster(Bbox box1, boolean flags) {
        int j, k = 0;
        int x, y;
        int x1, x2, y1, y2;

        boolean endChain = true;

        if (!flags) {
            //first node needed

            //get coordinates of all needed clusters
            x1 = (int)Math.round((box1.lat_min - ClustersLat0) / CLUSTER_SIZE);
            x2 = (int)Math.round((box1.lat_max - ClustersLat0) / CLUSTER_SIZE);
            y1 = (int)Math.round((box1.lon_min - ClustersLon0) / CLUSTER_SIZE);
            y2 = (int)Math.round((box1.lon_max - ClustersLon0) / CLUSTER_SIZE);

            //store bbox for next searches
            ClustersFindLastBbox = box1;
            x = x1;
            y = y1;
            x--;
        } else {
            if (ClustersFindLastNode == -1) {
                //Last time nothing found - nothing to do further
                return null;
            }

            //get coordinates of all needed clusters
            x1 = (int)Math.round((ClustersFindLastBbox.lat_min - ClustersLat0) / CLUSTER_SIZE);
            x2 = (int)Math.round((ClustersFindLastBbox.lat_max - ClustersLat0) / CLUSTER_SIZE);
            //y1 = (int)Math.round((ClustersFindLastBbox.lon_min - ClustersLon0) / CLUSTER_SIZE);
            y2 = (int)Math.round((ClustersFindLastBbox.lon_max - ClustersLon0) / CLUSTER_SIZE);

            //get coordinates of last used cluster
            x = ClustersFindLastCluster % ClustersLatNum;   // Mod
            y = (int)((ClustersFindLastCluster - x) / ClustersLatNum);  // было целое деление или как-то так "\"

            k = ClustersChain[ClustersFindLastNode];
            if (k == -1) {
    //            x++;
                endChain = true;
            } else { endChain = false; }
        }

        //proceed to next cluster
        while(true) {
            if (endChain) {
                x ++;
                //next line of cluster
                if (x > x2) {
                    y ++;
                    x = x1;
                }
                if (y > y2) {
                    //last cluster - no nodes
                    //nothing found
                    //nothing will be found next time
                    ClustersFindLastNode = -1;
                    ClustersFindLastCluster = -1;
                    break;
                }

                //get first node of cluster

                j = x + y * ClustersLatNum;
                k = ClustersFirst[j];
                //no first node - skip cluster
                if (k == -1) {
                    //*TODO:** goto found: GoTo lNextCluster;
                    continue;
                }
                ClustersFindLastCluster = j;
            }
            //get node from chain
            //k = ClustersChain[ClustersFindLastNode];
            // TODO: не уверен что так можно
            do { // ((k = ClustersChain[ClustersFindLastNode]) != -1) {
            //if (k != -1) {

                //not end of chain
//*TODO:** label found: lCheckNode:;
                //keep as last result
                ClustersFindLastNode = k;
                Node testNode = extLinkNodes.get(k);
                //deleted node - find next
                if (testNode.nodeID == Node.MARK_NODEID_DELETED) {
                //*TODO:** goto found: GoTo lNextNode;
                    continue;
                }
                //node outside desired bbox - find next
                if (testNode.lat < ClustersFindLastBbox.lat_min || testNode.lat > ClustersFindLastBbox.lat_max) {
                    //*TODO:** goto found: GoTo lNextNode;
                    continue;
                }
                if (testNode.lon < ClustersFindLastBbox.lon_min || testNode.lon > ClustersFindLastBbox.lon_max) {
                    //*TODO:** goto found: GoTo lNextNode;
                    continue;
                }
                //OK, found
                //*TODO:** goto found: GoTo lExit;
                return testNode;
            } while ((k = ClustersChain[ClustersFindLastNode]) != -1);
            //end of chain -> last node in cluster
            endChain = true;
        }
        return null;
    }
}
