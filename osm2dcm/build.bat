@echo off

SET LOG=d:\OSM\osm_data\log.txt
echo Начало процесса %DATE%_%TIME% >>%LOG%

rem - запускается файл, который делает конверсию по списку карт
cscript main.vbs

echo Окончание процесса %DATE%_%TIME% >>%LOG%

rem - update the list of maps
rem curl -T "history.txt" -u *****:***** ftp://gis-lab.info/history.txt
rem curl -T "log.txt" -u *****:***** ftp://gis-lab.info/log.txt
SET LOG=




