/**
 * MapListWriter.java
 *
 * Copyright (C) 2013 Sergey Astakhov. All Rights Reserved
 */
package ru.sergeyastakhov.osm2dcm;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sergey Astakhov
 * @version $Revision$
 */
public class MapListWriter
{
  private static final Logger log = Logger.getLogger("ru.sergeyastakhov.osm2dcm.MapListWriter");

  private String downloadUrlTemplate;
  private File mapListXMLFile;
  private String mapListXMLEncoding;
  private String runAfterUpdate;

  public void setDownloadUrlTemplate(String _downloadUrlTemplate)
  {
    downloadUrlTemplate = _downloadUrlTemplate;
  }

  public void setMapListXMLFile(File _mapListXMLFile)
  {
    mapListXMLFile = _mapListXMLFile;
  }

  public void setMapListXMLEncoding(String _mapListXMLEncoding)
  {
    mapListXMLEncoding = _mapListXMLEncoding;
  }

  public void setRunAfterUpdate(String _runAfterUpdate)
  {
    runAfterUpdate = _runAfterUpdate;
  }

  public synchronized void saveMapList(List<MapConversionTask> mapTaskList)
  {
    try
    {
      XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

      OutputStream os = new FileOutputStream(mapListXMLFile);

      try
      {
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter(os, mapListXMLEncoding);

        writer = XMLPrettyPrintProxy.createProxy(writer);

        try
        {
          writer.writeStartDocument(mapListXMLEncoding, "1.0");

          writer.writeStartElement("maplist");

          for( MapConversionTask task : mapTaskList )
          {
            if( task.isHaveMapDate() )
              task.writeTo(writer, downloadUrlTemplate);
          }

          writer.writeEndElement();

          writer.writeEndDocument();
        }
        catch( Exception e )
        {
          log.severe("Error writing xml file: " + e);
        }
        finally
        {
          writer.close();
        }
      }
      finally
      {
        os.close();
      }

      afterUpdate();
    }
    catch( Exception e )
    {
      log.severe("Error creating xml file: " + e);
    }
  }

  private void afterUpdate()
  {
    if( runAfterUpdate == null || runAfterUpdate.length() == 0 )
    {
      return;
    }

    log.log(Level.INFO, "Executing {0}", runAfterUpdate);

    try
    {
      ProcessBuilder pb = new ProcessBuilder(runAfterUpdate);

      pb.inheritIO();

      Process process = pb.start();

      int result = process.waitFor();

      if( result != 0 )
      {
        log.log(Level.WARNING, "Execution of {0} failed. Result={1}", new Object[]{runAfterUpdate, result});
      }
    }
    catch( Exception e )
    {
      log.log(Level.SEVERE, "Execution of " + runAfterUpdate + " failed", e);
    }
  }
}
