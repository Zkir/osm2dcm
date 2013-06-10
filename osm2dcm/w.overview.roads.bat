@echo off
set MAPID=_world.ovrv
set SOURCE=d:\osm\planet\europe.osm.pbf
rem set SOURCE=d:\osm\planet\planet-latest.osm.pbf
set WORK_PATH=d:\OSM\osm2dcm\_my\%MAPID%
set %tmp%=d:\_tmp
set %temp%=d:\_tmp

rem Первичная обработка. Дороги и отношения
call osmosis --read-pbf file=%SOURCE% --lp --way-key-value keyValueList="highway.motorway_link,highway.motorway,highway.trunk_link,highway.trunk,highway.primary_link,highway.primary,ferry.trunk,ferry.primary" --tf accept-relations type=restriction,route --used-node --write-xml file="%WORK_PATH%\roads.pre.osm" 
 
rem Поставим специальный тег на европейские маршруты
call osmosis --read-xml file="%WORK_PATH%\roads.pre.osm" --lp --tt --tt file="e-routes.xml" --write-xml file="%WORK_PATH%\roads.pre1.osm"  

rem Отфильтруем европейские маршруты
call osmosis --read-xml file="%WORK_PATH%\roads.pre1.osm" --lp --tf reject-relations --tf accept-ways eroute=yes --used-node outPipe.0=WAYS --read-xml file="%WORK_PATH%\roads.pre1.osm" --tf accept-relations eroute=yes --used-way --used-node outPipe.0=RELS  --merge inPipe.0=WAYS inPipe.1=RELS --write-xml file="%WORK_PATH%\roads.osm" 

echo osm2mp

perl -S osm2mp_new.pl  --config=osm2mp.config\cityguide.yml --mapid=%MAPID%osm --mapname="%MAPID%(OSM)" --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000 --nobackground --bbox=-180.00,-81,180.00,81.998070  --transport=car  %WORK_PATH%\roads.osm >%WORK_PATH%\%MAPID%.roads.pre.mp

echo postprocessor has been started       %DATE%_%TIME%
mpPostProcessor.exe %WORK_PATH%\%MAPID%.roads.pre.mp %WORK_PATH%\%MAPID%.roads.mp
echo postprocessor has been finished - OK %DATE%_%TIME%

rem corecmd.exe -site peirce -O -u %WORK_PATH%\%MAPID%.roads.mp_addr.xml   -p ADDR_CHK/ -s
del "%WORK_PATH%\EU.mp_addr.xml"
ren "%WORK_PATH%\%MAPID%.roads.mp_addr.xml" "EU.mp_addr.xml" 
corecmd.exe -site peirce -O -u %WORK_PATH%\EU.mp_addr.xml   -p ADDR_CHK/ -s

