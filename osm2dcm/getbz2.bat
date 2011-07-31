@echo off
echo %1
curl %2 >D:\OSM\osm_data\%1.osm.bz2
bunzip2.exe D:\OSM\osm_data\%1.osm.bz2 --force