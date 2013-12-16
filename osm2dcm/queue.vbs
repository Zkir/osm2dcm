'********************************************************************************************
' This script downloads data from OSM, converts it to RUS format and  upoads maps to ftp
' maps.txt is processed.
'
'********************************************************************************************
option explicit



const OSM_FILES_DIR = "D:\OSM\osm_data\"
const OSM_SRC_FILES_DIR = "D:\OSM\osm_data\_src\"
const MAPLIST_XML = "d:\osm\osm_data\queue.xml"

'История/список карт для конвертации
const RS_MAP_CODE="MapCode"
const RS_MAP_CGID="MapID"
const RS_MAP_PRIORITY="Priority"
const RS_MAP_TITLE="MapTitle"
const RS_MAP_LOCTITLE="MapLocTitle"
const RS_MAP_POLY="MapPolyFile"
const RS_MAP_SOURCE="Source"
const RS_MAP_QAMODE="DirectCopy"
const RS_MAP_CUSTOMKEYS="CustomKeys"
const RS_MAP_VIEWPOINT="VIEWPOINT"
const RS_MAP_LAST_TRY_DATE="LastTryDate"
const RS_MAP_DATE="CreationDate"
const RS_MAP_VERSION="MapVersion"
const RS_MAP_USEDTIME="UsedTime" 'Время в минутах, потраченное на карту.
const RS_MAP_NEXT_DATE="NextDate"
const RS_MAP_PLANNED="Planned"



'********************************************************************************************
'Создание  рекордсета со странами, и загрузка в него истории.
'********************************************************************************************
Private Function OpenMapHistory(strFileName)
  Dim rs
  Dim Men
  Dim A
  'создаем объект Recordset
  Set rs = CreateObject("ADODB.Recordset")
  rs.Fields.Append RS_MAP_CODE, 202, 255
  rs.Fields.Append RS_MAP_CGID, 202, 255 
  rs.Fields.Append RS_MAP_PRIORITY, 202, 255 
  rs.Fields.Append RS_MAP_TITLE, 202, 255
  rs.Fields.Append RS_MAP_LOCTITLE, 202, 255
  rs.Fields.Append RS_MAP_POLY, 202, 255
  rs.Fields.Append RS_MAP_SOURCE, 202, 255
  rs.Fields.Append RS_MAP_QAMODE, 202, 255
  rs.Fields.Append RS_MAP_CUSTOMKEYS, 202, 255 
  rs.Fields.Append RS_MAP_VIEWPOINT, 202, 64 
  rs.Fields.Append RS_MAP_LAST_TRY_DATE,7,255
  rs.Fields.Append RS_MAP_DATE, 7, 255
  rs.Fields.Append RS_MAP_VERSION,3
  rs.Fields.Append RS_MAP_USEDTIME, 3
  rs.Fields.Append RS_MAP_NEXT_DATE,7,255
  rs.Fields.Append RS_MAP_PLANNED,3

  rs.Open
  
  Set Men = FileSystemObject.OpenTextFile(strFileName, 1)'
  Do While Men.AtEndOfStream <> True
  TextLine =trim(Men.ReadLine)
  'Wscript.Echo TextLine 
    if left(TextLine,1)<>"#" and TextLine<>""  then
      A=split(textline, "|")
      rs.AddNew
      rs(RS_MAP_CODE)=trim(A(0))
      rs(RS_MAP_CGID)=trim(A(1))
      rs(RS_MAP_PRIORITY)=trim(A(2))
      rs(RS_MAP_LOCTITLE)=trim(A(3))       
      rs(RS_MAP_TITLE)=trim(A(4))    
     
      if rs(RS_MAP_TITLE).value="" then
        rs(RS_MAP_TITLE).value=rs(RS_MAP_LOCTITLE).value
      end if      

      rs(RS_MAP_POLY)=trim(A(5))
      rs(RS_MAP_SOURCE)=trim(A(6))
      rs(RS_MAP_QAMODE)=trim(A(7)) 
      rs(RS_MAP_CUSTOMKEYS)=trim(A(8)) 
      rs(RS_MAP_VIEWPOINT)=trim(A(9))
      rs(RS_MAP_LAST_TRY_DATE)=A(10)
      if trim(A(11))<>"" then
        rs(RS_MAP_DATE)=A(11)
      end if
      rs(RS_MAP_VERSION)=A(12)
      rs(RS_MAP_USEDTIME)=A(13)
    end if
  
