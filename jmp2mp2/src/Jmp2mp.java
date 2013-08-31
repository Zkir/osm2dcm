
import ru.zkir.mp2mp.core.MPParseException;
import ru.zkir.mp2mp.core.MpData;
import ru.zkir.mp2mp.core.ReadMpTask;
import ru.zkir.mp2mp.core.WriteMpTask;
import ru.zkir.mp2mp.taskforcejunctions.ForceJunctionsTask;
import ru.zkir.mp2mp.taskgeocoder.GeocoderTask;
import ru.zkir.mp2mp.tasksetheaderparams.SetHeaderParamsTask;
import ru.zkir.mp2mp.taskextsimp.SimplifyRoadsTask;

import java.io.IOException;




/**
 * Created with IntelliJ IDEA.
 * User: KBondarenko
 * Date: 15/01/13
 * Time: 11:49
 * To change this template use File | Settings | File Templates.
 */


public class Jmp2mp {

    public static void main(String[] args) throws IOException,MPParseException
    {
        System.out.println(" --| jmp2mp (c) Zkir 2012-2013");

        CmdLineParser cmdLineParser;
        cmdLineParser= new CmdLineParser();

        cmdLineParser.parseCommandLine(args);
        /*
        //Тест парсера командной строки
        for (int i=0; i<cmdLineParser.tasks.size();i++)
        {
          System.out.println(cmdLineParser.tasks.get(i).name );
          for(int j=0;j<cmdLineParser.tasks.get(i).parameters.size();j++ )
          {
              System.out.println(cmdLineParser.tasks.get(i).parameters.toString());
          }
        }
        */

        //Тут бы надо бы написать логику активации задач последовательно, на основ командной строки.
        //Пока тупо.
        String inpipe="DefaultPipe";
        String outpipe="DefaultPipe";
        MpData mpData=new MpData();
        int i;

        for(i = 0;i<cmdLineParser.tasks.size();i++ )
        {
          System.out.println("task:"+ cmdLineParser.tasks.get(i).name);

          //readmp
          if (cmdLineParser.tasks.get(i).name.equals("readmp"))
          {
            ReadMpTask readMpTask=new ReadMpTask();
            readMpTask.execute(mpData, cmdLineParser.tasks.get(i));
          }

          //geocode
          if (cmdLineParser.tasks.get(i).name.equals("geocode"))
          {
            GeocoderTask geocoder;
            geocoder=new GeocoderTask();
            geocoder.execute(mpData, cmdLineParser.tasks.get(i));
          }

          //forcejunctions
          if (cmdLineParser.tasks.get(i).name.equals("forcejunctions"))
          {
            ForceJunctionsTask fj;
            fj=new ForceJunctionsTask();
            fj.execute(mpData, cmdLineParser.tasks.get(i));
          }

          //setheaderparams
          if (cmdLineParser.tasks.get(i).name.equals("setheaderparams"))
          {
            SetHeaderParamsTask  shp;
            shp=new SetHeaderParamsTask();
            shp.execute(mpData, cmdLineParser.tasks.get(i));
          }

          //mp_extsimp
          if (cmdLineParser.tasks.get(i).name.equals("simplifyroads"))
          {
            SimplifyRoadsTask smpr;
            smpr=new SimplifyRoadsTask();
            smpr.execute(mpData, cmdLineParser.tasks.get(i));
          }

          //writemp
          if (cmdLineParser.tasks.get(i).name.equals("writemp"))
          {
            WriteMpTask writeMpTask;
            writeMpTask=new WriteMpTask();
            writeMpTask.execute(mpData, cmdLineParser.tasks.get(i));
          }
        }

    }
}



