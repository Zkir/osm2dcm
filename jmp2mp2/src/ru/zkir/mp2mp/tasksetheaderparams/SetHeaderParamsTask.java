package ru.zkir.mp2mp.tasksetheaderparams;

import ru.zkir.mp2mp.core.MpData;
import ru.zkir.mp2mp.core.TaskInfo;
import ru.zkir.mp2mp.core.MpSection;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 06.07.13
 * Time: 11:52
 * To change this template use File | Settings | File Templates.
 */
public class SetHeaderParamsTask {
  public void execute(MpData mpData, TaskInfo taskInfo)
  {
    MpSection oMpSection;
    mpData.moveFirst();
    while (!mpData.eof()){ //цикл по секциям
      //Здесь различные операции над секцией
      oMpSection=mpData.getCurrentSection();
      if (oMpSection.SectionType.equals("[IMG ID]") )
      {
        Iterator<Entry<String,String>> attributes;
        Entry<String,String> attribute;
        String value;
        attributes=taskInfo.parameters.entrySet().iterator();
        while (attributes.hasNext())
        {
          attribute=attributes.next();
          oMpSection.SetAttributeValue(attribute.getKey() ,attribute.getValue() );
        }
        break;
      }

      mpData.moveNext();
    }

  }
}
