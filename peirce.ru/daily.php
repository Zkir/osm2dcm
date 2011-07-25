<?php
#============================================
#Ежедневные сборки
#(c) Zkir 2010
#============================================	
include("ZSitePage.php");

  $zPage=new TZSitePage;
  $zPage->title="Ежедневные сборки карт OSM для Ситигида";
  $zPage->header="Ежедневные сборки карт OSM для Ситигида";

  $xml = simplexml_load_file("maplist.xml"); //Интерпретирует XML-файл в объект
  $xml3 = simplexml_load_file("maplist_old.xml"); //Ручные карты

  $zPage->WriteHtml( "<H1>Россия</H1>");
  $zPage->WriteHtml( "<H2>Карты с пробками</H2>");  
  PrintMapListOld ($xml3,"Карты с пробками");

  $zPage->WriteHtml( "<H2>Ежедневные сборки карт OSM для СитиГида</H2>");
  PrintMapList ($xml,"Россия");
  
 // $zPage->WriteHtml( "<H2>Зарубежье</H2>");
 // PrintMapList ($xml,"Зарубежье");
  
  
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
      if( ($strGroup=='Россия' AND substr($item->code,0,2)=='RU') OR 
      	  ($strGroup=='Зарубежье' AND substr($item->code,0,2)<>'RU')   )
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
