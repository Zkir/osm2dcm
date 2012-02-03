unit uRoutingTest;
(*Тест рутинга
 что мы собираемся сделать:

 1. Прочесть польский файл.
 2. Определить список городов
 3. Между городами попарно постороить маршруты
 4. Построенные маршруты сохранить в XML (главным образом важна длинна).
  Маршруты можно будет потом сравнивать, и видеть, что если маршрут отклонился от
  того как было раньше, значит надо разбираться.
  Если кто-то в осм удалил кусок федеральной трассы, маршруты сильно пострадают.
*)

interface
uses zADODB,ComObj, ADOInt,Variants;
Const
  RS_ROUTE_ORDERNO='OrdNo';
  RS_ROUTE_LAT='Lat';
  RS_ROUTE_LON='Lon';

Function FindRoute(lat1,lon1,lat2,lon2:real):Recordset;

Const
//Города
 RS_CITY_NAME = 'Name';
 RS_CITY_POPULATION = 'Population';
 RS_CITY_COORDS = 'Coords';

//Дороги
 RS_ROAD_ID  = 'RoadID';
 RS_ROAD_ONEWAY  = 'Oneway';
 RS_ROAD_STATUS  = 'Status';

//Вершины
 RS_NODE_ROADID = 'RoadID';
 RS_NODE_ORDERNO = 'OrderNo';
 RS_NODE_LAT = 'Lat';
 RS_NODE_LON = 'Lon';
 RS_NODE_ROUTINGNODE = 'RoutingNode';

var
  rsCities: zADODB.Recordset;
  rsRoads: zADODB.Recordset;
  rsNodes: zADODB.Recordset;

implementation

uses SysUtils,Classes,Contnrs;

type

  // Элемент маршрута.
  // Это вершина с указанием кординат и номера в мп(?)
  TRouteElement = class
      RoadId:integer;
      NodeNumber:integer;
      lat:real;
      lon:real;
      strRoutingNodeNo:string;
      constructor Create(aRoadId:integer; aNodeNumber:integer);Overload;
      constructor Create(aRoadId:integer; aNodeNumber:integer;aLat,aLon:Real;aRoutingNodeNo:string);Overload;
      constructor CreateCopy(Source:TRouteElement);
      //destructor Destroy; Override;
  end;


  // Маршрут - список вершин, входящих в него
  TRoute=class
    private
      FRouteElements:TObjectList;
      Length:real;//Длинна построеной части
      RemainingLength:real;//Оценка длинны оставшейся части

      //Искомая конечная точка маршрута
      FinishLat:real;
      FinishLon:real;

      function GetElementCount:integer;
      function GetElement(Index : Integer):TRouteElement;
    public

      property ElementCount:integer read GetElementCount;
      property Elements[Index : Integer]:TRouteElement  read GetElement;  default;
      //function ExplicitWordformCount:integer;
      //Добавление элемента в шаблон
      //function AddElement(const strWordForm,strPartOfSpeach,strGrammems: String;
      //                    blnTerminalElement:boolean):integer;
      //function AddElementCopy(Element:TSyntaxPatternElement):integer;


      constructor CreateInitialRoute(aStartRoadID,aStartNodeID,aFinishRoadID,aFinishNodeID:integer);
      constructor ContinueRoute(aRoute:TRoute; Continuation:TRouteElement);
      function TestLoops:boolean;
      function TestFinish:boolean;
      function TestDuplicate(aRoute:TRoute):boolean;
      //constructor CreateCopy(Source:TSyntaxPattern);

      destructor Destroy; override;
      function FullLength:real;
      //function AsString():String;
  end;

{
type
 //  Синтаксический анализатор, на основе порождающей грамматики
   TGenerativeGrammar = class
    private
        FAllRules:TObjectList;//Список правил, используемых для 'порождения'
        FWorkingRuleSet:TObjectList;// Рабочий набор правил, подмножетство FAllRules
        //FNonRootRules:TObjectList;
        FNonTerminalCategoriesList:TStringList;
        FTerminalCategoriesList:TStringList;
        function GetAllRuleList: TObjectList;
        Procedure ExpandVariables(PatternList: TObjectList);
        Procedure ProcessPermutations(PatternList: TObjectList);
        procedure CleanUpRules(Phrase: TPhrase);

        function GetRuleListSubset(RightElement:TSyntaxPatternElement): TObjectList;
        function ModifyTransformationFormula(strFormula, strElFormula:String;N,M,j:integer):String;
    public
      //Эта функция собственно и делает ситаксический разбор.
      // по заданной фразе возвращается список соответствующих ей синтаксических структур.
      function GetPatternList(Phrase: TPhrase): TObjectList;

      //Обертка вокруг предыдущей фунуции
      function SyntaxAnalysis(Phrase: TPhrase; var intMatchedWords,
                              intUnmatchedWords:integer): TSyntaxPattern;
      constructor Create();
      destructor Destroy; Override;

  end;
}

