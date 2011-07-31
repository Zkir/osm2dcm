@echo off

echo trimming file=%1 poly=%2

Set SOURCEFILE=%3
set JUST_COPY=%4

echo source=%SOURCEFILE%
echo directcopy=%JUST_COPY%

if "%JUST_COPY%"=="yes" (
copy "d:\OSM\osm_data\%SOURCEFILE%" "d:\OSM\osm2dcm\_my\%1\final.full.osm"
) else (
call osmosis --read-xml-0.6 file="d:\OSM\osm_data\%SOURCEFILE%" --buffer bufferCapacity=100000 --bounding-polygon-0.6 file="d:\OSM\osm2dcm\poly\%2" completeWays=yes --buffer bufferCapacity=100000  --write-xml-0.6 file="d:\OSM\osm2dcm\_my\%1\final.full.osm"
)

call osmosis --read-xml file="d:\OSM\osm2dcm\_my\%1\final.full.osm" --lp --tf reject-ways source=lsat7-clc2000-i.smap,lsat7-clc2000-grass-i.smap --tf reject-relations source=lsat7-clc2000-i.smap,lsat7-clc2000-grass-i.smap --uwn --tt --write-xml d:\OSM\osm2dcm\_my\%1\final.osm 
