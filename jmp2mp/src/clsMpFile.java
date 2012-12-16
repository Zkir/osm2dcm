/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 07.10.12
 * Time: 14:37
 * To change this template use File | Settings | File Templates.
 */
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class clsMpFile {
  public clsMpSection CurrentSection;
  private BufferedReader oInFile;
  private BufferedWriter oOutFile;
  private int m_intMode;
  public clsMpFile(String strFileName, int intMode) throws FileNotFoundException, IOException
  {
    m_intMode=intMode;
    // Открываем файл
    if (intMode==0){

      //oInFile = new BufferedReader(new FileReader(strFileName)  );
      oInFile = new BufferedReader(new InputStreamReader(new FileInputStream(strFileName), "windows-1251"));

    }
    if (intMode==1){
      //oOutFile = new BufferedWriter(new FileWriter(strFileName));
      oOutFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(strFileName), "windows-1251"));
    }

  }

  public void Close()  throws IOException
  {
    if (m_intMode==1){
      oOutFile.close();
    }

  }
  //Чтение секции из файла
  public boolean ReadNextSection()  throws IOException
  {

    CurrentSection = new clsMpSection();
    boolean blnSectionStarted;
    boolean blnCommentStarted;

    blnSectionStarted = false;
    blnCommentStarted = false;

    String strMpLine = null;

    while( (strMpLine = oInFile.readLine()) != null) {


      strMpLine = strMpLine.trim();



      if ( (strMpLine.equals("")) & (! blnSectionStarted)){
        if (! blnCommentStarted){
          CurrentSection.SectionType = "BLANK";
        }
        else{
          // Это комментарий
        }

        // Так или иначе пустая строчка завершает секцию
        break;
      }


      //System.out.println(strMpLine) ;
      //comment
      if (vb6.Left(strMpLine,1).equals( ";")) {
        CurrentSection.SectionType = "COMMENT";
        blnCommentStarted = true;
        CurrentSection.AddCommentLine(strMpLine);
        continue;
      }


      if ((vb6.Left(strMpLine, 1).equals("[")) & (vb6.Right(strMpLine, 1).equals("]") )) {
        if(! blnSectionStarted){
         CurrentSection.SectionType = strMpLine;
         blnSectionStarted = true;
        }
        else{
          //Конец секции
          CurrentSection.SectionEnding = strMpLine;
          break;
        }
      }
      else{
           //Отфильтруем кавычки, которые СГ не понимает
            strMpLine = strMpLine.replaceAll( "“", "");
            strMpLine = strMpLine.replaceAll( "”", "");
            strMpLine = strMpLine.replaceAll( "„", "");
            strMpLine = strMpLine.replaceAll( "«", "");
            strMpLine = strMpLine.replaceAll( "»", "");

           //Прямые удаляет сам Osm2mp

           // Антиёфикация
           strMpLine = strMpLine.replaceAll( "Ё", "Е");
           strMpLine = strMpLine.replaceAll( "ё", "е");

           //Убьем пробел перед номером
           strMpLine = strMpLine.replaceAll( "№ ", "№");

           //Номер СГ таки  не понимает.
           strMpLine = strMpLine.replaceAll( "№", "No ");

           if (!strMpLine.equals("")){
             CurrentSection.AddAttributeLine(strMpLine);
           }
        }



        }
        //System.out.println(strMpLine);
        return !(strMpLine==null);
      }

      public void WriteSection(clsMpSection Section)  throws IOException
      {

        //System.out.println("WriteSection");
        int i;
        if(Section.SectionType.equals("COMMENT") ){

            for(i=0;i<Section.oComments.size();i++ )
            {
              oOutFile.write(Section.oComments.get(i)+"\r\n");
            }
            oOutFile.write("\r\n"); //Комментарий заканчивается пустой строчкой
        }
        else
        if(Section.SectionType.equals("BLANK") ){
          oOutFile.write("\r\n");
        }
        else{
            //Сперва комментарии
            for(i=0;i<Section.oComments.size();i++ )
            {
              oOutFile.write(Section.oComments.get(i)+"\r\n");
            }

            oOutFile.write(Section.SectionType+"\r\n");
            for(i=0;i<Section.oAttributes.size();i++ )
            {
              oOutFile.write(Section.oAttributes.get(i)+"\r\n");
            }
            oOutFile.write(Section.SectionEnding+"\r\n");
        }


      }
    }