constructor TRouteElement.Create(aRoadId:integer; aNodeNumber:integer);
var varFilter:OleVariant;
begin

  RoadId:=aRoadId;
  NodeNumber:=aNodeNumber;
  varFilter:=rsNodes.Filter;
  rsNodes.Filter:= RS_NODE_ROADID+'='+IntToStr(aRoadId)+' and '+
                   RS_NODE_ORDERNO+'='+Inttostr(aNodeNumber);


  lat:=rsNodes.Fields[RS_NODE_LAT].Value;
  lon:=rsNodes.Fields[RS_NODE_LON].Value;
  strRoutingNodeNo:=trim(rsNodes.Fields[RS_NODE_ROUTINGNODE].Value);
  rsNodes.Filter:=varFilter;
end;

constructor TRouteElement.Create(aRoadId:integer; aNodeNumber:integer;aLat,aLon:Real;aRoutingNodeNo:string);

begin
  RoadId:=aRoadId;
  NodeNumber:=aNodeNumber;
  lat:=aLat;
  lon:=aLon;
  strRoutingNodeNo:=aRoutingNodeNo;
end;

constructor  TRouteElement.CreateCopy(Source:TRouteElement) ;
begin
  RoadId:=Source.RoadId;
  NodeNumber:=Source.NodeNumber ;
  lat:=Source.lat;
  lon:=Source.lon;
  strRoutingNodeNo:=Source.strRoutingNodeNo;
end;



//Расстояние между двумя точками
Function Distance(const lat1,lon1,lat2,lon2:double):double;
begin
  //для простоты растояние прямо в градусах.
  result:=sqrt(sqr(lat2-lat1)+sqr(lon2-lon1));
end;

constructor TRoute.CreateInitialRoute(aStartRoadID,aStartNodeID,aFinishRoadID,aFinishNodeID:integer);
var aNode:TRouteElement;
begin
  FRouteElements:=TObjectList.Create;
  aNode:=TRouteElement.Create(aStartRoadID,aStartNodeID);
  FRouteElements.Add(aNode);
  Length:=0;

  rsNodes.Filter:= RS_NODE_ROADID+'='+IntToStr(aFinishRoadID)+' and '+
                   RS_NODE_ORDERNO+'='+IntTostr(aFinishNodeID);


  FinishLat:=rsNodes.Fields[RS_NODE_LAT].Value;
  FinishLon:=rsNodes.Fields[RS_NODE_LON].Value;
  rsNodes.Filter:=adFilterNone;

  RemainingLength:=Distance(Elements[0].Lat,
                            Elements[0].Lon,
                            FinishLat,FinishLon)
end;

constructor TRoute.ContinueRoute(aRoute:TRoute; Continuation:TRouteElement);
var
  i:integer;
begin
  FRouteElements:=TObjectList.Create;
  //Cкопировать вершины исходного маршрута
  Length:=aRoute.Length;
  RemainingLength:=aRoute.RemainingLength;
  FinishLat:=aRoute.FinishLat;
  FinishLon:=aRoute.FinishLon;

  for i := 0 to aRoute.ElementCount-1 do
    FRouteElements.Add(TRouteElement.CreateCopy(aRoute[i]));
  //Добавим новую вершину
  FRouteElements.Add(TRouteElement.CreateCopy(Continuation));

  //Пересчитаем длины
  Length:=Length+Distance(Elements[ElementCount-2].Lat,
                          Elements[ElementCount-2].Lon,
                          Elements[ElementCount-1].Lat,
                          Elements[ElementCount-1].Lon);

  RemainingLength:=Distance(Elements[ElementCount-1].Lat,
                            Elements[ElementCount-1].Lon,
                            FinishLat,FinishLon)

end;

destructor TRoute.Destroy;
begin
  FRouteElements.Free;
end;

function TRoute.GetElement(Index : Integer):TRouteElement;
begin
  Result:=TRouteElement(FRouteElements[Index]);
end;

function TRoute.GetElementCount:integer;
begin
  Result:=FRouteElements.Count;
end;

function TRoute.TestFinish():boolean;
begin
  Result:=not (RemainingLength>0);
end;

