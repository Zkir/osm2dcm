rem @echo off
set MAPID=%1
set WORK_PATH=d:\OSM\osm_data\_my\%MAPID%

echo makemp.overview.cities %1 %2 %3 %4

rem call osmosis --read-pbf file="d:\osm\osm_data\_src\%3" --node-key-value keyValueList="place.city,place.town"  --write-xml file="%WORK_PATH%\cities.osm" 

call osmosis ^
   --read-pbf file="d:\osm\osm_data\_src\%3" ^
   --node-key-value keyValueList="place.country,place.region,place.county,place.state,place.city,place.town" outPipe.0=places ^
   --read-pbf file="d:\osm\osm_data\_src\%3" ^
   --tf reject-relations ^
   --tf accept-ways boundary=administrative ^
   --tf accept-ways admin_level=1,2,3,4 ^
   --used-node idTrackerType=Dynamic    outPipe.0=borders ^
   --merge inPipe.0=borders inPipe.1=places ^
   --write-xml file="%WORK_PATH%\cities.osm"



echo osm2mp

Set CUSTOM_KEYS=%~4

perl -S osm2mp_new.pl  --config=osm2mp.config\cityguide.OVRV.yml --mapid="%MAPID% (OSM)" --mapname="%~2 (OSM)"  --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000 --background  --bpoly=d:\OSM\osm2dcm\poly\%1.poly   --transport=car %CUSTOM_KEYS%  %WORK_PATH%\cities.osm >%WORK_PATH%\%MAPID%.cities.pre.mp

rem bbox для россии
rem  --bbox=18.00,40.424110,180.00,81.998070


echo postprocessor has been started       %DATE%_%TIME%
rem mpPostProcessor.exe %WORK_PATH%\%MAPID%.cities.pre.mp %WORK_PATH%\%MAPID%.cities.mp
java -jar jmp2mp.jar %WORK_PATH%\%MAPID%.cities.pre.mp %WORK_PATH%\%MAPID%.cities.mp
echo postprocessor has been finished - OK %DATE%_%TIME%



