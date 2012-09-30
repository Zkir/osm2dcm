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

Const mptCityOver10M = "0x0100"
Const mptCity200_500K = "0x0600"
Const mptCity100_200K = "0x0700"
Const mptCity50_100K = "0x0800"

Const mptSettlement2_5K = "0x0C00"
Const mptSettlement500_1000 = "0x0E00"
Const mptSettlement200_500 = "0x0F00"
Const mptSettlementLess100 = "0x1100"


'Cтатистика по секциям польского файла

Const RS_STAT_KIND = "Kind"
Const RS_STAT_TYPE = "Type"
Const RS_STAT_COUNT = "Count"
Const RS_STAT_TOTAL_SIZE = "Size"

'Статистика по типам секций
Dim rsMPStat     As ADODB.Recordset

'Показатели
'Общая протяженность дорог
'Протяженность дворовых проездов
'Общее число ПОИ
'Число ПОИ с адресной/контактной информацией
Dim dblRoadLength As Double
Dim dblServiceRoadLength As Double
Dim intCitiesNumber As Long
Dim intTotalPoiNumber As Long
Dim intPoiWithAddressNumber As Long

Private Sub Class_Initialize()
  Set rsMPStat = New ADODB.Recordset

  rsMPStat.Fields.Append RS_STAT_KIND, adVarWChar, 20
  rsMPStat.Fields.Append RS_STAT_TYPE, adVarWChar, 10
  rsMPStat.Fields.Append RS_STAT_COUNT, adInteger
  rsMPStat.Fields.Append RS_STAT_TOTAL_SIZE, adInteger
    
  rsMPStat.Open
    
  dblRoadLength = 0
  dblServiceRoadLength = 0
  intTotalPoiNumber = 0
  intPoiWithAddressNumber = 0
  intCitiesNumber = 0
End Sub

Public Sub ProcessSection(oMpSection As clsMpSection)
  Dim dblRoadLen As Double
  'Самая общая статистика, об объектах мп по типу.
  ProcessSectionGen oMpSection.SectionType, oMpSection.mpType, oMpSection.SizeInBytes
  
  'Специфические показатели
  'Пои
  If oMpSection.SectionType = "[POI]" Then
    If (oMpSection.mpType >= mptCityOver10M And oMpSection.mpType <= mptSettlementLess100) Then
      'Населенные пункты учитываются отдельно и в число ПОИ не включаются.
      intCitiesNumber = intCitiesNumber + 1
     
    Else
    'Собственно пои
      If oMpSection.mpType <> "0x6100" Then 'точечные дома учитывать не надо, они уже учтены в адресах
        intTotalPoiNumber = intTotalPoiNumber + 1
        If Trim$(oMpSection.GetAttributeValue("HouseNumber")) <> "" And _
           Trim$(oMpSection.GetAttributeValue("StreetDesc")) <> "" Then
          intPoiWithAddressNumber = intPoiWithAddressNumber + 1
        End If
      End If
    End If
  End If
  'Дороги
  If oMpSection.SectionType = "[POLYLINE]" Then
    If oMpSection.mpRouteParam <> "" Then
      If oMpSection.GetOsmHighway <> "service" Then
        dblRoadLen = oMpSection.CalculateLength
        If oMpSection.isOneWay Then
          dblRoadLen = dblRoadLen / 2
        End If
        dblRoadLength = dblRoadLength + dblRoadLen
      Else
        dblServiceRoadLength = dblServiceRoadLength + oMpSection.CalculateLength
      End If
    Else
      If oMpSection.mpType = "0x8849" Then
        'Если дворовые проезды не влючены, они получают тип 0x8849
        dblServiceRoadLength = dblServiceRoadLength + oMpSection.CalculateLength
      End If
    End If
  End If
  
End Sub

'Самая общая статистика, об объектах мп по типу.
Private Sub ProcessSectionGen(strKind As String, strType As String, intSize As Long)
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
  Print #FileNumber, "<Summary>"
  Print #FileNumber, "  <RoadLengthKm>" & Round(dblRoadLength) & "</RoadLengthKm>"
  Print #FileNumber, "  <ServiceRoadLengthKm>" & Round(dblServiceRoadLength) & "</ServiceRoadLengthKm>"
  Print #FileNumber, "  <CitiesNumber>" & intCitiesNumber & "</CitiesNumber>"
  Print #FileNumber, "  <TotalPoiNumber>" & intTotalPoiNumber & "</TotalPoiNumber>"
  Print #FileNumber, "  <PoiWithAddressNumber>" & intPoiWithAddressNumber & "</PoiWithAddressNumber>"
  Print #FileNumber, "</Summary>"
  
  Print #FileNumber, "<TypeList>"
  Do While Not rsMPStat.EOF
    Print #FileNumber, "  <Type>"
    Print #FileNumber, "    <Kind>" & rsMPStat(RS_STAT_KIND).Value & "</Kind>"
    Print #FileNumber, "    <Type>" & rsMPStat(RS_STAT_TYPE).Value & "</Type>"
    Print #FileNumber, "    <Count>" & rsMPStat(RS_STAT_COUNT).Value & "</Count>"
    Print #FileNumber, "    <Size>" & rsMPStat(RS_STAT_TOTAL_SIZE).Value & "</Size>"
    Print #FileNumber, "  </Type>"
    rsMPStat.MoveNext
  Loop
  Print #FileNumber, "</TypeList>"
  Print #FileNumber, "</Statistics>"
  

End Sub



