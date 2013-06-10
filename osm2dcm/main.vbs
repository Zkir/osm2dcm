'********************************************************************************************
' This script downloads data from OSM, converts it to RUS format and  upoads maps to ftp
' maps.txt is processed.
'
'********************************************************************************************
option explicit

'Логин:Пароль для загрузки списка карт на сайт
const FTP_SERVER="******"
const FTP_LOGIN_PASSWORD="*****"

'const DOWNLOAD_URL= "http://peirce.osm.rambler.ru/cg_maps"
const DOWNLOAD_URL= "http://peirce.osm.rambler.ru/static/cg7_maps"

const OSM_FILES_DIR = "D:\OSM\osm_data\"
const OSM_SRC_FILES_DIR = "D:\OSM\osm_data\_src\"
const MAPLIST_XML = "d:\osm\osm_data\maplist.xml"
const STATISTICS_XML = "d:\osm\osm_data\statistics.xml"

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

'********************************************************************************************
'Создание карты - запускается бат-файл
'********************************************************************************************
Private Function  ProcessMap(strMapCode, strMapCGID, strMapTitle, strMapLocTitle, strPoly, strSourceFile, strDirectCopy, _
                             strCustomKeys, strViewPoint, dtMapDate, intNewVersion, intPriority)
 dim intResult 
 dim WshShell
 dim strDCMMapTitle

 if trim(strPoly)="" then
   strPoly=strMapCode 
 end if 

 strDCMMapTitle=strMapTitle

 if trim(strDCMMapTitle)="" then
   strDCMMapTitle=strMapLocTitle 
 end if 

  
 Wscript.Echo strMapCode & " " & strMapTitle & " " & strSourceFile
 
 Set WshShell = WScript.CreateObject("WScript.Shell")
 intResult=WshShell.Run( "make.bat " & strMapCode & " """ & strDCMMapTitle & """ " & strPoly & " " & strSourceFile & " " & strDirectCopy & " """ & strCustomKeys & """ """ & strViewPoint & """ " & intNewVersion & " "& strMapCGID & " >>D:\OSM\osm_data\log.txt 2>>&1", 1,TRUE) 
 Wscript.Echo "Result:" & intResult       
 if intResult=0 then 
   ProcessMap= TRUE
 else
   ProcessMap=FALSE
 end if
End Function

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
  rs.Open
  
  Set Men = FileSystemObject.OpenTextFile(strFileName, 1)'
  Do While Men.AtEndOfStream <> True
  TextLine =trim(Men.ReadLine)
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
      rs(RS_MAP_DATE)=A(11)
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
   
  rs.Sort= RS_MAP_LOCTITLE 
  rs.MoveFirst 
  Do While Not rs.EOF 
    
    'if FormatDateISO(rs(RS_MAP_DATE).Value)<>"1900-01-01" then
    if FormatDateISO(rs(RS_MAP_DATE).Value) >"2013-01-01" then
      strHtmlMapList = strHtmlMapList & "<map>" & vbCrLf

      strHtmlMapList = strHtmlMapList & "  <code>" & rs(RS_MAP_CODE).Value &  "</code>" & vbCrLf
      strHtmlMapList = strHtmlMapList & "  <uid>" & rs(RS_MAP_CGID).Value &  "</uid>" & vbCrLf
      if trim (rs(RS_MAP_TITLE).Value)<>"" then 
        strHtmlMapList = strHtmlMapList & "  <name>" & rs(RS_MAP_TITLE).Value &  "</name>" & vbCrLf
      else
        strHtmlMapList = strHtmlMapList & "  <name>" & rs(RS_MAP_LOCTITLE).Value &  "</name>" & vbCrLf
      end if
      strHtmlMapList = strHtmlMapList & "  <name_ru>" & rs(RS_MAP_LOCTITLE).Value &  "</name_ru>" & vbCrLf
      'strHtmlMapList = strHtmlMapList & "  <group>" & "Россия" & "</group>" & vbCrLf
      strHtmlMapList = strHtmlMapList & "  <date>" & FormatDateTimeISO(rs(RS_MAP_DATE).Value) & "</date>" & vbCrLf
      strHtmlMapList = strHtmlMapList & "  <version>1." & rs(RS_MAP_VERSION).Value & "</version>" & vbCrLf
      strHtmlMapList = strHtmlMapList & "  <url>" & DOWNLOAD_URL & "/" & rs(RS_MAP_CODE).Value & ".cgmap" & "</url>" & vbCrLf
      if rs(RS_MAP_CODE).Value = "EU-OVRV" then
        strHtmlMapList = strHtmlMapList & "  <overview>1</overview>" & vbCrLf
      end if 
                                         
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

Private Function SaveMapCreationHistory(rs, strFileName)
  Wscript.Echo  strFileName
  dim Men
  Set Men = FileSystemObject.OpenTextFile(strFileName, 2, True)'
  rs.MoveFirst 
  Do While Not rs.EOF  
    Men.WriteLine MyFormat(rs(RS_MAP_CODE).Value,11)       & " | " & _ 
                  MyFormat(rs(RS_MAP_CGID).Value,11)       & " | " & _
                  MyFormat(rs(RS_MAP_PRIORITY).Value,2)       & " | " & _
                  MyFormat(rs(RS_MAP_LOCTITLE).Value,32)   & " | " & _
                  MyFormat(AB(rs(RS_MAP_LOCTITLE).Value,rs(RS_MAP_TITLE).Value),32)      & " | " & _
                  MyFormat(rs(RS_MAP_POLY).Value,10)       & " | " & _
                  MyFormat(rs(RS_MAP_SOURCE).Value,20)     & " | " & _
                  MyFormat(rs(RS_MAP_QAMODE).Value,3)  & " | " & _
                  MyFormat(rs(RS_MAP_CUSTOMKEYS).Value,45) & " | " & _
                  MyFormat(rs(RS_MAP_VIEWPOINT).Value,20)  & " | " & _
                  MyFormat(rs(RS_MAP_LAST_TRY_DATE).Value,20)       & " | " & _
                  MyFormat(rs(RS_MAP_DATE).Value,20)       & " | " & _
                  MyFormat(rs(RS_MAP_VERSION).Value,3)     & " | " & _
                  MyFormat(rs(RS_MAP_USEDTIME).Value,3)
    
    rs.MoveNext
  Loop
  Men.close
    
 
End Function

'*******************************************************************************************
'Делается запись в истории, список карт на сайте обновляется
'*******************************************************************************************
Private Function UpdateHistoryAndSite(intUsedTime,intNewVersion, blnSuccess, intNewPriority)

	  'Найдем соответствующую запись в истории
	  If not (rsMapCreationHistory.BOF and rsMapCreationHistory.EOF)   then
	    rsMapCreationHistory.MoveFirst
	    rsMapCreationHistory.Find RS_MAP_CODE & "='" & rsMapList(RS_MAP_CODE).Value & "'"
	  end if
	  if rsMapCreationHistory.eof then
	    rsMapCreationHistory.AddNew
	    rsMapCreationHistory(RS_MAP_CODE).value=rsMapList(RS_MAP_CODE).Value
	  end if  
	  rsMapCreationHistory(RS_MAP_TITLE).value=rsMapList(RS_MAP_TITLE).Value
	  rsMapCreationHistory(RS_MAP_LOCTITLE).value=rsMapList(RS_MAP_LOCTITLE).Value
	  rsMapCreationHistory(RS_MAP_POLY).value=rsMapList(RS_MAP_POLY).Value
	  
	  rsMapCreationHistory(RS_MAP_LAST_TRY_DATE).value=dtMapDate
	  rsMapCreationHistory(RS_MAP_PRIORITY).value=intNewPriority
	 
      if blnSuccess then
        rsMapCreationHistory(RS_MAP_DATE).value=dtMapDate
        rsMapCreationHistory(RS_MAP_USEDTIME)=intUsedTime
        rsMapCreationHistory(RS_MAP_VERSION).value=intNewVersion
        
        if rsMapList(RS_MAP_QAMODE).value="no1" then
          rsMapCreationHistory(RS_MAP_QAMODE).value="yes"
        end if
      end if
      
      ' Сохраним историю
	  SaveMapCreationHistory rsMapCreationHistory, OSM_FILES_DIR & "history.txt"
	  SaveMapCreationHistory rsMapCreationHistory, OSM_FILES_DIR & "history-log\history.bak-"&Year(dtMapDate)&Month(dtMapDate)&Day(dtMapDate)&Hour(dtMapDate)&Minute(dtMapDate)
	
      if blnSuccess then
      
	    ' Обновим список карт на сайте
	    ' Сохранение списка карт в html на основе шаблона
	    strHtmlMapList=CreateHtml(rsMapCreationHistory)

	    Set flPage = FileSystemObject.OpenTextFile(MAPLIST_XML, 2, True)'
	    flPage.WriteLine strHtmlMapList
	    flPage.Close 'Закрываем его.
	    Set flPage = Nothing  
	  
	 
	    'Забросим список карт на сайт 
	    WshShell.Run "corecmd.exe -site peirce -O -u "& MAPLIST_XML &"  -s" 

      end if
     'Забросим статистику на сайт 
     WshShell.Run "corecmd.exe -site peirce -O -u "& STATISTICS_XML & "  -s"

End Function

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

Wscript.Echo "OSM-->DCM conversion process"

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

dtStartDate=Now()

Set WshShell = WScript.CreateObject("WScript.Shell")


'Прочтем список карт для конвертации
set rsMapList=OpenMapHistory(OSM_FILES_DIR & "history.txt")

'Прочтем историю конвертации
set rsMapCreationHistory=OpenMapHistory(OSM_FILES_DIR & "history.txt")

'rsMapList.sort = RS_MAP_PRIORITY & " asc, " & RS_MAP_DATE & " asc"
rsMapList.sort = RS_MAP_PRIORITY & " asc, " & RS_MAP_LAST_TRY_DATE & " asc, " & RS_MAP_USEDTIME & " asc"
'rsMapList.sort = RS_MAP_USEDTIME & " asc"

'Начнем процесс
rsMapList.MoveFirst 
Do While Not (rsMapList.EOF or ((Now()-dtStartDate)>0.25)) 
    
    strSource=trim(rsMapList(RS_MAP_SOURCE).Value)
    if strSource="" then
      strSource=trim(rsMapList(RS_MAP_CODE).Value) & ".osm"
    end if
 
    dtMapDate=Now()
    dtSourceDate = GetSourceFileDate(strSource)
    Wscript.Echo rsMapList(RS_MAP_SOURCE).Value & " " & dtSourceDate  & " " & rsMapList(RS_MAP_DATE).value  & " " & rsMapList(RS_MAP_LAST_TRY_DATE).value
 
       

    intPeriodicity= 0.5
 

    intNewVersion=rsMapList(RS_MAP_VERSION).value+1
    
    'Россия обновляется принудительно
    if  ((dtSourceDate>rsMapList(RS_MAP_LAST_TRY_DATE).value) or (strSource="russia.pbf_")  or (strSource="russia.o5m_") or (strSource="local-1.pbf_")) _
        and ( ((dtMapDate-rsMapList(RS_MAP_LAST_TRY_DATE).value) >intPeriodicity) ) or (rsMapList(RS_MAP_PRIORITY).value=0)  then 'карты, которые уже сегодня обновлялись, собирать не надо.
       
      blnSuccess=ProcessMap(rsMapList(RS_MAP_CODE).Value, _
                     rsMapList(RS_MAP_CGID).Value, _
                     rsMapList(RS_MAP_TITLE).Value, _
                     rsMapList(RS_MAP_LOCTITLE).Value, _
                     rsMapList(RS_MAP_POLY).Value, _
                     strSource, _
                     rsMapList(RS_MAP_QAMODE).Value, _
                     rsMapList(RS_MAP_CUSTOMKEYS).Value, _
                     rsMapList(RS_MAP_VIEWPOINT).Value, _
                     dtMapDate, _
                     intNewVersion, _
                     rsMapList(RS_MAP_PRIORITY).value)
         'Если карта собралась
         'Найдем затраченное время в минутах
         intUsedTime=int((Now-dtMapDate)*24*60)
         
         'Приоритет сбрасывается
         if rsMapList(RS_MAP_CODE).Value="EU-OVRV" then
           UpdateHistoryAndSite intUsedTime,intNewVersion, blnSuccess, 1
         else
           UpdateHistoryAndSite intUsedTime,intNewVersion, blnSuccess, 6
         end if
      
    end if   
    rsMapList.MoveNext
  
Loop

