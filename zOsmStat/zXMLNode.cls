VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
  Persistable = 0  'NotPersistable
  DataBindingBehavior = 0  'vbNone
  DataSourceBehavior  = 0  'vbNone
  MTSTransactionMode  = 0  'NotAnMTSObject
END
Attribute VB_Name = "zXMLNode"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
'******************************************************************************
'Доморощенный парсер XML
'******************************************************************************
Option Explicit


Public dtTimeStamp As Date
Private elements() As String
 
 
Public Function ParseNode(strNode As String) As Date


 dtTimeStamp = NULL_DATE
 strNode = Mid$(strNode, 2, Len(strNode) - 2)
 elements = Split(strNode, " ")
 
 Select Case elements(0)
   Case "?xml": dtTimeStamp = NULL_DATE
   Case "osm": dtTimeStamp = NULL_DATE
   Case "/osm": dtTimeStamp = NULL_DATE
   Case "bound": dtTimeStamp = NULL_DATE
   'node (has timestamp)
   Case "node": dtTimeStamp = ConvertFromXMLDate(GetNodeTime())
   Case "/node": dtTimeStamp = NULL_DATE
   Case "tag": dtTimeStamp = NULL_DATE
   'way (has timestamp)
   Case "way": dtTimeStamp = ConvertFromXMLDate(GetNodeTime())
   Case "/way": dtTimeStamp = NULL_DATE
   Case "nd": dtTimeStamp = NULL_DATE
   'relation
   Case "relation": dtTimeStamp = ConvertFromXMLDate(GetNodeTime())
   Case "/relation": dtTimeStamp = NULL_DATE
   Case "member": dtTimeStamp = NULL_DATE
   Case Else
     Err.Raise vbObjectError, "", "unknown tag found: " & elements(0)
 End Select

 ParseNode = dtTimeStamp
End Function

'Найдем значение тега timestamp
Private Function GetNodeTime() As String
  GetNodeTime = GetAttributeValue("timestamp")
End Function
Public Function GetAttributeValue(strAttributeName) As String
Dim i As Integer
Dim strAttributeValue As String
Dim intAttrNameLen As Integer
  intAttrNameLen = Len(strAttributeName)
  strAttributeValue = ""
  For i = 1 To UBound(elements)
   If Left$(elements(i), intAttrNameLen) = strAttributeName Then
     strAttributeValue = Mid$(elements(i), intAttrNameLen + 2)
     If Mid$(elements(i), intAttrNameLen + 1, 1) <> "=" Then
       Err.Raise vbObjectError, "", "= is missing"
     End If
     
     Exit For
   End If
  Next i
  
  If Trim$(strAttributeValue) <> "" Then
    If Left$(strAttributeValue, 1) <> """" Or Right$(strAttributeValue, 1) <> """" Then
       Err.Raise vbObjectError, "", "quotation marks are missing"
    End If
     
    strAttributeValue = Mid$(strAttributeValue, 2, Len(strAttributeValue) - 2)
  End If

GetAttributeValue = strAttributeValue
End Function
