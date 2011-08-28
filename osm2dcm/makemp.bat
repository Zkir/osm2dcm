@echo off

echo OSM-^>DCM converter script by Zkir 2010


set WORK_PATH=d:\OSM\osm2dcm\_my\%1

rem --------------------------------------------------------------------------------
rem Convert osm to mp 
rem --------------------------------------------------------------------------------

set CUSTOM_KEYS=%~4



if "%1"=="GE-FULL" (
  set CUSTOM_KEYS=--ttable=georgian.decode --nametaglist name:ka,name,ref,int_ref,addr:housenumber 
)

if "%1"=="AM-FULL" (
  set CUSTOM_KEYS=--codepage=1251 --translit
)

if "%1"=="AZ-FULL" (
  set CUSTOM_KEYS=--codepage=1251 --translit
)

if "%1"=="canary_islands" (
  set CUSTOM_KEYS=--codepage=1252
)


if "%1"=="FI-SOUTH" (
  rem set CUSTOM_KEYS=--codepage=1252
  set CUSTOM_KEYS=--codepage=1251 --translit
)

if "%1"=="FI-NORTH" (
  rem set CUSTOM_KEYS=--codepage=1252
  set CUSTOM_KEYS=--codepage=1251 --translit
)

if "%1"=="MD-FULL" (
rem set CUSTOM_KEYS=--codepage=1250
  set CUSTOM_KEYS=--codepage=1251 --translit
)

rem Italy islands
if "%1"=="IT-82" (
  set CUSTOM_KEYS=--codepage=1252 --seaback
)

if "%1"=="IT-88" (
  set CUSTOM_KEYS=--codepage=1252 --seaback
)

echo Custome keys:
echo %CUSTOM_KEYS%
echo ---

rem -----
echo -starting osm2mp  %DATE%_%TIME% 

perl -S osm2mp_new.pl  --config=osm2mp.config\cityguide.yml  --mapid=%1_osm --mapname="%~2 (OSM)" --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000 --background  --bpoly=d:\OSM\osm2dcm\poly\%3   --transport car %CUSTOM_KEYS%  %WORK_PATH%\final.osm >%WORK_PATH%\%1.pre.mp 



if errorlevel 1 goto error

rem ----------------------------------------------------------------------------------
rem Постпроцессинг
rem ----------------------------------------------------------------------------------
echo postprocessor has been started       %DATE%_%TIME%
mpPostProcessor.exe %WORK_PATH%\%1.pre.mp %WORK_PATH%\%1.mp %~5
if errorlevel 1 goto error
echo postprocessor has been finished - OK %DATE%_%TIME%
corecmd.exe -site peirce -O -u %WORK_PATH%\%1.mp_addr.xml   -p ADDR_CHK/ -s

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