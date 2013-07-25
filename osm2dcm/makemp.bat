@echo off

echo OSM-^>DCM converter script by Zkir 2010

set MAPID=%1
set WORK_PATH=d:\OSM\osm_data\_my\%MAPID%
SET _country_code=%MAPID:~0,2%
SET _region_code=%MAPID:~3,4%


rem --------------------------------------------------------------------------------
rem Convert osm to mp 
rem --------------------------------------------------------------------------------

set CUSTOM_KEYS=%~4
set CONFIG_YML=--config=osm2mp.config\cityguide.yml


if "%1"=="GE-FULL" (
  set CUSTOM_KEYS=--ttable=georgian.decode --nametaglist name:ka,name,ref,int_ref,addr:housenumber 
)





rem ===============================================================================
rem Для финки применяется особый конфиг, потому что у них схема адресации другая.
rem ===============================================================================

if "%_country_code%"=="FI" (
  set CONFIG_YML=--config=osm2mp.config\cityguide.FI.yml
)


rem кое-какие карты не дозрели до дворовых проездов
rem Сейчас все дозрели
rem if "%1"=="KG-FULL" (
rem  set CONFIG_YML=--config=osm2mp.config.no-service\cityguide.yml
rem )

rem Франция 

if "%_country_code%"=="FR" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

Rem Бельгия
if "%_country_code%"=="BE" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

Rem Германия
if "%_country_code%"=="DE" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)


echo Custome keys:
echo %CUSTOM_KEYS%
echo %CONFIG_YML%

echo ---

rem -----
echo -starting osm2mp  %DATE%_%TIME% 

perl -S osm2mp_new.pl  %CONFIG_YML%  --mapid="%~2 (OSM)" --mapname="%~2 (OSM)" --navitel --nointerchange3d  --nomarine --nodestsigns --shorelines --hugesea=1000000 --background  --bpoly=d:\OSM\osm2dcm\poly\%3.poly   --transport car %CUSTOM_KEYS%  %WORK_PATH%\final.osm >%WORK_PATH%\%1.pre.mp 



if errorlevel 1 goto error

rem ----------------------------------------------------------------------------------
rem Постпроцессинг
rem ----------------------------------------------------------------------------------
echo postprocessor has been started       %DATE%_%TIME%

set DO_TESTS=1
set DO_LITE=0


rem ***************************************************************
rem Для некоторых карт QA принудительно отключен
rem ***************************************************************


set QA_ENABLED=1
echo QA %~6 


if "%~6"=="no" (
  set QA_ENABLED=0
)

if "%~6"=="no1" (
  set QA_ENABLED=0
)

echo do tests: %DO_TESTS%
echo QA activated: %QA_ENABLED%


echo additional geocoding
SET HEADER_PARAMS=Country=%_country_code% 
rem if "%_region_code%"=="FULL" (
rem SET HEADER_PARAMS=RegionMap=1 Country=%_country_code% 
rem )

echo java  -Xmx4248m -jar jmp2mp2.jar --readmp file="%WORK_PATH%\%1.pre.mp" --geocode src="%7" mapcode="%1" --setheaderparams %HEADER_PARAMS% --writemp file="%WORK_PATH%\%1.pre.mp"
java  -Xmx4248m -jar jmp2mp2.jar --readmp file="%WORK_PATH%\%1.pre.mp" --geocode src="%7" mapcode="%1" --setheaderparams %HEADER_PARAMS% --writemp file="%WORK_PATH%\%1.pre.mp"

if errorlevel 1 goto error

echo java postprocessor
java -jar jmp2mp.jar %WORK_PATH%\%1.pre.mp %WORK_PATH%\%1.mp %~5
if errorlevel 1 goto error

rem echo vb postprocessor
rem mpPostProcessor.exe %WORK_PATH%\%1.pre.mp %WORK_PATH%\%1.mp %~5 %DO_TESTS% %DO_LITE%
rem if errorlevel 1 goto error
rem echo postprocessor has been finished - OK %DATE%_%TIME%

rem upload validation results
echo upload validation results to production server
corecmd.exe -site peirce -O -u %WORK_PATH%\%1.hwconstr_chk.xml   -p ADDR_CHK/ -s
corecmd.exe -site peirce -O -u %WORK_PATH%\%1.mp_addr.xml   -p ADDR_CHK/ -s
corecmd.exe -site peirce -O -u %WORK_PATH%\%1_editors.xml   -p ADDR_CHK/ -s

rem upload validation results to validator web-UI
echo upload validation results to validator web-UI
corecmd.exe -site peirce2 -O -u %WORK_PATH%\%1.hwconstr_chk.xml   -p /http/ADDR_CHK/ -s
corecmd.exe -site peirce2 -O -u %WORK_PATH%\%1.mp_addr.xml   -p /http/ADDR_CHK/ -s
corecmd.exe -site peirce2 -O -u %WORK_PATH%\%1_editors.xml   -p /http/ADDR_CHK/ -s


if "%QA_ENABLED%"=="0" (
  echo No QA 
  goto end
)

java -jar qa_release.jar %WORK_PATH%\%1.mp_addr.xml
if errorlevel 2 goto error
if errorlevel 1 goto qa_test_failed
goto end

rem --------------------------------------------------------------------------------
rem Error handling
rem --------------------------------------------------------------------------------
:error
echo.
echo error in process
Exit /b 99
:qa_test_failed
echo.
echo qa test failed
Exit /b 1
:end
Exit /b 0