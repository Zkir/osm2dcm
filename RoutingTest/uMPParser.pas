unit uMPParser;

interface
uses SysUtils,classes,IniFiles;

CONST

ST_IMG_ID='[IMG ID]';
ST_POLYLINE='[POLYLINE]';
ST_POI='[POI]';
ST_POLYGON='[POLYGON]';
ST_COMMENT = 'COMMENT';

type TLatLon=String;

type TMpSection=class
  private
    Attributes:THashedStringList;
    SectionEnding:string;
  public
    SectionType:string;
    Comments:TStringList;

    constructor Create();
    destructor Destroy; Override;
    function GetAttributeValue(strAttrName:string):string;
    procedure SetAttributeValue(strAttrName:string;strAttrValue:string);
    Function GetCoords(): TLatLon;
    Function GetOsmHighway(): String;
    function mpRouteParam():String;
    function mpType():String;
    procedure WriteSection();
end;

type TMpParser=class
  private
     FFile:TextFile;
  public


    constructor Create(const strFileName: String);
    destructor Destroy; Override;
    function ReadNextSection:TMpSection;
    function EOF:Boolean;
end;
Function OSMLevelByTag(Tag: String):Integer;
var tgtFile:TextFile;
implementation
uses uvb6runtime;
constructor TMpSection.Create();
begin
   Comments:=TStringList.create;
   Attributes:=THashedStringList.create;
end;

destructor TMpSection.Destroy;
begin
   Comments.Free;
   Attributes.Free;
end;

function TMpSection.GetAttributeValue(strAttrName:string):string;
begin
  result:=Attributes.Values[strAttrName];
end;

procedure TMpSection.SetAttributeValue(strAttrName:string;strAttrValue:string);
begin
  Attributes.Values[strAttrName]:=strAttrValue;
end;

function TMpSection.mpRouteParam(): String;
begin
  Result := GetAttributeValue('RouteParam');
  If Trim(Result) = '' Then
    Result := GetAttributeValue('RouteParams');
End;

function TMpSection.mpType():String;
begin
  Result := GetAttributeValue('Type');
End;

Function TMpSection.GetCoords(): TLatLon;
var
  strData: String;
  s: String;
  i: Integer;
  Nodes:TStringList;
begin
  i:= 0;
  repeat
    strData := GetAttributeValue('Data' + IntToStr( i));
    i := i + 1;
  Until (strData <> '') Or (i > 7);

  If strData <> '' Then
  begin
    Nodes:= Split(strData, ')');
    s:=Nodes [0];
    If vb6_Left(s, 1) = '(' Then
      s := vb6_Right(s, Length(s) - 1);

  end;
  Result := s;
  Nodes.Free;
End;

//Ищем значение тега Highway. Он сидит в комментариях
Function TMpSection.GetOsmHighway(): String;
var
  strCommentLine:string;
  strValue: String;
  j, i: Integer;

begin
  For j:= 0 To Comments.Count - 1 do
  begin
    strCommentLine := Comments[j];
    i := Pos('highway =',strCommentLine);
    If i > 0 Then
      begin
        // Найдено!
        result := trim(Copy(strCommentLine, i+9));
        break;
      End;
  end;

End;

procedure TMpSection.WriteSection();
var i:integer;
Begin
  if SectionType='BLANK' then
    writeln(tgtFile)
  else
  begin

    for i := 0 to Comments.Count-1  do
      Writeln (tgtFile,Comments[i]);
    if SectionType<>'COMMENT' then
    begin
      Writeln (tgtFile,SectionType);
      for i := 0 to Attributes.Count-1  do
        Writeln (tgtFile,Attributes[i]);

      Writeln (tgtFile,SectionEnding );
    end
    else
      writeln(tgtFile);
  end;
End;

//******************************************************************************
//                             TMpParser
//******************************************************************************
constructor TMpParser.Create(const strFileName: String);
begin

  AssignFile(FFile, strFileName);
  // открытие файла для чтения
  Reset(FFile);

end;

destructor TMpParser.Destroy;
begin
  CloseFile(FFile);
end;

function TMpParser.EOF:Boolean;
begin
  result:=system.Eof(FFile);
end;



function TMpParser.ReadNextSection:TMpSection;
label next_line;
var
  strMpLine:string;
  blnSectionStarted: Boolean;
  blnCommentStarted: Boolean;

