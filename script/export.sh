#!/bin/bash
clear

#PostGRES Parameters
host=localhost
port=5432
database=Lyon_Database
username=postgres
password=postgres

road_id=gid
folder_output=./out/

radius=200

set PGPASSWORD=postgres
export PGPASSWORD=$password

echo "Creating : $folder_output"

mkdir -p $folder_output


echo "List roads : SELECT $road_id  FROM public.ROADS"

psql -At -d $database -U $username -h $host -p $port  -c "SELECT $road_id  FROM public.ROADS" | while read -a Record ; do

	echo "---- Creating :$folder_output${Record[0]}"

	mkdir  "$folder_output${Record[0]}"


	echo "---- Writing :  $folder_output${Record[0]}/road.shp with query : select * from ROADS where  $road_id = ${Record[0]}"

	pgsql2shp -f  "$folder_output${Record[0]}/road.shp"  -u $username -h $host -p $port   $database "select * from ROADS where  $road_id = ${Record[0]}"


	echo "---- Getting road geometry : select * from ROADS where  $road_id = ${Record[0]}"

	psql -At -d $database -U $username -h $host -p $port  -c "SELECT ST_AsEWKT(geom)  FROM public.ROADS where gid=${Record[0]}" | while read Record2 ; do

		echo 	"---- Selecting buildings : select * from BUILDINGS where ST_Intersects( ST_Buffer(ST_GeomFromEWKT('${Record2}'), $radius), geom)"
		echo "Exporting buildings : $folder_output${Record[0]}/buildings.shp"

		pgsql2shp -f  "$folder_output${Record[0]}/buildings.shp"  -u $username -h $host -p $port   $database "select * from BUILDINGS where ST_Intersects(ST_Buffer(ST_GeomFromEWKT('${Record2[0]}'), $radius), geom)"
	
	done
	
	
done



echo $vartest


