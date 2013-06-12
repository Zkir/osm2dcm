rem @echo off
echo %~1 %2 %~3 %4 %5 %6 %7 %8 %9

set MAPID=%1
set WORK_PATH=d:\OSM\osm_data\_my\%1
set PRE_PATH=d:\osm\osm2dcm\pre.mp


rem Обзорная карта содержит список дочерних карт. Его надо скопировать.
copy /Y %PRE_PATH%\%1.MapList.txt %WORK_PATH%


call makemp.overview.roads.bat %1 %2 %4 %6
if errorlevel 1 goto error

call makemp.overview.cities.bat %1 %2 %4 %6
if errorlevel 1 goto error


rem - для целей статистики нам нужен final.osm!
call osmosis --rx %WORK_PATH%\cities.osm --rx %WORK_PATH%\roads.osm  --merge --wx %WORK_PATH%\final.osm
echo statistics
rem - note that statistics is calculated by final.osm - its currently hardcoded
zOsmStat.exe %MAPID% %2
if errorlevel 1 goto error

rem - Теперь соберем mp, для конвертации, с упрощенными дорогами.
mp2mp %WORK_PATH%\%1.mp pre.mp\%1.xml
if errorlevel 1 goto error

SET MAP_SCALE=1000000
rem if "%1"=="RU-OVRV" (
rem SET MAP_SCALE=2000000
rem )

rem для обзорной карты применяется особые правила - сама обзорная карта не содержит адрески
echo GeoConstructor.exe -loadrule:d:\osm\Constructor\BASEMAP_OSM.shm  -mp:%WORK_PATH%\%1.mp -scale:%MAP_SCALE% -scamax:50000000 -codepage:1251 -version:1.%8 -uniqueid:%9
GeoConstructor.exe -loadrule:d:\osm\Constructor\BASEMAP_OSM.shm  -mp:%WORK_PATH%\%1.mp -scale:%MAP_SCALE% -scamax:50000000 -codepage:1251 -version:1.%8 -uniqueid:%9
if errorlevel 1 goto error

rem в обзорную карту нужно вложить файл для индекса НП
CGMapToolPublic.exe Type=CountryTowns InFolder=d:\osm\osm_data\_output.dcm InFile=%WORK_PATH%\%1.dcm
if errorlevel 1 goto error

7z a -tzip %WORK_PATH%\%1.dcm %WORK_PATH%\%1.sdt
if errorlevel 1 goto error

CGMapToolPublic.exe Type=CrtCGMap InFile=%WORK_PATH%\%1.dcm OutFolder=%WORK_PATH%
if errorlevel 1 goto error

goto end

rem --------------------------------------------------------------------------------
rem Error handling
rem --------------------------------------------------------------------------------
:error
echo.
echo error in process
Exit /b 99
:end
Exit /b 0