<?php
#============================================
#(c) Zkir 2010
#============================================
include("ZSitePage.php");


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
  $zPage->WriteHtml('<h2>Тест рутингового графа ('.$mapid.', дороги: '.mb_strtolower (FormatRoutingLevelName($level), 'UTF-8'  ).')</h2>');
  $zPage->WriteHtml('<p style="text-align:right"><a href="/addr.php?mapid='.$mapid.'">
                     Назад к таблице</a> </p>' );
  $zPage->WriteHtml('<p>Показываются "изоляты", т.е. дороги или группы дорог,
                     несвязанные с основным дорожным графом. ' );
  $zPage->WriteHtml('<a href="http://peirce.gis-lab.info/blog.php?postid=14435">
                     Подробнее...</a> </p>' );
  $zPage->WriteHtml('<p>Почему "изоляты" это так плохо? Потому что они мешают
                     рутингу, прокладке маршрута. Когда старт и финиш оказывается
                     в разных подграфах, маршрут не строится. </p> ' );

  $zPage->WriteHtml('<table><tr>
                       <td><b>Уровень дорог:</b></td>');
  for($i=4;$i>=0;$i--)
  {
     $zPage->WriteHtml('<td>');
     if ($i<>$level)
       $zPage->WriteHtml('<a href="routing-map.php?mapid='.$mapid.'&level='.$i.'">'.FormatRoutingLevelName($i).'</a>');
     else
       $zPage->WriteHtml('<b>'.FormatRoutingLevelName($i).'</b>');
     $zPage->WriteHtml('</td>');
     if($i>0)
     {
       $zPage->WriteHtml('<td>◆</td>');
     }
  };
  $zPage->WriteHtml('</tr> </table> ');
  $zPage->WriteHtml('
                    <div id="cm-example" style="width: 100%; height: 450px"></div>
                    <script type="text/javascript" src="http://tile.cloudmade.com/wml/latest/web-maps-lite.js"></script>
                    <script type="text/javascript" src="js/routing-map.js"> </script>
                    <script type="text/javascript">
                      ProcessMap("ADDR_CHK/'.$mapid.'.mp_addr.xml","'.$level.'");
                    </script>
                    <img id="ttt" src="" style="display:none;" alt="Dummy item for JOSM" />
  	  ');
  $zPage->WriteHtml('<p>По щелчку на маркере открывается JOSM, он должен быть запущен.</p>');

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
