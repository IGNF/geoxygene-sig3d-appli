package fr.ign.cogit.streetprofile.demo;

import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.streetprofile.model.Profile;

/**
 * Classe de démonstration permettant d'exploiter les résultats du stage de
 * Marina Fund
 *  (recherche.ign.fr/labos/cogit/publiCOGITDetail.php?idpubli=5214) 
 *  
 * Classe de test des fonctions de tracé de séquences urbaine Stage de Marina
 * Fund : USAGE D'INDICATEUR 3D ET AMENAGEMENT URBAIN
 * 
 * Cette classe utilise les fichiers de démonstration et les paramètres définis
 * dans BuildingProfilDemoParamters
 * 
 * 
 * Test class to generate data from Marin Fund trainingship 
 * (recherche.ign.fr/labos/cogit/publiCOGITDetail.php?idpubli=5214)
 * 
 * This class use data declared in BuildingProfilDemoParamters class
 * 
 * @author MFund
 * @author YMeneroux
 * @author MBrasebin
 * @author JPerret
 * 
 */
public class Main {

  public static void main(String[] args) {
	  //Mandatory due to precision trunk in Geoxygene core
	  DirectPosition.PRECISION = 10;
	  
	  //Settings of out folder
	  BuildingProfilExecParamters.FOLDER_OUT = "/home/mickael/temp/";

    Profile profile = new Profile(
        ShapefileReader.read(BuildingProfilExecParamters.FILE_IN_ROADS_DEMO),
        // Set of contigus roads from which the profil is calculated
        ShapefileReader.read(BuildingProfilExecParamters.FILE_IN_BUILDING_DEMO),
        // 3D buildings used
        ShapefileReader.read(BuildingProfilExecParamters.FILE_IN_PARCELS_DEMO),
        // Parcel as input (only buildings in the first parcel are used)
        38, // minimal height of the profile (it may be calculated automatically)
        74, //  maximal height of the profile (it may be calculated automatically)
        4, // step along curvilinear abscissa
        4 // z step for profil calculation
    );

    // Data loading, if parcels have no z they are translated to the minimal z of the scene
    profile.loadData();

    // This lines allows the visualisation of the scene
     profile.display();

    // Calculation of the profilLe
    //The results may be acccessible by getPproj method
    // They are represented by  2D points  with X = curvilinear abscissa et Y = height
    // (the value is positive or negative according to the side of the orad)
    // height is measured according to an origin based on minimal height but my be
    //parametrized by profile.setYProjectionShifting
    profile.process();
 
    //Update in the visualisation if available
  profile.updateDisplay();

    //Point export
    profile.exportPoints(BuildingProfilExecParamters.FOLDER_OUT + BuildingProfilExecParamters.FILE_OUT_POINTS_DEMO);

    //Point export as circle
    profile.exportAsCircle(BuildingProfilExecParamters.FOLDER_OUT + BuildingProfilExecParamters.FILE_OUT_POLYGON_DEMO,
        100);

  }

}
