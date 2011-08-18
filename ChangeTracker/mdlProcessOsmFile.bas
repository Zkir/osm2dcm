Attribute VB_Name = "mdlProcessOsmFile"
Option Explicit
' Константы полей
'
Const WAY_MAPID = "MapID"
Const WAY_WAYID = "WayID"
Const WAY_HIGHWAY = "Highway"
Const WAY_NAME = "Name"
Const WAY_REF = "Ref"
Const WAY_CURRENTVERSION = "CurrentVersion"
Const WAY_PREVIOUSVERSION = "PreviousVersion"
Const WAY_CHANGEDESCRIPTION = "ChangeDescription"
Const WAY_CHANGESET = "Changeset"
Const WAY_CHANGEUSER = "ChangeUser"
Const WAY_CHANGEDATE = "ChangeDate"
Const WAY_DANGERSTATUS = "DangerStatus"
Const WAY_APPROVEDVERSION = "ApprovedVersion"
Const WAY_APPROVEDUSER = "ApprovedUser"
Const WAY_APPROVEDDATE = "ApprovedDate"
Const WAY_UPDATEDATE = "UpdateDate"

Const WAY_NNODES = "NNodes"
Const WAY_NTAGS = "NTags"

Public strConnectionString As String
Public strOSMPath As String

Private Sub LoadData(strMapID As String, rsWays As ADODB.Recordset, rsNodes As ADODB.Recordset, blnStructureOnly)
  
  Dim objCon As ADODB.Connection
  Dim strSelect As String
  Set objCon = New ADODB.Connection
  objCon.Open strConnectionString
  
  
  Set rsWays = New ADODB.Recordset
  rsWays.CursorType = adOpenStatic
  rsWays.CursorLocation = adUseClient
  
  strSelect = " where " & WAY_MAPID & " = '" & strMapID & "'"
  If blnStructureOnly Then
    strSelect = strSelect + " and 1=0"
  End If
  
  rsWays.Open "Select * from WayLog " & strSelect, objCon, adOpenStatic, adLockBatchOptimistic, adCmdText

  Set rsNodes = New ADODB.Recordset
  rsNodes.CursorType = adOpenStatic
  rsNodes.CursorLocation = adUseClient
'  rsNodes.Open "Select * from Nodes", objCon, adOpenStatic, adLockBatchOptimistic, adCmdText

End Sub

Private Function ProcessOsmFile(strMapID As String, rsWays As ADODB.Recordset, rsNodes As ADODB.Recordset, strFileName As String)
  Dim textline As String
  Dim clsXMLNode As zXMLNode
  Dim dtDate As Date
  Dim strWayId As String
  Dim intVersion As String
  Dim strUser As String
  Dim strHighway As String
  Dim strRef As String
  Dim strName As String
  Dim intNNodes As Long
  Dim intNTags As Long
  Dim strChangeset  As String
  
  Dim NodeCollection As Collection
  
  
On Error GoTo finalize
  PrintToLog "Processing file: " & strFileName
  Open strFileName For Input As #1
  Do While EOF(1) <> True
    
    Line Input #1, textline
    Set clsXMLNode = New zXMLNode
    clsXMLNode.ParseNode textline
    
    If clsXMLNode.Name = "way" Then
      'Запомним атрибуты
      
      strWayId = clsXMLNode.GetAttributeValue("id")
      dtDate = clsXMLNode.dtTimeStamp
      strUser = clsXMLNode.GetAttributeValue("user")
      intVersion = clsXMLNode.GetAttributeValue("version")
      strChangeset = clsXMLNode.GetAttributeValue("changeset")
      
      strRef = ""
      strHighway = ""
      strName = ""
      intNNodes = 0
      intNTags = 0
      
      Set NodeCollection = New Collection
      
      Do
        Line Input #1, textline
        Set clsXMLNode = New zXMLNode
        clsXMLNode.ParseNode textline
        
        If clsXMLNode.Name = "nd" Then
          intNNodes = intNNodes + 1
          NodeCollection.Add clsXMLNode.GetAttributeValue("ref")
    
        End If
        
        
        If clsXMLNode.Name = "tag" Then
          intNTags = intNTags + 1
           
          'Не работает
          ' - кодировка не совпадает
          If clsXMLNode.GetAttributeValue("k") = "name" Then
            strName = clsXMLNode.GetAttributeValue("v")
          End If
          
          If clsXMLNode.GetAttributeValue("k") = "ref" Then
            strRef = clsXMLNode.GetAttributeValue("v")
          End If
          
          If clsXMLNode.GetAttributeValue("k") = "highway" Then
            strHighway = clsXMLNode.GetAttributeValue("v")
          End If
        
        End If
              
      Loop Until clsXMLNode.Name = "/way"
    
      If (strHighway = "motorway") Or (strHighway = "motorway_link") Or _
         (strHighway = "trunk") Or (strHighway = "trunk_link") Or _
         (strHighway = "primary") Or (strHighway = "primary_link") Then
        'Это в самом деле дорога
        rsWays.AddNew
        rsWays(WAY_MAPID).Value = strMapID
        rsWays(WAY_WAYID).Value = strWayId
        rsWays(WAY_HIGHWAY).Value = strHighway
        rsWays(WAY_REF).Value = strRef
        rsWays(WAY_NAME).Value = strName
        rsWays(WAY_CHANGEDATE).Value = dtDate
        rsWays(WAY_CHANGEUSER).Value = strUser
        rsWays(WAY_CHANGESET).Value = strChangeset
        rsWays(WAY_CURRENTVERSION).Value = intVersion
        rsWays(WAY_NNODES).Value = intNNodes
        rsWays(WAY_NTAGS).Value = intNTags
        
        
        rsWays.Update
        
      End If
    End If
    
  Loop
  
