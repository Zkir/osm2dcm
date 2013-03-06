package ru.zkir.mp2mp.core;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 18.01.13
 * Time: 23:18
 * To change this template use File | Settings | File Templates.
 */
public class MpData
{
   public ArrayList<MpSection> sections;
   public MpData()
   {
     sections=new ArrayList<MpSection>();
     intCursorPosition=0;
     blnCurrentRecordDeleted=false;
   }
  //Это такой прикол, имитируем вибишный рекодсет
  private int intCursorPosition;
  private boolean blnCurrentRecordDeleted;
  public boolean eof()
  {
    return (intCursorPosition>=sections.size()) ;
  }
  public MpSection getCurrentSection()
  {
    if (!blnCurrentRecordDeleted)
    {
      return sections.get(intCursorPosition);
    }
    else
    {
      return null;
    }
  }
  public void moveFirst()
  {
    intCursorPosition=0;
    blnCurrentRecordDeleted=false;
  }
  public void moveNext()
  {
    intCursorPosition++;
    blnCurrentRecordDeleted=false;
  }

  public void delete()
  {
    sections.remove(intCursorPosition);
    blnCurrentRecordDeleted=true;
    intCursorPosition--;
  }
}
