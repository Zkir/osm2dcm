package ru.zkir.mp2mp.vb6;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 07.10.12
 * Time: 16:37
 * To change this template use File | Settings | File Templates.
 */
public class vb6 {
  public static String Left(String str, int count)
  {
   String result;
    if (str.length()>=count){
      result=str.substring(0,count);
    }
    else{
      result=str.substring(0,str.length());
    }
  return result;
  }

  public static String Right(String str, int count)
  {
    String result;
    if (str.length()>=count){
      result=str.substring(str.length()-count,str.length());
    }
    else{
      result=str.substring(0,str.length());
    }
    return result;
  }
  public static int InStr (String str, String substr)
  {
     return (str.indexOf(substr)+1);

  }
  public static long Round(double x)
  {
    return Math.round(x);
  }
}
