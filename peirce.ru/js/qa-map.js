//==================================================================================================
// Visualization for validator (various steps)
// (c) zkir 2012
//==================================================================================================
//
// По идее, есть два этапа
// 1. Получить массив ошибок, которые нужно показать на карте, их xml
//   Этот этап может зависить от xml/теста
// 2. Отобразить ошибки на карте. Этот этап должен быть одинаков для всех тестов
// Единственно что, ошибка может быть как точечной (разрыв береговой линии), так и с ббоксом (рутинговый подграф, просроченная дорога)   

// Глобальные переменные

var errorList = []; //Cписок ошибок
var currentMarker=-1; //Номер "текущей" ошибки
var map;

//==========================================================================================================================
//Main function
//==========================================================================================================================
function ProcessMap(TestName, XmlFileName, strLevel) {
	try {
		//0. Создаем карту
		initializeMap(XmlFileName.indexOf('EU-OVRV') >= 0 && (TestName == 'isltd-hw' || TestName == 'dnodes'));

		// 1. - получение списка ошибок.
		errorList = loadErrorList(TestName, XmlFileName, strLevel);

		var markers = [];
		var mapBBOX = new BBOX();

		// 2. - отображение их на карте
		for (var i = 0; i < errorList.length; i++) {

			// Точка - для нее просто маркер
			if(errorList[i].Kind == "POINT") {
				mapBBOX.extend(errorList[i]);
				
				markers.push(new L.Marker([errorList[i].lat, errorList[i].lon], {
					title : errorList[i].Descr
				}));
				
				markers[i].on('click',
						function(evnt) {
					doClickLatLon(evnt.latlng.lat, evnt.latlng.lng);
				});
			}
			else if(errorList[i].Kind == "BBOX") {
				
				mapBBOX.extend(errorList[i].bbox);				
				
				markers.push(new L.Marker(errorList[i].center, {
					title : errorList[i].Descr
				}));
				
				markers[i].errBBOX = errorList[i].bbox;
				markers[i].on('click',	function(evnt) {
					doClickBBOX(this.errBBOX);
				});
				
				var polygon = new L.Polygon([
                      [errorList[i].bbox.lat1, errorList[i].bbox.lon1],
                      [errorList[i].bbox.lat1, errorList[i].bbox.lon2],
                      [errorList[i].bbox.lat2, errorList[i].bbox.lon2],
                      [errorList[i].bbox.lat2, errorList[i].bbox.lon1] 
                ]);
				
				map.addLayer(polygon);
			}

		}
		
		window.setTimeout(function(){
			if(!(window.location.href.indexOf('#') >= 0 && window.location.href.indexOf('zoom=') > 0)) {
				map.fitBounds([
				               [mapBBOX.latMin, mapBBOX.lonMin],
				               [mapBBOX.latMax, mapBBOX.lonMax]
				               ]);
			}
			map.mClusters.addLayers(markers);
		}, 500);
	
		document.write('<p><a href="javascript:void(0)" onclick="showNextProblem(0)">Показать следущую ошибку</a> -- ');
		document.write('<a href="javascript:void(0)" onclick="showNextProblem(1)">Показать случайную ошибку</a> -- ');
		document.write('<a href="javascript:void(0)" onclick="LoadCurrentView()">Загрузить текущий вид</a> -- ');
		document.write('<a href="javascript:void(0)" onclick="ThisPlaceAtOsm()">Это место на OSM.org</a> </p>');
		document.write('<p> Всего ошибок: ' + errorList.length + '</p>');

	} catch (err) {
		document.write("<p>Ошибка выполнения: " + err + "</p>");
	}
}

//Тестовый список ошибок
function getTesterrorList(XmlFileName)
{	
  var EL = [];
  EL.push (new ErrorItem(10,10));
  EL.push (new ErrorItem(20,20));
  EL.push (new ErrorItem(30,30));
  EL.push (new ErrorItem(40,40));
  EL.push (new ErrorItem(50,50));
  EL.push (new ErrorItem(60,60));
  EL.push (new ErrorItem(70,70));
  EL.push (new ErrorItem(80,80));
  return EL;
}

