<?php
//Почини меня - недоступные ребра. Пьявка к валидатору OSRM
if ($_GET['permalink']!="")
{
  $blnPermalink=True;
  $coord = explode(",", $_GET['permalink']);
}
else
{
	$blnPermalink=False;
}	
echo
	'<html>
	<head>
	<title>Почини меня - недоступные ребра</title>
	  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	   
	  <script type="text/javascript" src="http://tile.cloudmade.com/wml/latest/web-maps-lite.js"></script> 
		   
	  <script type="text/javascript" src="/2/fixme-map.js"> </script>'; 
	  	  
if (!$blnPermalink)	  	  
  echo(' <script type="text/javascript" src="http://openstreetmap.by/?request=osrm_error_to_josm&format=json&lat=55.77&lon=37.8&rnd='.mt_rand(0,100000).'"> </script> ');

echo 	  
	'</head>
	<body>
	  <h1>Так вот ты какое, недоступное ребро!</h1>
		<table>
			<tr>
			  <td><div id="cm-example" style="width: 400px; height: 400px"></div> </td> 
			  <td valign="top" width="550px" style="padding:5px" >
			     <p><b>Что это такое?</b><br /> 
			     На этой странице представлены  участки дорог, на которые по разным причинам невозможно заехать 
			     (если по-умному, "ребра дорожного графа, недоступные для рутинга"). В большинстве случаев они возникают из-за ошибок редакторов карты.</p>
			     <p>
			       <b>Откуда взялись эти данные?</b><br /> 
			       Анализ дорог в OSM проведен <a href="http://map.project-osrm.org/">проектом OsRM</a>. 
			     </p>
			     <p>
			       <b>Что мне делать?</b><br />
			       Нажмите F5. Страница обновится, и на карте покажется одно из проблемных ребр. Если Josm запущен, этот участок карты автоматически загрузится в Josm для редактирования. 
			       (Если Josm не загружает это место, можно попробовать <span id="josm_mlink"></span>)<br/><br/>
			       Если заметно что-то подозрительное (<a href="http://wiki.openstreetmap.org/wiki/User:Zkir/Так_вот_ты_какое,_недоступное_ребро!">оторванные от основного графа куски дорог, односторонние дороги, на которые есть только въезд или только выезд,
			       или просто несоединенные, где надо вершины</a>), <i>почините :) </i> <br/> <br/>
			       После этого нажмите F5 - отобразится следующий сегмент. 
			     </p>
			     
			  </td>
		    </tr>
		    <tr>
		      <td><span id="permalink"> </span> -- <a href="?next">Покажите следующее место!</></td>
		      <td>
		      	  <b>Статистика:</b><br /> 
		      	  Всего недоступных сегментов дорог: <b><span id="total_segs">xxx</span></b>,  показано сегодня: <b><span id="shown_segs">yyy</span></b>.
		      </td>
		    </tr>
		</table>
		<iframe id="ttt" src="" style="display:none;"></iframe>
		<hr/>
		<p><small>(c) Komzpa, Zkir, OsRM и другие участники проекта OSM, СС-BY-SA, и прочее, и прочее</small></p>
		<p><small><a href="http://openstreetmap.by/?request=osrm_error_to_josm">Оригинальная страница </a> </small></p>
		
		  <script type="text/javascript">
		    var lat1,lon1,lat2,lon2;';
if (!$blnPermalink){
  echo '		  
		    
		    lat1=EdgeData.coordinates[0][0][1];
		    lon1=EdgeData.coordinates[0][0][0];
		    lat2=EdgeData.coordinates[0][1][1];
		    lon2=EdgeData.coordinates[0][1][0];';
		  }
else{		    
  echo	   'lat1='.$coord[0].';
		    lon1='.$coord[1].';
		    lat2='.$coord[2].'; 
		    lon2='.$coord[3].';';
}
echo '
		    
	        ProcessMap(lat1,lon1,lat2,lon2);
            
            var Elem=document.getElementById(\'total_segs\');
            Elem.innerHTML=EdgeData.properties.count_all;
             
            Elem=document.getElementById(\'shown_segs\');
            Elem.innerHTML=EdgeData.properties.count_fixed;
             	        
	   </script> 
	</body>
	</html>'
?>