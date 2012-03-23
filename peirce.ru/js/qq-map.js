//Main fuction
function ProcessMap(XmlFileName, ReportErrType1)
{	
 try{	
  var cloudmade = new CM.Tiles.OpenStreetMap.Mapnik();
  var map = new CM.Map('cm-example', cloudmade);
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
   	   	   
    var items = doc.getElementsByTagName("DuplicatePoint");
    var HouseName="";
    var StreetName="";
    var HouseLat=0;
    var HouseLon=0;
    var MyErrType="0";
    
    var LatMin=90;
    var LonMin=180;
    var LatMax=0;
    var LonMax=0;
  
    var markers = [];
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
               	HouseLat= f_child.getElementsByTagName("Lat")[0].firstChild.nodeValue;
               	HouseLon= f_child.getElementsByTagName("Lon")[0].firstChild.nodeValue;
              }
              catch(err){
                throw('Координаты дома '+HouseName+ ' не заданы');
              }
        	  break;
		}
  	  	  //Устанавливаем следующий узел
		f_child = f_child.nextSibling;	
		
      } while (f_child) ;
      
      if (parseFloat(HouseLat)<LatMin) LatMin=parseFloat(HouseLat);
      if (parseFloat(HouseLat)>LatMax) LatMax=parseFloat(HouseLat);
      if (parseFloat(HouseLon)<LonMin) LonMin=parseFloat(HouseLon);
      if (parseFloat(HouseLon)>LonMax) LonMax=parseFloat(HouseLon);
      
      
      if(ReportErrType1==""||ReportErrType1==MyErrType) 
      { 	   
      
        var aBugDescr="Ошибка топологии: дубликат рутингового ребра";
        //document.write( "<p>" + aBugDescr  + "</p>");
      	markers.push(new CM.Marker(new CM.LatLng(HouseLat, HouseLon),{title: aBugDescr}));
    	CM.Event.addListener(markers[intMarkerCount], 'click', function(latlng) {
    		doClick(latlng.lat(),latlng.lng());
   		     	});
      		
        intMarkerCount=intMarkerCount+1;
      }
     
    }//кц по домам
    var bounds = new CM.LatLngBounds(
                      new CM.LatLng(LatMin, LonMin), 
                      new CM.LatLng(LatMax, LonMax));
    map.zoomToBounds(bounds );	
    if ( intLen = 0 )
    {
      typeErr.innerHTML = "<font color='#aa4411' size=+1>Ошибок по этому региону нет</font>";
    }
    	
    var clusterer = new CM.MarkerClusterer(map, {clusterRadius: 60});
    clusterer.addMarkers(markers);
    
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
      var delta=0.0006;
      document.getElementById('ttt').contentWindow.location.href="http://localhost:8111/load_and_zoom?top="+(lat+delta)+"&bottom="+(lat-delta)+"&left="+(lon-delta)+"&right="+(lon+delta);
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