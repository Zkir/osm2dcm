@echo off
set SOURCE_FILE=%1
set WORKING_FOLDER=d:\osm\osm_data\_src
set POLY_FILE=d:\osm\osm2dcm\poly.src\%SOURCE_FILE%.poly

echo updating file %SOURCE_FILE%

If Not Exist %WORKING_FOLDER%\%SOURCE_FILE% (
echo: %SOURCE_FILE% does not exist 
echo: lets try to extract it from the planet osm
call extract %SOURCE_FILE% %POLY_FILE%
if errorlevel 1 goto error
)

Echo - updating %SOURCE_FILE%
del %WORKING_FOLDER%\%SOURCE_FILE%_bak
ren %WORKING_FOLDER%\%SOURCE_FILE% %SOURCE_FILE%_bak
osmup %WORKING_FOLDER%\%SOURCE_FILE%_bak %WORKING_FOLDER%\%SOURCE_FILE% --keep-tempfiles -v -B=%POLY_FILE% --tempfiles=%WORKING_FOLDER%\osmupdate_temp\temp 
rem --hour
if errorlevel 1 goto error

echo - delete admin_border file
del %WORKING_FOLDER%\%SOURCE_FILE%.boundaries.osm

goto end
:error
echo.
echo unable to update source data
echo try to restore old data
rem ren %WORKING_FOLDER%\russia_bak.pbf russia.pbf
pause
Exit /b 1
:end