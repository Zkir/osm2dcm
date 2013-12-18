/**
 * MapConversionTask.java
 *
 * Copyright (C) 2013 Sergey Astakhov. All Rights Reserved
 */
package ru.sergeyastakhov.osm2dcm;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sergey Astakhov
 * @version $Revision$
 */
public class MapConversionTask
{
  private static final Logger log = Logger.getLogger("ru.sergeyastakhov.osm2dcm.MapConversionTask");

  public static final Comparator<? super MapConversionTask> PRIORITY_SORT = new Comparator<MapConversionTask>()
  {
    @Override
    public int compare(MapConversionTask m1, MapConversionTask m2)
    {
      int result = Integer.valueOf(m1.priority).compareTo(m2.priority);

      if( result == 0 )
      {
        long l1 = m1.lastTryDate != null ? m1.lastTryDate.getTime() : 0;
        long l2 = m2.lastTryDate != null ? m2.lastTryDate.getTime() : 0;

        result = Long.valueOf(l1).compareTo(l2);
      }

      if( result == 0 )
      {
        result = Integer.valueOf(m1.usedTime).compareTo(m2.usedTime);
      }

      if( result == 0 )
      {
        result = m1.code.compareTo(m2.code);
      }

      return result;
    }
  };

  public static final Comparator<? super MapConversionTask> NAME_SORT = new Comparator<MapConversionTask>()
  {
    @Override
    public int compare(MapConversionTask m1, MapConversionTask m2)
    {
      return m1.locTitle.compareTo(m2.locTitle);
    }
  };

  private String code;
  private String cgId;
  private int priority;
  private String locTitle;
  private String title;
  private String poly;
  private String source;
  private String qaMode;
  private String customKeys;
  private String viewPoint;
  private Date lastTryDate;
  private Date date;
  private int version;
  private int usedTime;

  public MapConversionTask(String line)
  {
    String[] fields = line.split("\\|");

    code = fields[0].trim();
    cgId = fields[1].trim();
    priority = Integer.parseInt(fields[2].trim());

    locTitle = fields[3].trim();
    title = fields[4].trim();

    poly = fields[5].trim();
    source = fields[6].trim();
    qaMode = fields[7].trim();
    customKeys = fields[8].trim();
    viewPoint = fields[9].trim();

    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    try
    {
      lastTryDate = dateFormat.parse(fields[10].trim());
    }
    catch( ParseException ignore )
    {
    }

    try
    {
      date = dateFormat.parse(fields[11].trim());
    }
    catch( ParseException ignore )
    {
    }

    version = Integer.parseInt(fields[12].trim());
    usedTime = Integer.parseInt(fields[13].trim());
  }

  public synchronized String toTextLine()
  {
    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    return String.format
        ("%-11s | %-11s | %-2s | %-32s | %-32s | %-10s | %-20s | %-3s | %-45s | %-20s | %-20s | %-20s | %-3s | %-3s",
         code,cgId,priority,locTitle,title,poly,source,qaMode,customKeys,viewPoint,
         lastTryDate != null ? dateFormat.format(lastTryDate) : "",
         date != null ? dateFormat.format(date) : "",
         version, usedTime);
  }

  public synchronized void writeTo(XMLStreamWriter writer, String downloadUrlTemplate) throws XMLStreamException
  {
    writer.writeStartElement("map");

    writer.writeStartElement("code");
    writer.writeCharacters(code);
    writer.writeEndElement();

    writer.writeStartElement("uid");
    writer.writeCharacters(cgId);
    writer.writeEndElement();

    writer.writeStartElement("name");
    writer.writeCharacters(title.length() > 0 ? title : locTitle);
    writer.writeEndElement();

    writer.writeStartElement("name_ru");
    writer.writeCharacters(locTitle);
    writer.writeEndElement();

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    writer.writeStartElement("date");
    writer.writeCharacters(dateFormat.format(date));
    writer.writeEndElement();

    writer.writeStartElement("version");
    writer.writeCharacters("1." + version);
    writer.writeEndElement();

    writer.writeStartElement("url");
    writer.writeCharacters(MessageFormat.format(downloadUrlTemplate, code));
    writer.writeEndElement();

    if( code.equals("EU-OVRV") )
    {
      writer.writeStartElement("overview");
      writer.writeCharacters("1");
      writer.writeEndElement();
    }

    writer.writeEndElement();
  }

  public String getCode()
  {
    return code;
  }

  public Date getLastTryDate()
  {
    return lastTryDate;
  }

  public boolean isHaveMapDate()
  {
    return date != null;
  }

  public boolean convertMap(File logDir, File sourceDir) throws Exception
  {
    String sourceFileName = getSourceFileName();
    File sourceFile = new File(sourceDir, sourceFileName);

    File logFile = new File(logDir, code + ".log");

    log.log(Level.INFO, "Process map {0} {1} {2,date,short} {3,date,short}",
            new Object[]{code, source, date, lastTryDate});

    if( priority != 0 )
    {
      if( !sourceFile.exists() )
      {
        log.log(Level.WARNING, "Can''t convert map {0} - no source file {1}", new Object[]{code, sourceFile});
        return false;
      }

      Date sourceFileTime = new Date(sourceFile.lastModified());

      if( !sourceFileTime.after(lastTryDate) )
      {
        log.log(Level.INFO, "Skipping map {0} - source file {1} is not updated since last try ({2,date, short})",
                new Object[]{code, sourceFile, lastTryDate});
        return false;
      }
    }

    // Запуск конвертации

    return runConversion(sourceFileName, logFile);
  }

  private boolean runConversion(String sourceFileName, File logFile) throws Exception
  {
    log.log(Level.INFO, "Start conversion for map {0} {1} {2}", new Object[]{code, title, sourceFileName});

    String dcmTitle = title.length() != 0 ? title : locTitle;
    String polyFile = poly.length() != 0 ? poly : code;

    int newVersion = version + 1;

    ProcessBuilder pb = new ProcessBuilder
        ("make.bat",
         code,
         StringTools.quoteString(dcmTitle),
         polyFile,
         sourceFileName,
         qaMode,
         StringTools.quoteString(customKeys),
         StringTools.quoteString(viewPoint),
         Integer.toString(newVersion),
         cgId);

    pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
    pb.redirectError(ProcessBuilder.Redirect.INHERIT);

    long startMark = System.currentTimeMillis();

    Process process = pb.start();

    int result = process.waitFor();

    long elapsedTime = System.currentTimeMillis() - startMark;

    log.log(Level.INFO, "Conversion for map {0} completed. Result code = {1}", new Object[]{code, result});

    lastTryDate = new Date();

    boolean conversionSuccess = result == 0;

    if( conversionSuccess )
    {
      // При успешной конвертации - обновление атрибутов карты
      synchronized( this )
      {
        version = newVersion;
        usedTime = (int) TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
        date = lastTryDate;
        priority = 6;
      }
    }

    return conversionSuccess;
  }

  public String getSourceFileName()
  {
    return source.length() != 0 ? source : code + ".osm";
  }

  public int getPriority()
  {
    return priority;
  }
}
