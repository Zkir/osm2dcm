rem @echo off
set MAPID=%1
set WORK_PATH=d:\OSM\osm_data\_my\%MAPID%

echo roads  1=%1 2=%2 3=%3 4=%4

Set CUSTOM_KEYS=%~4


call osmosis --read-pbf file="d:\osm\osm_data\_src\%3" --way-key-value keyValueList="highway.motorway_link,highway.motorway,highway.trunk_link,highway.trunk,highway.primary_link,highway.primary,highway.secondary_link,highway.secondary,ferry.trunk,ferry.primary,ferry.secondary" --used-node idTrackerType=Dynamic --tf accept-relations route=road --write-xml file="%WORK_PATH%\roads.pre.osm" 


call osmosis --read-xml file="%WORK_PATH%\roads.pre.osm" --bounding-polygon file="d:\OSM\osm2dcm\poly\%1.poly" completeWays=yes --lp  --tt --write-xml file="%WORK_PATH%\roads.osm"  


echo osm2mp

perl -S osm2mp_new.pl %CUSTOM_KEYS% --bpoly=d:\OSM\osm2dcm\poly\%1.poly --config=osm2mp.config\cityguide.yml --mapid="%MAPID% (OSM)" --mapname="%~2 (OSM)" --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000  --transport=car  %WORK_PATH%\roads.osm >%WORK_PATH%\%MAPID%.roads.pre.mp


rem полигон для россии  --bbox=18.00,40.424110,180.00,81.998070

echo postprocessor has been started       %DATE%_%TIME%
rem mpPostProcessor.exe %WORK_PATH%\%MAPID%.roads.pre.mp %WORK_PATH%\%MAPID%.roads.mp
java -jar jmp2mp.jar "%WORK_PATH%\%MAPID%.roads.pre.mp" "%WORK_PATH%\%MAPID%.roads.mp" "" "0" "0" "1"


echo postprocessor has been finished - OK %DATE%_%TIME%

rem corecmd.exe -site peirce -O -u %WORK_PATH%\%MAPID%.roads.mp_addr.xml   -p ADDR_CHK/ -s
del "%WORK_PATH%\%MAPID%.mp_addr.xml"
ren "%WORK_PATH%\%MAPID%.roads.mp_addr.xml" "%MAPID%.mp_addr.xml" 



echo road graph simlifier has been started       %DATE%_%TIME%


rem mp_extsimp.exe  %WORK_PATH%\%MAPID%.roads.mp
rem java  -Xmx1500m -jar mp_extsimp.jar %WORK_PATH%\%MAPID%.roads.mp
java  -Xmx1500m -jar jmp2mp2.jar --simplifyroads src="%WORK_PATH%\%MAPID%.roads.mp"
echo road graph simlifier has been finished - OK %DATE%_%TIME%
:End