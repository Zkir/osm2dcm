@echo off
rem set MAPID=RU-NIZ
set MAPID=Test
set WORK_PATH=d:\OSM\osm2dcm\_my\%MAPID%

echo osm2mp
rem perl -S osm2mp_new.pl  --config=cityguide.yml --mapid=osm --mapname="(OSM)" --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000 --background  --bpoly=d:\OSM\osm2dcm\poly\%MAPID%  --transport=car  %WORK_PATH%\final.osm >%WORK_PATH%\%MAPID%.pre.mp

perl -S osm2mp_new.pl  --config=cityguide.yml --mapid=%MAPID%osm --mapname="%MAPID%(OSM)" --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000 --background  --osmbbox  --transport=car  %WORK_PATH%\final.osm >%WORK_PATH%\%MAPID%.pre.mp

echo postprocessor
mpPostProcessor.exe d:\osm\osm2dcm\_my\%MAPID%\%MAPID%.pre.mp d:\osm\osm2dcm\_my\%MAPID%\%MAPID%.mp

echo geoconstructor
"d:\osm\Constructor 1.2.my\GeoConstructor.exe" -mp:%WORK_PATH%\%MAPID%.mp -scamax:1000000 -codepage:1251 -version:1.77 -uniqueid:7777777