Loop
Men.Close 'Закрываем его.
  
  Set OpenMapHistory=rs
End Function


'********************************************************************************************
' Создание Html-файла со списком карт
'********************************************************************************************
private function FormatDateTimeISO(dtDate)
 FormatDateTimeISO=DatePart("yyyy",dtDate) & "-"  & _
                   right("00" & DatePart("m",dtDate),2) & "-"  & right("00" & DatePart("d",dtDate),2) & _
                   " " & right("00" & DatePart("h",dtDate),2) & ":" & right("00" & DatePart("n",dtDate),2)
End Function

private function FormatDateISO(dtDate)
 FormatDateISO=DatePart("yyyy",dtDate) & "-"  & _
                   right("00" & DatePart("m",dtDate),2) & "-"  & right("00" & DatePart("d",dtDate),2) 
End Function

'
'Сохранение истории в xml
'
Private function CreateHtml(rs)
  Dim strHtmlMapList
  
   
    
  strHtmlMapList ="<?xml version=""1.0"" encoding=""windows-1251""?>"  & vbCrLf
  strHtmlMapList = strHtmlMapList & "<maplist>" & vbCrLf
   
  rs.Sort= RS_MAP_NEXT_DATE & "," & RS_MAP_LAST_TRY_DATE 
  rs.MoveFirst 
  Do While Not rs.EOF 
    
    'if FormatDateISO(rs(RS_MAP_DATE).Value)<>"1900-01-01" then
    if FormatDateISO(rs(RS_MAP_DATE).Value) >"2013-01-01" then
      strHtmlMapList = strHtmlMapList & "<map>" & vbCrLf

      strHtmlMapList = strHtmlMapList & "  <code>" & rs(RS_MAP_CODE).Value &  "</code>" & vbCrLf
      if trim (rs(RS_MAP_TITLE).Value)<>"" then 
        strHtmlMapList = strHtmlMapList & "  <name>" & rs(RS_MAP_TITLE).Value &  "</name>" & vbCrLf
      else
        strHtmlMapList = strHtmlMapList & "  <name>" & rs(RS_MAP_LOCTITLE).Value &  "</name>" & vbCrLf
      end if
      strHtmlMapList = strHtmlMapList & "  <name_ru>" & rs(RS_MAP_LOCTITLE).Value &  "</name_ru>" & vbCrLf



      strHtmlMapList = strHtmlMapList & "  <time>" & rs(RS_MAP_USEDTIME) & "</time>" & vbCrLf
      strHtmlMapList = strHtmlMapList & "  <planned>" & rs(RS_MAP_PLANNED) & "</planned>" & vbCrLf
      strHtmlMapList = strHtmlMapList & "  <last_try_date>" & FormatDateTimeISO(rs(RS_MAP_LAST_TRY_DATE).Value) & "</last_try_date>" & vbCrLf
      strHtmlMapList = strHtmlMapList & "  <date>" & FormatDateTimeISO(rs(RS_MAP_DATE).Value) & "</date>" & vbCrLf
      strHtmlMapList = strHtmlMapList & "  <next_date>" & FormatDateTimeISO(rs(RS_MAP_NEXT_DATE).Value) & "</next_date>" & vbCrLf

      
                                         
      strHtmlMapList = strHtmlMapList & "</map>" & vbCrLf
    end if                                  
    rs.MoveNext
  Loop
  
  strHtmlMapList = strHtmlMapList & "</maplist>"  
    
  CreateHtml=strHtmlMapList
End Function