//Дубликаты рутингового графа
function getRDerrorList(XmlFileName) {
	var result = [];
	loadAndParseXML(XmlFileName, function(xmlhttp) {
		var doc = xmlhttp.responseXML.documentElement;
		var items = doc.getElementsByTagName("DuplicatePoint");
   	   
		//Проходимся по всем элементам-записям и составляем их репрезентацию
		for (var i = 0; i < items.length; i++) {
			var r = parseItemNode(items[i], LatLonParser);
			if(r)
			result.push(new ErrorItem(r.lat, r.lon, 'Ошибка топологии: дубликат рутингового ребра'));
		}   
	});
	return result;
}

//Просроченные дороги
function getHWCerrorList(XmlFileName)
{
	var result = [];
	loadAndParseXML(XmlFileName, function(xmlhttp) {
		var doc = xmlhttp.responseXML.documentElement;
	    var items = doc.getElementsByTagName("error");
	    
	    //Проходимся по всем элементам-записям и составляем их репрезентацию
		for (var i = 0; i < items.length; i++)	{
			var r = parseItemNode(items[i], ConstructedHWErrorsParser);
			
			var strErrType=items[i].getAttribute('errorType');
			
		    var aDescr = strErrType + ': ' + r.checkDate + '/' + r.openingDate;
			switch (strErrType) {
				case "CHECK_DATE_TOO_OLD":
					aDescr='Дорога давно не проверялась: ' + r.checkDate;
					break;
				case "OPENING_DATE_PASSED":
					aDescr='Ожидаемая дата открытия уже наступила: ' + r.openingDate;
					break;
		        case "CHECK_DATE_FORMAT_ERROR":
		        	aDescr='Неверный формат даты: ' + r.checkDate;
		        	break;  
		        case "OPENING_DATE_FORMAT_ERROR":
		        	aDescr='Неверный формат даты: '+ r.openingDate;
		        	break;
			}
			
			result.push(new ErrorItemBBox(r.bbox, aDescr));
		}
	});
  
	return result;
}

//Тупики важных дорог.
function getDnodeserrorList(XmlFileName)
{
  var result = [];
   
  loadAndParseXML(XmlFileName, function(xmlhttp){
	  
	  var doc = xmlhttp.responseXML.documentElement;
	  var items = doc.getElementsByTagName("DeadEnd");
	  
	  //Проходимся по всем элементам-записям и составляем их репрезентацию
	  for (var i = 0; i < items.length; i++)  {
		  var r = parseItemNode(items[i], LatLonParser);
		  result.push(new ErrorItem(r.lat, r.lon, 'Ошибка присвоения статуса: тупик важной дороги'));
	  }
  });
    	
  return result;
}

//Улицы вне городов
function getAddrStreeterrorList(XmlFileName)
{
	var result = [];
	   
	loadAndParseXML(XmlFileName, function(xmlhttp) {
		var doc = xmlhttp.responseXML.documentElement;
   	   
		//Проходимся по всем элементам-записям и составляем их репрезентацию
		var items=doc.getElementsByTagName("StreetsOutsideCities")[1].childNodes;   	   

		for (var i = 0; i < items.length; i++)	{
			var r = parseItemNode(items[i], LatLonParser);
			if(r) {
				result.push(new ErrorItem(r.lat, r.lon, 'Улица за пределами НП: ' + (r.streetName | 'б/имени')));
			}
		}
	});
	
	return result;
}

