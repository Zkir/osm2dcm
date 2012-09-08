<?php
#============================================
#Валидатор адресов
#(c) Zkir 2010
#============================================
include("ZSitePage.php");
require_once("include/misc_utils.php"); 

  $zPage=new TZSitePage;
 
  $page=$_GET['page'];
  $mapid=$_GET['mapid'];
  $errtype=$_GET['errtype'];
  
  
  
  switch($page)
  {
   	  
    case 'routing':
      $zPage->title="Контроль качества, рутинг  - $mapid";
      $zPage->header="Контроль качества ($mapid, рутинг)";	
	  $zPage->WriteHtml('<h1>'.($zPage->header).'</h1>');
	  break;
    
    //Cводка по адресам (вспомогательная страница)  
    case 'addr_summary':
      PrintAddressSummaryPage();
      break;
      
    case 'qa_summary':         
    default:
      
      if($mapid!="")
      {
  	    $zPage->title="Контроль качества  - $mapid";
        $zPage->header="Контроль качества - $mapid";
        PrintQADetailsPage($mapid,$errtype);
      }
      else
      {
  	    $zPage->title="Контроль качества  - сводка по регионам";
        $zPage->header="Контроль качества - сводка по регионам";
        PrintQASummaryPage();
      }
  }



$zPage->WriteHtml('
<div style="display: none;">
<iframe name="josm"></iframe>
</div>');

 $zPage->Output("1");

/* 
=============================================================================
     Разные полезные фукции
===============================================================================
*/
//Имя файла ошибок по коду
function GetXmlFileName($mapid)
{
  return "ADDR_CHK/".$mapid.".mp_addr.xml";
}
function GetHWCXmlFileName($mapid)
{
  return "ADDR_CHK/".$mapid.".hwconstr_chk.xml";
}

//Ссылка на Josm
function MakeJosmLink($lat,$lon)
{
  $delta = 0.0001;

  $Link = "http://localhost:8111/load_and_zoom?top=".((float)$lat + $delta)."&bottom=".((float)$lat - $delta)."&left=".((float)$lon - $delta)."&right=".((float)$lon + $delta);

  return $Link;
}

function MakeJosmLinkBbox($lat1,$lon1,$lat2,$lon2)
{
  $delta = 0.0001;

  $Link = "http://localhost:8111/load_and_zoom?top=".$lat2."&bottom=".$lat1."&left=".$lon1."&right=".$lon2;

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

function TestX($x,$x0 )
{
	if (((float)$x)>((float)$x0))
	  {	return '<img src="/img/cross.gif" alt= "Допустимо '.$x0.'"  height="25px" />'; }
	else
	  {	return '<img src="/img/tick.gif" height="25px" />'; }
}

//============================================================================================================
//      Страница деталей области.
//============================================================================================================
function PrintQADetailsPage($mapid, $errtype)
{
  global $zPage;
  
  $zPage->WriteHtml( "<h1>Контроль качества ($mapid)</h1>");
  
  $xml = simplexml_load_file(GetXmlFileName($mapid));
  $xml1 = simplexml_load_file(GetHWCXmlFileName($mapid));
  $xmlQCR = simplexml_load_file("QualityCriteria.xml");

if ($errtype=="")
{
  $xml_stat = simplexml_load_file('statistics.xml');
  $LastKnownEdit='???';
  foreach ($xml_stat->mapinfo as $item)
  {
      if($mapid==$item->MapId)
        $LastKnownEdit=$item->LastKnownEdit.' (UTC)';
  }

   	
  	
  $zPage->WriteHtml('<p align="right"><a href="/qa">Назад к списку регионов</a> </p>' );
  
  $UnmatchedStreetsRate=(float)($xml->AddressTest->Summary->StreetsOutsideCities/$xml->AddressTest->Summary->TotalStreets);
  $zPage->WriteHtml('<table>
              <tr><td>Код карты</td><td><b>'.$mapid.'</b></td></tr>
              <tr><td>Дата прохода валидатора </td><td>'.$xml->Date.'</td></tr>
              <tr><td>Последняя известная правка  </td><td>'.$LastKnownEdit.'</td></tr>
              <tr><td>Потраченное время </td><td>'.$xml->TimeUsed.'</td></tr>
              <tr><td>RSS</td><td><a href="/qa/'.$mapid.'/rss"><img src="/img/feed-icon-14x14.png"/></a></td></tr>
              </table>
              <h2>Сводка</h2>
              <table>
                <tr><td><b>Отрисовка карты</b></td></tr>
                <tr>
                  <td>&nbsp;&nbsp;Разрывы береговой линии:</td>
                  <td>'.$xml->CoastLineTest->Summary->NumberOfBreaks.'</td>
                  <td>'.TestX($xml->CoastLineTest->Summary->NumberOfBreaks,$xmlQCR->ClassA->MaxSealineBreaks).'</td>
                  <td><a href="#shorelinebreaks">список</a></td>
                  <td></td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;Города без населения:</td>
                  <td>'.$xml->AddressTest->Summary->CitiesWithoutPopulation.'</td>
                  <td>'.TestX($xml->AddressTest->Summary->CitiesWithoutPopulation,$xmlQCR->ClassA->MaxCitiesWithoutPopulation).'</td>
                  <td><a href="#citynopop">список</a></td>
                  <td></td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;Города без полигональных границ:</td>
                  <td>'.$xml->AddressTest->Summary->CitiesWithoutPlacePolygon.'</td>
                  <td>'.TestX($xml->AddressTest->Summary->CitiesWithoutPlacePolygon,$xmlQCR->ClassA->MaxCitiesWithoutPlacePolygon).'</td>
                  <td><a href="#citynoborder">список</a></td>
                  <td></td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;Просроченные строящиеся дороги:</td>
                  <td>'.$xml1->summary->total.'</td>
                  <td>'.TestX($xml1->summary->total,$xmlQCR->ClassA->MaxOutdatedConstructions).'</td>
                  <td><a href="#hwconstr_chk">список</a></td> 
                  <td><a href="/qa/'.$mapid.'/hwc-map">на карте</a></td>
                </tr>
                
                <tr><td><b>Рутинговый граф</b></td></tr>
                <tr>
                  <td>&nbsp;&nbsp;Число рутинговых ребер :</td>
                  <td>'.$xml->RoutingTest->Summary->NumberOfRoutingEdges.'</td>
                  <td>'.TestX($xml->RoutingTest->Summary->NumberOfRoutingEdges,$xmlQCR->ClassA->MaxRoutiningEdges).'</td>
                  <td></td>
                  <td></td>  
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;Изолированные рутинговые подграфы(все) :</td>
                  <td>'.$xml->RoutingTest->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTest->Summary->NumberOfSubgraphs,$xmlQCR->ClassA->MaxIsolatedSubgraphs).'</td>
                  <td><a href="#isol">список</a></td> 
                  <td><a href="/qa/'.$mapid.'/routing-map">на карте</a></td>  
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;&nbsp;&nbsp;tertiary и выше:</td>
                  <td>'.$xml->RoutingTestByLevel->Tertiary->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTestByLevel->Tertiary->Summary->NumberOfSubgraphs,$xmlQCR->ClassA->MaxIsolatedSubgraphsTertiary).'</td>
                  <td><a href="#isol3">список</a></td>
                  <td><a href="/qa/'.$mapid.'/routing-map/3">на карте</a></td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;&nbsp;&nbsp;secondary и выше:</td>
                  <td>'.$xml->RoutingTestByLevel->Secondary->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTestByLevel->Secondary->Summary->NumberOfSubgraphs,$xmlQCR->ClassA->MaxIsolatedSubgraphsSecondary).'</td>
                  <td><a href="#isol2">список</a></td>
                  <td><a href="/qa/'.$mapid.'/routing-map/2">на карте</a></td>
                </tr>                	
                <tr>
                  <td>&nbsp;&nbsp;&nbsp;&nbsp;primary и выше:</td>
                  <td>'.$xml->RoutingTestByLevel->Primary->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTestByLevel->Primary->Summary->NumberOfSubgraphs,$xmlQCR->ClassA->MaxIsolatedSubgraphsPrimary).'</td>
                  <td><a href="#isol1">список</a></td>
                  <td><a href="/qa/'.$mapid.'/routing-map/1">на карте</a></td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;&nbsp;&nbsp;trunk:</td>
                  <td>'.$xml->RoutingTestByLevel->Trunk->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTestByLevel->Trunk->Summary->NumberOfSubgraphs,$xmlQCR->ClassA->MaxIsolatedSubgraphsTrunk).'</td>
                  <td><a href="#isol0">список</a></td>
                  <td><a href="/qa/'.$mapid.'/routing-map/0">на карте</a></td>
                </tr>

                <tr>
                  <td>&nbsp;&nbsp;Дубликаты ребер:</td>
                  <td>'.$xml->RoadDuplicatesTest->Summary->NumberOfDuplicates.'</td>
                  <td>'.TestX($xml->RoadDuplicatesTest->Summary->NumberOfDuplicates,$xmlQCR->ClassA->MaxRoadDuplicates).'</td>
                  <td><a href="#rdups">список</a></td>
                  <td><a href="/qa/'.$mapid.'/rd-map">на карте</a></td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;Тупики важных дорог:</td>
                  <td>'.$xml->DeadEndsTest->Summary->NumberOfDeadEnds.'</td>
                  <td>'.TestX($xml->DeadEndsTest->Summary->NumberOfDeadEnds,$xmlQCR->ClassA->MaxDeadEnds).'</td>
                  <td><a href="#deadends">список</a></td> 
                  <td><a href="/qa/'.$mapid.'/dnodes-map">на карте</a></td>
                </tr>

                <tr><td><b>Адресный реестр</b></td></tr> 
                <tr>
                  <td>&nbsp;&nbsp;Доля улиц, не сопоставленых НП:</td>
                  <td>'.number_format(100.00*$UnmatchedStreetsRate,2,'.', ' ').'%</td>
                  <td>'.TestX(100.00*$UnmatchedStreetsRate,100*(float)$xmlQCR->ClassA->MaxUnmatchedAddrStreets).'</td>
                  <td><a href="#addr-street">список</a></td>
                  <td><a href="/qa/'.$mapid.'/addr-street-map">на карте</a></td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;Доля не сопоставленых адресов:</td>
                  <td>'.number_format(100.00*(float)$xml->AddressTest->Summary->ErrorRate,2,'.', ' ').'%</td>
                  <td>'.TestX(100.00*(float)$xml->AddressTest->Summary->ErrorRate,100*(float)$xmlQCR->ClassA->MaxUnmatchedAddrHouses).'</td>
                  <td><a href="#addr">список</a></td>
                  <td><a href="/qa/'.$mapid.'/addr-map">на карте</a></td>
                </tr>
                </table>
              <hr/>'  );
                
  $zPage->WriteHtml("<H2>Сводка по адресации</H2>" );
  $zPage->WriteHtml("<table>" );
  $zPage->WriteHtml("<tr><td align=\"right\"><b>По домам<b></td><td></td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Всего адресов</td><td>".$xml->AddressTest->Summary->TotalHouses."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Всего улиц</td><td>".$xml->AddressTest->Summary->TotalStreets."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Не сопоставлено адресов</td><td>".$xml->AddressTest->Summary->UnmatchedHouses."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Доля несопоставленых адресов</td><td>".number_format(100.00*(float)$xml->AddressTest->Summary->ErrorRate,2,'.', ' ')."%</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Из них, по типу ошибок:</td><td></td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(I)  <a href="/qa/'.$mapid.'/addr/1">Дом вне НП</a></td><td>'.$xml->AddressTest->Summary->HousesWOCities."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(II) <a href="/qa/'.$mapid.'/addr/2">Улица не задана</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotSet."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(III)<a href="/qa/'.$mapid.'/addr/3">Улица не найдена</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotFound."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(IV) <a href="/qa/'.$mapid.'/addr/4">Улица не связана с городом</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRelatedToCity."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(V)  <a href="/qa/'.$mapid.'/addr/5"> Дом номеруется по территории</a></td><td>'.$xml->AddressTest->Summary->HousesNumberRelatedToTerritory."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(VI) <a href="/qa/'.$mapid.'/addr/6">Улица не является рутинговой в СГ</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRoutable."</td></tr>" );
  $zPage->WriteHtml("<tr><td><b></td><td></td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\"><b>По улицам<b></td><td></td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Всего улиц</td><td>".$xml->AddressTest->Summary->TotalStreets."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Улиц вне НП:</td><td>".$xml->AddressTest->Summary->StreetsOutsideCities."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Доля несопоставленых улиц</td><td>".number_format(100.00*$UnmatchedStreetsRate ,2,'.', ' ')."%</td></tr>" );


  $zPage->WriteHtml("</table>" );
  $zPage->WriteHtml("<p/>" );

  $zPage->WriteHtml(' <hr/>'  );
  
  
/*==========================================================================
                 Разрывы береговой линии
============================================================================*/
  
  //$zPage->WriteHtml('<H2>Отрисовка карты</H2>');
  //$zPage->WriteHtml('<p>В эту группу включены тесты, показывающее качество отрисовки карты, насколько она выглядит красиво и опрятно.</p>');
  
  
  $zPage->WriteHtml('<h2><a name="shorelinebreaks"></a>Разрывы береговой линии</h2>');
  $zPage->WriteHtml('<p>Когда в береговой линии имеются разрывы, море не создается. </p>');
  $zPage->WriteHtml('<p/>' );
  if ($xml->CoastLineTest->Summary->NumberOfBreaks>0)
  {	  
  $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>
                  <td><b>Название</b></td>
                  <td width="100px" align="center"><b>Править <BR/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <BR/>в Potlach</b></td>
         </tr>');

  foreach ($xml->CoastLineTest->BreakList->BreakPoint as $item)
    {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>&lt;Разрыв&gt;</td>');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->Lat,$item->Coord->Lon).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->Lat,$item->Coord->Lon) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
     }
  $zPage->WriteHtml( '</table>');
  }
  else
  {$zPage->WriteHtml( '<i>Ошибок данного типа не обнаружено.</i>');}


/*==========================================================================
                 Города без населения
============================================================================*/
  $zPage->WriteHtml('<a name="citynopop"></a><h2>Города без населения</h2>');
  $zPage->WriteHtml('<p>Наличие указанного населения (тега population=*) крайне
                     желательно, поскольку от него зависит размер надписи города в СГ и других приложениях OSM.</BR>
                     В данный список включены города (place=city и place=town), наличие тега population для деревень и поселков не столь критично.
                     Правила классификации населенных пунктов можно посмотреть на <a href="http://wiki.openstreetmap.org/wiki/RU:Key:place">OSM-Вики</a>. </p>');
  $zPage->WriteHtml("<p/>" );
  if($xml->AddressTest->Summary->CitiesWithoutPopulation>0)
  {	  
    $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>
                  <td><b>Название</b></td>
                  <td width="100px" align="center"><b>Править <BR/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <BR/>в Potlach</b></td>
         </tr>');

    foreach ($xml->AddressTest->CitiesWithoutPopulation->City as $item)
    {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td><a href="'.MakeWikiLink($item->City).'" target="_blank">'.$item->City.'</a></td>');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->lat,$item->Coord->lon).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->lat,$item->Coord->lon) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
     }
    $zPage->WriteHtml( '</table>');
  }
  else
    {$zPage->WriteHtml( '<i>Ошибок данного типа не обнаружено.</i>');}

/*==========================================================================
                 Города без полигональных границ
============================================================================*/
  $zPage->WriteHtml('<a name="citynoborder"></a><h2>Города без полигональных границ</h2>');
  $zPage->WriteHtml('<p>Наличие полигональных границ (полигона с place=* и name=* или
                     place_name=*, такими же, что и на точке города) критически важно
                     для адресного поиска. По ним определяется принадлежность домов и улиц
                     населенным пунктам.<BR/> В данный список включены города (place=city и place=town),
                     для которых полигональные границы не обнаружены.
                     Деревни и поселки (place=village|hamlet) в этом списке не показываются,
                     поскольку деревень может быть очень много, но, если в деревне
                     есть улицы и дома, полигональные границы так же нужны. Все дома вне НП будут
                     показаны в  секции "Несопоставленные адреса" ниже.
                      </p>');
  if($xml->AddressTest->Summary->CitiesWithoutPlacePolygon>0)
  {	  
    $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>
                  <td><b>Название</b></td>
                  <td width="100px" align="center"><b>Править <BR/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <BR/>в Potlach</b></td>
         </tr>');

    foreach ($xml->AddressTest->CitiesWithoutPlacePolygon->City as $item)
    {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$item->City.'</td>');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->lat,$item->Coord->lon).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->lat,$item->Coord->lon) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
     }
    $zPage->WriteHtml( '</table>');
  }
  else
  {
    $zPage->WriteHtml( '<i>Ошибок данного типа не обнаружено.</i>');
  }	    
/*==========================================================================
                 Города без точечного центра
============================================================================*/
  $zPage->WriteHtml("<h2>Города без точечного центра</h2>");
  $zPage->WriteHtml('<p>В этом списке отображаются населенные пункты, у которых есть полигональные границы
  	                 (полигон с place=* и name=* или  place_name=*), но нет точки с place=*.
  	                 Такие НП в СитиГИДе не отображаются.
                     </p>');
  $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>
                  <td><b>Название</b></td>
                  <td width="100px" align="center"><b>Править <BR/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <BR/>в Potlach</b></td>
         </tr>');

  foreach ($xml->AddressTest->CitiesWithoutPlaceNode->City as $item)
    {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$item->City.'</td>');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->lat,$item->Coord->lon).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->lat,$item->Coord->lon) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
     }
  $zPage->WriteHtml( '</table>');

/*==========================================================================
                 Highway=construction
============================================================================*/
  $zPage->WriteHtml('<a name="hwconstr_chk"><H2>Просроченные строящиеся дороги</H2></a>');
  $zPage->WriteHtml('<p>В этом тесте показываются строящиеся дороги, ожидаемая дата открытия которых уже наступила, дороги которые проверялись слишком давно,
                    а так же дороги, дата проверки или дата открытия которых нераспознанны. </p>');
  $zPage->WriteHtml("<p>Правильный формат даты: YYYY-MM-DD, например, двадцать девятое марта 2012 года должно быть записано как 2012-03-29<p/>" );
  
  
 if ($xml1->summary->total>0)
 {
  $zPage->WriteHtml('<p><b><a href="/qa/'.$mapid.'/hwc-map">Посмотреть просроченные дороги на карте</a></b></p>');
  $zPage->WriteHtml("<p><small>Таблица сортируется. Достаточно щелкнуть по заголовку столбца</small><p/>" );
  $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>
                  <td><b>Тип ошибки</b></td>
                  <td><b>Ожидаемая дата открытия</b></td>
                  <td><b>Дата последней проверки</b></td>
                  <td width="100px" align="center"><b>Править <BR/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <BR/>в Potlach</b></td>
         </tr>');

  foreach ($xml1->error_list->error as $item)
    {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$item['errorType'].'</td>');
        $zPage->WriteHtml( '<td>'.$item->opening_date.'</td>');
        $zPage->WriteHtml( '<td>'.$item->check_date.'</td>');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->bound['top'],$item->bound['left']).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->bound['top'],$item->bound['left']) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
     }
  $zPage->WriteHtml( '</table>');   
 }
 else
 {$zPage->WriteHtml( '<i>Ошибок данного типа не обнаружено</i>');};
 
