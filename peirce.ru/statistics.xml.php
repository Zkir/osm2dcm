<?php
#============================================
#Эта страница имитирует statistics.xml
  header("Content-Type: text/xml");
  echo '<?xml version="1.0" encoding="utf-8"?>'.PHP_EOL;
  echo '<mapstatistics>'.PHP_EOL;
  //Cписок строим по основному списку карт
   $xml = simplexml_load_file("maplist.xml");
   foreach ($xml->map as $item)
    {
        $xmlfilename="ADDR_CHK/".$item->code.".osm.statistics.xml";
        if(file_exists($xmlfilename))
        {
          $xml_stat = @simplexml_load_file($xmlfilename);
		  if ($xml_stat){
			  echo '<mapinfo>'.PHP_EOL;
				  echo '<MapId>'.$xml_stat->mapinfo->MapId.'</MapId>'.PHP_EOL;
				  echo '<MapDate>'.$xml_stat->mapinfo->MapDate.'</MapDate>'.PHP_EOL;
				  echo '<MapName>'.$item->name_ru.'</MapName>'.PHP_EOL;
				  echo '<Square>'.$xml_stat->mapinfo->Square.'</Square>'.PHP_EOL;
				  echo '<NumberOfObjects>'.$xml_stat->mapinfo->NumberOfObjects.'</NumberOfObjects>'.PHP_EOL;
				  echo '<EditsPerDay>'.$xml_stat->mapinfo->EditsPerDay.'</EditsPerDay>'.PHP_EOL;
				  echo '<M14>'.$xml_stat->mapinfo->M14.'</M14>'.PHP_EOL;
				  echo '<M100>'.$xml_stat->mapinfo->M100.'</M100>'.PHP_EOL;
				  echo '<M365>'.$xml_stat->mapinfo->M365.'</M365>'.PHP_EOL;
				  echo '<AverageObjectAge>'.$xml_stat->mapinfo->AverageObjectAge.'</AverageObjectAge>'.PHP_EOL;
				  echo '<ObjectsPerSquareKm>'.$xml_stat->mapinfo->ObjectsPerSquareKm.'</ObjectsPerSquareKm>'.PHP_EOL;
				  echo '<EditsPerDayPerSquareKm>'.$xml_stat->mapinfo->EditsPerDayPerSquareKm.'</EditsPerDayPerSquareKm>'.PHP_EOL;
				  echo '<ActiveUsers>'.$xml_stat->mapinfo->ActiveUsers.'</ActiveUsers>'.PHP_EOL;
				  echo '<LastKnownEdit>'.$xml_stat->mapinfo->LastKnownEdit.'</LastKnownEdit>'.PHP_EOL;
			  echo '</mapinfo>'.PHP_EOL;
		  }           
        
        }
  
    }
  echo '</mapstatistics>'.PHP_EOL;
?>

