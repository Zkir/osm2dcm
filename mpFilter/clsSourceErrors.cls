VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
  Persistable = 0  'NotPersistable
  DataBindingBehavior = 0  'vbNone
  DataSourceBehavior  = 0  'vbNone
  MTSTransactionMode  = 0  'NotAnMTSObject
END
Attribute VB_Name = "clsSourceErrors"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
'Обработка ошибок найденных osm2mp.pl
Option Explicit

Const RS_CL_BREAKS_LAT = "Lat"
Const RS_CL_BREAKS_LON = "Lon"

Const RS_RD_BREAKS_LAT = "Lat"
Const RS_RD_BREAKS_LON = "Lon"

'Рекордсеты с данными
Dim rsCoastLineBreaks     As ADODB.Recordset
Dim rsDuplicateRoads      As ADODB.Recordset


Private Sub Class_Initialize()

  
  'Разрывы береговой линии
  Set rsCoastLineBreaks = New ADODB.Recordset

  rsCoastLineBreaks.Fields.Append RS_CL_BREAKS_LAT, adDouble
  rsCoastLineBreaks.Fields.Append RS_CL_BREAKS_LON, adDouble
    
  rsCoastLineBreaks.Open
  
  'Дупликаты ребер.
  
  Set rsDuplicateRoads = New ADODB.Recordset

  rsDuplicateRoads.Fields.Append RS_RD_BREAKS_LAT, adDouble
  rsDuplicateRoads.Fields.Append RS_RD_BREAKS_LON, adDouble
    
  rsDuplicateRoads.Open
End Sub

Public Sub ProcessComment(ByVal strComment As String)
Dim lat1 As Double, lon1 As Double, lat2 As Double, lon2 As Double
Dim strError As String

'Нас интересуют:
'1-береговая линия.
'; ERROR: Possible coastline break at (59.870581,30.1579854) or (59.8707318,30.1609809)
If Left(strComment, 36) = "; ERROR: Possible coastline break at" Then
  'Debug.Print strComment
  If ParseCLCoords(strComment, lat1, lon1, lat2, lon2) Then
    rsCoastLineBreaks.AddNew
    rsCoastLineBreaks(RS_CL_BREAKS_LAT).Value = lat1
    rsCoastLineBreaks(RS_CL_BREAKS_LON).Value = lon1

    rsCoastLineBreaks.AddNew
    rsCoastLineBreaks(RS_CL_BREAKS_LAT).Value = lat2
    rsCoastLineBreaks(RS_CL_BREAKS_LON).Value = lon2
  Else
    Err.Raise vbObjectError, "ProcessComment", "Unable to parse coordinates"
  End If
End If
 
'2-Дупликаты рутинговых ребер.
'; ERROR: Roads 91630559:0, 91630559:0, 91630559:0 have 1 duplicate segments near (52.6322258,38.4829784)
If Left(strComment, 14) = "; ERROR: Roads" Then
  'Debug.Print strComment
  If ParseRDCoords(strComment, lat1, lon1) Then
    rsDuplicateRoads.AddNew
    rsDuplicateRoads(RS_RD_BREAKS_LAT).Value = lat1
    rsDuplicateRoads(RS_RD_BREAKS_LON).Value = lon1
  Else
    Err.Raise vbObjectError, "ProcessComment", "Unable to parse coordinates"
  End If
End If
 
End Sub
Private Function Ereg_Match(ByVal Expression As String, _
                            ByVal Mask As String, _
                            Optional blnIgnoreCase As Boolean = False) As Variant
  Dim colMatches As MatchCollection
  Static soRegExp As New VBScript_RegExp_55.RegExp
  soRegExp.IgnoreCase = blnIgnoreCase
  soRegExp.Pattern = Mask
  Set colMatches = soRegExp.Execute(Expression)
  
  Set Ereg_Match = colMatches.Item(0).SubMatches
  
