<?php
#============================================
#Ежедневные сборки
#(c) Zkir 2010
#============================================
include("ZSitePage.php");

  $zPage=new TZSitePage;
  $zPage->title="Валидатор адресов";
  $zPage->header="Валидатор адресов";

  $mapid=@$_GET['mapid'];
  $errtype=@$_GET['errtype'];

  $zPage->WriteHtml( "<h1>Валидатор адресов</h1>");
  $zPage->WriteHtml('<p align="right"><a href="/addr.php?mapid='.$mapid.'">Назад к таблице</a> </p>' );
  $zPage->WriteHtml('<p>Отображение на карте пока в тестовом режиме, прошу строго не судить :)</p>');
  $zPage->WriteHtml('<p>По клику на маркере открывается JOSM, он должне быть запущен.</p>');
  
  $zPage->WriteHtml('<span id=typeErr><table width=400><tr><td>Выбор типа ошибок:</td>');
  if ( $errtype > 0 ) $zPage->WriteHtml("<td><a href='addr-map.php?mapid=$mapid'>Все</a></td>");
  else $zPage->WriteHtml('<td><b>Все<b></td>');
  for ( $ii = 1; $ii < 7; $ii++ )
  {
    $zPage->WriteHtml('<td>◆</td>');
    if ($ii <> $errtype)
      $zPage->WriteHtml("<td><a href='addr-map.php?mapid=$mapid&errtype=$ii' title='".FormatAddrErrName($ii)."'>".FormatAddrErrType($ii).'</a></td>');
    else
      $zPage->WriteHtml('<td><b>'.FormatAddrErrType($ii).'</b></td>');
  }
  $zPage->WriteHtml("</tr></table>Показаны ошибки типа: <b>".($errtype > 0 ? FormatAddrErrName($errtype) : "Все")."</b></span>");
  
  if($mapid!="")
  {
    PrintMap($mapid,$errtype);
  }
  else
  {
    PrintMap("RU-MOS","");    
  }

 $zPage->Output("1");

/* =============================================================================
     Разные полезные фукции
===============================================================================*/

function PrintMap($mapid,$errtype)
{
  global $zPage;
  $zPage->WriteHtml('
  <div id="cm-example" style="width: 100%; height: 600px"></div> 
  <script type="text/javascript" src="http://tile.cloudmade.com/wml/latest/web-maps-lite.js"></script> 
	   
  <script type="text/javascript" src="./js/js-map.js"> </script> 
  <script type="text/javascript">
      ProcessMap("ADDR_CHK/'.$mapid.'.mp_addr.xml", -1,"'.$errtype.'");
   </script> 
  <iframe id="ttt" src="" style="display:none;"></iframe>');
   
  //Классификатор ошибок
  $zPage->WriteHtml( '<h2><a name="errdescr">Объяснение типов ошибок</a></h2>');
  $zPage->WriteHtml( '<small><table>');
  for ($i=1;$i<=6;$i++)
  {
    $zPage->WriteHtml( '<tr>');
    $zPage->WriteHtml( '<td valign="top"><b>'.FormatAddrErrType($i).'</b></td>');
    $zPage->WriteHtml( '<td valign="top">'.FormatAddrErrName($i).'</td>');
    $zPage->WriteHtml( '<td>'.FormatAddrErrDesc($i).'</td>');
    $zPage->WriteHtml( '</tr>');
  }
  $zPage->WriteHtml( '</table></small>');

}
	

function FormatAddrErrType($number)
{
$str="?";
switch ($number) {
case 0:
    $str="-";
    break;
case 1:
    $str="I";
    break;
case 2:
    $str="II";;
    break;
case 3:
    $str="III";;
    break;
case 4:
    $str="IV";;
    break;
case 5:
    $str="V";;
    break;
case 6:
    $str="VI";;
    break;
}
	
return $str;
}

function FormatAddrErrName($number)
{
$str="?";
switch ($number) {
case 0:
    $str="-";
    break;
case 1:
    $str="Дом вне НП";
    break;
case 2:
    $str="Улица не задана";;
    break;
case 3:
    $str="Улица не найдена";;
    break;
case 4:
    $str="Улица не связана с городом";;
    break;
case 5:
    $str="Дом номеруется по территории";
    break;
case 6:
    $str="Улица не является рутинговой в СГ";
    break;
}
	
return $str;
}
function FormatAddrErrDesc($number)
{
$str="?";
switch ($number) {
case 0:
    $str="-";
    break;
case 1:
    $str='<b>В чем проблема:</b> дом находится вне границ населенного пункта, обозначенных полигоном place=city|town|village|hamlet. <br/>
          <b>Как починить:</b> проверить наличие полигона place, в случае отсутствия добавить.';
    break;
case 2:
    $str='<b>В чем проблема:</b> тег addr:street на доме не заполнен. 
          <b>Как починить:</b> добавить addr:street. ';
    break;
case 3:
    $str='<b>В чем проблема:</b> улица, указанная на доме, в данном НП не обнаружена. Скорее всего это опечатка, например "улица Гибоедова" вместо
          "улицы Грибоедова" или разнобой в порядке статусной части: "проспект Космонавтов" на доме и "Космонавтов проспект" на улице.<br/> 
           <b>Как починить:</b> сделать, чтобы в  addr:street дома было в точности равно name соответствующей улицы.';
    break;
case 4:
    $str='<b>В чем проблема:</b> улица, указанная в теге addr:street дома найдена в некоторой окресности, но она не связана с городом.
          Обычно так бывает, когда значительная часть улицы оказалась вне границ НП (полигона place), или когда  начало и конец улицы лежат в разных населенных пунктах.<br/>
          <b>Как починить:</b> следует проверить границу города. Если граница города правильная, следует разделить вей улицы, создав в месте раздела общую точку с границей НП, так, что бы улица находилась внутри границ НП.
          При этом нужно убрать name c части вея, оставшегося вне НП. Если же граница города неправильная, следует ее откорректировать, чтобы улицы города находились внутри города. ';
    break;
case 5:
    $str='<b>В чем проблема:</b> дом имеет адрес вида <i>город N., 6-й микрорайон, дом 77</i>, т.е. топоним, указанный в addr:street означает не улицу,
          а район, квартал, или некую местность. <br/> 
           Часть адресов такого типа может попадать в категорию III, 
           потому что анализ данного типа ошибок частично эвристический. <br/> 
          <b>Как починить:</b> никак, поддержки адресов такого типа в СитиГиде нет. ';
    break;
case 6:
    $str="<b>В чем проблема:</b> улица с таким названием есть в OSM, но не является рутиговой в СитиГиде. На данный момент это 
          highway=service и highway=pedestrian.<br/>
          <b>Как починить:</b> следует проверить, насколько обосновано улице присвоен статус service.
          Обычно наличие собственного названия и домов с адресами по этой улице есть некий аргумент в поддержку того,
          что это именно улица (highway=residential), а не дворовый проезд (highway=service). 
          Пешеходные улицы (highway=pedestrian) трогать не рекомендуется.";
    break;
}
	
return $str;
}
?>
