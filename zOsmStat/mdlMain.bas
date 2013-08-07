Attribute VB_Name = "mdlMain"
Option Explicit
Private Declare Sub ExitProcess Lib "kernel32" (ByVal uExitCode As Long)
Dim N  As Long
Dim M14  As Long
Dim M100  As Long
Dim M365  As Long
Dim AvgAge As Variant
Dim NUsers As Long
Dim dtLastEditDate As Date


Public Const NULL_DATE = "1900-1-1"



'pathes
Public Const PATH_TO_POLY = "D:\OSM\osm2dcm\poly\"
Public Const PATH_TO_LOG = "D:\osm\osm_data\"
Public Const PATH_TO_OSM = "D:\osm\osm_data\_my\"

'Const PATH_TO_POLY = "O:\osm2dcm\poly\"
'Const PATH_TO_OSM = "O:\osm2dcm\_my\"


Public Function ConvertFromXMLDate(ByVal strDate As String) As Date
  Dim dtTime As Date
  Dim dtDate As Date
  If Right$(strDate, 1) <> "Z" Then
    Err.Raise vbObjectError, "", "Z letter is missing"
  End If
  strDate = Left$(strDate, Len(strDate) - 1)
  dtDate = DateSerial(Mid$(strDate, 1, 4), Mid$(strDate, 6, 2), Mid$(strDate, 9, 2))
  dtTime = TimeSerial(Mid$(strDate, 12, 2), Mid$(strDate, 15, 2), Mid$(strDate, 18, 2))
  ConvertFromXMLDate = dtDate + dtTime
End Function

Public Function FormatXMLDate(CurrDate As Date) As String
 FormatXMLDate = Year(CurrDate) & "-" & Format(Month(CurrDate), "00") & "-" & Format(Day(CurrDate), "00")
End Function
Public Function FormatXMLDateTime(CurrDate As Date) As String
 FormatXMLDateTime = Year(CurrDate) & "-" & Format(Month(CurrDate), "00") & "-" & Format(Day(CurrDate), "00") & " " & Format(Hour(CurrDate), "00") & ":" & Format(Minute(CurrDate), "00") & ":" & Format(Second(CurrDate), "00")
End Function

Private Function AddUser(colUsers As Collection, strUser As String) As Boolean
On Error GoTo catch
colUsers.Add strUser, strUser
AddUser = True
catch:
 If Err.Number <> 0 Then
   If Err.Number = 457 Then
     'this user exists already in collection
     AddUser = False
   Else
     Err.Raise vbObjectError, "", Err.Description
   End If
   
 End If
End Function

Public Function ProcessOSMFile(strFileName As String, dtCurrentDate As Date, strMapID As String, objCalendarOfEdits As clsCalendarOfEdits) As Boolean
  Dim textline As String
  Dim dtDate As Date
  Dim clsXMLNode As zXMLNode
  Dim colUsers As Collection
  Dim strUserName As String
 
  
On Error GoTo finalize

   
   

  Open strFileName For Input As #1
  N = 0
  M14 = 0
  M100 = 0
  M365 = 0
  AvgAge = CDec(0)
  dtLastEditDate = NULL_DATE
  
  Set colUsers = New Collection
  Do While EOF(1) <> True
    Line Input #1, textline
    textline = Trim$(textline)
    If Left$(textline, 1) <> "<" Or Right$(textline, 1) <> ">" Then
      Err.Raise vbObjectError, "", "angle brakets are missing"
    End If
    
    Set clsXMLNode = New zXMLNode
    clsXMLNode.ParseNode (textline)
    dtDate = clsXMLNode.dtTimeStamp
    
    If dtDate > dtLastEditDate Then
     dtLastEditDate = dtDate
    End If
    
    If dtDate <> NULL_DATE Then
      objCalendarOfEdits.AddUserRS dtDate, clsXMLNode.GetAttributeValue("uid"), clsXMLNode.GetAttributeValue("user")
    
    
      N = N + 1
      AvgAge = AvgAge + Int(dtCurrentDate - dtDate)
      If dtDate > dtCurrentDate - 14 Then
        M14 = M14 + 1
        'Добавим пользователя в коллекцию
         
        If AddUser(colUsers, clsXMLNode.GetAttributeValue("uid")) Then
         'Debug.Print clsXMLNode.GetAttributeValue("user")
        End If
        
      End If
      If dtDate > dtCurrentDate - 100 Then
        M100 = M100 + 1
      End If
      If dtDate > dtCurrentDate - 365 Then
        M365 = M365 + 1
      End If
    End If
    Set clsXMLNode = Nothing
  Loop
  NUsers = colUsers.Count
  Set colUsers = Nothing