/*==========================================================================
                 Изолированные рутинговые подграфы
============================================================================*/
  $zPage->WriteHtml('<h2><a name="isol"></a>Изолированные рутинговые подграфы</h2>');
  
  $zPage->WriteHtml('<p>В данном тесте показываются "изоляты", т.е. дороги или группы дорог,
                     не связанные с основным дорожным графом. ' );
  $zPage->WriteHtml('<a href="http://peirce.gis-lab.info/blog/14435">
                     Подробнее...</a> </p>' );
  $zPage->WriteHtml('<p>Почему "изоляты" это так плохо? Потому что они мешают
                     рутингу, прокладке маршрута. Когда старт и финиш оказываются
                     в разных подграфах, маршрут не строится. </p> ' );

  $zPage->WriteHtml('<p>Почему должна соблюдаться связность по уровням? Потому значение тега highway используется для генерализации при построения обзорных карт
                     При выборке дорог определенного уровня (например, только trunk, или trunk и primary) должен получаться связный граф, пригодный для навигации
                    (прокладки маршрутов), а не бессмысленный лес из не связанных между собой палочек. </p> ' );
  $zPage->WriteHtml("<p/>" );

  $zPage->WriteHtml( '<h3>Изоляты - все дороги (residential/unclassified и выше) </h3>');
  
 
  PrintIsolatedSubgraphTable($xml->RoutingTest,'/qa/'.$mapid.'/routing-map');
  

  $zPage->WriteHtml( '<h3><a name="isol3"></a>Изоляты - третичные (tertiary) и выше	  </h3>');
  PrintIsolatedSubgraphTable($xml->RoutingTestByLevel->Tertiary,'/qa/'.$mapid.'/routing-map/3');
  
  $zPage->WriteHtml( '<h3><a name="isol2"></a>Изоляты - вторичные (secondary) и выше	  </h3>');
  PrintIsolatedSubgraphTable($xml->RoutingTestByLevel->Secondary,'/qa/'.$mapid.'/routing-map/2');
 

  $zPage->WriteHtml( '<h3><a name="isol1"></a>Изоляты - первичные (primary) и выше	  </h3>');
  PrintIsolatedSubgraphTable($xml->RoutingTestByLevel->Primary,'/qa/'.$mapid.'/routing-map/1');
 

  $zPage->WriteHtml( '<h3><a name="isol0"></a>Изоляты - только столбовые (trunk) 	  </h3>');
  PrintIsolatedSubgraphTable($xml->RoutingTestByLevel->Trunk,'/qa/'.$mapid.'/routing-map/0');

/*==========================================================================
                 Дубликаты рутинговых ребер
============================================================================*/
  $zPage->WriteHtml('<a name="rdups"><H2>Дубликаты рутинговых ребер</H2></a>');
  $zPage->WriteHtml('<p>Дубликаты рутинговых ребер являются топологической ошибкой и мешают рутингу. <a href="/blog/16019">Подробнее про дубликаты дорог...</a> </p>');
  $zPage->WriteHtml("<p/>" );
  if($xml->RoadDuplicatesTest->Summary->NumberOfDuplicates>0)
  {	  
    $zPage->WriteHtml('<p><b><a href="/qa/'.$mapid.'/rd-map">Посмотреть дубликаты рутинговых ребер на карте</a></b></p>');
    $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>  
    	          <td width="20px"><b>#</b></td>
                  <td><b>Название</b></td>
                  <td width="100px" align="center"><b>Править <BR/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <BR/>в Potlach</b></td>
         </tr>');
 
    $LineNum=0;
    foreach ($xml->RoadDuplicatesTest->DuplicateList->DuplicatePoint as $item)
    {   $LineNum++;
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '  <td>'.$LineNum.'.</td>');
        $zPage->WriteHtml( '  <td>&lt;двойное ребро&gt;</td>');
        $zPage->WriteHtml( '  <td align="center"> <a href="'.MakeJosmLink($item->Coord->Lat,$item->Coord->Lon).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '  <td align="center"> <a href="'.MakePotlatchLink($item->Coord->Lat,$item->Coord->Lon) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
    }
    $zPage->WriteHtml( '</table>');  
  }
  else
    {$zPage->WriteHtml( '<i>Ошибок данного типа не обнаружено</i>');};
/*==========================================================================
                 Тупики важных дорог
============================================================================*/
  $zPage->WriteHtml('<a name="deadends"><H2>Тупики важных дорог</H2></a>');
  $zPage->WriteHtml('<p>'.GetDeadEndsTestDescription().' <a href="http://peirce.gis-lab.info/blog.php?postid=17547">Подробнее...</a></p>');
  
  $zPage->WriteHtml("<p/>" );
  
  if($xml->DeadEndsTest->Summary->NumberOfDeadEnds>0)
  {	  
    $zPage->WriteHtml('<p><b><a href="/qa/'.$mapid.'/dnodes-map">Посмотреть тупики важных дорог на карте</a></b></p>');
    $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>  
    	          <td width="20px"><b>#</b></td>
                  <td><b>Название</b></td>
                  <td width="100px" align="center"><b>Править <BR/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <BR/>в Potlach</b></td>
         </tr>');
    $LineNum=0;
    foreach ($xml->DeadEndsTest->DeadEndList->DeadEnd as $item)
    {
    	$LineNum++;
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$LineNum.'.</td>');
        $zPage->WriteHtml( '<td>&lt;тупик&gt;</td>');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->Lat,$item->Coord->Lon).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->Lat,$item->Coord->Lon) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
     }
    $zPage->WriteHtml( '</table>');    
  }
  else
  {
    $zPage->WriteHtml( '<i>Ошибок данного типа не обнаружено</i>');
  }
/*==========================================================================
                 Тест Адрески, улицы
============================================================================*/
  $zPage->WriteHtml('<a name="addr-street"><h2>Тест адресов, улицы </h2></a>' );
  $zPage->WriteHtml('<p>В этом тесте показываются улицы, не попавшие в адресный поиск, потому что они не находятся внутри полигонов place=*</p>');
  
  if($xml->AddressTest->Summary->StreetsOutsideCities>0)
  {	  
    $zPage->WriteHtml('<p><b><a href="/qa/'.$mapid.'/addr-street-map">Посмотреть ошибки адресации на карте</a></b></p>');
    $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>  
    	          <td width="20px"><b>#</b></td>
                  <td><b>Название</b></td>
                  <td width="100px" align="center"><b>Править <BR/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <BR/>в Potlach</b></td>
         </tr>');
    $LineNum=0;
    foreach ($xml->AddressTest->StreetsOutsideCities->Street as $item)
    {
    	$LineNum++;
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$LineNum.'.</td>');
        $zPage->WriteHtml( '<td>'.($item->Street).'</td>');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->Lat,$item->Coord->Lon).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->Lat,$item->Coord->Lon) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
     }
    $zPage->WriteHtml( '</table>');    
  }
  else
  {
    $zPage->WriteHtml( '<i>Ошибок данного типа не обнаружено</i>');
  }