End Function
Private Function ParseCLCoords(ByVal strComment As String, ByRef lat1 As Double, ByRef lon1 As Double, ByRef lat2 As Double, ByRef lon2 As Double) As Boolean
Dim Matches As Variant
 'ERROR: Possible coastline break at \(([0-9.]+),30.1579854\) or \(59.8707318,30.1609809\)"
 Set Matches = Ereg_Match(strComment, "Possible coastline break at \(([0-9.]+),([0-9.]+)\) or \(([0-9.]+),([0-9.]+)\)")
 If Matches.Count = 4 Then
   lat1 = Matches.Item(0)
   lon1 = Matches.Item(1)
   lat2 = Matches.Item(2)
   lon2 = Matches.Item(3)
   ParseCLCoords = True
 Else
   ParseCLCoords = False
 End If
End Function


Private Function ParseRDCoords(ByVal strComment As String, _
                               ByRef lat1 As Double, ByRef lon1 As Double) As Boolean
Dim Matches As Variant
 ''; ERROR: Roads 91630559:0, 91630559:0, 91630559:0 have 1 duplicate segments near (52.6322258,38.4829784)
 Set Matches = Ereg_Match(strComment, "ERROR: Roads .+ duplicate segments near \(([0-9.]+),([0-9.]+)\)")
 If Matches.Count = 2 Then
   lat1 = Matches.Item(0)
   lon1 = Matches.Item(1)
   ParseRDCoords = True
 Else
   ParseRDCoords = False
 End If
End Function


Public Sub PrintErrorsToXML(FileNumber As Integer)
 rsCoastLineBreaks.Filter = adFilterNone
  Print #FileNumber, "<CoastLineTest>"
  Print #FileNumber, "<Summary>"
  Print #FileNumber, "  <NumberOfBreaks>" & rsCoastLineBreaks.RecordCount & "</NumberOfBreaks>"
  Print #FileNumber, "</Summary>"
  
  Print #FileNumber, "<BreakList>"
  Do While Not rsCoastLineBreaks.EOF
    Print #FileNumber, "  <BreakPoint>"
    Print #FileNumber, "    <Coord>"
    Print #FileNumber, "      <Lat>" & rsCoastLineBreaks(RS_CL_BREAKS_LAT).Value & "</Lat>"
    Print #FileNumber, "      <Lon>" & rsCoastLineBreaks(RS_CL_BREAKS_LON).Value & "</Lon>"
    Print #FileNumber, "    </Coord>"
    Print #FileNumber, "  </BreakPoint>"
    rsCoastLineBreaks.MoveNext
  Loop
  Print #FileNumber, "</BreakList>"
  Print #FileNumber, "</CoastLineTest>"
  
'2-Дупликаты рутинговых ребер.
 rsDuplicateRoads.Filter = adFilterNone
  Print #FileNumber, "<RoadDuplicatesTest>"
  Print #FileNumber, "<Summary>"
  Print #FileNumber, "  <NumberOfDuplicates>" & rsDuplicateRoads.RecordCount & "</NumberOfDuplicates>"
  Print #FileNumber, "</Summary>"
  
  Print #FileNumber, "<DuplicateList>"
  Do While Not rsDuplicateRoads.EOF
    Print #FileNumber, "  <DuplicatePoint>"
    Print #FileNumber, "    <Coord>"
    Print #FileNumber, "      <Lat>" & rsDuplicateRoads(RS_RD_BREAKS_LAT).Value & "</Lat>"
    Print #FileNumber, "      <Lon>" & rsDuplicateRoads(RS_RD_BREAKS_LON).Value & "</Lon>"
    Print #FileNumber, "    </Coord>"
    Print #FileNumber, "  </DuplicatePoint>"
    rsDuplicateRoads.MoveNext
  Loop
  Print #FileNumber, "</DuplicateList>"
  Print #FileNumber, "</RoadDuplicatesTest>"

End Sub

