package ru.zkir.mp2mp.taskgeocoder;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 02.02.14
 * Time: 13:19
 * To change this template use File | Settings | File Templates.
 */ // http://java.sun.com/j2se/1.4.2/docs/api/org/xml/sax/helpers/DefaultHandler.html
public class MyParser extends DefaultHandler {
  //Осм файл состоит из точек, линий и отношений.
  public HashMap<String,OsmNode> nodes;
  public HashMap<String,OsmWay> ways;
  public ArrayList<OsmRelation> relations;

  OsmNode currentNode;
  OsmWay currentWay;
  OsmRelation currentRelation;
  String currentObjectType;
  MyParser()
  {
    nodes=new HashMap<String,OsmNode>();
    ways=new HashMap<String,OsmWay>();
    relations=new ArrayList<OsmRelation>() ;
  }

  @Override
  public void startElement(String uri, String localName, String qName,
                           Attributes attributes) throws SAXException {

    if (qName.equals("node")) {
      currentNode=new OsmNode(attributes.getValue("id"),attributes.getValue("lat"),attributes.getValue("lon"));
      nodes.put(attributes.getValue("id"),currentNode ) ;
      currentObjectType="node";
    }

    if (qName.equals("way")) {
       currentWay = new OsmWay();
       currentWay.id=attributes.getValue("id");
       currentObjectType="way";
    }

    if (qName.equals("nd")) {
      if (currentObjectType.equals("way"))
      {
        currentWay.nodeRefs.add(attributes.getValue("ref"));
      }
    }


    if (qName.equals("relation")) {
      currentRelation = new OsmRelation();
      currentObjectType="relation";
      currentRelation.id=attributes.getValue("id");
    }

    //<tag k='admin_level' v='8' />
    if (qName.equals("tag")) {

      String strKey;
      String strValue;

      strKey=attributes.getValue("k");
      strValue = attributes.getValue("v");
      if (currentObjectType.equals("relation"))
      {
        currentRelation.addTag(strKey,strValue);
      }

      //Это нужно для городов
      if (currentObjectType.equals("node"))
      {
        if (strKey.equals("place")||strKey.equals("name")||strKey.equals("name:en") ) {
          currentNode.addTag(strKey, strValue);
        }
      }

    }
    //<member type='way' ref='38081911' role='outer' />
    if (qName.equals("member")) {
      if (attributes.getValue("type").equals("way"))
      {
        String strRole;
        strRole=attributes.getValue("role");
        if (!(strRole.equals("")||strRole.equals("outer")||strRole.equals("inner") ))
        {
          System.out.println("Warning: unknown role "+strRole);
        }

        currentRelation.addMember(attributes.getValue("ref"),attributes.getValue("role"));
      }
    }


    //      System.out.println(attributes.getLength());
    super.startElement(uri, localName, qName, attributes);


  }

  /*
  @Override
  public void characters(char[] c, int start, int length)
          throws SAXException {
    super.characters(c, start,  length);
    for (int i=start;i< start+length;++i) {
      System.err.print(c[i]);
    }
  }
  */

  @Override
  public void endElement(String uri, String localName, String qName)
          throws SAXException {

   // System.out.println("Тег разобран: "+qName);
    if (qName.equals("way"))
    {
        ways.put(currentWay.id,currentWay) ;
    }

    if (qName.equals("relation"))
    {
      if (currentRelation.tags.containsKey("admin_level"))
      {
         relations.add(currentRelation);
      }
    }


    //если закончился релейшн или вей, нужно накопленную информацию о них куда-нибудь зафигачить.
    // в частности, для веев это список точек, а для релейшенов - список веев.
    super.endElement(uri,localName, qName);

  }

  @Override
  public void startDocument() throws SAXException {
    System.out.println("Начало разбора документа!");
    currentObjectType="";
    super.startDocument();
  }

  @Override
  public void endDocument() throws SAXException {
    super.endDocument();
    System.out.println("Разбор документа окончен!");

  }

}
