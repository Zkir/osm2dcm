@echo off
set MAPID=_world.ovrv.1
set SOURCE="d:\osm\planet\planet.osm"
set WORK_PATH=d:\OSM\osm2dcm\_my\%MAPID%
set %tmp%=d:\_tmp
set %temp%=d:\_tmp

call osmosis --read-xml file=%SOURCE% --lp --way-key-value keyValueList="highway.motorway_link,highway.motorway,highway.trunk_link,highway.trunk,ferry.trunk,ferry.primary" --tf reject-relations --used-node --write-xml file="%WORK_PATH%\roads.pre.osm" 
rem call osmosis 

call osmosis --read-xml file="%WORK_PATH%\roads.pre.osm" --lp  --tt --write-xml file="%WORK_PATH%\roads.osm"  


echo osm2mp

perl -S osm2mp_new.pl  --config=osm2mp.config\cityguide.yml --mapid=%MAPID%osm --mapname="%MAPID%(OSM)" --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000 --nobackground --bbox=-180.00,-81,180.00,81.998070  --transport=car  %WORK_PATH%\roads.osm >%WORK_PATH%\%MAPID%.roads.pre.mp

echo postprocessor has been started       %DATE%_%TIME%
mpPostProcessor.exe %WORK_PATH%\%MAPID%.roads.pre.mp %WORK_PATH%\%MAPID%.roads.mp
echo postprocessor has been finished - OK %DATE%_%TIME%

rem corecmd.exe -site peirce -O -u %WORK_PATH%\%MAPID%.roads.mp_addr.xml   -p ADDR_CHK/ -s
del "%WORK_PATH%\W.mp_addr.xml"
ren "%WORK_PATH%\%MAPID%.roads.mp_addr.xml" "W.mp_addr.xml" 
corecmd.exe -site peirce -O -u %WORK_PATH%\W.mp_addr.xml   -p ADDR_CHK/ -s

