VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
  Persistable = 0  'NotPersistable
  DataBindingBehavior = 0  'vbNone
  DataSourceBehavior  = 0  'vbNone
  MTSTransactionMode  = 0  'NotAnMTSObject
END
Attribute VB_Name = "clsRoutingTest"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
'***************************************************************************
'Тест связности рутингового графа
'***************************************************************************
Option Explicit

'Константы полей в рекордсетах
'Подграфы
Const RS_SUBGRAPH_ID = "Id"
Const RS_SUBGRAPH_ROADCOUNT = "RoadCount"
Const RS_SUBGRAPH_LAT1 = "Lat1"
Const RS_SUBGRAPH_LON1 = "Lon1"
Const RS_SUBGRAPH_LAT2 = "Lat2"
Const RS_SUBGRAPH_LON2 = "Lon2"
Const RS_SUBGRAPH_HAS_EXTNODES = "HasExternalNodes"
Const RS_SUBGRAPH_KNOWN_EXCEPTION = "KnownException"

'Вершины
Const RS_NODE_ID = "Id"
Const RS_NODE_SUBGRAPH = "SubGraphID"

'Рекордсеты с данными
Dim rsSubGraph     As ADODB.Recordset
Dim rsNodes        As ADODB.Recordset
Dim intSubgraphCount As Long


Const RS_EXCEP_MAPCODE = "MapCode"
Const RS_EXCEP_NAME = "Name"
Const RS_EXCEP_LAT = "Lat"
Const RS_EXCEP_LON = "Lon"
Const RS_EXCEP_DESCR = "Descr"
Const RS_EXCEP_N = "n"

Dim rsExceptions As ADODB.Recordset

Private NTotalRoads As Long
Private NRoutiningEdges As Long



Private Sub Class_Initialize()

 On Error GoTo finalize
  
  'Подграфы
  Set rsSubGraph = New ADODB.Recordset
  rsSubGraph.Fields.Append RS_SUBGRAPH_ID, adInteger
  rsSubGraph.Fields.Append RS_SUBGRAPH_ROADCOUNT, adInteger
  rsSubGraph.Fields.Append RS_SUBGRAPH_LAT1, adDouble
  rsSubGraph.Fields.Append RS_SUBGRAPH_LON1, adDouble
  rsSubGraph.Fields.Append RS_SUBGRAPH_LAT2, adDouble
  rsSubGraph.Fields.Append RS_SUBGRAPH_LON2, adDouble
  rsSubGraph.Fields.Append RS_SUBGRAPH_HAS_EXTNODES, adBoolean
  rsSubGraph.Fields.Append RS_SUBGRAPH_KNOWN_EXCEPTION, adBoolean
  
    
  rsSubGraph.Open
  rsSubGraph(RS_SUBGRAPH_ID).Properties("Optimize") = True
  
  'Вершины
  Set rsNodes = New ADODB.Recordset
  rsNodes.Fields.Append RS_NODE_ID, adInteger
  rsNodes.Fields.Append RS_NODE_SUBGRAPH, adInteger
  rsNodes.Open
  rsNodes(RS_NODE_ID).Properties("Optimize") = True
  rsNodes(RS_NODE_SUBGRAPH).Properties("Optimize") = True
  
  
  'Начальное значение счетчика подграфов
  intSubgraphCount = 0
  NTotalRoads = 0
  NRoutiningEdges = 0
  
  'Создадим рекордсет c исключениями
  Set rsExceptions = New ADODB.Recordset
  rsExceptions.Fields.Append RS_EXCEP_MAPCODE, adVarWChar, 10
  rsExceptions.Fields.Append RS_EXCEP_LAT, adDouble
  rsExceptions.Fields.Append RS_EXCEP_LON, adDouble
  rsExceptions.Fields.Append RS_EXCEP_NAME, adVarWChar, 256
  rsExceptions.Fields.Append RS_EXCEP_DESCR, adVarWChar, 256
  rsExceptions.Fields.Append RS_EXCEP_N, adInteger ' Число найденных исключений
  
  
     
  rsExceptions.Open
  
  
finalize:
  If Err.Number <> 0 Then
    Err.Raise Err.Number, "Class_Initialize", Err.Description & " clsAddrRegistry.Initialize:" & Erl
  End If
End Sub

Private Sub Class_Terminate()
  Set rsSubGraph = Nothing
  Set rsNodes = Nothing
End Sub

