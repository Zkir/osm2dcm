var map;
var markers = [];
var CurrentMarker=-1;
//Main fuction
function ProcessMap(XmlFileName, ReportErrType1)
{	
 try{	
  var cloudmade = new CM.Tiles.OpenStreetMap.Mapnik();
  map = new CM.Map('cm-example', cloudmade);
  var topRight = new CM.ControlPosition(CM.TOP_RIGHT, new CM.Size(50, 20));
  map.addControl(new CM.LargeMapControl());
  map.addControl(new CM.ScaleControl());
 
  map.setCenter(new CM.LatLng(55.75,37.6), 5);
  	  	  
  var xmlhttp = getXmlHttp1();
  xmlhttp.open('GET', XmlFileName, false);
  xmlhttp.send(null);
  if(xmlhttp.status == 200) 
  {
    var doc = xmlhttp.responseXML.documentElement;
    // document.write(doc.getElementsByTagName("TotalHouses").item(0).firstChild.nodeValue);//item(0).firstChild
   	   
   	//Проходимся по всем элементам-записям и  составляем их репрезентацию
   	   	   
    var items = doc.getElementsByTagName("House");
    var HouseName="";
    var StreetName="";
    var HouseLat=0;
    var HouseLon=0;
    var MyErrType="0";
    
    var Lat0=0.0;
    var Lon0=0.0;
    var LatZ=0.0;
    var LonZ=0.0;
  
   
    var intLen=0;
    intLen = items.length;
    var intMarkerCount=0;
    //intLen = 1797;//1795-1800
    	
            
    
    for (var i = 0; i < intLen; i++)
    {
	  	  
	  //Отсчитываем с первого дочернего узла
	  var f_child = items[i].firstChild;
	
      HouseName="";
      StreetName="";
	  	  
	  do
  	  {
  	  	  
    	//Выбираем имя узла и в соответствии с этим выполняем необходимое действие
		switch (f_child.nodeName)
		{
			//Если это заголовок — оформляем как заголовок
			case "City":
				//document.write("<p>city</p>");
			 // document.write( "<h3>" + f_child.firstChild.nodeValue + "</h3>");
			break;
			//Если это описание, оформляем как описание
			case "HouseNumber":
			  HouseName=f_child.firstChild.nodeValue;
			  break;
			case "Street":
              try{
			    StreetName=f_child.firstChild.nodeValue;
			  }  
			   catch(err){
                StreetName="<улица не задана>";
              }
			  break;  
  
			case "ErrType":
			  MyErrType=f_child.firstChild.nodeValue;
			  break;  
			  
			case "Coord":
              try{
               	HouseLat= f_child.getElementsByTagName("lat")[0].firstChild.nodeValue;
               	HouseLon= f_child.getElementsByTagName("lon")[0].firstChild.nodeValue;
              }
              catch(err){
                throw('Координаты дома '+HouseName+ ' не заданы');
              }
        	  break;
		}
  	  	  //Устанавливаем следующий узел
		f_child = f_child.nextSibling;	
		
      } while (f_child) ;
      
      LatZ=LatZ+parseFloat(HouseLat);
      LonZ=LonZ+parseFloat(HouseLon);
       //parseInt(MyErrType)==3
      if(ReportErrType1==""||ReportErrType1==MyErrType) 
      { 	   
        Lat0=Lat0+parseFloat(HouseLat);
        Lon0=Lon0+parseFloat(HouseLon);
      
        var aBugDescr=StreetName+", "+HouseName+". Тип ошибки: "+MyErrType;
        //document.write( "<p>" + aBugDescr  + "</p>");
      	markers.push(new CM.Marker(new CM.LatLng(HouseLat, HouseLon),{title: aBugDescr}));
    	CM.Event.addListener(markers[intMarkerCount], 'click', function(latlng) {
    		doClick(latlng.lat(),latlng.lng());
   		     	});
      		
        intMarkerCount=intMarkerCount+1;
      }
     
    }//кц по домам
    if ( intMarkerCount > 0 )
    {
      Lat0=Lat0/intMarkerCount;
      Lon0=Lon0/intMarkerCount;
    }
    else
    {
      Lat0=LatZ/intLen;
      Lon0=LonZ/intLen;
    }
    if ( intLen > 0 )
      map.setCenter(new CM.LatLng(Lat0,Lon0), 8);
    else
      typeErr.innerHTML = "<font color='#aa4411' size=+1>Ошибок по этому региону нет</font>"
    	
    var clusterer = new CM.MarkerClusterer(map, {clusterRadius: 60});
    clusterer.addMarkers(markers);
    
    document.write('<p><a href="javascript:void(0)" onclick="ShowNextProblem()">Показать следущую ошибку</a> -- ');  
    document.write('<a href="javascript:void(0)" onclick="LoadCurrentView()">Загрузить текущий вид</a> -- ');
    document.write('<a href="javascript:void(0)" onclick="ThisPlaceAtOsm()">Это место на OSM.org</a> </p>');  
    document.write('<p> всего ошибок:'+intMarkerCount+'</p>');
    	
  }	//условия удачной загрузки xml
} //блока try
  catch(err){
  document.write("<p>Ошибка выполнения: "+err+"</p>"); 
}  
}//функции

 //---------------Обработка щелчка по маркеру------------------------------------------------
    function doClick(lat,lon)
    {       	 
      var delta=0.0002;
      document.getElementById('ttt').contentWindow.location.href="http://localhost:8111/load_and_zoom?top="+(lat+delta)+"&bottom="+(lat-delta)+"&left="+(lon-delta)+"&right="+(lon+delta);
    }
    
