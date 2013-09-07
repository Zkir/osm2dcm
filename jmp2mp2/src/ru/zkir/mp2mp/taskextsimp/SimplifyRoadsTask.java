package ru.zkir.mp2mp.taskextsimp;

import ru.zkir.mp2mp.core.MPParseException;
import ru.zkir.mp2mp.core.MpData;
import ru.zkir.mp2mp.core.TaskInfo;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 29.08.13
 * Time: 20:26
 * To change this template use File | Settings | File Templates.
 */
public class SimplifyRoadsTask {

  public void execute(MpData mpData, TaskInfo taskInfo) throws MPParseException
  {
    String strSrcFileName;
    int intEdgeLengthLimit=3;
    strSrcFileName=taskInfo.parameters.get("src");
    if (taskInfo.parameters.containsKey("edgelengthlimit"))
    {
      intEdgeLengthLimit=Integer.parseInt(taskInfo.parameters.get("edgelengthlimit"));
    }

    System.out.println( " source file: "+strSrcFileName);
    System.out.println( " edge length limit: "+intEdgeLengthLimit);

    Mp_extsimp objSimplifier;
    objSimplifier = new Mp_extsimp();
    objSimplifier.optimizeRouting(strSrcFileName, intEdgeLengthLimit);
  }
}