'Заполнение списка исключений (действительно оторванных кусков)
'RU-SPO|Название|59.749664, 32.9696697|комментарий
Public Sub FillExceptionList()
  Dim strExceptionFileName As String
  
  Dim ObjXml As MSXML2.DOMDocument
  Set ObjXml = New MSXML2.DOMDocument
  
  strExceptionFileName = "IsolatedSubGraphList.xml"
  
  If Not ObjXml.Load(strExceptionFileName) Then
    Err.Raise vbObjectError, "clsRoutingTest.FillExceptionList", "Unable to load exception list for isolated subgraph test: " & strExceptionFileName
  End If
  
  Dim strExceptionList As String
  strExceptionList = ObjXml.documentElement.getElementsByTagName("rev").Item(0).Text
  

  
  Dim strLine As String
  Dim strLines() As String
  Dim strValues() As String
  Dim blnInSection As Boolean
  Dim i As Integer
  strLines = Split(strExceptionList, vbLf)
  
  blnInSection = False
  For i = LBound(strLines) To UBound(strLines)
    strLine = Trim(strLines(i))
    'Debug.Print strLine
    If (strLine) = "<pre>" Then
      blnInSection = True
    End If
    
    If (strLine) = "</pre>" Then
      blnInSection = False
    End If
    
    If blnInSection Then
      If (Left(strLine, 1) <> "#") And (strLine <> "") And (strLine <> "<pre>") Then
      strValues = Split(strLine, "|")
      If UBound(strValues) = 4 Then
        AddException strValues(0), strValues(1), Split(strValues(2), ",")(0), Split(strValues(2), ",")(1), strValues(3), strValues(4)
      End If
    
      End If
    End If
  Next i
  
  'Здесь нужно парсить файл
  'D:\OSM\_osm2dcm\SubGraphList.xml
  

End Sub
'Добавление исключения в список
Private Sub AddException(ByVal strMapCode As String, _
                         ByVal strName As String, _
                         ByVal dblLat As Double, _
                         ByVal dblLon As Double, _
                         ByVal strMaxStatus As String, _
                         ByVal strDescr As String)
                         
  rsExceptions.AddNew
  rsExceptions(RS_EXCEP_MAPCODE).Value = strMapCode
  rsExceptions(RS_EXCEP_LAT).Value = dblLat
  rsExceptions(RS_EXCEP_LON).Value = dblLon
  rsExceptions(RS_EXCEP_NAME).Value = Left(strName, rsExceptions(RS_EXCEP_NAME).DefinedSize)
  rsExceptions(RS_EXCEP_DESCR).Value = Left(strDescr, rsExceptions(RS_EXCEP_DESCR).DefinedSize)
  rsExceptions(RS_EXCEP_N).Value = 0
End Sub


'Самая важная функция.
'Добавляется дорога.
'Передается ID дороги и список номеров ее вершин.
'И еще хорошо бы BBOX
'
'алгоритм прост как колобок.
'1. Начать с пустого списка подграфов.
'2. Взять следующую дорогу из списка
'3. Проверить, есть ли подграф (подграфы), в который уже входят  рутинговые ноды данного графа.
'* Если нет, добавить в список новый подграф, дорогу и ее ноды пометить как относящиеся
'  к данному подграфу.
'* Если есть ровно один такой подграф, дорогу и ее ноды пометить как относящиеся к данному подграфу.
'* Если таких подграфов несколько (данная дорога входит в несколько из уже найденных подграфов,
'  являсь, таким образом, перемычкой ними)
'- оставить только первый подграф, остальные исключить из списка, относящиеся к ним ноды
'  и дороги пометить как относящиеся к первому подграфу
'4. Продолжать с пункта 2, пока есть необработанные ребра.
'5. Отобразить список найденных подграфов, в порядке убывания(или возрастания) числа ребер.
'В идеале должен быть один подграф. Практически может быть несколько (какие-нибудь карты островов).
' В любом случае подграфы с несколькими (до десятка) ребрами суть ошибки.
'
' NodeList() - список айдишников рутинговых вершин
' NodeExtrAttrList() - 0 для обычной вершины и 1 для внешней (которая соединяется с чем-то за пределами карты)
' Подграф, содержащий внешние вершины, не является вполне изолированным.
Public Sub AddRoad(NodeList() As Long, NodeExtrAttrList() As Long, _
                   lat1 As Double, lon1 As Double, lat2 As Double, lon2 As Double)
