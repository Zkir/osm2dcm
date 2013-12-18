/**
 * Main.java
 *
 * Copyright (C) 2013 Sergey Astakhov. All Rights Reserved
 */
package ru.sergeyastakhov.osm2dcm;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sergey Astakhov
 * @version $Revision$
 */
public class Main
{
  private static final Logger log = Logger.getLogger("ru.sergeyastakhov.osm2dcm.Main");

  public static void main(String[] args)
  {
    try
    {
      // Загрузка конфигурации
      log.info("Loading config...");

      String configFile = "config/config.properties";

      if( args.length > 0 )
        configFile = args[0];

      Properties config = loadConfig(configFile);

      // Настройка конвертера
      MapConverter mapConverter = new MapConverter();

      mapConverter.setEncoding(config.getProperty("history.fileEncoding", "Windows-1251"));
      mapConverter.setHistoryFile(new File(config.getProperty("history.filePath", "history.txt")));

      File historyLogDir = new File(config.getProperty("history.logDir", "history-log"));
      historyLogDir.mkdirs();
      mapConverter.setHistoryLogDir(historyLogDir);

      File sourceDir = new File(config.getProperty("sourceDir"));
      mapConverter.setSourceDir(sourceDir);

      File processLogDir = new File(config.getProperty("processing.logDir", "process-log"));
      processLogDir.mkdirs();
      mapConverter.setProcessLogDir(processLogDir);

      MapListWriter mapListWriter = new MapListWriter();
      mapListWriter.setDownloadUrlTemplate(config.getProperty("mapListXML.downloadUrlTemplate"));
      mapListWriter.setMapListXMLFile(new File(config.getProperty("mapListXML.file")));
      mapListWriter.setMapListXMLEncoding(config.getProperty("mapListXML.encoding","Windows-1251"));
      mapListWriter.setRunAfterUpdate(config.getProperty("mapListXML.runAfterUpdate","uploadmaplist.bat"));

      mapConverter.setMapListWriter(mapListWriter);

      PeriodMapUpdatePolicy mapUpdatePolicy = new PeriodMapUpdatePolicy();

      mapUpdatePolicy.setSourceDir(sourceDir);
      mapUpdatePolicy.setSourceExpiredDays(Integer.parseInt(config.getProperty("mapUpdatePolicy.sourceExpiredDays","30")));
      mapUpdatePolicy.setSourceIsNewDays(Integer.parseInt(config.getProperty("mapUpdatePolicy.sourceIsNewDays","7")));

      mapConverter.setMapUpdatePolicy(mapUpdatePolicy);

      // Загрузка списка карт
      log.info("Loading task list...");
      mapConverter.loadTaskList();

      // Пул потоков
      int poolSize = Integer.parseInt(config.getProperty("processing.poolSize", "4"));

      ExecutorService convertExecutor = new ThreadPoolExecutor
          (poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>());

      mapConverter.setConvertExecutor(convertExecutor);

      ExecutorService updateExecutor = Executors.newFixedThreadPool(1);

      mapConverter.setUpdateExecutor(updateExecutor);

      // Запуск конвертации
      mapConverter.doConversion();
    }
    catch( Exception e )
    {
      log.log(Level.SEVERE, "Error while executing conversion tasks", e);
    }
  }

  private static Properties loadConfig(String configFile) throws IOException
  {
    Properties config = new Properties();

    FileReader configReader = new FileReader(configFile);

    try
    {
      config.load(configReader);

      return config;
    }
    finally
    {
      configReader.close();
    }
  }
}
