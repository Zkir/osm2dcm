//Main fuction
function ProcessMap(lat1,lon1,lat2,lon2)
{	
 try{	
  var cloudmade = new CM.Tiles.OpenStreetMap.Mapnik();
  var map = new CM.Map('cm-example', cloudmade);
  var topRight = new CM.ControlPosition(CM.TOP_RIGHT, new CM.Size(50, 20));
  map.addControl(new CM.LargeMapControl());
  map.addControl(new CM.ScaleControl());
  var MinLat;
  var MinLon;
  var MaxLat;
  var MaxLon;
  if (lat1>lat2) {MaxLat=lat1;MinLat=lat2} else {MaxLat=lat2;MinLat=lat1};
if (lon1>lon2) {MaxLon=lon1;MinLon=lon2} else {MaxLon=lon2;MinLon=lon1};
  
  var bounds = new CM.LatLngBounds(
                      [new CM.LatLng(MinLat, MinLon), 
                      new CM.LatLng(MaxLat, MaxLon)],
                      "#ff0000");
  
  map.zoomToBounds(bounds );
  
  var polyline = new CM.Polyline([
	new CM.LatLng(lat1, lon1),
	new CM.LatLng(lat2, lon2)
  ]);
  
  map.addOverlay(polyline);
  
  var delta=0.0002;
  var strJosmLink;
  strJosmLink="http://localhost:8111/load_and_zoom?top="+(MaxLat+delta)+"&bottom="+(MinLat-delta)+"&left="+(MinLon-delta)+"&right="+(MaxLon+delta);
  strPermalink="?permalink="+lat1+","+lon1+","+lat2+","+lon2;
  	  	  
  document.getElementById('ttt').contentWindow.location.href=strJosmLink;
  
  var Elem=document.getElementById('josm_mlink');
  Elem.innerHTML='<a href="'+strJosmLink+'" target="_new">ручную ссылку</a>';
	
  var Elem=document.getElementById('permalink');
  Elem.innerHTML='<a href="'+strPermalink+'">Постоянная ссылка</a>'; 	  
  //document.write('<p><a href="'+strJosmLink+'" target="_new">ссылка для JOSM</a></p>'); 
  	  	  
 } //блока try
  catch(err){
  document.write("<p>Ошибка выполнения: "+err+"</p>"); 
}  
}//функции

