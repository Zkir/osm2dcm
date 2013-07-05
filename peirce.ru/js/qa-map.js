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
 var map; //Объект карта
 var ErrorList = []; //Cписок ошибок
 var CurrentMarker=-1; //Номер "текущей" ошибки
 

//конструктор для "ошибки вообще"
function ErrorItem(Lat,Lon, Descr)
{
    this.Kind='POINT';
    this.Descr=Descr;
    this.Lat=parseFloat(Lat);
    this.Lon=parseFloat(Lon);
}

//Ошибка с bbox
function ErrorItemBBox(Lat1,Lon1,Lat2,Lon2, Descr)
{
    this.Kind='BBOX';
    this.Descr=Descr;
    this.Lat1=parseFloat(Lat1);
    this.Lon1=parseFloat(Lon1);
    this.Lat2=parseFloat(Lat2);
    this.Lon2=parseFloat(Lon2);
}

//Тестовый список ошибок
function GetTestErrorList(XmlFileName)
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
function GetRDErrorList(XmlFileName)
{
  var EL = [];
   
  
  var xmlhttp = getXmlHttp1();
  xmlhttp.open('GET', XmlFileName, false);
  xmlhttp.send(null);
  if(xmlhttp.status == 200) 
  {
    var doc = xmlhttp.responseXML.documentElement;
   	   
   	//Проходимся по всем элементам-записям и составляем их репрезентацию
   	   	   
    var items = doc.getElementsByTagName("DuplicatePoint");
   
    var HouseLat=0;
    var HouseLon=0;
    var MyErrType="0";
    
  
    var intLen=0;
    intLen = items.length;
  
    
    for (var i = 0; i < intLen; i++)
    {
	  	  
	  //Отсчитываем с первого дочернего узла
	  var f_child = items[i].firstChild;

	  	  
	  do
  	  {
  	  	  
    	//Выбираем имя узла и в соответствии с этим выполняем необходимое действие
		switch (f_child.nodeName)
		{
			case "ErrType":
			  MyErrType=f_child.firstChild.nodeValue;
			  break;  
			  
			case "Coord":
              try{
               	HouseLat= f_child.getElementsByTagName("Lat")[0].firstChild.nodeValue;
               	HouseLon= f_child.getElementsByTagName("Lon")[0].firstChild.nodeValue;
              }
              catch(err){
                throw('Координаты точки не заданы');
              }
        	  break;
		}
  	  	  //Устанавливаем следующий узел
		f_child = f_child.nextSibling;	
		
      } while (f_child) ;
      
      EL.push (new ErrorItem(HouseLat,HouseLon, 'Ошибка топологии: дубликат рутингового ребра'));
      
    }//кц по ошибкам
    	
  }	//условия удачной загрузки xml
  else
    {throw new Error("Unable to load xml file with error list: "+XmlFileName);}
  
  return EL;
}

