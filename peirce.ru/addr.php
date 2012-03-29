<?php
#============================================
#Валидатор адресов
#(c) Zkir 2010
#============================================
include("ZSitePage.php");

  $zPage=new TZSitePage;
  $zPage->title="Валидатор адресов";
  $zPage->header="Валидатор адресов";

  $mapid=$_GET['mapid'];
  $errtype=$_GET['errtype'];

  if($mapid!="")
  {
    $zPage->WriteHtml( "<h1>Контроль качества</h1>");
	  
   
    PrintAddresses($mapid,$errtype);
  }
  else
  {
    
    $zPage->WriteHtml( '<h1>Контроль качества</h1>');
    $zPage->WriteHtml( '<p>На этой странице представлены основные показатели, отражающие качество карт, при конвертации в СитиГид.
                       Проверяется адресный реестр, дорожный граф и отрисовка карты.
                       Данные обновляются одновременно с картами для СГ, т.е. по возможности ежедневно.</p>' );
    $zPage->WriteHtml( '<h2>Дорожный граф </h2>');  
    $zPage->WriteHtml( '<p>Проверяется связность дорожного графа, т.е. отсутствие фрагментов, оторванных от основной дорожной сети 
                       (такие фрагменты недоступны для рутинга. <a href="/blog/14435">Подробнее про связность дорожного графа...</a>
                       </p> Также проверяется отсутствие дубликатов дорог. <a href="/blog/16019">Подробнее дубликаты дорог...</a></p>');
    
    $zPage->WriteHtml( '<p>Связность дорожного графа в масштабах всей России можно посмотреть <a href="/qc/RU/routing-map">здесь</a>.
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
    $zPage->WriteHtml( '<h2>Россия</h2>');
    $zPage->WriteHtml( '<p><small>Между прочим, таблица сортируется. Нужно кликнуть
                        на заголовок столбца. </small></p> ');

    PrintAddressesSummary(0);
    
    $zPage->WriteHtml( '<h2>Заграница</h2>');
    PrintAddressesSummary(1);
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


//Страница деталей области.
function PrintAddresses($mapid, $errtype)
{
  global $zPage;
   $xml = simplexml_load_file(GetXmlFileName($mapid));
   $xml1 = simplexml_load_file(GetHWCXmlFileName($mapid));

if ($errtype=="")
{
  $zPage->WriteHtml('<p align="right"><a href="/qc">Назад к списку регионов</a> </p>' );
  $zPage->WriteHtml('<table>
              <tr><td>Код карты</td><td><b>'.$mapid.'</b></td></tr>
              <tr><td>Дата прохода валидатора </td><td>'.$xml->Date.'</td></tr>
              <tr><td>Потраченное время </td><td>'.$xml->TimeUsed.'</td></tr>
              <tr><td>RSS</td><td><a href="/qc/'.$mapid.'/rss"><img src="/img/feed-icon-14x14.png"/></a></td></tr>
              </table>
              <h2>Сводка</h2>
              <table>
                <tr><td><b>Отрисовка карты</b></td></tr>
                <tr>
                  <td>&nbsp;&nbsp;Разрывы береговой линии:</td>
                  <td>'.$xml->CoastLineTest->Summary->NumberOfBreaks.'</td>
                  <td>'.TestX($xml->CoastLineTest->Summary->NumberOfBreaks,0).'</td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;Города без населения:</td>
                  <td>'.$xml->AddressTest->Summary->CitiesWithoutPopulation.'</td>
                  <td>'.TestX($xml->AddressTest->Summary->CitiesWithoutPopulation,0).'</td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;Города без полигональных границ:</td>
                  <td>'.$xml->AddressTest->Summary->CitiesWithoutPlacePolygon.'</td>
                  <td>'.TestX($xml->AddressTest->Summary->CitiesWithoutPlacePolygon,0).'</td>
                </tr>
                <tr><td><b>Рутинговый граф</b></td></tr>
                <tr>
                  <td>&nbsp;&nbsp;Изолированные рутинговые подграфы(все) :</td>
                  <td>'.$xml->RoutingTest->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTest->Summary->NumberOfSubgraphs,10).'</td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;trunk:</td>
                  <td>'.$xml->RoutingTestByLevel->Trunk->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTestByLevel->Trunk->Summary->NumberOfSubgraphs,3).'</td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;primary и выше:</td>
                  <td>'.$xml->RoutingTestByLevel->Primary->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTestByLevel->Primary->Summary->NumberOfSubgraphs,3).'</td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;secondary и выше:</td>
                  <td>'.$xml->RoutingTestByLevel->Secondary->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTestByLevel->Secondary->Summary->NumberOfSubgraphs,3).'</td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;tertiary и выше:</td>
                  <td>'.$xml->RoutingTestByLevel->Tertiary->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTestByLevel->Tertiary->Summary->NumberOfSubgraphs,3).'</td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;Дубликаты ребер:</td>
                  <td>'.$xml->RoadDuplicatesTest->Summary->NumberOfDuplicates.'</td>
                  <td>'.TestX($xml->RoadDuplicatesTest->Summary->NumberOfDuplicates,0).'</td>
                </tr>

                <tr><td><b>Адресный реестр</b></td></tr> 
                <tr>
                  <td>&nbsp;&nbsp;Доля несопоставленых адресов:</td>
                  <td>'.number_format(100.00*(float)$xml->AddressTest->Summary->ErrorRate,2,'.', ' ').'%</td>
                  <td>'.TestX(100.00*(float)$xml->AddressTest->Summary->ErrorRate,5).'</td></tr>
              </table>
              <hr/>'  );
  $zPage->WriteHtml("<H2>Подробности</H2>" );
  $zPage->WriteHtml("<table>" );
 

  $zPage->WriteHtml("<tr><td align=\"right\">Разрывы береговой линии </td><td>".$xml->CoastLineTest->Summary->NumberOfBreaks."</td><tr>" );
  $zPage->WriteHtml('<tr><td align=\"right\"><a href="/qc/'.$mapid.'/routing-map">Изолированные рутинговые подграфы</a> </td><td>'.$xml->RoutingTest->Summary->NumberOfSubgraphs.'</td><tr>' );
  $zPage->WriteHtml('<tr><td align=\"right\"><a href="#rdups">Дубликаты рутинговых ребер</a></td><td>'.$xml->RoadDuplicatesTest->Summary->NumberOfDuplicates.'</td><tr>' );
  $zPage->WriteHtml('<tr><td align=\"right\"><a href="#hwconstr_chk">Просроченные строящиеся дороги</a> </td><td>'.$xml1->summary->total.'</td><tr>' );


  $zPage->WriteHtml("<tr><td align=\"right\">Города без населения </td><td>".$xml->AddressTest->Summary->CitiesWithoutPopulation."</td><tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Города без полигональных границ</td><td>".$xml->AddressTest->Summary->CitiesWithoutPlacePolygon."</td><tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Города без точечного центра</td><td>".$xml->AddressTest->Summary->CitiesWithoutPlaceNode."</td><tr>" );

  $zPage->WriteHtml("<tr><td align=\"right\">Всего адресов</td><td>".$xml->AddressTest->Summary->TotalHouses."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Всего улиц</td><td>".$xml->AddressTest->Summary->TotalStreets."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Не сопоставлено адресов</td><td>".$xml->AddressTest->Summary->UnmatchedHouses."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Доля несопоставленых адресов</td><td>".number_format(100.00*(float)$xml->AddressTest->Summary->ErrorRate,2,'.', ' ')."%</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Из них, по типу ошибок:</td><td></td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(I)  <a href="/qc/'.$mapid.'/addr/1">Дом вне НП</a></td><td>'.$xml->AddressTest->Summary->HousesWOCities."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(II) <a href="/qc/'.$mapid.'/addr/2">Улица не задана</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotSet."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(III)<a href="/qc/'.$mapid.'/addr/3">Улица не найдена</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotFound."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(IV) <a href="/qc/'.$mapid.'/addr/4">Улица не связана с городом</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRelatedToCity."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(V)  <a href="/qc/'.$mapid.'/addr/5"> Дом номеруется по территории</a></td><td>'.$xml->AddressTest->Summary->HousesNumberRelatedToTerritory."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(VI) <a href="/qc/'.$mapid.'/addr/6">Улица не является рутинговой в СГ</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRoutable."</td></tr>" );


  $zPage->WriteHtml("</table>" );
  $zPage->WriteHtml("<p/>" );


/*==========================================================================
                 Города без полигональных границ
============================================================================*/
  $zPage->WriteHtml("<H2>Города без полигональных границ</H2>");
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
/*==========================================================================
                 Города без точечного центра
============================================================================*/
  $zPage->WriteHtml("<H2>Города без точечного центра</H2>");
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
                 Города без населения
============================================================================*/
  $zPage->WriteHtml("<H2>Города без населения</H2>");
  $zPage->WriteHtml('<p>Наличие населения (тега population=*) непосредственно на
                     адресный поиск не влияет. Тем не менее, указание населения крайне
                     желательно, поскольку от него зависит размер надписи города в СГ и других приложениях OSM.</BR>
                     В данный список включены города (place=city и place=town), наличие тега population для деревень и поселков не столь критично.
                     Правила классификации населенных пунктов можно посмотреть на <a href="http://wiki.openstreetmap.org/wiki/RU:Key:place">OSM-Вики</a>. </p>');
  $zPage->WriteHtml("<p/>" );

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

/*==========================================================================
                 Изолированные рутинговые подграфы
============================================================================*/
/*  $zPage->WriteHtml("<H2>Изолированные рутинговые подграфы</H2>");
  $zPage->WriteHtml('<p>Должен быть единственный, односвязный рутинговый граф. </p>');
  $zPage->WriteHtml("<p/>" );

  $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>
                  <td><b>Число ребер</b></td>
                  <td width="100px" align="center"><b>Править <BR/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <BR/>в Potlach</b></td>
         </tr>');

  foreach ($xml->RoutingTest->SubgraphList->Subgraph as $item)
    {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$item->NumberOfRoads.'</td>
                            <td align="center"> <a href="'.MakeJosmLinkBbox($item->Bbox->Lat1,$item->Bbox->Lon1,$item->Bbox->Lat2,$item->Bbox->Lon2).'" target="josm" title="JOSM"> <img src="img/josm.png"/></a> </td>
                            <td align="center"> <a href="'.MakePotlatchLink($item->Bbox->Lat1,$item->Bbox->Lon1) .'" target="_blank" title="Potlach"><img src="img/potlach.png"/></a> </td>
                           </tr>');
     }
  $zPage->WriteHtml( '</table>');
*/

/*==========================================================================
                 Разрывы береговой линии
============================================================================*/
  $zPage->WriteHtml("<H2>Разрывы береговой линии</H2>");
  $zPage->WriteHtml('<p>Когда в береговой линии имеются разрывы, море не создается. </p>');
  $zPage->WriteHtml("<p/>" );

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
  

/*==========================================================================
                 Дубликаты рутинговых ребер
============================================================================*/
  $zPage->WriteHtml('<a name="rdups"><H2>Дубликаты рутинговых ребер</H2></a>');
  $zPage->WriteHtml('<p>Дубликаты рутинговых ребер являются топологической ошибкой и мешают рутингу. </p>');
  $zPage->WriteHtml("<p/>" );
  
  $zPage->WriteHtml('<p><b><a href="/qc/'.$mapid.'/rd-map">Посмотреть дубликаты рутинговых ребер на карте</a></b></p>');


  $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>
                  <td><b>Название</b></td>
                  <td width="100px" align="center"><b>Править <BR/> в JOSM</b></td>
                  <td width="100px" align="center"><b>Править <BR/>в Potlach</b></td>
         </tr>');

  foreach ($xml->RoadDuplicatesTest->DuplicateList->DuplicatePoint as $item)
    {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>&lt;двойное ребро&gt;</td>');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->Lat,$item->Coord->Lon).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->Lat,$item->Coord->Lon) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
     }
  $zPage->WriteHtml( '</table>');  
  
/*==========================================================================
                 Highway=construction
============================================================================*/
  $zPage->WriteHtml('<a name="hwconstr_chk"><H2>Просроченные строящиеся дороги</H2></a>');
  $zPage->WriteHtml('<p>В этом разделе показываются строящиеся дороги, ожидаемая дата открытия которых уже наступила, дороги которые проверялись слишком давно,
                    а так же дороги, дата проверки или дата открытия которых нераспознанны. </p>');
  $zPage->WriteHtml("<p>Правильный формат даты: YYYY-MM-DD, например, двадцать девятое марта 2012 года должно быть записано как 2012-03-29<p/>" );
  //$zPage->WriteHtml('<p><b><a href="/qq-map.php?mapid='.$mapid.'&test=rd">Посмотреть дубликаты рутинговых ребер на карте</a></b></p>');

 if ($xml1->summary->total>0)
 {
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
 {$zPage->WriteHtml( 'Ошибок данного типа не обнаружено');};
  
/*==========================================================================
                 Несопоставленные адреса
============================================================================*/
  $zPage->WriteHtml("<H2>Несопоставленные адреса </H2>" );
  $zPage->WriteHtml('<p> Данный валидатор показывает какие дома/адреса <b>не</b>
                     попадают в адресный поиск после конвертации карт в СитиГид. <BR/>
                     Объяснение типов ошибок <a href="#errdescr">см. ниже</a></p>');
  $zPage->WriteHtml('<p><b><a href="/qc/'.$mapid.'/addr-map">Посмотреть ошибки адресации на карте</a></b></p>');

}
else //Задан конкретный тип ошибки
{
   $zPage->WriteHtml('<p align="right"><a href="/qc/'.$mapid.'">Назад к сводке '.$mapid.'</a></p>');
   $zPage->WriteHtml("<H2>".FormatAddrErrName($errtype)."</H2>" );
   $zPage->WriteHtml(FormatAddrErrDesc($errtype));
   $zPage->WriteHtml('<p><b><a href="/qc/'.$mapid.'/addr-map/'.$errtype.'">Посмотреть ошибки адресации на карте</a></b></p>');

}

 
  if ( ($errtype=="")and( ($xml->AddressTest->Summary->UnmatchedHouses>5000)))
  {
  	  $zPage->WriteHtml( '<p><b>К сожалению, ошибок настолько много, что отобразить их все невозможно.
  	                      Следует сначала починить отдельные типы.</b></p>');
      $zPage->WriteHtml("<b>по типам ошибок</b>");
      $zPage->WriteHtml('<table>');
      $zPage->WriteHtml('<tr><td>(I)   </td><td><a href="/qc/'.$mapid.'/addr/1"> Дом вне НП</a></td><td>'.$xml->AddressTest->Summary->HousesWOCities."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(II)  </td><td><a href="/qc/'.$mapid.'/addr/2"> Улица не задана</a> </td><td>'.$xml->AddressTest->Summary->HousesStreetNotSet."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(III) </td><td><a href="/qc/'.$mapid.'/addr/3">Улица не найдена</a> </td><td>'.$xml->AddressTest->Summary->HousesStreetNotFound."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(IV)  </td><td><a href="/qc/'.$mapid.'/addr/4"> Улица не связана с городом</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRelatedToCity."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(V)   </td><td><a href="/qc/'.$mapid.'/addr/5"> Дом номеруется по территории</a> </td><td>'.$xml->AddressTest->Summary->HousesNumberRelatedToTerritory."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(VI)  </td><td><a href="/qc/'.$mapid.'/addr/6">Улица не является рутинговой в СГ</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRoutable."</td></tr>" );

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
  $zPage->WriteHtml( '<a name="errdescr"><H2>Объяснение типов ошибок</H2></a>');
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

function PrintAddressesSummary($mode)
{
   global $zPage;

   //Cписок пока строим по статистике
   $xml = simplexml_load_file("statistics.xml");

   $zPage->WriteHtml( '<table width="900px" class="sortable">

   	    <tr>
                  <td width="80px"><b>Код</b></td>
                  <td width="180px"><b>Карта</b></td>
                  <td><b>Всего<BR/> адресов</b></td>
                  <!--<td><b>Всего<BR/> улиц</b></td> -->

                  <td><b>Домов <BR/> вне НП</b></td>
                  <td><b> Улица не задана</b></td>
                  <td><b> Улица не найдена</b></td>
                  <td><b>Номера&shy;ция по терри&shy;тории</b></td>
                  <!--
                   <td><b>Города без населения</b></td> -->
                  <!-- <td><b>Города без поли&shyгональных границ</b></td> -->
               	  <td><b>Не сопос&shyтавлено<BR/> адресов</b></td>
                  <td><b>Доля<BR/> битых<BR/> адресов, %</b></td>
                  <td><b>Число рутин&shy;говых подгра&shy;фов</b></td>
               	  <td><b>Дуб&shy;ли&shy;каты ребер</b></td>
               	  <td><b>Просро&shy;ченные пере&shy;крытия</b></td>
                  <td width="150px"><b>Дата</b></td>
                  <td><b>Ошибки</b></td>
         </tr>');

  foreach ($xml->mapinfo as $item)
    {
      if(  (substr($item->MapId,0,2)=='RU' and $mode==0)or (substr($item->MapId,0,2)!='RU' and $mode==1) )
      {
        $xmlfilename=GetXmlFileName($item->MapId);

        if(file_exists($xmlfilename))
        {
          $xml_addr = simplexml_load_file($xmlfilename);
          
          if(file_exists(GetHWCXmlFileName($item->MapId)))
          { 
            $xml_hwchk = simplexml_load_file(GetHWCXmlFileName($item->MapId));
            $N_hwc=$xml_hwchk->summary->total; 
          }
          else $N_hwc='-';
          
          $zPage->WriteHtml( '<tr>');
          $zPage->WriteHtml( '<td width="80px">'.$item->MapId.'</td>');
          $zPage->WriteHtml( '<td width="180px">'.$item->MapName.'</td>');
          $zPage->WriteHtml( '<td>'.$xml_addr->AddressTest->Summary->TotalHouses.'</td>' );
         // $zPage->WriteHtml( '<td>'.$xml_addr->AddressTest->Summary->TotalStreets.'</td>' );

          $zPage->WriteHtml( '<td>'.$xml_addr->AddressTest->Summary->HousesWOCities.'</td>' );

          $zPage->WriteHtml('<td>'.$xml_addr->AddressTest->Summary->HousesStreetNotSet."</td>" );
          $zPage->WriteHtml('<td>'.$xml_addr->AddressTest->Summary->HousesStreetNotFound."</td>" );
          //$zPage->WriteHtml('<td>'.$xml->AddressTest->Summary->HousesStreetNotRelatedToCity."</td>");
          $zPage->WriteHtml('<td>'.$xml_addr->AddressTest->Summary->HousesNumberRelatedToTerritory."</td>" );

         // $zPage->WriteHtml( '<td>'.$xml_addr->Summary->CitiesWithoutPopulation.'</td>' );
          //$zPage->WriteHtml( '<td>'.$xml_addr->Summary->CitiesWithoutPlacePolygon.'</td>' );

          $zPage->WriteHtml( '<td>'.$xml_addr->AddressTest->Summary->UnmatchedHouses.'</td>');
          $zPage->WriteHtml( '<td>'.number_format(100.00*(float)$xml_addr->AddressTest->Summary->ErrorRate,2,'.', ' ').'</td>');
          $zPage->WriteHtml('<td><a href="/qc/'.$item->MapId.'/routing-map">'.$xml_addr->RoutingTest->Summary->NumberOfSubgraphs."</a></td>" );
          $zPage->WriteHtml('<td><a href="/qc/'.$item->MapId.'/rd-map">'.$xml_addr->RoadDuplicatesTest->Summary->NumberOfDuplicates."</a></td>" );
          $zPage->WriteHtml( '<td>'.$N_hwc.'</td>');
          //$zPage->WriteHtml( '<td>'.str_replace('-','.',$xml_addr->Date).'</td>');
          $zPage->WriteHtml( '<td>'.$xml_addr->Date.'</td>');
          $zPage->WriteHtml( '<td><a href="/qc/'.$item->MapId.'">посмотреть</a></td>');

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

?>
