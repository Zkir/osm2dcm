rem @echo off
set MAPID=EU-OVRV
set WORK_PATH=d:\OSM\osm_data\_my\%MAPID%
set SOURCE_FILE=europe.o5m

SET MAP_VERSION=%8
SET MAP_UID=%9


chcp 1251

rem update %SOURCE_FILE%
echo preliminary extraction
osmfilter d:\osm\osm_data\%SOURCE_FILE% --keep= --keep-ways="( highway=*  or route=ferry ) and ( network=e-road or network=e-road:A or ref=E* or int_ref=E* or ref=Е* or int_ref=Е* )" --keep-relations="route=road and ( network=e-road or network=E-road_link or network=e-road_link or ref=E* or int_ref=E* or ref=Е* or int_ref=Е*  ) " --emulate-osmosis >%WORK_PATH%\final.osm

echo transform
call osmosis --read-xml file="%WORK_PATH%\final.osm"  --tt file="transform-EU.xml" --write-xml %WORK_PATH%\final1.osm 

echo final extraction
osmfilter %WORK_PATH%\final1.osm --keep= --keep-ways="E_route_included=yes" --keep-relations="E_route_included=yes" >%WORK_PATH%\final2.osm

echo statistics
rem - note that statistics is calculated by final.osm - its currently hardcoded
zOsmStat.exe %MAPID% "Euroroutes"

echo osm2mp

set OSM_BOUNDARY= --bpoly=d:\OSM\osm2dcm\poly\%MAPID%.poly
perl -S osm2mp_new.pl  --config=osm2mp.config\cityguide.yml --mapid=%MAPID%osm --mapname="%MAPID%(OSM)" --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000 --background  %OSM_BOUNDARY%  --transport=car  %WORK_PATH%\final2.osm >%WORK_PATH%\%MAPID%.pre.mp
if errorlevel 1 goto error

echo postprocessor
rem parametes mean that only euroroutes are used for shields, and no connectivity test by levels
java -jar jmp2mp.jar %WORK_PATH%\%MAPID%.pre.mp %WORK_PATH%\%MAPID%.mp "" "1" "1"
if errorlevel 1 goto error

echo siplify road graph

mp_extsimp.exe %WORK_PATH%\%MAPID%.mp

echo upload statistics
corecmd.exe -site peirce -O -u %WORK_PATH%\%MAPID%.mp_addr.xml   -p ADDR_CHK/ -s
corecmd.exe -site peirce -O -u %WORK_PATH%\%MAPID%_editors.xml   -p ADDR_CHK/ -s


SET MAP_SCALE=2000000

rem Еще одно упрощение
java -Xmx2248m  -jar jmp2mp2.jar --readmp file="%WORK_PATH%\%MAPID%.mp_opt.mp" --forcejunctions --writemp file="%WORK_PATH%\%MAPID%.roads.mp"

ren %WORK_PATH%\%MAPID%.mp %MAPID%.orig.mp
mp2mp %WORK_PATH%\%MAPID%.mp pre.mp\%MAPID%.xml

rem для обзорной карты применяется особые правила - сама обзорная карта не содержит адрески
echo GeoConstructor
echo GeoConstructor.exe -loadrule:d:\osm\Constructor\BASEMAP_OSM.shm  -mp:%WORK_PATH%\%MAPID%.mp -scale:%MAP_SCALE% -scamax:100000000 -codepage:1251 -version:1.%MAP_VERSION% -uniqueid:%MAP_UID%
GeoConstructor.exe -loadrule:d:\osm\Constructor\BASEMAP_OSM.shm  -mp:%WORK_PATH%\%MAPID%.mp -scale:%MAP_SCALE% -scamax:100000000 -codepage:1251 -version:1.%MAP_VERSION% -uniqueid:%MAP_UID%
if errorlevel 1 goto error

echo CGMapToolPublic
CGMapToolPublic.exe Type=CrtCGMap InFile=%WORK_PATH%\%MAPID%.dcm OutFolder=%WORK_PATH%
if errorlevel 1 goto error

echo update web-map
call upd_eudb.bat


echo Everything OK
goto end
:error
echo.
echo ERROR HAS OCCURED!!!
Exit /b 1
:end