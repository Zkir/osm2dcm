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

if "%SOURCE_FILE%"=="euroroutes.osm" (
del %WORKING_FOLDER%\%SOURCE_FILE%_full
ren %WORKING_FOLDER%\%SOURCE_FILE% %SOURCE_FILE%_full
osmfilter %WORKING_FOLDER%\%SOURCE_FILE%_full --keep= --keep-ways="( highway=*  or route=ferry ) and ( network=e-road or network=e-road:A or ref=E* or int_ref=E* or ref=Å* or int_ref=Å* )" --keep-relations="route=road and ( network=e-road or network=E-road_link or network=e-road_link or ref=E* or int_ref=E* or ref=Å* or int_ref=Å*  ) " --emulate-osmosis >%WORKING_FOLDER%\%SOURCE_FILE%
)

goto end
:error
echo.
echo unable to update source data
echo try to restore old data
rem ren %WORKING_FOLDER%\russia_bak.pbf russia.pbf
rem pause
Exit /b 1
:end