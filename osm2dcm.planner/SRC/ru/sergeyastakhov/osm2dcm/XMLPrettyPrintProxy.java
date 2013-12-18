/**
 * XMLPrettyPrintProxy.java
 *
 * Copyright (C) 2013 Sergey Astakhov. All Rights Reserved
 */
package ru.sergeyastakhov.osm2dcm;

import javax.xml.stream.XMLStreamWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Pretty print xml handler.
 * Derived from code http://www.ewernli.com/web/guest/47
 *
 * @author Sergey Astakhov
 * @version $Revision$
 */
public class XMLPrettyPrintProxy implements InvocationHandler
{
  public static XMLStreamWriter createProxy(XMLStreamWriter writer)
  {
    return (XMLStreamWriter) Proxy.newProxyInstance(
        XMLPrettyPrintProxy.class.getClassLoader(),
        new Class[]{XMLStreamWriter.class},
        new XMLPrettyPrintProxy(writer));
  }

  private XMLStreamWriter target;

  private int depth = 0;
  private Map<Integer,Boolean> hasChildElement = new HashMap<Integer,Boolean>();

  private static final String INDENT_CHAR = " ";
  private static final String LINEFEED_CHAR = "\n";

  public XMLPrettyPrintProxy(XMLStreamWriter _target)
  {
    target = _target;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    String m = method.getName();

    // Needs to be BEFORE the actual event, so that for instance the
    // sequence writeStartElem, writeAttr, writeStartElem, writeEndElem, writeEndElem
    // is correctly handled

    if( "writeStartElement".equals( m ))
    {
      // update state of parent node
      if( depth > 0 )
      {
        hasChildElement.put( depth-1, true );
      }

      // reset state of current node
      hasChildElement.put( depth, false );

      // indent for current depth
      target.writeCharacters( LINEFEED_CHAR );
      target.writeCharacters( repeat( depth, INDENT_CHAR ));

      depth++;
    }

    if( "writeEndElement".equals( m ))
    {
      depth--;

      if( hasChildElement.get( depth) == true )
      {
        target.writeCharacters( LINEFEED_CHAR );
        target.writeCharacters( repeat( depth, INDENT_CHAR ));
      }

    }

    if( "writeEmptyElement".equals(m ))
    {
      // update state of parent node
      if( depth > 0 )
      {
        hasChildElement.put( depth-1, true );
      }

      // indent for current depth
      target.writeCharacters( LINEFEED_CHAR );
      target.writeCharacters( repeat( depth, INDENT_CHAR ));
    }

    method.invoke( target, args );

    return null;
  }

  private static String repeat( int d, String s )
  {
    StringBuilder sb = new StringBuilder();

    while( d-- > 0 )
    {
      sb.append(s);
    }

    return sb.toString();
  }
}