'********************************************************************************************
'Сохрание истории конвертаций в файл
'********************************************************************************************
Private Function MyFormat(str, l)
  str = trim(str)
  if len(str)<l then
    MyFormat= str & Space(l-len(str)) 
  else
    MyFormat=str
  end if    
end function

private function AB(a,b)
 if a=b then
   AB="" 
 else
   AB=b 
 end if

end function




Private Function GetSourceFileDate(strSourceFile)
  dim objFile
  if FileSystemObject.FileExists(OSM_SRC_FILES_DIR & strSourceFile) then
    Set objFile = FileSystemObject.GetFile(OSM_SRC_FILES_DIR & strSourceFile)
    GetSourceFileDate = objFile.datelastmodified
  else
    GetSourceFileDate =CDate("1900-01-01")
  end if 
End Function

'********************************************************************************************
'Основной блок
'********************************************************************************************

Wscript.Echo "Queue analyzer"

dim flPage
Dim strHtmlMapList

Dim FileSystemObject
Set FileSystemObject=CreateObject("scripting.filesystemobject") 

Dim TextLine
Dim rsMapList
Dim rsMapCreationHistory
Dim WshShell
dim dtMapDate
dim dtStartDate
dim dtSourceDate
dim intUsedTime
dim intNewVersion
dim intPeriodicity
dim strSource
dim blnSuccess
dim intUpdateResult
dim AccumulatedTime


Set WshShell = WScript.CreateObject("WScript.Shell")


'Прочтем список карт для конвертации
set rsMapList=OpenMapHistory(OSM_FILES_DIR & "history.txt")
rsMapList.sort = RS_MAP_PRIORITY & " asc, " & RS_MAP_LAST_TRY_DATE & " asc, " & RS_MAP_USEDTIME & " asc"


'Начнем процесс
dtStartDate=Now()
AccumulatedTime=0
rsMapList.MoveFirst 
Do While Not (rsMapList.EOF ) 
    
    strSource=trim(rsMapList(RS_MAP_SOURCE).Value)
    if strSource="" then
      strSource=trim(rsMapList(RS_MAP_CODE).Value) & ".osm"
    end if
 
    dtMapDate=Now()
    dtSourceDate = GetSourceFileDate(strSource)
    'Wscript.Echo rsMapList(RS_MAP_CODE).Value & " " & rsMapList(RS_MAP_SOURCE).Value & " " & dtSourceDate  & " " & rsMapList(RS_MAP_DATE).value  & " " & rsMapList(RS_MAP_LAST_TRY_DATE).value
    

    if  ( (dtSourceDate>rsMapList(RS_MAP_LAST_TRY_DATE).value)  or (rsMapList(RS_MAP_PRIORITY).value=0) and (dtSourceDate<>CDate("1900-01-01"))   )then 
      rsMapList(RS_MAP_NEXT_DATE).value=dtStartDate+(AccumulatedTime/3.0+rsMapList(RS_MAP_USEDTIME).value)/60/24
      AccumulatedTime=AccumulatedTime+rsMapList(RS_MAP_USEDTIME).value  
      rsMapList(RS_MAP_PLANNED)=1
       
    else 
      'Если более нового файла нет, мы просто говорим, что карты в течение восьми (полного цикла) дней могут быть получены.
       rsMapList(RS_MAP_NEXT_DATE).value=dtStartDate+8
       rsMapList(RS_MAP_PLANNED)=0  
    end if   
    rsMapList.MoveNext
  
Loop

 ' Обновим файл очереди на сервере
 ' Сохранение списка карт в xml на основе шаблона
  
  strHtmlMapList=CreateHtml(rsMapList)

  Set flPage = FileSystemObject.OpenTextFile(MAPLIST_XML, 2, True)'
  flPage.WriteLine strHtmlMapList
  flPage.Close 'Закрываем его.
  Set flPage = Nothing  
	  
	 
  'Забросим список карт на сайт 
  ' WshShell.Run "corecmd.exe -site peirce -O -u "& MAPLIST_XML &"  -s" 

  Wscript.Echo "total maps " & rsMapList.recordcount 