Attribute VB_Name = "mdlDBI"
Option Explicit

'Поля в рекордсете
Public Const RS_STAT_MAPID = "MapID"
Public Const RS_STAT_MAPNAME = "MapName"
Public Const RS_STAT_DATE = "StatDate"
Public Const RS_STAT_NOBJECTS = "Nobjects"
Public Const RS_STAT_M14 = "M14"
Public Const RS_STAT_M100 = "M100"
Public Const RS_STAT_M365 = "M365"
Public Const RS_STAT_EDITSPERDAY = "EditsPerDay"
Public Const RS_STAT_AVGAGE = "AvAge"
Public Const RS_STAT_SQUARE = "Square"
Public Const RS_STAT_NUSERS = "NUsers"
Public Const RS_STAT_LASTKNOWNEDITDATE = "LastKnownEditDate"

Public Function MakeStatsRS()
Dim rs As Recordset

  Set rs = New Recordset
  rs.Fields.Append RS_STAT_MAPID, adVarWChar, 255
  rs.Fields.Append RS_STAT_DATE, adDate
  rs.Fields.Append RS_STAT_MAPNAME, adVarWChar, 255
  rs.Fields.Append RS_STAT_NOBJECTS, adInteger
  rs.Fields.Append RS_STAT_M14, adDouble
  rs.Fields.Append RS_STAT_M100, adDouble
  rs.Fields.Append RS_STAT_M365, adDouble
  rs.Fields.Append RS_STAT_EDITSPERDAY, adDouble
  rs.Fields.Append RS_STAT_AVGAGE, adDouble
  rs.Fields.Append RS_STAT_SQUARE, adDouble
  rs.Fields.Append RS_STAT_NUSERS, adInteger
  rs.Fields.Append RS_STAT_LASTKNOWNEDITDATE, adDate
 
  rs.Open

  Set MakeStatsRS = rs
End Function
Public Sub LoadStatRS(ByRef rsStat As Recordset)
Dim textline As String
Dim A() As String
Dim i As Integer

  Open App.Path & "\" & "statistics.dat" For Input As #1

  Set rsStat = MakeStatsRS

  Do While Not EOF(1)
    Line Input #1, textline
    A = Split(textline, "|")
    rsStat.AddNew
    For i = 0 To UBound(A)
      If Trim(A(i)) <> "" Then
        rsStat(i).Value = A(i)
      End If
    Next i
    rsStat.Update
  Loop
  Close #1

End Sub

Public Sub SaveStatRs(rsStat As Recordset)

  Open App.Path & "\" & "statistics.dat" For Output As #1
  
  rsStat.MoveFirst
  Do While Not rsStat.EOF
    Print #1, _
      rsStat(RS_STAT_MAPID).Value & "|" & _
      rsStat(RS_STAT_DATE).Value & "|" & _
      rsStat(RS_STAT_MAPNAME).Value & "|" & _
      rsStat(RS_STAT_NOBJECTS).Value & "|" & _
      rsStat(RS_STAT_M14).Value & "|" & _
      rsStat(RS_STAT_M100).Value & "|" & _
      rsStat(RS_STAT_M365).Value & "|" & _
      rsStat(RS_STAT_EDITSPERDAY).Value & "|" & _
      rsStat(RS_STAT_AVGAGE).Value & "|" & _
      rsStat(RS_STAT_SQUARE).Value & "|" & _
      rsStat(RS_STAT_NUSERS).Value & "|" & _
      rsStat(RS_STAT_LASTKNOWNEDITDATE).Value
    rsStat.MoveNext
  
  Loop

  Close #1
End Sub



