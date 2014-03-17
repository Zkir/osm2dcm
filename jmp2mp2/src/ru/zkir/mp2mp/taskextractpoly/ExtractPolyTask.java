package ru.zkir.mp2mp.taskextractpoly;

import ru.zkir.mp2mp.core.MPParseException;
import ru.zkir.mp2mp.core.MpData;
import ru.zkir.mp2mp.core.MpSection;
import ru.zkir.mp2mp.core.TaskInfo;

import java.io.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 15.03.14
 * Time: 13:30
 * To change this template use File | Settings | File Templates.
 */
public class ExtractPolyTask {

  public void execute(MpData mpData, TaskInfo taskInfo) throws MPParseException,IOException
  {
    String strOutFolderName;


    strOutFolderName=taskInfo.parameters.get("outfolder");

    Date dtProcessEnd,  dtProcessStart;

    System.out.println("Task: ExtractPoly");
    System.out.println(" output folder: " + strOutFolderName);


    extractPolyFromMp(mpData, strOutFolderName);

  }
  private void extractPolyFromMp(MpData mpData, String strOutFolderName)  throws IOException,MPParseException
  {

    MpSection ms;
    mpData.moveFirst();
    while (!mpData.eof() )
    {
      ms=mpData.getCurrentSection();
      if(
         ms.SectionType.equals("[POLYGON]") && ms.mpType().equalsIgnoreCase("0x4b")
         )
      {
        System.out.print(" poly:"+ms.GetAttributeValue("Label"));
        savePolygonAsPoly(ms, strOutFolderName, ms.GetAttributeValue("Label"));
        System.out.println(" OK");
      }
      mpData.moveNext();
    }

  }
  private void savePolygonAsPoly (MpSection ms, String strOutFolderName, String strPolyName) throws IOException,MPParseException
  {
    String strFileName;
    strFileName=strOutFolderName+"\\"+strPolyName+".poly";
    BufferedWriter oOutFile;
    oOutFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(strFileName), "windows-1251"));

    double[][] coords=ms.GetCoordArray(true);

    oOutFile.write(strPolyName+"\r\n");
    oOutFile.write("1\r\n");
    for (int i=0;i<coords.length;i++)
    {
      oOutFile.write("   "+ coords[i][1]+"    "+coords[i][0]+" \r\n");
    }

    oOutFile.write("END\r\n");
    oOutFile.write("END\r\n");
    oOutFile.close();

  }



}
