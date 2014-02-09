package ru.zkir.mp2mp.taskgeocoder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

// SAX
import org.xml.sax.Attributes;
// import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 *
 * @author Zkir
 */

/*
Нужно найти границы адресных полигонов.
Таковыми могут быть отношения с admin_level и замкнутые(!) веи

*/

public class OsmParser {
  public MyParser myParser;
  public OsmParser (String strOsmFileName)
  {
    SAXParserFactory factory = SAXParserFactory.newInstance();

    factory.setValidating(true);
    factory.setNamespaceAware(false);
    SAXParser parser;

    InputStream xmlData = null;
    try {
       xmlData = new FileInputStream(strOsmFileName);

      parser = factory.newSAXParser();
      myParser =new MyParser();
      parser.parse(xmlData, myParser);


    } catch (FileNotFoundException e) {
      e.printStackTrace();
      // обработки ошибки, файл не найден
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
      // обработка ошибки Parser
    } catch (SAXException e)
    {
      e.printStackTrace();
      // обработка ошибки SAX
    } catch (IOException e)
    {
      e.printStackTrace();
      // обработка ошибок ввода
    }

  }
}

