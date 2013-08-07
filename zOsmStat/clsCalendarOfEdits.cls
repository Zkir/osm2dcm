VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
  Persistable = 0  'NotPersistable
  DataBindingBehavior = 0  'vbNone
  DataSourceBehavior  = 0  'vbNone
  MTSTransactionMode  = 0  'NotAnMTSObject
END
Attribute VB_Name = "clsCalendarOfEdits"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
'******************************************************************************
'Календарь правок
'******************************************************************************
Option Explicit
Dim rsUsers As ADODB.Recordset

  Const RS_USER_ID = "user_id"
  Const RS_USER_NAME = "user_name"
  Const RS_USER_P01 = "p1"
  Const RS_USER_P02 = "p2"
  Const RS_USER_P03 = "p3"
  Const RS_USER_P04 = "p4"
  Const RS_USER_P05 = "p5"
  Const RS_USER_P06 = "p6"
  Const RS_USER_P07 = "p7"
  Const RS_USER_P08 = "p8"
  Const RS_USER_P09 = "p9"
  Const RS_USER_P10 = "p10"
  Const RS_USER_P11 = "p11"
  Const RS_USER_P12 = "p12"
  Const RS_USER_P13 = "p13"
  Const RS_USER_EDIT_COUNT = "user_edit_count"
  
Dim dtPeriods(13, 1) As Date
  
  
Private Sub Class_Initialize()
 
  Set rsUsers = New ADODB.Recordset

  
  rsUsers.Fields.Append RS_USER_ID, adVarWChar, 10
  rsUsers.Fields.Append RS_USER_NAME, adVarWChar, 250
  rsUsers.Fields.Append RS_USER_P01, adInteger
  rsUsers.Fields.Append RS_USER_P02, adInteger
  rsUsers.Fields.Append RS_USER_P03, adInteger
  rsUsers.Fields.Append RS_USER_P04, adInteger
  rsUsers.Fields.Append RS_USER_P05, adInteger
  rsUsers.Fields.Append RS_USER_P06, adInteger
  rsUsers.Fields.Append RS_USER_P07, adInteger
  rsUsers.Fields.Append RS_USER_P08, adInteger
  rsUsers.Fields.Append RS_USER_P09, adInteger
  rsUsers.Fields.Append RS_USER_P10, adInteger
  rsUsers.Fields.Append RS_USER_P11, adInteger
  rsUsers.Fields.Append RS_USER_P12, adInteger
  rsUsers.Fields.Append RS_USER_P13, adInteger
  rsUsers.Fields.Append RS_USER_EDIT_COUNT, adInteger
  
  
  rsUsers.Open
  rsUsers(RS_USER_ID).Properties("Optimize") = True
  
  'Определимся с периодами. Периоды это 12 месяцев скользящего года, плюс незавершенный месяц.
  
  Dim dtCurrentDate As Date
  Dim i As Integer
  dtCurrentDate = Now
  
  'Найдем первый день текущего месяца
  
  dtPeriods(13, 0) = DateSerial(Year(dtCurrentDate), Month(dtCurrentDate), 1)
  dtPeriods(13, 1) = DateAdd("M", 1, dtPeriods(13, 0)) - 1
  For i = 12 To 1 Step -1
    dtPeriods(i, 0) = DateAdd("M", -1, dtPeriods(i + 1, 0))
    dtPeriods(i, 1) = DateAdd("D", -1, dtPeriods(i + 1, 0))
  Next i
  
End Sub
  
  
Public Sub AddUserRS(dtEditDate As Date, strUserID As String, strUserName As String)
Dim i As Integer
  dtEditDate = DateSerial(Year(dtEditDate), Month(dtEditDate), Day(dtEditDate))

  If (dtEditDate >= dtPeriods(1, 0)) And (dtEditDate <= dtPeriods(13, 1)) Then
    rsUsers.Filter = RS_USER_ID & " ='" & strUserID & "'"
    If rsUsers.RecordCount = 0 Then
      rsUsers.AddNew
      rsUsers(RS_USER_ID).Value = strUserID
      rsUsers(RS_USER_NAME).Value = strUserName
      rsUsers(RS_USER_EDIT_COUNT).Value = 0
      rsUsers(RS_USER_P01).Value = 0
      rsUsers(RS_USER_P02).Value = 0
      rsUsers(RS_USER_P03).Value = 0
      rsUsers(RS_USER_P04).Value = 0
      rsUsers(RS_USER_P05).Value = 0
      rsUsers(RS_USER_P06).Value = 0
      rsUsers(RS_USER_P07).Value = 0
      rsUsers(RS_USER_P08).Value = 0
      rsUsers(RS_USER_P09).Value = 0
      rsUsers(RS_USER_P10).Value = 0
      rsUsers(RS_USER_P11).Value = 0
      rsUsers(RS_USER_P12).Value = 0
      rsUsers(RS_USER_P13).Value = 0
    End If
    rsUsers(RS_USER_EDIT_COUNT).Value = rsUsers(RS_USER_EDIT_COUNT).Value + 1
    Dim blnFlag As Boolean
    blnFlag = False
    For i = 1 To 13
      If (dtEditDate >= dtPeriods(i, 0)) And (dtEditDate <= dtPeriods(i, 1)) Then
        rsUsers("p" & i).Value = rsUsers("p" & i).Value + 1
        blnFlag = True
      End If
    Next i
    If Not blnFlag Then
      Debug.Print dtEditDate
    End If
  End If
