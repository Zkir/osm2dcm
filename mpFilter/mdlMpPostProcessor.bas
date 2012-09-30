Attribute VB_Name = "mdlMpPostProcessor"
Option Explicit
'***************************************************************************
'Фильтрация mp
'***************************************************************************
Public Function ereg(ByVal Expression As String, _
                     ByVal Mask As String, _
                     Optional blnIgnoreCase As Boolean = False) As Boolean
  Static soRegExp As New VBScript_RegExp_55.RegExp
  soRegExp.IgnoreCase = blnIgnoreCase
  soRegExp.Pattern = Mask
  ereg = soRegExp.Test(Expression)
  
  
End Function


'Я хочу выбросить из карты пешеходные дорожки без названий.
'0x16

'Будем считать что mp состоит из секций [...] ... [END...]
'Будем обрабатывать секцию за секцией.
'Будем также считать что секция состоит из атрибутов
'Name = Value

Public Function NormalizeStreetName(ByVal strStreetName As String, ByVal blnKillUl As Boolean) As String
Dim l As Long
  strStreetName = Trim$(strStreetName)
   
 
  l = Len(strStreetName)
  If blnKillUl Then
    
    If LCase(Left(strStreetName, 5)) = "улица" Then strStreetName = Right(strStreetName, l - 5)
    If LCase(Left(strStreetName, 3)) = "ул." Then strStreetName = Right(strStreetName, l - 3)
  
    If LCase(Right(strStreetName, 5)) = "улица" Then strStreetName = Left(strStreetName, l - 5)
    If LCase(Right(strStreetName, 3)) = "ул." Then strStreetName = Left(strStreetName, l - 3)
    
    
  End If
  
  'Применим сокращения
  '
  strStreetName = " " & Trim$(strStreetName) & " "
  
  If Not (ereg(strStreetName, "^ [0-9]+-я Набережная $") Or strStreetName = " Набережная " Or _
          ereg(strStreetName, "^ [0-9]+-я Набережная улица $") Or strStreetName = " Набережная улица ") Then
    ' названия типа "6-я набережная" не сокращаются, во избежание "6-я наб."
    strStreetName = Replace$(strStreetName, " набережная ", " наб. ", , , vbTextCompare)
  End If
  
  
  strStreetName = Replace$(strStreetName, " проспект ", " пр. ", , , vbTextCompare)
  strStreetName = Replace$(strStreetName, " площадь ", " пл. ", , , vbTextCompare)
  
  strStreetName = Replace$(strStreetName, " переулок ", " пер. ", , , vbTextCompare)
  strStreetName = Replace$(strStreetName, " проезд ", " пр-д. ", , , vbTextCompare)
  strStreetName = Replace$(strStreetName, " шоссе ", " ш. ", , , vbTextCompare)
  strStreetName = Replace$(strStreetName, " улица ", " ул. ", , , vbTextCompare)
  
  ' 'Убьем пробел перед номером
  'strStreetName = Replace$(strStreetName, " № ", " №", , , vbTextCompare)
  
  ''Номер СГ таки  не понимает.
  'strStreetName = Replace$(strStreetName, " № ", " №", , , vbTextCompare)

   
  'Обтримливание на всякий случай
  strStreetName = Trim$(strStreetName)
  
  ' статусная часть переносится в конец
  If Not blnKillUl Then
    l = Len(strStreetName)
    If LCase(Left(strStreetName, 3)) = "ул." Then strStreetName = Right(strStreetName, l - 3) + " ул."
    'If LCase(Left(strStreetName, 3)) = "пр." Then strStreetName = Right(strStreetName, l - 3) + " пр."
  End If
  
  strStreetName = Trim$(strStreetName)
  
  NormalizeStreetName = strStreetName

End Function

'mp-файл преобразуется по определенным правилам.
' также делаются тесты (или не делаются, смотря по параметру)
Public Sub ProcessMP(strSrcFileName As String, _
                     strTargetFileName As String, _
                     strViewPoint As String, _
                     blnDoTests As Boolean, _
                     blnLite As Boolean)
                     
Dim blnSkipSection As Boolean
Dim oMpSection As clsMpSection
Dim oAddrRegisty As clsAddrRegistry

Dim oRoutingTestFull As clsRoutingTest
Dim oRoutingTest0 As clsRoutingTest
Dim oRoutingTest1 As clsRoutingTest
Dim oRoutingTest2 As clsRoutingTest
Dim oRoutingTest3 As clsRoutingTest