begin
  blnSectionStarted:= False;
  blnCommentStarted:= False;

  result:=TMpSection.Create;
  // чтение содержимого файла
  while true do
  begin
    ReadLn(FFile, strMpLine);
    strMpLine:= Trim(strMpLine);

    If (strMpLine = '') And (Not blnSectionStarted) Then
    begin
      If Not blnCommentStarted Then
        result.SectionType:= 'BLANK'
      Else
        // Это комментарий
      ;
      // Так или иначе пустая строчка завершает секцию
      break;
    end;


    //comment
    If vb6_Left(strMpLine, 1) = ';' Then
    begin
      result.SectionType:= 'COMMENT';
      blnCommentStarted:= True;
      result.Comments.Add(strMpLine);
      GoTo next_line;
    End;


    If (vb6_Left(strMpLine, 1) = '[') And (vb6_Right(strMpLine, 1) = ']') Then
    begin
      If Not blnSectionStarted Then
        begin
          result.SectionType:= strMpLine;
          blnSectionStarted:= True;
        end
      Else
        begin
          //Конец секции
          result.SectionEnding:= strMpLine;
          break;
        End;
    end
    Else
      begin
        {//Отфильтруем кавычки, которые СГ не понимает
        strMpLine := Replace(strMpLine, "“", "");
        strMpLine := Replace(strMpLine, "”", "");
        strMpLine := Replace(strMpLine, "„", "");
        strMpLine := Replace(strMpLine, "«", "");
        strMpLine := Replace(strMpLine, "»", "");

        //Прямые удаляет сам Osm2mp

        // Антиёфикация
        strMpLine: = Replace(strMpLine, "Ё", "Е");
        strMpLine: = Replace(strMpLine, "ё", "е");}

        If strMpLine <> '' Then
          result.Attributes.Add(strMpLine);
      end;
next_line:
  end;

end;

Function OSMLevelByTag(Tag: String):Integer;
var intLevel: Integer;
begin
    intLevel := 4;
    if (tag='trunk') or (tag='trunk_link') or  (tag='motorway') or (tag='motorway_link') then
      intLevel := 0;
    if (tag='primary') or (tag='primary_link') then
      intLevel := 1;
    if  (tag='secondary') or (tag='secondary_link') then
      intLevel := 2;
    if  (tag='tertiary') or  (tag='tertiary_link') then
      intLevel := 3;

  result := intLevel

End;

