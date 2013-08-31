/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.zkir.mp2mp.taskextsimp;

/**
 *
 * @author freeExec
 */
public class Mark {
    // Consts

    //Specific marking of edges/nodes for algorithms
    public static final int MARK_JUNCTION = 1;
    public static final int MARK_COLLAPSING = 2;
    public static final int MARK_AIMING = 4;
    public static final int MARK_DISTCHECK = 8;
    public static final int MARK_SIDE1CHECK = 8;
    public static final int MARK_SIDE2CHECK = 16;
    public static final int MARK_SIDESCHECK = 24;
    public static final int MARK_WAVEPASSED = 16;
    public static final int MARK_NODE_BORDER = -2;
    public static final int MARK_NODE_OF_JUNCTION = -3;
    public static final int MARK_NODEID_DELETED = -2;

}
