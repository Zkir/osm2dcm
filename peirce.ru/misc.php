<?php
#============================================
#Cтраница с картами
#(c) Zkir 2010
#============================================
include("ZSitePage.php");

  $zPage=new TZSitePage;
	
  $zPage->title="Зарубежные страны - карты OSM для Ситигида";
  $zPage->header="Карты OSM зарубежных стран для Ситигида";

  $xml1 = simplexml_load_file("maplist.xml"); //Карты России, osm1
//  $xml2 = simplexml_load_file("maplist1.xml"); //Карты зарубежья, osm2
  $xml3 = simplexml_load_file("maplist_old.xml"); //Ручные карты

  $zPage->WriteHtml("<H1>Карты OSM зарубежных стран для СитиГида</H1>");
  $zPage->WriteHtml( "<H2>Карты с пробками</H2>");  
  PrintMapListOld ($xml3,"Карты с пробками");

  $zPage->WriteHtml( "<H2>Ближнее зарубежье </H2>");  
  PrintMapList ($xml1,"Ближнее Зарубежье");

  $zPage->WriteHtml( "<H2>Дальнее зарубежье </H2>");
  PrintMapList ($xml1,"Дальнее Зарубежье");

//  $zPage->WriteHtml( "<H2>Разное </H2>");
//  PrintMapListOld ($xml3,"Разное");
 


  
  #Выведем содержимое, применяя шаблон.
  $zPage->Output();


Function GetGroup($strCountryCode)
{
    switch ($strCountryCode)
    {
        case 'RU':
            $result="Россия";
            break;
        case "AZ":
        case "AM":
        case "BY":
        case "GE":
        case "KZ":
        case "KG":
        case "MD":
        case "UA":
        case "FI":
        case "LV":
        case "LT":
        case "EE": 
        case "UZ":         	
            $result="Ближнее Зарубежье";
            break;
        default:
            $result="Дальнее Зарубежье";
            break;
    }

return $result;
}

function PrintMapList($xml, $strGroup)
{
   global $zPage;
   $zPage->WriteHtml( '<table width="500px" class="sortable">
          <tr>
            <td><b>Код</b></td>
            <td width="200px"><b>Имя файла</b></td>
            <td><b>Дата обновления</b></td>
            <td><b>Cкачать<b></td>
          </tr>');

  foreach ($xml->map as $item)
    {

      //if(trim($item->group)==$strGroup)
      if( $strGroup==GetGroup(substr($item->code,0,2))
        )
      {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$item->code.'</td>');
        $zPage->WriteHtml( '<td width="200px">'.$item->name.'</td>');
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
      if(trim($item->group)==$strGroup AND substr($item->code,0,2)<>'RU')
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