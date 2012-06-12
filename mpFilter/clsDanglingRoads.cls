VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
  Persistable = 0  'NotPersistable
  DataBindingBehavior = 0  'vbNone
  DataSourceBehavior  = 0  'vbNone
  MTSTransactionMode  = 0  'NotAnMTSObject
END
Attribute VB_Name = "clsDanglingRoads"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
'***************************************************************************
'Тест висячих вершин
'***************************************************************************
Option Explicit

'Идея очень простая.
'Важные дороги (trunk,primary,secondary), не могут просто обрываться,
' а должны переходить в менее важные.


'Константы полей в рекордсетах

'Висячие вершины
Const RS_DANGLING_NODES_ID = "Id"
Const RS_DANGLING_NODES_LAT = "Lat"
Const RS_DANGLING_NODES_LON = "Lon"
Const RS_DANGLING_NODES_LEVEL = "Level"

'Полный список вершин.
Const RS_ALL_NODES_ID = "Id"
Const RS_ALL_NODES_COUNT = "Count"


Dim rsDanglingNodes As ADODB.Recordset
Dim rsAllNodes As ADODB.Recordset

Private Sub Class_Initialize()
 On Error GoTo finalize
  
  'Висячие вершины
  Set rsDanglingNodes = New ADODB.Recordset
  rsDanglingNodes.Fields.Append RS_DANGLING_NODES_ID, adInteger
  rsDanglingNodes.Fields.Append RS_DANGLING_NODES_LAT, adDouble
  rsDanglingNodes.Fields.Append RS_DANGLING_NODES_LON, adDouble
  rsDanglingNodes.Fields.Append RS_DANGLING_NODES_LEVEL, adInteger

  rsDanglingNodes.Open
  'rsDanglingNodes(RS_SUBGRAPH_ID).Properties("Optimize") = True
  
  'Вершины
  Set rsAllNodes = New ADODB.Recordset
  rsAllNodes.Fields.Append RS_ALL_NODES_ID, adInteger
  rsAllNodes.Fields.Append RS_ALL_NODES_COUNT, adInteger
  rsAllNodes.Open
  rsAllNodes(RS_ALL_NODES_ID).Properties("Optimize") = True

     
finalize:
  If Err.Number <> 0 Then
    Err.Raise Err.Number, "Class_Initialize", Err.Description & " clsAddrRegistry.Initialize:" & Erl
  End If
End Sub


Public Sub AddRoad(intRoadLevel As Integer, _
                   NodeList() As Long, _
                   NodeList2() As Long, _
                   lat1 As Double, lon1 As Double, _
                   lat2 As Double, lon2 As Double)

Dim i As Integer
'Добавим вершины список вершин
  For i = LBound(NodeList) To UBound(NodeList)
    rsAllNodes.Filter = RS_ALL_NODES_ID & "=" & NodeList(i)
    If rsAllNodes.RecordCount > 0 Then
      rsAllNodes(RS_ALL_NODES_COUNT).Value = rsAllNodes(RS_ALL_NODES_COUNT).Value + 1
    Else
      rsAllNodes.AddNew
      rsAllNodes(RS_ALL_NODES_ID).Value = NodeList(i)
      rsAllNodes(RS_ALL_NODES_COUNT).Value = 1
      
    End If
    
  Next i


'Добавим концевые вершины в список висячих вершин
If intRoadLevel <= 2 Then
  If NodeList2(LBound(NodeList)) <> 1 Then
    rsDanglingNodes.AddNew
    rsDanglingNodes(RS_DANGLING_NODES_ID).Value = NodeList(LBound(NodeList))
    rsDanglingNodes(RS_DANGLING_NODES_LAT).Value = lat1
    rsDanglingNodes(RS_DANGLING_NODES_LON).Value = lon1
    rsDanglingNodes(RS_DANGLING_NODES_LEVEL).Value = intRoadLevel
  End If
  
  If NodeList2(UBound(NodeList)) <> 1 Then
    rsDanglingNodes.AddNew
    rsDanglingNodes(RS_DANGLING_NODES_ID).Value = NodeList(UBound(NodeList))
    rsDanglingNodes(RS_DANGLING_NODES_LAT).Value = lat2
    rsDanglingNodes(RS_DANGLING_NODES_LON).Value = lon2
    rsDanglingNodes(RS_DANGLING_NODES_LEVEL).Value = intRoadLevel
  End If
End If

End Sub

Public Sub Validate()
If rsDanglingNodes.RecordCount > 0 Then
  rsDanglingNodes.MoveFirst
  Do While Not rsDanglingNodes.EOF
    rsAllNodes.Filter = RS_ALL_NODES_ID & " = " & rsDanglingNodes(RS_DANGLING_NODES_ID).Value
    If rsAllNodes(RS_ALL_NODES_COUNT).Value > 1 Then
      rsDanglingNodes.Delete
    End If
    rsDanglingNodes.MoveNext
  Loop
  End If

End Sub


Public Sub PrintErrorsToXML(FileNumber As Integer)
 rsDanglingNodes.Filter = adFilterNone
 
 Print #FileNumber, "<DeadEndsTest>"
 Print #FileNumber, "<Summary>"
 Print #FileNumber, "  <NumberOfDeadEnds>" & rsDanglingNodes.RecordCount & "</NumberOfDeadEnds>"
 Print #FileNumber, "</Summary>"
 Print #FileNumber, "  <DeadEndList>"
   Do While Not rsDanglingNodes.EOF
    Print #FileNumber, "  <DeadEnd>"
    Print #FileNumber, "    <Coord>"
    Print #FileNumber, "      <Lat>" & rsDanglingNodes(RS_DANGLING_NODES_LAT).Value & "</Lat>"
    Print #FileNumber, "      <Lon>" & rsDanglingNodes(RS_DANGLING_NODES_LON).Value & "</Lon>"
    Print #FileNumber, "    </Coord>"
    Print #FileNumber, "  </DeadEnd>"
    rsDanglingNodes.MoveNext
  Loop
  Print #FileNumber, "  </DeadEndList>"
  Print #FileNumber, "</DeadEndsTest>"
  

End Sub