/*==========================================================================
                 Тест Адрески, Дома
============================================================================*/
  $zPage->WriteHtml('<a name="addr"><h2>Тест адресов, дома </h2></a>' );
  $zPage->WriteHtml('<p> В данном тесте проверяется, какие дома/адреса <b>не</b>
                     попадают в адресный поиск после конвертации карт в СитиГид. <BR/>
                     Объяснение типов ошибок <a href="#errdescr">см. ниже</a></p>');
  $zPage->WriteHtml('<p><b><a href="/qa/'.$mapid.'/addr-map">Посмотреть ошибки адресации на карте</a></b></p>');

}
else //Задан конкретный тип ошибки
{
   $zPage->WriteHtml('<p align="right"><a href="/qa/'.$mapid.'">Назад к сводке '.$mapid.'</a></p>');
   $zPage->WriteHtml("<H2>".FormatAddrErrName($errtype)."</H2>" );
   $zPage->WriteHtml(FormatAddrErrDesc($errtype));
   $zPage->WriteHtml('<p><b><a href="/qa/'.$mapid.'/addr-map/'.$errtype.'">Посмотреть ошибки адресации на карте</a></b></p>');

}

 
  if ( ($errtype=="")and( ($xml->AddressTest->Summary->UnmatchedHouses>5000)))
  {
  	  $zPage->WriteHtml( '<p><b>К сожалению, ошибок настолько много, что отобразить их все невозможно.
  	                      Следует сначала починить отдельные типы.</b></p>');
      $zPage->WriteHtml("<b>по типам ошибок</b>");
      $zPage->WriteHtml('<table>');
      $zPage->WriteHtml('<tr><td>(I)   </td><td><a href="/qa/'.$mapid.'/addr/1"> Дом вне НП</a></td><td>'.$xml->AddressTest->Summary->HousesWOCities."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(II)  </td><td><a href="/qa/'.$mapid.'/addr/2"> Улица не задана</a> </td><td>'.$xml->AddressTest->Summary->HousesStreetNotSet."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(III) </td><td><a href="/qa/'.$mapid.'/addr/3">Улица не найдена</a> </td><td>'.$xml->AddressTest->Summary->HousesStreetNotFound."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(IV)  </td><td><a href="/qa/'.$mapid.'/addr/4"> Улица не связана с городом</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRelatedToCity."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(V)   </td><td><a href="/qa/'.$mapid.'/addr/5"> Дом номеруется по территории</a> </td><td>'.$xml->AddressTest->Summary->HousesNumberRelatedToTerritory."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(VI)  </td><td><a href="/qa/'.$mapid.'/addr/6">Улица не является рутинговой в СГ</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRoutable."</td></tr>" );

      $zPage->WriteHtml('</table>');

  }
  else
  {
  $zPage->WriteHtml( '<P><small>И, между прочим, таблица сортируется. Нужно кликнуть на заголовок столбца. </small></P> ');


  $zPage->WriteHtml( '<table width="900px" class="sortable">

   	    <tr>
                  <td><b>Город</b></td>
                  <td><b>Улица</b></td>
                  <td><b>Номер дома </b></td>
                  <td><b>Тип Ошибки</b></td>
                  <td width="100px" align="center"><b>Править <BR/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <BR/>в Potlach</b></td>
         </tr>');

  $count=0;
  $max_errors=4000;
  foreach ($xml->AddressTest->AddressErrorList->House as $item)
    {
      if (($errtype=="") or ($item->ErrType== $errtype))
      {
      	$count=$count+1;
      	if ($count>$max_errors) break;
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$item->City.'</td>');
        $zPage->WriteHtml( '<td>'.$item->Street.'</td>');
        $zPage->WriteHtml( '<td>'.$item->HouseNumber.'</td>');
        $zPage->WriteHtml( '<td>'.FormatAddrErrType($item->ErrType).'</td>');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->lat,$item->Coord->lon).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->lat,$item->Coord->lon) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
       }
     }

  $zPage->WriteHtml( '</table>');
  }
  if ($count>$max_errors)
   $zPage->WriteHtml( '<p>Показаны первые '.$max_errors.' ошибок</p>');

  //Классификатор ошибок
  $zPage->WriteHtml( '<a name="errdescr"><h3>Объяснение типов ошибок</h3></a>');
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


