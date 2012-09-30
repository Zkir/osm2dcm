@echo off
rem set MAPID=RU-NIZ
set MAPID=Test
set WORK_PATH=d:\OSM\osm2dcm\_my\%MAPID%
chcp 1251

echo osm2mp
rem perl -S osm2mp_new.pl  --config=cityguide.yml --mapid=osm --mapname="(OSM)" --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000 --background  --bpoly=d:\OSM\osm2dcm\poly\%MAPID%  --transport=car  %WORK_PATH%\final.osm >%WORK_PATH%\%MAPID%.pre.mp

perl -S osm2mp_new.pl  --config=osm2mp.config\cityguide.yml --mapid=%MAPID%osm --mapname="%MAPID%(OSM)" --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000 --background  --osmbbox  --transport=car  %WORK_PATH%\final.osm >%WORK_PATH%\%MAPID%.pre.mp
if errorlevel 1 goto error

echo postprocessor
mpPostProcessor.exe %WORK_PATH%\%MAPID%.pre.mp %WORK_PATH%\%MAPID%.mp
if errorlevel 1 goto error

echo qa_release
java -jar qa_release.jar "%WORK_PATH%\%MAPID%.mp_addr.xml"
echo Итог %errorlevel%
if errorlevel 1 goto error

echo geoconstructor
"d:\osm\geoconstructor.342\GeoConstructor.exe" -mp:%WORK_PATH%\%MAPID%.mp -subrouter:5 -scale:200000 -scamax:3000000 -codepage:1251 -version:1.77 -uniqueid:7777777
if errorlevel 1 goto error

echo Everything OK
goto end
:error
echo.
echo ERROR HAS OCCURED!!!
Exit /b 1
:end