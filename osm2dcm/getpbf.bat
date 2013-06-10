@echo off
echo %1
curl %2 >D:\OSM\osm_data\%1.pbf.tmp
if errorlevel 1 goto error

del /q D:\OSM\osm_data\%1.pbf
ren D:\OSM\osm_data\%1.pbf.tmp %1.pbf

goto end
rem --------------------------------------------------------------------------------
rem Error handling
rem --------------------------------------------------------------------------------
:error
echo.
echo error in process
Exit /b 1
:end
Exit /b 0