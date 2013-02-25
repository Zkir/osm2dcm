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

    private static boolean  CheckReleaseCritria(String strValidatorReportFileName)  {

      boolean blnResult=true;
      String strQAClass;

      //Текущие значения. Их точно нужно прочесть из xml :)
      QaReport objQaReport=new QaReport(strValidatorReportFileName);


      //Получим класс качества карты
      strQAClass=GetQAClass(objQaReport);
      System.out.println("Класс качества карты: "+strQAClass);
      if (strQAClass.equals("A") || strQAClass.equals("B") || strQAClass.equals("B-"))
      {blnResult=true;}
      else
      {blnResult=false;}

      return blnResult;
    }

    private static String GetQAClass(QaReport objQaReport) {
      QAClassicator qa_class;
      qa_class=new QAClassicator("QualityCriteria.xml");

      //Класс A
      if (qa_class.testQaClass(objQaReport,"ClassA"))
      {
        return "A";
      }

      //Класс B
      if (qa_class.testQaClass(objQaReport,"ClassB"))
      {
        return "B";
      }

      //Класс B-
      if (qa_class.testQaClass(objQaReport,"ClassBm0"))
      {
        return "B-";
      }

      if (qa_class.testQaClass(objQaReport,"ClassBm1"))
      {
        return "B-";
      }

      //Класс C
      if (qa_class.testQaClass(objQaReport,"ClassC"))
      {
        return "C";
      }

      //Класс C-
      if (qa_class.testQaClass(objQaReport,"ClassCm"))
      {
        return "C";
      }

      //Класс D
      if (qa_class.testQaClass(objQaReport,"ClassD"))
      {
        return "D";
      }

      return "E";

    }



}
