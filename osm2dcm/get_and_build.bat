@echo off

curl http://peirce.gis-lab.info/QualityCriteria.xml >QualityCriteria.xml

:restart
call update europe.o5m
call update russia.pbf 
call update local-1.pbf 
call update sri-lanka.pbf

call build.bat

rem update planet because it's nice to have the fresh planet
rem call upd_w.bat
del /q d:\osm\osm_data\osmupdate_temp\*.*
goto restart
