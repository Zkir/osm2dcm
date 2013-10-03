echo trim
rem call osmosis --truncate-pgsql host=euroroutes.zkir.ru database=euovrv user=citygyde password=32167

echo  
echo update DB - raw
rem call osmosis --read-xml file="d:\osm\osm2dcm\_my\EU-OVRV\final2.osm" --write-pgsql nodeLocationStoreType=TempFile host=euroroutes.zkir.ru database=euovrv user=citygyde password=32167

echo   
echo update DB gen map
rem java -jar load-highways.jar d:\osm\osm_data\_my\EU-OVRV\EU-OVRV.mp
java -jar load-highways.jar d:\osm\osm_data\_my\EU-OVRV\EU-OVRV.roads.mp
