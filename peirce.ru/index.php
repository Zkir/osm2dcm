<?php
#============================================
#Страница с картами
#(c) Zkir 2008
#============================================
include("ZSitePage.php");


  $zPage=new TZSitePage;

  $zPage->title="Новости -- Карты OSM для СитиГида";
  $zPage->WriteHtml('<h1>Карты OSM для СитиГида</h1>'."\n"); 
  $zPage->WriteHtml('<P> <img src="img/peirce.jpg" height="65px"  style="float:left;"
  	                      title="Чарльз Сандерс Пирс - знаменитый американский ученый, философ, логик и картограф">
  	                 Приветствую тебя, путешественник! <BR/>
  	                 Что-то мне говорит, что ты ищешь карты. Ты попал туда, куда нужно.  
                     Здесь представлены карты из проекта OpenStreetMap, преобразованные для навигационной системы 
                     <b>СитиГИД</b>.
                     У нас есть карты <a href="http://peirce.gis-lab.info/misc.php">европейских стран для Ситигида</a>
                     а так же <a href="http://peirce.gis-lab.info/daily.php">ежедневные сборки</a> регионов России
                     на базе данных OSM.
                       
                     </P>');

  $url = 'http://www.openstreetmap.org/user/Zkir/diary/rss';       //адрес RSS ленты
  $rss = simplexml_load_file($url);       //Интерпретирует XML-файл в объект
  //цикл для обхода всей RSS ленты
  $i=0;
  foreach ($rss->channel->item as $item) {
  	if (substr($item->title,0,1)!="*")  {
    $zPage->WriteHtml( '<div class="post">'."\n");
    $zPage->WriteHtml( '<h2>'.str_replace("[Карты OSM для СитиГИДа]","",$item->title).'</h2>'."\n"); //выводим на печать заголовок статьи
    //$zPage->WriteHtml(substr($item->title,1,1));
    $contents=$item->description;
    $contents=str_replace('<a href', '<a target="_top" href',$contents);
     //выводим на печать текст статьи
    if (strlen($contents)<2000)
      { 
       $zPage->WriteHtml('<div class="entry">'.$contents.'</div>'."\n");
      }
    else
    {
    	$blog_link=$g_SelfUrl.'/blog.php?postid='.substr($item->link,45,5);
    	$zPage->WriteHtml('<div class="entry">'.substr($contents,0,1000).'... <a href="'.$blog_link.'">/читать дальше/<a></div>'."\n");
    }          
    //$zPage->WriteHtml( '<p align="right"><a href="'.$blog_link.'#Comment'.'" >'."Комментировать".'</a></p>');      
    //$zPage->WriteHtml('<p/>');
    $zPage->WriteHtml( '</div>'."\n\n");
    $i=$i+1;
    if ($i>=$g_BlogPostNumber)
    {
  	    break;
    }
  }  
  }



  #Выведем содержимое, применяя шаблон.
	$zPage->Output();


?>