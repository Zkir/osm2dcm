@echo off
rem set MAPID=RU-SVE
rem set MAPID=EU-OVRV
set MAPID=Test
set WORK_PATH=d:\OSM\osm_data\_my\%MAPID%
chcp 1251

echo get exceptions from wiki
curl  "http://wiki.openstreetmap.org/w/api.php?format=xml&action=query&titles=RU:List_of_isolated_subgraphs&prop=revisions&rvprop=content" > IsolatedSubGraphList.xml
curl  "http://wiki.openstreetmap.org/w/api.php?format=xml&action=query&titles=RU:List_of_pseudostreets&prop=revisions&rvprop=content" > PseudoSteetsList.xml


echo osm2mp

set OSM_BOUNDARY= --bpoly=d:\OSM\osm2dcm\poly\%MAPID%.poly
if "%MAPID%"=="Test" (
  set OSM_BOUNDARY= --osmbbox
)
perl -S osm2mp_new.pl  --config=osm2mp.config\cityguide.yml --mapid=%MAPID%osm --mapname="%MAPID%(OSM)" --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000 --background  %OSM_BOUNDARY%  --transport=car  %WORK_PATH%\final.osm >%WORK_PATH%\%MAPID%.pre.mp
if errorlevel 1 goto error


echo geocoding
java  -Xmx2248m -jar jmp2mp2.jar --readmp file="%WORK_PATH%\%MAPID%.pre.mp" --geocode src="d:\OSM\osm2dcm\_my\%MAPID%\boundaries.osm" mapcode="RU-KDA" --writemp file="%WORK_PATH%\%MAPID%.pre.mp"

echo postprocessor
java -jar jmp2mp.jar %WORK_PATH%\%MAPID%.pre.mp %WORK_PATH%\%MAPID%.mp
if errorlevel 1 goto error

echo qa_release
java -jar qa_release.jar "%WORK_PATH%\%MAPID%.mp_addr.xml"
echo Итог %errorlevel%
if errorlevel 1 goto error

echo geoconstructor
"GeoConstructor.exe" -mp:%WORK_PATH%\%MAPID%.mp -subrouter:5 -scale:200000 -scamax:3000000 -codepage:1251 -version:1.77 -uniqueid:7777777
if errorlevel 1 goto error

echo DCM-CGMAP 
"CGMapToolPublic.exe" Type=CrtCGMap InFile=%WORK_PATH%\%MAPID%.dcm OutFolder=%WORK_PATH%
if errorlevel 1 goto error

echo Everything OK
goto end
:error
echo.
echo ERROR HAS OCCURED!!!
Exit /b 1
:end