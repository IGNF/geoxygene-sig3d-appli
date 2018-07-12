# Scripts for distribution

These scripts are developed to allow the distribution of Profile process through OpenMole from BDTopo Database. For the distributino, OpenMole needs a set of separated folders with indpendant zones inside. As we aim to cover very large zones, the operation of preparing these folder is made from a PostGIS database.


Thus threee scripts are required :
- **PrepareBase.sh** : Creation of a database structure and import of BDTopo data inside
- **Export.sh** : export the data into a set of folder with necessary data (warning : it is **very** long)
- **Distribution.oms** : OpenMole script to distribute the task (defined in Java project as ProfilTask) for OpenMole.

Distribution.oms requires a OpenMole plugin that can be built with sbt and the script contained in the **build** folder. The code of the process is contained in the class fr.ign.cogit.task.profile.ProfileTask of the project.

The output is a set of folders that corresponds to the input folders with the results of the aglorithm :
- **debug.shp** : the 2D directions and lengths of the casted rays
- **outpoints.csv** : the 3D points obtained by the ray casting
- **outpointsProfile.csv** : the points in the Profile coordinate system
- **patternout.csv** : the detected patterns along a road the fields are : Folder Name; Pattern Side (LEFT or RIGHT); X of the begning of the pattern; LENGTH of the pattern; number of repetition; correlation score
- **output.csv** : it calculates indicators on the whole concerned zone. Fields are :  Folder Name; Side (LEFT or RIGHT); Min height; Max height; Avg height; Med height; STD height; moran indicator

The results can be loaded in a database by using the script fr.ign.cogit.task.profile.database.ImportResultInPostGIS