Dim oSourceErrors As clsSourceErrors
Dim oStatistic    As clsStatistic

Dim oDanglingRoads As clsDanglingRoads

Dim Size As Double
Dim strLabel As String

On Error GoTo finalize

If blnDoTests Then
  Set oAddrRegisty = New clsAddrRegistry
  Set oRoutingTestFull = New clsRoutingTest
  oRoutingTestFull.FillExceptionList
  Set oRoutingTest0 = New clsRoutingTest
  Set oRoutingTest1 = New clsRoutingTest
  Set oRoutingTest2 = New clsRoutingTest
  Set oRoutingTest3 = New clsRoutingTest
  
  Set oDanglingRoads = New clsDanglingRoads
End If

Set oSourceErrors = New clsSourceErrors
Set oStatistic = New clsStatistic

Open strSrcFileName For Input As #1
Open strTargetFileName For Output As #2

Dim dtStart As Date
Dim dtEnd   As Date

dtStart = Date + Time

Do While Not EOF(1)
  
  Set oMpSection = New clsMpSection
  oMpSection.ReadSection
  
  blnSkipSection = False
  
  
  '0 Расставим доп теги
  If oMpSection.SectionType = "[IMG ID]" Then
    If Trim(strViewPoint) <> "" Then
      oMpSection.SetAttributeValue "PointView", strViewPoint
    End If
  End If
  
  
  '1
  'Убьем названия огородов (это очевидная бага конвертора )
  If oMpSection.SectionType = "[POLYGON]" Then
    If (oMpSection.mpType = "0x4e") And oMpSection.mpLabel <> "" Then
      'Or oMpSection.mpType = "0x3f"
      oMpSection.SetAttributeValue "Label", ""
    End If
  End If
  
