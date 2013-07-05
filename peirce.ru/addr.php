<?php
#============================================
#Валидатор адресов
#(c) Zkir 2010
#============================================
include("ZSitePage.php");
$blnCityBoundaryTestApplicable=false;
require_once("include/misc_utils.php"); 

  $zPage=new TZSitePage;
 
  $page=$_GET['page'];
  $mapid=$_GET['mapid'];
  $errtype=$_GET['errtype'];


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
    case 'stat_summary':  
      $zPage->title="Статистика (наполненность карт)";       	
      PrintMpStatSummaryPage($_GET['group']);
      break;
    case 'rss':
    	$zPage->title="Контроль качества - $mapid";
        $zPage->header="Контроль качества - $mapid";
        PrintQADetailsPageRss($mapid);
    	break;  
    case 'qa_summary':         
    default:
      
      if(($mapid!="")and($mapid!="RU")and($mapid!="CIS") and ($mapid!="WORLD") and (strlen($mapid)!=2) )
      {
  	    $zPage->title="Контроль качества  - $mapid";
        $zPage->header="Контроль качества - $mapid";
        if(file_exists(GetXmlFileName($mapid)))
        {	
          PrintQADetailsPage($mapid,$errtype);
        }
        else
        { 
        	
       		$zPage->WriteHtml('<h1>Контроль качества ('.$mapid.')</h1>');
        	$zPage->WriteHtml('Для региона '.$mapid.' данные валидатора в настоящее время отсутствуют. ');
        }	  
      }
      else
      {
  	    $zPage->title="Контроль качества  - сводка по регионам";
        $zPage->header="Контроль качества - сводка по регионам";
        
        $MapGroupName="Россия";
        if ($mapid=="CIS")
        {$MapGroupName="Ближнее Зарубежье";}
        
        if ($mapid=="WORLD")
        {$MapGroupName="Дальнее Зарубежье";}
        
        if (strlen($mapid)==2)
        {$MapGroupName=$mapid;}	
        
        PrintQASummaryPage($MapGroupName);
      }
  }


