<?php
#============================================
#Ежедневные сборки
#(c) Zkir 2010
#============================================
include("ZSitePage.php");

  $zPage=new TZSitePage;
  $zPage->title="Контроль качества";
  $zPage->header="Контроль качества";

  $mapid=@$_GET['mapid'];
  $test=@$_GET['test'];
  $errtype=@$_GET['errtype'];

  $zPage->WriteHtml( "<h1>Дубликаты рутинговых ребер</h1>");
  $zPage->WriteHtml('<p align="right"><a href="/qc/'.$mapid.'">Назад к таблице</a> </p>' );
  $zPage->WriteHtml('<p>На этой странице показываются дубликаты рутинговых ребер. Дубликаты рутинговых ребер мешают
  	                 прокладке маршрутов и расстановке запретов поворотов. <a href="http://peirce.gis-lab.info/blog/16019">Подробнее...</a> 
                    </p>');
  
  $zPage->WriteHtml('<p>По клику на маркере открывается JOSM, он должен быть запущен.</p>');
  
  
  
  if($mapid!="")
  {
    PrintMap($mapid,$errtype);
  }
  else
  {
    PrintMap("RU-SPO","");    
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
	   
  <script type="text/javascript" src="/js/qq-map.js"> </script> 
  <script type="text/javascript">
      ProcessMap("/ADDR_CHK/'.$mapid.'.mp_addr.xml","'.$errtype.'");
   </script> 
  <iframe id="ttt" src="" style="display:none;"></iframe>');
   
 
}
	





?>
