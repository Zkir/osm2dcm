import ru.zkir.mp2mp.core.MpData;
import ru.zkir.mp2mp.core.MpFile;
import ru.zkir.mp2mp.core.MpSection;
import ru.zkir.mp2mp.core.TaskInfo;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 18.01.13
 * Time: 21:45
 * To change this template use File | Settings | File Templates.
 */
public class ReadMpTask {

  public void execute(MpData mpData, TaskInfo taskInfo)   throws IOException
  {
    String strSrcFileName;
    strSrcFileName=taskInfo.parameters.get("file");

    System.out.println("Task: read mp file");
    System.out.println(" source file: " + strSrcFileName);

    MpFile mpFile;
    mpFile =new MpFile(strSrcFileName,0);
    MpSection ms;
    do{
      ms=mpFile.ReadNextSection();
      if (ms!=null){
        mpData.sections.add(ms);
      }
      //mpFile.ReadNextSection();
    } while(!mpFile.eof) ;

    mpFile.close();

    System.out.println(mpData.sections.size()+ " секций прочитано");
  }

}

