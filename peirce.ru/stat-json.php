<?php
//header("Content-type: plain/text");
#============================================
# Статистика в json
#(c) Zkir 2010
#============================================	
  header('Content-Type: text/plain'); 
  require_once("settings.php");
  global $g_SelfUrl;

  $xml = simplexml_load_file($g_SelfUrl."/statistics.xml"); //Интерпретирует XML-файл в объект

  PrintStatistics ($xml,'Россия');



function PrintStatistics($xml, $strGroup)
{
   
   $i=0;
   foreach ($xml->mapinfo as $item)
    {
      if( ($strGroup=='Россия' AND substr($item->MapId,0,2)=='RU') OR 
      	  ($strGroup=='Зарубежье' AND substr($item->MapId,0,2)<>'RU')   )
      {
        $i++;
        $element[$i]="";  
        $element[$i]=$element[$i]."{\n";
        $element[$i]=$element[$i]. '  id: "'.$item->MapId.'",'."\n";
        $element[$i]=$element[$i]. '  MapDate: "'.$item->MapDate.'",'."\n";
        $element[$i]=$element[$i]. '  MapName: "'.$item->MapName.'",'."\n";
        //$element[$i]=$element[$i]. '  Square: '.number_format($item->Square/1000,0,'.', '').",\n";
        $element[$i]=$element[$i]. '  NumberOfObjects: '.$item->NumberOfObjects.",\n";
        $element[$i]=$element[$i]. '  EditsPerDay: '.str_replace(",", ".", $item->EditsPerDay).",\n";
        $element[$i]=$element[$i]. '  M14: '.str_replace(",", ".",$item->M14).",\n";
        $element[$i]=$element[$i].'  M100: '.str_replace(",", ".",$item->M100).",\n";
        $element[$i]=$element[$i]. '  M365: '.str_replace(",", ".",$item->M365).",\n";
        $element[$i]=$element[$i]. '  AverageObjectAge: '.number_format(str_replace(",", ".", $item->AverageObjectAge),0,'.', '').",\n";
        $element[$i]=$element[$i]. '  ObjectsPerSquareKm: '.number_format(str_replace(",", ".",$item->ObjectsPerSquareKm),2,'.', '').",\n";
        $element[$i]=$element[$i].'  EditsPerDayPerSquareKm: '.number_format((((float)str_replace(",", ".",$item->EditsPerDayPerSquareKm))*1000.0) ,1,'.', '').",\n";
        $element[$i]=$element[$i].'  ActiveUsers: '.$item->ActiveUsers."\n";
        $element[$i]=$element[$i]. '}';
      }
    }
  WriteHtml("statJson = \n");
  WriteHtml( '['."\n");
  $n=$i;
  for ($i=1;$i<=$n;$i++)
    {
      WriteHtml($element[$i]);
      if($i<>$n) 
        {WriteHtml(",\n");}  
    }
   
  WriteHtml("\n".']');
}
function WriteHtml($str)
{
echo $str; 
}
?>
