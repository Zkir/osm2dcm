<?php
#============================================
#(c) Zkir 2010
#============================================
include("ZSitePage.php");

  $zPage=new TZSitePage;
  $mapid=$_GET['mapid'];
  if ($mapid=="")  $mapid="RU-SPO";
  
  $zPage->title="Валидатор адресов";
  $zPage->header="Валидатор адресов";
  $zPage->WriteHtml('<H2>Тест рутингового графа ('.$mapid.')</H2>');

  	  
  $zPage->WriteHtml('
                    <div id="cm-example" style="width: 100%; height: 450px"></div> 
                    <script type="text/javascript" src="http://tile.cloudmade.com/wml/latest/web-maps-lite.js"></script>
                    <script type="text/javascript" src="http://peirce.gis-lab.info/mymap1.js"> </script> 
                    <script type="text/javascript">
                      ProcessMap("ADDR_CHK/'.$mapid.'.mp_addr.xml","");
                    </script> 
                    <img id="ttt" src="" style="display:none;">
  	  ');
  $zPage->WriteHtml('<P>По щелчку на маркере открывается JOSM, он должен быть запущен</P>');

 $zPage->Output("1");
?>

