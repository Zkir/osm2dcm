<?php
#============================================
#(c) Zkir 2010
#============================================
include("ZSitePage.php");
require_once("include/misc_utils.php"); 

$zPage=new TZSitePage;


/* Zkir: cтраница имеет два параметра
 mapid - код карты и
 level - уровень графа дорог, для которого показываются подграфы
*/
  $mapid=$_GET['mapid'];
  if ($mapid=="")  $mapid="RU-SPO";

  $level=$_GET['level'];
  if ($level=="")  $level="4";

  $zPage->title="Валидатор дорожного графа";
  $zPage->header="Валидатор дорожного графа";
  #$zPage->WriteHtml('<h2>Тест рутингового графа ('.$mapid.', дороги: '.mb_strtolower (FormatRoutingLevelName($level), 'UTF-8'  ).')</h2>');

  $zPage->WriteHtml('<h2>Тест рутингового графа ('.$mapid.', дороги: '. FormatRoutingLevelName($level).')</h2>');


  if ($mapid!="RU")
  {
  $zPage->WriteHtml('<p style="text-align:right"><a href="/qa/'.$mapid.'">
                     Назад к таблице</a> </p>' );
  }
  $zPage->WriteHtml('<p>Показываются "изоляты", т.е. дороги или группы дорог,
                     не связанные с основным дорожным графом. ' );
  $zPage->WriteHtml('<a href="http://peirce.gis-lab.info/blog/14435">
                     Подробнее...</a> </p>' );
  $zPage->WriteHtml('<p>Почему "изоляты" это так плохо? Потому что они мешают
                     рутингу, прокладке маршрута. Когда старт и финиш оказываются
                     в разных подграфах, маршрут не строится. </p> ' );

 $zPage->WriteHtml('<p>Почему должна соблюдаться связность по уровням? Потому значение тега highway используется для генерализации при построения обзорных карт
                     При выборке дорог определенного уровня (например, только trunk, или trunk и primary) должен получаться связный граф, пригодный для навигации
                    (прокладки маршрутов) а не бессмысленный лес из не связанных между собой палочек. </p> ' );


  $zPage->WriteHtml('<table><tr>
                       <td><b>Уровень дорог:</b></td>');
  for($i=4;$i>=0;$i--)
  {
     $zPage->WriteHtml('<td>');
     if (($i<>$level) and !( (($i==4) or ($i==3) )and $mapid=="RU") )
       $zPage->WriteHtml('<a href="/qa/'.$mapid.'/routing-map/'.$i.'">'.FormatRoutingLevelName($i).'</a>');
     else{
       if (!( (($i==4) or ($i==3) )and $mapid=="RU" )){ 	  
         $zPage->WriteHtml('<b>'.FormatRoutingLevelName($i).'</b>');}
       else
       {$zPage->WriteHtml(''.FormatRoutingLevelName($i).'');} 
     }    
     $zPage->WriteHtml('</td>');
     if($i>0)
     {
       $zPage->WriteHtml('<td>◆</td>');
     }
  };
  $zPage->WriteHtml('</tr> </table> ');
  $zPage->WriteHtml('
		<div id="cm-example" style="width: 100%; height: 450px"></div>

                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.8.0/dist/leaflet.css"
                  integrity="sha512-hoalWLoI8r4UszCkZ5kL8vayOGVae1oxXe/2A4AO6J9+580uKHDO3JdHb7NzwwzK5xr/Fs0W40kiNHxM9vyTtQ=="
                  crossorigin=""/>
                <script src="https://unpkg.com/leaflet@1.8.0/dist/leaflet.js"
                  integrity="sha512-BB3hKbKWOc9Ez/TAwyWxNXeoV9c1v6FIeYiBieIWkpLjauysF18NzgR1MBNBXf8/KABdlkX68nAhlwcDFLGPCQ=="
                  crossorigin=""></script>

		<script type="text/javascript" src="/js/qa-map.js"> </script>

		<script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet.markercluster/1.4.1/leaflet.markercluster.js"></script>
		<script src="/js/Permalink.js" ></script>
		<link rel="stylesheet" href="/js/MarkerCluster.css" />
		<link rel="stylesheet" href="/js/MarkerCluster.Default.css" />

		<script type="text/javascript">
		ProcessMap("isltd-hw", "/ADDR_CHK/'.$mapid.'.mp_addr.xml", "'.$level.'");
		</script>

		<iframe id="ttt" src="" style="display:none;"></iframe>
  	  ');
  $zPage->WriteHtml('<p>По щелчку на маркере открывается JOSM, он должен быть запущен.</p>');
  
 
  $zPage->WriteHtml('<h2>Другие тесты</h2>');
  PrintTestNavigator($mapid);
  $zPage->Output("1");

function FormatRoutingLevelName($level)
{
$str="?";
switch ($level) {

case 0:
    $str="Только столбовые";
    break;
case 1:
    $str="Первичные и выше";
    break;
case 2:
    $str="Вторичные и выше";
    break;
case 3:
    $str="Третичные и выше";
    break;
case 4:
    $str="Все";
    break;
}

return $str;

}

?>
