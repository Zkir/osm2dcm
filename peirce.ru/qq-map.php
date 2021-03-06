<?php
#============================================
#Ежедневные сборки
#(c) Zkir 2010
#============================================
include("ZSitePage.php");
require_once("include/misc_utils.php"); 

  $zPage=new TZSitePage;
  $zPage->title="Контроль качества";
  $zPage->header="Контроль качества";

  $mapid=@$_GET['mapid'];
  $test=@$_GET['test'];
  $errtype=@$_GET['errtype'];

  switch ($test){
  case "rd":
    $zPage->WriteHtml( "<h1>Дубликаты рутинговых ребер</h1>");
    $zPage->WriteHtml('<p align="right"><a href="/qa/'.$mapid.'">Назад к таблице</a> </p>' );
    $zPage->WriteHtml('<p>На этой странице показываются дубликаты рутинговых ребер. Дубликаты рутинговых ребер мешают
  	                   прокладке маршрутов и расстановке запретов поворотов. <a href="http://peirce.gis-lab.info/blog/16019">Подробнее...</a> 
                      </p>');
  
    $zPage->WriteHtml('<p>По клику на маркере открывается JOSM, он должен быть запущен.</p>');
  
  
  
    if($mapid!="")
    {
      PrintMap("rd",$mapid,$errtype);
    }
    else
    {
      PrintMap("rd","RU-SPO","");    
    }
  break;
  case "hwc":
    $zPage->WriteHtml('<h1>Просроченные перекрытия дорог ('.$mapid.') </h1>');
    $zPage->WriteHtml('<p align="right"><a href="/qa/'.$mapid.'">Назад к таблице</a> </p>' );
    $zPage->WriteHtml('<p>По клику на маркере открывается JOSM, он должен быть запущен.</p>');
    
    PrintMapAlt("hwc", $mapid,$errtype);
    break;
  case "dnodes":
    $zPage->WriteHtml('<h1>Тупики магистралей ('.$mapid.') </h1>');
    $zPage->WriteHtml('<p align="right"><a href="/qa/'.$mapid.'">Назад к таблице</a> </p>' );
       
    $zPage->WriteHtml('<p>'.GetDeadEndsTestDescription().'</p>');
    
    PrintMap("dnodes", $mapid,$errtype);
    $zPage->WriteHtml('<p>По клику на маркере открывается JOSM, он должен быть запущен.</p>');
    $zPage->WriteHtml('<h2>Как починить</h2>');
    $zPage->WriteHtml('<p>Следует, в зависимости от ситуации, либо скорректировать статус дороги,
                          либо исправить геометрию - соединить вершины, восстановить удаленные участки дорог. 
                         <a href="http://peirce.gis-lab.info/blog.php?postid=17547">Подробнее...</a> </p>');
    
    break;
    
  case "addr-street":
    $zPage->WriteHtml('<h1>Улицы, не сопоставленные населенным пунктам('.$mapid.') </h1>');
    $zPage->WriteHtml('<p align="right"><a href="/qa/'.$mapid.'">Назад к таблице</a> </p>' );
       
    $zPage->WriteHtml('<p>'.'В этом тесте показываются улицы, не попавшие в адресный поиск, потому что они не находятся внутри полигонов place=*'.'</p>');
    
    PrintMap("addr-street", $mapid, $errtype);
    $zPage->WriteHtml('<p>По клику на маркере открывается JOSM, он должен быть запущен.</p>');
    //$zPage->WriteHtml('<h2>Как починить</h2>');
    //$zPage->WriteHtml('<p>...</p>');
    
    break;      
  default: 
    $zPage->WriteHtml('<p>Неизвестный тест "'.$test.'"</p>');
  }
  
  $zPage->WriteHtml('<h2>Другие тесты</h2>');
  PrintTestNavigator($mapid);

 
  $zPage->Output("1");

/* =============================================================================
     Разные полезные фукции
===============================================================================*/

function PrintMap($test, $mapid,$errtype)
{
	global $zPage;
	$zPage->WriteHtml('
		<div id="cm-example" style="width: 100%; height: 450px"></div>

		<link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.6.3/leaflet.css" />
		<script src="http://cdn.leafletjs.com/leaflet-0.6.3/leaflet.js"></script>

		<script type="text/javascript" src="/js/qa-map.js"> </script>

		<script src="/js/leaflet.markercluster.js"></script>
		<script src="/js/Permalink.js" ></script>
		<link rel="stylesheet" href="/js/MarkerCluster.css" />
		<link rel="stylesheet" href="/js/MarkerCluster.Default.css" />

		<script type="text/javascript">
		ProcessMap("'.$test.'","/ADDR_CHK/'.$mapid.'.mp_addr.xml","'.$errtype.'");
		</script>

		<iframe id="ttt" src="" style="display:none;"></iframe>
  	');
}

function PrintMapAlt($test, $mapid,$errtype)
{
  global $zPage;
  $zPage->WriteHtml('
    <div id="cm-example" style="width: 100%; height: 600px"></div> 
    <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.6.3/leaflet.css" />
    <script src="http://cdn.leafletjs.com/leaflet-0.6.3/leaflet.js"></script>
	   
    <script type="text/javascript" src="/js/qa-map.js"> </script> 

    <script src="/js/leaflet.markercluster.js"></script>
		<link rel="stylesheet" href="/js/MarkerCluster.css" />
		<script src="/js/Permalink.js" ></script>
		<link rel="stylesheet" href="/js/MarkerCluster.Default.css" />

    <script type="text/javascript">
       ProcessMap("'.$test.'","/ADDR_CHK/'.$mapid.'.hwconstr_chk.xml","'.$errtype.'");
    </script> 
    <iframe id="ttt" src="" style="display:none;"></iframe>');
   
 
}


                  
                  
                  
                  


?>
