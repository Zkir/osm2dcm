<?php
#==================================================
# Шаблон страницы для z-site :)
# (с) Zkir, 2008
#==================================================

# Используется класс TZSitePage
# В нем должны быть объявлены переменные:
# $title -   Заголовок страницы;
# $header -  Заголовок  (H1);
# $content - содержимое ;

  if(!isset($this))
    {
      die ("Содержимое страницы не определено");
    }
//Боковой блок - текст
function PrintSideBlock($header,$text)
{
   echo '<div class="sidebar1">
         <div class="sidebar1-top"></div>
         <div class="listasidebar1" class="box">
         <div class="sidebar1-text">';
   echo  "<h2>$header</h2>";
   echo  $text; 	   
   echo ' </div>
          </div>
          <div class="sidebar1-bottom"></div>
          </div>';	
}

//Боковой блок - Новости
function PrintSideBlockNews()
{
  echo '<div class="sidebar1">
        <div class="sidebar1-top"></div>
        <div class="listasidebar1" class="box">
        <div class="sidebar1-text">
        <h2>Новости</h2>';
        
  $url = 'http://www.openstreetmap.org/user/Zkir/diary/rss';       //адрес RSS ленты
  $rss = simplexml_load_file($url);       //Интерпретирует XML-файл в объект
  //цикл для обхода всей RSS ленты
  $i=0;
  foreach ($rss->channel->item as $item)
   {
    if (substr($item->title,0,1)!="*")  {
    echo '<div class="post">'."\n";
    echo '<p>';
            
    //$blog_link=$g_SelfUrl.'/blog.php?postid='.substr($item->link,45,5);
    $blog_link=$g_SelfUrl.'/blog/'.substr($item->link,45,5);
    //выводим на печать заголовок новости
    echo '<small><b><a href="'.$blog_link.'" target="_top">'.str_replace("[Карты OSM для СитиГИДа]","",$item->title).'</a></b></small>'."\n";
    $contents=$item->description;
    $contents=str_replace('<a href', '<a target="_top" href',$contents);

    #echo mb_substr (strip_tags($contents),0,101)."\n";        //выводим на печать текст новости
            
    echo '</p>';
    echo  '</div>'."\n\n";
    $i=$i+1;
    if ($i>=10)
      {
  	    break;
      }
    }
   }   
       
  echo '</div>
        </div>
        <div class="sidebar1-bottom"></div>
        </div>';
}	    
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" >
   <head>
  <meta http-equiv="content-type" content="text/html; charset=utf-8" />
  <link rel="stylesheet" type="text/css" href="/skins/kotelnikov/style.css" />
  <title>
    <?php
      echo $this->title;
    ?>

  </title>
  <script src="/sorttable.js" type="text/javascript"> </script>

 
  </head>
  <body class="claro"><div id="box">
   <!-- header -->
   <div id="header">
    <!-- logo -->
    <div id="logo">
     <a href="/"><img src="/skins/kotelnikov/img/logo.png" 
      title="OpenStreetMap - это свободная карта всего мира, которую может редактировать каждый" 
      alt="Карты OpenStreetMap для CитиГида" style="border: 0px" /></a>
    </div>

   </div>
    <!-- menu -->
       <div id="menu">
        <ul>
            <?php
            echo '<li id="active"><a href="'.$g_SelfUrl.'">'._('Новости').'</a></li>
                  <li><a href="'.$g_SelfUrl.'/daily">'._('Карты').'</a></li>';
            echo  '<li><a href="'.$g_SelfUrl.'/qa">'._('Контроль качества').'</a></li>';       
            //echo  '<li><a href="'.$g_SelfUrl.'/maps5.php">для  СГ 5.x</a></li>';
            echo  '<li><a href="'.$g_SelfUrl.'/stat">'._('Статистика 1').'</a></li>';
            echo  '<li><a href="'.$g_SelfUrl.'/stat2">'._('Статистика 2').'</a></li>';
          
            
                 // <li><a href="http://wiki.openstreetmap.org/wiki/RU:%D0%A1%D0%B8%D1%82%D0%B8%D0%93%D0%98%D0%94">ЧаВо</a></li>';
            echo  '<li><a href="http://forum.probki.net/forum/121-osm-karti-dlja-sitigid/">'._('Форум').'</a></li>';
                             
            ?>
        </ul>
  </div>


  <div id="wrap">
    <!-- content -->
    
    <?php
    if ($UseWide!="1")
	{
      echo '<div id="content">
            <div id="content-top"></div>
            <div id="lista" class="box">
            <div id="content-text" >';
      echo $this->content;
      
      echo '</div>
            </div>
            <div id="content-bottom"></div>
            </div>';
    }
    else
    {
      echo '<div id="content-wide">
	        <div id="content-wide-top"></div>
	        <div id="lista-wide" class="box">
	        <div id="content-wide-text">';
	  
	  echo $this->content;
	  echo '</div>
	        </div>
            <div id="content-wide-bottom"></div>
            </div>'; 
    }
        
        
        
     
   		
    if ($UseWide!="1")
    {	

     //Дежурный-По-Сайту
    /*'<p>Если у вас возникли вопросы, задайте их нашему онлайн-консультанту.
                   Даже если он не знает ответ на ваш вопрос, разговор <b>будет записан</b>
                   и мы постараемся ответить этот вопрос позже:</p> */
    PrintSideBlock('Дежурный-По-Сайту',
                  '<p>Есть вопрос? Самое время его задать. Мы обязательно на него ответим:</p>
                   <p>
                     <!--<iframe src="http://ai.zkir.ru/inf?bot_id=00000000-0000-0000-0000-000000000001" height="200" width="330" frameborder="0" scrolling="no" >  
                     </iframe>
                     <iframe src="http://ask.fm/widget/16578df4e26fce1dd76ad4aa05a8c3cc91b5eeaf?stylesheet=large&fgcolor=%23000000&bgcolor=%23EFEFEF&lang=2" frameborder="0" scrolling="no" width="330" height="200" style="border:none;"></iframe>
                     -->
                     <iframe src="http://ask.fm/widget/87768842a61cbcb377f2322b2e1b9c5f5e9ae926?stylesheet=large&fgcolor=%23000000&bgcolor=%23ffffff&lang=2" frameborder="0" scrolling="no" width="330" height="200" style="border:none;"></iframe>
                   <br />
                   <a href="/#QnA"><i>Почитать ответы...</i> </a> </p>');
           
       
    PrintSideBlock('СитиГид',
          '<img style="float:left;position:relative;top:-7px;" src="/img/cg_logo.gif">
          <p><strong>СитиГид</strong> - это популярная программа-навигатор с поддержкой пробок.
             Существуют версии практически для всех
             распространенных платформ: Windows Mobile, WinCE (автонавигаторы, PNA), Android, Symbian, iPhone.
             Ее можно найти (и приобрести) <a href="http://probki.net">на сайте производителя</a>. ');

    PrintSideBlock('Что такое OSM?',
          '<img style="float:right;position:relative;top:-7px;" src="/img/osm_logo.png">
		  <p><a href="http://openstreetmap.org">OpenStreetMap</a> — это свободно редактируемая карта всего мира.
		  Она сделана такими же людьми, как и вы.</p>
          <p><a href="http://openstreetmap.org">OpenStreetMap</a> позволяет совместно просматривать,
          изменять и использовать географические данные в любой точке Земли.</p>');
   
   
   /*<!-- Линия жизни -->
    PrintSideBlock('Линия Жизни',
		          '<p align="center"><a href="http://life-line.ru" 
		   	           target="_blank">
		   	           <img src="http://life-line.ru/files/banners/life-line_240x400_sms.gif" 
		   		           _fcksavedurl="http://life-line.ru/files/banners/life-line_240x400_sms.gif" 
		    		           alt="Помоги детям!" >
		   		  </a></p>'); */
   
   //<!-- Новости -->
    PrintSideBlockNews();
   

  
   
    PrintSideBlock('Гис-Лаб', 
		          '<img style="float:right;position:relative;top:-7px" src="http://gis-lab.info/images/gis-lab-button.gif"/> 
		           <p> <a href="http://gis-lab.info">Gis-Lab.info</a> - сообщество специалистов в области геоинформационных систем (ГИС)
		              и дистанционного зондирования Земли (ДЗЗ). 
		              В том числе, GIS-Lab занимается развитием ГИС с открытым исходным кодом и 
		              проектами по созданию открытых источников данных.');
   
    PrintSideBlock('Полезные ссылки',
          '<p><ul>
		  <li><a href="http://openstreetmap.org">OpenStreetMap.org</a></li>
		  <li><a href="http://wiki.openstreetmap.org/wiki/Ru:Main_Page">OSM Wiki (на русском)</a></li>
		  <li><a href="http://forum.openstreetmap.org/viewforum.php?id=21">Русскоязычный форум OSM</a></li>
		  <li><a href="http://gis-lab.info/qa/osm-begin.html">Начало работы с OSM</a></li>
		  <li><a href="http://wiki.openstreetmap.org/index.php/Ru:Beginners_Guide">Руководство для начинающих</a></li>
		  <li><a href="http://gis-lab.info/data/mp/">Карты OSM для Garmin</a></li> 
  		  <li><a href="http://navitel.osm.rambler.ru/">Карты OSM для Navitel</a></li>
		  <ul></p>');  
	}
   ?>		
 


    <!-- Footer -->
    <div id="footer">

     <div id="footertext">

    &copy; Карты — участники проекта <a href="http://openstreetmap.org">OpenStreetMap</a>, 
    по лицензии <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>.<br />
	<!--Преобразование использует (в том числе) конвертор <a href="http://wiki.openstreetmap.org/index.php?title=Ru:Osm2mp">osm2mp</a> от <a href="http://forum.openstreetmap.org/profile.php?id=927">liosha</a>.<br />
	Cборки карт, конфиги: <a href="http://forum.openstreetmap.org/profile.php?id=2739">Zkir</a>.<br /> -->
	Дизайн страницы на основе макета <a href="http://kotelnikov.net">Владимира Котельникова</a>.<br />
	Cайт существует при поддержке <a href="http://gis-lab.info">Гис-Лаб </a>.
	   	   <!-- Yandex.Metrika -->
		<script src="//mc.yandex.ru/metrika/watch.js" type="text/javascript"></script>
		<div style="display:none;"><script type="text/javascript">
		try { var yaCounter1224821 = new Ya.Metrika(1224821); } catch(e){}
		</script></div>
		<noscript><div style="position:absolute"><img src="//mc.yandex.ru/watch/1224821" alt="" /></div></noscript>
		<!-- /Yandex.Metrika -->
     </div>
    </div>

   </div></div>


  </body>
</html>
