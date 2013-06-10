@echo off
set MAPID=_world.ovrv
set SOURCE="d:\osm\planet\planet.osm"
set WORK_PATH=d:\OSM\osm2dcm\_my\%MAPID%

call osmosis --read-xml file=%SOURCE%  --node-key-value keyValueList="place.country,place.city,place.town"  --write-xml file="%WORK_PATH%\cities.osm" 


echo osm2mp

perl -S osm2mp_new.pl  --config=osm2mp.config.w\cityguide.yml --mapid=%MAPID%osm --mapname="%MAPID%(OSM)" --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000 --nobackground  --bbox=-180.00,-81,180.00,81.998070  --transport=car  %WORK_PATH%\cities.osm >%WORK_PATH%\%MAPID%.cities.pre.mp



echo postprocessor has been started       %DATE%_%TIME%
mpPostProcessor.exe %WORK_PATH%\%MAPID%.cities.pre.mp %WORK_PATH%\%MAPID%.cities.mp
echo postprocessor has been finished - OK %DATE%_%TIME%



