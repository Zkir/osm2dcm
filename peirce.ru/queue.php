<?php
#============================================
#Ежедневные сборки
#(c) Zkir 2010
#============================================	
include("ZSitePage.php");
require_once("include/misc_utils.php"); 


   // Задаем текущий язык проекта
    //putenv("LANG=ru_RU"); 
    //putenv("LANG=en_US"); 
    putenv("LANG=pt"); 
    

    // Задаем текущую локаль (кодировку)
    //setlocale (LC_ALL,"Russian");
    setlocale (LC_ALL, "pt_BR");

    // Указываем имя домена
    $domain = 'default';

    // Задаем каталог домена, где содержатся переводы
    bindtextdomain ($domain, "./locale");

    // Выбираем домен для работы

    textdomain ($domain);

    // Если необходимо, принудительно указываем кодировку
    // (эта строка не обязательна, она нужна,
    // если вы хотите выводить текст в отличной
    // от текущей локали кодировке).
    bind_textdomain_codeset($domain, 'UTF-8');


  $zPage=new TZSitePage;
  $zPage->title=_("Очередь на конвертацию");
  $zPage->header=_("Очередь на конвертацию");
  
   if(1==0)
   {
   
     $zPage->WriteHtml( "<H1>"._("Очередь на конвертацию")."</H1>");
     $zPage->WriteHtml('<p>На этой странице представлена очередь и политика обновления карт. </p>');
     $zPage->WriteHtml('<h2>Очередь</h2>');	
     //$zPage->WriteHtml("Процесс ОСТАНОВЛЕН");
     $zPage->WriteHtml('<p> <b><i>К сожалению, валидатор и конвертор в настоящее время <u>остановлены</u>. Будем надеяться, что временно) </i></b> </p>');
     $zPage->Output(1);
     return;
   }
   

  $xml = simplexml_load_file("queue.xml"); //Интерпретирует XML-файл в объект

  //найдем показатели латентности
  $intNumberOfMaps=0;
  $intQAIndex1=0;
  $intQAIndex1Max=0;
  $strMapID1Max='';
  
  $intNumberOfMaps2=0;
  $intQAIndex2=0;
  $intQAIndex2Max=0;
  $strMapID2Max='';
  
  foreach ($xml->map as $item)
    {   
      if ($item->date!="")
      {  	
        //Группа весь мир
        $intDateDiff=DateDiff2($item->date);
        //$intDateDiff=DateDiff2($item->last_try_date);
        if ($intDateDiff>$intQAIndex1Max)
        {
          $intQAIndex1Max=$intDateDiff;
          $strMapID1Max=$item->code;	
        }        	
        $intQAIndex1=$intQAIndex1+$intDateDiff;
        $intNumberOfMaps++;
        
        //Группа Россия
        if (substr($item->code,0,2)=='RU')
        {
          if ($intDateDiff>$intQAIndex2Max)
          {
            $intQAIndex2Max=$intDateDiff;
            $strMapID2Max=$item->code;	
          }        	
          $intQAIndex2=$intQAIndex2+$intDateDiff;
          $intNumberOfMaps2++;
        }  
      }
    }  
             
  $intQAIndex1= (float)$intQAIndex1/(float)$intNumberOfMaps;
  $intQAIndex2= (float)$intQAIndex2/(float)$intNumberOfMaps2;
	  
  $zPage->WriteHtml( "<H1>"._("Очередь на конвертацию")."</H1>");
  $zPage->WriteHtml('<p>На этой странице представлена очередь и политика обновления карт. </p>');
  $zPage->WriteHtml('<h2>Параметры конвертации</h2>');
  $zPage->WriteHtml('<p>Число используемых процессорных ядер: <b>'.$xml->attributes()->ncores.' </b> </p>');
  $zPage->WriteHtml('<p>Номинальный цикл обновления карт: <b>'.$xml->attributes()->cycle.'</b> дней </p>');
  $zPage->WriteHtml('<p>Минимальное общее время, требующееся на полную пересборку всех карт: <b>'.$xml->attributes()->totaltime.'</b> дней </p>');
  $zPage->WriteHtml('<p>Показатель латентности по группе "Россия": <b>'.number_format($intQAIndex2,1,'.', ' ').'</b> дней. Максимальный: '.number_format($intQAIndex2Max,1,'.', ' ').' дней ('.$strMapID2Max.') </p>');
  $zPage->WriteHtml('<p>Показатель латентности по группе "Весь Мир": <b>'.number_format($intQAIndex1,1,'.', ' ').'</b> дней. Максимальный: '.number_format($intQAIndex1Max,1,'.', ' ').' дней ('.$strMapID1Max.') </p>');
  
  $zPage->WriteHtml('<h2>Очередь</h2>');
  
  $group=$_GET['group'] ?? '';
  $zPage->WriteHtml("<small>Таблица сортируется, достаточно кликнуть на заголовок столбца</small>");
  PrintQueue ($xml,$group);

  $zPage->WriteHtml('<p>Зеленый цвет строки означает, что для карты получены свежие данные из OSM и ожидается сборка. </p>');
  $zPage->WriteHtml('<p>Желтый цвет означает, что ожидается обновление OSM-данных.</p>');
  $zPage->WriteHtml('<p>Красный цвет означает, что карта ожидает постановки в очередь в соответствии с расписанием.</p>');
  $zPage->WriteHtml('<p>Темно-красный цвет означает, что сборка карты невозможна или не планируется.</p>');
  $zPage->WriteHtml('<p>Темно-красный цвет даты означает, что предыдущая сборка завершилась неудачно.</p>'); 
  
  $zPage->Output(1);
  