function TRoute.FullLength:real;
begin
  Result:=Length/1.25+RemainingLength;
end;

function TRoute.TestLoops:boolean;
var i:integer;
begin
  Result:=False;
  for i :=ElementCount-2  downto 0  do
   if (Elements[i].lat=Elements[ElementCount-1].lat) and(Elements[i].lon=Elements[ElementCount-1].lon)     then
   begin
     Result:=true;
     break;
   end;

end;

//Проверяем, что оба маршрута ведут в одну и ту же точку.
function TRoute.TestDuplicate(aRoute:TRoute):boolean;
begin
  Result:=False;
  if (Elements[ElementCount-1].lat=aRoute.Elements[aRoute.ElementCount-1].lat ) and
     (Elements[ElementCount-1].lon=aRoute.Elements[aRoute.ElementCount-1].lon ) then
    Result:=True;

end;



//Самая сложная функция. Список вершин, которыми можно продолжить данный маршрут.
function GetContinuationsList(aRoute:TRoute):TObjectList;
var CurrentNode,NewNode:TRouteElement;
  StartRoadID,StartNodeID:integer;
  strRoutingNodeNo:string;

  NewRoadId,NewNodeNumber:integer;
  rsLinkedNodes:Recordset;

  dir:integer;

begin
  Result:=TObjectList.Create;
  CurrentNode:=aRoute.Elements[aRoute.ElementCount-1];
  strRoutingNodeNo:=trim(CurrentNode.strRoutingNodeNo);
  if strRoutingNodeNo='721' then
     writeln('!');


  //По текущей дороге вверх и/или вниз.
  dir:=0;
  if aRoute.ElementCount>=2  then
    if aRoute[aRoute.ElementCount-1].RoadId = aRoute[aRoute.ElementCount-2].RoadId  then
    begin
      if (aRoute[aRoute.ElementCount-1].NodeNumber - aRoute[aRoute.ElementCount-2].NodeNumber)>0   then
        dir:=1
       else
        dir:=-1;
    end;

  rsRoads.Filter:=RS_ROAD_ID+'='+IntToStr(CurrentNode.RoadId);
  if rsRoads.RecordCount<>0   then
    if rsRoads.Fields[RS_ROAD_ONEWAY].Value<>0 then
      dir:=rsRoads.Fields[RS_ROAD_ONEWAY].Value ;
  rsRoads.Filter:=adFilterNone;

if (dir=0) or  (dir=1) then
begin
  rsNodes.Filter:= RS_NODE_ROADID+'='+IntToStr(CurrentNode.RoadID)+' and '+
                   RS_NODE_ORDERNO+'='+IntTostr(CurrentNode.NodeNumber+1);

  if rsNodes.RecordCount<>0  then
  begin
    NewNode:=TRouteElement.Create(CurrentNode.RoadID,CurrentNode.NodeNumber+1,
                                  rsNodes.Fields[RS_NODE_LAT].Value,
                                  rsNodes.Fields[RS_NODE_LON].Value,
                                  rsNodes.Fields[RS_NODE_ROUTINGNODE].Value  );
    Result.Add(NewNode);
  end;
end;
if (dir=0) or  (dir=-1) then
begin
  rsNodes.Filter:= RS_NODE_ROADID+'='+IntToStr(CurrentNode.RoadID)+' and '+
                   RS_NODE_ORDERNO+'='+IntTostr(CurrentNode.NodeNumber-1);

  if rsNodes.RecordCount<>0  then
  begin
    NewNode:=TRouteElement.Create(CurrentNode.RoadID,CurrentNode.NodeNumber-1,
                                  rsNodes.Fields[RS_NODE_LAT].Value,
                                  rsNodes.Fields[RS_NODE_LON].Value,
                                  rsNodes.Fields[RS_NODE_ROUTINGNODE].Value  );
    Result.Add(NewNode);
  end;