Dim i As Integer
Dim N As Long ' число найденных подграфов
Dim SubGraphs(10000) As Long '  найденные подграфы (1..N)
Dim blnHasExternalNodes As Boolean

  N = 0
  blnHasExternalNodes = False
  For i = LBound(NodeList) To UBound(NodeList)
    rsNodes.Filter = RS_NODE_ID & "=" & NodeList(i)
    If rsNodes.RecordCount > 1 Then
      Err.Raise vbObjectError, "AddRoad", "Error on the previous step, Node belongs to more than one sub-graph"
    End If
    If rsNodes.RecordCount > 0 Then
      'Подграф найден
      N = N + 1
      SubGraphs(N) = rsNodes(RS_NODE_SUBGRAPH).Value
    End If
    
    If NodeExtrAttrList(i) = 1 Then
      blnHasExternalNodes = True
    End If
  Next i
  
  If N = 0 Then
    ' Добавим новый подграф
    rsSubGraph.AddNew
    rsSubGraph(RS_SUBGRAPH_ID).Value = intSubgraphCount 'rsSubGraph.RecordCount
    intSubgraphCount = intSubgraphCount + 1
    rsSubGraph(RS_SUBGRAPH_ROADCOUNT).Value = 0
    rsSubGraph(RS_SUBGRAPH_LAT1).Value = lat1
    rsSubGraph(RS_SUBGRAPH_LON1).Value = lon1
    rsSubGraph(RS_SUBGRAPH_LAT2).Value = lat2
    rsSubGraph(RS_SUBGRAPH_LON2).Value = lon2
    rsSubGraph(RS_SUBGRAPH_HAS_EXTNODES).Value = blnHasExternalNodes
        
    N = 1
    SubGraphs(1) = rsSubGraph(RS_SUBGRAPH_ID).Value
  End If
  
  'Объединим найденные подграфы
  For i = 2 To N
    If SubGraphs(1) <> SubGraphs(i) Then
      MergeSubGraphs SubGraphs(1), SubGraphs(i)
    End If
  Next i
  
  'Добавим вершины нашей дороги в первый подграф
  For i = LBound(NodeList) To UBound(NodeList)
    AddNodeToSubGraph SubGraphs(1), NodeList(i)
  Next i
  
  'Добавим дорогу в наш первый подграф.
  '* число ребер увеличивется на единицу
  '* Bbox расширяется
  
  rsSubGraph.Filter = RS_SUBGRAPH_ID & "=" & SubGraphs(1)
  rsSubGraph(RS_SUBGRAPH_ROADCOUNT).Value = rsSubGraph(RS_SUBGRAPH_ROADCOUNT).Value + 1
  If lat1 < rsSubGraph(RS_SUBGRAPH_LAT1).Value Then rsSubGraph(RS_SUBGRAPH_LAT1).Value = lat1
  If lon1 < rsSubGraph(RS_SUBGRAPH_LON1).Value Then rsSubGraph(RS_SUBGRAPH_LON1).Value = lon1
  If lat2 > rsSubGraph(RS_SUBGRAPH_LAT2).Value Then rsSubGraph(RS_SUBGRAPH_LAT2).Value = lat2
  If lon2 > rsSubGraph(RS_SUBGRAPH_LON2).Value Then rsSubGraph(RS_SUBGRAPH_LON2).Value = lon2
  
  'Если в данной дороге есть внешняя нода, значит во всем подграфе есть внешняя нода.
  If blnHasExternalNodes Then rsSubGraph(RS_SUBGRAPH_HAS_EXTNODES).Value = True
  
  'Общее число дорог так же увеличивается.
  
  NTotalRoads = NTotalRoads + 1
  
  'число рутинговых ребер в данной дороге равно количеству рутинговых вершин минус один.
  NRoutiningEdges = NRoutiningEdges + (UBound(NodeList) - LBound(NodeList))

  
  rsSubGraph.Filter = adFilterNone
  
  
