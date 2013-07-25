@echo off
set WORKING_FOLDER=d:\osm\osm_data

del %WORKING_FOLDER%\planet_bak.pbf
ren %WORKING_FOLDER%\planet-m.pbf planet_bak.pbf
osmup %WORKING_FOLDER%\planet_bak.pbf %WORKING_FOLDER%\planet-m.pbf --tempfiles=d:\osm\osm_data\_src\osmupdate_temp\temp -v 
if errorlevel 1 goto error

goto end
:error
echo.
echo unable to update source data
echo try to restore old data
ren %WORKING_FOLDER%\planet_bak.pbf planet-m.pbf
pause
Exit 1
:end