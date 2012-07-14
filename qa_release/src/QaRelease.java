/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 14.07.12
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */

/*
* Программа проверки качества выпускаемой карты.
* Только карты, удовлетворяющие определенным критериям качества, должны попадать широкой публике
* На данный момент критерии такие:
*  Общее
*   Разрывов береговой линии  - 0
* Рутинговый граф
*   Общее число изолятов - не более 50
*   Общее число изолятов в основном дорожном графе (начиная с tertiary) - не более 5
*   Тупиков важных дорог - не более 5
*   Общее число рутинговых ребер - не более 300000 (триста тысяч)
* Адресный реестр
*   Доля несопоставленных домов - не более 15%
*   Доля несопоставленных улиц - не более 15%
 */
public class QaRelease {
    public static void  main(String args[]){
        int intExitCode=0;
        System.out.println("Программа проверки качества выпускаемой карты");
        try{
          if (args.length!=1 )
          {
            throw new Exception("Следует указать имя xml-файла с результатом валидатора в качестве аргумента ком. строки");
          }
          System.out.println("Входной файл: "+ args[0]);
          if (CheckReleaseCritria(args[0]))
          {
            System.out.println("Проверка пройдена");
            intExitCode=0;
          }
          else
          {
            System.out.println("Проверка НЕ пройдена");
            intExitCode=1;
          }


        }
        catch (Exception e)
        {
            System.out.println("Внутренняя ошибка в программе: " + e.toString() );
            intExitCode=99;
        }
        //По завершении программы выдается код завершения
        //0 - Карта соответствует критерию выпуска.
        //1 - Карта не соответствует критерию выпуска
        //99 - Внутренняя ошибка
        System.exit(intExitCode);
    }
    /**
    *  Общее
    *   Разрывов береговой линии  - 0
    * Рутинговый граф
      *   Общее число изолятов - не более 50
      *   Общее число изолятов в основном дорожном графе (начиная с tertiary) - не более 5
      *   Тупиков важных дорог - не более 10
      *   Общее число рутинговых ребер - не более 300000 (триста тысяч)
    * Адресный реестр
      *   Доля несопоставленных домов - не более 15%
      *   Доля несопоставленных улиц - не более 15%
     */
    private static boolean  CheckReleaseCritria(String strValidatorReportFileName)  {
      boolean blnResult=true;

      //Эти константы по хорошему нужно прочесть из xml
      final int MAX_SEALINE_BREAKS=0;
      final int MAX_ISOLATED_SUBGRAPHS=50;
      final int MAX_ISOLATED_SUBGRAPHS_TERTIARY=5;
      final int MAX_DEAD_ENDS=10;
      final int MAX_ROUTINING_EDGES=300000;
      final double MAX_UNMATCHED_ADDR_HOUSES=0.15;
      final double MAX_UNMATCHED_ADDR_STREETS=0.15;

      //Текущие значения. Их точно нужно прочесть из xml :)
      //TODO: read xml
      QaReport objQaReport=new QaReport(strValidatorReportFileName);


      int intSealineBreaks=objQaReport.getSealineBreaks();
      int intIsolatedSubgraphs=objQaReport.getIsolatedSubgraphs();
      int intIsolatedSubgraphsTertiary=objQaReport.getIsolatedSubgraphsTertiary();
      int intDeadEnds=objQaReport.getDeadEnds();
      int intRoutiningEdges=objQaReport.getRoutiningEdges();
      double dblUnmatchedAddrHouses=objQaReport.getUnmatchedAddrHouses();
      double dblUnmatchedAddrStreets=objQaReport.getUnmatchedAddrStreets();

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
      if (CheckSingleCriterionF("Доля несопоставленных домов",dblUnmatchedAddrHouses,MAX_UNMATCHED_ADDR_HOUSES)!=true)
      {
        blnResult=false;
      }
      if (CheckSingleCriterionF("Доля несопоставленных улиц",dblUnmatchedAddrStreets,MAX_UNMATCHED_ADDR_STREETS)!=true)
      {
        blnResult=false;
      }

      return blnResult;
    }

    private static boolean CheckSingleCriterion(String strCriterionName, int intValue,int intMaxValue)
    {
        boolean blnResult=false;
        System.out.println(strCriterionName);
        System.out.println("    Имеется в карте: "+intValue);
        System.out.println("    Предельно допустимо: "+intMaxValue);
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

    private static boolean CheckSingleCriterionF(String strCriterionName, double intValue,double intMaxValue)
    {
        boolean blnResult=false;
        System.out.println(strCriterionName);
        System.out.println("    Имеется в карте: "+intValue);
        System.out.println("    Предельно допустимо: "+intMaxValue);
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
