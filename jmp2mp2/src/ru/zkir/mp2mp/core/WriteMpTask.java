package ru.zkir.mp2mp.core;

import ru.zkir.mp2mp.core.MpData;
import ru.zkir.mp2mp.core.MpFile;
import ru.zkir.mp2mp.core.TaskInfo;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 18.01.13
 * Time: 23:09
 * To change this template use File | Settings | File Templates.
 */
public class WriteMpTask {

  public void execute(MpData mpData, TaskInfo taskInfo)   throws IOException
  {
    String strTargetFileName;
    strTargetFileName=taskInfo.parameters.get("file");

    System.out.println("Task: write mp file");
    System.out.println(" target file: " + strTargetFileName);

    MpFile mpFile;
    mpFile =new MpFile(strTargetFileName,1);


    for(int i=0;i<mpData.sections.size();i++ )
    {
      mpFile.WriteSection(mpData.sections.get(i));

    }
    mpFile.close();
    System.out.println(mpData.sections.size()+ " секций записано");
  }
}
