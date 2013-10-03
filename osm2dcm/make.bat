@echo off

echo OSM-^>DCM converter script by Zkir 2010
echo converting name=%1 altname=%2 poly=%3 priority %1

set MAPID=%1

set WORK_PATH=d:\OSM\osm_data\_my\%MAPID%
set RELEASE_PATH=d:\OSM\osm_data\_output.cgmap
set RELEASE_PATH_DCM=d:\OSM\osm_data\_output.dcm

md %WORK_PATH%
del %WORK_PATH%\*.* /q


rem --------------------------------------------------------------------------------
rem Alternative maps
rem --------------------------------------------------------------------------------
SET _country_code=%MAPID:~0,2%
SET _region_code=%MAPID:~3,4%

if "%1"=="EU-OVRV" (
  call makemp.eu.bat  %~1 %2 %~3 %4 %5 %6 %7 %8 %9
  if errorlevel 1 goto error
  goto pack_and_upload
)


if "%_region_code%"=="OVRV" (
  call makemp.overview.bat  %~1 %2 %~3 %4 %5 %6 %7 %8 %9
  if errorlevel 1 goto error
  goto pack_and_upload
)



rem --------------------------------------------------------------------------------
rem Get latest updates from OSM
rem --------------------------------------------------------------------------------
echo source: "%~4" 

rem we curently skip update of ru and local-1
goto skip_ru

if "%~4"=="russia.pbf" (
  call update %~4
  if errorlevel 1 goto error
  goto trim
)

if "%~4"=="russia.o5m" (
  call update %~4
  if errorlevel 1 goto error
  goto trim
)

if "%~4"=="local-1.pbf" (
  call call update %~4
  if errorlevel 1 goto error
  goto trim
)

:skip_ru
rem raw data for maps with priority 0 is updated
rem if "%10"=="0" (
rem  echo force update of "%~4"
rem  call d:\osm\osm_data\update.bat %~4
rem  if errorlevel 1 goto error
rem )


rem --------------------------------------------------------------------------------
rem Trim osm file
rem --------------------------------------------------------------------------------
:trim
if NOT "%~4"=="russia.pbf" (
  echo extract boundaries for geocoder
  call getbnd %1 d:\OSM\osm_data\_src\%4
)

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
goto error


:change_tracker
rem nothing here


rem --------------------------------------------------------------------------------
rem Convert osm to mp and then mp to dcm
rem --------------------------------------------------------------------------------

echo creating mp from osm
call makemp %~1 %2 %~3 %6 %7 %~5 d:\OSM\osm_data\_src\%4.boundaries.osm

rem error
if errorlevel 2 goto error

rem Quality test failed
if errorlevel 1 goto error

rem принудительный выход
rem goto error

echo running DCM constructor           %DATE%_%TIME%  version %7
rem для путевых карт (входящих в атлас) предельный масштаб 1.5 млн
rem для монокарт - 20 млн, т.е. такой же как и для атласов 
SET MAP_MAX_SCALE=1500000
if "%_region_code%"=="FULL" (
SET MAP_MAX_SCALE=20000000
)
echo  GeoConstructor.exe -mp:%WORK_PATH%\%~1.mp -subrouter:5 -scale:200000 -scamax:%MAP_MAX_SCALE% -codepage:1251 -version:1.%8 -uniqueid:%9 
GeoConstructor.exe -mp:%WORK_PATH%\%~1.mp -subrouter:5 -scale:200000 -scamax:%MAP_MAX_SCALE% -codepage:1251 -version:1.%8 -uniqueid:%9 
rem -multilevels

if errorlevel 1 goto error
echo DCM constructor has been finished %DATE%_%TIME% - OK

rem для монокарты нужно еще создать индекс-файл для поиска НП
if "%_region_code%"=="FULL" (
CGMapToolPublic.exe Type=CountryTowns InFolder=%WORK_PATH% InFile=%WORK_PATH%\%1.dcm
if errorlevel 1 goto error

7z a -tzip %WORK_PATH%\%1.dcm %WORK_PATH%\%1.sdt
if errorlevel 1 goto error

CGMapToolPublic.exe Type=CrtCGMap InFile=%WORK_PATH%\%1.dcm OutFolder=%WORK_PATH%
if errorlevel 1 goto error
)

:pack_and_upload

rem copy final map to the output folder
copy /Y %WORK_PATH%\%1.cgmap %RELEASE_PATH%
copy /Y %WORK_PATH%\%1.dcm   %RELEASE_PATH_DCM%


rem --------------------------------------------------------------------------------
rem dcm to archive and put it to ftp
rem --------------------------------------------------------------------------------
rem del "%WORK_PATH%\%1.rar"
rem rar a -ep "%WORK_PATH%\%1.rar" "%WORK_PATH%\%1.dcm"


rem --------------------------------------------------------------------------------
rem upload rar archive file to the ftp server 
rem --------------------------------------------------------------------------------


:retry_upload2
echo - uploading map 7 to server ...
corecmd.exe -site rambler -O -u %WORK_PATH%\%1.cgmap   -p /usr/www/peirce/static/cg7_maps/ -s
if errorlevel 1 goto retry_upload2
echo done

echo - delete osm file - they are too big
del %WORK_PATH%\*.osm


echo.
echo Conversion of %1 has finished successfully.

goto end
:error
echo.
echo ERROR HAS OCCURED!!!
Exit 1
:end




