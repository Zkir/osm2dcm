<?php
#============================================
#Ежедневные сборки
#(c) Zkir 2010
#============================================
include("ZSitePage.php");

  $zPage=new TZSitePage;
  $zPage->title="Валидатор адресов";
  $zPage->header="Валидатор адресов";

  $mapid=$_GET['mapid'];
  $errtype=$_GET['errtype'];

  $zPage->WriteHtml( "<h1>Валидатор адресов</h1>");
  $zPage->WriteHtml('<p align="right"><a href="/addr.php?mapid='.$mapid.'">Назад к таблице</a> </p>' );
  $zPage->WriteHtml('<p>Отображение на карте пока в тестовом режиме, прошу строго не судить :)</p>');
  $zPage->WriteHtml('<p>По клику на маркере открывается JOSM, он должне быть запущен.</p>');
  if($mapid!="")
  {
    PrintMap($mapid,$errtype);
  }
  else
  {
    PrintMap("RU-MOS","");    
  }


$zPage->WriteHtml('
<div style="display: none;">
<iframe name="josm"></iframe>
</div>');

 $zPage->Output("1");

/* =============================================================================
     Разные полезные фукции
===============================================================================*/
//Имя файла ошибок по коду
function GetXmlFileName($mapid)
{
  return "/ADDR_CHK/".$mapid.".mp_addr.xml";
}

//Ссылка на Josm
function MakeJosmLink($lat,$lon)
{
  $delta = 0.0001;

  $Link = "http://localhost:8111/load_and_zoom?top=".((float)$lat + $delta)."&bottom=".((float)$lat - $delta)."&left=".((float)$lon - $delta)."&right=".((float)$lon + $delta);

  return $Link;
}

//Ссылка на Potlatch
function MakePotlatchLink($lat,$lon)
{
  $Link = "http://openstreetmap.org/edit?lat=".$lat."&lon=".$lon."&zoom=17";
  return $Link;
}

function MakeWikiLink($city)
{
	return "http://ru.wikipedia.org/wiki/".$city;
}

function PrintMap($mapid,$errtype)
{
  global $zPage;
  $zPage->WriteHtml('
  <div id="cm-example" style="width: 100%; height: 600px"></div> 
  <script type="text/javascript" src="http://tile.cloudmade.com/wml/latest/web-maps-lite.js"></script> 
	   
  <script type="text/javascript" src="http://peirce.gis-lab.info/js/addr-map.js"> </script> 
  <script type="text/javascript">
      ProcessMap("ADDR_CHK/'.$mapid.'.mp_addr.xml","'.$errtype.'");
   </script> 
  <img id="ttt" src="" style="display:none;" alt="dummy link for Josm"/> ');
   
  //Классификатор ошибок
  $zPage->WriteHtml( '<h2><a name="errdescr">Объяснение типов ошибок</a></h2>');
  $zPage->WriteHtml( '<small><table>');
  for ($i=1;$i<=6;$i++)
  {
    $zPage->WriteHtml( '<tr>');
    $zPage->WriteHtml( '<td valign="top"><b>'.FormatAddrErrType($i).'</b></td>');
    $zPage->WriteHtml( '<td valign="top">'.FormatAddrErrName($i).'</td>');
    $zPage->WriteHtml( '<td>'.FormatAddrErrDesc($i).'</td>');
    $zPage->WriteHtml( '</tr>');
  }
  $zPage->WriteHtml( '</table></small>');

}
	
//Страница деталей области.
function PrintAddresses($mapid)
{
  global $zPage;
  $zPage->WriteHtml('<P align=right><a href="/addr.php">Назад к списку регионов</a> <p>' );

  $xml = simplexml_load_file(GetXmlFileName($mapid));
  $zPage->WriteHtml("<h2>Сводка </h2>" );
  $zPage->WriteHtml("<table>" );
  $zPage->WriteHtml("<tr><td>Код карты</td><td><b>".$mapid."</b></td></tr>" );
  $zPage->WriteHtml("<tr><td>Всего адресов</td><td>".$xml->Summary->TotalHouses."</td></tr>" );
  $zPage->WriteHtml("<tr><td>Всего улиц</td><td>".$xml->Summary->TotalStreets."</td></tr>" );
  $zPage->WriteHtml("<tr><td>Не сопоставлено адресов</td><td>".$xml->Summary->UnmatchedHouses."</td></tr>" );
  $zPage->WriteHtml("<tr><td>Домов без городов</td><td>".$xml->Summary->HousesWOCities."</td></tr>" );
  $zPage->WriteHtml("<tr><td>Доля несопоставленых адресов</td><td>".number_format(100.00*(float)$xml->Summary->ErrorRate,2,'.', ' ')."%</td></tr>" );
  $zPage->WriteHtml("</table>" );
  $zPage->WriteHtml("<p/>" );

/*==========================================================================
                 Города без полигональных границ
============================================================================*/
  $zPage->WriteHtml("<h2>Города без полигональных границ</h2>");
  $zPage->WriteHtml('<p>Наличие полигональных границ (полигона с place=* и name=* или
                     place_name=*, такими же, что и на точке города) критически важно
                     для адресного поиска. По ним определяется принадлежность домов и улиц
                     населенным пунктам.<br/> В данный список включены города (place=city и place=town),
                     для которых полигональные границы не обнаружены.
                     Деревни (place=village|hamlet) в этом списке не показываются,
                     поскольку деревень может быть очень много, но, если в деревне
                     есть улицы и дома, полигональные границы так же нужны. Все дома вне НП будут
                     показаны в  секции "Несопоставленные адреса" ниже.
                      </p>');
  $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>
                  <td><b>Название</b></td>
                  <td width="100px" align="center"><b>Править <br/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <br/>в Potlach</b></td>
         </tr>');

  foreach ($xml->CitiesWithoutPlacePolygon->City as $item)
    {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$item->City.'</td>');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->lat,$item->Coord->lon).'" target="josm"> <img src="img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->lat,$item->Coord->lon) .'" target="_blank"><img src="img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
     }
  $zPage->WriteHtml( '</table>');
/*==========================================================================
                 Города без населения
============================================================================*/
  $zPage->WriteHtml("<h2>Города без населения</h2>");
  $zPage->WriteHtml('<p>Наличие населения (тега population=*) непосредственно на
                     адресный поиск не влияет. Тем не менее, указание населения крайне
                     желательно, поскольку от него зависит размер надписи города.
                      </p>');
  $zPage->WriteHtml("<p/>" );

  $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>
                  <td><b>Название</b></td>
                  <td width="100px" align="center"><b>Править <br/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <br/>в Potlach</b></td>
         </tr>');

  foreach ($xml->CitiesWithoutPopulation->City as $item)
    {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td><a href="'.MakeWikiLink($item->City).'" target="_blank">'.$item->City.'</a></td>');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->lat,$item->Coord->lon).'" target="josm"> <img src="img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->lat,$item->Coord->lon) .'" target="_blank"><img src="img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
     }
  $zPage->WriteHtml( '</table>');

/*==========================================================================
                 Несопоставленные адреса
============================================================================*/
  $zPage->WriteHtml("<h2>Несопоставленные адреса </h2>" );
  $zPage->WriteHtml('<p> Данный валидатор показывает какие дома/адреса <b>не</b>
                     попадают в адресный поиск после конвертации карт в СитиГид. <br/>
                     Пустая улица означает, что на доме не заполнено поле addr:street.
                     Пустой город означает, что дом находится вне полигона place=*.');
  $zPage->WriteHtml("<p/>" );
  if ($xml->Summary->HousesWOCities>5000)
  {
  	  $zPage->WriteHtml( '<p><b>К сожалению, ошибок настолько много, что отобразить их все невозможно. 
  	                      Следует сначала починить полигональные границы городов.</b></p>');
  }
  else
  {	  
  $zPage->WriteHtml( '<P>И, между прочим, таблица сортируется. Нужно кликнуть на заголовок столбца. </P> ');


  $zPage->WriteHtml( '<table width="900px" class="sortable">

   	    <tr>
                  <td><b>Город</b></td>
                  <td><b>Улица</b></td>
                  <td><b>Номер дома </b></td>
                  <td width="100px" align="center"><b>Править <br/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <br/>в Potlach</b></td>
         </tr>');

  foreach ($xml->ErrorList->House as $item)
    {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$item->City.'</td>');
        $zPage->WriteHtml( '<td>'.$item->Street.'</td>');
        $zPage->WriteHtml( '<td>'.$item->HouseNumber.'</td>');
        if ($item->JosmLink<>"")
        {
          $zPage->WriteHtml( '<td> <a href="'.$item->JosmLink.'" target="josm"> <img src="img/josm.png"/></a> </td> ');
          $zPage->WriteHtml( '<td> - </td> ');
        }
        else
        {
          $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->lat,$item->Coord->lon).'" target="josm"> <img src="img/josm.png"/></a> </td> ');
          $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->lat,$item->Coord->lon) .'" target="_blank"><img src="img/potlach.png"/></a> </td> ');
        }
        $zPage->WriteHtml( '</tr>');
     }

  $zPage->WriteHtml( '</table>');
  }
}

