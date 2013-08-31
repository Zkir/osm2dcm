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
    strSrcFileName=taskInfo.parameters.get("src");
    Mp_extsimp objSimplifier;
    objSimplifier = new Mp_extsimp();
    objSimplifier.optimizeRouting(strSrcFileName);
  }
}
