package fr.ign.cogit.indicator3D.morpho;



import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.citygml4j.xml.io.reader.CityGMLReadException;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ITriangle;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.convert.FromGeomToSurface;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.gui.MainWindow;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.Context;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.LoaderCityGML;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_AbstractBoundarySurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_AbstractBuilding;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_Building;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_RoofSurface;
import fr.ign.cogit.geoxygene.sig3d.representation.citygml.representation.CG_StyleGenerator;
import fr.ign.cogit.geoxygene.sig3d.semantic.Map3D;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_Polygon;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiSurface;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_OrientableSurface;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Solid;




public class Computation3DIndicators {

  public static void main(String[] args) throws CityGMLReadException, JAXBException {

    // TODO Auto-generated method stub
   // try {

      //Remove geoxygene geometry to lighten memory
      LoaderCityGML.CLEAN_GEOX_GEOM = false;

      //CityGML file Name
      String fileName = "ZoneAExporter.gml";

      //FolderName
      //String folder = "/home/mbrasebin/Documents/Donnees/Paris/EXPORT_1296-13718/export-CityGML/";
      String folder = "/media/paulchapron/Data/DATA-Bati_3D/Paris/EXPORT_1296-13718/export-CityGML/";
      
      String path = folder + fileName;
      
    
      //Folder where image are included
      String folderImage = folder ;

  
      //Texture is loaded or not
      CG_StyleGenerator.LOAD_TEXTURE = true;

      //Level of Detail of the data
      Context.LOD_REP = 2;


  
      
      //Loading CityGLK
      VectorLayer vl = LoaderCityGML.read(new File(path), folderImage, "Layer", true);
    
      
      System.out.println( vl.size() + "  objets dans la scène \n");

      ArrayList<CG_Building> batis = new ArrayList<>() ;
      
      for (IFeature feat : vl ) {
        if(feat instanceof CG_Building) {
          batis.add((CG_Building)feat); 
        }
      }
      
      int nbbatis = batis.size();
      System.out.println(nbbatis + " bâtiments dans la scène");
      
      CG_Building oneOfBati = batis.get(2);
      CG_Building oneOfBati2 = batis.get(5);

    

    //  Double volBati = Compacity.volumeOfCGBuilding(oneOfBati);
      Double volBati2 = Compacity.volumeOfCGBuilding(oneOfBati2);
      
      
      //  System.out.println("volume du bâtiment : " + volBati);
      

      
   //   Creating main window
      MainWindow win = new MainWindow();
     // Getting 3D map
      Map3D carte = win.getInterfaceMap3D().getCurrent3DMap();
      //Adding layer to map
      carte.addLayer(vl);

      // On fabrique les collection d'objets à afficher
      //1er entité le toit et seconde le mur
      List<IFeature> featCDebug = separateRoofAndWall(oneOfBati2);
    
  //    IFeature feat2 = new DefaultFeature(geomExtraction(oneOfBati));
      // Collection prête (1 pour le toti et une pour le mur)
      FT_FeatureCollection<IFeature> featColl = new FT_FeatureCollection<IFeature>();
      featColl.add(featCDebug.get(0));
      
      FT_FeatureCollection<IFeature> featColl2 = new FT_FeatureCollection<IFeature>();
      featColl2.add(featCDebug.get(1));
      //featColl.add(feat2);
      
      
      //On refait une triangulation pour voir sur quelle géométrie on calcule le folume undersurface
      List<ITriangle> lTriangles = Compacity.convertToTriangle(FromGeomToSurface.convertMSGeom(featCDebug.get(0).getGeom()));
   
      //on ajoute à la collection
      IFeature feat3 = new DefaultFeature(new GM_MultiSurface<>(lTriangles));
      FT_FeatureCollection<IFeature> featColl3 = new FT_FeatureCollection<IFeature>();
      featColl3.add(feat3);
      

    

      
      VectorLayer couche = new VectorLayer(featColl,// la collection qui
          // constituera la
          // couche
          "titi", // Le nom de la couche
          true, // Indique qu'une couleur déterminée sera appliquée
          Color.orange, // La couleur à appliquer
          1, // Le coefficient d'opacité
          true// Indique que l'on souhaite une représentation solide et
      // non filaire
      );
      
      
      VectorLayer couche2 = new VectorLayer(featColl2,// la collection qui
              // constituera la
              // couche
              "titi2", // Le nom de la couche
              true, // Indique qu'une couleur déterminée sera appliquée
              Color.BLUE, // La couleur à appliquer
              1, // Le coefficient d'opacité
              true// Indique que l'on souhaite une représentation solide et
          // non filaire
          );
          
      
      
      VectorLayer couche3 = new VectorLayer(featColl3,// la collection qui
              // constituera la
              // couche
              "titi3", // Le nom de la couche
              true, // Indique qu'une couleur déterminée sera appliquée
              Color.GRAY, // La couleur à appliquer
              1, // Le coefficient d'opacité
              true// Indique que l'on souhaite une représentation solide et
          // non filaire
          );
          
      
 
        // deuxième façon pluis fine  : creer représentation des batiments
      //feat.setRepresentation(new ObjectCartoon(feat, Color.pink)); 
      //feat2.setRepresentation(new ObjectCartoon(feat2, Color.red));  
         
        //VectorLayer couche = new VectorLayer(featColl,"Cube");

      
      
      
      carte.addLayer(couche);
      carte.addLayer(couche2);
      carte.addLayer(couche3);
      
      
      simpleHouse3DModel(10., 10., 0., 0., 0.);
      
      System.out.println("tutu titi ");
//      
//  } catch (CityGMLReadException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//  } catch (JAXBException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//  }
 
  }


