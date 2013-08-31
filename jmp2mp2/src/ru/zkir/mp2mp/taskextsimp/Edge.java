/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.zkir.mp2mp.taskextsimp;

/**
 *
 * @author freeExec
 */
public class Edge {
    //public int node1;      //first node (index in Nodes array)
    //public int node2;      //second node
    public Node node1;
    public Node node2;
    public byte roadtype;   //roadtype, see HIGHWAY_ consts
    public byte oneway;     //0 - no, 1 - yes ( goes from node1 to node2 )
    public int mark;    //internal marker for all-network algo-s
    public byte speed;      //speed class (in .mp terms)
    public String label;    //label of road (only ref= values currently, not name= )    

    public Edge() {
    }

    public Edge(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public Edge(Edge edge) {
        this.node1 = edge.node1;
        this.node2 = edge.node2;
        this.roadtype = edge.roadtype;
        this.oneway = edge.oneway;
        this.mark = edge.mark;
        this.speed = edge.speed;
        this.label = edge.label;
    }

    public String toString() {
        return String.format("[Num1=%d, Num2=%d]", this.node1.VBNum, this.node2.VBNum);
    }

    //Delete edge and remove all references to it from both nodes
    public void delEdge() { //Node node1) {
        
        //find this edge among edges of node1
        //edge already deleted        
        if (this.node1 == null) { return; }
        this.node1.edgeL.remove(this);    // вроде как особого влияния на результат не оказывает.
        /*int index = this.node1.edgeL.indexOf(this);
        int lastEl = this.node1.edgeL.size() - 1;
        if (index != -1) {
            if (index != lastEl) { this.node1.edgeL.set(index, this.node1.edgeL.get(lastEl)); }
            this.node1.edgeL.remove(lastEl);
        }*/   // в релизе лучше закоментить
        this.node1 = null;
        this.node2.edgeL.remove(this);
        /*index = this.node2.edgeL.indexOf(this);
        lastEl = this.node2.edgeL.size() - 1;
        if (index != -1) {
            if (index != lastEl) { this.node2.edgeL.set(index, this.node2.edgeL.get(lastEl)); }
            this.node2.edgeL.remove(lastEl);
        }*/
        this.node2 = null;
    }

        //Get bounding box of edge
    public static Bbox getEdgeBbox(Node node1, Node node2) {
        Bbox result = new Bbox();
        result.lat_min = node1.lat;
        result.lat_max = node1.lat;
        result.lon_min = node1.lon;
        result.lon_max = node1.lon;
        if (result.lat_min > node2.lat) {
            result.lat_min = node2.lat;
        }
        if (result.lat_max < node2.lat) {
            result.lat_max = node2.lat;
        }
        if (result.lon_min > node2.lon) {
            result.lon_min = node2.lon;
        }
        if (result.lon_max < node2.lon) {
            result.lon_max = node2.lon;
        }
        return result;
    }

    // необходим во внешнем добавить его в Edges
    //Join two nodes by new edge
    //node1 - start node, node2 - end node
    //return: index of new edge
    public static Edge joinByEdge(Node node1, Node node2) {
        //int _rtn = 0;
        //int k = 0;
        //Edges[EdgesNum].node1 = node1;
        //Edges[EdgesNum].node2 = node2;
        Edge result = new Edge(node1, node2);
        //Edges.add(result);
        
        //add edge to both nodes
        /*
        k = Nodes[node1].Edges;
        Nodes[node1].edge[k] = EdgesNum;
        Nodes[node1].Edges = Nodes[node1].Edges + 1;
        k = Nodes[node2].Edges;
        Nodes[node2].edge[k] = EdgesNum;
        Nodes[node2].Edges = Nodes[node2].Edges + 1;
        */
        node1.addEdge(result);
        node2.addEdge(result);
        
        //_rtn = EdgesNum;
        //addEdge();
        return result; //EdgesNum; //_rtn;
    }

    //Calc cosine of angle betweeen two edges
    //(calc via vectors on reference ellipsoid, 180/-180 safe)
    public static double cosAngleBetweenEdges(Edge edge1, Edge edge2) {
        double _rtn = 0;
        //double x1, y1, z1;
        //double x2, y2, z2;
        //double x3, y3, z3;
        //double x4, y4, z4;
        //int node1, node2;
        double len1, len2;

        XYZ first;
        XYZ second;
        XYZ third;
        XYZ fourth;

        //XYZ
        //node1 = edge1.node1;
        //.node2 = edge1.node2;
        first = XYZ.latLonToXYZ(edge1.node1.lat, edge1.node1.lon);
        second = XYZ.latLonToXYZ(edge1.node2.lat, edge1.node2.lon);
        //node1 = Edges[edge2].node1;
        //.node2 = Edges[edge2]..node2;
        third = XYZ.latLonToXYZ(edge2.node1.lat, edge2.node1.lon);
        fourth = XYZ.latLonToXYZ(edge2.node2.lat, edge2.node2.lon);

        //vectors
        //x2 = second.x - first.x;
        //y2 = second.y - first.y;
        //z2 = second.z - first.z;
        second.Sub(first);
        //x4 = x4 - x3;
        //y4 = y4 - y3;
        //z4 = z4 - z3;
        fourth.Sub(third);

        //vector lengths
        //len1 = Sqr(x2 * x2 + y2 * y2 + z2 * z2);
        len1 = Math.sqrt(second.x * second.x + second.y * second.y + second.z * second.z);
        //len2 = Sqr(x4 * x4 + y4 * y4 + z4 * z4);
        len2 = Math.sqrt(fourth.x * fourth.x + fourth.y * fourth.y + fourth.z * fourth.z);

        if (len1 == 0  || len2 == 0) {
            //one of vectors is void
            _rtn = 0;
        }
        else {
            //Cosine of angle is scalar multiply divided by lengths
            _rtn = (second.x * fourth.x + second.y * fourth.y + second.z * fourth.z) / (len1 * len2);
        }

        return _rtn;
    }
    
    //Calc distance between not crossing edges (edge1 and edge2)
    //(180/-180 safe)
    public static double distanceBetweenSegments(Edge edge1, Edge edge2) {
        double d1, d2;
        
        //Just minimum of 4 distances (each ends to each other edge)
        d1 = Node.distanceToSegment(edge1.node1, edge1.node2, edge2.node1);
        d2 = Node.distanceToSegment(edge1.node1, edge1.node2, edge2.node2);
        if (d2 < d1) { d1 = d2; }
        d2 = Node.distanceToSegment(edge2.node1, edge2.node2, edge1.node1);
        if (d2 < d1) { d1 = d2; }
        d2 = Node.distanceToSegment(edge2.node1, edge2.node2, edge1.node2);
        if (d2 < d1) { d1 = d2; }
        return d1;
    }

    //Get edge, conecting node1 and node2, return -1 if no connection
    //TODO(opt): swap node1 and node2 if node2 have smaller edges
    public static Edge getEdgeBetween(Node node1, Node node2) {
        for (Edge edgeJ: node1.edgeL) {
            if (edgeJ.node1 == node2  || edgeJ.node2 == node2) {
                //found
                return edgeJ;
            }
        }
        return null;
    }
    
    // проверка цепочки Edge -> Node -> Edge (Edge == Edge)
    public int validLinks() {
        boolean ok1 = false;
        if (this.node1 != null) {
            for (Edge edgeI: this.node1.edgeL) {
                if (edgeI == this) { ok1 = true;}
            }
        } else { return 0;}
        boolean ok2 = false;
        if (this.node2 != null) {
            for (Edge edgeI:this.node2.edgeL) {
                if (edgeI == this) { ok2 = true;}
            }
        } else { return 0;}
        if (ok1 & ok2) { return 1; }
        return -1;
    }

    //Combine edges. edge2 is deleted, edge1 is kept
    //assumed, that edges have at leaset 1 common node
    //return: 0 - not combined, 1 - combined
    public static boolean combineEdges(Edge edge1, Edge edge2, Node commonNode) {
        boolean _rtn = false;
        if (combineEdgeParams(edge1, edge2, commonNode)) {
            _rtn = true;
            edge2.delEdge();
        }
        return _rtn;
    }

    //Combine edge parameters and store it to edge1
    //return: 0 - not possible to combine, 1 - combined
    public static boolean combineEdgeParams(Edge edge1, Edge edge2, Node commonNode) {
        int k1 = 0;
        int k2 = 0;
        int k3 = 0;

        if (commonNode == null) {
            //common node not specified in call
            if (edge1.node1 == edge2.node1  || edge1.node1 == edge2.node2) {
                commonNode = edge1.node1;
            }
            else if (edge1.node2 == edge2.node1  || edge1.node2 == edge2.node2) {
                commonNode = edge1.node2;
            } else {
                //can't combine edges without at least one common point
                return false;
            }
        }

        //calc combined label - by stats
        Mp_extsimp.resetLabelStats();
        Mp_extsimp.addLabelStat0(edge1.label);
        Mp_extsimp.addLabelStat0(edge2.label);
        edge1.label = Mp_extsimp.getLabelByStats(0);

        //calc combiner road type
        if (edge1.roadtype != edge2.roadtype) {
            //combine main road type - higher by OSM
            k1 = edge1.roadtype & Highway.HIGHWAY_MASK_MAIN;
            k2 = edge2.roadtype & Highway.HIGHWAY_MASK_MAIN;
            //keep link only if both are links
            k3 = (edge2.roadtype & edge2.roadtype & Highway.HIGHWAY_MASK_LINK);
            //numerically min roadtype
            if (k2 < k1) { k1 = k2; }
            edge1.roadtype = (byte)(k1 | k3);
        }

        //combined speed - lower
        if (edge2.speed < edge1.speed) {

            edge1.speed = edge2.speed;
        }

        if (edge1.oneway == 1) {
            //combined oneway - keep only if both edges directed in one way
            if (edge2.oneway == 1) {
                //both edges are oneway
                if (edge1.node1 == commonNode  && edge2.node2 == commonNode) {
                    //edges are opposite oneway, result is bidirectional
                    edge1.oneway = 0;
                }
                else if (edge1.node2 == commonNode  && edge2.node1 == commonNode) {
                    //edges are opposite oneway, result is bidirectional
                    edge1.oneway = 0;
                }
                //else - result is oneway
            }
            else {
                //edge2 is bidirectional, so result also
                edge1.oneway = 0;
            }
        }
        //if edge1 is bidirectional, so also result

        return true;
    }

    //Reconnect edge1 from node1 to node2
    //assumed that node1 is present in edge1
    public static void reconnectEdge(Edge edge1, Node node1, Node node2) {
        int i = 0;
        if (edge1.node1 == node1) {
            edge1.node1 = node2;
        }
        else {
            edge1.node2 = node2;
        }

        //remove edge1 from node1 edges
        node1.edgeL.remove(edge1);
/*        for (Edge edgeI: node1.edgeL) {
            if (edgeI == edge1) {
                //Nodes[node1]..edge(i) = Nodes[node1]..edge(Nodes[node1].Edges - 1);
                //Nodes[node1].Edges = Nodes[node1].Edges - 1;
                node1.edgeL.remove(edgeI);
        //goto found: GoTo lFound;
                break;
            }
        }*/
//label found: lFound:;
        //add edge1 to node2 edges
        //Nodes[node2]..edge(Nodes[node2].Edges) = edge1;
        //Nodes[node2].Edges = Nodes[node2].Edges + 1;
        node2.edgeL.add(edge1);
    }
}
