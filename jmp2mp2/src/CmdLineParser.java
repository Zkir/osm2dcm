import ru.zkir.mp2mp.core.TaskInfo;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 15.01.13
 * Time: 20:38
 * To change this template use File | Settings | File Templates.
 */
public class CmdLineParser
{
  ArrayList<TaskInfo> tasks;
  CmdLineParser()
  {
    tasks=new ArrayList<TaskInfo>();
  }
  void parseCommandLine(String[] args)
  {
    TaskInfo newTask=null;
    for (int i=0; i<args.length;i++)
    {
      String currentToken;
      currentToken= args[i];
      //System.out.println(currentToken.substring(0,2));
      if (currentToken.substring(0,2).equals("--"))
      {

        newTask = new TaskInfo();
        newTask.name  =currentToken.substring(2);
        tasks.add(newTask);
      }
      else
      {
        String[] params;
        params=currentToken.split("=",2);
        if (newTask!=null)
        {newTask.parameters.put(params[0],params[1]);}
      }
    }

  };
}