  public static List<IFeature> separateRoofAndWall(CG_Building b) {
	  
	  List<IFeature> featOut = new ArrayList<>();
	  
	  

	    CG_AbstractBuilding abstractBuilding = (CG_AbstractBuilding) b;
	    IMultiSurface<IOrientableSurface> surfList1 = new GM_MultiSurface<>();
	    
	    IMultiSurface<IOrientableSurface> surfList2 = new GM_MultiSurface<>();
	    
	    for (CG_AbstractBuilding building : abstractBuilding.getConsistsOfBuildingPart()) {
	      List<CG_AbstractBoundarySurface> lABS = building.getBoundedBySurfaces();
	      for (CG_AbstractBoundarySurface aBS : lABS) {
	    	  if(aBS instanceof CG_RoofSurface) {
	    		  surfList1.addAll(aBS.getLod2MultiSurface());
	    		     
	    	  }else {
	    		  surfList2.addAll(aBS.getLod2MultiSurface());
	    		     
	    	  }
	        }
	    }
	    featOut.add(new DefaultFeature(surfList1));
	    featOut.add(new DefaultFeature(surfList2));
	    return featOut;
	  }
  
  
  public static IGeometry geomExtraction(CG_Building b) {

    CG_AbstractBuilding abstractBuilding = (CG_AbstractBuilding) b;
    IMultiSurface<IOrientableSurface> surfList = new GM_MultiSurface<>();
    for (CG_AbstractBuilding building : abstractBuilding.getConsistsOfBuildingPart()) {
      List<CG_AbstractBoundarySurface> lABS = building.getBoundedBySurfaces();
      for (CG_AbstractBoundarySurface aBS : lABS) {
        surfList.addAll(aBS.getLod2MultiSurface());
      }
    }
    return surfList;
  }

  
  // crée une maison (cube + toit triangulaire)  calée sur un point "en bas à gauche" de coordonnees X0Y0Z0,
  // le plancher est à l'altitude Zoffset
  public static GM_Solid simpleHouse3DModel(Double side, Double zOffset, Double X0, Double Y0, Double Z0) {
    List<DirectPositionList> faces = new ArrayList<DirectPositionList>();
    
   // IntStream.range(1, 6)
    //.forEach(x -> faces.add(new DirectPositionList()));
  
    //face  plancher 
    DirectPositionList LFace1 = new DirectPositionList();
    //face du grenier
    DirectPositionList LFace2 = new DirectPositionList();
  //face à droite  
    DirectPositionList LFace3 = new DirectPositionList();
    //face au fond  
    DirectPositionList LFace4 = new DirectPositionList();
    //face à gauche  
    DirectPositionList LFace5 = new DirectPositionList();
  //face devant  
    DirectPositionList LFace6 = new DirectPositionList();
    
    
    faces.add(LFace1);
    faces.add(LFace2);
    faces.add(LFace3);
    faces.add(LFace4);
    faces.add(LFace5);
    faces.add(LFace6);
    
    
    for (int i = 0 ; i< 4 ; i ++) {
      Double X = X0 + (i %2) * side ; 
      Double Y = Y0 + (i/2) * side ; 
      LFace1.add(new DirectPosition(X,Y, zOffset   ));
      LFace2.add(new DirectPosition(X,Y, (zOffset+ side)   ));
    }
    // on swappe les éléments 3 et 2 pour avoir les points dans le bon ordre 
    LFace1.permuter(3, 2);
    LFace2.permuter(3, 2);
    //on ajoute le premier point pour boucler la face
    LFace1.add(LFace1.get(0));
    LFace2.add(LFace2.get(0));
    
    
    
    
    // index des points des faces plancher et grenier pour faire les faces des murs 
    List<Integer> idx= Arrays.asList(1,2,2,1,1);
    LFace3.setList(Arrays.asList(LFace1.get(idx.get(0)), LFace1.get(idx.get(1)), LFace2.get(idx.get(2)), LFace2.get(idx.get(3)), LFace1.get(idx.get(4))));
    
          
    idx.stream().map(x -> (x +1)%4 );
    LFace4.setList(Arrays.asList(LFace1.get(idx.get(0)), LFace1.get(idx.get(1)), LFace2.get(idx.get(2)), LFace2.get(idx.get(3)), LFace1.get(idx.get(4))));
    
    idx.stream().map(x -> (x +1)%4 );
    LFace5.setList(Arrays.asList(LFace1.get(1), LFace1.get(2), LFace2.get(2), LFace2.get(1), LFace1.get(1)));
    

    idx.stream().map(x -> (x +1)%4 );
    LFace6.setList(Arrays.asList(LFace1.get(1), LFace1.get(2), LFace2.get(2), LFace2.get(1), LFace1.get(1)));
    

    
    // affichage coords
    faces.stream()
    .map(x->{
        System.out.println("_________FACE"+(faces.indexOf(x) + 1 )+"________");
        return x;})
    .forEach(System.out::println);
    
    
    
    //creation cube
  ArrayList<IOrientableSurface> listeFaces = 
  faces.stream()
  .map(x-> new GM_LineString(x))
      .map(x->(GM_OrientableSurface) new GM_Polygon(x))
      .collect(Collectors.toCollection(ArrayList::new));
        
    return new GM_Solid(listeFaces);
  }
  

}