function ThisPlaceAtOsm()
{
  var bounds;      	 
  bounds=map.getBounds();
  var strUrl;
  strUrl="http://www.openstreetmap.org/?bbox="+ (bounds._sw._lng)+"%2C"+ (bounds._sw._lat)+"%2C"+(bounds._ne._lng)+"%2C"+(bounds._ne._lat);
 // document.location.href=strUrl;
  var newWin = window.open(strUrl, "_blank")


}
   
function LoadCurrentView()
{ 
  var bounds;      	 
  bounds=map.getBounds();
  var strJosmLink;
  strJosmLink="http://localhost:8111/load_and_zoom?top="+  (bounds._ne._lat)+"&bottom="+(bounds._sw._lat)+"&left="+(bounds._sw._lng)+"&right="+(bounds._ne._lng);
  document.getElementById('ttt').contentWindow.location.href=strJosmLink;
}	    
    
function ShowNextProblem()
{
	var LatMin=0;
    var LonMin=0;
	var LatMax=0;
    var LonMax=0;
    
    CurrentMarker=CurrentMarker+1;
    
    if (CurrentMarker>=markers.length)
    {CurrentMarker=0;}
    
    LatMin=markers[CurrentMarker]._position._lat;
    LonMin=markers[CurrentMarker]._position._lng;
	LatMax=markers[CurrentMarker]._position._lat;
    LonMax=markers[CurrentMarker]._position._lng;
	
	
	var bounds = new CM.LatLngBounds(
                      new CM.LatLng(LatMin, LonMin), 
                      new CM.LatLng(LatMax, LonMax));
    map.zoomToBounds(bounds );
}
 
 //---------------Вспомогательная фукция получения XMLHTTP----------------------------------    
    function getXmlHttp(){
    var xmlhttp;
    try {
      xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e) {
      try {
        xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
      } catch (E) {
        xmlhttp = false;
      }
    }
    if (!xmlhttp && typeof XMLHttpRequest!='undefined') {
      xmlhttp = new XMLHttpRequest();
    }
      return xmlhttp;
    }
    
  function getXmlHttp1() {
  if (typeof XMLHttpRequest == 'undefined') {
    XMLHttpRequest = function() {
      try { return new ActiveXObject("Msxml2.XMLHTTP.6.0"); }
        catch(e) {}
      try { return new ActiveXObject("Msxml2.XMLHTTP.3.0"); }
        catch(e) {}
      try { return new ActiveXObject("Msxml2.XMLHTTP"); }
        catch(e) {}
      try { return new ActiveXObject("Microsoft.XMLHTTP"); }
        catch(e) {}
      document.write("This browser does not support XMLHttpRequest");  
      throw new Error("This browser does not support XMLHttpRequest.");
    };
  }
  return new XMLHttpRequest();
}