/*=====================================================================================================
Сводная таблица по адресам
=======================================================================================================*/
function PrintAddressSummaryPage()
{
  global $zPage;	 
  $zPage->WriteHtml( '<h1>Адресный валидатор (сводка)</h1>');
  $zPage->WriteHtml( '<h2>Россия</h2>');
  $zPage->WriteHtml( '<p><small>Между прочим, таблица сортируется. Нужно кликнуть
                          на заголовок столбца. </small></p> ');
  PrintAddressSummary(0);
      
  $zPage->WriteHtml( '<h2>Заграница</h2>');
  PrintAddressSummary(1);
	
}

function PrintAddressSummary($mode)
{
   global $zPage;

   //Cписок пока строим по статистике
   $xml = simplexml_load_file("maplist.xml");

   $zPage->WriteHtml( '<table width="900px" class="sortable">

   	    <tr>
                  <td width="80px"><b>Код</b></td>
                  <td width="180px"><b>Карта</b></td>
                  <td><b>Всего<BR/> адресов</b></td>
                  <!--<td><b>Всего<BR/> улиц</b></td> -->
                  <td><b>Не сопос&shyтавлено<BR/> адресов</b></td>
                  <td><b>Доля<BR/> битых<BR/> адресов, %</b></td>   

                  <td><b>Домов <BR/> вне НП</b></td>
                  <td><b> Улица не задана</b></td>
                  <td><b> Улица не найдена</b></td>
                  <td><b> Улица не связана с городом</b></td>
                  <td><b>Номера&shy;ция по терри&shy;тории</b></td>
                  <!--
                   <td><b>Города без населения</b></td> -->
                  <!-- <td><b>Города без поли&shyгональных границ</b></td> -->
               	              
                  <td width="150px"><b>Дата</b></td>
                  <td><b>Ошибки</b></td>
         </tr>');

  foreach ($xml->map as $item)
    {
      if(  (substr($item->code,0,2)=='RU' and $mode==0)or (substr($item->code,0,2)!='RU' and $mode==1) )
      {
        $xmlfilename=GetXmlFileName($item->code);

        if(file_exists($xmlfilename))
        {
          $xml_addr = simplexml_load_file($xmlfilename);
          
                    
          $zPage->WriteHtml( '<tr>');
          $zPage->WriteHtml( '<td width="80px">'.$item->code.'</td>');
          $zPage->WriteHtml( '<td width="180px">'.$item->name.'</td>');
          $zPage->WriteHtml( '<td>'.$xml_addr->AddressTest->Summary->TotalHouses.'</td>' );
         // $zPage->WriteHtml( '<td>'.$xml_addr->AddressTest->Summary->TotalStreets.'</td>' );
          $zPage->WriteHtml( '<td>'.$xml_addr->AddressTest->Summary->UnmatchedHouses.'</td>');
          $zPage->WriteHtml( '<td>'.number_format(100.00*(float)$xml_addr->AddressTest->Summary->ErrorRate,2,'.', ' ').'</td>');

          $zPage->WriteHtml( '<td>'.$xml_addr->AddressTest->Summary->HousesWOCities.'</td>' );

          $zPage->WriteHtml('<td>'.$xml_addr->AddressTest->Summary->HousesStreetNotSet."</td>" );
          $zPage->WriteHtml('<td>'.$xml_addr->AddressTest->Summary->HousesStreetNotFound."</td>" );
          $zPage->WriteHtml('<td>'.$xml_addr->AddressTest->Summary->HousesStreetNotRelatedToCity."</td>");
          $zPage->WriteHtml('<td>'.$xml_addr->AddressTest->Summary->HousesNumberRelatedToTerritory."</td>" );

         // $zPage->WriteHtml( '<td>'.$xml_addr->Summary->CitiesWithoutPopulation.'</td>' );
          //$zPage->WriteHtml( '<td>'.$xml_addr->Summary->CitiesWithoutPlacePolygon.'</td>' );

          
          //$zPage->WriteHtml('<td><a href="/qa/'.$item->code.'/routing-map">'.$xml_addr->RoutingTest->Summary->NumberOfSubgraphs."</a></td>" );
          //$zPage->WriteHtml('<td><a href="/qa/'.$item->code.'/rd-map">'.$xml_addr->RoadDuplicatesTest->Summary->NumberOfDuplicates."</a></td>" );
          //$zPage->WriteHtml( '<td><a href="/qa/'.$item->code.'/hwc-map">'.$N_hwc.'</a></td>');
          //$zPage->WriteHtml( '<td>'.str_replace('-','.',$xml_addr->Date).'</td>');
          $zPage->WriteHtml( '<td>'.$xml_addr->Date.'</td>');
          $zPage->WriteHtml( '<td><a href="/qa/'.$item->code.'">посмотреть</a></td>');

          $zPage->WriteHtml( '</tr>');
        }



      }
    }

  $zPage->WriteHtml( '</table>');
}

function TestQaClass($xml_addr, $xnClass)
{
  $Result=TRUE;
  if ($xnClass->MaxTotalAddr!=""){
    if( (int)$xml_addr->AddressTest->Summary->TotalHouses > (int)$xnClass->MaxTotalAddr)
  	  $Result=FALSE;
  }  	
  if( (int)$xml_addr->CoastLineTest->Summary->NumberOfBreaks > (int)$xnClass->MaxSealineBreaks)
  	$Result=FALSE;
  if((int)$xml_addr->RoutingTest->Summary->NumberOfSubgraphs > (int)$xnClass->MaxIsolatedSubgraphs)
    $Result=FALSE;
  if( (int)$xml_addr->RoutingTestByLevel->Tertiary->Summary->NumberOfSubgraphs > (int)$xnClass->MaxIsolatedSubgraphsTertiary)
    $Result=FALSE;
  if( $xml_addr->DeadEndsTest->Summary->NumberOfDeadEnds >   (int)$xnClass->MaxDeadEnds)
    $Result=FALSE;
  if((int)$xml_addr->RoutingTest->Summary->NumberOfRoutingEdges > (int)$xnClass->MaxRoutiningEdges)
    $Result=FALSE;
  if((float)$xml_addr->AddressTest->Summary->ErrorRate > (float)$xnClass->MaxUnmatchedAddrHouses)
    $Result=FALSE;
  if( ((float)($xml_addr->AddressTest->Summary->StreetsOutsideCities/$xml_addr->AddressTest->Summary->TotalStreets))>   (float)$xnClass->MaxUnmatchedAddrStreets)
    $Result=FALSE;
  
  return $Result;
}

//Проверка класса качества карты
function GetQaClass($xml_addr, $xmlQCR)
{

  //Класс A
  if (TestQaClass($xml_addr,$xmlQCR->ClassA))
  {
    $QARating="A";
    return $QARating;
  }

  //Класс B
  if (TestQaClass($xml_addr,$xmlQCR->ClassB))
  {
    $QARating="B";
    return $QARating;
  }
  
  //Класс B-
  if (TestQaClass($xml_addr,$xmlQCR->ClassBm))
  {
    $QARating="B-";
    return $QARating;
  }
  
  
  //Класс C
  if (TestQaClass($xml_addr,$xmlQCR->ClassC))
  {
    $QARating="C";
    return $QARating;
  }
  
  //Класс C-
  if (TestQaClass($xml_addr,$xmlQCR->ClassCm))
  {
    $QARating="C";
    return $QARating;
  }
  
  
  //Класс D
  if (TestQaClass($xml_addr,$xmlQCR->ClassD))
  {
    $QARating="D";
    return $QARating;
  }
  
  //Теперь будем двигаться сверху вниз, опуская рейтинг, если нужно
  $QARating="E";
  
   

  
  //Класс E
  if( (int)$xml_addr->CoastLineTest->Summary->NumberOfBreaks > (int)$xmlQCR->ClassB->MaxSealineBreaks)
  	$QARating="E";
  if((int)$xml_addr->RoutingTest->Summary->NumberOfSubgraphs > 2*(int)$xmlQCR->ClassB->MaxIsolatedSubgraphs)
    $QARating="E";
  if( (int)$xml_addr->RoutingTestByLevel->Tertiary->Summary->NumberOfSubgraphs > 2*(int)$xmlQCR->ClassB->MaxIsolatedSubgraphsTertiary)
    $QARating="E";
  if( $xml_addr->DeadEndsTest->Summary->NumberOfDeadEnds >   2*(int)$xmlQCR->ClassB->MaxDeadEnds)
    $QARating="E";
  if((int)$xml_addr->RoutingTest->Summary->NumberOfRoutingEdges > 2*(int)$xmlQCR->ClassB->MaxRoutiningEdges)
    $QARating="E";
  if((float)$xml_addr->AddressTest->Summary->ErrorRate > 2*(float)$xmlQCR->ClassB->MaxUnmatchedAddrHouses)
    $QARating="E";
  if( ((float)($xml_addr->AddressTest->Summary->StreetsOutsideCities/$xml_addr->AddressTest->Summary->TotalStreets))>  2* (float)$xmlQCR->ClassB->MaxUnmatchedAddrStreets)
    $QARating="E";
  
  
    //Класс F
  if( (int)$xml_addr->CoastLineTest->Summary->NumberOfBreaks > (int)$xmlQCR->ClassB->MaxSealineBreaks)
  	$QARating="F";
  if((int)$xml_addr->RoutingTest->Summary->NumberOfSubgraphs > 3*(int)$xmlQCR->ClassB->MaxIsolatedSubgraphs)
    $QARating="F";
  if( (int)$xml_addr->RoutingTestByLevel->Tertiary->Summary->NumberOfSubgraphs > 3*(int)$xmlQCR->ClassB->MaxIsolatedSubgraphsTertiary)
    $QARating="F";
  if( $xml_addr->DeadEndsTest->Summary->NumberOfDeadEnds >   3*(int)$xmlQCR->ClassB->MaxDeadEnds)
    $QARating="F";
  if((int)$xml_addr->RoutingTest->Summary->NumberOfRoutingEdges > 3*(int)$xmlQCR->ClassB->MaxRoutiningEdges)
    $QARating="F";
  if((float)$xml_addr->AddressTest->Summary->ErrorRate > 3*(float)$xmlQCR->ClassB->MaxUnmatchedAddrHouses)
    $QARating="F";
  if( ((float)($xml_addr->AddressTest->Summary->StreetsOutsideCities/$xml_addr->AddressTest->Summary->TotalStreets))>  3* (float)$xmlQCR->ClassB->MaxUnmatchedAddrStreets)
    $QARating="F";
  
  //Класс X
  if ((int)$xml_addr->CoastLineTest->Summary->NumberOfBreaks > $xmlQCR->ClassC->MaxSealineBreaks)
    $QARating="X";
  if ((int)$xml_addr->RoutingTest->Summary->NumberOfRoutingEdges> $xmlQCR->ClassC->MaxRoutiningEdges)
    $QARating="X";
          
          
          
  return $QARating;
}	

/*=====================================================================================================
Сводная таблица по контролю качества
=======================================================================================================*/
function PrintQASummaryPage()
{
  global $zPage;

	  $zPage->WriteHtml( '<h1>Контроль качества</h1>');
      $zPage->WriteHtml( '<p>На этой странице представлены основные показатели, отражающие качество карт, при конвертации в СитиГид.
                         Проверяется адресный реестр, дорожный граф и отрисовка карты.
                         Данные обновляются одновременно с картами для СГ, т.е. по возможности ежедневно.</p>' );
      $zPage->WriteHtml( '<h2>Дорожный граф </h2>');  
      $zPage->WriteHtml( '<p>Проверяется связность дорожного графа, т.е. отсутствие фрагментов, оторванных от основной дорожной сети 
                         (такие фрагменты недоступны для рутинга. <a href="/blog/14435">Подробнее про связность дорожного графа...</a>
                         </p> Также проверяется отсутствие дубликатов дорог. <a href="/blog/16019">Подробнее дубликаты дорог...</a></p>');
    
      $zPage->WriteHtml( '<p>Связность дорожного графа в масштабах всей России можно посмотреть <a href="/qa/RU/routing-map">здесь</a>.
                         В отличие от теста по отдельным картам (см. таблицу ниже), где в граф включены все дороги, в этот тест включены дороги secondary и выше .</p>');
    
    	 	  
      $zPage->WriteHtml( '<h2>Адресный реестр</h2>');

      $zPage->WriteHtml('<p> Этот тест  показывает, какие дома/адреса <b>не</b> попадают в адресный поиск
                       после конвертации карт в СитиГид.</p>
                       В СитиГиде в адресный поиск попадают дома, которые удалось сопоставить с улицами, т.е. название улицы в addr:street на доме
                       соответствует значению тега name некой улицы, причем и дом, и улица находятся внутри одного населенного пункта, обозначенного полигоном place. <BR/>
                       Что делает данный валидатор: проверяет соответствие имеющихся в OSM домов улицам, с учетом принятых  при конвертации в СГ сокращений статусных частей. <BR/>
                       Чего данный валидатор не делает: не сверяет адреса ни с какой другой адресной базой типа КЛАДРа,
                	   не проверяет названия на соответствие <a href="http://wiki.openstreetmap.org/wiki/RU:%D0%92%D0%B8%D0%BA%D0%B8%D0%9F%D1%80%D0%BE%D0%B5%D0%BA%D1%82_%D0%A0%D0%BE%D1%81%D1%81%D0%B8%D1%8F/%D0%A1%D0%BE%D0%B3%D0%BB%D0%B0%D1%88%D0%B5%D0%BD%D0%B8%D0%B5_%D0%BE%D0%B1_%D0%B8%D0%BC%D0%B5%D0%BD%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B8_%D0%B4%D0%BE%D1%80%D0%BE%D0%B3">соглашению</a> об именовании улиц.
                       <p>Обсуждение на <a href="http://forum.openstreetmap.org/viewtopic.php?id=12233">форуме OSM</a></p>
                       
                       <p>Еще один валидатор, проверяющий согласованность адресов, доступен здесь:
                       <a href="http://addresses.amdmi3.ru/">addresses.amdmi3.ru</a> </p>
                       <p>См. также <a href="http://wiki.openstreetmap.org/wiki/RU:%D0%92%D0%B0%D0%BB%D0%B8%D0%B4%D0%B0%D1%82%D0%BE%D1%80%D1%8B">
                       список валидаторов</a> в осм-вики.</p>        ' );
      $zPage->WriteHtml( '<h2>Отрисовка карты</h2>'); 
      $zPage->WriteHtml( '<p>Проверяется целостность береговой линии, наличие городов без указанного населения, а так же наличие городов без полигональных границ.</p>');                   
      $zPage->WriteHtml( '<h2>Система рейтинга</h2>');
      $zPage->WriteHtml( '<p>Каждой карте присваивается буквенная оценка качества: A, B, C, D, E, F, X (колонка "Рейтинг").</p>
                         <p><b>A</b> - Идеальные адресный реестр и дорожный граф. Минимальное количество ошибок по всем показателям. То к чему нужно стремиться.</p>
                         <p><b>B</b> - Целый адресный реестр и дорожный граф. Количественные критерии: до 15% не сопоставленных адресов,
                                       не более 50 изолятов в полном дорожном графе, не более 5 изолятов в основных (начиная с tertiary) дорогах,
                                       не более 10 тупиков магистралей. </p>
                         <p><b>C</b> - Кандидат в B. По сравнению с B, наличествуют изоляты в основных (начиная с tertiary)
                                       дорогах и тупики магистралей.  </p>
                         <p><b>D</b> - Целый адресный реестр. Дорожный граф в неудовлетворильном состоянии (до 100 изолятов). </p>
                         <p><b>E, F</b>  - Многочисленные ошибки как в адресном реестре, так и в дорожном графе.</p>
                         <p><b>X</b> - Критические ошибки, приводящие к неработоспособности/неприглядному виду карты: разрывы береговой линии, 
                         ("разлившееся" море), превышение допустимого количества рутинговых ребер. </p>     
                       ');
      $zPage->WriteHtml( '<p>C июля 2012 года выпускаются только те карты, которые получили оценки A и B</p>');
      $zPage->WriteHtml( '<h2>Россия</h2>');
      $zPage->WriteHtml( '<p><small>Между прочим, таблица сортируется. Нужно кликнуть
                        на заголовок столбца. </small></p> ');

      PrintQASummary("Россия");
    
      $zPage->WriteHtml( '<h2>Заграница</h2>');
      $zPage->WriteHtml( '<h3>Ближнее зарубежье</h3>');
      PrintQASummary("Ближнее Зарубежье");
      $zPage->WriteHtml( '<h3>Дальнее зарубежье</h3>');
      PrintQASummary("Дальнее Зарубежье");
}	

function PrintQASummary($strGroup)
{
   global $zPage;

   //Cписок пока строим по статистике
   $xml = simplexml_load_file("maplist.xml");
   $xmlQCR = simplexml_load_file("QualityCriteria.xml");
   
   $NumberOfA=0;
   $NumberOfB=0;
   $NumberOfC=0;
   $NumberOfD=0;
   $NumberOfE=0;
   $NumberOfF=0;
   $NumberOfX=0;
   $zPage->WriteHtml( '<table width="900px" class="sortable">

   	    <tr style="background: #AFAFAF">
                  <td width="80px"><b>Код</b></td>
                  <td width="180px"><b>Карта</b></td>
                  <td><b>Всего<BR/> адре&shy;сов</b></td>
                  <td><b>Доля<BR/> битых<BR/> адресов, дома %</b></td>
                  <td><b>Доля<BR/> битых<BR/> адресов, улицы %</b></td>
                  <!--<td><b>Города без поли&shy;гональ&shy;ных границ</b></td>--> 
                 
                  <td><b>Число рутин&shy;говых ребер</b></td>               
                  <td><b>Число рутин&shy;говых подгра&shy;фов</b></td>
                  <td><b>Число рутин&shy;говых подгра&shy;фов tertiary</b></td>
                  <td><b>Ту&shy;пики важ&shy;ных дорог</b></td>
               	  <td><b>Дуб&shy;ли&shy;каты ребер</b></td>
               	  <!--<td><b>Просро&shy;ченные пере&shy;крытия</b></td> -->
                  <td><b>Города без насе&shy;ления</b></td>
                  <td><b>Раз&shy;рывы бере&shy;говой линии</b></td>
                  <td width="150px"><b>Дата</b></td>
                  <td><b>Рей&shy;тинг</b></td>
                  <td><b>Ошибки</b></td>
         </tr>');

 
  foreach ($xml->map as $item)
    {
      //if(  (substr($item->code,0,2)=='RU' and $mode==0)or (substr($item->code,0,2)!='RU' and $mode==1) )
      if( $strGroup==GetMapGroup(substr($item->code,0,2)) )
      {
        $xmlfilename=GetXmlFileName($item->code);

        if(file_exists($xmlfilename))
        {
          $xml_addr = simplexml_load_file($xmlfilename);
          
          if(file_exists(GetHWCXmlFileName($item->code)))
          { 
            $xml_hwchk = simplexml_load_file(GetHWCXmlFileName($item->code));
            $N_hwc=$xml_hwchk->summary->total; 
          }
          else $N_hwc='-';
          
          
          $QARating=GetQaClass($xml_addr, $xmlQCR);
          	  
          $Style="";
           switch ($QARating) {
            case "A":
             $Style="background: #DDFFCC";
             $NumberOfA=$NumberOfA+1;
             break;
            case "B":
             $Style="background: #FFFF90";
             $NumberOfB=$NumberOfB+1;
             break;
            case "B-":
             $Style="background: #FFFF75";
             $NumberOfB=$NumberOfB+1;
             break; 
            case "C":
             $Style="background: #FFFF60";
             $NumberOfC=$NumberOfC+1;
             break; 
            case "D":
             $Style="background: #FFDDBB";
             $NumberOfD=$NumberOfD+1;
             break;
            case "E":
             $Style="background: #FFD0B0";
             $NumberOfE=$NumberOfE+1;
             break;
            case "F":
             $Style="background: #FFB0B0";
             $NumberOfF=$NumberOfF+1;
             break;    
            case "X":
             $Style="background: #FFA090";
             $NumberOfX=$NumberOfX+1;
             break;
          }
          	  
        
          
          
          $zPage->WriteHtml( '<tr style="'.$Style.'">');
          $zPage->WriteHtml( '<td width="80px">'.$item->code.'</td>');
          $zPage->WriteHtml( '<td width="180px">'.$item->name.'</td>');
          $zPage->WriteHtml( '<td>'.$xml_addr->AddressTest->Summary->TotalHouses.'</td>' );
          $zPage->WriteHtml( '<td><a href="/qa/'.$item->code.'/addr-map">'.number_format(100.00*(float)$xml_addr->AddressTest->Summary->ErrorRate,2,'.', ' ').'</a></td>');
          $zPage->WriteHtml( '<td><a href="/qa/'.$item->code.'/addr-street-map">'.number_format(100.00*(float)($xml_addr->AddressTest->Summary->StreetsOutsideCities/$xml_addr->AddressTest->Summary->TotalStreets),2,'.', ' ').'</a></td>');


          //$zPage->WriteHtml( '<td>'.$xml_addr->AddressTest->Summary->HousesWOCities.'</td>' );
          //$zPage->WriteHtml( '<td>'.$xml_addr->AddressTest->Summary->UnmatchedHouses.'</td>');

          
          //$zPage->WriteHtml( '<td>'.$xml_addr->AddressTest->Summary->CitiesWithoutPlacePolygon.'</td>' );
          
          
          
          $zPage->WriteHtml('<td>'.$xml_addr->RoutingTest->Summary->NumberOfRoutingEdges."</td>" );
          $zPage->WriteHtml('<td><a href="/qa/'.$item->code.'/routing-map">'.$xml_addr->RoutingTest->Summary->NumberOfSubgraphs."</a></td>" );
          $zPage->WriteHtml('<td><a href="/qa/'.$item->code.'/routing-map/3">'.$xml_addr->RoutingTestByLevel->Tertiary->Summary->NumberOfSubgraphs."</a></td>" );
          
          $zPage->WriteHtml('<td><a href="/qa/'.$item->code.'/dnodes-map">'.$xml_addr->DeadEndsTest->Summary->NumberOfDeadEnds."</a></td>" );
          $zPage->WriteHtml('<td><a href="/qa/'.$item->code.'/rd-map">'.$xml_addr->RoadDuplicatesTest->Summary->NumberOfDuplicates."</a></td>" );
          //$zPage->WriteHtml( '<td><a href="/qa/'.$item->code.'/hwc-map">'.$N_hwc.'</a></td>');
          
          $zPage->WriteHtml( '<td>'.$xml_addr->AddressTest->Summary->CitiesWithoutPopulation.'</td>' );
          $zPage->WriteHtml( '<td>'.$xml_addr->CoastLineTest->Summary->NumberOfBreaks.'</td>' );
          $zPage->WriteHtml( '<td>'.$xml_addr->Date.'</td>');
          $zPage->WriteHtml( '<td>'.$QARating.'</td>');
          $zPage->WriteHtml( '<td><a href="/qa/'.$item->code.'">посмотреть</a></td>');

          $zPage->WriteHtml( '</tr>');
        }



      }
    }

  $zPage->WriteHtml( '</table>');
  $zPage->WriteHtml( '<p>Итого в данном списке, '.($NumberOfA+$NumberOfB).' карт прошло QC (A+B)</p>');
  $zPage->WriteHtml( 'A: '.$NumberOfA.', ');
  $zPage->WriteHtml( 'B: '.$NumberOfB.', ');
  $zPage->WriteHtml( 'C: '.$NumberOfC.', ');
  $zPage->WriteHtml( 'D: '.$NumberOfD.', ');
  $zPage->WriteHtml( 'E: '.$NumberOfE.', ');
  $zPage->WriteHtml( 'F: '.$NumberOfF.', ');
  $zPage->WriteHtml( 'X: '.$NumberOfX.'</p>');
  
  //$QAIndex=(6*$NumberOfA+5*$NumberOfB+4*$NumberOfC+3*$NumberOfD+2*$NumberOfE+1*$NumberOF+0*$NumberOfX)/($NumberOfA+$NumberOfB+$NumberOfC+$NumberOfD+$NumberOfE+$NumberOF+$NumberOfX);
  //$zPage->WriteHtml( '<p>Условный индекс по данному списку: '.$QAIndex.'</p>');
  
}

/*=============================================================================================================================
* 
===============================================================================================================================*/
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
    $str='<b>В чем проблема:</b> дом находится вне границ населенного пункта, обозначенных полигоном place=city|town|village|hamlet. <BR/>
          <b>Как починить:</b> проверить наличие полигона place, в случае отсутствия добавить.';
    break;
case 2:
    $str='<b>В чем проблема:</b> тег addr:street на доме не заполнен.
          <b>Как починить:</b> добавить addr:street. ';
    break;
case 3:
    $str='<b>В чем проблема:</b> улица, указанная на доме, в данном НП не обнаружена. Скорее всего это опечатка, например "улица Гибоедова" вместо
          "улицы Грибоедова" или разнобой в порядке статусной части: "проспект Космонавтов" на доме и "Космонавтов проспект" на улице.<BR/>
           <b>Как починить:</b> сделать, чтобы в  addr:street дома было в точности равно name соответствующей улицы.';
    break;
case 4:
    $str='<b>В чем проблема:</b> улица, указанная в теге addr:street дома найдена в некоторой окресности, но она не связана с городом.
          Обычно так бывает, когда значительная часть улицы оказалась вне границ НП (полигона place), или когда  начало и конец улицы лежат в разных населенных пунктах.<BR/>
          <b>Как починить:</b> следует проверить границу города. Если граница города правильная, следует разделить вей улицы, создав в месте раздела общую точку с границей НП, так, что бы улица находилась внутри границ НП.
          При этом нужно убрать name c части вея, оставшегося вне НП. Если же граница города неправильная, следует ее откорректировать, чтобы улицы города находились внутри города. ';
    break;
case 5:
    $str='<b>В чем проблема:</b> дом имеет адрес вида <i>город N., 6-й микрорайон, дом 77</i>, т.е. топоним, указанный в addr:street означает не улицу,
          а район, квартал, или некую местность. <BR/>
           Часть адресов такого типа может попадать в категорию III,
           потому что анализ данного типа ошибок частично эвристический. <BR/>
          <b>Как починить:</b> никак, поддержки адресов такого типа в СитиГиде нет. ';
    break;
case 6:
    $str='<b>В чем проблема:</b> улица с таким названием есть в OSM, но не является рутиговой в СитиГиде. На данный момент это
          highway=service и highway=pedestrian.<BR/>
          <b>Как починить:</b> следует проверить, насколько обосновано улице присвоен статус service.
          Обычно наличие собственного названия и домов с адресами по этой улице есть некий аргумент в поддержку того,
          что это именно улица (highway=residential), а не дворовый проезд (highway=service).
          Пешеходные улицы (highway=pedestrian) трогать не рекомендуется.';
    break;
}

return $str;
}
//Вывод рутинговых подграфов списком
function PrintIsolatedSubgraphTable($RoutingTest,$strMapLink)
{
  global $zPage;
  if ($RoutingTest->Summary->NumberOfSubgraphs>0)
  {
    $zPage->WriteHtml('<p><b><a href="'.$strMapLink.'">Посмотреть изоляты на карте</a></b></p>');
  	  
    $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>  <td width="20px"><b>#</b></td>
                  <td><b>Название</b></td>
                  <td><b>Число ребер</b></td>
                  <td width="100px" align="center"><b>Править <BR/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <BR/>в Potlach</b></td>
         </tr>');
  
    $LineNum=0;
    foreach ($RoutingTest->SubgraphList->Subgraph as $item)
    { 
    	$LineNum++;
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '  <td>'.$LineNum.'.</td>');
        $zPage->WriteHtml( '  <td>&lt;изолят&gt;</td>');
        $zPage->WriteHtml( '  <td>'.$item->NumberOfRoads.'</td>
                              <td align="center"> <a href="'.MakeJosmLinkBbox($item->Bbox->Lat1,$item->Bbox->Lon1,$item->Bbox->Lat2,$item->Bbox->Lon2).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td>
                              <td align="center"> <a href="'.MakePotlatchLink($item->Bbox->Lat1,$item->Bbox->Lon1) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td>
                           </tr>');
    }
      $zPage->WriteHtml( '</table>');
    }
    else
    {
  	  $zPage->WriteHtml( '<i>Ошибок данного типа не обнаружено</i>');
    }	  
}

?>