ProcessOSMFile = True
finalize:
 Close #1
 If Err.Number <> 0 Then
   If Err.Number = 76 Then
     'file not found
     ProcessOSMFile = False
   Else
     Err.Raise vbObjectError, "", Err.Description
   End If
 End If
End Function
Private Sub UpdateStatToFile(strFileName As String, strMapID As String, dtDate As Date)
  Open strFileName For Append As #1
  Print #1, strMapID & "|" & dtDate & "|" & N & "|" & M14 / N & "|" & M100 / N & "|" & M365 / N & "|" & M14 / 14 & "|" & AvgAge / N & "|" & NUsers
  Close #1
End Sub
'Субьективный рейтинг по пятибальной шкале
Public Function Rating(X As Double, Avg As Double, Max As Double) As Integer
If X > Avg Then
  Rating = 5 + (X - Avg) / (Max - Avg) * 5
Else
  Rating = 1 + X / Avg * 4
End If

End Function


Private Sub ProcessMap(rsStat As Recordset, strMapID As String, strMapName As String, strFileName As String, objCalendarOfEdits As clsCalendarOfEdits)
Dim i         As Integer
Dim s         As Double
Dim AvgLat    As Double
    
    'Найдем площадь по соответствующему  poly-файлу
    s = CalculateSquare(PATH_TO_POLY & strMapID & ".poly", AvgLat)
    s = s * (111 ^ 2) * Cos(AvgLat / 180 * 3.14159)
    If ProcessOSMFile(strFileName, Date, strMapID, objCalendarOfEdits) Then
    
      rsStat.Find RS_STAT_MAPID & "= '" & strMapID & "'", , , adBookmarkFirst
      
      If rsStat.EOF Then
        rsStat.AddNew
      End If
      
      rsStat(RS_STAT_MAPID).Value = strMapID
      rsStat(RS_STAT_DATE).Value = Date
      rsStat(RS_STAT_MAPNAME).Value = strMapName
            
      'Число объектов
      rsStat(RS_STAT_NOBJECTS).Value = N
      
      'Доля объектов моложе 14 дней
      rsStat(RS_STAT_M14).Value = M14 / N
      rsStat(RS_STAT_M100).Value = M100 / N
      rsStat(RS_STAT_M365).Value = M365 / N
      rsStat(RS_STAT_EDITSPERDAY).Value = M14 / 14
      
      'Средний возраст
      rsStat(RS_STAT_AVGAGE).Value = AvgAge / N
      
      'Площадь
      rsStat(RS_STAT_SQUARE).Value = s
      
      'Число активных пользователей
      rsStat(RS_STAT_NUSERS).Value = NUsers
      
      'Дата последней известной правки
      rsStat(RS_STAT_LASTKNOWNEDITDATE).Value = dtLastEditDate
      
      
    End If

