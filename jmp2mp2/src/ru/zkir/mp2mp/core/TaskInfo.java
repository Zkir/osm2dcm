package ru.zkir.mp2mp.core;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 18.01.13
 * Time: 21:05
 * To change this template use File | Settings | File Templates.
 */
public class TaskInfo
{
  public String name;
  public HashMap<String, String> parameters;
  public TaskInfo()
  { parameters=new HashMap<String, String>() ;}
}
