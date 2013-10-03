rem @echo off
echo %~1 %2 %~3 %4 %5 %6 %7 %8 %9

set MAPID=%1
set WORK_PATH=d:\OSM\osm_data\_my\%MAPID%
set PRE_PATH=d:\osm\osm2dcm\pre.mp

SET _country_code=%MAPID:~0,2%
SET _region_code=%MAPID:~3,4%


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

rem Upload validation results
echo upload validation results to production server
corecmd.exe -site peirce -O -u %WORK_PATH%\%MAPID%.mp_addr.xml   -p ADDR_CHK/ -s
corecmd.exe -site peirce -O -u %WORK_PATH%\%MAPID%_editors.xml   -p ADDR_CHK/ -s

echo upload validation results to validator web-UI
corecmd.exe -site peirce2 -O -u %WORK_PATH%\%MAPID%.mp_addr.xml   -p /http/ADDR_CHK/ -s
corecmd.exe -site peirce2 -O -u %WORK_PATH%\%MAPID%_editors.xml   -p /http/ADDR_CHK/ -s


rem - Теперь соберем mp, для конвертации, с упрощенными дорогами.
mp2mp %WORK_PATH%\%1.mp pre.mp\%1.xml
if errorlevel 1 goto error

rem Установка доп хедерных параметров

SET HEADER_PARAMS=Country=%_country_code%
java  -Xmx4248m -jar jmp2mp2.jar --readmp file="%WORK_PATH%\%1.mp" --setheaderparams %HEADER_PARAMS% --writemp file="%WORK_PATH%\%1.mp"


SET MAP_SCALE=1000000
SET MAP_MAX_SCALE=20000000
if "%1"=="RU-OVRV" (
SET MAP_MAX_SCALE=50000000
)

rem для обзорной карты применяется особые правила - сама обзорная карта не содержит адрески
echo GeoConstructor.exe -loadrule:d:\osm\Constructor\BASEMAP_OSM.shm  -mp:%WORK_PATH%\%1.mp -scale:%MAP_SCALE% -scamax:%MAP_MAX_SCALE% -codepage:1251 -version:1.%8 -uniqueid:%9
GeoConstructor.exe -loadrule:d:\osm\Constructor\BASEMAP_OSM.shm  -mp:%WORK_PATH%\%1.mp -scale:%MAP_SCALE% -scamax:%MAP_MAX_SCALE% -codepage:1251 -version:1.%8 -uniqueid:%9
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