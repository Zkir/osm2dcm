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
Private Const NULL_DATE = "1900-1-1"


Public dtTimeStamp As Date
Private elements() As String
 
 
Public Function ParseNode(strNode As String) As Date


  strNode = Trim$(strNode)
  If Left$(strNode, 1) <> "<" Or Right$(strNode, 1) <> ">" Then
    Err.Raise vbObjectError, "", "angle brakets are missing"
  End If

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
   
   Case "bounds": dtTimeStamp = NULL_DATE
   Case "/bounds": dtTimeStamp = NULL_DATE
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
Dim j As Integer
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
  j = i
  
  If Trim$(strAttributeValue) <> "" Then
    If Left$(strAttributeValue, 1) <> """" Then
       Err.Raise vbObjectError, "", "left quotation mark is missing"
    End If
    
10:
    If Right(strAttributeValue, 1) = "/" Then
      strAttributeValue = Left(strAttributeValue, Len(strAttributeValue) - 1)
    End If
    
    

    If Right$(strAttributeValue, 1) <> """" Then
     'Нет правой кавычки. Это может быть вызвано тем, что "элементы" разделены пробелами.
     'поэтому мы попытаемся прибавить элемент который справа
       If j < UBound(elements) Then
         strAttributeValue = strAttributeValue + " " + elements(j + 1)
         j = j + 1
       Else
         Err.Raise vbObjectError, "", "right quotation mark is missing"
       End If
       GoTo 10 ' а как все зациклится!
      
    End If
     
    strAttributeValue = Mid$(strAttributeValue, 2, Len(strAttributeValue) - 2)
  End If

GetAttributeValue = strAttributeValue
End Function

Function Name() As String
  Name = elements(0)
End Function
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

