#!/bin/bash
clear

#PostGRES Parameters
host=localhost
port=5432
database=Lyon_Database
username=postgres
password=postgres

set PGPASSWORD=postgres
export PGPASSWORD=$password

BUILDING_FILE=/home/mbrasebin/Documents/Donnees/BDTopoRhone/DONNEES/D06904_AV-3D_LAMB93/E_BATI/BATIMENT.SHP
ROAD_FILE=/home/mbrasebin/Documents/Donnees/BDTopoRhone/DONNEES/D06904_AV-3D_LAMB93/A_VOIES_COMM_ROUTE/TRONCON_ROUTE.shp

echo "Creation of database $database"
echo "Connexion for user : $username"
createdb $database -U $username -h $host -p $port

echo "Extension spatial addition"

psql -U $username  -h $host -p $port -c  $database -c "create extension postgis"

echo "Adding shapefiles"

echo "-- Building shapefile :  $BUILDING_FILE"

shp2pgsql -s 2154 -t '3DZ' -c -D -I -W "latin1"  $BUILDING_FILE public.BUILDINGS | psql -d $database -h $host  -p $port -U $username

echo "-- Road shapefile : $ROAD_FILE "

shp2pgsql -s 2154 -t '3DZ' -c -D -I -W "latin1" $ROAD_FILE  public.ROADS | psql -d $database -h $host  -p $port -U $username 

vartest=`psql -X -A -d $database -U $username -h $host -p $port -t -c "SELECT * FROM public.ROADS limit 10"`

echo vartest