end;

  //По смежным дорогам вверх и вниз.




  if strRoutingNodeNo<>'' then
  begin
     rsLinkedNodes:=rsNodes.Clone( adLockBatchOptimistic ) ;
     rsLinkedNodes.Filter:= RS_NODE_ROUTINGNODE+'='+strRoutingNodeNo;
    //Это мы нашли вершины, смежные с данной.
     while not rsLinkedNodes.EOF do
     begin
       NewRoadId:= rsLinkedNodes.Fields[RS_NODE_ROADID].Value;
       NewNodeNumber := rsLinkedNodes.Fields[RS_NODE_ORDERNO].Value;
       if (NewRoadId<>CurrentNode.RoadID) {and (NewNodeNumber<>CurrentNode.NodeNumber)} then
       begin

         rsNodes.Filter:= RS_NODE_ROADID+'='+IntToStr(NewRoadID)+' and '+
                          RS_NODE_ORDERNO+'='+IntTostr(NewNodeNumber+1);

         if rsNodes.RecordCount<>0  then
         begin
            NewNode:=TRouteElement.Create(NewRoadID,NewNodeNumber+1,
                                          rsNodes.Fields[RS_NODE_LAT].Value,
                                          rsNodes.Fields[RS_NODE_LON].Value,
                                          rsNodes.Fields[RS_NODE_ROUTINGNODE].Value  );
            Result.Add(NewNode);
         end;

         rsNodes.Filter:= RS_NODE_ROADID+'='+IntToStr(NewRoadID)+' and '+
                          RS_NODE_ORDERNO+'='+IntTostr(NewNodeNumber-1);

         if rsNodes.RecordCount<>0  then
         begin
           NewNode:=TRouteElement.Create(NewRoadID,NewNodeNumber-1,
                                        rsNodes.Fields[RS_NODE_LAT].Value,
                                        rsNodes.Fields[RS_NODE_LON].Value,
                                        rsNodes.Fields[RS_NODE_ROUTINGNODE].Value  );
           Result.Add(NewNode);
         end;

       end;
       rsLinkedNodes.Movenext;
     end;

  end;

  rsNodes.Filter:=adFilterNone;

end;

{ Теперь самое интересное :)
    Работаем с множеством маршрутов.
    На первом шаге в маршрут добавляем начальную точку.

    На каждом шаге алгоритма - находим наиболее привлекательный вариант.
    Для него смотрим какие есть варианты продолжения, их добавляем в множество, а сам маршрут исключаем.
    Продолжаем до тех пор, пока конечная точка не будет достигнута
 }
Function CreateRoute(StartRoadID,StartNodeID:integer;
                     FinishRoadID,FinishNodeID:integer):TRoute;
var
  FWorkingSet,Continuations:TObjectList;
  aRoute:TRoute;
  i:integer;
  K:integer;
  l:real;
begin
  FWorkingSet:= TObjectList.Create;

  aRoute:=TRoute.CreateInitialRoute(StartRoadID,StartNodeID,FinishRoadID,FinishNodeID);
  FWorkingSet.Add(aRoute);
  repeat
    //Найдем наиболее привлекательный маршрут для продолжения

    K:=FWorkingSet.Count-1; //Ищем наиболее привлекательный
                     //маршртут для продолжения, начиная с конца очереди.

    l:=TRoute(FWorkingSet[K]).FullLength;
    for i := FWorkingSet.Count-1 downto 0  do
      if TRoute(FWorkingSet[i]).FullLength<l then
      begin
        l:=TRoute(FWorkingSet[i]).FullLength;
        K:=i;
      end;

    //Устраним дубликаты.
    for i := FWorkingSet.Count-1 downto 0 do
      if i<>K then
        if TRoute(FWorkingSet[i]).TestDuplicate(TRoute(FWorkingSet[K])) then
        begin
          FWorkingSet.Delete(i);
          if K>i then K:=K-1;
          writeln('Duplicate deleted');

        end;

    writeln('*', FWorkingSet.Count,' ',TRoute(FWorkingSet[K]).FullLength,' ',
            TRoute(FWorkingSet[K]).RemainingLength,' ', TRoute(FWorkingSet[K]).ElementCount  );

    if (TRoute(FWorkingSet[K]).TestFinish()) or
        (TRoute(FWorkingSet[K]).ElementCount>1000 )  then  break;

    //Надо получить  список вершин, которыми можно продожить данный маршрут
    Continuations:=GetContinuationsList(TRoute(FWorkingSet[K]));
    for i := 0 to Continuations.Count-1  do
    begin
      aRoute:=TRoute.ContinueRoute(TRoute(FWorkingSet[K]),
                                   TRouteElement(Continuations[i]));
      if not aRoute.TestLoops then
        FWorkingSet.Add(aRoute)
      else
       aRoute.Free;
    end;
    //После того как все возможные продолжения обработаны, маршрут удаляется.
    FWorkingSet.Delete(K);
    Continuations.Free;



  until (FWorkingSet.Count=0);

  Result:= TRoute(FWorkingSet[K]);
  //Найденный маршрут удаляется из списка
  FWorkingSet.Extract(Result);

  FWorkingSet.Free;
end;