//Изоляты дорожного графа
function getIsolatedHighwayserrorList(xmlFileName, strLevel) {
	
	var result = [];
	
	loadAndParseXML(xmlFileName, function(xmlhttp) {
		var doc = xmlhttp.responseXML.documentElement;
		var items = getRouteTestElement(doc, strLevel).getElementsByTagName("Subgraph");
		var mapBBOX = new BBOX();
		
		//Проходимся по всем элементам-записям и составляем их репрезентацию
		for (var i = 0; i < items.length; i++) {
			var pr = parseItemNode(items[i], IsolatedHWParser);
			
			if ( i >= 0 ) {
				var aBugDescr="Число ребер: " + pr.nRoads;
				result.push(new ErrorItemBBox(pr.bbox, aBugDescr));
			}
		}
		
	});
	
	return result;
}

function parseItemNode(item, parser) {

	var result = {};
	
	//Отсчитываем с первого дочернего узла
	var f_child = item.firstChild;
	
	if(f_child) {
		do	{
			//Выбираем имя узла и в соответствии с этим выполняем необходимое действие
			if(parser[f_child.nodeName] !== undefined && typeof parser[f_child.nodeName] == 'function') {
				parser[f_child.nodeName](f_child, result);
			}
			
			f_child = f_child.nextSibling;	
		} 
		while (f_child);
		
		return result;
	}
}

LatLonParser = {
	"Coord" : function (f_child, result) {
		try {
			result.lat = f_child.getElementsByTagName("Lat")[0].firstChild.nodeValue;
			result.lon = f_child.getElementsByTagName("Lon")[0].firstChild.nodeValue;
		}
		catch(err) {
			throw('Координаты точки не заданы');
		}
	}
}

HouseOutsideNPParser = {
	//парсер координат	
	"Coord" : LatLonParser["Coord"],
	
	"Street": function (f_child, result) {
		result.streetName=f_child.firstChild.nodeValue;
	}
}

IsolatedHWParser = {
	"NumberOfRoads" : function(f_child, result) {
		result.nRoads=f_child.firstChild.nodeValue;
	},
	
	"Bbox" : function(f_child, result) {
		result.bbox = {};
		try {
			result.bbox.lat1 = parseFloat(f_child.getElementsByTagName("Lat1")[0].firstChild.nodeValue);
			result.bbox.lon1 = parseFloat(f_child.getElementsByTagName("Lon1")[0].firstChild.nodeValue);
			result.bbox.lat2 = parseFloat(f_child.getElementsByTagName("Lat2")[0].firstChild.nodeValue);
			result.bbox.lon2 = parseFloat(f_child.getElementsByTagName("Lon2")[0].firstChild.nodeValue);
		}
		catch(err){
			throw('Координаты  не заданы');
		}
	}
}

ConstructedHWErrorsParser = {
	"opening_date" : function(f_child, result) {
		result.openingDate = f_child.firstChild.nodeValue;
	},
	  
	"check_date" : function(f_child, result) {
		result.checkDate = f_child.firstChild.nodeValue;
	},
	
	"bound" : function(f_child, result) {
		result.bbox = {};
	    try{
	    	result.bbox.lat1 = parseFloat(f_child.getAttribute("bottom"));
	    	result.bbox.lon1 = parseFloat(f_child.getAttribute("left"));
	    	result.bbox.lat2 = parseFloat(f_child.getAttribute("top"));
	    	result.bbox.lon2 = parseFloat(f_child.getAttribute("right"));
	    }
	    catch(err){
	      throw('Координаты  не заданы');
	    }
	}
}


function getRouteTestElement(doc, strLevel) {
	switch (strLevel) {
	case "0":
		return doc.getElementsByTagName("RoutingTestByLevel")[0].getElementsByTagName("Trunk")[0];
	case "1":
		return doc.getElementsByTagName("RoutingTestByLevel")[0].getElementsByTagName("Primary")[0];
	case "2":
		return doc.getElementsByTagName("RoutingTestByLevel")[0].getElementsByTagName("Secondary")[0];
	case "3":
		return doc.getElementsByTagName("RoutingTestByLevel")[0].getElementsByTagName("Tertiary")[0];
	case "4":
		return doc.getElementsByTagName("RoutingTest")[0];
	}
}