End Sub
Private Sub SaveStatisticsToXml(rsStat As Recordset, strXmlFileName As String)

  Open strXmlFileName For Output As #1
  
  Print #1, "<?xml version=""1.0"" encoding=""windows-1251""?>"
  Print #1, "<mapstatistics>"
  rsStat.MoveFirst
  Do While Not rsStat.EOF
    Print #1, "<mapinfo>"
     
      'Карта
      Print #1, "  <MapId>" & rsStat(RS_STAT_MAPID).Value & "</MapId>"
      'Дата
      Print #1, "  <MapDate>" & rsStat(RS_STAT_DATE).Value & "</MapDate>"
      'Название
      Print #1, "  <MapName>" & rsStat(RS_STAT_MAPNAME).Value & "</MapName>"
      'Площадь, кв. км
      Print #1, "  <Square>" & Format(rsStat(RS_STAT_SQUARE).Value, "#0") & "</Square>"
      'Число объектов
      Print #1, "  <NumberOfObjects>" & rsStat(RS_STAT_NOBJECTS).Value & "</NumberOfObjects>"
      'Правок в день
      Print #1, "  <EditsPerDay>" & Format(rsStat(RS_STAT_EDITSPERDAY).Value, "#0.0") & "</EditsPerDay>"
      '14 дней
      Print #1, "  <M14>" & Format(rsStat(RS_STAT_M14).Value * 100, "#0.0") & "</M14>"
      '100 дней
      Print #1, "  <M100>" & Format(rsStat(RS_STAT_M100).Value * 100, "#0") & "</M100>"
      '365 дней
      Print #1, "  <M365>" & Format(rsStat(RS_STAT_M365).Value * 100, "#0") & "</M365>"
      'Ср. возраст
      Print #1, "  <AverageObjectAge>" & Format(rsStat(RS_STAT_AVGAGE).Value, "#0") & "</AverageObjectAge>"
      'Число объектов на кв. км
      Print #1, "  <ObjectsPerSquareKm>" & rsStat(RS_STAT_NOBJECTS).Value / rsStat(RS_STAT_SQUARE).Value & "</ObjectsPerSquareKm>"
      'Правок в день на кв. км
      Print #1, "  <EditsPerDayPerSquareKm>" & rsStat(RS_STAT_EDITSPERDAY).Value / rsStat(RS_STAT_SQUARE).Value & "</EditsPerDayPerSquareKm>"
      'Активные участники
      Print #1, "  <ActiveUsers>" & rsStat(RS_STAT_NUSERS).Value & "</ActiveUsers>"
      'Последняя известная правка
      Print #1, "  <LastKnownEdit>" & rsStat(RS_STAT_LASTKNOWNEDITDATE).Value & "</LastKnownEdit>"
    Print #1, "</mapinfo>"
    rsStat.MoveNext
  
  Loop
  Print #1, "</mapstatistics>"
  
  Close #1

End Sub



'Обработаем осм-файлы и вычислим статистику
Public Sub Main1(strMapID As String, strMapName As String)
 Dim rsStat As Recordset
 Dim objCalendarOfEdits As clsCalendarOfEdits
   
 LoadStatRS rsStat
 Set objCalendarOfEdits = New clsCalendarOfEdits

 'ProcessMap rsStat, strMapID, strMapName, "d:\osm\osm2dcm\_my\" & strMapID & "\final.full.osm"
 ProcessMap rsStat, strMapID, strMapName, PATH_TO_OSM & strMapID & "\final.osm", objCalendarOfEdits
 
 ' Файл статистики по данной карте
 objCalendarOfEdits.save_local_stat PATH_TO_OSM & "\" & strMapID & "\" & strMapID & "_editors.xml", rsStat
   
 ' Файл статистики по всем картам
 SaveStatisticsToXml rsStat, PATH_TO_LOG & "statistics.xml"
 
 SaveStatRs rsStat
 
End Sub
Private Sub ParseCommandLine(strMapID As String, strMapName As String)
Dim strCommandLine As String
Dim args() As String
  strCommandLine = Trim$(Command())
  args = Split(strCommandLine, " ", 2)
  If UBound(args) >= 0 Then
    strMapID = args(0)
  Else
    strMapID = ""
  End If
  
  If UBound(args) >= 1 Then
    strMapName = args(1)
  Else
    strMapName = ""
  End If
  
  If Left$(strMapName, 1) = """" And Right$(strMapName, 1) = """" Then
    strMapName = Mid(strMapName, 2, Len(strMapName) - 2)
  End If
End Sub
Public Sub Main()
 Dim strMapID As String
 Dim strMapName As String
On Error GoTo finalize
  Open PATH_TO_LOG & "log-statistics.txt" For Append As #3
  
  ParseCommandLine strMapID, strMapName
  If strMapID <> "" Then
    Print #3, ""
    Print #3, " --| OSM statistics, (C) Zkir 2010"
    Print #3, "Statistics calcualtion has been started"
    Print #3, "MapID: " & strMapID
    Print #3, "MapName: " & strMapName

    Main1 strMapID, strMapName
    Print #3, "Statistics calculation has been finished OK"
  Else
    MsgBox "Usage: zOsmStat <MapID>"
  End If
  
finalize:
 If Err.Number <> 0 Then
    Print #3, "Error: " & Err.Description
    Close #3
    ExitProcess 1
 End If
 Close #3

 
End Sub