//Просроченные дороги
function GetHWCErrorList(XmlFileName)
{
  var EL = [];
   
  
  var xmlhttp = getXmlHttp1();
  xmlhttp.open('GET', XmlFileName, false);
  xmlhttp.send(null);
  if(xmlhttp.status == 200) 
  {
    var doc = xmlhttp.responseXML.documentElement;
   	   
   	//Проходимся по всем элементам-записям и составляем их репрезентацию
   	   	   
    var items = doc.getElementsByTagName("error");
   
    var HouseLat1=0;
    var HouseLon1=0;
    var HouseLat2=0;
    var HouseLon2=0;
    var MyErrType="0";
    var OpeningDate="";
    var CheckDate="";
    
  
    var intLen=0;
    intLen = items.length;
  
    
    for (var i = 0; i < intLen; i++)
    {
	  	  
	  //Отсчитываем с первого дочернего узла
	  var f_child = items[i].firstChild;
      OpeningDate="";
      CheckDate="";
	  	  
	  do
  	  {
  	  	  
    	//Выбираем имя узла и в соответствии с этим выполняем необходимое действие
		switch (f_child.nodeName)
		{
			case "opening_date":
			  OpeningDate=f_child.firstChild.nodeValue;
			  break;
			  
			case "check_date":
			  CheckDate=f_child.firstChild.nodeValue;
			  break;
			  
			case "bound":
              try{
               	HouseLat1= f_child.getAttribute("bottom");
               	HouseLon1= f_child.getAttribute("left");
               	HouseLat2= f_child.getAttribute("top");
               	HouseLon2= f_child.getAttribute("right");
              }
              catch(err){
                throw('Координаты точки не заданы');
              }
        	  break;
		}
  	  	  //Устанавливаем следующий узел
		f_child = f_child.nextSibling;	
		
      } while (f_child) ;
      
      var strErrType=items[i].getAttribute('errorType');
      var aDescr=""
      
      
      switch (strErrType) {
        case "CHECK_DATE_TOO_OLD":
          aDescr='Дорога давно не проверялась: '+CheckDate;
          break;
        case "OPENING_DATE_PASSED":
          aDescr='Ожидаемая дата открытия уже наступила: '+OpeningDate;
          break;
        case "CHECK_DATE_FORMAT_ERROR":
          aDescr='Неверный формат даты: '+CheckDate;
          break;  
        case "OPENING_DATE_FORMAT_ERROR":
          aDescr='Неверный формат даты: '+OpeningDate;
          break;
        default:
          aDescr=strErrType+': '+CheckDate +'/'+OpeningDate;
      }
      	  
      EL.push (new ErrorItemBBox(HouseLat1,HouseLon1, HouseLat2,HouseLon2, aDescr ));
      
    }//кц по ошибкам
    	
  }	//условия удачной загрузки xml
  else
    {throw new Error("Unable to load xml file with error list: "+XmlFileName);}
  
  return EL;
}


//Тупики важных дорог.
function GetDnodesErrorList(XmlFileName)
{
  var EL = [];
   
  
  var xmlhttp = getXmlHttp1();
  xmlhttp.open('GET', XmlFileName, false);
  xmlhttp.send(null);
  if(xmlhttp.status == 200) 
  {
    var doc = xmlhttp.responseXML.documentElement;
   	   
   	//Проходимся по всем элементам-записям и составляем их репрезентацию
   	   	   
    var items = doc.getElementsByTagName("DeadEnd");
   
    var HouseLat=0;
    var HouseLon=0;
    var MyErrType="0";
    
  
    var intLen=0;
    intLen = items.length;
  
    
    for (var i = 0; i < intLen; i++)
    {
	  	  
	  //Отсчитываем с первого дочернего узла

      var f_child = items[i].firstChild;
	  do
  	  {
  	  	  
    	//Выбираем имя узла и в соответствии с этим выполняем необходимое действие
		switch (f_child.nodeName)
		{
			  
			case "Coord":
              try{
               	HouseLat= f_child.getElementsByTagName("Lat")[0].firstChild.nodeValue;
               	HouseLon= f_child.getElementsByTagName("Lon")[0].firstChild.nodeValue;
              }
              catch(err){
                throw('Координаты точки не заданы');
              }
        	  break;
		}
  	  	  //Устанавливаем следующий узел
		f_child = f_child.nextSibling;	
		
      } while (f_child) ;
      
      
      EL.push (new ErrorItem(HouseLat,HouseLon, 'Ошибка присвоения статуса: тупик важной дороги'));
      
    }//кц по ошибкам
    	
  }	//условия удачной загрузки xml
  else
    {throw new Error("Unable to load xml file with error list: "+XmlFileName);}
  
  return EL;
}

