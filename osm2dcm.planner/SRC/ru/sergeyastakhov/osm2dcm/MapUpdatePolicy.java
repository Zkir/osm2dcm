/**
 * MapUpdatePolicy.java
 *
 * Copyright (C) 2013 Sergey Astakhov. All Rights Reserved
 */
package ru.sergeyastakhov.osm2dcm;

/**
 * @author Sergey Astakhov
 * @version $Revision$
 */
public interface MapUpdatePolicy
{
  boolean isSourceUpdateNeeded(MapConversionTask task);
}
