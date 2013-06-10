rem @echo off
set MAPID=%1
set WORK_PATH=d:\OSM\osm_data\_my\%MAPID%

echo makemp.overview.cities %1 %2 %3 %4

call osmosis --read-pbf file="d:\osm\osm_data\_src\%3" --node-key-value keyValueList="place.city,place.town"  --write-xml file="%WORK_PATH%\cities.osm" 


echo osm2mp

Set CUSTOM_KEYS=%~4

perl -S osm2mp_new.pl  --config=osm2mp.config\cityguide.yml --mapid="%MAPID% (OSM)" --mapname="%~2 (OSM)"  --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000 --background  --bpoly=d:\OSM\osm2dcm\poly\%1.poly   --transport=car %CUSTOM_KEYS%  %WORK_PATH%\cities.osm >%WORK_PATH%\%MAPID%.cities.pre.mp

rem bbox для россии
rem  --bbox=18.00,40.424110,180.00,81.998070


echo postprocessor has been started       %DATE%_%TIME%
rem mpPostProcessor.exe %WORK_PATH%\%MAPID%.cities.pre.mp %WORK_PATH%\%MAPID%.cities.mp
java -jar jmp2mp.jar %WORK_PATH%\%MAPID%.cities.pre.mp %WORK_PATH%\%MAPID%.cities.mp
echo postprocessor has been finished - OK %DATE%_%TIME%