function PrintAddressesSummary()
{
   global $zPage;

   //Cписок пока строим по статистике
   $xml = simplexml_load_file("statistics.xml");

   $zPage->WriteHtml( '<table width="900px" class="sortable">

   	    <tr>
                  <td width="80px"><b>Код</b></td>
                  <td width="180px"><b>Карта</b></td>
                  <td><b>Всего<br/> адресов</b></td>
                  <td><b>Всего<br/> улиц</b></td>
                  <td><b>Не сопоставлено<br/> адресов</b></td>
                  <td><b>Домов <br/> вне НП</b></td>
                  <td><b>Доля<br/> битых<br/> адресов, %</b></td>
                  <td><b>Ошибки</b></td>

                 <!-- <td><b>Дата</b></td> -->
         </tr>');

  foreach ($xml->mapinfo as $item)
    {
      if(  substr($item->MapId,0,2)=='RU' )
      {
        $xmlfilename=GetXmlFileName($item->MapId);

        if(file_exists($xmlfilename))
        {
          $xml_addr = simplexml_load_file($xmlfilename);
          $zPage->WriteHtml( '<tr>');
          $zPage->WriteHtml( '<td>'.$item->MapId.'</td>');
          $zPage->WriteHtml( '<td>'.$item->MapName.'</td>');
          $zPage->WriteHtml( '<td>'.$xml_addr->Summary->TotalHouses.'</td>' );
          $zPage->WriteHtml( '<td>'.$xml_addr->Summary->TotalStreets.'</td>' );
          $zPage->WriteHtml( '<td>'.$xml_addr->Summary->UnmatchedHouses.'</td>');
          $zPage->WriteHtml( '<td>'.$xml_addr->Summary->HousesWOCities.'</td>' );
          $zPage->WriteHtml( '<td>'.number_format(100.00*(float)$xml_addr->Summary->ErrorRate,2,'.', ' ').'</td>');
          $zPage->WriteHtml( '<td><a href="/addr.php?mapid='.$item->MapId.'">посмотреть</a></td>');

          $zPage->WriteHtml( '</tr>');
        }



      }
    }

  $zPage->WriteHtml( '</table>');
}

function FormatAddrErrType($number)
{
$str="?";
switch ($number) {
case 0:
    $str="-";
    break;
case 1:
    $str="I";
    break;
case 2:
    $str="II";;
    break;
case 3:
    $str="III";;
    break;
case 4:
    $str="IV";;
    break;
case 5:
    $str="V";;
    break;
case 6:
    $str="VI";;
    break;
}
	
return $str;
}

function FormatAddrErrName($number)
{
$str="?";
switch ($number) {
case 0:
    $str="-";
    break;
case 1:
    $str="Дом вне НП";
    break;
case 2:
    $str="Улица не задана";;
    break;
case 3:
    $str="Улица не найдена";;
    break;
case 4:
    $str="Улица не связана с городом";;
    break;
case 5:
    $str="Дом номеруется по территории";
    break;
case 6:
    $str="Улица не является рутинговой в СГ";
    break;
}
	
return $str;
}
function FormatAddrErrDesc($number)
{
$str="?";
switch ($number) {
case 0:
    $str="-";
    break;
case 1:
    $str='<b>В чем проблема:</b> дом находится вне границ населенного пункта, обозначенных полигоном place=city|town|village|hamlet. <br/>
          <b>Как починить:</b> проверить наличие полигона place, в случае отсутствия добавить.';
    break;
case 2:
    $str='<b>В чем проблема:</b> тег addr:street на доме не заполнен. 
          <b>Как починить:</b> добавить addr:street. ';
    break;
case 3:
    $str='<b>В чем проблема:</b> улица, указанная на доме, в данном НП не обнаружена. Скорее всего это опечатка, например "улица Гибоедова" вместо
          "улицы Грибоедова" или разнобой в порядке статусной части: "проспект Космонавтов" на доме и "Космонавтов проспект" на улице.<br/> 
           <b>Как починить:</b> сделать, чтобы в  addr:street дома было в точности равно name соответствующей улицы.';
    break;
case 4:
    $str='<b>В чем проблема:</b> улица, указанная в теге addr:street дома найдена в некоторой окресности, но она не связана с городом.
          Обычно так бывает, когда значительная часть улицы оказалась вне границ НП (полигона place), или когда  начало и конец улицы лежат в разных населенных пунктах.<br/>
          <b>Как починить:</b> следует проверить границу города. Если граница города правильная, следует разделить вей улицы, создав в месте раздела общую точку с границей НП, так, что бы улица находилась внутри границ НП.
          При этом нужно убрать name c части вея, оставшегося вне НП. Если же граница города неправильная, следует ее откорректировать, чтобы улицы города находились внутри города. ';
    break;
case 5:
    $str='<b>В чем проблема:</b> дом имеет адрес вида <i>город N., 6-й микрорайон, дом 77</i>, т.е. топоним, указанный в addr:street означает не улицу,
          а район, квартал, или некую местность. <br/> 
           Часть адресов такого типа может попадать в категорию III, 
           потому что анализ данного типа ошибок частично эвристический. <br/> 
          <b>Как починить:</b> никак, поддержки адресов такого типа в СитиГиде нет. ';
    break;
case 6:
    $str="<b>В чем проблема:</b> улица с таким названием есть в OSM, но не является рутиговой в СитиГиде. На данный момент это 
          highway=service и highway=pedestrian.<br/>
          <b>Как починить:</b> следует проверить, насколько обосновано улице присвоен статус service.
          Обычно наличие собственного названия и домов с адресами по этой улице есть некий аргумент в поддержку того,
          что это именно улица (highway=residential), а не дворовый проезд (highway=service). 
          Пешеходные улицы (highway=pedestrian) трогать не рекомендуется.";
    break;
}
	
return $str;
}
?>
