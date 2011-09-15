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

  $zPage->WriteHtml( "<h1>Валидатор адресов</h1>");
  if($mapid!="")
  {

    PrintAddresses($mapid,$errtype);
  }
  else
  {

    $zPage->WriteHtml('<p> Этот валидатор  показывает, какие дома/адреса <b>не</b> попадают в адресный поиск
                       после конвертации карт в СитиГид.</p>
                       В СитиГиде в адресный поиск попадают дома, которые удалось сопоставить с улицами, т.е. название улицы в addr:street на доме
                       соответствует значению тега name некой улицы, причем и дом, и улица находятся внутри одного населенного пункта, обозначенного полигоном place. <BR/>
                       Что делает данный валидатор: проверяет соответствие имеющихся в OSM домов улицам, с учетом принятых  при конвертации в СГ сокращений статусных частей. <BR/>
                       Чего данный валидатор не делает: не сверяет адреса ни с какой другой адресной базой типа КЛАДРа,
                	    не проверяет названия на соответствие <a href="http://wiki.openstreetmap.org/wiki/RU:%D0%92%D0%B8%D0%BA%D0%B8%D0%9F%D1%80%D0%BE%D0%B5%D0%BA%D1%82_%D0%A0%D0%BE%D1%81%D1%81%D0%B8%D1%8F/%D0%A1%D0%BE%D0%B3%D0%BB%D0%B0%D1%88%D0%B5%D0%BD%D0%B8%D0%B5_%D0%BE%D0%B1_%D0%B8%D0%BC%D0%B5%D0%BD%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B8_%D0%B4%D0%BE%D1%80%D0%BE%D0%B3">соглашению</a> об именовании улиц.
                       <p> Кроме этого, показываются города, для которых не указано население
                       и города без полигональных границ.</p>
                       <p>Обсуждение на <a href="http://forum.openstreetmap.org/viewtopic.php?id=12233">форуме OSM</a></p>
                       <p>Данные обновляются одновременно с картами для СГ, т.е.  по возможности ежедневно.</p>
                       <p>Еще один валидатор, проверяющий согласованность адресов, доступен здесь:
                       <a href="http://addresses.amdmi3.ru/">addresses.amdmi3.ru</a> </p>
                       <p>См. также <a href="http://wiki.openstreetmap.org/wiki/RU:%D0%92%D0%B0%D0%BB%D0%B8%D0%B4%D0%B0%D1%82%D0%BE%D1%80%D1%8B">
                       список валидаторов</a> в осм-вики.</p>        ' );
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
	  {	return '<img src="img/cross.gif" alt= "Допустимо '.$x0.'"  height="25px" />'; }
	else
	  {	return '<img src="img/tick.gif" height="25px" />'; }
}


//Страница деталей области.
function PrintAddresses($mapid, $errtype)
{
  global $zPage;
   $xml = simplexml_load_file(GetXmlFileName($mapid));

if ($errtype=="")
{
  $zPage->WriteHtml('<p align="right"><a href="/addr.php">Назад к списку регионов</a> </p>' );
  $zPage->WriteHtml('<table>
              <tr><td>Код карты</td><td><b>'.$mapid.'</b></td></tr>
              <tr><td>Дата прохода валидатора </td><td>'.$xml->Date.'</td></tr>
              <tr><td>Потраченное время </td><td>'.$xml->TimeUsed.'</td></tr>
              </table>
              <h2>Контроль качества</h2>
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

                <tr><td><b>Адресный реестр</b></td></tr> 
                <tr>
                  <td>&nbsp;&nbsp;Доля несопоставленых адресов:</td>
                  <td>'.number_format(100.00*(float)$xml->AddressTest->Summary->ErrorRate,2,'.', ' ').'%</td>
                  <td>'.TestX(100.00*(float)$xml->AddressTest->Summary->ErrorRate,5).'</td></tr>
              </table>
              <hr/>'  );
  $zPage->WriteHtml("<H2>Сводка </H2>" );
  $zPage->WriteHtml("<table>" );
 

  $zPage->WriteHtml("<tr><td align=\"right\">Разрывы береговой линии </td><td>".$xml->CoastLineTest->Summary->NumberOfBreaks."</td><tr>" );
  $zPage->WriteHtml('<tr><td align=\"right\"><a href="/routing-map.php?mapid='.$mapid.'">Изолированные рутинговые подграфы</a> </td><td>'.$xml->RoutingTest->Summary->NumberOfSubgraphs.'</td><tr>' );


  $zPage->WriteHtml("<tr><td align=\"right\">Города без населения </td><td>".$xml->AddressTest->Summary->CitiesWithoutPopulation."</td><tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Города без полигональных границ</td><td>".$xml->AddressTest->Summary->CitiesWithoutPlacePolygon."</td><tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Города без точечного центра</td><td>".$xml->AddressTest->Summary->CitiesWithoutPlaceNode."</td><tr>" );

  $zPage->WriteHtml("<tr><td align=\"right\">Всего адресов</td><td>".$xml->AddressTest->Summary->TotalHouses."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Всего улиц</td><td>".$xml->AddressTest->Summary->TotalStreets."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Не сопоставлено адресов</td><td>".$xml->AddressTest->Summary->UnmatchedHouses."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Доля несопоставленых адресов</td><td>".number_format(100.00*(float)$xml->AddressTest->Summary->ErrorRate,2,'.', ' ')."%</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">Из них, по типу ошибок:</td><td></td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(I) <a href="/addr.php?mapid='.$mapid.'&errtype=1">Дом вне НП</a></td><td>'.$xml->AddressTest->Summary->HousesWOCities."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(II) <a href="/addr.php?mapid='.$mapid.'&errtype=2">Улица не задана</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotSet."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(III) <a href="/addr.php?mapid='.$mapid.'&errtype=3">Улица не найдена</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotFound."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(IV) <a href="/addr.php?mapid='.$mapid.'&errtype=4">Улица не связана с городом</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRelatedToCity."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(V) <a href="/addr.php?mapid='.$mapid.'&errtype=5"> Дом номеруется по территории</a></td><td>'.$xml->AddressTest->Summary->HousesNumberRelatedToTerritory."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right">(VI) <a href="/addr.php?mapid='.$mapid.'&errtype=6">Улица не является рутинговой в СГ</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRoutable."</td></tr>" );


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
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->lat,$item->Coord->lon).'" target="josm" title="JOSM"> <img src="img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->lat,$item->Coord->lon) .'" target="_blank" title="Potlach"><img src="img/potlach.png"/></a> </td> ');
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
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->lat,$item->Coord->lon).'" target="josm" title="JOSM"> <img src="img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->lat,$item->Coord->lon) .'" target="_blank" title="Potlach"><img src="img/potlach.png"/></a> </td> ');
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
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->lat,$item->Coord->lon).'" target="josm" title="JOSM"> <img src="img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->lat,$item->Coord->lon) .'" target="_blank" title="Potlach"><img src="img/potlach.png"/></a> </td> ');
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
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->Lat,$item->Coord->Lon).'" target="josm" title="JOSM"> <img src="img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->Lat,$item->Coord->Lon) .'" target="_blank" title="Potlach"><img src="img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
     }
  $zPage->WriteHtml( '</table>');