function PrintQueue($xml)
{
   global $zPage;
   $zPage->WriteHtml( '<table  class="sortable">
          <tr style="background: #AFAFAF">
            <td width="40px"><b>№</b></td>
            <td width="100px"><b>Код</b></td>
            <td width="350px"><b>Имя файла</b></td>
            <td width="150px"><b>Дата предыдущей успешной сборки</b></td>
            <td width="70px"><b>Время сборки, мин.</b></td>
            <td width="150px"><b>Ожидаемая дата следующей сборки<b></td>
            <td width="100px"><b>Статус</b></td>
          </tr>');
  $i=0;
  foreach ($xml->map as $item)
    {   
        $i++; 
    	if ($item->planned==1)
          {$strStyle="background: #DDFFCC";}
        else if ($item->planned==2)
          {$strStyle="background: #FFFF60";}
        else if ($item->planned==3)
          {$strStyle="background: #FFDDBB";}
        else
          {$strStyle="background: #FFA090";}
        
        $zPage->WriteHtml( '<tr style="'.$strStyle.'">');
        $zPage->WriteHtml( '<td align="center">'.$i.'</td>');
        $zPage->WriteHtml( '<td >'.$item->code.'</td>');
        $zPage->WriteHtml( '<td >'.$item->name_ru.'</td>');
        $zPage->WriteHtml( '<td >'.$item->date.'</td>');
        $zPage->WriteHtml( '<td>'.$item->time.'</td>');
        
        if (((string) ($item->last_try_date) <= (string) ($item->date)) and ($item->last_try_date!=''))
        {	
          //Все клево 	 
          $zPage->WriteHtml( '<td>'.$item->next_date.'</td>');
          $zPage->WriteHtml( '<td>OK</td>');
         
        }
        else   
        {	
          //Предыдущая сборка завершилась неудачно	
          $zPage->WriteHtml( '<td style="background: #FFA090">'.$item->next_date.'</td>');
          $zPage->WriteHtml( '<td style="background: #FFA090">FAILED. <a href="/qa/'.$item->code.'">QA</a></td>');
        }

       
        $zPage->WriteHtml( '</tr>');

    }

  $zPage->WriteHtml( '</table>');
}


function DateDiff2($StartDate)
{
  $Y1=substr($StartDate,0,4);
  $M1=substr($StartDate,5,2);
  $D1=substr($StartDate,8,2);
  
  $HH=substr($StartDate,11,2);;
  $MM=substr($StartDate,14,2);;
  //echo "$Y1-$M1-$D1 $HH:$MM, <br />";
  
  $current_date =time();//mktime (0,0,0,date("m") ,date("d"),date("Y"));  //текущее время 
  $old_date = mktime ($HH,$MM,0,$M1,$D1,$Y1); //2004.11.25
  $difference = ($current_date - $old_date); //разница в секундах
  $difference_in_days = ($difference / 86400); //разница в днях
  
 // echo "$StartDate, $EndDate, $difference_in_days  <br /> ";
  return $difference_in_days;
}


?>
