rem set MAPID=Test
set MAPID=RU-NIZ
set WORK_PATH=d:\OSM\osm2dcm\_my\%MAPID%


echo  test postprocessor/validator
java -jar jmp2mp.jar %WORK_PATH%\%MAPID%.pre.mp %WORK_PATH%\%MAPID%.mp
if errorlevel 1 goto error

