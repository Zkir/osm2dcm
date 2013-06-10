@echo off
set SOURCE_FILE=%1

set WORKING_FOLDER=d:\osm\osm_data
set POLY_FILE=%2

echo extracting file %SOURCE_FILE%
osmconvert  %WORKING_FOLDER%\planet-m.pbf -o=%WORKING_FOLDER%\_src\%SOURCE_FILE% -B=%POLY_FILE% 

if errorlevel 1 goto error

rem call osmosis --read-pbf file="%WORKING_FOLDER%\planet-m.pbf" --bp file="%POLY_FILE%"  --write-pbf file="%WORKING_FOLDER%\%SOURCE_FILE%_osmosis" 


goto end
:error
echo.
echo unable to update source data
echo try to restore old data
rem ren %WORKING_FOLDER%\russia_bak.pbf russia.pbf
pause
Exit /b 1
:end