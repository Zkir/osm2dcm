<?php
#============================================
#Ежедневные сборки
#(c) Zkir 2010
#============================================	
include("ZSitePage.php");
require_once("include/misc_utils.php"); 

  $zPage=new TZSitePage;
  $zPage->title="Карты для Ситигид 5.x";
  $zPage->header="Карты для Ситигид 5.x";

  $xml = simplexml_load_file("maplist_5.xml"); //Интерпретирует XML-файл в объект
  $xml3 = simplexml_load_file("maplist_old.xml"); //Ручные карты

  //$zPage->WriteHtml( "<H1>Карты для Ситигид 5.x</H1>");
  //$zPage->WriteHtml( "<H2>Карты с пробками</H2>");  
  //PrintMapListOld ($xml3,"Карты с пробками");
  
  $zPage->WriteHtml( '<P> <img src="img/peirce.jpg" height="65px"  style="float:left;"
                        title="Чарльз Сандерс Пирс - знаменитый американский ученый, философ, логик и картограф"> На этой странице представлены карты OSM для СитиГид 5.x. 
                      Поскольку 5.x является устаревшей версией, эти карты больше обновляться не будут. Настоятельно рекомендуется обновиться до версии 7.x </p>');

  $zPage->WriteHtml( "<H2>Россия</H2>");
  PrintMapList ($xml,"Россия");
  
  $zPage->WriteHtml( "<H2>Ближнее зарубежье </H2>");  
  PrintMapList ($xml,"Ближнее Зарубежье");

  $zPage->WriteHtml( "<H2>Дальнее зарубежье </H2>");
  PrintMapList ($xml,"Дальнее Зарубежье");
  
  
  
 $zPage->Output();


function PrintMapList($xml, $strGroup)
{
   global $zPage;
   $zPage->WriteHtml( '<table width="500px" class="sortable">
          <tr>
            <td width="80px"><b>Код</b></td>
            <td width="200px"><b>Имя файла</b></td>
            <td><b>Дата обновления</b></td>
            <td><b>Cкачать<b></td>
          </tr>');

  foreach ($xml->map as $item)
    {

      //if(trim($item->group)==$strGroup)
      if( $strGroup==GetMapGroup(substr($item->code,0,2))
        )
      {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td >'.$item->code.'</td>');
        $zPage->WriteHtml( '<td >'.$item->name.'</td>');
        $zPage->WriteHtml( '<td>'.$item->date.'</td>');
       // $zPage->WriteHtml( '<td><a href="'.$item->url.'"> скачать</a></td> </tr>');
        $zPage->WriteHtml( '<td><a href="/download.php?mapid='.$item->code.'"> скачать</a></td> </tr>');
       
        $zPage->WriteHtml( '</tr>');
      }
    }

  $zPage->WriteHtml( '</table>');
}
function PrintMapListOld($xml, $strGroup)
{
  global $zPage;	 
   $zPage->WriteHtml( '<table width="400px" class="sortable">
          <tr>
            <td width="80px"><b>Код</b></td>
            <td width="200px"><b>Имя файла</b></td><td><b>Дата обновления</b></td>
            <td><b>Cкачать<b></td>
          </tr>');


  foreach ($xml->map as $item)
    {
      if(trim($item->group)==$strGroup AND substr($item->code,0,2)=='RU')
      {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$item->code.'</td>');
        $zPage->WriteHtml( '<td width="200px">'.$item->name.'</td>');
        $zPage->WriteHtml( '<td>'.$item->date.'</td>');
        $zPage->WriteHtml( '<td><a href="'.$item->url.'"> скачать</a></td> </tr>');
        $zPage->WriteHtml('</tr>');
      }
    }

  $zPage->WriteHtml( '</table>');
}


?>
