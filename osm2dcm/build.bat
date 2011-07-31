@echo off

SET LOG=log.txt
echo Начало процесса %DATE%_%TIME% >%LOG%

rem Получим правильную историю
rem curl http://osm-russa.narod.ru/history.txt >history.txt

rem - запускается файл, который делает конверсию по списку карт
main.vbs

echo Окончание процесса %DATE%_%TIME% >>%LOG%

rem - update the list of maps
rem curl -T "history.txt" -u *****:***** ftp://gis-lab.info/history.txt
rem curl -T "log.txt" -u *****:***** ftp://gis-lab.info/log.txt
SET LOG=