/*==========================================================================
                 Несопоставленные адреса
============================================================================*/
  $zPage->WriteHtml("<H2>Несопоставленные адреса </H2>" );
  $zPage->WriteHtml('<p> Данный валидатор показывает какие дома/адреса <b>не</b>
                     попадают в адресный поиск после конвертации карт в СитиГид. <BR/>
                     Объяснение типов ошибок <a href="#errdescr">см. ниже</a></p>');
}
else //Задан конкретный тип ошибки
{
   $zPage->WriteHtml('<p align="right"><a href="/addr.php?mapid='.$mapid.'">Назад к сводке '.$mapid.'</a></p>');
   $zPage->WriteHtml("<H2>".FormatAddrErrName($errtype)."</H2>" );
   $zPage->WriteHtml(FormatAddrErrDesc($errtype));

}

  $zPage->WriteHtml('<p><b><a href="/addr-map.php?mapid='.$mapid.'&errtype='.$errtype.'">Посмотреть ошибки адресации на карте</a></b></p>');

  if ( ($errtype=="")and( ($xml->AddressTest->Summary->UnmatchedHouses>5000)))
  {
  	  $zPage->WriteHtml( '<p><b>К сожалению, ошибок настолько много, что отобразить их все невозможно.
  	                      Следует сначала починить отдельные типы.</b></p>');
      $zPage->WriteHtml("<b>по типам ошибок</b>");
      $zPage->WriteHtml('<table>');
      $zPage->WriteHtml('<tr><td>(I)   </td><td><a href="/addr.php?mapid='.$mapid.'&errtype=1"> Дом вне НП</a></td><td>'.$xml->AddressTest->Summary->HousesWOCities."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(II)  </td><td><a href="/addr.php?mapid='.$mapid.'&errtype=2"> Улица не задана</a> </td><td>'.$xml->AddressTest->Summary->HousesStreetNotSet."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(III) </td><td><a href="/addr.php?mapid='.$mapid.'&errtype=3">Улица не найдена</a> </td><td>'.$xml->AddressTest->Summary->HousesStreetNotFound."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(IV)  </td><td><a href="/addr.php?mapid='.$mapid.'&errtype=4"> Улица не связана с городом</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRelatedToCity."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(V)   </td><td><a href="/addr.php?mapid='.$mapid.'&errtype=5"> Дом номеруется по территории</a> </td><td>'.$xml->AddressTest->Summary->HousesNumberRelatedToTerritory."</td></tr>" );
      $zPage->WriteHtml('<tr><td>(VI)  </td><td><a href="/addr.php?mapid='.$mapid.'&errtype=6">Улица не является рутинговой в СГ</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRoutable."</td></tr>" );

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
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->lat,$item->Coord->lon).'" target="josm" title="JOSM"> <img src="img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->lat,$item->Coord->lon) .'" target="_blank" title="Potlach"><img src="img/potlach.png"/></a> </td> ');
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
                  <td><b>Номерация по территории</b></td>
                  <!--
                   <td><b>Города без населения</b></td> -->
                  <!-- <td><b>Города без поли&shyгональных границ</b></td> -->
               	  <td><b>Не сопос&shyтавлено<BR/> адресов</b></td>
                  <td><b>Доля<BR/> битых<BR/> адресов, %</b></td>
                  <td><b>Число рутинговых подграфов</b></td>
                  <td width="100px"><b>Дата</b></td>
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
          $zPage->WriteHtml('<td><a href="/routing-map.php?mapid='.$item->MapId.'">'.$xml_addr->RoutingTest->Summary->NumberOfSubgraphs."</a></td>" );
          $zPage->WriteHtml( '<td>'.str_replace('-','.',$xml_addr->Date).'</td>');
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
    $str="<b>В чем проблема:</b> улица с таким названием есть в OSM, но не является рутиговой в СитиГиде. На данный момент это
          highway=service и highway=pedestrian.<BR/>
          <b>Как починить:</b> следует проверить, насколько обосновано улице присвоен статус service.
          Обычно наличие собственного названия и домов с адресами по этой улице есть некий аргумент в поддержку того,
          что это именно улица (highway=residential), а не дворовый проезд (highway=service).
          Пешеходные улицы (highway=pedestrian) трогать не рекомендуется.";
    break;
}

return $str;
}

?>