function loadErrorList(TestName, XmlFileName, strLevel) {
	
	switch (TestName) {
	case "test":
		return getTesterrorList(XmlFileName);
	case "rd":
		return getRDerrorList(XmlFileName);
	case "hwc":
		return getHWCerrorList(XmlFileName);
	case "dnodes":
		return getDnodeserrorList(XmlFileName);
	case "addr-street":
		return getAddrStreeterrorList(XmlFileName);
	case "isltd-hw":
		return getIsolatedHighwayserrorList(XmlFileName, strLevel);

	default:
		throw new Error("Unknown test: " + TestName);
	}
	
}

function initializeMap(showEURoutes) {
	map = new L.map('cm-example').setView([55.75,37.6], 8);

	var mapsurfer = L.tileLayer('http://129.206.74.245:8001/tms_r.ashx?x={x}&y={y}&z={z}', {
    		attribution: 'Данные карты &copy; <a href="http://osm.org">участники OpenStreetMap</a>, ' + 
			'Отрисовка карты <a href=\"http://giscience.uni-hd.de/\" target=\"_blank\">GIScience' +
			' Research Group @ University of Heidelberg</a>',
    		maxZoom: 18
	});

	var osmUrl='http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
	var osmAttrib='Данные карты - участники <a href=\"http://www.openstreetmap.org/copyright\" target=\"_blank\">© OpenStreetMap</a>';
	var osm = new L.TileLayer(osmUrl, {minZoom: 1, maxZoom: 18, attribution: osmAttrib});		

	osm.addTo(map);		

	if(showEURoutes) {
		var gen = L.tileLayer.wms("http://euroroutes.zkir.ru:8080/service", {
			layers: 'generalizedhw',
			format: 'image/png',
			transparent: true
		});

		var gen_nc = L.tileLayer.wms("http://81.176.229.99/cgi-bin/qgis_mapserv.fcgi", {
			layers: 'generalizedhw',
			'map': '/home/citygyde/euovrv.qgs',
			format: 'image/png',
			transparent: true
		});
		
		gen.addTo(map);
	}

	var baseMaps = {
	    "mapnik": osm,
	    "mapsurfer": mapsurfer
	};

	
	var overlayMaps = {};
	
	if(showEURoutes) {
		overlayMaps = {
				"E-Routes (generalized)": gen,
				"E-Routes (generalized, no caching)": gen_nc
		};
	}

	L.control.layers(baseMaps, overlayMaps).addTo(map);
	map.addControl(new L.Control.Permalink());
	
	//map.mClusters = new L.MarkerClusterGroup({ spiderfyOnMaxZoom: false, showCoverageOnHover: false, zoomToBoundsOnClick: true });
	map.mClusters = new L.MarkerClusterGroup({ spiderfyOnMaxZoom: false, showCoverageOnHover: false, zoomToBoundsOnClick: true, maxClusterRadius: 25 });
	map.mClusters.addTo(map);
}

//--------------- Конструкторы вспомогательных объектов ------------------------------------------------

function BBOX() {
	this.latMin=90;
	this.lonMin=180;
	this.latMax=0;
	this.lonMax=0;
}

BBOX.prototype.extend = function(b) {
	if(b.lat1 !== undefined && b.lon2 !== undefined) {
		if (b.lat1 < this.latMin) this.latMin=b.lat1;
		if (b.lat2 > this.latMax) this.latMax=b.lat2;
		if (b.lon1 < this.lonMin) this.lonMin=b.lon1;
		if (b.lon2 > this.lonMax) this.lonMax=b.lon2;
	}
	else if(b.lat !== undefined && b.lon != undefined) {
		if (b.lat < this.latMin) this.latMin = b.lat;
		if (b.lat > this.latMax) this.latMax = b.lat;
		if (b.lon < this.lonMin) this.lonMin = b.lon;
		if (b.lon > this.lonMax) this.lonMax = b.lon;
	}
}
 
