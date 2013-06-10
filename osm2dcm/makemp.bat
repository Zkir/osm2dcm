@echo off

echo OSM-^>DCM converter script by Zkir 2010


set WORK_PATH=d:\OSM\osm_data\_my\%1



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

if "%1"=="FI-IS" (
  set CONFIG_YML=--config=osm2mp.config\cityguide.FI.yml
)

if "%1"=="FI-LS" (
  set CONFIG_YML=--config=osm2mp.config\cityguide.FI.yml
)

if "%1"=="FI-LL" (
  set CONFIG_YML=--config=osm2mp.config\cityguide.FI.yml
)

if "%1"=="FI-OL" (
  set CONFIG_YML=--config=osm2mp.config\cityguide.FI.yml
)

if "%1"=="FI-ES" (
  set CONFIG_YML=--config=osm2mp.config\cityguide.FI.yml
)

rem кое-какие карты не дозрели до дворовых проездов
rem Сейчас все дозрели
rem if "%1"=="KG-FULL" (
rem  set CONFIG_YML=--config=osm2mp.config.no-service\cityguide.yml
rem )

rem Франция 

if "%1"=="FR-A" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

if "%1"=="FR-B" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

if "%1"=="FR-C" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

if "%1"=="FR-D" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

if "%1"=="FR-E" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

if "%1"=="FR-F" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

if "%1"=="FR-G" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

if "%1"=="FR-H" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

if "%1"=="FR-I" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

if "%1"=="FR-J" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)
if "%1"=="FR-K" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)
if "%1"=="FR-L" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)
if "%1"=="FR-M" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)
if "%1"=="FR-N" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)
if "%1"=="FR-O" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)
if "%1"=="FR-P" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)
if "%1"=="FR-Q" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

if "%1"=="FR-R" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

if "%1"=="FR-S" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

if "%1"=="FR-T" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)

if "%1"=="FR-U" (
  set CONFIG_YML=--config=osm2mp.config.no_buildings\cityguide.yml
)
if "%1"=="FR-V" (
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

echo java  -Xmx2248m -jar jmp2mp2.jar --readmp file="%WORK_PATH%\%1.pre.mp" --geocode src="%7" mapcode="%1" --writemp file="%WORK_PATH%\%1.pre.mp"
java  -Xmx4248m -jar jmp2mp2.jar --readmp file="%WORK_PATH%\%1.pre.mp" --geocode src="%7" mapcode="%1" --writemp file="%WORK_PATH%\%1.pre.mp"

if errorlevel 1 goto error

echo java postprocessor
java -jar jmp2mp.jar %WORK_PATH%\%1.pre.mp %WORK_PATH%\%1.mp %~5
if errorlevel 1 goto error

rem echo vb postprocessor
rem mpPostProcessor.exe %WORK_PATH%\%1.pre.mp %WORK_PATH%\%1.mp %~5 %DO_TESTS% %DO_LITE%
rem if errorlevel 1 goto error
rem echo postprocessor has been finished - OK %DATE%_%TIME%

corecmd.exe -site peirce -O -u %WORK_PATH%\%1.mp_addr.xml   -p ADDR_CHK/ -s
corecmd.exe -site peirce -O -u %WORK_PATH%\%1_editors.xml   -p ADDR_CHK/ -s



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