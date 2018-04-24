package fr.ign.cogit.indicator3D.morpho;



import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.bind.JAXBException;

import org.citygml4j.model.citygml.building.RoofSurface;
import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.postgis.LineString;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ITriangle;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.convert.FromGeomToSurface;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.feature.SchemaDefaultFeature;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.AttributeType;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.FeatureType;
import fr.ign.cogit.geoxygene.sig3d.calculation.Util;
import fr.ign.cogit.geoxygene.sig3d.gui.MainWindow;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.Context;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.LoaderCityGML;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_Building;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_AbstractBoundarySurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_AbstractBuilding;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_Building;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_RoofSurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_WallSurface;
import fr.ign.cogit.geoxygene.sig3d.representation.citygml.representation.CG_StyleGenerator;
import fr.ign.cogit.geoxygene.sig3d.representation.sample.ObjectCartoon;
import fr.ign.cogit.geoxygene.sig3d.semantic.Map3D;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_Polygon;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiSurface;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_OrientableSurface;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Solid;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;




public class Computation3DIndicators {

  public static void main(String[] args) throws CityGMLReadException, JAXBException {

    // TODO Auto-generated method stub
 try {
     
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
      CG_StyleGenerator.LOAD_TEXTURE = false;
      //Level of Detail of the data
      Context.LOD_REP = 2;

      
      System.out.println("loading GML");
      //Loading CityGLK
      VectorLayer vl = LoaderCityGML.read(new File(path), folderImage, "Layer", true);
    
      
      
      System.out.println( vl.size() + "  objets dans la scène \n");
      ArrayList<CG_Building> batis = new ArrayList<>() ;
      
      for (IFeature feat : vl ) {
        if(feat instanceof CG_Building) {
          batis.add((CG_Building)feat); 
        }
        else {
      //    System.out.println("autre type: " + feat.getClass());
        }
      }
      
      int nbbatis = batis.size();
      System.out.println(nbbatis + " bâtiments dans la scène");
      
      CG_Building oneOfBati = batis.get(2);
      CG_Building oneOfBati2 = batis.get(5);

    
      
      
   //   Creating main window
      MainWindow win = new MainWindow();
       
      //Getting 3D map
     Map3D carte = win.getInterfaceMap3D().getCurrent3DMap();

     AttributeManager.addAttribute(oneOfBati2, "titi", new Integer(10), "Integer");
     System.out.println("OKOKOKOKOKOKOK");
     
     
     carte.addLayer(vl);
     
     computeCompacities(batis);
     

      // On fabrique les collection d'objets à afficher
      //1er entité le toit et seconde le mur
      List<IFeature> featCDebug = separateRoofAndWall(oneOfBati2);
    
      
      // Collection prête (1 pour le toit et une pour le mur)
      FT_FeatureCollection<IFeature> featCollToit = new FT_FeatureCollection<IFeature>();
      featCollToit.add(featCDebug.get(0));
      
      FT_FeatureCollection<IFeature> featCollMurs = new FT_FeatureCollection<IFeature>();
      featCollMurs.add(featCDebug.get(1));

      
      
      //On refait une triangulation pour voir sur quelle géométrie on calcule le volume undersurface
      List<ITriangle> lTrianglesToit = Compacity.convertToTriangle(FromGeomToSurface.convertMSGeom(featCDebug.get(0).getGeom()));
   
      //on ajoute à la collection
      IFeature feat3 = new DefaultFeature(new GM_MultiSurface<>(lTrianglesToit));
      FT_FeatureCollection<IFeature> featCollTrianglesToit = new FT_FeatureCollection<IFeature>();
      featCollTrianglesToit.add(feat3);
      

           
      
      

//          VectorLayer couche = new VectorLayer(featCollToit,// la collection qui
//          // constituera la
//          // couche
//          "Toit", // Le nom de la couche
//          true, // Indique qu'une couleur déterminée sera appliquée
//          Color.orange, // La couleur à appliquer
//          1, // Le coefficient d'opacité
//          true// Indique que l'on souhaite une représentation solide et
//      // non filaire
//      );
//      
        
      
//      VectorLayer couche2 = new VectorLayer(featCollMurs,// la collection qui
//              // constituera la
//              // couche
//              "Mur", // Le nom de la couche
//              true, // Indique qu'une couleur déterminée sera appliquée
//              Color.BLUE, // La couleur à appliquer
//              1, // Le coefficient d'opacité
//              true// Indique que l'on souhaite une représentation solide et
//          // non filaire
//          );
          
      
      
//      VectorLayer couche3 = new VectorLayer(featCollTrianglesToit,// la collection qui
//              // constituera la
//              // couche
//              "Toit triangles", // Le nom de la couche
//              true, // Indique qu'une couleur déterminée sera appliquée
//              Color.GRAY, // La couleur à appliquer
//              1, // Le coefficient d'opacité
//              true// Indique que l'on souhaite une représentation solide et
//          // non filaire
//          );

      
      //carte.addLayer(couche);
      //carte.addLayer(couche2);
      //carte.addLayer(couche3);
      
      


      // geom.getList sort les surface orientable à partir de la geométrie
      //Désormais on triangule la surface avant de faire appel à cette fonction
      double volContrib = Util.volumeUnderSurface((lTrianglesToit));
      
      System.out.println("volume calculé avec compacity.Volume " + Compacity.volumeOfCGBuilding(oneOfBati2));

     
  /*    
      VectorLayer vl2= new VectorLayer("CubeTest") ;
      

      
      GM_Solid cubeTest = simpleHouse3DModel(100., 10., 0., 0., 0.);
      IFeature featCube = new DefaultFeature(cubeTest);
      featCube.setRepresentation(new ObjectCartoon(featCube, Color.orange )); 
      

      //System.out.println(Compacity.volumeOfCGBuilding(cube));
      
      
      
      vl2.add(featCube) ;
      

      
      //   Creating main window
         MainWindow win = new MainWindow();
          
         //Getting 3D map
        Map3D carte = win.getInterfaceMap3D().getCurrent3DMap();
      
      
      carte.addLayer(vl2);
*/    
      System.out.println("tutu titi ");
 } 
      catch (CityGMLReadException e) {
//       TODO Auto-generated catch block
      e.printStackTrace();
  } catch (JAXBException e) {
  //     TODO Auto-generated catch block
      e.printStackTrace();
  }
  
  
  }


