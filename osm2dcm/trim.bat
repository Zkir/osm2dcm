@echo off

echo trimming file=%1 poly=%2

Set SOURCEFILE=%3
set JUST_COPY=no

set WORK_PATH=d:\OSM\osm_data\_my\%1

echo source=%SOURCEFILE%
echo directcopy=%JUST_COPY%

if "%JUST_COPY%"=="yes" (

osmconvert d:\OSM\osm_data\_src\%SOURCEFILE% -o=%WORK_PATH%\final.full.pbf

) else (

osmconvert d:\OSM\osm_data\_src\%SOURCEFILE% -B=d:\OSM\osm2dcm\poly\%2.poly --complex-ways -o=%WORK_PATH%\final.full.pbf

)
if errorlevel 1 goto error

call osmosis --read-pbf file="%WORK_PATH%\final.full.pbf" --lp  --construction-way daysBeforeOpening=60 daysAfterChecking=60 writeErrorXML="%WORK_PATH%\%1.hwconstr_chk.xml" --tt --write-xml %WORK_PATH%\final.osm 

if errorlevel 1 goto error


rem --------------------------------------------------------------------------------
rem Error handling
rem --------------------------------------------------------------------------------
goto end
:error
echo.
echo error in process
Exit /b 1
:end
Exit /b 0