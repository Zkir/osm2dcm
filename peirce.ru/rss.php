<?php 
  $MapId=$_GET['mapid'];

  echo '<?xml version="1.0" encoding="utf-8"?>
        <rss version="2.0">
        <channel>';
  if ($MapId=='')
  {
    echo '<title>Контроль качества - Все карты</title>
          <link>http://peirce.gis-lab.info/qa</link>';
  }
  else
  {
  	 echo '<title>Контроль качества - '.$MapId.'</title>
           <link>http://peirce.gis-lab.info/qa/'.$MapId.'</link>';
  }
  
  $xml = simplexml_load_file("statistics.xml");
  foreach ($xml->mapinfo as $item)
    {
      if(($MapId==$item->MapId) or ($MapId==''))
        PrintItem($item->MapId,$item->MapName);
    }
  echo '</channel>
       </rss>';

function PrintItem($MapId,$MapName){
	
  $xml = simplexml_load_file(GetXmlFileName($MapId));
  $xml1 = simplexml_load_file(GetHWCXmlFileName($MapId));

	echo '<item>
    <guid>'.$MapId.'/'.$xml->Date.'</guid>
    <title>'.$MapName.'('.$MapId.') - '.$xml->Date.' </title>
    <link>http://peirce.gis-lab.info/qa/'.$MapId.'</link>
    <author>Ch.S. Peirce</author>
    <pubDate>'.$xml->Date.'</pubDate>
    <description>';
    echo '<![CDATA[';
    WriteHtml('<table>
              <tr><td>Код карты</td><td><b>'.$MapId.'</b></td></tr>
              <tr><td>Название</td><td><b>'.$MapName.'</b></td></tr>
             
              <tr><td>Дата прохода валидатора </td><td>'.$xml->Date.'</td></tr>
              <tr><td>Потраченное время </td><td>'.$xml->TimeUsed.'</td></tr>
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
  WriteHtml("<H2>Подробности</H2>" );
  WriteHtml("<table>" );
 

  WriteHtml("<tr><td align=\"right\">Разрывы береговой линии </td><td>".$xml->CoastLineTest->Summary->NumberOfBreaks."</td><tr>" );
  WriteHtml('<tr><td align=\"right\"><a href="/routing-map.php?mapid='.$MapId.'">Изолированные рутинговые подграфы</a> </td><td>'.$xml->RoutingTest->Summary->NumberOfSubgraphs.'</td><tr>' );
  WriteHtml('<tr><td align=\"right\"><a href="/addr.php?mapid='.$MapId.'#rdups">Дубликаты рутинговых ребер</a></td><td>'.$xml->RoadDuplicatesTest->Summary->NumberOfDuplicates.'</td><tr>' );
  WriteHtml('<tr><td align=\"right\"><a href="/addr.php?mapid='.$MapId.'#hwconstr_chk">Просроченные строящиеся дороги</a> </td><td>'.$xml1->summary->total.'</td><tr>' );


  WriteHtml("<tr><td align=\"right\">Города без населения </td><td>".$xml->AddressTest->Summary->CitiesWithoutPopulation."</td><tr>" );
  WriteHtml("<tr><td align=\"right\">Города без полигональных границ</td><td>".$xml->AddressTest->Summary->CitiesWithoutPlacePolygon."</td><tr>" );
  WriteHtml("<tr><td align=\"right\">Города без точечного центра</td><td>".$xml->AddressTest->Summary->CitiesWithoutPlaceNode."</td><tr>" );

  WriteHtml("<tr><td align=\"right\">Всего адресов</td><td>".$xml->AddressTest->Summary->TotalHouses."</td></tr>" );
  WriteHtml("<tr><td align=\"right\">Всего улиц</td><td>".$xml->AddressTest->Summary->TotalStreets."</td></tr>" );
  WriteHtml("<tr><td align=\"right\">Не сопоставлено адресов</td><td>".$xml->AddressTest->Summary->UnmatchedHouses."</td></tr>" );
  WriteHtml("<tr><td align=\"right\">Доля несопоставленых адресов</td><td>".number_format(100.00*(float)$xml->AddressTest->Summary->ErrorRate,2,'.', ' ')."%</td></tr>" );
  WriteHtml("<tr><td align=\"right\">Из них, по типу ошибок:</td><td></td></tr>" );
  WriteHtml('<tr><td align="right">(I) <a href="/addr.php?mapid='.$MapId.'&errtype=1">Дом вне НП</a></td><td>'.$xml->AddressTest->Summary->HousesWOCities."</td></tr>" );
  WriteHtml('<tr><td align="right">(II) <a href="/addr.php?mapid='.$MapId.'&errtype=2">Улица не задана</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotSet."</td></tr>" );
  WriteHtml('<tr><td align="right">(III) <a href="/addr.php?mapid='.$MapId.'&errtype=3">Улица не найдена</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotFound."</td></tr>" );
  WriteHtml('<tr><td align="right">(IV) <a href="/addr.php?mapid='.$MapId.'&errtype=4">Улица не связана с городом</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRelatedToCity."</td></tr>" );
  WriteHtml('<tr><td align="right">(V) <a href="/addr.php?mapid='.$MapId.'&errtype=5"> Дом номеруется по территории</a></td><td>'.$xml->AddressTest->Summary->HousesNumberRelatedToTerritory."</td></tr>" );
  WriteHtml('<tr><td align="right">(VI) <a href="/addr.php?mapid='.$MapId.'&errtype=6">Улица не является рутинговой в СГ</a></td><td>'.$xml->AddressTest->Summary->HousesStreetNotRoutable."</td></tr>" );


  WriteHtml("</table>" );
  WriteHtml("<p/>" );
  
  echo ']]>';
  echo '</description>
        </item>';
}
function WriteHtml($txt){
echo $txt;
}
//Имя файла ошибок по коду
function GetXmlFileName($mapid)
{
  return "ADDR_CHK/".$mapid.".mp_addr.xml";
}
function GetHWCXmlFileName($mapid)
{
  return "ADDR_CHK/".$mapid.".hwconstr_chk.xml";
}
function TestX($x,$x0 )
{
	if (((float)$x)>((float)$x0))
	  {	return '<img src="/img/cross.gif" alt= "Допустимо '.$x0.'"  height="25px" />'; }
	else
	  {	return '<img src="/img/tick.gif" height="25px" />'; }
}

?>