''  Теперь это делается в osm2mp, более аккуратно
''  '2
''  'Переделаем pedestrian у которых нет названия в нерутинговые пешеходные дорожки.
''  'В СГ это 0x8849
''  If oMpSection.SectionType = "[POLYLINE]" Then
''    If oMpSection.mpType = "0x16" And oMpSection.mpLabel = "" Then
''      oMpSection.mpType = "0x8849"
''      oMpSection.mpEndLevel = 0
''    End If
''  End If
  
  '3
  'Переделаем нерутинговые улицы и проезды  в пешеходные улицы
  If oMpSection.SectionType = "[POLYLINE]" Then
    If (oMpSection.mpType = "0x03" Or oMpSection.mpType = "0x06" Or oMpSection.mpType = "0x07") Then
      If oMpSection.mpRouteParam = "" Then
        If oMpSection.mpLabel <> "" Then
          'Именованный нерутинговый переулок или проезд переделаем в пешеходный
          'За неимением лучшего
          oMpSection.mpType = "0x16"
          Debug.Print oMpSection.mpLabel
        Else
          blnSkipSection = True
        End If
      End If
    End If
  End If
 
  
  '4
  'Убьем слово улица, ул. в названиях улиц
  If oMpSection.SectionType = "[POLYLINE]" Then
    If (oMpSection.mpType >= "0x01" And oMpSection.mpType <= "0x0C") Or _
        oMpSection.mpType = "0x0a" Or oMpSection.mpType = "0x0b" Or oMpSection.mpType = "0x0c" Or _
        oMpSection.mpType = "0x16" Or oMpSection.mpType = "0x8849" Or oMpSection.mpType = "0x880a" Then
      strLabel = oMpSection.mpLabel
      
      'Это особенная бага osm2mp. StreetDesc нерутинговым улицам не присваевается.
      'Тем не менее, оригинальное название нам понадобиться для адресного теста.
      If strLabel <> "" Then
        If oMpSection.GetAttributeValue("StreetDesc") = "" Then
          oMpSection.SetAttributeValue "StreetDesc", strLabel
        End If
      End If
      
      'Нормализуем Label
      If strLabel <> "" Then
        strLabel = NormalizeStreetName(strLabel, True)
        oMpSection.SetAttributeValue "Label", strLabel
      End If
    End If
  End If
  
  '5
  'Убьем слово улица, ул. в адресах
  
  strLabel = oMpSection.GetAttributeValue("StreetDesc")
  If strLabel <> "" Then
    strLabel = NormalizeStreetName(strLabel, False)
    oMpSection.SetAttributeValue "StreetDesc", strLabel
  End If
  
  
  '6
  'Особый тип для грунтовых дорог. "Грунтовыми" будем считать дороги с классом скорости 0 (5 км/ч)
  'Это правильно, потому что непроезжие дороги заметны визуально.
  If oMpSection.SectionType = "[POLYLINE]" Then
    If (oMpSection.mpType <> "0x1b") And (oMpSection.mpType <> "0x47") And _
      (oMpSection.mpType <> "0x16") And (oMpSection.mpType <> "0x8849") And (oMpSection.mpType <> "0x07") Then
    ' Кроме паромных переправ,  строящихся дорог и пешеходных дорог и служебных проездов
      If Left(Trim(oMpSection.mpRouteParam), 1) = "0" Then
        oMpSection.mpType = "0x0a"
      End If
    End If
  End If
  
  '7
  'убьем нерутинговые паромные переправы
  If oMpSection.SectionType = "[POLYLINE]" Then
    If oMpSection.mpType = "0x1b" Then
      If oMpSection.mpRouteParam = "" Then
        blnSkipSection = True
      End If
    End If
  End If
  
  '8
  'Запреты проезда в СГ не поддерживаются. Вместо них поддерживается запрет транзитного рутинга.
  If oMpSection.SectionType = "[POLYLINE]" Then
    If oMpSection.mpRouteParam <> "" Then
      If Split(oMpSection.mpRouteParam, ",")(6) = 1 Then
        oMpSection.SetAttributeValue "RouteParamExt", 1
      End If
    End If
    
  End If
  
  
  '9
  ' Классифицируем города по населению.
  Dim intPopulation As Long
  Dim strPopulation As String

  If oMpSection.SectionType = "[POI]" Then
    If oMpSection.GetAttributeValue("City") = "Y" Then
    
      If oMpSection.GetAttributeValue("CityName") = "" Then
        oMpSection.SetAttributeValue "CityName", oMpSection.mpLabel
      End If
      
      'Получим население
      strPopulation = oMpSection.GetAttributeValue("Population")
      If IsNumeric(strPopulation) Then
        intPopulation = strPopulation
      Else
        intPopulation = -1
        If Trim$(strPopulation) <> "" Then
          Print #3, "unparsed population value: " & strPopulation
        End If
      End If
       
      'Добавим город в адресный реестр.
      'Возможно нужно проверять еще и тип.
      If blnDoTests Then
        oAddrRegisty.AddCityToRegistry oMpSection.mpLabel, oMpSection.GetCoords, intPopulation, False, False, oMpSection.mpType
      End If
      
      If intPopulation > 0 Then
        If intPopulation >= 10000000# Then ' Мегаполис, >10 млн
          oMpSection.mpType = "0x0100"
        ElseIf intPopulation >= 5000000# Then ' Мегаполис, 5-10 млн
          oMpSection.mpType = "0x0200"

        ElseIf intPopulation >= 2000000# Then ' Крупный город, 2-5 млн
          oMpSection.mpType = "0x0300"

        ElseIf intPopulation >= 1000000# Then ' Крупный город, 1-2 млн
          oMpSection.mpType = "0x0400"

        ElseIf intPopulation >= 500000# Then 'Город 0.5-1 млн
          oMpSection.mpType = "0x0500"

        ElseIf intPopulation >= 200000# Then 'Город 200 тыс - 500 тыс
          oMpSection.mpType = "0x0600"

        ElseIf intPopulation >= 100000# Then 'Город 100 тыс - 200 тыс
          oMpSection.mpType = "0x0700"

        ElseIf intPopulation >= 50000# Then 'Город 50 тыс - 100 тыс
          oMpSection.mpType = "0x0800"

        ElseIf intPopulation >= 20000# Then 'Город 20 тыс - 50 тыс
          oMpSection.mpType = "0x0900"

        ElseIf intPopulation >= 10000# Then 'Город 10 тыс - 20 тыс
          oMpSection.mpType = "0x0a00"

        ElseIf intPopulation >= 5000# Then 'населенный пункт 5 тыс - 10 тыс
          oMpSection.mpType = "0x0b00"

        ElseIf intPopulation >= 5000# Then 'населенный пункт 2 тыс - 5 тыс
          oMpSection.mpType = "0x0c00"

        ElseIf intPopulation >= 5000# Then 'населенный пункт 1 тыс - 2 тыс
          oMpSection.mpType = "0x0d00"
        Else
          oMpSection.mpType = "0x0e00"
        End If
      End If
        
    End If
  End If
  
  '10
  'Добавим в реестр полигональные НП
  If blnDoTests Then
  If oMpSection.SectionType = "[POLYGON]" Then
    If oMpSection.mpType = "0x01" Or oMpSection.mpType = "0x03" Then  'Город
      oAddrRegisty.AddCityToRegistry oMpSection.mpLabel, oMpSection.GetCoords, 0, True, oMpSection.mpType = "0x01", ""
    End If
  End If
  End If
  
  '11
  'Классифицируем озера по размеру
  If oMpSection.SectionType = "[POLYGON]" Then
    If oMpSection.mpType = "0x3f" Then  'Medium Lake
    
