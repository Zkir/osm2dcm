<?php
#============================================
#Страница с картами
#(c) Zkir 2008
#============================================
include("ZSitePage.php");


  $zPage=new TZSitePage;
  $postid=$_GET['postid'];
  //$zPage->WriteHtml( "postid=$postid");

  $url = 'http://www.openstreetmap.org/user/Zkir/diary/rss';       //адрес RSS ленты
  $rss = simplexml_load_file($url);       //Интерпретирует XML-файл в объект
  //цикл для обхода всей RSS ленты
  $i=0;
  foreach ($rss->channel->item as $item) {
  	  
  	if($item->guid=='http://www.openstreetmap.org/user/Zkir/diary/'.$postid)
  	{   
    $zPage->WriteHtml( '<div class="post">'."\n");
    $zPage->title='Блог - '.$item->title;
    $zPage->WriteHtml( '<h2> <img src="/img/peirce.jpg" height="65px"  style="float:left;">'.$item->title.'</h2>'."\n"); //выводим на печать заголовок статьи
    //	<img src="img/peirce.jpg" height="75px" align="left">');
   //echo '<p class="meta">'.$item->pubDate.'</p>';
    $contents=$item->description;
    $contents=str_replace('<a href', '<a target="_top" href',$contents);

    $zPage->WriteHtml('<div class="entry">'.$contents.'</div>'."\n");        //выводим на печать текст статьи
    //$zPage->WriteHtml( '<p align="right"><a href="'.$item->link.'" target="_top">'."Комментировать".'</a></p>');       //выводим на печать заголовок статьи
    $zPage->WriteHtml( '<HR/">');
    $zPage->WriteHtml( '<a name="Comment"> </a>');
    $zPage->WriteHtml( '<p>');
    //<img src="/img/peirce.jpg" height="65px"  style="float:left;">');
    $zPage->WriteHtml( '<BR/><i>Нет, это не жежешечка, и оставлять комменты тут нельзя :)<BR/> 
                           <!--Но зато можно высказать свое мнение Дежурному-По-Сайту. -->
                            Если есть вопросы или предложения, лучше всего написать на <a href="http://forum.probki.net/forum/121-osm-karti-dlja-sitigid">наш форум </a>.
                           </i>');
    $zPage->WriteHtml('<p/>');
    $zPage->WriteHtml( '</div>'."\n\n");
    }
  }



  #Выведем содержимое, применяя шаблон.
	$zPage->Output();


?>