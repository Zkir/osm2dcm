/**
 * MapConverter.java
 *
 * Copyright (C) 2013 Sergey Astakhov. All Rights Reserved
 */
package ru.sergeyastakhov.osm2dcm;

import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sergey Astakhov
 * @version $Revision$
 */
public class MapConverter
{
  private static final Logger log = Logger.getLogger("ru.sergeyastakhov.osm2dcm.MapConverter");

  private List<MapConversionTask> mapTaskList = new ArrayList<MapConversionTask>();
  private List<MapConversionTask> sortedTaskList;

  private File historyFile;
  private File historyLogDir;
  private String encoding;

  private File sourceDir;
  private File processLogDir;

  private MapListWriter mapListWriter;

  private MapUpdatePolicy mapUpdatePolicy;

  private ExecutorService convertExecutor;

  private ExecutorService updateExecutor;

  public void setHistoryFile(File _historyFile)
  {
    historyFile = _historyFile;
  }

  public void setHistoryLogDir(File _historyLogDir)
  {
    historyLogDir = _historyLogDir;
  }

  public void setEncoding(String _encoding)
  {
    encoding = _encoding;
  }

  public void setSourceDir(File _sourceDir)
  {
    sourceDir = _sourceDir;
  }

  public void setProcessLogDir(File _processLogDir)
  {
    processLogDir = _processLogDir;
  }

  public void setMapListWriter(MapListWriter _mapListWriter)
  {
    mapListWriter = _mapListWriter;
  }

  public void setMapUpdatePolicy(MapUpdatePolicy _mapUpdatePolicy)
  {
    mapUpdatePolicy = _mapUpdatePolicy;
  }

  public void setConvertExecutor(ExecutorService _convertExecutor)
  {
    convertExecutor = _convertExecutor;
  }

  public void setUpdateExecutor(ExecutorService _updateExecutor)
  {
    updateExecutor = _updateExecutor;
  }

  public void doConversion() throws InterruptedException
  {
    // Запуск заданий на выполнение
    log.info("Start executing conversion tasks...");

    // Сортировка списка
    List<MapConversionTask> sortedTaskList = new ArrayList<MapConversionTask>(mapTaskList);

    Collections.sort(sortedTaskList, MapConversionTask.PRIORITY_SORT);

    // Формирование списка задач на выполнение

    for( MapConversionTask task : sortedTaskList )
    {
      convertExecutor.execute(new ConversionTask(task, true));
    }
  }

  private void checkForEmptyQueues()
  {
    ThreadPoolExecutor ctpe = (ThreadPoolExecutor) convertExecutor;
    ThreadPoolExecutor utpe = (ThreadPoolExecutor) updateExecutor;

    log.log(Level.FINE,
            "Queue state: convertExecutor - {0} from {1} completed, updateExecutor - {2} from {3} completed",
            new Object[]{ctpe.getCompletedTaskCount(), ctpe.getTaskCount(), utpe.getCompletedTaskCount(), utpe.getTaskCount()});

    if( ctpe.getCompletedTaskCount() + utpe.getCompletedTaskCount() + 1 == ctpe.getTaskCount() + utpe.getTaskCount() )
    {
      log.info("Queue task is empty, exiting");
      System.exit(0);
    }
  }

  public synchronized void loadTaskList() throws IOException
  {
    mapTaskList.clear();

    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(historyFile), encoding));

    try
    {
      String line;

      while( (line = br.readLine()) != null )
      {
        line = line.trim();
        if( line.length() == 0 || line.startsWith("#") )
          continue;

        mapTaskList.add(new MapConversionTask(line));
      }
    }
    finally
    {
      br.close();
    }

    sortedTaskList = new ArrayList<MapConversionTask>(mapTaskList);

    Collections.sort(sortedTaskList, MapConversionTask.NAME_SORT);
  }

  private synchronized void saveTaskList(File file) throws IOException
  {
    PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));

    try
    {
      for( MapConversionTask task : mapTaskList )
      {
        pw.println(task.toTextLine());
      }
    }
    finally
    {
      pw.close();
    }
  }

  private class ConversionTask implements Runnable, Comparable<ConversionTask>
  {
    private MapConversionTask task;
    private boolean checkForUpdate = true;

    private ConversionTask(MapConversionTask _task, boolean _checkForUpdate)
    {
      task = _task;
      checkForUpdate = _checkForUpdate;
    }

    @Override
    public void run()
    {
      if( checkForUpdate && mapUpdatePolicy.isSourceUpdateNeeded(task) )
      {
        updateExecutor.execute(new UpdateSourceTask(task));
        return;
      }

      try
      {
        boolean conversionSuccess = task.convertMap(processLogDir, sourceDir);

        try
        {
          // Сохранение списка в отдельном каталоге
          String fileName = MessageFormat.format("history.txt-{0,time,yyyyMMddHHmm}", task.getLastTryDate());

          File historyLogFile = new File(historyLogDir, fileName);

          saveTaskList(historyLogFile);

          saveTaskList(historyFile);
        }
        catch( Exception e )
        {
          log.log(Level.WARNING, "Error saving history", e);
        }

        if( conversionSuccess )
        {
          mapListWriter.saveMapList(sortedTaskList);
        }

        checkForEmptyQueues();
      }
      catch( Exception e )
      {
        log.log(Level.WARNING, "Error executing conversion task", e);
      }
    }

    @Override
    public int compareTo(ConversionTask o)
    {
      return MapConversionTask.PRIORITY_SORT.compare(task, o.task);
    }
  }

  public class UpdateSourceTask implements Runnable
  {
    private MapConversionTask task;

    public UpdateSourceTask(MapConversionTask _task)
    {
      task = _task;
    }

    @Override
    public void run()
    {
      if( !mapUpdatePolicy.isSourceUpdateNeeded(task) )
      {
        convertExecutor.execute(new ConversionTask(task, true));
        return;
      }

      File logFile = new File(processLogDir, "update.log");

      String sourceFileName = task.getSourceFileName();
      String code = task.getCode();

      log.log(Level.INFO, "Trying to update source file {0} for map {1}", new Object[]{sourceFileName, code});

      try
      {
        ProcessBuilder pb = new ProcessBuilder("update.bat", sourceFileName);

        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process process = pb.start();

        int result = process.waitFor();

        boolean sourceUpdated = !mapUpdatePolicy.isSourceUpdateNeeded(task);

        if( result == 0 && (sourceUpdated || task.getPriority()==0) )
        {
          convertExecutor.execute(new ConversionTask(task, sourceUpdated));
        }
        else
        {
          log.log(Level.WARNING, "Update source file {0} for map {1} failed. Result={2}",
                  new Object[]{sourceFileName, code, result});

          checkForEmptyQueues();
        }
      }
      catch( Exception e )
      {
        log.log(Level.SEVERE, "Update source file failed", e);
      }
    }
  }


}