  // toit en position 0 , murs en position 1 
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
  

  
  public static void  computeCompacities( ArrayList<CG_Building> batis) {
    
    
    Double compMax = Double.NEGATIVE_INFINITY ;
    Double compMin = Double.POSITIVE_INFINITY ;
    
    /*
    FeatureType BatiFeatureType = new FeatureType();
    BatiFeatureType.setTypeName("Bati3DParis");
    BatiFeatureType.setGeometryType(GM_MultiSurface.class);
    
    AttributeType compacitySphere = new AttributeType("compacitySphere", "compacitySphere", "Double");
    AttributeType compacityCube = new AttributeType("compacityCube", "compacityCube", "Double");
    AttributeType compacityDemiSphere = new AttributeType("compacityDemiSphere", "compacityDemiSphere", "Double");
    
    
    BatiFeatureType.addFeatureAttribute(compacitySphere);
    BatiFeatureType.addFeatureAttribute(compacityDemiSphere);
    BatiFeatureType.addFeatureAttribute(compacityCube);
    
    
    
    
    // Création d'un schéma associé au featureType
    SchemaDefaultFeature schemaBati = new SchemaDefaultFeature();
    schemaBati.setFeatureType(BatiFeatureType);


    Map<Integer, String[]> attLookup = new HashMap<Integer, String[]>(0);
    attLookup.put(new Integer(0), new String[] { compacitySphere.getNomField(), compacitySphere.getMemberName() });
    attLookup.put(new Integer(1), new String[] { compacityDemiSphere.getNomField(), compacityDemiSphere.getMemberName() });
    attLookup.put(new Integer(2), new String[] { compacityCube.getNomField(), compacityCube.getMemberName() });
    schemaBati.setAttLookup(attLookup);
    
    
    BatiFeatureType.setSchema(schemaBati);
    */
    
    Integer nbbatis= batis.size(); 
    
    
    for (CG_Building b : batis) {
    
      Double vol = Compacity.volumeOfCGBuilding(b);
      Double surf  = Compacity.surfaceOfCGBuilding(b);
      Double comp = Compacity.RelativeCompacityDemiSphere(vol, surf);
     if( comp < compMin) {
       compMin = comp;
     }
     if (comp > compMax) {
       compMax = comp;
     }
      //b.setFeatureType(BatiFeatureType);  
   
      CG_AbstractBuilding abstractB= (CG_AbstractBuilding) b;
      System.out.println("=#=#=#=#=#=#=#=#=#=#" +b.getGeom()+ " bati  " + batis.indexOf(b)+"/" + nbbatis);
 
      AttributeManager.addAttribute(abstractB, "compacityDemiSphere",  comp, "Double");
      System.out.println("batiment attribué ");
    
    }   
    System.out.println("compacités calculées !" );
 
    
    
  }
  
  

  
  // crée une maison (cube + toit triangulaire)  calée sur un point "en bas à gauche" de coordonnees X0Y0Z0,
  // le plancher est à l'altitude Zoffset
  public static GM_Solid simpleHouse3DModel(Double side, Double zOffset, Double X0, Double Y0, Double Z0) {
    List<DirectPositionList> faces = new ArrayList<DirectPositionList>();
    
    IntStream.range(0, 6)
    .forEach(x -> faces.add(new DirectPositionList()));
  
    //on sort le plancher 0 et le grenier 1
    DirectPositionList LFace1 = faces.get(0);
    DirectPositionList LFace2 = faces.get(1);
    
   
    
    
    for (int i = 0 ; i< 4 ; i ++) {
      Double X = X0 + (i %2) * side ; 
      Double Y = Y0 + (i/2) * side ; 
      LFace1.add(new DirectPosition(X,Y, zOffset   ));
      LFace2.add(new DirectPosition(X,Y, (zOffset+ side)   ));
    }
    // on swappe les éléments 3 et 2 pour avoir les points dans le bon ordre 
    LFace1.permuter(3, 2);
    LFace2.permuter(3, 2);
    //on ajoute le premier point en bout de liste pour boucler la face
    LFace1.add(LFace1.get(0));
    LFace2.add(LFace2.get(0));
    
    
//    DirectPositionList LFace3 = faces.get(2); //face a droite
//    DirectPositionList LFace4 = faces.get(3); //face derrière
//    DirectPositionList LFace5 = faces.get(4); //face à gauche 
//    DirectPositionList LFace6 = faces.get(5); // face devant 
    
    // index des points des faces plancher et grenier pour faire les faces des murs 
    // on commence par la droite, comme Macron
    List<Integer> idx= Arrays.asList(1,2,2,1,1);
    
    for (int i=2;i<6;i++) {
      faces.get(i).setList(Arrays.asList(LFace1.get(idx.get(0)), LFace1.get(idx.get(1)), LFace2.get(idx.get(2)), LFace2.get(idx.get(3)), LFace1.get(idx.get(4))));
      idx= idx.stream().map(x -> (x +1)%4 ).collect(Collectors.toList());
    }
    
    
//    LFace3.setList(Arrays.asList(LFace1.get(idx.get(0)), LFace1.get(idx.get(1)), LFace2.get(idx.get(2)), LFace2.get(idx.get(3)), LFace1.get(idx.get(4))));
//    System.out.println(idx);
//    //on incrémente les indexs en changeant de face
//    idx= idx.stream().map(x -> (x +1)%4 ).collect(Collectors.toList());
//    LFace4.setList(Arrays.asList(LFace1.get(idx.get(0)), LFace1.get(idx.get(1)), LFace2.get(idx.get(2)), LFace2.get(idx.get(3)), LFace1.get(idx.get(4))));
//    System.out.println(idx);
//    
//    idx= idx.stream().map(x -> (x +1)%4 ).collect(Collectors.toList());
//    LFace5.setList(Arrays.asList(LFace1.get(idx.get(0)), LFace1.get(idx.get(1)), LFace2.get(idx.get(2)), LFace2.get(idx.get(3)), LFace1.get(idx.get(4))));
//    System.out.println(idx);
//    
//
//    idx= idx.stream().map(x -> (x +1)%4 ).collect(Collectors.toList());
//    LFace6.setList(Arrays.asList(LFace1.get(idx.get(0)), LFace1.get(idx.get(1)), LFace2.get(idx.get(2)), LFace2.get(idx.get(3)), LFace1.get(idx.get(4))));
//    System.out.println(idx);
    

    
    // affichage coords
    faces.stream()
    .map(x->{
        System.out.println("_________FACE"+(faces.indexOf(x) + 1 )+"________");
        return x;
    })
    .forEach(System.out::println);
    
    
    
    //creation cube en tant que GM_Solide
  ArrayList<IOrientableSurface> listeFaces = 
  faces.stream()
  .map(x-> new GM_LineString(x))
      .map(x->(GM_OrientableSurface) new GM_Polygon(x))
      .collect(Collectors.toCollection(ArrayList::new));
    System.out.println("cube généré !");    
    return new GM_Solid(listeFaces);
    
  }
  
  
  

}