End Sub
'Граф intGraph2ID присоединяется к графу intGraph1ID
Private Sub MergeSubGraphs(ByVal intGraph1ID As Long, ByVal intGraph2ID As Long)
Dim Nroads As Long
Dim lat1 As Double, lon1 As Double, lat2 As Double, lon2 As Double
Dim blnHasExternalNodes As Boolean
Dim varNodeFilter
Dim varSGFilter
  'Число ребер в первом подграфе увеличивается
  varSGFilter = rsSubGraph.Filter
  rsSubGraph.Filter = RS_SUBGRAPH_ID & "=" & intGraph2ID
  
  'Граф мог быть уже удален, потому что список найденных подграфов не уникальный
  If rsSubGraph.RecordCount <> 0 Then
    
    'Число дорог и bbox "прибавляются" к основному подграфу
    Nroads = rsSubGraph(RS_SUBGRAPH_ROADCOUNT).Value
    lat1 = rsSubGraph(RS_SUBGRAPH_LAT1).Value
    lon1 = rsSubGraph(RS_SUBGRAPH_LON1).Value
    lat2 = rsSubGraph(RS_SUBGRAPH_LAT2).Value
    lon2 = rsSubGraph(RS_SUBGRAPH_LON2).Value
    blnHasExternalNodes = rsSubGraph(RS_SUBGRAPH_HAS_EXTNODES).Value
    
    'Запись удаляется. число дорог зануляется на всякий случай
    rsSubGraph(RS_SUBGRAPH_ROADCOUNT).Value = 0
    rsSubGraph.Delete adAffectCurrent
   
    rsSubGraph.Filter = adFilterNone
  
    rsSubGraph.Filter = RS_SUBGRAPH_ID & "=" & intGraph1ID
    rsSubGraph(RS_SUBGRAPH_ROADCOUNT).Value = rsSubGraph(RS_SUBGRAPH_ROADCOUNT).Value + Nroads
    
    'И еще не хило бы BBox расширить
    If lat1 < rsSubGraph(RS_SUBGRAPH_LAT1).Value Then rsSubGraph(RS_SUBGRAPH_LAT1).Value = lat1
    If lon1 < rsSubGraph(RS_SUBGRAPH_LON1).Value Then rsSubGraph(RS_SUBGRAPH_LON1).Value = lon1
    If lat2 > rsSubGraph(RS_SUBGRAPH_LAT2).Value Then rsSubGraph(RS_SUBGRAPH_LAT2).Value = lat2
    If lon2 > rsSubGraph(RS_SUBGRAPH_LON2).Value Then rsSubGraph(RS_SUBGRAPH_LON2).Value = lon2
    
    'Если в данном подграфе есть внешняя нода, значит  и в объединенном подграфе есть внешняя нода.
    If blnHasExternalNodes Then rsSubGraph(RS_SUBGRAPH_HAS_EXTNODES).Value = True
  
  End If
  
  rsSubGraph.Filter = varSGFilter
  'Всем вершинам приписывается первый подграф
  varNodeFilter = rsNodes.Filter
  rsNodes.Filter = RS_NODE_SUBGRAPH & "=" & intGraph2ID
  Do While Not rsNodes.EOF
    rsNodes(RS_NODE_SUBGRAPH).Value = intGraph1ID
    rsNodes.MoveNext
  Loop
  
  rsNodes.Filter = varNodeFilter
  
  'BBox'ы объединяются
End Sub

'Добавляем ноду в граф
Private Sub AddNodeToSubGraph(ByVal intGraphID As Long, ByVal intNodeID As Long)
Dim varNodeFilter
  varNodeFilter = rsNodes.Filter
  rsNodes.Filter = RS_NODE_ID & "=" & intNodeID
  If rsNodes.RecordCount = 0 Then
    rsNodes.AddNew
    rsNodes(RS_NODE_ID).Value = intNodeID
    rsNodes(RS_NODE_SUBGRAPH).Value = intGraphID
  Else
    If rsNodes(RS_NODE_SUBGRAPH).Value <> intGraphID Then
      Err.Raise vbObjectError, "AddNodeToSubGraph", "Internal Algorithm error: wrong sub-graph!"
    End If
  End If
  rsNodes.Filter = varNodeFilter

End Sub
'Проверка списка исключений
Public Function CheckExceptions()
Dim varFilter As Variant
  
  varFilter = rsSubGraph.Filter
  
  
  If rsExceptions.RecordCount > 0 Then
    rsExceptions.MoveFirst
    Do While Not rsExceptions.EOF
      ' Тут то как раз и нужно проверить, есть ли подграф, соответсвующий данному. Просто по координате.
      ' Дополнительно, нужно удостовериться, что это не главный (основной) подграф.
      rsSubGraph.Filter = RS_SUBGRAPH_LAT1 & "<" & rsExceptions(RS_EXCEP_LAT) & _
                          " And " & RS_SUBGRAPH_LAT2 & ">" & rsExceptions(RS_EXCEP_LAT) & _
                          " And " & RS_SUBGRAPH_LON1 & "<" & rsExceptions(RS_EXCEP_LON) & _
                          " And " & RS_SUBGRAPH_LON2 & ">" & rsExceptions(RS_EXCEP_LON) & _
                          " And " & RS_SUBGRAPH_HAS_EXTNODES & "=False"
      If rsSubGraph.RecordCount > 0 Then
        rsExceptions(RS_EXCEP_N).Value = rsExceptions(RS_EXCEP_N).Value + 1
        rsSubGraph(RS_SUBGRAPH_KNOWN_EXCEPTION).Value = True
      End If
       
      
      rsExceptions.MoveNext
    Loop
  End If
  