'      If oMpSection.mpLabel = "озеро Неро" Then
'        'Debug.Print "ffff!"
'
'      End If
      
      Size = oMpSection.CalculateArea()
      If Size < 0.25 Then
        'EndLevel = 3
        oMpSection.mpType = "0x41"
        oMpSection.SetAttributeValue "EndLevel", 2
      ElseIf (Size <= 11) Then
        oMpSection.mpType = "0x40"
      ElseIf (Size <= 25) Then
        oMpSection.mpType = "0x3f"
        oMpSection.SetAttributeValue "EndLevel", 4
      ElseIf (Size <= 75) Then
        oMpSection.mpType = "0x3e"
        oMpSection.SetAttributeValue "EndLevel", 4
      ElseIf (Size <= 250) Then
        oMpSection.mpType = "0x3d"
        oMpSection.SetAttributeValue "EndLevel", 4
      ElseIf (Size <= 600) Then
        oMpSection.mpType = "0x3c"
        oMpSection.SetAttributeValue "EndLevel", 4
      ElseIf (Size <= 1100) Then
        oMpSection.mpType = "0x44"
        oMpSection.SetAttributeValue "EndLevel", 4
      ElseIf (Size <= 3300) Then
        oMpSection.mpType = "0x43"
        oMpSection.SetAttributeValue "EndLevel", 4
      Else
        oMpSection.mpType = "0x42"
        oMpSection.SetAttributeValue "EndLevel", 4
      End If
      
      ' Убьем слово "озеро" в названиях озер
      strLabel = oMpSection.mpLabel
      If strLabel <> "" Then
        strLabel = Replace$(" " & strLabel & " ", " озеро ", "", vbTextCompare)
        strLabel = Replace$(strLabel, "водохранилище", "вдхр.", vbTextCompare)
        oMpSection.mpLabel = Trim$(strLabel)
      End If
    
    End If
  End If
  
  ' 12
  ' Определим посты ДПС.
  If oMpSection.SectionType = "[POI]" Then
    If oMpSection.mpType = "0x3001" Then
      If ereg(oMpSection.mpLabel, "ДПС", True) Or ereg(oMpSection.mpLabel, "ГИБДД", True) Then
        oMpSection.mpType = "0xf202"
      End If
    End If
  End If

  '13
  'Отфильтруем лишние запреты поворотов
   If oMpSection.SectionType = "[Restrict]" Then
    Dim strRestrParam As String
    strRestrParam = oMpSection.GetAttributeValue("RestrParam")
    If strRestrParam <> "" Then
      If Split(strRestrParam, ",")(2) = 1 Then
      ' Это такой странный запрет поворота, который на автомобили не распространяется.
        blnSkipSection = True
      End If
    End If
  End If
 
  
  '14-1
  'создадим адресный реестр.
  'Дома, или во всяком случае, объекты с номером дома.
  'Const CityNameAttr = "CityIdx"
  Const CityNameAttr = "CityName"
  If blnDoTests And ( _
    ((oMpSection.SectionType = "[POLYGON]") And (oMpSection.mpType = "0x13")) Or _
    ((oMpSection.SectionType = "[POI]") And (oMpSection.mpType = "0x6100")) _
    ) Then
    If oMpSection.GetAttributeValue("HouseNumber") <> "" Then
      oAddrRegisty.AddHouseToRegistry _
                 Trim$(oMpSection.GetAttributeValue("HouseNumber")), _
                 Trim$(oMpSection.GetAttributeValue("StreetDesc")), _
                 Trim$(oMpSection.GetAttributeValue(CityNameAttr)), _
                 oMpSection.GetCoords()
                 
                 
    End If
  End If
  
   
  '14-2
  ' Улицы. Так случилось что в СитиГиде дома должны быть привязаны
  ' к *Рутинговым* улицам
  If blnDoTests And oMpSection.SectionType = "[POLYLINE]" Then

    If (oMpSection.mpRouteParam <> "" Or _
        oMpSection.mpType = "0x16" Or oMpSection.mpType = "0x8849") Then
       'And  (oMpSection.GetAttributeValue(CityNameAttr) <> "")
       
      
      oAddrRegisty.AddStreetToRegistry _
                 Trim$(oMpSection.GetAttributeValue("StreetDesc")), _
                 Trim$(oMpSection.GetAttributeValue(CityNameAttr)), _
                 (oMpSection.mpRouteParam <> "") And Not (oMpSection.mpType = "0x07"), _
                 oMpSection.GetCoords(), _
                 (oMpSection.mpType = "0x06") 'Or (В названии есть слово улица)
      
      ' Примечания.
      '  1. Подразумевается что hw=residential и hw=living_street должны быть внутри городов
      '  2. Дворовые проезды все равно не ищутся, даже рутинговые.
                 
    End If
  End If
  
  '14-3
  'Территории, по которым может вестись адресация.
  If blnDoTests Then
    If (oMpSection.SectionType = "[POI]") And (oMpSection.mpType = "0x1F00" Or oMpSection.mpType = "0x1F01") Then
    
      oAddrRegisty.AddAddrTerritoryToRegistry _
                    Trim$(oMpSection.GetAttributeValue(CityNameAttr)), _
                    Trim$(oMpSection.mpLabel)
    End If
  End If
  
  
  '15
  'Добавим улицу в тест рутинга
  If blnDoTests And (oMpSection.SectionType = "[POLYLINE]") Then

    If (oMpSection.mpRouteParam <> "") Then
      Dim NodeList() As Long
      Dim NodeList2() As Long
      Dim NN As Long
      Dim aNode As Long
      Dim strNodeAttr
      Dim lat1 As Double, lon1 As Double, lat2 As Double, lon2 As Double
      
      oMpSection.CalculateBBOX lat1, lon1, lat2, lon2
      
      aNode = -1
      NN = 0
      ReDim NodeList(100)
      ReDim NodeList2(100)
      Do
        strNodeAttr = oMpSection.GetAttributeValue("Nod" & NN)
        If strNodeAttr <> "" Then
          NN = NN + 1
          aNode = Split(strNodeAttr, ",")(1)
          NodeList(NN - 1) = aNode
          
          NodeList2(NN - 1) = Split(strNodeAttr, ",")(2)
        End If
        
      Loop Until strNodeAttr = ""
      
      ReDim Preserve NodeList(NN - 1)
      ReDim Preserve NodeList2(NN - 1)
      
      'Передается список рутинговых нод, и bbox для данной дороги

      
      If OSMLevelByTag(oMpSection.GetOsmHighway) <= 0 Then
        oRoutingTest0.AddRoad NodeList, NodeList2, lat1, lon1, lat2, lon2
      End If
      
      If OSMLevelByTag(oMpSection.GetOsmHighway) <= 1 Then
        oRoutingTest1.AddRoad NodeList, NodeList2, lat1, lon1, lat2, lon2
      End If
      
      If OSMLevelByTag(oMpSection.GetOsmHighway) <= 2 Then
        oRoutingTest2.AddRoad NodeList, NodeList2, lat1, lon1, lat2, lon2
      End If
      
      If OSMLevelByTag(oMpSection.GetOsmHighway) <= 3 Then
        oRoutingTest3.AddRoad NodeList, NodeList2, lat1, lon1, lat2, lon2
      End If
      
      'Это все дороги кроме service
      If OSMLevelByTag(oMpSection.GetOsmHighway) <= 4 Then
        oRoutingTestFull.AddRoad NodeList, NodeList2, lat1, lon1, lat2, lon2
      End If
      
      
      'Нужно передавать координаты первой и последней вершины
      oMpSection.CalculateFirstLast lat1, lon1, lat2, lon2
      oDanglingRoads.AddRoad oMpSection.mpType, OSMLevelByTag(oMpSection.GetOsmHighway), NodeList, NodeList2, lat1, lon1, lat2, lon2
                 
    End If
  End If
  
  'Убьем CountryName, оно в СГ не используется
  If oMpSection.GetAttributeValue("CountryName") <> "" Then
    oMpSection.DeleteAttribute "CountryName"
  End If
  
  'Комментарии. В них содержаться ошибки найденные Osm2mp.pl
  Dim i As Integer
  If oMpSection.SectionType = "COMMENT" Then
    For i = 0 To oMpSection.nComments - 1
      oSourceErrors.ProcessComment oMpSection.strComments(i)
    Next i
  End If
  
  'Статистика. Передаем просто секцию
  oStatistic.ProcessSection oMpSection
  
  If blnLite Then
    ' "Облегченная карта"
   If oMpSection.SectionType = "[POLYGON]" Then
     If oMpSection.mpType = "0x13" Or oMpSection.mpType = "0x50" Then
     'Леса и дома
       blnSkipSection = True
     End If
   End If
   If oMpSection.SectionType = "[POLYLINE]" Then
     'Грунтовые дороги и служебные проезды
     If oMpSection.mpType = "0x880a" Or oMpSection.mpType = "0x8849" Then
       blnSkipSection = True
     End If
   End If
  End If
  
  'Запишем секцию
  If Not blnSkipSection Then
    oMpSection.WriteSection
  End If
 
  Set oMpSection = Nothing
  
