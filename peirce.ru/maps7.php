<?php
#============================================
#Ежедневные сборки
#(c) Zkir 2010
#============================================	
include("ZSitePage.php");
require_once("include/misc_utils.php"); 


   // Задаем текущий язык проекта
    //putenv("LANG=ru_RU"); 
    //putenv("LANG=en_US"); 
    putenv("LANG=pt"); 
    

    // Задаем текущую локаль (кодировку)
    //setlocale (LC_ALL,"Russian");
    setlocale (LC_ALL, "pt_BR");

    // Указываем имя домена
    $domain = 'default';

    // Задаем каталог домена, где содержатся переводы
    bindtextdomain ($domain, "./locale");

    // Выбираем домен для работы

    textdomain ($domain);

    // Если необходимо, принудительно указываем кодировку
    // (эта строка не обязательна, она нужна,
    // если вы хотите выводить текст в отличной
    // от текущей локали кодировке).
    bind_textdomain_codeset($domain, 'UTF-8');


  $zPage=new TZSitePage;
  $zPage->title=_("Карты для Ситигид 7.x");
  $zPage->header=_("Карты для Ситигид 7.x");

  $xml = simplexml_load_file("maplist.xml"); //Интерпретирует XML-файл в объект
  $xml3 = simplexml_load_file("maplist_old.xml"); //Ручные карты
  	  
  $zPage->WriteHtml( "<H1>"._("Карты для Ситигид 7.x")."</H1>");
  $zPage->WriteHtml('<P> <img src="/img/peirce.jpg" height="65px"  style="float:left;"
                        title="'._('Чарльз Сандерс Пирс - знаменитый американский ученый, философ, логик и картограф').'">
  	                 '._('На этой странице представленны карты Openstreetmap для навигационной программы Ситигид 7.x.').' 
  	                 '._('Карты для предыдущей версии СГ 5.x все еще можно найти <a href="/maps5.php">здесь</a>.').' 
                       
                     </P>');
  /*
  $zPage->WriteHtml( "<H2>Обзорные карты</H2>");
  $zPage->WriteHtml('
      <a href="http://peirce.gis-lab.info/maps7/World-OVRV.cgmap"><b>World-OVRV</b></a> - обзорная карта Мира, для 7.2 <br/>
      <b>RU-OVRV.cgmap</b>  - обзорная карта всей России (см. ниже, в общем списке)<br />
      </p>');
*/
//  $zPage->WriteHtml('<H2>Карты с поддержкой пробок</H2>');
//  $zPage->WriteHtml('Карты, представленные в этом разделе поддерживаются пробочным сервисом и корректурами.'); 
//  PrintMapListOld ($xml3,"Карты с пробками");
  $group=$_GET['group'];
  $zPage->WriteHtml('<H2>'._('Ежедневные карты').'</H2>');
  $zPage->WriteHtml(_('"Ежедневные карты" пробками не поддерживаются, но зато обновляются практически каждый день.').'<br/>');

  
  if ($group!='')  
  {
  	  $zPage->WriteHtml( "<H3>$group</H3>");
	  PrintMapList ($xml,$group);
  }
  else
  {	  	  
	  $zPage->WriteHtml( "<H3>Россия</H3>");
      $zPage->WriteHtml(_('<p><b>Важно</b>: в этот список включаются только те карты, которые прошли <a href="/qa">контроль качества</a>.</p> ')); 
	  PrintMapList ($xml,"Россия");
	  
	  $zPage->WriteHtml( "<H3>Ближнее зарубежье </H3>");  
	  PrintMapList ($xml,"Ближнее Зарубежье");

	  $zPage->WriteHtml( "<H3>Дальнее зарубежье </H3>");
	  PrintMapList ($xml,"Дальнее Зарубежье");
  }

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
      if( 
      	  ($strGroup==GetMapGroup(substr($item->code,0,2))) or
      	  ($strGroup==substr($item->code,0,2) )
        )
      {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td >'.$item->code.'</td>');
        $zPage->WriteHtml( '<td >'.$item->name_ru.'</td>');
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
