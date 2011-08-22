@echo off

echo OSM-^>DCM converter script by Zkir 2010
echo converting name=%1 altname=%2 poly=%3 

set WORK_PATH=d:\OSM\osm2dcm\_my\%1


md %WORK_PATH%
del %WORK_PATH%\*.* /q

rem --------------------------------------------------------------------------------
rem Trim osm file
rem --------------------------------------------------------------------------------
echo trimming source file
call trim %~1 %~3 %~4 %~5 
if errorlevel 1 goto error

rem --------------------------------------------------------------------------------
rem Statistic and Change Tracker
rem --------------------------------------------------------------------------------
echo.
echo Calculate statistics
zOsmStat.exe %1 %2
if errorlevel 1 goto error_stat
echo  Statistics - OK
goto change_tracker
:error_stat
echo  Statistics - failed

:change_tracker
echo.
echo Change Tracker
set DO_CT=0
if "%1"=="RU-MOS" (
  set DO_CT=1
)
if "%1"=="RU-SPO" (
  set DO_CT=1
)
if "%1"=="RU-IVA" (
  set DO_CT=1
)
if "%1"=="RU-SVE" (
  set DO_CT=1
)
if "%1"=="RU-KDA" (
  set DO_CT=1
)

echo "%DO_CT%" 
echo "%1" 
if "%DO_CT%"=="1" (
  echo ChangeTracker.exe %1
  ChangeTracker.exe %1 
)


rem --------------------------------------------------------------------------------
rem Convert osm to mp and then mp to dcm
rem --------------------------------------------------------------------------------

echo creating mp from osm
call makemp %~1 %2 %~3 %6 %7
if errorlevel 1 goto error


echo running DCM constructor           %DATE%_%TIME%  version %7
GeoConstructor.exe -mp:%WORK_PATH%\%~1.mp -scamax:1000000 -codepage:1251 -version:1.%8 -uniqueid:%9

if errorlevel 1 goto error
echo DCM constructor has been finished %DATE%_%TIME% - OK

rem --------------------------------------------------------------------------------
rem dcm to archive and put it to ftp
rem --------------------------------------------------------------------------------
del "%WORK_PATH%\%1.rar"
rar a -ep "%WORK_PATH%\%1.rar" "%WORK_PATH%\%1.dcm"


rem --------------------------------------------------------------------------------
rem upload rar archive file to the ftp server 
rem --------------------------------------------------------------------------------
:retry_upload
echo - uploading map to server ...
corecmd.exe -site rambler -O -u %WORK_PATH%\%1.rar   -p www/cg_maps/ -s
if errorlevel 1 goto retry_upload
echo done

echo.
echo Conversion of %1 has finished successfully.

goto end
:error
echo.
echo ERROR HAS OCCURED!!!
Exit 1
:end




