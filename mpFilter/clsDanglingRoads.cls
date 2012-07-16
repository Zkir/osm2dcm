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
'“ест вис€чих вершин
'***************************************************************************
Option Explicit

'»де€ очень проста€.
'¬ажные дороги (trunk,primary,secondary), не могут просто обрыватьс€,
' а должны переходить в менее важные.


' онстанты полей в рекордсетах

'¬ис€чие вершины
Const RS_DANGLING_NODES_ID = "Id"
Const RS_DANGLING_NODES_LAT = "Lat"
Const RS_DANGLING_NODES_LON = "Lon"
Const RS_DANGLING_NODES_LEVEL = "Level"
Const RS_DANGLING_NODES_HWLINK = "hwlink"

'ѕолный список вершин.
Const RS_ALL_NODES_ID = "Id"
Const RS_ALL_NODES_COUNT = "Count"


Dim rsDanglingNodes As ADODB.Recordset
Dim rsAllNodes As ADODB.Recordset

Private Sub Class_Initialize()
 On Error GoTo finalize
  
  '¬ис€чие вершины
  Set rsDanglingNodes = New ADODB.Recordset
  rsDanglingNodes.Fields.Append RS_DANGLING_NODES_ID, adInteger
  rsDanglingNodes.Fields.Append RS_DANGLING_NODES_LAT, adDouble
  rsDanglingNodes.Fields.Append RS_DANGLING_NODES_LON, adDouble
  rsDanglingNodes.Fields.Append RS_DANGLING_NODES_LEVEL, adInteger
  rsDanglingNodes.Fields.Append RS_DANGLING_NODES_HWLINK, adInteger
  

  rsDanglingNodes.Open
  'rsDanglingNodes(RS_SUBGRAPH_ID).Properties("Optimize") = True
  
  '¬ершины
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

'ƒобавление концов дороги в список.
' NodeList() - список айдишников рутинговых вершин
' NodeExtrAttrList() - 0 дл€ обычной веришины и 1 дл€ внешней (котора€ соедин€етс€ с чем-то за пределами карты)
'
Public Sub AddRoad(strMpType As String, _
                   intRoadLevel As Integer, _
                   NodeList() As Long, _
                   NodeExtrAttrList() As Long, _
                   lat1 As Double, lon1 As Double, _
                   lat2 As Double, lon2 As Double)

Dim i As Integer
'ƒобавим вершины список вершин
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


'ƒобавим концевые вершины в список вис€чих вершин
If intRoadLevel <= 2 Then
  If NodeExtrAttrList(LBound(NodeList)) <> 1 Then
    '  роме внешних рутинговых вершин. ќни не €вл€ютс€ вис€чими, потому что с чем-то соедин€ютс€.
    rsDanglingNodes.AddNew
    rsDanglingNodes(RS_DANGLING_NODES_ID).Value = NodeList(LBound(NodeList))
    rsDanglingNodes(RS_DANGLING_NODES_LAT).Value = lat1
    rsDanglingNodes(RS_DANGLING_NODES_LON).Value = lon1
    rsDanglingNodes(RS_DANGLING_NODES_LEVEL).Value = intRoadLevel
    
     '€вл€етс€ ли дорога куском разв€зки
    rsDanglingNodes(RS_DANGLING_NODES_HWLINK).Value = IIf((strMpType = "0x08") Or (strMpType = "0x0B"), 1, 0)
  End If
  
  If NodeExtrAttrList(UBound(NodeList)) <> 1 Then
    rsDanglingNodes.AddNew
    rsDanglingNodes(RS_DANGLING_NODES_ID).Value = NodeList(UBound(NodeList))
    rsDanglingNodes(RS_DANGLING_NODES_LAT).Value = lat2
    rsDanglingNodes(RS_DANGLING_NODES_LON).Value = lon2
    rsDanglingNodes(RS_DANGLING_NODES_LEVEL).Value = intRoadLevel
    
    '€вл€етс€ ли дорога куском разв€зки
    rsDanglingNodes(RS_DANGLING_NODES_HWLINK).Value = IIf((strMpType = "0x08") Or (strMpType = "0x0B"), 1, 0)
  End If
End If

End Sub

Public Sub Validate()

rsDanglingNodes.Filter = adFilterNone
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
 'rsDanglingNodes.Filter = adFilterNone
 rsDanglingNodes.Filter = RS_DANGLING_NODES_HWLINK & "=" & 0
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
