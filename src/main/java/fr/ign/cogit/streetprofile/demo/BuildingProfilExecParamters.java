package fr.ign.cogit.streetprofile.demo;

/**
* 
* 
* Classe contenant les paramètres nécessaires pour exécuter la demonstration.
* 
* ATTENTION : il est nécessaire pour toutes utilisation de redéfinir la valeur de FOLDER_OUT dans lequel seront sauvegardés les résultats
* 
* 
* @author MFund
* @author YMeneroux
* @author MBrasebin
* @author JPerret
* 
*/
public class BuildingProfilExecParamters {
  
  
 

  //Input folder
  public  static String DEMO_FOLDER = BuildingProfilExecParamters.class.getClassLoader().getResource("./fr/ign/cogit/streetprofile/").toString() ;  
  
  //Building layer (3D shapefile)
  public  static String FILE_IN_BUILDING_DEMO = DEMO_FOLDER + "building.shp";
  //Road layer (3D shapefile)
  public  static String FILE_IN_ROADS_DEMO = DEMO_FOLDER +  "road.shp";
  //Parcel layer (2D shapefile)
  public  static String FILE_IN_PARCELS_DEMO =  DEMO_FOLDER + "parcel.shp";
   
    
  //Output folder
  public  static String FOLDER_OUT = "~/temps";
  
  
  //Output point file
  public  static String FILE_OUT_POINTS_DEMO = "points_out.shp";
  
  //Output polygon file (if generated
  public  static String FILE_OUT_POLYGON_DEMO =  "polygon.shp";
  
  
  //For preprocessing (input generated point folder)
  public  static String FILE_IN_POINTS_DEMO = "points_out.shp";
  
  
  
  


  
  
  

  

  



}