ProcessOsmFile = True
finalize:
  Close #1
  If Err.Number <> 0 Then
    Err.Raise vbObjectError, "ProcessOsmFile", Err.Description
   End If
End Function
Private Sub CompareVersions(rsWays As ADODB.Recordset, rsWaysNew As ADODB.Recordset)
  Dim dtUpdateDate As Date
  Dim strChangeDesc As String
  Dim intDangerStatus As Integer
  
  dtUpdateDate = Now()
  
  rsWaysNew.MoveFirst
  Do While Not rsWaysNew.EOF
    rsWays.Filter = WAY_WAYID & "='" & rsWaysNew(WAY_WAYID).Value & "'"
    If rsWays.RecordCount = 0 Then
      'Это вновь добавленный вей
      rsWays.AddNew
        
      rsWays(WAY_MAPID).Value = rsWaysNew(WAY_MAPID).Value
      rsWays(WAY_WAYID).Value = rsWaysNew(WAY_WAYID).Value
      rsWays(WAY_HIGHWAY).Value = rsWaysNew(WAY_HIGHWAY).Value
      rsWays(WAY_REF).Value = rsWaysNew(WAY_REF).Value
      rsWays(WAY_NAME).Value = rsWaysNew(WAY_NAME).Value
      rsWays(WAY_CHANGEDATE).Value = rsWaysNew(WAY_CHANGEDATE).Value
      rsWays(WAY_CHANGEUSER).Value = rsWaysNew(WAY_CHANGEUSER).Value
      rsWays(WAY_CHANGESET).Value = rsWaysNew(WAY_CHANGESET).Value
      
      rsWays(WAY_CURRENTVERSION).Value = rsWaysNew(WAY_CURRENTVERSION).Value
      rsWays(WAY_NNODES).Value = rsWaysNew(WAY_NNODES).Value
      rsWays(WAY_NTAGS).Value = rsWaysNew(WAY_NTAGS).Value
      
      rsWays(WAY_DANGERSTATUS).Value = 1 ' Минимальный уровень опасности для новой линии
      
      'Дата и причина добавления в трекер
      If rsWays(WAY_CURRENTVERSION).Value = 1 Then
        rsWays(WAY_CHANGEDESCRIPTION).Value = "Линия создана"
        rsWays(WAY_DANGERSTATUS).Value = 1 ' Минимальный уровень опасности для новой линии
      Else
        rsWays(WAY_CHANGEDESCRIPTION).Value = "Линия добавлена в трекер"
        rsWays(WAY_DANGERSTATUS).Value = 2 ' Чуть больше для "пойманной" линии
      End If
      
      'Одобрение снимается
      rsWays(WAY_APPROVEDDATE).Value = Null
      rsWays(WAY_APPROVEDUSER).Value = Null
      rsWays(WAY_APPROVEDVERSION).Value = Null
      
      rsWays(WAY_UPDATEDATE).Value = dtUpdateDate
      rsWays.Update
    Else
      'Это существующий (измененный)  вей
      If rsWays(WAY_CURRENTVERSION).Value <> rsWaysNew(WAY_CURRENTVERSION).Value Then
        rsWays(WAY_MAPID).Value = rsWaysNew(WAY_MAPID).Value
        rsWays(WAY_WAYID).Value = rsWaysNew(WAY_WAYID).Value
        rsWays(WAY_HIGHWAY).Value = rsWaysNew(WAY_HIGHWAY).Value
        rsWays(WAY_REF).Value = rsWaysNew(WAY_REF).Value
        rsWays(WAY_NAME).Value = rsWaysNew(WAY_NAME).Value
        rsWays(WAY_CHANGEDATE).Value = rsWaysNew(WAY_CHANGEDATE).Value
        rsWays(WAY_CHANGEUSER).Value = rsWaysNew(WAY_CHANGEUSER).Value
        rsWays(WAY_CHANGESET).Value = rsWaysNew(WAY_CHANGESET).Value
        
        rsWays(WAY_PREVIOUSVERSION).Value = rsWays(WAY_CURRENTVERSION).Value
        rsWays(WAY_CURRENTVERSION).Value = rsWaysNew(WAY_CURRENTVERSION).Value
        
        rsWays(WAY_NNODES).Value = rsWaysNew(WAY_NNODES).Value
        rsWays(WAY_NTAGS).Value = rsWaysNew(WAY_NTAGS).Value
      
        
        'Дата и причина добавления в трекер
        rsWays(WAY_UPDATEDATE).Value = dtUpdateDate
        strChangeDesc = ""
        intDangerStatus = 1
        
        'Что изменено
        If rsWays(WAY_NNODES).Value > rsWays(WAY_NNODES).OriginalValue Then
          strChangeDesc = strChangeDesc & " Добавлены вершины."
          intDangerStatus = 2
        End If
        
        If rsWays(WAY_NTAGS).Value > rsWays(WAY_NTAGS).OriginalValue Then
          strChangeDesc = strChangeDesc & " Добавлены теги."
          intDangerStatus = 2
        End If
        
        If rsWays(WAY_NTAGS).Value < rsWays(WAY_NTAGS).OriginalValue Then
          strChangeDesc = strChangeDesc & " Удалены теги."
          intDangerStatus = 3
        End If
        
        If rsWays(WAY_NNODES).Value < rsWays(WAY_NNODES).OriginalValue Then
          strChangeDesc = strChangeDesc & " Удалены вершины."
          intDangerStatus = 4
        End If
        
        If strChangeDesc = "" Then
           strChangeDesc = "Изменены теги/вершины"
           intDangerStatus = 1
        End If
        rsWays(WAY_CHANGEDESCRIPTION).Value = strChangeDesc
        rsWays(WAY_DANGERSTATUS).Value = intDangerStatus
        
        'Одобрение снимается
        rsWays(WAY_APPROVEDDATE).Value = Null
        rsWays(WAY_APPROVEDUSER).Value = Null
        rsWays(WAY_APPROVEDVERSION).Value = Null
        
        rsWays.Update
      Else
        'Вей не изменен, но дата последней проверки все равно устанавливается
        rsWays(WAY_UPDATEDATE).Value = dtUpdateDate
      End If
    End If
    rsWaysNew.MoveNext
  Loop
  
  rsWays.Filter = WAY_UPDATEDATE & "<'" & dtUpdateDate & "'"
  'Это веи, которых нет в новом файле
  Do While Not rsWays.EOF
    If rsWays(WAY_CURRENTVERSION).Value <> 0 Then
      rsWays(WAY_PREVIOUSVERSION).Value = rsWays(WAY_CURRENTVERSION).Value
      rsWays(WAY_CURRENTVERSION).Value = 0
      
      'Дату и версию удаления мы по понятной причине получить не можем
      rsWays(WAY_CHANGEDATE).Value = dtUpdateDate
      rsWays(WAY_CHANGEUSER).Value = "???"
      rsWays(WAY_CHANGESET).Value = "???"
          
      'Дата и причина добавления в трекер
      rsWays(WAY_CHANGEDESCRIPTION).Value = "Линия удалена"
      rsWays(WAY_DANGERSTATUS).Value = 5
      
      rsWays(WAY_APPROVEDDATE).Value = Null
      rsWays(WAY_APPROVEDUSER).Value = Null
      rsWays(WAY_APPROVEDVERSION).Value = Null
      
    Else
      ' если мы уже знаем, что вей  удален, ничего делать не надо
    End If
    rsWays(WAY_UPDATEDATE).Value = dtUpdateDate
    
    rsWays.MoveNext
  Loop
  
  rsWays.Filter = adFilterNone
End Sub

Public Sub ProcessMap(MapCode As String)
Dim rsNodes As ADODB.Recordset
Dim rsWays As ADODB.Recordset

Dim rsNodesNew As ADODB.Recordset
Dim rsWaysNew As ADODB.Recordset
 
  
  'Загрузим данные из базы
  LoadData MapCode, rsWays, rsNodes, False
  LoadData MapCode, rsWaysNew, rsNodesNew, True

  'Загрузим данные из осм-файла

  ProcessOsmFile MapCode, rsWaysNew, rsNodesNew, strOSMPath & "\" & MapCode & "\final.osm"
  
  CompareVersions rsWays, rsWaysNew

  rsWays.UpdateBatch adAffectAll


End Sub


