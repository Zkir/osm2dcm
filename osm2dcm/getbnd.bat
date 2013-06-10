echo getbnd %1 %2
SET MAPID=%1
If Exist %2.boundaries.osm (
goto skip
)
osmosis ^
  --read-pbf file="%2" ^
  --tf reject-relations ^
  --tf accept-nodes place=* ^
  --tf reject-ways outPipe.0=places ^
  --read-pbf file="%2" ^
  --tf accept-relations admin_level=* ^
  --used-way idTrackerType=Dynamic     ^
  --used-node idTrackerType=Dynamic    outPipe.0=borders ^
  --merge inPipe.0=borders inPipe.1=places ^
  --wx %2.boundaries.osm

:skip
