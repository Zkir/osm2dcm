<?php 
  $MapId=$_GET['mapid'];

  echo '<?xml version="1.0" encoding="utf-8"?>
        <rss version="2.0">
        <channel>';
  
  $xml = simplexml_load_file("maplist.xml");
  
  if ($MapId=='')
  {
    echo '<title>Контроль качества - Сводка</title>
          <link>http://peirce.gis-lab.info/qa</link>';
    PrintSummaryItem($xml);
  }
  else
  {
    echo '<title>Контроль качества - '.$MapId.'</title>
           <link>http://peirce.gis-lab.info/qa/'.$MapId.'</link>';
     
     
    
    foreach ($xml->map as $item)
    {
      if(($MapId==$item->code) )
        PrintItem($item->code,$item->name);
    }
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
    
    $UnmatchedStreetsRate=(float)($xml->AddressTest->Summary->StreetsOutsideCities/$xml->AddressTest->Summary->TotalStreets);

    WriteHtml('<table>
              <tr><td>Код карты</td><td><b>'.$mapid.'</b></td></tr>
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
                <tr>
                  <td>&nbsp;&nbsp;Просроченные строящиеся дороги:</td>
                  <td>'.$xml1->summary->total.'</td>
                  <td>'.TestX($xml1->summary->total,5).'</td>
                </tr>
                <tr><td><b>Рутинговый граф</b></td></tr>
                <tr>
                  <td>&nbsp;&nbsp;Число рутинговых ребер :</td>
                  <td>'.$xml->RoutingTest->Summary->NumberOfRoutingEdges.'</td>
                  <td>'.TestX($xml->RoutingTest->Summary->NumberOfRoutingEdges,300000.0).'</td>
                </tr>
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
                  <td>'.TestX($xml->RoadDuplicatesTest->Summary->NumberOfDuplicates,5).'</td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;Тупики важных дорог:</td>
                  <td>'.$xml->DeadEndsTest->Summary->NumberOfDeadEnds.'</td>
                  <td>'.TestX($xml->DeadEndsTest->Summary->NumberOfDeadEnds,10).'</td>
                </tr>

                <tr><td><b>Адресный реестр</b></td></tr> 
                <tr>
                  <td>&nbsp;&nbsp;Доля улиц, несопоставленых НП:</td>
                  <td>'.number_format(100.00*$UnmatchedStreetsRate,2,'.', ' ').'%</td>
                  <td>'.TestX(100.00*$UnmatchedStreetsRate,5).'</td>
                </tr>
                <tr>
                  <td>&nbsp;&nbsp;Доля несопоставленых адресов:</td>
                  <td>'.number_format(100.00*(float)$xml->AddressTest->Summary->ErrorRate,2,'.', ' ').'%</td>
                  <td>'.TestX(100.00*(float)$xml->AddressTest->Summary->ErrorRate,5).'</td>
                </tr>
              
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
	  {	
	    //return '<img src="/img/cross.gif" alt= "Допустимо '.$x0.'"  height="25px" />';
	    return '<font color="red">✘</font>';
	  }
	else
	  {	
	    //return '<img src="/img/tick.gif" height="25px" />';
	   return '<font color="green">✔</font>';
	  }
}



function PrintSummaryItem($xml){
	
	$strPubDate=date("Y-m-d");
	
	$strQAStartDate='2012-07-21';
	$intNumberOfMaps=0;
	$intNumberOfQAPassedMaps=0;
	$intQAIndex1=0;
	$intQAIndex1Max=0;
	
	$intQAIndex2=0;
	$intQAIndex2Max=0;
	$intDateDiff=0;
	
	//Отставание карт
	foreach ($xml->map as $item)
    {
       if( (substr($item->code,0,2)=='RU') and ($item->code!='RU-OVRV'))
       {
       	   $intNumberOfMaps=$intNumberOfMaps+1;
       	   $intDateDiff=DateDiff($item->date, $strPubDate );
       	   $intQAIndex1=$intQAIndex1+$intDateDiff;
       	     if($intDateDiff>$intQAIndex1Max)
       	       $intQAIndex1Max=$intDateDiff;
       	   
       	   if($item->date>$strQAStartDate)
       	   {	    
       	     $intNumberOfQAPassedMaps=$intNumberOfQAPassedMaps+1;
       	     $intQAIndex2=$intQAIndex2+$intDateDiff;
       	     
       	     if($intDateDiff>$intQAIndex2Max)
       	       $intQAIndex2Max=$intDateDiff;	  
       	     //echo $item->code.",".$item->date. ", ". $strPubDate . ", ". DateDiff($item->date, $strPubDate )." <br /> ";
       	   }  
       }	   
    }
    
    $intQAIndex1= (float)$intQAIndex1/(float)$intNumberOfMaps;
	$intQAIndex2= (float)$intQAIndex2/(float)$intNumberOfQAPassedMaps;
	
	
	//Отставание данных валидатора. 
	$xml_stat = simplexml_load_file("statistics.xml");
	$intQAIndex3=0;
	$intQAIndex3Max=0;
	$intDateDiff=0;
	$intNumberOfMapsB=0;
	foreach ($xml_stat->mapinfo as $item)
    {
       if( (substr($item->MapId,0,2)=='RU') and ($item->code!='RU-OVRV'))
       {
       	   $intNumberOfMapsB=$intNumberOfMapsB+1;
       	   $intDateDiff=DateDiff2($item->LastKnownEdit);
       	   $intQAIndex3=$intQAIndex3+$intDateDiff;
       	     if($intDateDiff>$intQAIndex3Max)
       	       $intQAIndex3Max=$intDateDiff;
       	   
       }	   
    }
    
    $intQAIndex3= (float)$intQAIndex3/(float)$intNumberOfMapsB;
    
    
	
	echo '<item>
    <guid>'.'QA-'.$strPubDate.'(test-0)</guid>
    <title>'.'Контроль качества, сводка '.$strPubDate.' </title>
    <link>http://peirce.gis-lab.info/qa</link>
    <author>Ch.S. Peirce</author>
    <pubDate>'.$strPubDate.'</pubDate>
    <description>';
    echo "<![CDATA[";
	
	echo "<h1>Показатели качества ($strPubDate)</h1>";
	echo '<p>Количество карт в группе "Россия" (за все время): '.$intNumberOfMaps.'</p>';
	echo '<p>Количество карт прошедших QA в группе "Россия" (за все время): '.$intNumberOfQAPassedMaps.'</p>';
	
	echo '<p>Показатель латентности I: '.number_format($intQAIndex1,1,'.', ' ') .' дней (Максимальный: '.$intQAIndex1Max.'  дней)</p>';
	echo '<p>Показатель латентности II: '.number_format($intQAIndex2,1,'.', ' ').' дней (Максимальный: '.$intQAIndex2Max.'  дней)</p>';
	echo '<p>Показатель латентности III: '.number_format($intQAIndex3,1,'.', ' ').' дней (Максимальный: '.number_format($intQAIndex3Max,1,'.', ' ').'  дней)</p>';
	
	
		
    echo ']]>';
    echo '</description>
        </item>';
}
function DateDiff($StartDate, $EndDate)
{
  $Y1=substr($StartDate,0,4);
  $M1=substr($StartDate,5,2);
  $D1=substr($StartDate,8,2);
  //echo "$M1,$D1,$Y1 <br />";
  
  $current_date = mktime (0,0,0,date("m") ,date("d"),date("Y"));  //дата сегодня
  $old_date = mktime (0,0,0,$M1,$D1,$Y1); //2004.11.25
  $difference = ($current_date - $old_date); //разница в секундах
  $difference_in_days = ($difference / 86400); //разница в днях
  
 // echo "$StartDate, $EndDate, $difference_in_days  <br /> ";
  return $difference_in_days;
}


function DateDiff2($StartDate)
{
  $Y1=substr($StartDate,6,4);
  $M1=substr($StartDate,3,2);
  $D1=substr($StartDate,0,2);
  
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