/**
 * PeriodMapUpdatePolicy.java
 *
 * Copyright (C) 2013 Sergey Astakhov. All Rights Reserved
 */
package ru.sergeyastakhov.osm2dcm;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Sergey Astakhov
 * @version $Revision$
 */
public class PeriodMapUpdatePolicy implements MapUpdatePolicy
{
  private File sourceDir;
  private int sourceExpiredDays = 30;
  private int sourceIsNewDays = 7;

  public void setSourceDir(File _sourceDir)
  {
    sourceDir = _sourceDir;
  }

  public void setSourceExpiredDays(int _sourceExpiredDays)
  {
    sourceExpiredDays = _sourceExpiredDays;
  }

  public void setSourceIsNewDays(int _sourceIsNewDays)
  {
    sourceIsNewDays = _sourceIsNewDays;
  }

  @Override
  public boolean isSourceUpdateNeeded(MapConversionTask task)
  {
    String sourceFileName = task.getSourceFileName();
    Date lastTryDate = task.getLastTryDate();
    int priority = task.getPriority();

    File sourceFile = new File(sourceDir, sourceFileName);

    Date sourceFileTime = sourceFile.exists() ? new Date(sourceFile.lastModified()) : null;

    Date currentTime = new Date();

    return priority < 9 &&
        lastTryDate != null &&
        TimeUnit.MILLISECONDS.toDays(currentTime.getTime() - lastTryDate.getTime()) > sourceExpiredDays &&
        (sourceFileTime == null ||
            TimeUnit.MILLISECONDS.toDays(currentTime.getTime() - sourceFileTime.getTime()) > sourceIsNewDays);
  }
}
