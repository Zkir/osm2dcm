<?php
#============================================
#Ежедневные сборки
#(c) Zkir 2010
#============================================	
include("ZSitePage.php");
require_once("include/misc_utils.php"); 

  $zPage=new TZSitePage;
  $zPage->title="Карты для Ситигид 7.x";
  $zPage->header="Карты для Ситигид 7.x";

  $xml = simplexml_load_file("maplist.xml"); //Интерпретирует XML-файл в объект
  $xml3 = simplexml_load_file("maplist_old.xml"); //Ручные карты
  	  
  $zPage->WriteHtml( "<H1>Карты для Ситигид 7.x</H1>");
  $zPage->WriteHtml('<P> <img src="img/peirce.jpg" height="65px"  style="float:left;"
                        title="Чарльз Сандерс Пирс - знаменитый американский ученый, философ, логик и картограф">
  	                 На этой странице представленны карты Openstreetmap для навигационной программы Ситигид 7.x. 
  	                 Карты для предыдущей версии СГ 5.x все еще можно найти <a href="/maps5.php">здесь<a>. 
                       
                     </P>');
  
  $zPage->WriteHtml( "<H2>Обзорные карты</H2>");
  $zPage->WriteHtml('
      <a href="http://peirce.gis-lab.info/maps7/RU-OVRV.cgmap">RU-OVRV.cgmap</a>  - обзорная карта всей России<br />
      <a href="http://peirce.gis-lab.info/maps7/World-OVRV.cgmap">World-OVRV.cgmap</a> - обзорная карта Мира
      </p>');

//  $zPage->WriteHtml('<H2>Карты с поддержкой пробок</H2>');
//  $zPage->WriteHtml('Карты, представленные в этом разделе поддерживаются пробочным сервисом и корректурами.'); 
//  PrintMapListOld ($xml3,"Карты с пробками");
  
  $zPage->WriteHtml('<H2>Ежедневные карты</H2>');
  $zPage->WriteHtml('"Ежедневные карты" пробками не поддерживаются, но зато обновляются практически каждый день. <br/>
                     <b>Важно</b>: в этот список включаются только те карты, которые прошли <a href="/qa">контроль качества</a>  '); 
    
  $zPage->WriteHtml( "<H3>Россия</H3>");
  PrintMapList ($xml,"Россия");
  
  $zPage->WriteHtml( "<H3>Ближнее зарубежье </H3>");  
  PrintMapList ($xml,"Ближнее Зарубежье");

  $zPage->WriteHtml( "<H3>Дальнее зарубежье </H3>");
  PrintMapList ($xml,"Дальнее Зарубежье");

/*
  $zPage->WriteHtml( "<H2>Все карты, торрент </H2>");
    
  $zPage->WriteHtml( '

      <p>Также, вашему вниманию предлагается <a href="http://peirce.gis-lab.ru/torrents/OSM.CG7.ALL.torrent"><strong>Полное собрание карт OSM для СГ7.x </strong></a>
      в виде торрент-файла.  Скачать можно при помощи, например, <a href="http://www.utorrent.com/intl/ru/">uTorrent</a></p>
      
      <p>В эту раздачу включены все карты,  как <strong>России</strong>, так и <strong>зарубежных стран</strong>.  Последнее обновление торрента - 03.05.2012.
      </p>
      	  
      <p>Размер раздачи составляет около 4 Гб, тем не менее все скачивать не обязательно, можно выбрать только нужные файлы (большинство торрент-клиентов это позволяет).<br />
      Если такой способ распространения карт себя оправдает, раздача будет обновляться.</p>
      
      <p>
      P.S.<br/>
      По техническим причинам, в раздачу не попали две имеющиеся обзорки, Мира и России (экспериметальные)<br />'
      );
    
  */
//  $zPage->WriteHtml( "<H2>См. также </H2>");
//  $zPage->WriteHtml( "Возникли вопросы? Их можно задать <a href="">на форуме!</a>");
  
  
 $zPage->Output();


function PrintMapList($xml, $strGroup)
{
   global $zPage;
   $zPage->WriteHtml( '<table width="500px" class="sortable">
          <tr>
            <td width="80px"><b>Код</b></td>
            <td width="200px"><b>Имя файла</b></td>
            <td><b>Дата обновления</b></td>
            <td><b>Cкачать<b></td>
          </tr>');

  foreach ($xml->map as $item)
    {

      //if(trim($item->group)==$strGroup)
      if( $strGroup==GetMapGroup(substr($item->code,0,2))
        )
      {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td >'.$item->code.'</td>');
        $zPage->WriteHtml( '<td >'.$item->name.'</td>');
        $zPage->WriteHtml( '<td>'.$item->date.'</td>');
       // $zPage->WriteHtml( '<td><a href="'.$item->url.'"> скачать</a></td> </tr>');
        $zPage->WriteHtml( '<td><a href="http://peirce.osm.rambler.ru/static/cg7_maps/'.$item->code.'.cgmap"> скачать</a></td> </tr>');
       
        $zPage->WriteHtml( '</tr>');
      }
    }

  $zPage->WriteHtml( '</table>');
}
function PrintMapListOld($xml, $strGroup)
{
  global $zPage;	 
   $zPage->WriteHtml( '<table width="400px" class="sortable">
          <tr>
            <td width="80px"><b>Код</b></td>
            <td width="200px"><b>Имя файла</b></td><td><b>Дата обновления</b></td>
            <td><b>Cкачать<b></td>
          </tr>');


  foreach ($xml->map as $item)
    {
      if(trim($item->group)==$strGroup AND substr($item->code,0,2)=='RU')
      {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$item->code.'</td>');
        $zPage->WriteHtml( '<td width="200px">'.$item->name.'</td>');
        $zPage->WriteHtml( '<td>'.$item->date.'</td>');
        $zPage->WriteHtml( '<td><a href="'.$item->url.'"> скачать</a></td> </tr>');
        $zPage->WriteHtml('</tr>');
      }
    }

  $zPage->WriteHtml( '</table>');
}


?>
