<?php
#============================================
#Страница с картами
#(c) Zkir 2008
#============================================
include("ZSitePage.php");


  $zPage=new TZSitePage;
  $mapid=$_GET['mapid'];
  
    
  $url = 'maplist.xml';               //Cписка карт
  $xml = simplexml_load_file($url);       //Интерпретирует XML-файл в объект
  //цикл для обхода всех записей
  $i=0;
  foreach ($xml->map as $item) {
  	  
  	 if ($item->code==$mapid)  
  	   PrintMapInfo($item);
    
  }
  $zPage->WriteHtml( '<HR/">');
  $zPage->WriteHtml( '<p><img src="img/peirce.jpg" height="65px"  style="float:left;">');
  $zPage->WriteHtml( '<BR/><i>Нет, это не жежешечка, и оставлять комменты тут нельзя :) 
	                       Но зато можно высказать свое мнение Дежурному-По-Сайту.</i>');
  $zPage->WriteHtml('<p/>');


  #Выведем содержимое, применяя шаблон.
	$zPage->Output();

function PrintMapInfo($map)
{
  global $zPage;
	  
  $zPage->WriteHtml(' <h2>'.$map->name.'</h2>');
  $zPage->title=$map->name;
  
  $zPage->WriteHtml('<P><b>Название карты</b>:'.$map->name.'</P>');
  //$zPage->WriteHtml('<P>(c) СС-BY-SA, участники проекта OpenStreetMap</P>');
  $zPage->WriteHtml('<P><b>Код карты</b>:'.$map->code.'</P>');
  $zPage->WriteHtml('<P><b>Дата сборки</b>:'.$map->date.'</P>');
  
  $zPage->WriteHtml('<P>Эта сборка основана на данных проекта <a href="www.osm.org">OpenStreetMap</a> 
   и распространяется по лицензии <a href="http://creativecommons.org/licenses/by-sa/2.0">СС-BY-SA</a>.<BR/> ');
  $zPage->WriteHtml('Вопросы и пожелания просьба отправлять
  	                 на  форум <a href="http://forum.probki.net/forum_posts.asp?TID=7768">Ситигида </a>');

  $zPage->WriteHtml('<P align="center"><img src="img/osm_logo.png" height="30px" / ><b>
                     <a href="'.$map->url.'">Скачать!</a></b></P>');

}
?>