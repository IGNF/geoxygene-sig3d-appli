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


## Street profil calculation


![Image of profile result](https://raw.githubusercontent.com/IGNF/geoxygene-sig3d-appli/master/img/StreetProfile.png)


This code (in the package streetprofile) was developed during a Msc training course. The thesis is available [here](http://recherche.ign.fr/labos/cogit/publiCOGITDetail.php?idpubli=5214).

The test class fr.ign.cogit.streetprofile.demo.Main.java is directly runnable as it uses data included in the project.


## Sky openess calculation

![Image of generated geometry](https://raw.githubusercontent.com/IGNF/geoxygene-sig3d-appli/master/img/openess.png )

### Introduction

Sky openess is a 3D spatial indicator that assess the sky visibility from a given point. This indicator may be used to determine the comfort of a urban zone and some characteristics relative to Urban Heat Island effect.

This code (in the package exec) was developed for a research work about the influence of data quality on sky openess calcultion. The paper about this work is available [here](http://recherche.ign.fr/labos/cogit/publiCOGITDetail.php?idpubli=4759&portee=chercheur&id=59&classement=date&duree=100&nomcomplet=Brasebin%20Mickael&annee=2012&principale=)

An executable is available in the folder executable of the project. All the geographic data used during the process are in the shapefile format.

### Inputs

The parameters of the algorithm can be show with the *-h* parameter.

A minimal set of parameters is mandatory :
+ *-buildings filepath* : the file path to the building shapefile. Either geometries are stored as 3D surfaces, either they can be extruded according to an attribute with *-extrude  extrude-attribute* and the minimal z is set to zero.
+ *-points filepath* : the file path to the points from which the calculation is proceeded. Either there are 3D points and the z is used, either they are 2D points and the z can be set at a given altitude with *-z value*
+ *-o <file-out>* : the output points with attributes about sky openess indicators
+ *-output <output-folder>*  : the output folder where all results will be stored

Other parameters are optionnal :
+ About discretization :
 + *-r* <radius> : Radius of raycasting (500 m as default value)
 + *-s* <path> :    Number of points casted for 180° (180 as default value).
+ About geometries export (all geometries are stored in different file in order to save memory during the process) :
 +   *-id <id>* : the id attribute from the points layer. The ID must be unique, this information is used in the filename to store the output geometires.
 +  *-g2D*   : export the 2D geometry of polygon visibility limited by the input radius(what is visible at the height of the point).
  +  *-g3D*   :  export the 3D geometry of what is visible from a point (like in the figure at the begin of this section)

### Algorithm

The method proposed here allows the generation of the 3D geometry resulting from the sky openess calculation. A discrete ray casting is proceed according to a given angular step and the first intersection is considered if the distance if lesser than the given radius.

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
