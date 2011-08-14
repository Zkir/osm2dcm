Attribute VB_Name = "mdlMain"
Option Explicit
Private Declare Sub ExitProcess Lib "kernel32" (ByVal uExitCode As Long)

Private Sub ParseCommandLine(strSource As String)
Dim strCommandLine As String
Dim args() As String
  strCommandLine = Trim(Command())
  
  If strCommandLine = "" Then
    Err.Raise vbObjectError, "ParseCommandLine", "Invalid command line. Usage: ChangeTracker <MapCode>"
  End If
  
  args = Split(strCommandLine, " ")
  strSource = args(0)
  
'  If UBound(args) >= 0 Then
'    strTarget = args(1)
'    If UBound(args) > 1 Then
'      strViewPoint = Replace(args(2), ",", " ")
'    End If
'  End If
End Sub

Private Sub LoadConfig()
  Open App.Path & "\ChangeTracker.ini" For Input As #2
  
    
  Line Input #2, strConnectionString
  Line Input #2, strOSMPath
  
  Close #2

End Sub
Public Sub PrintToLog(strLine As String)
Print #3, strLine
End Sub
'Основная процедура
' Файловые переменные такие
' #1 - исходный осм-файл
' #2 - конфиг с строкой доступа к DB
' #3 - лог
Sub Main()
  Dim MapCode As String
On Error GoTo finalize
  Open "log-changetracker.txt" For Append As #3
  Print #3, ""
  Print #3, " --| OSM change tracking utility, (C) Zkir 2011"
  
  ParseCommandLine MapCode
  If MapCode = "" Then
    Err.Raise vbObjectError, "Main", "Map code is not specified"
  End If
 
  Print #3, "Source file: " & MapCode
  
  Print #3, "Loading config"
  'Прочтем строку доступа к данным
  Call LoadConfig
  
  'Запускаем обработку
  Call ProcessMap(MapCode)
  
  Print #3, "All done - OK"
  
finalize:
 If Err.Number <> 0 Then
    Print #3, "Error: " & Err.Description
    Close #3
   ' ExitProcess 1
 End If
 Close #3
End Sub