{
 VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
  Persistable = 0  'NotPersistable
  DataBindingBehavior = 0  'vbNone
  DataSourceBehavior  = 0  'vbNone
  MTSTransactionMode  = 0  'NotAnMTSObject
END
Attribute VB_Name = "clsMpSection"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
'***************************************************************************
'Парсинг mp файла
'***************************************************************************

Option Explicit
Const l_grad = 111.321322222222
Const RS_ATTR_NAME = "name"
Const RS_ATTR_VALUE = "value"
Private m_strComments(10000) As String
Public nComments As Integer
Private rsAttributes As ADODB.Recordset
Public SectionType As String
Public SectionEnding As String
Private m_Type As String
Private m_Label As String

Private Sub Class_Initialize()
  SectionType = ""
  SectionEnding = ""
  m_Type = ""
  Set rsAttributes = New ADODB.Recordset
  rsAttributes.Fields.Append RS_ATTR_NAME, adWChar, 255
  rsAttributes.Fields.Append RS_ATTR_VALUE, adWChar, 2048
  rsAttributes.Open
  rsAttributes(RS_ATTR_NAME).Properties("Optimize") = True
  nComments = 0

End Sub

Private Sub Class_Terminate()
  Set rsAttributes = Nothing
End Sub

Private Sub AddAttributeLine(strMpLine As String)
Dim strName
Dim strValue
Dim s() As String
  s = Split(strMpLine, "=", 2)
  strName = s(0)
  strValue = s(1)

  rsAttributes.AddNew
  rsAttributes(RS_ATTR_NAME).Value = Trim$(strName)
  rsAttributes(RS_ATTR_VALUE).Value = Trim$(strValue)
End Sub

Public Sub ReadSection()
  Dim strMpLine As String
  Dim blnSectionStarted As Boolean
  Dim blnCommentStarted As Boolean

  blnSectionStarted = False
  blnCommentStarted = False

  Do
    Line Input #1, strMpLine
    strMpLine = Trim(strMpLine)

    If (strMpLine = "") And (Not blnSectionStarted) Then
      If Not blnCommentStarted Then
        SectionType = "BLANK"
      Else
        ' Это комментарий
      End If
      ' Так или иначе пустая строчка завершает секцию
      Exit Do
    End If


    'comment
    If Left(strMpLine, 1) = ";" Then
      SectionType = "COMMENT"
      blnCommentStarted = True
      If nComments < UBound(m_strComments) Then
        m_strComments(nComments) = strMpLine
        nComments = nComments + 1
      End If
      GoTo next_line
    End If


    If Left(strMpLine, 1) = "[" And Right(strMpLine, 1) = "]" Then
      If Not blnSectionStarted Then
        SectionType = strMpLine
        blnSectionStarted = True
      Else
        'Конец секции
        SectionEnding = strMpLine
        Exit Do
      End If
    Else

      'Отфильтруем кавычки, которые СГ не понимает
      strMpLine = Replace(strMpLine, "“", "")
      strMpLine = Replace(strMpLine, "”", "")
      strMpLine = Replace(strMpLine, "„", "")
      strMpLine = Replace(strMpLine, "«", "")
      strMpLine = Replace(strMpLine, "»", "")

      'Прямые удаляет сам Osm2mp

      ' Антиёфикация
      strMpLine = Replace$(strMpLine, "Ё", "Е", , , vbBinaryCompare)
      strMpLine = Replace$(strMpLine, "ё", "е", , , vbBinaryCompare)

      If strMpLine <> "" Then
        AddAttributeLine strMpLine
      End If
    End If
next_line:
  Loop

End Sub

Public Sub WriteSection()
Dim i As Integer

Select Case SectionType
  Case "COMMENT":
    For i = 0 To nComments - 1
      Print #2, m_strComments(i)
    Next i
    Print #2, "" ' Это потому что коммент заканчивается пустой строчкой.
  Case "BLANK"
    Print #2, ""
  Case Else
    For i = 0 To nComments - 1
      Print #2, m_strComments(i)
    Next i

    Print #2, SectionType

    rsAttributes.MoveFirst
    Do While Not rsAttributes.EOF
      Print #2, rsAttributes(RS_ATTR_NAME) & "=" & rsAttributes(RS_ATTR_VALUE)

      rsAttributes.MoveNext
    Loop

    Print #2, SectionEnding
End Select

End Sub
Public Function strComments(i As Integer) As String
  strComments = m_strComments(i)
End Function
Public Function GetAttributeValue(ByVal strAttributeName As String) As String
Dim strAttributeValue

  rsAttributes.Find RS_ATTR_NAME & "='" & strAttributeName & "'", , adSearchForward, adBookmarkFirst
  If Not rsAttributes.EOF Then
    strAttributeValue = rsAttributes(RS_ATTR_VALUE).Value
  Else
    'empty by default
    strAttributeValue = ""
  End If

  GetAttributeValue = strAttributeValue
End Function

Public Function SetAttributeValue(ByVal strAttributeName As String, ByVal strAttributeValue As String)

  rsAttributes.Find RS_ATTR_NAME & "='" & strAttributeName & "'", , adSearchForward, adBookmarkFirst
  If rsAttributes.EOF Then
    rsAttributes.AddNew
    rsAttributes(RS_ATTR_NAME).Value = strAttributeName
  End If
  rsAttributes(RS_ATTR_VALUE).Value = strAttributeValue

  'Сбросим кеш
  m_Label = ""
  m_Type = ""
End Function

Property Get mpType() As String
  If m_Type = "" Then
    m_Type = GetAttributeValue("Type")
  End If
  mpType = m_Type
End Property
Property Let mpType(strNewType As String)

  SetAttributeValue "Type", strNewType
  m_Type = strNewType
End Property

Property Get mpEndLevel() As Integer

  mpEndLevel = GetAttributeValue("EndLevel")

End Property
Property Let mpEndLevel(intNewValue As Integer)

  SetAttributeValue "EndLevel", intNewValue

End Property

Property Get mpLabel() As String
  If m_Label = "" Then
    m_Label = GetAttributeValue("Label")
  End If
  mpLabel = m_Label
End Property
Property Let mpLabel(strNewLabel As String)

  SetAttributeValue "Label", strNewLabel
  m_Label = strNewLabel
End Property

Public Function GetSize()
Dim strData0 As String
Dim coords() As Double ' массив координат вершин полигона
Dim tmp() As String
Dim strX As String
Dim strY As String
Dim i As Long, N As Long
Dim s As Double
'Найдем размер объекта в квадратных километрах
'предполагаем что Data0 содержит внешний конкурс полигона
  strData0 = GetAttributeValue("Data0")

'Распарсим его.
'Формат
'(x1,y1),(x2,y2),(x3,y3), ...,(xN,yN)
  tmp = Split(strData0, "),")
  N = UBound(tmp)
  ReDim coords(N + 1, 1)
  For i = 0 To N
    strX = Trim$(Split(tmp(i), ",")(0)) 'Широта
    strY = Trim$(Split(tmp(i), ",")(1)) 'Догота
    'Широта

    coords(i, 0) = Right(strX, Len(strX) - 1)


    'Долгота
    If i = N Then
      coords(i, 1) = Left(strY, Len(strY) - 1)
    Else
      coords(i, 1) = strY
    End If

  Next i
  'Убедимся что полигон замкнутый
  If (coords(0, 0) <> coords(N, 0)) Or (coords(0, 1) <> coords(N, 1)) Then
    'Err.Raise vbObjectError, "GetSize", "Polygon is not closed"
    N = N + 1
    coords(N, 0) = coords(0, 0)
    coords(N, 1) = coords(0, 1)
  End If
'Найдем площадь в квадратных градусах
  s = 0
  For i = 0 To N - 1
    s = s + (coords(i, 0) - coords(i + 1, 0)) * (coords(i, 1) + coords(i + 1, 1)) / 2
  Next i

'Переведем площадь из квадратных градусов в км^2 (приближенно)
  s = s * l_grad * l_grad * Cos(coords(0, 0) * 3.141592653 / 180)

  'Знак зависит от направления обхода, но площадь полигона так или иначе положительна
  GetSize = Abs(s)
End Function



Public Sub CalculateBBOX(lat1 As Double, lon1 As Double, lat2 As Double, lon2 As Double)
Dim coords() As String
Dim i As Integer
Dim lat As Double, lon As Double
Dim strData As String

  strData = GetAttributeValue("Data0")

  strData = Replace(strData, "(", "")
  strData = Replace(strData, ")", "")
  coords = Split(strData, ",")

  'Первая точка
  lat1 = coords(0)
  lon1 = coords(1)
  lat2 = coords(0)
  lon2 = coords(1)

  For i = 2 To UBound(coords) Step 2
    lat = coords(i + 0)
    lon = coords(i + 1)

    If lat < lat1 Then lat1 = lat
    If lat > lat2 Then lat2 = lat

    If lon < lon1 Then lon1 = lon
    If lon > lon2 Then lon2 = lon
  Next i

End Sub

'Несколько оптимистичная функция
'OSM ID содержится в первой строке коментария
Public Function GetOsmID() As String
Dim s() As String
Dim strCommentLine
Dim strType As String
Dim strNumber As String
Dim i As Integer

  strCommentLine = m_strComments(0)

  i = InStr(strCommentLine, "NodeID =")
  If i > 0 Then
   strCommentLine = Mid(strCommentLine, i)
  End If

  i = InStr(strCommentLine, "WayID =")
  If i > 0 Then
   strCommentLine = Mid(strCommentLine, i)
  End If

  i = InStr(strCommentLine, "RelID =")
  If i > 0 Then
   strCommentLine = Mid(strCommentLine, i)
  End If


  s = Split(strCommentLine, "=")
  strType = Trim(s(0))
  strNumber = Trim(s(1))
  Select Case strType
    'Точка
    Case "NodeID"
      GetOsmID = "N:" & strNumber
    'Линия
    Case "WayID"
      GetOsmID = "W:" & strNumber
    'Отношение
    Case "RelID"
      GetOsmID = "R:" & strNumber
    Case Else
      Debug.Print strCommentLine
      Err.Raise vbObjectError, "GetOsmID", "Неизвестный тип объекта " & strCommentLine
  End Select

End Function



}

end.