if($page!='rss')
{	 
  $zPage->WriteHtml('
              <div style="display: none;">
              <iframe name="josm"></iframe>
              </div>');
  $zPage->Output("1");
}
else
{
  $zPage->OutputAsRss();
}

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

function GetEditorsXmlFileName($mapid)
{
  return "ADDR_CHK/".$mapid."_editors.xml";
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

function TestX($x,$x0, $blnRss )
{
	if ($blnRss)
	{
		$CrossHtml='<font color="red">✘</font>';
		$TickHtml='<font color="green">✔</font>';
	}
	else
	{
		$CrossHtml='<img src="/img/cross.gif" alt= "Допустимо '.$x0.'"  height="25px" />';
		$TickHtml='<img src="/img/tick.gif" height="25px" />';
	}	
		 
	if (((float)$x)>((float)$x0))
	  {	return $CrossHtml; }
	else
	  {	return $TickHtml; }
}

//============================================================================================================
//      Страница деталей области.
//============================================================================================================
function PrintQADetailsMainDetails($mapid,$strMapName,$xml,$LastKnownEdit,$blnRss)
{
  global $zPage;
  global $blnCityBoundaryTestApplicable;
  
  $xmlQCR = simplexml_load_file("QualityCriteria2.xml");
  $xmlQCR =GetQAScale($xmlQCR,$mapid);
  if (file_exists(GetHWCXmlFileName($mapid)))
  {
    $xml1 = simplexml_load_file(GetHWCXmlFileName($mapid));
  }
  
  //Провека границ населенных пуктов применима для не всех стран.
  $blnCityBoundaryTestApplicable= false;
  if((substr ($mapid,0,2)=='RU')or (substr ($mapid,0,2)=='UA') )
  {
  	  $blnCityBoundaryTestApplicable = true;
  }
 
  if($mapid=='RU-OVRV' )
  {
  	  $blnCityBoundaryTestApplicable = false;
  }	  
  
  if ($xml->AddressTest->Summary->TotalStreets!=0)
  {	  	  
    $UnmatchedStreetsRate=(float)($xml->AddressTest->Summary->StreetsOutsideCities/$xml->AddressTest->Summary->TotalStreets);
  }
  else
  {
  	  $UnmatchedStreetsRate=0;
  }	  
  $zPage->WriteHtml('<table>
              <tr><td>'._("Код карты").'</td><td><b>'.$mapid.'</b></td></tr>
              <tr><td>'._("Название карты").'</td><td><b>'.$strMapName.'</b></td></tr>
              <tr><td>'._("Дата прохода валидатора").' </td><td>'.$xml->DateWithTime.'</td></tr>
              <tr><td>'._("Последняя известная правка").'  </td><td>'.$LastKnownEdit.'</td></tr>
              <tr><td>'._("Потраченное время").' </td><td>'.$xml->TimeUsed.'</td></tr>');
  if(!$blnRss)
  {
    if (!isset($zPage->item_link)) $zPage->item_link='/qa/'.$mapid;
    $zPage->WriteHtml(
              '<tr><td>RSS</td><td><a href="/qa/'.$mapid.'/rss"><img src="/img/feed-icon-14x14.png"/></a></td></tr>');
  }

  $zPage->WriteHtml(
  	          '</table>
              <h2>'._("Сводка").'</h2>
              <table>
                <tr><td><b>'._("Рейтинг").'</b></td><td><b>'.GetQaClass($xml, $xmlQCR,substr($mapid,0,2)).'</b></td></tr>
                <tr></tr>
                <tr><td><b>'._("Отрисовка карты").'</b></td></tr>
                <tr>
                  <td>&nbsp;&nbsp;'._("Разрывы береговой линии:").'</td>
                  <td>'.$xml->CoastLineTest->Summary->NumberOfBreaks.'</td>
                  <td>'.TestX($xml->CoastLineTest->Summary->NumberOfBreaks,$xmlQCR->ClassA->MaxSealineBreaks,$blnRss).'</td>
                  <td><a href="'.$zPage->item_link.'#shorelinebreaks">'._("список").'</a></td>
                  <td></td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;'._("Города без населения:").'</td>
                  <td>'.$xml->AddressTest->Summary->CitiesWithoutPopulation.'</td>
                  <td>'.TestX($xml->AddressTest->Summary->CitiesWithoutPopulation,$xmlQCR->ClassA->MaxCitiesWithoutPopulation,$blnRss).'</td>
                  <td><a href="'.$zPage->item_link.'#citynopop">'._("список").'</a></td>
                  <td></td>
                </tr>');
  if($blnCityBoundaryTestApplicable)
       $zPage->WriteHtml(
                '<tr>
                  <td>&nbsp;&nbsp;'._("Города без полигональных границ:").'</td>
                  <td>'.$xml->AddressTest->Summary->CitiesWithoutPlacePolygon.'</td>
                  <td>'.TestX($xml->AddressTest->Summary->CitiesWithoutPlacePolygon,$xmlQCR->ClassA->MaxCitiesWithoutPlacePolygon,$blnRss).'</td>
                  <td><a href="'.$zPage->item_link.'#citynoborder">'._("список").'</a></td>
                  <td></td>
                </tr>');
  $zPage->WriteHtml('<tr>
                  <td>&nbsp;&nbsp;'._("Просроченные строящиеся дороги:").'</td>
                  <td>'.$xml1->summary->total.'</td>
                  <td>'.TestX($xml1->summary->total,$xmlQCR->ClassA->MaxOutdatedConstructions,$blnRss).'</td>
                  <td><a href="'.$zPage->item_link.'#hwconstr_chk">'._("список").'</a></td>
                  <td><a href="'.$zPage->item_link.'/hwc-map">'._("на карте").'</a></td>
                </tr>

                <tr><td><b>'._("Рутинговый граф").'</b></td></tr>
                <tr>
                  <td>&nbsp;&nbsp;'._("Число рутинговых ребер:").'</td>
                  <td>'.$xml->RoutingTest->Summary->NumberOfRoutingEdges.'</td>
                  <td>'.TestX($xml->RoutingTest->Summary->NumberOfRoutingEdges,$xmlQCR->ClassA->MaxRoutiningEdges,$blnRss).'</td>
                  <td></td>
                  <td></td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;'._("Изолированные рутинговые подграфы(все):").'</td>
                  <td>'.$xml->RoutingTest->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTest->Summary->NumberOfSubgraphs,$xmlQCR->ClassA->MaxIsolatedSubgraphs,$blnRss).'</td>
                  <td><a href="'.$zPage->item_link.'#isol">'._("список").'</a></td>
                  <td><a href="'.$zPage->item_link.'/routing-map">'._("на карте").'</a></td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;&nbsp;&nbsp;'._("tertiary и выше:").'</td>
                  <td>'.$xml->RoutingTestByLevel->Tertiary->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTestByLevel->Tertiary->Summary->NumberOfSubgraphs,$xmlQCR->ClassA->MaxIsolatedSubgraphsTertiary,$blnRss).'</td>
                  <td><a href="'.$zPage->item_link.'#isol3">'._("список").'</a></td>
                  <td><a href="'.$zPage->item_link.'/routing-map/3">'._("на карте").'</a></td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;&nbsp;&nbsp;'._("secondary и выше:").'</td>
                  <td>'.$xml->RoutingTestByLevel->Secondary->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTestByLevel->Secondary->Summary->NumberOfSubgraphs,$xmlQCR->ClassA->MaxIsolatedSubgraphsSecondary,$blnRss).'</td>
                  <td><a href="'.$zPage->item_link.'#isol2">'._("список").'</a></td>
                  <td><a href="'.$zPage->item_link.'/routing-map/2">'._("на карте").'</a></td>
                </tr>                	
                <tr>
                  <td>&nbsp;&nbsp;&nbsp;&nbsp;'._("primary и выше:").'</td>
                  <td>'.$xml->RoutingTestByLevel->Primary->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTestByLevel->Primary->Summary->NumberOfSubgraphs,$xmlQCR->ClassA->MaxIsolatedSubgraphsPrimary,$blnRss).'</td>
                  <td><a href="'.$zPage->item_link.'#isol1">'._("список").'</a></td>
                  <td><a href="'.$zPage->item_link.'/routing-map/1">'._("на карте").'</a></td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;&nbsp;&nbsp;trunk:</td>
                  <td>'.$xml->RoutingTestByLevel->Trunk->Summary->NumberOfSubgraphs.'</td>
                  <td>'.TestX($xml->RoutingTestByLevel->Trunk->Summary->NumberOfSubgraphs,$xmlQCR->ClassA->MaxIsolatedSubgraphsTrunk,$blnRss).'</td>
                  <td><a href="'.$zPage->item_link.'#isol0">'._("список").'</a></td>
                  <td><a href="'.$zPage->item_link.'/routing-map/0">'._("на карте").'</a></td>
                </tr>

                <tr>
                  <td>&nbsp;&nbsp;'._("Дубликаты ребер:").'</td>
                  <td>'.$xml->RoadDuplicatesTest->Summary->NumberOfDuplicates.'</td>
                  <td>'.TestX($xml->RoadDuplicatesTest->Summary->NumberOfDuplicates,$xmlQCR->ClassA->MaxRoadDuplicates,$blnRss).'</td>
                  <td><a href="'.$zPage->item_link.'#rdups">'._("список").'</a></td>
                  <td><a href="'.$zPage->item_link.'/rd-map">'._("на карте").'</a></td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;'._("Тупики важных дорог:").'</td>
                  <td>'.$xml->DeadEndsTest->Summary->NumberOfDeadEnds.'</td>
                  <td>'.TestX($xml->DeadEndsTest->Summary->NumberOfDeadEnds,$xmlQCR->ClassA->MaxDeadEnds,$blnRss).'</td>
                  <td><a href="'.$zPage->item_link.'#deadends">'._("список").'</a></td>
                  <td><a href="'.$zPage->item_link.'/dnodes-map">'._("на карте").'</a></td>
                </tr>

                <tr><td><b>'._("Адресный реестр").'</b></td></tr>
                <tr>
                  <td>&nbsp;&nbsp;'._("Доля улиц, не сопоставленых НП:").'</td>
                  <td>'.number_format(100.00*$UnmatchedStreetsRate,2,'.', ' ').'%</td>
                  <td>'.TestX(100.00*$UnmatchedStreetsRate,100*(float)$xmlQCR->ClassA->MaxUnmatchedAddrStreets,$blnRss).'</td>
                  <td><a href="'.$zPage->item_link.'#addr-street">'._("список").'</a></td>
                  <td><a href="'.$zPage->item_link.'/addr-street-map">'._("на карте").'</a></td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;'._("Доля не сопоставленых адресов:").'</td>
                  <td>'.number_format(100.00*(float)$xml->AddressTest->Summary->ErrorRate,2,'.', ' ').'%</td>
                  <td>'.TestX(100.00*(float)$xml->AddressTest->Summary->ErrorRate,100*(float)$xmlQCR->ClassA->MaxUnmatchedAddrHouses,$blnRss).'</td>
                  <td><a href="'.$zPage->item_link.'#addr">'._("список").'</a></td>
                  <td><a href="'.$zPage->item_link.'/addr-map">'._("на карте").'</a></td>
                </tr>');
  
  $SolvableErrorsRate=0;              
  if ((float)$xml->AddressTest->Summary->TotalHouses!=0)
  {	  
  $SolvableErrorsRate=100.00*( ((float)$xml->AddressTest->Summary->HousesWOCities+
                                //(float)$xml->AddressTest->Summary->HousesStreetNotSet+
                                (float)$xml->AddressTest->Summary->HousesStreetNotFound+
                                (float)$xml->AddressTest->Summary->HousesStreetNotRelatedToCity ) /(float)$xml->AddressTest->Summary->TotalHouses);              
  }
  $zPage->WriteHtml('
                </table>
              <hr/>'  );

  $zPage->WriteHtml("<H2>"._("Сводка по адресации")."</H2>" );
  $zPage->WriteHtml("<table>" );
  $zPage->WriteHtml("<tr><td align=\"right\"><b>"._("По домам")."<b></td><td></td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">"._("Всего адресов")."</td><td>".$xml->AddressTest->Summary->TotalHouses."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">"._("Всего улиц")."</td><td>".$xml->AddressTest->Summary->TotalStreets."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">"._("Не сопоставлено адресов")."</td><td>".$xml->AddressTest->Summary->UnmatchedHouses."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">"._("Доля несопоставленых адресов")."</td><td>".number_format(100.00*(float)$xml->AddressTest->Summary->ErrorRate,2,'.', ' ')."%</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">"._("Из них, по типу ошибок:")."</td><td></td></tr>" );
  $zPage->WriteHtml('<tr><td align="right"><a href="'.$zPage->item_link.'/addr/1">'._('(I) Дом вне НП').'</a></td><td>'.$xml->AddressTest->Summary->HousesWOCities."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right"><a href="'.$zPage->item_link.'/addr/2">'._('(II) Улица не задана').'</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotSet."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right"><a href="'.$zPage->item_link.'/addr/3">'._('(III) Улица не найдена').'</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotFound."</td></tr>" );
  $zPage->WriteHtml('<tr><td align="right"><a href="'.$zPage->item_link.'/addr/4">'._('(IV) Улица не связана с городом').'</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRelatedToCity."</td></tr>" );
  //$zPage->WriteHtml('<tr><td align="right"><a href="'.$zPage->item_link.'/addr/5">'._('(V) Дом номеруется по территории').'</a></td><td>'.$xml->AddressTest->Summary->HousesNumberRelatedToTerritory."</td></tr>" );
  //$zPage->WriteHtml('<tr><td align="right"><a href="'.$zPage->item_link.'/addr/6">'._('(VI)Улица не является рутинговой в СГ').'</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRoutable."</td></tr>" );
  $zPage->WriteHtml("<tr><td><b></td><td></td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\"><b>"._("По улицам")."<b></td><td></td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">"._("Всего улиц")."</td><td>".$xml->AddressTest->Summary->TotalStreets."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">"._("Улиц вне НП:")."</td><td>".$xml->AddressTest->Summary->StreetsOutsideCities."</td></tr>" );
  $zPage->WriteHtml("<tr><td align=\"right\">"._("Доля несопоставленых улиц")."</td><td>".number_format(100.00*$UnmatchedStreetsRate ,2,'.', ' ')."%</td></tr>" );
  $zPage->WriteHtml("</table>" );
  
}

function PrintQADetailsPageRss($mapid)
{
  global $zPage;
  global $blnCityBoundaryTestApplicable;
  	  
  //Загрузка данных
  $xml = simplexml_load_file(GetXmlFileName($mapid));

  $xml_stat = simplexml_load_file('statistics.xml');
  $LastKnownEdit='???';
  foreach ($xml_stat->mapinfo as $item)
  {
      if($mapid==$item->MapId)
      {	  
        $LastKnownEdit=$item->LastKnownEdit.' (UTC)';
        $strMapName=$item->MapName;
        
        $objStatRecord=$item;
      }  
  }

  //Параметры rss
  $zPage->cnl_link='http://peirce.gis-lab.info/qa/'.$mapid;
  $zPage->item_guid=$mapid.'/'.$xml->Date.'/x';
  $zPage->item_title=$strMapName.'('.$mapid.') - '.$xml->Date;
  $zPage->item_link='http://peirce.gis-lab.info/qa/'.$mapid;
  $zPage->item_pubDate=$xml->Date;

  
  
  //Вывод
  PrintQADetailsMainDetails($mapid,$strMapName,$xml,$LastKnownEdit,True); 
  $zPage->WriteHtml("<p/>" );
  
  //Cтатистика
  PrintStatistics($objStatRecord,$xml);

}	

function PrintQADetailsPage($mapid, $errtype)
{
  global $zPage;
  global $blnCityBoundaryTestApplicable;
  
  $zPage->WriteHtml( '<h1>'.sprintf(_('Контроль качества (%s)'),$mapid).'</h1>');
  
  $xml = simplexml_load_file(GetXmlFileName($mapid));
  if (file_exists(GetHWCXmlFileName($mapid)))
    $xml1 = simplexml_load_file(GetHWCXmlFileName($mapid));
 

if ($errtype=="")
{
  $xml_stat = simplexml_load_file('statistics.xml');
  $LastKnownEdit='???';
  foreach ($xml_stat->mapinfo as $item)
  {
      if($mapid==$item->MapId)
      {	  
        $LastKnownEdit=$item->LastKnownEdit.' (UTC)';
        $strMapName=$item->MapName;
        
        $objStatRecord=$item;
      }  
  }

   	
  	
  $zPage->WriteHtml('<p align="right"><a href="/qa">'._('Назад к списку регионов').'</a> </p>' );
  
  PrintQADetailsMainDetails($mapid,$strMapName,$xml,$LastKnownEdit,False);
  
  
  
  $zPage->WriteHtml("<p/>" );

 
  
  //Cтатистика
  PrintStatistics($objStatRecord,$xml);

 	 
  $zPage->WriteHtml("<p/>" );
 
  
  //Редакторы карты
  if(file_exists(GetEditorsXmlFileName($mapid)))
  {	
    PrintEditors($mapid);
  
  }
  
  
  
  
  $zPage->WriteHtml(' <hr/>'  );
/*==========================================================================
                 Разрывы береговой линии
============================================================================*/
  
  //$zPage->WriteHtml('<H2>Отрисовка карты</H2>');
  //$zPage->WriteHtml('<p>В эту группу включены тесты, показывающее качество отрисовки карты, насколько она выглядит красиво и опрятно.</p>');
  
  
  $zPage->WriteHtml('<h2><a name="shorelinebreaks"></a>'._('Разрывы береговой линии').'</h2>');
  $zPage->WriteHtml('<p>'._('Когда в береговой линии имеются разрывы, море не создается.').'</p>');
  $zPage->WriteHtml('<p/>' );
  if ($xml->CoastLineTest->Summary->NumberOfBreaks>0)
  {	  
  $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>
                  <td><b>Название</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в JOSM').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в Potlach').'</b></td>
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
  {$zPage->WriteHtml( '<i>'._('Ошибок данного типа не обнаружено.').'</i>');}


/*==========================================================================
                 Города без населения
============================================================================*/
  $zPage->WriteHtml('<a name="citynopop"></a><h2>'._('Города без населения').'</h2>');
  $zPage->WriteHtml('<p>'._('Наличие указанного населения (тега population=*) крайне желательно, поскольку от него зависит размер надписи города в СГ и других приложениях OSM.').'<br />'.
                     _('В данный список включены города (place=city и place=town), наличие тега population для деревень и поселков не столь критично.'));
  $zPage->WriteHtml(sprintf(_('Правила классификации населенных пунктов можно посмотреть на <a href="%s">OSM-Вики</a>.'),'http://wiki.openstreetmap.org/wiki/RU:Key:place').'</p>');
  $zPage->WriteHtml("<p/>" );
  if($xml->AddressTest->Summary->CitiesWithoutPopulation>0)
  {	  
    $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>
                  <td><b>Название</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в JOSM').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в Potlach').'</b></td>
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
    {$zPage->WriteHtml( '<i>'._('Ошибок данного типа не обнаружено.').'</i>');}

/*==========================================================================
                 Города без полигональных границ
============================================================================*/
if($blnCityBoundaryTestApplicable)
{	
  $zPage->WriteHtml('<a name="citynoborder"></a><h2>'._('Города без полигональных границ').'</h2>');
  $zPage->WriteHtml('<p>'._('Наличие полигональных границ (полигона с place=* и name=* или
                     place_name=*, такими же, что и на точке города) критически важно
                     для адресного поиска. По ним определяется принадлежность домов и улиц
                     населенным пунктам.<BR/> В данный список включены города (place=city и place=town),
                     для которых полигональные границы не обнаружены.
                     Деревни и поселки (place=village|hamlet) в этом списке не показываются,
                     поскольку деревень может быть очень много, но, если в деревне
                     есть улицы и дома, полигональные границы так же нужны. Все дома вне НП будут
                     показаны в  секции "Несопоставленные адреса" ниже.').
                      '</p>');
  if($xml->AddressTest->Summary->CitiesWithoutPlacePolygon>0)
  {	  
    $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>
                  <td><b>'._('Название').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в JOSM').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в Potlach').'</b></td>
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
    $zPage->WriteHtml( '<i>'._('Ошибок данного типа не обнаружено.').'</i>');
  }
}  	    
/*==========================================================================
                 Города без точечного центра
============================================================================*/
if($blnCityBoundaryTestApplicable)
{	
  $zPage->WriteHtml('<h2>'._('Города без точечного центра').'</h2>');
  $zPage->WriteHtml('<p>'._('В этом списке отображаются населенные пункты, у которых есть полигональные границы
  	                 (полигон с place=* и name=* или  place_name=*), но нет точки с place=*.
  	                 Такие НП в СитиГИДе не отображаются.').
                     '</p>');
  $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>
                  <td><b>'._('Название').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в JOSM').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в Potlach').'</b></td>
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
}
/*==========================================================================
                 Highway=construction
============================================================================*/
  $zPage->WriteHtml('<a name="hwconstr_chk"><h2>'._('Просроченные строящиеся дороги').'</h2></a>');
  $zPage->WriteHtml('<p>'._('В этом тесте показываются строящиеся дороги, ожидаемая дата открытия которых уже наступила, дороги которые проверялись слишком давно,
                    а так же дороги, дата проверки или дата открытия которых нераспознанны.').'</p>');
  $zPage->WriteHtml('<p>'._('Правильный формат даты: YYYY-MM-DD, например, двадцать девятое марта 2012 года должно быть записано как 2012-03-29').'<p/>' );
  
  
 if ($xml1->summary->total>0)
 {
  $zPage->WriteHtml('<p><b><a href="/qa/'.$mapid.'/hwc-map">'._('Посмотреть просроченные дороги на карте').'</a></b></p>');
  $zPage->WriteHtml('<p><small>'._('Таблица сортируется. Достаточно щелкнуть по заголовку столбца').'</small><p/>' ); 
  $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>
                  <td><b>'._('Тип ошибки').'</b></td>
                  <td><b>'._('Ожидаемая дата открытия').'</b></td>
                  <td><b>'._('Дата последней проверки').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в JOSM').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в Potlach').'</b></td>
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
 {$zPage->WriteHtml( '<i>'._('Ошибок данного типа не обнаружено.').'</i>');};
 
/*==========================================================================
                 Изолированные рутинговые подграфы
============================================================================*/
  $zPage->WriteHtml('<h2><a name="isol"></a>'._('Изолированные рутинговые подграфы').'</h2>');
  
  $zPage->WriteHtml('<p>'._('В данном тесте показываются "изоляты", т.е. дороги или группы дорог, несвязанные с основным дорожным графом.') );
  $zPage->WriteHtml('<a href="http://peirce.gis-lab.info/blog/14435">'._('Подробнее...').'</a> </p>' );
  $zPage->WriteHtml('<p>'._('Почему "изоляты" это так плохо? Потому что они мешают
                     рутингу, прокладке маршрута. Когда старт и финиш оказываются
                     в разных подграфах, маршрут не строится.').'</p>' );

  $zPage->WriteHtml('<p>'._('Почему должна соблюдаться связность по уровням? Потому значение тега highway используется для генерализации при построения обзорных карт
                     При выборке дорог определенного уровня (например, только trunk, или trunk и primary) должен получаться связный граф, пригодный для навигации
                    (прокладки маршрутов), а не бессмысленный лес из не связанных между собой палочек.').'</p> ' );
  $zPage->WriteHtml("<p />" );

  $zPage->WriteHtml( '<h3>'._('Изоляты - все дороги (residential/unclassified и выше)').'</h3>');
  
 
  PrintIsolatedSubgraphTable($xml->RoutingTest,'/qa/'.$mapid.'/routing-map');
  

  $zPage->WriteHtml( '<h3><a name="isol3"></a>'._('Изоляты - третичные (tertiary) и выше').'</h3>');
  PrintIsolatedSubgraphTable($xml->RoutingTestByLevel->Tertiary,'/qa/'.$mapid.'/routing-map/3');
  
  $zPage->WriteHtml( '<h3><a name="isol2"></a>'._('Изоляты - вторичные (secondary) и выше').'</h3>');
  PrintIsolatedSubgraphTable($xml->RoutingTestByLevel->Secondary,'/qa/'.$mapid.'/routing-map/2');
 

  $zPage->WriteHtml( '<h3><a name="isol1"></a>'._('Изоляты - первичные (primary) и выше').'</h3>');
  PrintIsolatedSubgraphTable($xml->RoutingTestByLevel->Primary,'/qa/'.$mapid.'/routing-map/1');
 

  $zPage->WriteHtml( '<h3><a name="isol0"></a>'._('Изоляты - только столбовые (trunk)').'</h3>');
  PrintIsolatedSubgraphTable($xml->RoutingTestByLevel->Trunk,'/qa/'.$mapid.'/routing-map/0');

/*==========================================================================
                 Дубликаты рутинговых ребер
============================================================================*/
  $zPage->WriteHtml('<a name="rdups"><H2>'._('Дубликаты рутинговых ребер').'</H2></a>');
  $zPage->WriteHtml('<p>'._('Дубликаты рутинговых ребер являются топологической ошибкой и мешают рутингу.'));
  $zPage->WriteHtml('<a href="/blog/16019">'._('Подробнее про дубликаты дорог...').'</a></p>');
  $zPage->WriteHtml("<p/>" );
  if($xml->RoadDuplicatesTest->Summary->NumberOfDuplicates>0)
  {	  
    $zPage->WriteHtml('<p><b><a href="/qa/'.$mapid.'/rd-map">'._('Посмотреть дубликаты рутинговых ребер на карте').'</a></b></p>');
    $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>  
    	          <td width="20px"><b>#</b></td>
                  <td><b>'._('Название').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в JOSM').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в Potlach').'</b></td>
         </tr>');
 
    $LineNum=0;
    foreach ($xml->RoadDuplicatesTest->DuplicateList->DuplicatePoint as $item)
    {   $LineNum++;
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '  <td>'.$LineNum.'.</td>');
        $zPage->WriteHtml( '  <td>'._('&lt;двойное ребро&gt;').'</td>');
        $zPage->WriteHtml( '  <td align="center"> <a href="'.MakeJosmLink($item->Coord->Lat,$item->Coord->Lon).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '  <td align="center"> <a href="'.MakePotlatchLink($item->Coord->Lat,$item->Coord->Lon) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
    }
    $zPage->WriteHtml( '</table>');  
  }
  else
    {$zPage->WriteHtml( '<i>'._('Ошибок данного типа не обнаружено.').'</i>');};
/*==========================================================================
                 Тупики важных дорог
============================================================================*/
  $zPage->WriteHtml('<a name="deadends"><H2>'._('Тупики важных дорог').'</H2></a>');
  $zPage->WriteHtml('<p>'.GetDeadEndsTestDescription().' <a href="http://peirce.gis-lab.info/blog.php?postid=17547">'._('Подробнее...').'</a></p>');
  
  $zPage->WriteHtml("<p/>" );
  
  if($xml->DeadEndsTest->Summary->NumberOfDeadEnds>0)
  {	  
    $zPage->WriteHtml('<p><b><a href="/qa/'.$mapid.'/dnodes-map">'._('Посмотреть тупики важных дорог на карте').'</a></b></p>');
    $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>  
    	          <td width="20px"><b>#</b></td>
                  <td><b>'._('Название').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в JOSM').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в Potlach').'</b></td>
         </tr>');
    $LineNum=0;
    foreach ($xml->DeadEndsTest->DeadEndList->DeadEnd as $item)
    {
    	$LineNum++;
    	
    	if ($LineNum>500)
    	{
    	  break;
    	}
    	
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$LineNum.'.</td>');
        $zPage->WriteHtml( '<td>'._('&lt;тупик&gt;').'</td>');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->Lat,$item->Coord->Lon).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->Lat,$item->Coord->Lon) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
     }
    $zPage->WriteHtml( '</table>');    
  }
  else
  {
    $zPage->WriteHtml( '<i>'._('Ошибок данного типа не обнаружено.').'</i>');
  }
/*==========================================================================
                 Тест Адрески, улицы
============================================================================*/
  $zPage->WriteHtml('<a name="addr-street"><h2>'._('Тест адресов, улицы').'</h2></a>' );
  $zPage->WriteHtml('<p>'._('В этом тесте показываются улицы, не попавшие в адресный поиск, потому что они не находятся внутри полигонов place=*').'</p>');
  
  if($xml->AddressTest->Summary->StreetsOutsideCities>0)
  {	  
    $zPage->WriteHtml('<p><b><a href="/qa/'.$mapid.'/addr-street-map">'._('Посмотреть ошибки адресации на карте').'</a></b></p>');
    $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>  
    	          <td width="20px"><b>#</b></td>
                  <td><b>'._('Название').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в JOSM').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в Potlach').'</b></td>
         </tr>');
    $LineNum=0;
    foreach ($xml->AddressTest->StreetsOutsideCities->Street as $item)
    {
    	$LineNum++;
    	if ($LineNum>500)
    	{
    	  break;
    	}	
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$LineNum.'.</td>');
        $zPage->WriteHtml( '<td>'.($item->Street).'</td>');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakeJosmLink($item->Coord->Lat,$item->Coord->Lon).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td> ');
        $zPage->WriteHtml( '<td align="center"> <a href="'.MakePotlatchLink($item->Coord->Lat,$item->Coord->Lon) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td> ');
        $zPage->WriteHtml( '</tr>');
     }
    $zPage->WriteHtml( '</table>');    
    if ($LineNum>500)
    	{
    	  $zPage->WriteHtml( '<i>'._('Ошибок очень много, показаны первые пятьсот ошибок.').'</i>');
    	}	
  }
  else
  {
    $zPage->WriteHtml( '<i>'._('Ошибок данного типа не обнаружено.').'</i>');
  }
/*==========================================================================
                 Тест Адрески, Дома
============================================================================*/
  $zPage->WriteHtml('<a name="addr"><h2>'._('Тест адресов, дома').'</h2></a>' );
  $zPage->WriteHtml('<p>'._('В данном тесте проверяется, какие дома/адреса <b>не</b>
                     попадают в адресный поиск после конвертации карт в СитиГид. <BR/>
                     Объяснение типов ошибок <a href="#errdescr">см. ниже</a>').'</p>');
  $zPage->WriteHtml('<p><b><a href="/qa/'.$mapid.'/addr-map">'._('Посмотреть ошибки адресации на карте').'</a></b></p>');

}
else //Задан конкретный тип ошибки
{
   $zPage->WriteHtml('<p align="right"><a href="/qa/'.$mapid.'">'.sprintf(_('Назад к сводке %s'),$mapid).'</a></p>');
   $zPage->WriteHtml("<H2>".FormatAddrErrName($errtype)."</H2>" );
   $zPage->WriteHtml(FormatAddrErrDesc($errtype));
   $zPage->WriteHtml('<p><b><a href="/qa/'.$mapid.'/addr-map/'.$errtype.'">'._('Посмотреть ошибки адресации на карте').'</a></b></p>');

}

 
  if ( ($errtype=="")and( ($xml->AddressTest->Summary->UnmatchedHouses>3000)))
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
  $zPage->WriteHtml( '<P><small>'._('И, между прочим, таблица сортируется. Нужно кликнуть на заголовок столбца.').'</small></P> ');


  $zPage->WriteHtml( '<table width="900px" class="sortable">

   	    <tr>
                  <td><b>'._('Город').'</b></td>
                  <td><b>'._('Улица').'</b></td>
                  <td><b>'._('Номер дома').'</b></td>
                  <td><b>'._('Тип ошибки').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в JOSM').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в Potlach').'</b></td>
         </tr>');

  $count=0;
  $max_errors=3000;
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
   $zPage->WriteHtml( '<p>'.sprintf(_('Показаны первые %s ошибок.'),$max_errors).'</p>');

  //Классификатор ошибок
  $zPage->WriteHtml( '<a name="errdescr"><h3>'._('Объяснение типов ошибок').'</h3></a>');
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
  
  //Ошибки адресации - улицы не привязанные к городам
  if (((float)$xml_addr->AddressTest->Summary->TotalStreets)!=0)
  {	  
    $tmp_rate=((float)($xml_addr->AddressTest->Summary->StreetsOutsideCities/$xml_addr->AddressTest->Summary->TotalStreets));
  }
  else
  {
  	  $tmp_rate=0;
  }	  
  if($tmp_rate  >   (float)$xnClass->MaxUnmatchedAddrStreets)
    $Result=FALSE;
  
 
  //Ошибки адресации - улицы не привязанные к регионам
  if ($xnClass->MaxUnmatchedAddrStreetsNoRegion!=""){
  	  //$Result=FALSE;
	  if (((float)$xml_addr->AddressTest->Summary->TotalStreets)!=0)
	  {	  
	    $tmp_rate=((float)($xml_addr->AddressTest->Summary->StreetsWithoutRegion/$xml_addr->AddressTest->Summary->TotalStreets));
	  }
	  else
	  {
	  	  $tmp_rate=0;
	  }	  
	  if( ($xml_addr->AddressTest->Summary->StreetsWithoutRegion=="") or ($tmp_rate  >   (float)$xnClass->MaxUnmatchedAddrStreetsNoRegion))
	    $Result=FALSE;
  }
  
  //"Исправимые" ошибки адресации - дома
  if ($xnClass->MaxUnmatchedAddrHousesFixable!=""){
  	  if (((float)$xml_addr->AddressTest->Summary->TotalHouses)!=0)
	  {	 
	  
	   $SolvableErrorsRate= ( ((float)$xml_addr->AddressTest->Summary->HousesWOCities+
	                           //(float)$xml_addr->AddressTest->Summary->HousesStreetNotSet+
	                           (float)$xml_addr->AddressTest->Summary->HousesStreetNotFound+
	                           (float)$xml_addr->AddressTest->Summary->HousesStreetNotRelatedToCity ) /(float)$xml_addr->AddressTest->Summary->TotalHouses); 
	   
	                 
	 
	    $tmp_rate=$SolvableErrorsRate;
	  }
	  else
	  {
	  	  $tmp_rate=0;
	  }	
	 
	  if($tmp_rate  >   (float)$xnClass->MaxUnmatchedAddrHousesFixable)
	    $Result=FALSE;
  }
   	  
  //Дупликаты дорог.
  if ($xnClass->MaxRoadDuplicates!=""){
    if( (int)$xml_addr->RoadDuplicatesTest->Summary->NumberOfDuplicates > (int)$xnClass->MaxRoadDuplicates)
  	  $Result=FALSE;
  }
  
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
  if (TestQaClass($xml_addr,$xmlQCR->ClassBm0))
  {
    $QARating="B-";
    return $QARating;
  }
  
  if (TestQaClass($xml_addr,$xmlQCR->ClassBm1))
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
  
  if ( ((float)$xml_addr->AddressTest->Summary->TotalStreets)!=0)
  {	   
    $tmpStreetRate=((float)($xml_addr->AddressTest->Summary->StreetsOutsideCities/$xml_addr->AddressTest->Summary->TotalStreets));
  }
  else
  {
    $tmpStreetRate=0;	  
  }	  
  
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
  if( tmpStreetRate >  2* (float)$xmlQCR->ClassB->MaxUnmatchedAddrStreets)
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
  if( tmpStreetRate >  3* (float)$xmlQCR->ClassB->MaxUnmatchedAddrStreets)
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
function PrintQASummaryPage($GroupName)
{
  global $zPage;

	  $zPage->WriteHtml( '<h1>'._("Контроль качества").'</h1>');
      $zPage->WriteHtml( '<p>'._('На этой странице представлены основные показатели, отражающие качество карт, при конвертации в СитиГид.
                         Проверяется адресный реестр, дорожный граф и отрисовка карты.
                         Данные обновляются одновременно с картами для СГ, т.е. по возможности ежедневно.').'</p>' );
      $zPage->WriteHtml( '<h2>Группы </h2>');  
      
      $zPage->WriteHtml( '
      	                  <table> 
      	        	      <tr> 
      	                    <td>
      	                     <a href="/qa/RU#table">Россия</a>
      	                    </td>
      	        	        <td>
      	                     <a href="/qa/CIS#table">Ближнее Зарубежье</a>
      	                    </td>
      	        	        <td>
      	                     <a href="/qa/WORLD#table">Дальнее Зарубежье</a>
      	                    </td>
      	                  </tr> 
      	                  </table> 
                          ');  
      
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
                         <p><b>B-</b> - Тоже что и B, но с поиском только до улиц. В этот класс попадают карты, где адресов мало, меньше двух тысяч,
                                        либо много "неисправимых" ошибок  (нумерация по территории, нерутинговые улицы). </p>
          
                         <p><b>C</b> - Кандидат в B. По сравнению с B, наличествуют изоляты в основных (начиная с tertiary)
                                       дорогах и тупики магистралей.  </p>
                         <p><b>D</b> - Целый адресный реестр. Дорожный граф в неудовлетворильном состоянии (до 100 изолятов). </p>
                         <p><b>E, F</b>  - Многочисленные ошибки как в адресном реестре, так и в дорожном графе.</p>
                         <p><b>X</b> - Критические ошибки, приводящие к неработоспособности/неприглядному виду карты: разрывы береговой линии, 
                         ("разлившееся" море), превышение допустимого количества рутинговых ребер. </p>     
                       ');
      $zPage->WriteHtml( '<p>C июля 2012 года выпускаются только те карты, которые получили оценки A и B</p>');
      $zPage->WriteHtml( '<h2><a name="table">'.$GroupName.'</a></h2>');
      $zPage->WriteHtml( '<p><small>Между прочим, таблица сортируется. Нужно кликнуть
                        на заголовок столбца. </small></p> ');

      PrintQASummary($GroupName);
    
      /*
      $zPage->WriteHtml( '<h2>Заграница</h2>');
      $zPage->WriteHtml( '<h3>Ближнее зарубежье<a name="CIS"></a></h3>');
      PrintQASummary("Ближнее Зарубежье");
      $zPage->WriteHtml( '<h3>Дальнее зарубежье<a name="WORLD"></a></h3>');
      PrintQASummary("Дальнее Зарубежье");
      */
      
       $zPage->WriteHtml( '<h2>См. также </h2>');
       $zPage->WriteHtml( '<p> Все вопросы по работе данного валидатора можно задать на <a href="http://forum.openstreetmap.org/viewtopic.php?id=12233">форуме OSM</a>. </p>');
      
}	

function GetQAScale($xmlQCR, $mapCode)
{
	if (substr($mapCode,3,4)=="OVRV")
          {
            return $xmlQCR->Scale3;
          }	  
          else{
          	  
          $strCountryCode=substr($mapCode,0,2);
          if (
          	     ($strCountryCode=="AT") //Австрия
          	  or ($strCountryCode=="AL") //Албания
          	  or ($strCountryCode=="BE") //Бельгия
          	  or ($strCountryCode=="BG") //Болгария
          	  or ($strCountryCode=="CH") //Швейцария
          	  or ($strCountryCode=="CZ") //Чехия
          	  or ($strCountryCode=="CY") //Кипр
          	  or ($strCountryCode=="DE") //Германия
          	  or ($strCountryCode=="DK") //Дания
           	  or ($strCountryCode=="ES") //Испания
           	  or ($strCountryCode=="HR") //Хорватия           	  
           	  or ($strCountryCode=="FR") //Франция
           	  or ($strCountryCode=="GB") //Великобритания
           	  or ($strCountryCode=="GR") //Греция
           	  or ($strCountryCode=="HU") //Венгрия
          	  or ($strCountryCode=="IE") //Ирландия
          	  or ($strCountryCode=="IS") //Исландия
          	  or ($strCountryCode=="IT") //Италия
          	  or ($strCountryCode=="MT") //Мальта
              or ($strCountryCode=="NL") //Голландия
              or ($strCountryCode=="NO") //Норвегия
              or ($strCountryCode=="PL") //Польша  
              or ($strCountryCode=="RO") //Румыния
              or ($strCountryCode=="RS") //Сербия
  	          or ($strCountryCode=="SE") //Швеция
  	          or ($strCountryCode=="SI") //Словения
  	          or ($strCountryCode=="SK") //Словакия
  	          or ($strCountryCode=="TR") //Турция 
  	             
  	          //Amerika
  	          or ($strCountryCode=="CL") //Чили
  	          or ($strCountryCode=="CR") //Коста-Рика
  	          or ($strCountryCode=="CU") 
          	  or ($strCountryCode=="GT") 
          	  or ($strCountryCode=="JM") 
       	  	  or ($strCountryCode=="NI") 
   	  	  	  or ($strCountryCode=="PY") 
  	  	  	  or ($strCountryCode=="US")
  	         )
  	        {	  
	          return $xmlQCR->Scale2;
            }
          else
            {
          	  return $xmlQCR->Scale1;
             }	     
          
          }
}	

function PrintQASummary($strGroup)
{
   global $zPage;

   //Cписок пока строим по статистике
   $xml = simplexml_load_file("maplist.xml");
   $xmlQCR = simplexml_load_file("QualityCriteria2.xml");
   
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
                  <td><b>Доля улиц вне города %</b></td>
                  <td><b>Доля улиц вне региона %</b></td>
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
      if( ($strGroup==GetMapGroup(substr($item->code,0,2))) or (substr($item->code,0,2)==$strGroup) )
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
          
          
          $QARating=GetQaClass($xml_addr, GetQAScale($xmlQCR,$item->code));
          
          
          	  
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
          if (((float)$xml_addr->AddressTest->Summary->TotalStreets)!=0)
          {  
            $zPage->WriteHtml( '<td><a href="/qa/'.$item->code.'/addr-street-map">'.number_format(100.00*(float)($xml_addr->AddressTest->Summary->StreetsOutsideCities/$xml_addr->AddressTest->Summary->TotalStreets),2,'.', ' ').'</a></td>');
            if($xml_addr->AddressTest->Summary->StreetsWithoutRegion!="")
              $zPage->WriteHtml( '<td>'.number_format(100.00*(float)($xml_addr->AddressTest->Summary->StreetsWithoutRegion/$xml_addr->AddressTest->Summary->TotalStreets),2,'.', ' ').'</td>');
            else
              $zPage->WriteHtml( '<td>?</td>');	          
          }
          else
          { 
            $zPage->WriteHtml( '<td><a href="/qa/'.$item->code.'/addr-street-map">'.number_format(0,2,'.', ' ').'</a></td>');
            $zPage->WriteHtml( '<td>'.number_format(0,2,'.', ' ').'</td>');
          }

       
          
          
          
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
  $zPage->WriteHtml( 'X: '.$NumberOfX.',');
  $NumberOfAll=$NumberOfA+$NumberOfB+$NumberOfC+$NumberOfD+$NumberOfE+$NumberOfF+$NumberOfX;
  $zPage->WriteHtml( ' всего '.$NumberOfAll.' </p>');
  
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
    $str=_("Дом вне НП");
    break;
case 2:
    $str=_("Улица не задана");
    break;
case 3:
    $str=_("Улица не найдена");
    break;
case 4:
    $str=_("Улица не связана с городом");
    break;
case 5:
    $str=_("Дом номеруется по территории");
    break;
case 6:
    $str=_("Улица не является рутинговой в СГ");
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
    $str=_('<b>В чем проблема:</b> дом находится вне границ населенного пункта, обозначенных полигоном place=city|town|village|hamlet.').' <BR/>'.
         _('<b>Как починить:</b> проверить наличие полигона place, в случае отсутствия добавить.');
    break;
case 2:
    $str=_('<b>В чем проблема:</b> тег addr:street на доме не заполнен либо дом не включен в отношение своей улицы.').'<BR/>'.
         _('<b>Как починить:</b> добавить addr:street либо включить дом в отношение соответствующей улицы. Если отношение отсутствует - создать его. ');
    break;
case 3:
    $str=_('<b>В чем проблема:</b> улица, указанная на доме, в данном НП не обнаружена. Скорее всего это опечатка, например "улица Гибоедова" вместо
          "улицы Грибоедова" или разнобой в порядке статусной части: "проспект Космонавтов" на доме и "Космонавтов проспект" на улице.').'<BR/>'.
          _(' <b>Как починить:</b> сделать, чтобы в  addr:street дома было в точности равно name соответствующей улицы.');
    break;
case 4:
    $str=_('<b>В чем проблема:</b> улица, указанная в теге addr:street дома найдена в некоторой окресности, но она не связана с городом.
          Обычно так бывает, когда значительная часть улицы оказалась вне границ НП (полигона place), или когда  начало и конец улицы лежат в разных населенных пунктах.').'<BR/>'.
         _('<b>Как починить:</b> следует проверить границу города. Если граница города правильная, следует разделить вей улицы, создав в месте раздела общую точку с границей НП, так, что бы улица находилась внутри границ НП.
          При этом нужно убрать name c части вея, оставшегося вне НП. Если же граница города неправильная, следует ее откорректировать, чтобы улицы города находились внутри города. ');
    break;
case 5:
    $str=_('<b>В чем проблема:</b> дом имеет адрес вида <i>город N., 6-й микрорайон, дом 77</i>, т.е. топоним, указанный в addr:street означает не улицу,
          а район, квартал, или некую местность. Часть адресов такого типа может попадать в категорию III, потому что анализ данного типа ошибок частично эвристический.').'<BR/>'.
          _('<b>Как починить:</b> никак, поддержки адресов такого типа в СитиГиде нет.');
    break;
case 6:
    $str=_('<b>В чем проблема:</b> улица с таким названием есть в OSM, но не является рутинговой в СитиГиде (или вынесена в так называемый вторичный дорожный граф).
          На данный момент это  highway=service и highway=pedestrian.').'<BR/>'.
          _('<b>Как починить:</b> следует проверить, насколько обосновано улице присвоен статус service.
          Обычно наличие собственного названия и домов с адресами по этой улице есть некий аргумент в поддержку того,
          что это именно улица (highway=residential), а не дворовый проезд (highway=service).
          Пешеходные улицы (highway=pedestrian) трогать не рекомендуется.');
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
    $zPage->WriteHtml('<p><b><a href="'.$strMapLink.'">'._('Посмотреть изоляты на карте').'</a></b></p>');
  	  
    $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>  <td width="20px"><b>#</b></td>
                  <td><b>'._('Название').'</b></td>
                  <td><b>'._('Число ребер').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в JOSM').'</b></td>
                  <td width="100px" align="center"><b>'._('Править <br /> в Potlach').'</b></td>
         </tr>');
  
    $LineNum=0;
    foreach ($RoutingTest->SubgraphList->Subgraph as $item)
    { 
    	$LineNum++;
    	if ($LineNum>500)
    	{
    	  break;
    	}
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '  <td>'.$LineNum.'.</td>');
        $zPage->WriteHtml( '  <td>'._('&lt;изолят&gt;').'</td>');
        $zPage->WriteHtml( '  <td>'.$item->NumberOfRoads.'</td>
                              <td align="center"> <a href="'.MakeJosmLinkBbox($item->Bbox->Lat1,$item->Bbox->Lon1,$item->Bbox->Lat2,$item->Bbox->Lon2).'" target="josm" title="JOSM"> <img src="/img/josm.png"/></a> </td>
                              <td align="center"> <a href="'.MakePotlatchLink($item->Bbox->Lat1,$item->Bbox->Lon1) .'" target="_blank" title="Potlach"><img src="/img/potlach.png"/></a> </td>
                           </tr>');
    }
      $zPage->WriteHtml( '</table>');
    }
    else
    {
  	  $zPage->WriteHtml( '<i>'._('Ошибок данного типа не обнаружено.').'</i>');
    }
    
    if ($LineNum>500)
    {
      $zPage->WriteHtml( '<i>'._('Ошибок очень много, показаны первые пятьсот ошибок').'</i>');
    }	  
}

//Редакторы карты
function PrintEditors($mapid)
{
 global $zPage;
 $xml=simplexml_load_file(GetEditorsXmlFileName($mapid));	
 $zPage->WriteHtml( '<h2><a name="editors">'._('Редакторы карты за последние 12 месяцев').'</a><sup><a href="#editors">#</a></sup></h2>');	
 
 
 $zPage->WriteHtml( '<table width="900px" class="sortable">
    	    <tr>  <td width="20px"><b>#</b></td>
                  <td><b>'._('Редактор').'</b></td>
                  <td><b>?</b></td>
                  <td><b>?</b></td>
                  <td><b>'.$xml->periods->period_1.'</b></td>
                  <td><b>'.$xml->periods->period_2.'</b></td>
                  <td><b>'.$xml->periods->period_3.'</b></td>
                  <td><b>'.$xml->periods->period_4.'</b></td>
                  <td><b>'.$xml->periods->period_5.'</b></td>
                  <td><b>'.$xml->periods->period_6.'</b></td>
                  <td><b>'.$xml->periods->period_7.'</b></td>
                  <td><b>'.$xml->periods->period_8.'</b></td>
                  <td><b>'.$xml->periods->period_9.'</b></td>
                  <td><b>'.$xml->periods->period_10.'</b></td>
                  <td><b>'.$xml->periods->period_11.'</b></td>
                  <td><b>'.$xml->periods->period_12.'</b></td>
                  <td><b>'.$xml->periods->period_13.'</b></td>
             </tr>');
  
    $LineNum=0;
    foreach ($xml->user as $user)
    { 
    	$LineNum++;
    	if ($LineNum>100)
    	{
    	  break;
    	}
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '  <td>'.$LineNum.'.</td>');
        $zPage->WriteHtml( '  <td><a href="http://www.openstreetmap.org/user/'.$user->name.'">'.$user->name.'</a></td>');
        $zPage->WriteHtml( '  <td><a href=" http://hdyc.neis-one.org/?'.$user->name.'">*</a></td>');
        $zPage->WriteHtml( '  <td><a href=" http://yosmhm.neis-one.org/?'.$user->name.'">*</a></td>');
        $zPage->WriteHtml( '  <td align="right">'.$user->period_1.'</td>');
        $zPage->WriteHtml( '  <td align="right">'.$user->period_2.'</td>');
        $zPage->WriteHtml( '  <td align="right">'.$user->period_3.'</td>');
        $zPage->WriteHtml( '  <td align="right">'.$user->period_4.'</td>');
        $zPage->WriteHtml( '  <td align="right">'.$user->period_5.'</td>');
        $zPage->WriteHtml( '  <td align="right">'.$user->period_6.'</td>');
        $zPage->WriteHtml( '  <td align="right">'.$user->period_7.'</td>');
        $zPage->WriteHtml( '  <td align="right">'.$user->period_8.'</td>');
        $zPage->WriteHtml( '  <td align="right">'.$user->period_9.'</td>');
        $zPage->WriteHtml( '  <td align="right">'.$user->period_10.'</td>');
        $zPage->WriteHtml( '  <td align="right">'.$user->period_11.'</td>');
        $zPage->WriteHtml( '  <td align="right">'.$user->period_12.'</td>');
        $zPage->WriteHtml( '  <td align="right">'.$user->period_13.'</td>');
        $zPage->WriteHtml( '</tr>');
    }
    $zPage->WriteHtml( '</table>');
    
    
    
    if ($LineNum>50)
    {
      $zPage->WriteHtml( '<i>'.sprintf(_('Показаны первые 100 редакторов из %s'),$xml->user_count).'</i>');
    }	  

 
};

//Вывод статистики
function PrintStatistics($objStatRecord,$xml)
{	
  global $zPage;
  $item=$objStatRecord;  
   
  $zPage->WriteHtml( '<h2><a name="stat_summary">'._('Основные статистические сведения').'</a><sup><a href="#stat_summary">#</a></sup></h2>');
  $zPage->WriteHtml( '<b>'.$item->MapId.'</b>- ');
  $zPage->WriteHtml( '<b>'.$item->MapName.'</b>');
  $zPage->WriteHtml( '<h3>'._('Osm-данные').'</h3>' );
  $zPage->WriteHtml( '<table>');
  $zPage->WriteHtml( '<tr>
              
                  <td><b>'._('Площадь,<br/> тыс.&nbsp;кв.&nbsp;км').'</b></td>
                  <td><b>'._('Число объектов').'</b></td>
                  <td><b>'._('Правок в день').'</b></td>
                  <td><b>'._('моложе 14 дней').'</b></td>
                  <td><b>'._('100 дней').'</b></td>
                  <td><b>'._('365 дней').'</b></td>
                  <td><b>'._('Ср. возраст, дни').'</b></td>
                  <td><b>'._('Число объектов<br/>  на кв.&nbsp;км').'</b></td>
                  <td><b>'._('Правок в день<br/>на тыс.&nbsp;кв.&nbsp;км').'</b></td>
                  <td><b>'._('Активные <br/> участники').'</b></td>
         </tr>');
        	  
  
      	
  $zPage->WriteHtml( '<tr>');
  $zPage->WriteHtml( '<td>'.number_format($item->Square/1000,0,'.', ' ').'</td>');
  $zPage->WriteHtml( '<td>'.$item->NumberOfObjects.'</td>');
  $zPage->WriteHtml( '<td>'.$item->EditsPerDay.'</td> ');
  $zPage->WriteHtml( '<td>'.$item->M14.'%</td> ');
  $zPage->WriteHtml( '<td>'.$item->M100.'%</td> ');
  $zPage->WriteHtml( '<td>'.$item->M365.'%</td> ');
  $zPage->WriteHtml( '<td>'.number_format($item->AverageObjectAge,0,'.', ' ').'</td> ');
  $zPage->WriteHtml( '<td>'.number_format($item->ObjectsPerSquareKm,2,'.', ' ').'</td> ');
  $zPage->WriteHtml( '<td>'.number_format((((float)$item->EditsPerDayPerSquareKm)*1000.0) ,1,'.', ' ').'</td> ');
  $zPage->WriteHtml( '<td>'.$item->ActiveUsers.'</td> ');
  $zPage->WriteHtml( '</tr>');      
  $zPage->WriteHtml( '</table>');
  $zPage->WriteHtml( '<p>'.sprintf(_('Расшифровку показателей см. на <a href="%s">странице статистики</a>.'),"/stat#descr").'</p>');
  
    $zPage->WriteHtml("<p/>" );
  $zPage->WriteHtml('<h3><a name="map_stat">'._('Итоговая карта').'</a><sup><a href="#map_stat">#</a></sup></h3>' );
  $zPage->WriteHtml(sprintf(
                             _('- Общая протяженность дорог: <b>%s</b> км, дворовых проездов: <b>%s</b> км'),
                             $xml->Statistics->Summary->RoadLengthKm, $xml->Statistics->Summary->ServiceRoadLengthKm
                           ). '<br />');
  $zPage->WriteHtml(sprintf(
                             _('- Общее количество точек интереса (POI): <b>%s</b> шт.,
                                в том числе с адресной информацией: <b>%s</b> шт.'),
                          ($xml->Statistics->Summary->TotalPoiNumber),($xml->Statistics->Summary->PoiWithAddressNumber)).'<br />');
  $zPage->WriteHtml(sprintf(
                             _('- Количество населенных пунктов, доступных в адресном поиске: <b>%s</b>'),
                             $xml->Statistics->Summary->CitiesNumber).'<br />'); 
  $zPage->WriteHtml(sprintf(_('- Общее количество зданий с адресной информацией: <b>%s</b> шт.'),(($xml->AddressTest->Summary->TotalHouses)-($xml->AddressTest->Summary->UnmatchedHouses))).'<br />');
}

/*=====================================================================================================
Сводная таблица по Статистике МП
=======================================================================================================*/
function PrintMpStatSummaryPage($group)
{
  global $zPage;	 
  if (trim($group)=='') $group='RU';
  
  $zPage->WriteHtml( '<h1>Статистика (наполненность карт для СГ)</h1>');
  $zPage->WriteHtml( '<p>   На этой странице приведены статистические показатели, отражающие наполненность (подробность) итоговых карт.
  	                  Эти показатели считаются по объектам, фактически попавшим в готовые карты osm для СГ 7.x  </p>');
  $zPage->WriteHtml( '<h2>'.$group.'</h2>');
  $zPage->WriteHtml( '<p><small>Между прочим, таблица сортируется. Нужно кликнуть
                          на заголовок столбца. </small></p> ');
  
  PrintMpStatSummary(0,$group);
      
  //$zPage->WriteHtml( '<h2>Заграница</h2>');
  //PrintAddressSummary(1);
	
}
function PrintMpStatSummary($mode, $group)
{
   global $zPage;

   //Cписок пока строим по статистике
   $xml = simplexml_load_file("maplist.xml");

   $zPage->WriteHtml( '<table width="900px" class="sortable">

   	    <tr style="background: #AFAFAF">
                  <td width="80px"><b>Код</b></td>
                  <td width="180px"><b>Карта</b></td>
                  <td><b>Протяжен&shy;ность дорог, км</b></td>
            
                  <td><b>Протяженность дворовых проездов, км</b></td>
                  <td><b>Общее количество точек интереса (POI)</b></td> 
                  <td><b>в том числе POI c адресами</b></td>
                  <td><b>Общее количество зданий с  адресами</b></td>
                  <td><b>Количество населенных пунктов</b></td>   
        </tr>');

  foreach ($xml->map as $item)
    {
      if(  (substr($item->code,0,2)==$group and $mode==0)or (substr($item->code,0,2)!='RU' and $mode==1) )
      {
        $xmlfilename=GetXmlFileName($item->code);

        if(file_exists($xmlfilename))
        {
          $xml_addr = simplexml_load_file($xmlfilename);
          
                    
          $zPage->WriteHtml( '<tr>');
          $zPage->WriteHtml( '<td width="80px">'.$item->code.'</td>');
          $zPage->WriteHtml( '<td width="180px">'.$item->name.'</td>');
          
          $zPage->WriteHtml('<td>'.$xml_addr->Statistics->Summary->RoadLengthKm."</td>" );
          $zPage->WriteHtml('<td>'.$xml_addr->Statistics->Summary->ServiceRoadLengthKm."</td>" );
          $zPage->WriteHtml('<td>'.$xml_addr->Statistics->Summary->TotalPoiNumber."</td>" );
          $zPage->WriteHtml('<td>'.$xml_addr->Statistics->Summary->PoiWithAddressNumber."</td>" );
          $zPage->WriteHtml('<td>'.(($xml_addr->AddressTest->Summary->TotalHouses)-($xml_addr->AddressTest->Summary->UnmatchedHouses))."</td>" );
          $zPage->WriteHtml('<td>'.$xml_addr->Statistics->Summary->CitiesNumber."</td>" );
       

          $zPage->WriteHtml( '</tr>');
        }



      }
    }

  $zPage->WriteHtml( '</table>');
  
  $zPage->WriteHtml( '<h2>Методика расчета</h2>');
  $zPage->WriteHtml( '<p>Данные показатели отражают подробность (детализированость) итоговых карт для Ситигида на основе OSM. В данной статистики отражаются объекты,
  	                     фактически попавшие в итоговые карты, а не все данные, которые есть в OSM.
                         Подсчет ведется по промежуточному польскому файлу. </p>');
  $zPage->WriteHtml( '<p><b>Протяженность дорог, км</b> - Протяженность дорог и улиц, без учета дворовых проездов и сельскохозяйственных грунтовок.
  	                        Длинна односторонних дорог учитывается с коэффициентом 0.5. Сделано это для того, чтобы способ, которым нарисована дорога 
  	                        (одним веем или двумя) не влиял на этот показатель. </p>');
            
  $zPage->WriteHtml('<p><b>Протяженность дворовых проездов, км</b> - протяженность дворовых проездов, проездов на заправках, и других служебных проездов.  </p>
  	                 <p><b>Общее количество точек интереса (POI)</b> - число точечных объектов (ПОИ) в карте, без учета точечных домов и пунсонов населенных пунктов. </p> 
                     <p><b>в том числе POI c адресами</b> - число ПОИ, имеющих адрес </td>
                     <p><b>Общее количество зданий с  адресами</b> - число зданий, как точечных, так и полигональных, имеющих адрес и попадающих в адресный поиск.</td>
                     <p><b>Количество населенных пунктов</b> - Количество точечных населенных пунктов (пунсонов). </p> ');
  $zPage->WriteHtml('<h2>См. также</h2>');
  $zPage->WriteHtml('<p> <ul> <li> <a href="/stat">Статистика по исходным данным OSM</a>  </li> </ul> </p> ');
  
}

?>
