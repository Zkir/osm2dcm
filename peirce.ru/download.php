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
  
  
}	
?>