//конструктор для "ошибки вообще"
function ErrorItem(Lat,Lon, Descr)
{
    this.Kind='POINT';
    this.Descr=Descr;
    this.lat = parseFloat(Lat);
    this.lon = parseFloat(Lon);
}

//Ошибка с bbox
function ErrorItemBBox(bbox, Descr)
{
    this.Kind='BBOX';
    this.Descr=Descr;
    
    this.bbox = bbox;
    this.center = [mean(bbox.lat1, bbox.lat2), mean(bbox.lon1, bbox.lon2)];    
}

function mean(a, b) {
	return Math.min(a, b) + Math.abs(a - b) / 2;
}

 // ---------------Обработка щелчка по маркеру------------------------------------------------
function doClickBBOX(b)
{
	var delta=0.0002;

	var hr = "http://localhost:8111/load_and_zoom?" + 
		"top=" + 	(b.lat2 + delta) + 
		"&bottom=" + 	(b.lat1 - delta) + 
		"&left=" +	(b.lon1 - delta) + 
		"&right=" + 	(b.lon2 + delta);
  
	document.getElementById('ttt').contentWindow.location.href=hr;
}

function doClickLatLon(lat, lon)
{
	var delta=0.0002;
	
	var hr = "http://localhost:8111/load_and_zoom?" + 
	"top=" + 	(lat + delta) + 
	"&bottom=" + 	(lat - delta) + 
	"&left=" +	(lon - delta) + 
	"&right=" + 	(lon + delta);
	
	document.getElementById('ttt').contentWindow.location.href=hr;
}

function ThisPlaceAtOsm() {
  var bounds = map.getBounds();      	 
  var strUrl= "http://www.openstreetmap.org/?bbox=" + 
  	(bounds.getSouthWest().lng) + "%2C" + 
  	(bounds.getSouthWest().lat) + "%2C" + 
  	(bounds.getNorthEast().lng) + "%2C" + 
  	(bounds.getNorthEast().lat);
  var newWin = window.open(strUrl, "_blank")
}
   
function LoadCurrentView()
{ 
  var bounds=map.getBounds();
  var strJosmLink = "http://localhost:8111/load_and_zoom" +
  		"?top=" + bounds.getNorth() + 
  		"&bottom=" + bounds.getSouth() + 
  		"&left=" + bounds.getWest() + 
  		"&right=" + bounds.getEast();
  document.getElementById('ttt').contentWindow.location.href=strJosmLink;
}	

//  Показать следующую проблему
function showNextProblem(blnShowRandom)
{
    
    if (blnShowRandom==0)  {	
      currentMarker=currentMarker + 1;
    } 
    else {	
      currentMarker=Math.floor(Math.random() * errorList.length); 
    }	  
    
    if (currentMarker >= errorList.length) { 
    	currentMarker = 0;
    }
    
    zoomToCurentMarker();
}

function zoomToCurentMarker() {
    var err = errorList[currentMarker];
    if(err.Kind == "POINT") {
    	map.fitBounds([
           [err.lat, err.lon],
           [err.lat, err.lon]
    	]);
    }
    else if (err.Kind == "BBOX") {
    	map.fitBounds([
           [err.bbox.lat1, err.bbox.lon1],
           [err.bbox.lat2, err.bbox.lon2]
       ]);
    }
}

function loadAndParseXML(xmlFileName, ready) {
	
	var xmlhttp = getXmlHttpObj();
	xmlhttp.open('GET', xmlFileName, false);
	xmlhttp.send(null);

	if(xmlhttp.status == 200) {
		ready(xmlhttp);
	}
	else {
		throw new Error("Unable to load xml file with error list: " + xmlFileName);
	}
	
}

function getXmlHttpObj() {
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
