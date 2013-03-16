/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 07.10.12
 * Time: 13:56
 * To change this template use File | Settings | File Templates.
 */
import ru.zkir.mp2mp.core.MPParseException;
import ru.zkir.mp2mp.core.MpFile;
import ru.zkir.mp2mp.core.MpSection;
import ru.zkir.mp2mp.taskvalidator.*;
import ru.zkir.mp2mp.vb6.vb6;

import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.*;

public class jmp2mp {

  //Основные параметры карты
  private static String strSource; //Входной файл
  private static String strTarget; //Выходной файл
  private static String strViewPoint; //Начальная точка карты
  private static boolean blnEroadShieldsOnly;//Только евромаршруты на щитах дорог.
  private static boolean blnNoRoutingTestByLevels;

  //Точка входа
  public static void  main(String args[]) throws IOException,MPParseException
  {
    System.out.println(" --| jmp2mp (c) Zkir 2012");
    ParseCommandLine(args);

    if ((strSource != "") & (strTarget != "")) {

      ValidatorTask validator;
      validator=new ValidatorTask();
      validator.execute (strSource, strTarget, strViewPoint, blnEroadShieldsOnly,blnNoRoutingTestByLevels);

    }
    else
    {
      System.out.println( "Usage: jmp2mp <source mp file> <target mp file> <view point>");
    }

  }

  private static void ParseCommandLine(String args[])
  {
    //Инициализация
    strSource="";
    strTarget="";
    strViewPoint="";
    blnEroadShieldsOnly=false;
    blnNoRoutingTestByLevels=false;

   // strSource="d:/OSM/osm2dcm/_my/test/Test.pre.mp";
   // strTarget="d:/OSM/osm2dcm/_my/test/Test.java.mp";

    strSource=args[0];
    strTarget=args[1];

    if(args.length>2)
      strViewPoint=args[2];

    if(args.length>3)
      {
        if (args[3].equals("1"))
        {
          blnEroadShieldsOnly=true;
        }
      }
    if(args.length>4)
    {
      if (args[4].equals("1"))
      {
        blnNoRoutingTestByLevels=true;
      }
    }


  }

}