End Sub
Private Function FormatPeriod(dtDate As Date) As String
  FormatPeriod = Year(dtDate) & "-" & Month(dtDate)
End Function


Public Sub save_local_stat(strFileName As String, rsStat As ADODB.Recordset)
  'Open App.Path & "\" & "statistics.xml" For Output As #1
  Dim i As Integer
  Dim CurrDate As Date
  Open strFileName For Output As #9
  CurrDate = Now()
  Print #9, "<?xml version=""1.0"" encoding=""utf-8""?>"
  Print #9, "<stat_users date=""" & FormatXMLDate(CurrDate) & """>"
  
  'Шапка
      Print #9, "<mapinfo>"
     
      'Карта
      Print #9, "  <MapId>" & rsStat(RS_STAT_MAPID).Value & "</MapId>"
      'Дата
      Print #9, "  <MapDate>" & FormatXMLDate(rsStat(RS_STAT_DATE).Value) & "</MapDate>"
      'Название
      ''оно в ср-м vb не юникодное! -'Print #9, "  <MapName>" & rsStat(RS_STAT_MAPNAME).Value & "</MapName>"
      'Площадь, кв. км
      Print #9, "  <Square>" & Format(rsStat(RS_STAT_SQUARE).Value, "#0") & "</Square>"
      'Число объектов
      Print #9, "  <NumberOfObjects>" & rsStat(RS_STAT_NOBJECTS).Value & "</NumberOfObjects>"
      'Правок в день
      Print #9, "  <EditsPerDay>" & Format(rsStat(RS_STAT_EDITSPERDAY).Value, "#0.0") & "</EditsPerDay>"
      '14 дней
      Print #9, "  <M14>" & Format(rsStat(RS_STAT_M14).Value * 100, "#0.0") & "</M14>"
      '100 дней
      Print #9, "  <M100>" & Format(rsStat(RS_STAT_M100).Value * 100, "#0") & "</M100>"
      '365 дней
      Print #9, "  <M365>" & Format(rsStat(RS_STAT_M365).Value * 100, "#0") & "</M365>"
      'Ср. возраст
      Print #9, "  <AverageObjectAge>" & Format(rsStat(RS_STAT_AVGAGE).Value, "#0") & "</AverageObjectAge>"
      'Число объектов на кв. км
      Print #9, "  <ObjectsPerSquareKm>" & rsStat(RS_STAT_NOBJECTS).Value / rsStat(RS_STAT_SQUARE).Value & "</ObjectsPerSquareKm>"
      'Правок в день на кв. км
      Print #9, "  <EditsPerDayPerSquareKm>" & rsStat(RS_STAT_EDITSPERDAY).Value / rsStat(RS_STAT_SQUARE).Value & "</EditsPerDayPerSquareKm>"
      'Активные участники
      Print #9, "  <ActiveUsers>" & rsStat(RS_STAT_NUSERS).Value & "</ActiveUsers>"
      'Последняя известная правка
      Print #9, "  <LastKnownEdit>" & FormatXMLDateTime(rsStat(RS_STAT_LASTKNOWNEDITDATE).Value) & "</LastKnownEdit>"
    Print #9, "</mapinfo>"
  
  'Редакторы карты за последние 13 месяцев
  Print #9, "<periods>"
  For i = 1 To 13
    Print #9, "  <period_" & i & ">" & FormatPeriod(dtPeriods(i, 0)) & "</period_" & i & ">"
  Next i
  Print #9, "</periods>"
   rsUsers.Filter = adFilterNone
   rsUsers.Sort = RS_USER_EDIT_COUNT & " desc"
   
   Print #9, "<user_count>" & rsUsers.RecordCount & "</user_count>"
   Do While Not rsUsers.EOF
     Print #9, "<user>"
     Print #9, _
      "  <id>" & rsUsers(RS_USER_ID).Value & "</id>" & _
      "<name>" & rsUsers(RS_USER_NAME).Value & "</name>" & _
      "<object_count>" & rsUsers(RS_USER_EDIT_COUNT).Value & "</object_count>"
      For i = 1 To 13
       Print #9, "  <period_" & i & ">" & rsUsers("p" & i).Value & "</period_" & i & ">"
      Next i
     Print #9, "</user>"
     rsUsers.MoveNext
  
  Loop
  Print #9, "</stat_users>"
  Close #9


End Sub

