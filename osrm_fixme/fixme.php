<?php
//==========================================================================================
//Почини меня - недоступные ребра. Пьявка к валидатору OSRM
// (c) Zkir, 2012. 
//==========================================================================================
session_start();

//Получим параметры
if ($_GET['permalink'] != "")
{
	$blnPermalink = True;
	$coord = explode(",", $_GET['permalink']);
}
else
{
	$blnPermalink = False;
}

if (trim($_GET['setlang']) != "")
{
	$_SESSION['lan_code'] = trim($_GET['setlang']);
}

$lan_code = $_SESSION['lan_code'];
if ($lan_code == "")
{
	$lan_code = 'en';	
}
	
// Установка языка
$locales = array(
	'ru' => 'ru_RU.utf8',
	'en' => 'en_US'
);
$lan_locale = 'en_US';
if (isset($locales[$lan_code]))
{
	$lan_locale = $locales[$lan_code];
}
putenv('LC_ALL='.$lan_locale);
setlocale(LC_ALL, $lan_locale);
bindtextdomain('fixme', './locale');
textdomain('fixme');

//Вывод страницы
echo '
	<html>
	<head>
		<title>'._('Fixme - inaccessible road').'</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<script type="text/javascript" src="http://tile.cloudmade.com/wml/latest/web-maps-lite.js"></script> 
		<script type="text/javascript" src="/2/fixme-map.js"></script>
	'.(!$blnPermalink ? 
		//'<script type="text/javascript" src="http://openstreetmap.by/?request=osrm_error_to_josm&format=json&lat=55.77&lon=37.8&rnd='.time().'"></script>'
		'<script type="text/javascript" src="http://openstreetmap.by/?request=osrm_error_to_josm&format=json&campaign=001&rnd='.time().'"></script>'
	: '' ).'
	</head>
	<body>
		<div style="float:right">
			<a href="?setlang=ru">RU</a> | <a href="?setlang=en">EN</a> 
		</div>
		<h1>'._('So that is what inaccessible road is!').'</h1>
		<table>
			<tr>
				<td><div id="cm-example" style="width: 450px; height: 400px"></div> </td> 
				<td valign="top" width="550px" style="padding-left:15px" >
			    	<p>
						<b>'._('Where am I?').'</b><br />
						'._('This page displays the road segments, which for various reasons are <i>inaccessible</i> (they are also called "not route-able parts of the road network").').'
						'._('In most cases these are errors in the OSM data which can and should be corrected.').' 
					</p>
					<p>
						<b>'._('Where this data is taken from?').'</b><br />
						'.sprintf(_('Data analysis is performed by the %s project.'), '<a href="http://map.project-osrm.org/">OsRM</a>').' 
					</p>
					<p>
						<b>'._('What should I do?').'</b><br />
						'._('Please press F5. This page will refresh, and one of the problematic routing edges will be displayed.').'
						'._('If Josm is running, this place will be automaticaly loaded into Josm for editing.').'
						('._('Make sure that Josm Remote Control is activated. You can also try \'manual\' link below the map.').')  
						<br/><br/>
						'.sprintf(_('If you see something suspicious, like %sisolated roads, or oneway roads where it is possible to drive out but not drive in and vice-versa, or just roads that are not properly connected by common nodes%s, <i>please fix</i> :)'), '<a href="http://wiki.openstreetmap.org/wiki/User:Zkir/Так_вот_ты_какое,_недоступное_ребро!">', '</a>').'
						<br/> <br/>
						'._('After that, please press F5 again - the next road segment will be displayed.').' 
					</p>
				</td>
			</tr>
			<tr>
				<td>
					<p align="center"><span id="josm_mlink"></span>  -- <span id="permalink"> </span> --  <span id="glagne_permalink"> </span> </p>
					<p align="center"><a href="?next">'._('Please show me the next problem!').'</a></p> 
				</td>
				<td style="padding-left:15px">
				'.(!$blnPermalink ? '
		      	  <b>'._('Statistics').':</b><br /> 
		          '.sprintf(_('World-wide, inaccessible road segments : %s, touched today: %s.'), '<b><span id="total_segs">xxx</span></b>', '<b><span id="shown_segs">yyy</span></b>').' </br>
		          '.sprintf(_('In the current campaign, inaccessible road segments: %s, touched today: %s.'), '<b><span id="total_segs1">xxx</span></b>', '<b><span id="shown_segs1">yyy</span></b>').' </br>
				' : '').'
				</td>
			</tr>
			<tr>
				<td></td>
				<td></td>
			</tr>
		</table>
		<iframe id="ttt" src="" style="display:none;"></iframe>
		<hr/>
		<p>
			<small>
				'._('This page is dedicated to the benefit of all living creatures.').'
				'.sprintf(_('Komzpa, Zkir, OsRM and other contributors of the %s project, СС-BY-SA, ODBL and all other magical incantations applicable in this case.'), '<a href="http://openstreetmap.org">Openstreetmap</a>').'
			</small>
	    </p>
		<p>
			<small>
				<a href="http://openstreetmap.by/?request=osrm_error_to_josm">'._('Original page').' </a>  --
				<a href="http://wiki.openstreetmap.org/wiki/User:Zkir/Так_вот_ты_какое,_недоступное_ребро!">'._('How to fix instructions').' </a> --
				<a href="http://forum.openstreetmap.org/viewtopic.php?id=18745">'._('Forum thread (ru)').' </a> 
			</small>
		</p>
		<script type="text/javascript">
			var lat1,lon1,lat2,lon2;
		'.(!$blnPermalink ? '
			lat1=EdgeData.coordinates[0][0][1];
			lon1=EdgeData.coordinates[0][0][0];
			lat2=EdgeData.coordinates[0][1][1];
			lon2=EdgeData.coordinates[0][1][0];
		' : '
			lat1='.$coord[0].';
			lon1='.$coord[1].';
			lat2='.$coord[2].'; 
			lon2='.$coord[3].';
		').'

			ProcessMap(lat1,lon1,lat2,lon2);

			Elem=document.getElementById(\'glagne_permalink\');
			Elem.innerHTML=\' <a href="http://www.openstreetmap.org/?lat=\'+(lat1+lat2)/2+\'&lon=\'+(lon1+lon2)/2+\'&zoom=18" target="_blank">'._('This place at osm.org').'</a>\';

			strPermalink="?permalink="+lat1+","+lon1+","+lat2+","+lon2;
			Elem=document.getElementById(\'permalink\');
			Elem.innerHTML=\'<a href="\'+strPermalink+\'">'._('Permalink').'</a>\'; 	  


			var Elem=document.getElementById(\'total_segs\');
			Elem.innerHTML=EdgeData.properties.count_all;
			 
			Elem=document.getElementById(\'shown_segs\');
			Elem.innerHTML=EdgeData.properties.count_fixed;

			var Elem=document.getElementById(\'total_segs1\');
			Elem.innerHTML=EdgeData.properties.count_campaign;
			 
			Elem=document.getElementById(\'shown_segs1\');
			Elem.innerHTML=EdgeData.properties.fixed_campaign;
		</script> 

		<!-- Yandex.Metrika -->
		<script src="//mc.yandex.ru/metrika/watch.js" type="text/javascript"></script>
		<div style="display:none;"><script type="text/javascript">
		try { var yaCounter1224821 = new Ya.Metrika(1224821); } catch(e){}
		</script></div>
		<noscript><div style="position:absolute"><img src="//mc.yandex.ru/watch/1224821" alt="" /></div></noscript>
		<!-- /Yandex.Metrika -->
	</body>
	</html>';
