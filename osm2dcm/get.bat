@echo off

rem ================================================================================
rem получим данные 
rem ================================================================================

rem call getbz2 russia      http://data.gis-lab.info/osm/russia/rus.osm.bz2
call getbz2 russia      http://data.gis-lab.info/osm_dump/dump/latest/RU.osm.bz2
call getbz2 ukraine     http://data.gis-lab.info/osm/ukraine/ukraine.osm.bz2
call getbz2 georgia     http://data.gis-lab.info/osm/georgia/georgia.osm.bz2
call getbz2 armenia     http://data.gis-lab.info/osm/armenia/armenia.osm.bz2
call getbz2 azerbaijan  http://data.gis-lab.info/osm/azerbaijan/azerbaijan.osm.bz2
call getbz2 kazakhstan  http://data.gis-lab.info/osm/kazakhstan/kazakhstan.osm.bz2 
call getbz2 kyrgyzstan  http://data.gis-lab.info/osm/kyrgyzstan/kyrgyzstan.osm.bz2
call getbz2 uzbekistan  http://data.gis-lab.info/osm/uzbekistan/uzbekistan.osm.bz2

rem ================================================================================
rem  разные страны
rem ================================================================================
call getbz2 moldova      http://download.geofabrik.de/osm/europe/moldova.osm.bz2
call getbz2 finland	 http://download.geofabrik.de/osm/europe/finland.osm.bz2
call getbz2 estonia      http://download.geofabrik.de/osm/europe/estonia.osm.bz2
call getbz2 latvia       http://download.geofabrik.de/osm/europe/latvia.osm.bz2
call getbz2 lithuania    http://download.geofabrik.de/osm/europe/lithuania.osm.bz2
call getbz2 cyprus       http://download.geofabrik.de/osm/europe/cyprus.osm.bz2

rem ================================================================================
rem Беларусь, с фтп
rem ================================================================================

rem curl ftp://188.40.19.246/osm/dumps/belarus.current.preprocessed.osm.bz2 >D:\OSM\osm_data\belarus.osm.bz2
rem bunzip2.exe D:\OSM\osm_data\belarus.osm.bz2 --force

curl http://maps-by.googlecode.com/files/belarus.7z >D:\OSM\osm_data\belarus.7z
7za e D:\OSM\osm_data\belarus.7z -oD:\OSM\osm_data
del D:\OSM\osm_data\belarus.osm
rename D:\OSM\osm_data\77777777.osm belarus.osm

