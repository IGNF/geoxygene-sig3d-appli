# Geoxygene 3D applications  [![Build Status](https://travis-ci.org/IGNF/geoxygene-sig3d-appli.svg?branch=master)](https://travis-ci.org/IGNF/geoxygene-sig3d-appli)


![3D Image](https://raw.githubusercontent.com/IGNF/geoxygene-sig3d-appli/master/img/3Dimg.png)

This project contains a set of 3D GIS applications developed from GeOxygene3D module from [GeOxygene platform](https://github.com/IGNF/geoxygene). It includes several published works :
 - [Street profil calculation](#street-profil-calculation)   
 - [Sky openess calculation](#sky-openess-calculation)
 - [Generation of hulls and buildings from urban regulation](#Generation-of-hulls-and-buildings-from-urban-regulation)

## Things to know

This is research work, so it is very probable that the project constains some bugs and project is not actively maintained, but do not hesitate to send an e-mail if you need help to use it.

## Contact for questions and feedback

Mickaël Brasebin, [Lastig/COGIT](http://recherche.ign.fr/labos/cogit/cv.php?nom=Brasebin), mickael.brasebin at ign dot fr

Julien Perret, [Lastig/COGIT](http://recherche.ign.fr/labos/cogit/cv.php?nom=Perret), julien.perret at ign dot fr


## Street profile calculation


![Image of profile result](https://raw.githubusercontent.com/IGNF/geoxygene-sig3d-appli/master/img/StreetProfile.png)

### Introduction

The street profile code is dedicated to analyze 3D façade rhythm along a trajectory (a road for example). In order to help in this task, the code aims at calculating a discrete 3D skyline from both part of a street with information (such as ID of the concerned building and distance to the road). The result is exported as a 2D shapefile that allows to visualize and analyze the results in a classical GIS.

This code (in the package streetprofile) was developed during a Msc training course. The thesis is available [here](http://recherche.ign.fr/labos/cogit/publiCOGITDetail.php?idpubli=5214).

A demo class fr.ign.cogit.streetprofile.demo.Main.java is directly runnable as it uses data included in the project.

An executable is available in the folder executable of the project. All the geographic data used during the process are in the 3D shapefile format. Concerning buildings, it requires that the roofs and the facades have to be modelled in a same feature.

The executed code is in the class fr.ign.cogit.exec.ProfileCalculation.

### Algorithm
![Image of the ray casting process](https://raw.githubusercontent.com/IGNF/geoxygene-sig3d-appli/master/img/ProfilAlg.png )

A 3D discretization is proceeded according to a curvilinear abscissa step (*sXY*) and an altitude step (*sZ*) like presented in the left figure. For each point, a ray is casted for each part of the trajectory (the right figure shows this for the left part of the trajectory) for each altitude from the minimal altitude of the scene to the maximal one. The first intersection between the ray and the 3D building layer is stored (if the distance is inferior than *-d* value). If a parcel layer is provided, only the buildings in the first parcel in the direction of the ray are considered in the process.


### Inputs

The parameters of the algorithm can be shown with the *-h* parameter.

Some parameters are mandatory :
 + *-buildings <building-file>* : the file path to the building shapefile.  Either geometries are stored as 3D surfaces (Roofs and Walls must be modelled as a multi-surface in a unique feature), either they can be extruded according to an attribute with *-extrude  extrude-attribute* and the minimal z is set to zero.
 + *-trajectory <trajectory-file>* : the file path to the analyzed trajectory. It must be represented as a LineString. If the file contains several linestrings, they are merged together.
 + *-output <output-folder>*  : the output folder where all results will be stored

Others are optionnal :
   + *-d <maximaldistance>* :  the maximal distance until buildings are considred (50 m by default)
   + *-id <id>*          :  the id attribute from the points layer (ID by default). It is used to indicated which building is visible in a direction.
   + *-o <file-out>* : the output file where points with attributes are stored (by default out.shp)
    + *-parcels <parcel-file>* : a parcel shapefile. By activating this option, at each step of the algorithm. Only buildings that lay inside the first met parcel are kept during the process.
    + *-sXY <StepXY>* : the discretization step according to the curvilinear abscissa (4 m by default)
    + *-sZ <StepZ>* : the discretization according to the altitude (4 m by default).

A basic method to run the program is to execute the following command line :


```
./ProfileCalculation.sh -buildings buildings.shp -trajectory road.shp -output /home/mickael/temp/ -sXY 10-sZ 10 -d 200
    ```

### Outputs

As output, the intersection points are stored as a 2D shapefile. Each point an attribute ID that contains the ID of the building (attribute *-id*) intersected by the ray and an attribute about the length of the ray.




## Sky openess calculation

![Image of generated geometry](https://raw.githubusercontent.com/IGNF/geoxygene-sig3d-appli/master/img/openess.png )

### Introduction

Sky openess is a 3D spatial indicator that assess the sky visibility from a given point. This indicator may be used to determine the comfort of a urban zone and some characteristics relative to Urban Heat Island effect.

This code (in the package exec) was developed for a research work about the influence of data quality on sky openess calcultion. The paper about this work is available [here](http://recherche.ign.fr/labos/cogit/publiCOGITDetail.php?idpubli=4759&portee=chercheur&id=59&classement=date&duree=100&nomcomplet=Brasebin%20Mickael&annee=2012&principale=)

An executable is available in the folder executable of the project. All the geographic data used during the process are in the 3D shapefile format.

The executed code is in the class fr.ign.cogit.exec.SkyOpeness.

### Algorithm

The method proposed here allows the generation of the 3D geometry resulting from the sky openess calculation. A discrete ray casting is proceed according to a given angular step (*-s*) and the first intersection is considered if the distance if lesser than the given radius (*-r*).

### Inputs

The parameters of the algorithm can be shown with the *-h* parameter.

A minimal set of parameters is mandatory :
+ *-buildings filepath* : the file path to the building shapefile. Either geometries are stored as 3D surfaces, either they can be extruded according to an attribute with *-extrude  extrude-attrib### Algorithmute* and the minimal z is set to zero.
+ *-points filepath* : the file path to the points from which the calculation is proceeded. Either there are 3D points and the z is used, either they are 2D points and the z can be set at a given altitude with *-z value*
+ *-o <file-out>* : the output points with attributes about sky openess indicators
+ *-output <output-folder>*  : the output folder where all results will be stored.

Other parameters are optionnal :
+ About discretization :
 + *-r* <radius> : Radius of raycasting (500 m as default value)
 + *-s* <path> :    Number of points casted for 180° (180 as default value).
+ About geometries export (all geometries are stored in different file in order to save memory during the process) :
 +   *-id <id>* : the id attribute from the points layer. The ID must be unique, this information is used in the filename to store the output geometires.
 +  *-g2D*   : export the 2D geometry of polygon visibility limited by the input radius(what is visible at the height of the point).
  +  *-g3D*   :  export the 3D geometry of what is visible from a point (like in the figure at the begin of this section)



A basic method to run the program is to execute the following command line :

```
./SkyOpeness.sh -buildings LOD_BUILDING_2012.shp -points trees.shp -output /temp/ -z 172 -r 100 -s 360 -id gid -g3D -g2D
```
### Outputs

The *<file-out>* is a shapefile with the geometries of the input points. It will contain several statistics about the measures processed during the calculation :
+ *miniRadDis* : distance to nearest object ;
+ *maxRadDis* : distance to furthest object or radius distance ;
+ *avgRadDis* : average distance to objects ;
+ *varRadDis* : std of distance distribution to objects ;
+ *mnRDis2D* : 2D distance to nearest object ;
+ *avgRDis2D* : 3D average distance to objects ;
+ *openess* : sky openess value between 0 and 1 ;
+ *ratioSph* : calculate the ratio of hit sucess / total number of hits ;
+ *visSkySurf* : the surface of visible sky ;
+ *visVol* : the volume of visible sky ;
+ *visVolRa* : the ratio of visible sky to potential visible sky.


If *-g2D* and/or  *-g3D*, shapefiles will be written in the output folder *<output-folder>*. Each shapefile will be named according to *<id>* attribute value.



## Generation of hulls and buildings from urban regulation

![Simulation image](https://raw.githubusercontent.com/IGNF/geoxygene-sig3d-appli/master/img/simul.png)

This code (in the package gru3d) was developed for a research work about assessing constructability from urban local regulation. The paper about this work is available [here](http://recherche.ign.fr/labos/cogit/publiCOGITDetail.php?idpubli=4120&portee=chercheur&id=59&classement=date&duree=100&nomcomplet=Brasebin%20Mickael&annee=2011&principale=)

## System requirements

+ Java (JDK 8 or more recent)
+ Eclipse
+ Maven
The necessary developpers tools are the same as necessary for GeOxygene project, you can find an installation manual [here](http://ignf.github.io/geoxygene/documentation/developer/install.html).

## Acknowledgments


+ This research is supported by the French National Mapping Agency ([IGN](http://www.ign.fr))
+ It is partially funded by the FUI TerraMagna project and by Île-de-France
Région in the context of [e-PLU projet](www.e-PLU.fr)
+ Marina Fund for the development of street profil module