Loop
Close #1
Close #2

If blnDoTests Then
 'Лог адресов.
  oAddrRegisty.ValidateCities
  oAddrRegisty.ValidateCitiesReverse
  oAddrRegisty.ValidateHouses
  
 'Висячие дороги
  oDanglingRoads.Validate
  
 'Исключения/изоляты в рутинговом графе
 oRoutingTestFull.CheckExceptions
  
End If
'Выведем отчет о проделанной работе в xml
Dim dtCurrentDate       As Date
dtCurrentDate = Date
dtEnd = Date + Time

Open strTargetFileName & "_addr.xml" For Output As #4
  Print #4, "<?xml version=""1.0"" encoding=""windows-1251""?>"
  Print #4, "<QualityReport>"
  Print #4, " <Date>" & FormatXMLDate(dtCurrentDate) & "</Date>"
  Print #4, " <TimeUsed>" & Hour(dtEnd - dtStart) & ":" & Minute(dtEnd - dtStart) & ":" & Second(dtEnd - dtStart) & "</TimeUsed>"
 
  If blnDoTests Then
    oAddrRegisty.PrintRegistryToXML
    
    Print #4, "<RoutingTest>"
    oRoutingTestFull.PrintRegistryToXML 4
    Print #4, "</RoutingTest>"
    
    Print #4, "<RoutingTestByLevel>"
    Print #4, "<Trunk>"
    oRoutingTest0.PrintRegistryToXML 4
    Print #4, "</Trunk>"
    
    
    Print #4, "<Primary>"
    oRoutingTest1.PrintRegistryToXML 4
    Print #4, "</Primary>"
    
    Print #4, "<Secondary>"
    oRoutingTest2.PrintRegistryToXML 4
    Print #4, "</Secondary>"
    
    Print #4, "<Tertiary>"
    oRoutingTest3.PrintRegistryToXML 4
    Print #4, "</Tertiary>"
    
    Print #4, "</RoutingTestByLevel>"
  End If
  oDanglingRoads.PrintErrorsToXML 4
  oSourceErrors.PrintErrorsToXML 4
  oStatistic.PrintErrorsToXML 4
  
  
  Print #4, "</QualityReport>"

Close #4

Set oAddrRegisty = Nothing
finalize:
  If Err.Number <> 0 Then
    Err.Raise Err.Number, "ProcessMP", Err.Description & " ProcessMP:" & Erl
  End If

End Sub

Function OSMLevelByTag(Tag As String) As Integer
  Dim intLevel As Integer
  
  Select Case Tag
    Case "trunk", "trunk_link", "motorway", "motorway_link"
      intLevel = 0
    Case "primary", "primary_link"
      intLevel = 1
    Case "secondary", "secondary_link"
      intLevel = 2
    Case "tertiary", "tertiary_link"
      intLevel = 3
    Case "residential", "unclassified", "living_street", "checked_construction"
      intLevel = 4
    Case "service"
      intLevel = 5
    Case Else
      intLevel = 5
  End Select
  
OSMLevelByTag = intLevel
  
End Function

Function FormatXMLDate(dtDate As Date) As String
  FormatXMLDate = Format(Year(dtDate), "0000") & "-" & Format(Month(dtDate), "00") & "-" & Format(Day(dtDate), "00")
End Function
