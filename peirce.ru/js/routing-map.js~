//==================================================================================================
// Visualization for routing test
// (c) zkir 2011
//==================================================================================================

var markers = [];
var subGraphs = [];
var currentMarker=-1;
var map;

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
function ProcessMap(XmlFileName, strLevel)
{
	try{
		map = new L.map('cm-example').setView([55.75,37.6], 8);
 
		var mapsurfer = L.tileLayer('http://129.206.74.245:8001/tms_r.ashx?x={x}&y={y}&z={z}', {
	    		attribution: 'Данные карты &copy; <a href="http://osm.org">участники OpenStreetMap</a>, ' + 
				'Отрисовка карты <a href=\"http://giscience.uni-hd.de/\" target=\"_blank\">GIScience' +
				' Research Group @ University of Heidelberg</a>',
	    		maxZoom: 18
		});

		var osmUrl='http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
		var osmAttrib='Map data В© OpenStreetMap contributors';
		var osm = new L.TileLayer(osmUrl, {minZoom: 1, maxZoom: 18, attribution: osmAttrib});		

		map.addLayer(mapsurfer);

		var ovrv = L.tileLayer.wms("http://81.176.229.99/cgi-bin/qgis_mapserv.fcgi", {
		    layers: 'highways',
		    'map': '/home/citygyde/euovrv.qgs',
		    format: 'image/png',
		    transparent: true
		});

		var gen = L.tileLayer.wms("http://81.176.229.99/cgi-bin/qgis_mapserv.fcgi", {
		    layers: 'generalizedhw',
		    'map': '/home/citygyde/euovrv.qgs',
		    format: 'image/png',
		    transparent: true
		});

		var baseMaps = {
		    "mapsurfer": mapsurfer,
		    "mapnik": osm
		};

		var overlayMaps = {
		    "e-routes": ovrv,
		    "generalized": gen
		};

		L.control.layers(baseMaps, overlayMaps).addTo(map);

		map.mClusters = new L.MarkerClusterGroup({ spiderfyOnMaxZoom: false, showCoverageOnHover: false, zoomToBoundsOnClick: true });

		var xmlhttp = getXmlHttp1();
		xmlhttp.open('GET', XmlFileName, false);
		xmlhttp.send(null);

		if(xmlhttp.status == 200) {
			var doc = xmlhttp.responseXML.documentElement;
			// document.write(doc.getElementsByTagName("TotalHouses").item(0).firstChild.nodeValue);//item(0).firstChild

			//Проходимся по всем элементам-записям и составляем их репрезентацию
			var RouteTestElement;

			switch (strLevel) {
			case "0":
				RouteTestElement = doc.getElementsByTagName("RoutingTestByLevel")[0].getElementsByTagName("Trunk")[0];
				break;
			case "1":
				RouteTestElement = doc.getElementsByTagName("RoutingTestByLevel")[0].getElementsByTagName("Primary")[0];
				break;
 			case "2":
				RouteTestElement = doc.getElementsByTagName("RoutingTestByLevel")[0].getElementsByTagName("Secondary")[0];
				break;
 			case "3":
				RouteTestElement = doc.getElementsByTagName("RoutingTestByLevel")[0].getElementsByTagName("Tertiary")[0];
				break;
			case "4":
				RouteTestElement = doc.getElementsByTagName("RoutingTest")[0];
				break;
    			}

			var items = RouteTestElement.getElementsByTagName("Subgraph");
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

			var intLen=0;
			intLen = items.length;
			var intMarkerCount=0;

			for (var i = 0; i < intLen; i++) {
				//Отсчитываем с первого дочернего узла
				var f_child = items[i].firstChild;

				NRoads=0;
				do {

					//Выбираем имя узла и в соответствии с этим выполняем необходимое действие
					switch (f_child.nodeName) {
					//Если это описание, оформляем как описание
					case "NumberOfRoads":
						NRoads=f_child.firstChild.nodeValue;
						break;

					case "Bbox":
						try {
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


				if (Lat1<LatMin) LatMin=Lat1;
				if (Lat2>LatMax) LatMax=Lat2;
				if (Lon1<LonMin) LonMin=Lon1;
				if (Lon2>LonMax) LonMax=Lon2;

				if ( i >= 0 ) {
					var aBugDescr="Число ребер: "+NRoads;

					markers.push(new L.Marker([(Lat1+Lat2)/2, (Lon1+Lon2)/2], {title: aBugDescr}));
					var aSubGraph=new SubGraphInfo(NRoads,Lat1,Lon1,Lat2,Lon2);
					subGraphs.push(aSubGraph);

					markers[intMarkerCount].on(
						'click', 
						function(evnt) {
							doClick(this.Lat1,this.Lon1,this.Lat2,this.Lon2);
						},
						aSubGraph
					);

					map.mClusters.addLayer(markers[intMarkerCount]);
					
					L.polygon([
						[Lat1, Lon1],
						[Lat1, Lon2],
						[Lat2, Lon2],
						[Lat2, Lon1]
					]).addTo(map);

					intMarkerCount=intMarkerCount+1;
				}


			}//кц по домам

	
			map.fitBounds([
			    [LatMin, LonMin],
			    [LatMax, LonMax]
			]);

			map.addLayer(map.mClusters);
	
			document.write('<p><a href="javascript:void(0)" onclick="ShowNextProblem(0)">Показать следущий изолят</a> -- ');
			document.write('<a href="javascript:void(0)" onclick="ShowNextProblem(1)">Показать случайный изолят</a> -- ');
			document.write('<a href="javascript:void(0)" onclick="LoadCurrentView()">Загрузить текущий вид</a> -- ');
			document.write('<a href="javascript:void(0)" onclick="ThisPlaceAtOsm()">Это место на OSM.org</a> </p>');

			document.write('<p> всего отдельных подграфов: '+intMarkerCount+'</p>');

		}//условия удачной загрузки xml

	} //блока try
  	catch(err){
		document.write("<p>Ошибка выполнения: "+err+"</p>");
	}

}//функции

//---------------Обработка щелчка по маркеру------------------------------------------------
function doClick(lat1,lon1,lat2,lon2)
{
  var delta=0.0002;
  document.getElementById('ttt').contentWindow.location.href="http://localhost:8111/load_and_zoom?top="+(lat2+delta)+"&bottom="+(lat1-delta)+"&left="+(lon1-delta)+"&right="+(lon2+delta);
}


function ThisPlaceAtOsm()
{
  var bounds;      	 
  bounds=map.getBounds();
  var strLink;
  strUrl="http://www.openstreetmap.org/?bbox="+ (bounds._sw._lng)+"%2C"+ (bounds._sw._lat)+"%2C"+(bounds._ne._lng)+"%2C"+(bounds._ne._lat);
  //document.location.href=strUrl;
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

function ShowNextProblem(blnShowRandom)
{
	var LatMin=0;
    var LonMin=0;
	var LatMax=0;
    var LonMax=0;
    
    if (blnShowRandom==0)
    {	
      currentMarker = currentMarker + 1;
    }
    else
    {	
      currentMarker = Math.floor(Math.random() * subGraphs.length); 
    }	  
    
    if (currentMarker>=subGraphs.length){
	currentMarker=0;
    }
    
    LatMin=subGraphs[currentMarker].Lat1;
    LonMin=subGraphs[currentMarker].Lon1;
    LatMax=subGraphs[currentMarker].Lat2;
    LonMax=subGraphs[currentMarker].Lon2;
	
	
	var bounds = new CM.LatLngBounds(
                      new CM.LatLng(LatMin, LonMin), 
                      new CM.LatLng(LatMax, LonMax));
    map.zoomToBounds(bounds );
}	
	    
 //---------------Вспомогательная фукция получения XMfLHTTP----------------------------------

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
