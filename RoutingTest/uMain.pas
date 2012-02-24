unit uMain;

interface

procedure Main();
implementation

uses uMPparser,SysUtils,zADODB, ComObj, ADOInt,Variants,Classes,uVB6runtime,uRoutingTest ;

const
//Города
 RS_CITY_NAME = 'Name';
 RS_CITY_POPULATION = 'Population';
 RS_CITY_COORDS = 'Coords';
var  rsCities: zADODB.Recordset;


procedure AddCity(name:string; latlon:TLatLon;Population:Integer);
begin
  //Writeln(name,' ',Population, ' (', FormatFloat('#,00.000',lat),',',FormatFloat('#,00.000',lon),')');
 // Writeln(name,' ',Population, ' (', latlon,')');

  rsCities.AddNew(EmptyParam,EmptyParam);
  rsCities.Fields[RS_CITY_NAME].value:=name;
  rsCities.Fields[RS_CITY_POPULATION].value:=Population;
  rsCities.Fields[RS_CITY_COORDS].value:=latlon;
  rsCities.Update(EmptyParam,EmptyParam);
end;

procedure ParseLatLon(var lat,lon:real;latLon:string);
var j:integer;
begin
    j:=Pos(',',latlon);

    lat := StrToFloat(Copy(latlon,1,j-1));
    lon:= StrToFloat(Copy(latlon,j+1,length(latlon)-j));
end;

procedure AddRoad(MpSection:TMpSection);
var
  strDir,strData0:string;
  lstNodes:TStringList;
  intRoadID:integer;
  i,j:integer;
  latlon:string;
  RoutingNodes:array[0..999,1..2] of integer;
  strNodeAttr:string;
  NN:integer;
  lstRNode:TStringList;
begin
//  writeln(MpSection.GetAttributeValue('Label'));

  intRoadID:=StrToInt(MpSection.GetAttributeValue('RoadID'));//rsRoads.RecordCount+1;
  //rsRoads.AddNew(EmptyParam,EmptyParam);

  //Номер, просто по порядку



  //rsRoads.Fields[RS_ROAD_ID].value:= intRoadID;
  arRoads[intRoadID].Status:=OSMLevelByTag(MpSection.GetOsmHighway);

  //Разрешенные направления движения
  strDir:=MpSection.GetAttributeValue('DirIndicator');
  if strDir='' then
    arRoads[intRoadID].OneWay:=0
  else
    arRoads[intRoadID].OneWay:=strToInt(strDir);

 // rsRoads.Update(EmptyParam,EmptyParam);

  //Рутинговые ноды
   NN:=0;
   repeat
     strNodeAttr := MpSection.GetAttributeValue('Nod' + intToStr(NN));
     If strNodeAttr <> '' Then
       begin
          lstRNode := Split(strNodeAttr, ',');
          RoutingNodes[NN,  1] := strToInt(lstRNode[0]);
          RoutingNodes[NN , 2] := strToInt(lstRNode[1]);
          lstRNode.Free;
          NN:= NN + 1
        End;

   Until strNodeAttr = '';


  //Вершины данной дороги (координаты).
  //Их нужно извлечь из списка.
  strData0:=MpSection.GetAttributeValue('Data0');

  //(x1,y1),(x2,y2),(x3,y3), ...,(xN,yN)
  lstNodes := Split(strData0, '),');

  SetLength(arRoads[intRoadID].Vertex,lstNodes.Count);

  For i:= 0 To lstNodes.Count-1 do
  begin

    rsNodes.AddNew(EmptyParam,EmptyParam);

    rsNodes.Fields[RS_NODE_ROADID].Value := intRoadID;
    rsNodes.Fields[RS_NODE_ORDERNO].Value := i;

    latlon:=lstNodes[i];
    if vb6_Left(latlon,1)='('  then
      latlon:=copy(latlon,2,length(latlon)-1);

    if vb6_Right(latlon,1)=')'  then
      latlon:=copy(latlon,1,length(latlon)-1);

 //   writeln(latlon);
    j:=Pos(',',latlon);

    //rsNodes.Fields[RS_NODE_LAT].Value := StrToFloat(Copy(latlon,1,j-1));
    //rsNodes.Fields[RS_NODE_LON].Value := StrToFloat(Copy(latlon,j+1,length(latlon)-j));

    arRoads[intRoadID].Vertex[i].Lat:=StrToFloat(Copy(latlon,1,j-1));
    arRoads[intRoadID].Vertex[i].Lon:=StrToFloat(Copy(latlon,j+1,length(latlon)-j));

    rsNodes.Fields[RS_NODE_ROUTINGNODE].value:= -1;
    arRoads[intRoadID].Vertex[i].RoutingNodeID:= -1;

    for j := 0 to NN-1 do
      begin
        if RoutingNodes[j,1]=i then
        begin
          rsNodes.Fields[RS_NODE_ROUTINGNODE].value:= RoutingNodes[j,2];
          arRoads[intRoadID].Vertex[i].RoutingNodeID:=RoutingNodes[j,2];
          break;
        end;
      end;

  end;

  lstNodes.Free;
end;



Procedure Main();
const strFileName='d:\OSM\_osm2dcm\RoutingTest\RU-KGD.mp';

var MpParser:TMpParser;
    MpSection:TMpSection;
    T0,T1:TDateTime;
    lat1,lon1,lat2,lon2:real;
    strCity1,strCity2:string;
    aRoute:TRoute;
begin
  Writeln('RoutingTest, (c) Zkir 2012, CC-BY-SA 2.0 ');

  Writeln('Loading map: ',strFileName);
  T0:=Now;

