<?php
// Счетчик по первому файлу: 
$download=$_GET['mapid'];
if (empty($download))
 {
  header("location: http://http://peirce.gis-lab.info"); //Здесь указываете путь к файлу, который нужно скачать
}
else 
{
  //header("location: http://osm.interlan.ru/cg_maps/$download.rar"); //Здесь указываете путь к файлу, который нужно скачать
  header("location: http://peirce.osm.rambler.ru/cg_maps/$download.rar"); //Здесь указываете путь к файлу, который нужно скачать
  
  //echo "<html>";
  //echo "download ".$download;
  //echo "</html>";
  $file=fopen("download_log.txt","a+"); //book1.txt - это имя файла, в котором будет храниться статистика закачек
  flock($file,LOCK_EX); 
  fwrite($file,$download."|".date("Y-m-d H:i:s")."\n"); 
  flock($file,LOCK_UN); 
  fclose($file); 
  
}	
?>