//Функция, которая по заданной начальной и конечной точке возращает маршрут.
Function FindRoute(lat1,lon1,lat2,lon2:real):Recordset;
var
  StartRoadID,StartNodeID:integer;
  FinishRoadID,FinishNodeID:integer;
  DStart,DFinish:real;
  DStartMin,DFinishMin:real;
  xml:TStringList;
  aRoute:TRoute;
  i:integer;
  t0,t1:TDateTime;
Begin
  t0:=Now;
  result:= CoRecordset.Create;
  result.Fields.Append( RS_ROUTE_ORDERNO, adInteger, 0,0,EmptyParam);
  result.Fields.Append( RS_ROUTE_LAT, adDouble, 0,0,EmptyParam);
  result.Fields.Append( RS_ROUTE_LON, adDouble, 0,0,EmptyParam);
  result.Open (EmptyParam,EmptyParam,adOpenStatic, adLockBatchOptimistic,  adCmdText);

  //Нужно найти начальную и конечную точки, принадлежащие графу, ближайшие к заданным

  //Берем первую вершину
  rsNodes.MoveFirst;
  StartRoadID:=rsNodes.Fields[RS_NODE_ROADID].Value;
  StartNodeID:=rsNodes.Fields[RS_NODE_ORDERNO].Value;
  FinishRoadID:=rsNodes.Fields[RS_NODE_ROADID].Value;
  FinishNodeID:=rsNodes.Fields[RS_NODE_ORDERNO].Value;

  DStartMin:= Distance(lat1,lon1,rsNodes.Fields[RS_NODE_LAT].Value,rsNodes.Fields[RS_NODE_LON].Value);
  DFinishMin:=Distance(lat2,lon2,rsNodes.Fields[RS_NODE_LAT].Value,rsNodes.Fields[RS_NODE_LON].Value);

  repeat
    DStart:= Distance(lat1,lon1,rsNodes.Fields[RS_NODE_LAT].Value,rsNodes.Fields[RS_NODE_LON].Value);
    if DStart<DStartMin then
      begin
        StartRoadID:=rsNodes.Fields[RS_NODE_ROADID].Value;
        StartNodeID:=rsNodes.Fields[RS_NODE_ORDERNO].Value;
        DStartMin:=DStart;
      end;

    DFinish:=Distance(lat2,lon2,rsNodes.Fields[RS_NODE_LAT].Value,rsNodes.Fields[RS_NODE_LON].Value);
    if DFinish<DFinishMin then
      begin
        FinishRoadID:=rsNodes.Fields[RS_NODE_ROADID].Value;
        FinishNodeID:=rsNodes.Fields[RS_NODE_ORDERNO].Value;
        DFinishMin:=DFinish;
      end;

    rsNodes.MoveNext;
  until rsNodes.EOF;

  {rsNodes.Filter:= RS_NODE_ROADID+'='+IntToStr(StartRoadID)+' and '+ RS_NODE_ORDERNO+'='+Inttostr(StartNodeID);
  writeln(rsNodes.Fields[RS_NODE_LAT].Value,',',rsNodes.Fields[RS_NODE_LON].Value);

  rsNodes.Filter:= RS_NODE_ROADID+'='+IntToStr(FinishRoadID)+' and '+ RS_NODE_ORDERNO+'='+Inttostr(FinishNodeID);
  writeln(rsNodes.Fields[RS_NODE_LAT].Value,',',rsNodes.Fields[RS_NODE_LON].Value);}

  aRoute:=CreateRoute(StartRoadID,StartNodeID,FinishRoadID,FinishNodeID);
  t1:=Now;
  xml:=TStringList.Create;
  xml.Add('<?xml version="1.0" encoding="WINDOWS-1251"?>');
  xml.Add('<gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xmlns="http://www.topografix.com/GPX/1/0" creator="Polar WebSync 2.3 - www.polar.fi" xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd">');
  xml.Add('<time>2011-09-22T18:56:51Z</time>');
  xml.Add('<trk>');
  xml.Add('  <name>found in '+FormatFloat('##0.00',(t1-t0)*24*60*60) + ' s</name>');
  xml.Add('  <trkseg>');
  for i := 0  to aRoute.ElementCount-1 do
  begin
    xml.Add('    <trkpt lat="'+FormatFloat('#0.0000000000',aRoute[i].lat) +'" lon="'+FormatFloat('#0.0000000000',aRoute[i].lon)+'">');
    xml.Add('    </trkpt>');
  end;
  xml.Add('  </trkseg>');
  xml.Add('</trk>');
  xml.Add('</gpx>');
  xml.SaveToFile('d:\test.gpx');
End;



end.
