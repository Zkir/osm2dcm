VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
  Persistable = 0  'NotPersistable
  DataBindingBehavior = 0  'vbNone
  DataSourceBehavior  = 0  'vbNone
  MTSTransactionMode  = 0  'NotAnMTSObject
END
Attribute VB_Name = "clsStatistic"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
Option Explicit

'Cтатистика по секциям польского файла

Const RS_STAT_KIND = "Kind"
Const RS_STAT_TYPE = "Type"
Const RS_STAT_COUNT = "Count"
Const RS_STAT_TOTAL_SIZE = "Size"

'Рекордсеты с данными
Dim rsMPStat     As ADODB.Recordset


Private Sub Class_Initialize()
  Set rsMPStat = New ADODB.Recordset

  rsMPStat.Fields.Append RS_STAT_KIND, adVarWChar, 20
  rsMPStat.Fields.Append RS_STAT_TYPE, adVarWChar, 10
  rsMPStat.Fields.Append RS_STAT_COUNT, adInteger
  rsMPStat.Fields.Append RS_STAT_TOTAL_SIZE, adInteger
    
  rsMPStat.Open
  
 
End Sub

Public Sub ProcessSection(strKind As String, strType As String, intSize As Long)
  rsMPStat.Filter = RS_STAT_KIND & "='" & strKind & "' and " & RS_STAT_TYPE & "='" & strType & "'"
  If rsMPStat.RecordCount = 0 Then
    rsMPStat.AddNew
    rsMPStat(RS_STAT_KIND).Value = strKind
    rsMPStat(RS_STAT_TYPE).Value = strType
    rsMPStat(RS_STAT_COUNT).Value = 0
    rsMPStat(RS_STAT_TOTAL_SIZE).Value = 0
  End If
  rsMPStat(RS_STAT_COUNT).Value = rsMPStat(RS_STAT_COUNT).Value + 1
  rsMPStat(RS_STAT_TOTAL_SIZE).Value = rsMPStat(RS_STAT_TOTAL_SIZE).Value + intSize
 
End Sub


Public Sub PrintErrorsToXML(FileNumber As Integer)
 rsMPStat.Filter = adFilterNone
 rsMPStat.Sort = RS_STAT_KIND & " asc ," & RS_STAT_TYPE & " asc"
  Print #FileNumber, "<Statistics>"
  
  Do While Not rsMPStat.EOF
    Print #FileNumber, "  <Type>"
    Print #FileNumber, "    <Kind>" & rsMPStat(RS_STAT_KIND).Value & "</Kind>"
    Print #FileNumber, "    <Type>" & rsMPStat(RS_STAT_TYPE).Value & "</Type>"
    Print #FileNumber, "    <Count>" & rsMPStat(RS_STAT_COUNT).Value & "</Count>"
    Print #FileNumber, "    <Size>" & rsMPStat(RS_STAT_TOTAL_SIZE).Value & "</Size>"
    Print #FileNumber, "  </Type>"
    rsMPStat.MoveNext
  Loop

  Print #FileNumber, "</Statistics>"
  

End Sub