//Улицы вне городов
function GetAddrStreetErrorList(XmlFileName)
{
  var EL = [];
   
  
  var xmlhttp = getXmlHttp1();
  xmlhttp.open('GET', XmlFileName, false);
  xmlhttp.send(null);
  if(xmlhttp.status == 200) 
  {
    var doc = xmlhttp.responseXML.documentElement;
   	   
   	//Проходимся по всем элементам-записям и составляем их репрезентацию
   	 
   	var testElement=doc.getElementsByTagName("StreetsOutsideCities");   	   
    var items =testElement[1].childNodes;
   
    var HouseLat=0;
    var HouseLon=0;
    var MyErrType="0";
    var StreetName="";
    
  
    var intLen=0;
    intLen = items.length;
  
    
    for (var i = 0; i < intLen; i++)
    if (items[i].nodeType==1)
    {
	  StreetName="б/имени";	  
	  //Отсчитываем с первого дочернего узла

      var f_child = items[i].firstChild;
	  do
  	  {
  	  	  
    	//Выбираем имя узла и в соответствии с этим выполняем необходимое действие
		switch (f_child.nodeName)
		{
			case "Street":
			  StreetName=f_child.firstChild.nodeValue;
			  break;  
			case "Coord":
              try{
               	HouseLat= f_child.getElementsByTagName("Lat")[0].firstChild.nodeValue;
               	HouseLon= f_child.getElementsByTagName("Lon")[0].firstChild.nodeValue;
              }
              catch(err){
                throw('Координаты точки не заданы');
              }
        	  break;
		}
  	  	  //Устанавливаем следующий узел
		f_child = f_child.nextSibling;	
		
      } while (f_child) ;
      
      
      EL.push (new ErrorItem(HouseLat,HouseLon, 'Улица за пределами НП: '+ StreetName ));
      
    }//кц по ошибкам
    	
  }	//условия удачной загрузки xml
  else
    {throw new Error("Unable to load xml file with error list: "+XmlFileName);}
  
  return EL;
}
//==========================================================================================================================
// Main function
//==========================================================================================================================
function ProcessMap(TestName,XmlFileName, ReportErrType1)
{	
 try{	
  var cloudmade = new CM.Tiles.OpenStreetMap.Mapnik();
  map = new CM.Map('cm-example', cloudmade);
  var topRight = new CM.ControlPosition(CM.TOP_RIGHT, new CM.Size(50, 20));
  map.addControl(new CM.LargeMapControl());
  map.addControl(new CM.ScaleControl());
 
  map.setCenter(new CM.LatLng(55.75,37.6), 5);
  
  //1.  - получение списка ошибок.
 
  
  var LatMin=90;
  var LonMin=180;
  var LatMax=0;
  var LonMax=0;
  
  var markers = [];
  var intMarkerCount=0;
  
  
  switch (TestName) {
    case "test":
      ErrorList=GetTestErrorList(XmlFileName);
      break;
    case "rd":
      ErrorList=GetRDErrorList(XmlFileName);
      break;
    case "hwc":
      ErrorList=GetHWCErrorList(XmlFileName);
      break;
    case "dnodes":
      ErrorList=GetDnodesErrorList(XmlFileName);
      break; 
    
    case "addr-street":
      ErrorList=GetAddrStreetErrorList(XmlFileName);
      break;  
        
    default:
      throw new Error("Unknown test: "+TestName);
  }


  
  
  
  //2. - отображение их на карте
  for (var i = 0; i < ErrorList.length; i++)
  {
  	  
    switch (ErrorList[i].Kind) {
      case "POINT":
        //Точка - для нее просто маркер
        //document.write( '<p> '+ErrorList[i].Descr+' ('+ErrorList[i].Lat+','+ErrorList[i].Lon+') </p>');
  	  
  	    if (ErrorList[i].Lat<LatMin) LatMin=ErrorList[i].Lat;
        if (ErrorList[i].Lat>LatMax) LatMax=ErrorList[i].Lat;
        if (ErrorList[i].Lon<LonMin) LonMin=ErrorList[i].Lon;
        if (ErrorList[i].Lon>LonMax) LonMax=ErrorList[i].Lon;
      
        markers.push(new CM.Marker(new CM.LatLng(ErrorList[i].Lat, ErrorList[i].Lon),{title: ErrorList[i].Descr}));
      	
        CM.Event.addListener(
    	                     markers[intMarkerCount],
    	                     'click',
    	                     function(latlng) { doClick(latlng.lat(),latlng.lng());}
   		     	            );
      		
        intMarkerCount=intMarkerCount+1;
        break;
      case "BBOX":
  	    if (ErrorList[i].Lat1<LatMin) LatMin=ErrorList[i].Lat1;
        if (ErrorList[i].Lat2>LatMax) LatMax=ErrorList[i].Lat2;
        if (ErrorList[i].Lon1<LonMin) LonMin=ErrorList[i].Lon1;
        if (ErrorList[i].Lon2>LonMax) LonMax=ErrorList[i].Lon2;
      
        markers.push(new CM.Marker(new CM.LatLng((ErrorList[i].Lat1+ErrorList[i].Lat2)/2, (ErrorList[i].Lon1+ErrorList[i].Lon2)/2),{title: ErrorList[i].Descr}));
      	
        CM.Event.addListener(
    	                     markers[intMarkerCount],
    	                     'click',
    	                     function(latlng) { doClick(latlng.lat(),latlng.lng());}
   		     	            );
      		
        intMarkerCount=intMarkerCount+1;
        var polygon = new CM.Polyline([
	        new CM.LatLng(ErrorList[i].Lat1, ErrorList[i].Lon1),
	        new CM.LatLng(ErrorList[i].Lat1, ErrorList[i].Lon2),
	        new CM.LatLng(ErrorList[i].Lat2, ErrorList[i].Lon2),
	        new CM.LatLng(ErrorList[i].Lat2, ErrorList[i].Lon1),
	        new CM.LatLng(ErrorList[i].Lat1, ErrorList[i].Lon1)
           ]);
        map.addOverlay(polygon);

        break;;  
      default:
        throw new Error("Unknown item kind: "+ErrorList[i].Kind);
      }
  	 
  }
      
    
  var bounds = new CM.LatLngBounds(
                      new CM.LatLng(LatMin, LonMin), 
                      new CM.LatLng(LatMax, LonMax));
  map.zoomToBounds(bounds );	
    
   // if ( intLen = 0 )
   // {
   //   typeErr.innerHTML = "<font color='#aa4411' size=+1>Ошибок по этому региону нет</font>";
   // }
    	
  var clusterer = new CM.MarkerClusterer(map, {clusterRadius: 60});
  clusterer.addMarkers(markers);
  
  document.write('<p><a href="javascript:void(0)" onclick="ShowNextProblem(0)">Показать следущую ошибку</a> -- ');  
  document.write('<a href="javascript:void(0)" onclick="ShowNextProblem(1)">Показать случайную ошибку</a> -- ');  
  document.write('<a href="javascript:void(0)" onclick="LoadCurrentView()">Загрузить текущий вид</a> -- ');
  document.write('<a href="javascript:void(0)" onclick="ThisPlaceAtOsm()">Это место на OSM.org</a> </p>');  
  document.write('<p> Всего ошибок:'+intMarkerCount+'</p>');
  

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

//-----------------------------------------------------------------------------------------------
 //  Показать следующую проблему
 //----------------------------------------------------------------------------------------------
function ShowNextProblem(blnShowRandom)
{
	var LatMin=0;
    var LonMin=0;
	var LatMax=0;
    var LonMax=0;
    
    if (blnShowRandom==0)
    {	
      CurrentMarker=CurrentMarker+1;
    }
    else
    {	
      CurrentMarker=Math.floor(Math.random()*ErrorList.length); 
    }	  
    
    if (CurrentMarker>=ErrorList.length)
    {CurrentMarker=0;}
    
     switch (ErrorList[CurrentMarker].Kind) {
      case "POINT":
        LatMin=ErrorList[CurrentMarker].Lat;
        LonMin=ErrorList[CurrentMarker].Lon;
        LatMax=ErrorList[CurrentMarker].Lat;
        LonMax=ErrorList[CurrentMarker].Lon;
	    break;
      case "BBOX":
        LatMin=ErrorList[CurrentMarker].Lat1;
        LonMin=ErrorList[CurrentMarker].Lon1;
        LatMax=ErrorList[CurrentMarker].Lat2;
        LonMax=ErrorList[CurrentMarker].Lon2;
      
	}
	var bounds = new CM.LatLngBounds(
                      new CM.LatLng(LatMin, LonMin), 
                      new CM.LatLng(LatMax, LonMax));
    map.zoomToBounds(bounds );
}

 //-----------------------------------------------------------------------------------------------
 //  Запрос данных к серверу (Вспомогательная фукция получения XMLHTTP )
 //-----------------------------------------------------------------------------------------------
    
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