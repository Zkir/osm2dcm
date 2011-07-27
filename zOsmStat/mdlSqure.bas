Attribute VB_Name = "mdlSqure"
'*********************************************************************************
'
'Расчет площади многоугольника в кв. градусах по поли-файлу
'
'*********************************************************************************
Option Explicit
Private Function GetLine() As String
Dim textline As String
  textline = ""
  Do
    Line Input #1, textline
  Loop Until textline <> ""
  
  GetLine = textline
End Function


Public Function CalculateSquare(strFileName As String, AvgLat As Double) As Double
Dim X() As Double
Dim Y() As Double

Dim textline As String
Dim i As Integer
Dim j As Integer
Dim N As Integer
Dim strTmp() As String
Dim s As Double
Dim dS As Double

ReDim Preserve X(100000)
ReDim Preserve Y(100000)

  Open strFileName For Input As #1
  ' name
  textline = GetLine
  ' number
  textline = GetLine
  If Trim(textline) <> "1" Then
    Err.Raise vbObjectError, "", "wrong block number"
  End If
  i = 0
  Line Input #1, textline
  Do
    textline = Replace(textline, vbTab, " ")
    strTmp = Split(Trim(textline), " ")
    X(i) = Val(strTmp(0))
    For j = 1 To UBound(strTmp)
     If strTmp(j) <> "" Then
       Y(i) = Val(strTmp(j))
       Exit For
     End If
    Next j
    i = i + 1
    Line Input #1, textline
  Loop While Trim(textline) <> "END"
  Line Input #1, textline
  
  'Ожидаем конец файла
  If Trim(textline) <> "END" Then
    Err.Raise vbObjectError, "", "File is not finished!"
  End If
  
Close #1

'Первый должен быть равен последнему
If (X(0) <> X(i - 1)) Or (Y(0) <> Y(i - 1)) Then
  X(i) = X(0)
  Y(i) = Y(0)
 Else
  i = i - 1 ' Теперь i указывает на последний элемент
End If
N = i
ReDim Preserve X(N)
ReDim Preserve Y(N)

'S = |Sum(x[i] * (y[i-1] - y[i+1]))| / 2 , от i = 0 до i = n-1.
'где (x0, y0) = (xn, yn)
s = 0
AvgLat = 0
For i = 1 To N
 AvgLat = AvgLat + Y(i)
 dS = ((X(i) - X(i - 1)) * (Y(i) + Y(i - 1)))
 s = s + dS
Next i
s = Abs(s / 2)
AvgLat = AvgLat / N

CalculateSquare = s

End Function