rsSubGraph.Filter = varFilter
End Function

'Cохранение списка ошибок в xml
Public Function PrintRegistryToXML(FileNumber As Long) As String
  
  'rsSubGraph.Filter = adFilterNone
  'Выводим только пографы без "внешних нод". Исключения тоже не выводим
  rsSubGraph.Filter = RS_SUBGRAPH_HAS_EXTNODES & "=False and " & RS_SUBGRAPH_KNOWN_EXCEPTION & "=False"

  
  rsSubGraph.Sort = RS_SUBGRAPH_ROADCOUNT & " desc"
  
  Print #FileNumber, "<Summary>"
  Print #FileNumber, "  <NumberOfSubgraphs>" & rsSubGraph.RecordCount & "</NumberOfSubgraphs>"
  Print #FileNumber, "  <NumberOfRoads>" & NTotalRoads & "</NumberOfRoads>"
  Print #FileNumber, "  <NumberOfRoutingEdges>" & NRoutiningEdges & "</NumberOfRoutingEdges>"
  
  Print #FileNumber, "</Summary>"
  
  Print #FileNumber, "<SubgraphList>"
  Do While Not rsSubGraph.EOF
    Print #FileNumber, "  <Subgraph>"
    Print #FileNumber, "    <NumberOfRoads>" & rsSubGraph(RS_SUBGRAPH_ROADCOUNT).Value & "</NumberOfRoads>"
    Print #FileNumber, "    <Bbox>"
    Print #FileNumber, "      <Lat1>" & rsSubGraph(RS_SUBGRAPH_LAT1).Value & "</Lat1>"
    Print #FileNumber, "      <Lon1>" & rsSubGraph(RS_SUBGRAPH_LON1).Value & "</Lon1>"
    Print #FileNumber, "      <Lat2>" & rsSubGraph(RS_SUBGRAPH_LAT2).Value & "</Lat2>"
    Print #FileNumber, "      <Lon2>" & rsSubGraph(RS_SUBGRAPH_LON2).Value & "</Lon2>"
    Print #FileNumber, "    </Bbox>"

    Print #FileNumber, "  </Subgraph>"

    rsSubGraph.MoveNext
  Loop
  Print #FileNumber, "</SubgraphList>"
  
  
  Print #FileNumber, "<ExceptionList>"
  If rsExceptions.RecordCount > 0 Then
    rsExceptions.MoveFirst
    Do While Not rsExceptions.EOF
      Print #FileNumber, "  <SubgraphExc>"
      Print #FileNumber, "    <MapId>" & rsExceptions(RS_EXCEP_MAPCODE).Value & "</MapId>"
      Print #FileNumber, "    <Name>" & rsExceptions(RS_EXCEP_NAME).Value & "</Name>"
      Print #FileNumber, "    <Descr>" & rsExceptions(RS_EXCEP_DESCR).Value & "</Descr>"
      Print #FileNumber, "    <Coord>"
      Print #FileNumber, "      <Lat>" & rsExceptions(RS_EXCEP_LAT).Value & "</Lat>"
      Print #FileNumber, "      <Lon>" & rsExceptions(RS_EXCEP_LON).Value & "</Lon>"
      Print #FileNumber, "    </Coord>"
      Print #FileNumber, "    <NumberOfFoundOccurences>" & rsExceptions(RS_EXCEP_N).Value & "</NumberOfFoundOccurences>"
      Print #FileNumber, "  </SubgraphExc>"
      rsExceptions.MoveNext
    Loop
  End If
  Print #FileNumber, "</ExceptionList>"
  
  
End Function


Private Function MakeXmlString(str As String) As String
  str = Replace(str, "&", "&amp;")
  MakeXmlString = str
End Function
Private Function StrForFilter(ByVal str As String) As String
   StrForFilter = Replace(str, "'", "''")
End Function
