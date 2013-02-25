import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 07.01.13
 * Time: 16:32
 * To change this template use File | Settings | File Templates.
 */
public class QAClassicator {
  private Document objXmlDocument;
  public QAClassicator(String strXmlQualityCriteriaFileName)
  {
    parseXmlFile(strXmlQualityCriteriaFileName);
  }

  private void parseXmlFile(String strXmlQualityCriteriaFileName){
    //get the factory
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    try {

      //Using factory get an instance of document builder
      DocumentBuilder db = dbf.newDocumentBuilder();

      //parse using builder to get DOM representation of the XML file
      objXmlDocument = db.parse(strXmlQualityCriteriaFileName);


    }catch(ParserConfigurationException pce) {
      pce.printStackTrace();
    }catch(SAXException se) {
      se.printStackTrace();
    }catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private double GetParameterValue(String strClassName, String strParamName){

    Element elmRT;
    Element elmTertiary;
    elmRT=(Element)objXmlDocument.getElementsByTagName(strClassName).item(0);
//    elmRT.getElementsByTagName("strParamName").item(0).getFirstChild().getNodeValue();
    if (elmRT.getElementsByTagName(strParamName).getLength()>0 )
    {
      return Double.parseDouble(elmRT.getElementsByTagName(strParamName).item(0).getFirstChild().getNodeValue());
    }
    else
    {
      return -1;
    }
  }

  public boolean testQaClass(QaReport objQaReport, String strClassName){
    boolean blnResult=true;

    //Эти константы по хорошему нужно прочесть из xml
    double MAX_SEALINE_BREAKS=GetParameterValue(strClassName,"MaxSealineBreaks");
    double MAX_ISOLATED_SUBGRAPHS=GetParameterValue(strClassName,"MaxIsolatedSubgraphs");
    double MAX_ISOLATED_SUBGRAPHS_TERTIARY=GetParameterValue(strClassName,"MaxIsolatedSubgraphsTertiary");
    double MAX_DEAD_ENDS=GetParameterValue(strClassName,"MaxDeadEnds");
    double MAX_ROUTINING_EDGES=GetParameterValue(strClassName,"MaxRoutiningEdges");
    double MAX_UNMATCHED_ADDR_HOUSES=GetParameterValue(strClassName,"MaxUnmatchedAddrHouses");
    double MAX_UNMATCHED_ADDR_STREETS=GetParameterValue(strClassName,"MaxUnmatchedAddrStreets");
    double MAX_UNMATCHED_ADDR_HOUSES_FIXABLE=GetParameterValue(strClassName,"MaxUnmatchedAddrHousesFixable");
    double MAX_TOTAL_NUMBER_OF_HOUSES_WADDR =GetParameterValue(strClassName,"MaxTotalAddr");



    System.out.println("");
    System.out.println("Проверяем класс " + strClassName);

    double intSealineBreaks=objQaReport.getSealineBreaks();
    double intIsolatedSubgraphs=objQaReport.getIsolatedSubgraphs();
    double intIsolatedSubgraphsTertiary=objQaReport.getIsolatedSubgraphsTertiary();
    double intDeadEnds=objQaReport.getDeadEnds();
    double intRoutiningEdges=objQaReport.getRoutiningEdges();
    double dblUnmatchedAddrHouses=objQaReport.getUnmatchedAddrHouses();
    double dblUnmatchedAddrStreets=objQaReport.getUnmatchedAddrStreets();
    double intTotalNumberOfHouses =objQaReport.getTotalNumberOfHouses();
    double dblUnmatchedAddrHousesFixable=objQaReport.getUnmatchedAddrHousesFixable();


    //Теперь собственно проверки

    if (CheckSingleCriterion("Разрывы береговой линии",intSealineBreaks,MAX_SEALINE_BREAKS)!=true)
    {
      blnResult=false;
    }

    if (CheckSingleCriterion("Изолированные рутинговые подграфы (все)",intIsolatedSubgraphs,MAX_ISOLATED_SUBGRAPHS)!=true)
    {
      blnResult=false;
    }
    if (CheckSingleCriterion("Изолированные рутинговые подграфы (tertiary)",intIsolatedSubgraphsTertiary,MAX_ISOLATED_SUBGRAPHS_TERTIARY)!=true)
    {
      blnResult=false;
    }
    if (CheckSingleCriterion("Тупики важных дорог",intDeadEnds,MAX_DEAD_ENDS)!=true)
    {
      blnResult=false;
    }

    if (CheckSingleCriterion("Общее число рутинговых ребер",intRoutiningEdges,MAX_ROUTINING_EDGES)!=true)
    {
      blnResult=false;
    }


    if (CheckSingleCriterion("Доля несопоставленных домов", dblUnmatchedAddrHouses, MAX_UNMATCHED_ADDR_HOUSES)!=true)
    {
      blnResult=false;
    }

    if (CheckSingleCriterion("Доля несопоставленных улиц", dblUnmatchedAddrStreets, MAX_UNMATCHED_ADDR_STREETS)!=true)
    {
      blnResult=false;
    }

    if (CheckSingleCriterion("Доля 'исправимых' ошибок домов", dblUnmatchedAddrHousesFixable, MAX_UNMATCHED_ADDR_HOUSES_FIXABLE)!=true)
    {
      blnResult=false;
    }


    if (CheckSingleCriterion("Общее число домов  с адресами", intTotalNumberOfHouses, MAX_TOTAL_NUMBER_OF_HOUSES_WADDR)!=true)
    {
      blnResult=false;
    }

    return blnResult;
  }

  private static boolean CheckSingleCriterion(String strCriterionName, double intValue,double intMaxValue)
  {
    boolean blnResult=false;
    if (intMaxValue==-1)
    {return true;}

    System.out.println(strCriterionName);
    System.out.print("    Имеется в карте: "+intValue);
    System.out.print("    Предельно допустимо: "+intMaxValue);
    if (intValue<=intMaxValue)
    {
      System.out.println("  Пройдено");
      blnResult=true;
    }
    else
    {
      System.out.println("  НЕ Пройдено");
      blnResult=false;
    }
    return blnResult;
  }



}
