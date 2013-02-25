/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 14.07.12
 * Time: 19:06
 * To change this template use File | Settings | File Templates.
 */

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class QaReport {

    private Document objXmlDocument;

    public int getSealineBreaks()
    {
      return Integer.parseInt(objXmlDocument.getElementsByTagName("NumberOfBreaks").item(0).getFirstChild().getNodeValue());
    }

    public int getIsolatedSubgraphs()
    {
      return Integer.parseInt(objXmlDocument.getElementsByTagName("NumberOfSubgraphs").item(0).getFirstChild().getNodeValue());
    }

    public int getIsolatedSubgraphsTertiary()
    {

      Element elmRT;
      Element elmTertiary;
      elmRT=(Element)objXmlDocument.getElementsByTagName("RoutingTestByLevel").item(0);
      elmTertiary=(Element)elmRT.getElementsByTagName("Tertiary").item(0);
      return Integer.parseInt(elmTertiary.getElementsByTagName("NumberOfSubgraphs").item(0).getFirstChild().getNodeValue());
    }

    public int getDeadEnds()
    {
      return Integer.parseInt(objXmlDocument.getElementsByTagName("NumberOfDeadEnds").item(0).getFirstChild().getNodeValue());
    }
    public int getRoutiningEdges()
    {
      return Integer.parseInt(objXmlDocument.getElementsByTagName("NumberOfRoutingEdges").item(0).getFirstChild().getNodeValue()) ;
    }
    public double getUnmatchedAddrHouses()
    {
      String strResult;
      strResult=objXmlDocument.getDocumentElement().getElementsByTagName("ErrorRate").item(0).getFirstChild().getNodeValue();

      return Double.parseDouble(strResult);
    }
    public double getUnmatchedAddrStreets()
    {
      double dblTotalStreets;
      double dblStreetsOutsideCities;
      dblStreetsOutsideCities=Double.parseDouble(objXmlDocument.getDocumentElement().getElementsByTagName("StreetsOutsideCities").item(0).getFirstChild().getNodeValue());
      dblTotalStreets=Double.parseDouble(objXmlDocument.getDocumentElement().getElementsByTagName("TotalStreets").item(0).getFirstChild().getNodeValue());
      return dblStreetsOutsideCities/dblTotalStreets;
    }
    public double getUnmatchedAddrHousesFixable()
    {
      double FixableErrors;
      FixableErrors=0;
      FixableErrors=FixableErrors+Double.parseDouble(objXmlDocument.getDocumentElement().getElementsByTagName("HousesWOCities").item(0).getFirstChild().getNodeValue()) ;
      FixableErrors=FixableErrors+Double.parseDouble(objXmlDocument.getDocumentElement().getElementsByTagName("HousesStreetNotFound").item(0).getFirstChild().getNodeValue());
      FixableErrors=FixableErrors+Double.parseDouble(objXmlDocument.getDocumentElement().getElementsByTagName("HousesStreetNotRelatedToCity").item(0).getFirstChild().getNodeValue()) ;

      return FixableErrors/getTotalNumberOfHouses();
    }
    public int getTotalNumberOfHouses()
    {
      return Integer.parseInt(objXmlDocument.getElementsByTagName("TotalHouses").item(0).getFirstChild().getNodeValue()) ;

    }
    public QaReport(String strXmlReportFileName)
    {
        parseXmlFile(strXmlReportFileName);
    }

    private void parseXmlFile(String strXmlReportFileName){
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            objXmlDocument = db.parse(strXmlReportFileName);


        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }catch(SAXException se) {
            se.printStackTrace();
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
