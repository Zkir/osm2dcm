@echo off
set MAPID=FI-OVRV
set WORK_PATH=d:\OSM\osm_data\_my\%MAPID%
Set SOURCE=local-1.pbf

rem call osmosis --read-pbf file="d:\osm\osm_data\%SOURCE%" --way-key-value keyValueList="place.city,place.town,boundary.administrative,waterway.river,waterway.riverbank,natural.water,natural.coastline" --used-node --write-xml file="%WORK_PATH%\objects.pre.osm" 
rem call osmosis --read-xml file="%WORK_PATH%\objects.pre.osm" --bounding-polygon file="d:\OSM\osm2dcm\poly\%MAPID%.poly" completeWays=yes --lp  --tt --write-xml file="%WORK_PATH%\objects.osm"  


echo osm2mp

perl -S osm2mp_new.pl  --config=osm2mp.config\cityguide.yml --background  --bpoly=d:\OSM\osm2dcm\poly\%MAPID%.poly --mapid=%MAPID%osm --mapname="%MAPID%(OSM)" --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000 --transport=car  %WORK_PATH%\objects.osm >%WORK_PATH%\%MAPID%.objects.pre.mp



