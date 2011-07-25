//==================================================================================================
// Visualization for routing test
// (c) zkir 2011
//==================================================================================================

//costructor for subrgraph marker
function SubGraphInfo(NRoads,Lat1,Lon1,Lat2,Lon2)
{
    this.NRoads=NRoads;
    this.Lat1=Lat1;
    this.Lon1=Lon1;
    this.Lat2=Lat2;
    this.Lon2=Lon2;
}
	
//Main function
function ProcessMap(XmlFileName, ReportErrType1)
{	
 try{	
  var cloudmade = new CM.Tiles.OpenStreetMap.Mapnik();
  var map = new CM.Map('cm-example', cloudmade);
  var topRight = new CM.ControlPosition(CM.TOP_RIGHT, new CM.Size(50, 20));
  map.addControl(new CM.LargeMapControl());
  map.addControl(new CM.ScaleControl());
 
  map.setCenter(new CM.LatLng(55.75,37.6), 8);
  
  

	  	  
  var xmlhttp = getXmlHttp1();
  xmlhttp.open('GET', XmlFileName, false);
  xmlhttp.send(null);
  if(xmlhttp.status == 200) 
  {
    var doc = xmlhttp.responseXML.documentElement;
    // document.write(doc.getElementsByTagName("TotalHouses").item(0).firstChild.nodeValue);//item(0).firstChild
   	   
   	//Проходимся по всем элементам-записям и  составляем их репрезентацию
   	   	   
    var items = doc.getElementsByTagName("Subgraph");
    var NRoads=0;
    
    var Lat1=0;
    var Lon1=0;
    var Lat2=0;
    var Lon2=0;
    
    
    
    
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
	
      NRoads=0;
     	  	  
	  do
  	  {
  	  	  
    	//Выбираем имя узла и в соответствии с этим выполняем необходимое действие
		switch (f_child.nodeName)
		{
			//Если это описание, оформляем как описание
			case "NumberOfRoads":
			  NRoads=f_child.firstChild.nodeValue;
			  break;
			  
  
			case "ErrType":
			  MyErrType=f_child.firstChild.nodeValue;
			  break;  
			  
			case "Bbox":
              try{
               	Lat1= parseFloat(f_child.getElementsByTagName("Lat1")[0].firstChild.nodeValue);
               	Lon1= parseFloat(f_child.getElementsByTagName("Lon1")[0].firstChild.nodeValue);
               	Lat2= parseFloat(f_child.getElementsByTagName("Lat2")[0].firstChild.nodeValue);
               	Lon2= parseFloat(f_child.getElementsByTagName("Lon2")[0].firstChild.nodeValue);
              }
              catch(err){
                throw('Координаты  не заданы');
              }
        	  break;
		}
  	  	  //Устанавливаем следующий узел
		f_child = f_child.nextSibling;	
		
      } while (f_child) ;
      
       //parseInt(MyErrType)==3
      if(ReportErrType1==""||ReportErrType1==MyErrType) 
      { 
        if (Lat1<LatMin) LatMin=Lat1;
        if (Lat2>LatMax) LatMax=Lat2;	   
        if (Lon1<LonMin) LonMin=Lon1;
        if (Lon2>LonMax) LonMax=Lon2;
      
        var aBugDescr="Число ребер: "+NRoads;
        //document.write( "<p>" + aBugDescr  + "</p>");
      	markers.push(new CM.Marker(new CM.LatLng((Lat1+Lat2)/2, (Lon1+Lon2)/2),{title: aBugDescr}));
      	var aSubGraph=new SubGraphInfo(NRoads,Lat1,Lon1,Lat2,Lon2);
    	CM.Event.addListener(markers[i], 'click', function(latlng) {
    		var delta=0.001;
    	   	//doClick(latlng.lat()-delta,latlng.lng()-delta,latlng.lat()+delta,latlng.lng()+delta);
    	   	doClick(this.Lat1,this.Lon1,this.Lat2,this.Lon2);
   		     	},aSubGraph);
      		
        intMarkerCount=intMarkerCount+1;
        
        var polygon = new CM.Polyline([
	        new CM.LatLng(Lat1, Lon1),
	        new CM.LatLng(Lat1, Lon2),
	        new CM.LatLng(Lat2, Lon2),
	        new CM.LatLng(Lat2, Lon1),
	        new CM.LatLng(Lat1, Lon1),
           ]);
        map.addOverlay(polygon);
      }
     
    }//кц по домам
    
   
    //map.setCenter(new CM.LatLng(Lat0,Lon0), 8);
    var bounds = new CM.LatLngBounds(
                      new CM.LatLng(LatMin, LonMin), 
                      new CM.LatLng(LatMax, LonMax));
    map.zoomToBounds(bounds );	
    var clusterer = new CM.MarkerClusterer(map, {clusterRadius: 30});
    clusterer.addMarkers(markers);
    
    document.write('<p> всего отдельных подграфов: '+intMarkerCount+'</p>');
    	
  }	//условия удачной загрузки xml
} //блока try
  catch(err){
  document.write("<p>Ошибка выполнения: "+err+"</p>"); 
}  
}//функции

 //---------------Обработка щелчка по маркеру------------------------------------------------
    function doClick(lat1,lon1,lat2,lon2)
    {       	 
      var el=document.getElementById('ttt');
      
      //pstr = "http://localhost:8111/import?url=http://openstreetmap.org/api/0.6/way/XXXX/full";
      pstr ="http://localhost:8111/load_and_zoom?top="+(lat2)+"&bottom="+(lat1)+"&left="+(lon1)+"&right="+(lon2)+"";
      //document.write(pstr);
      el.src = pstr;
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