{ 1. Прочесть польский файл.
     * Определить список городов для построения маршрутов
     * Построить некое представление дорожного графа }
  MpParser:=TMpParser.Create(strFileName);

  //Создадим список городов
  rsCities:= CoRecordset.Create;
  rsCities.Fields.Append( RS_CITY_NAME, adWChar, 255,0,EmptyParam);
  rsCities.Fields.Append( RS_CITY_POPULATION, adInteger,0,0,EmptyParam);
  rsCities.Fields.Append( RS_CITY_COORDS, adWChar, 255,0,EmptyParam);
  //rsCities.Fields.Append( RS_CITY_VALID, adInteger);
  //rsCities.Fields.Append( RS_CITY_ORIGTYPE, adWChar, 255);
  rsCities.Open (EmptyParam,EmptyParam,adOpenStatic, adLockBatchOptimistic,  adCmdText);

  //Создадим список дорог
    //В сущности, нужен только номер и признак односторонности.
  //rsRoads:= CoRecordset.Create;
  //rsRoads.Fields.Append( RS_ROAD_ID, adInteger, 0,0,EmptyParam);
  //rsRoads.Fields.Append( RS_ROAD_ONEWAY, adInteger,0,0,EmptyParam);
  //rsRoads.Fields.Append( RS_ROAD_STATUS, adInteger,0,0,EmptyParam);
  //rsRoads.Open (EmptyParam,EmptyParam,adOpenStatic, adLockBatchOptimistic,  adCmdText);

  //Создадим список вершин.
    //В какую дорогу входит, номер по порядку в данной дороге, координата,
    //и номер по порядку в данной вершине.
   //Вершины

  rsNodes:= CoRecordset.Create;
  rsNodes.Fields.Append( RS_NODE_ROADID, adInteger, 0,0,EmptyParam);
  rsNodes.Fields.Append( RS_NODE_ORDERNO, adInteger, 0,0,EmptyParam);

 // rsNodes.Fields.Append( RS_NODE_LAT, adDouble, 0,0,EmptyParam);
 // rsNodes.Fields.Append( RS_NODE_LON, adDouble, 0,0,EmptyParam);

  rsNodes.Fields.Append( RS_NODE_ROUTINGNODE, adInteger,0,0,EmptyParam);

  rsNodes.Open (EmptyParam,EmptyParam,adOpenStatic, adLockBatchOptimistic,  adCmdText);

  //Читаем исходный файл.
   while not MpParser.EOF do
   begin
     MpSection:=MpParser.ReadNextSection;

     //Добавим город в список городов
     If MpSection.SectionType = '[POI]' Then
       If MpSection.GetAttributeValue('City') = 'Y' Then
         if MpSection.GetAttributeValue('Population')<>'' then
           AddCity(MpSection.GetAttributeValue('Label'),
                   MpSection.GetCoords,
                   StrToInt(MpSection.GetAttributeValue('Population')) );

     //Добавим дорогу в тест рутинга
     If (MpSection.SectionType = '[POLYLINE]') Then
       If (MpSection.mpRouteParam <> '') Then
       If OSMLevelByTag(MpSection.GetOsmHighway) <= 2 Then
       begin
           AddRoad(MpSection);

       end;


     MpSection.Free;
   end;



  T1:=now;
  Writeln('Map loaded, in ',FormatFloat('#0.00', (T1-T0)*24*60*60),'  second(s)');

  Writeln('Cities :',rsCities.RecordCount);
  //Writeln('Roads :',rsRoads.RecordCount);
  Writeln('Nodes :',rsNodes.RecordCount);

  MpParser.Free;

{ 2. Между городами попарно постороить маршруты}
  rsCities.sort:=RS_CITY_POPULATION +' desc';
  rsCities.MoveFirst;
  rsCities.MoveNext;

  writeln(rsCities.Fields[RS_CITY_NAME].Value, ' ',rsCities.Fields[RS_CITY_COORDS].Value );
  StrCity1:=rsCities.Fields[RS_CITY_NAME].Value;

  ParseLatLon(lat1,lon1,rsCities.Fields[RS_CITY_COORDS].Value);

  rsCities.MoveNext;

  rsCities.MoveNext;
  rsCities.MoveNext;
  //rsCities.MoveNext;

  writeln(rsCities.Fields[RS_CITY_NAME].Value, ' ',rsCities.Fields[RS_CITY_COORDS].Value  );
  StrCity2:=rsCities.Fields[RS_CITY_NAME].Value;
  ParseLatLon(lat2,lon2,rsCities.Fields[RS_CITY_COORDS].Value);

  T0:=now;
  aRoute:=FindRoute(lat1,lon1,lat2,lon2);
  //rsRoute:=FindRoute(lat2,lon2,lat1,lon1);

  T1:=now;
  aRoute.SaveToGpx('d:\test.gpx',
                   StrCity1 + ' - ' + strCity2 + ' '+
                   FormatFloat('##0.00',aRoute.GetLengthKm) + 'km, found in '+FormatFloat('##0.00',(t1-t0)*24*60*60) + ' s' );



  writeln('Route ',aRoute.ElementCount,' node(s)');

  readln;


 {
  //Это просто тест.
  rsNodes.Filter:=RS_NODE_ROADID+'=1';
  repeat
    writeln(rsNodes.Fields[RS_NODE_LAT].Value, ' ',rsNodes.Fields[RS_NODE_LON].Value,' ',rsNodes.Fields[RS_NODE_ROUTINGNODE].Value  );
    rsNodes.MoveNext;
  until rsNodes.EOF;}

{ 4. Построенные маршруты сохранить в XML (главным образом важна длинна). }
//В gpx)
end;


end.
