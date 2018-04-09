package fr.ign.cogit.indicator3D.morpho;



import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import javax.xml.bind.JAXBException;

import org.citygml4j.xml.io.reader.CityGMLReadException;


import fr.ign.cogit.geoxygene.sig3d.gui.MainWindow;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.Context;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.LoaderCityGML;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_Building;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_BuildingPart;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_GroundSurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_RoofSurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_WallSurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_AbstractBoundarySurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_AbstractBuilding;
import fr.ign.cogit.geoxygene.sig3d.representation.citygml.representation.CG_StyleGenerator;
import fr.ign.cogit.geoxygene.sig3d.semantic.Map3D;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
import fr.ign.cogit.indicator3D.transform.CityGMLToShapeFile;

import  fr.ign.cogit.geoxygene.sig3d.calculation.Util;

import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiSurface;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_OrientableSurface;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.Representation;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.convert.FromGeomToSurface;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.indicator3D.transform.CityGMLToShapeFile; 
import fr.ign.cogit.geoxygene.sig3d.representation.citygml.building.*;




public class Computation3DIndicators {

  public static void main(String[] args) {
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
       
        
    
    
    
    
    // traitement sur un batiment 
    
      
      CG_AbstractBuilding absBati =   (CG_AbstractBuilding) oneOfBati ;
        
      ArrayList<CG_BuildingPart> lBatiPart = (ArrayList<CG_BuildingPart>) absBati.getConsistsOfBuildingPart(); 
    
      for (CG_AbstractBuilding partie : lBatiPart) {
       System.out.println("partie "+ (lBatiPart.indexOf(partie) + 1 )+" / "+ lBatiPart.size() + "\n" );
        List<CG_AbstractBoundarySurface> boundaries =  partie.getBoundedBySurfaces();
        System.out.println(boundaries.size()+ " boundaries Surfaces"   );  
        
        
           
            
        for (CG_AbstractBoundarySurface aBS : boundaries) {

         IMultiSurface<IOrientableSurface> geom = aBS.getLod2MultiSurface();
         IGeometry geom2 = aBS.getLod2MultiSurface(); 
         

          if (aBS instanceof CG_RoofSurface) {

            
            System.out.println("### toit détecté");

            
            //conversion deguelasse pour appeller volumeundersurface
            List < IOrientableSurface> ll = new ArrayList();
            for(IOrientableSurface g: geom) {
              ll.add(g);
            }
            
            
            

            System.out.println("volume sous le toît ? " + Util.volumeUnderSurface(ll));
            
            
        }

      
      
      }
      }
      
      
      
      
    
    
//    
//    
//     
//        
//      System.out.println(boundaries);
//      System.out.println("#### Volum" + volume);
//     
//
//      System.out.println("boudariesSurfaces  " + boundaries.size());
//      
 //     IFeatureCollection<IFeature> bf = CityGMLToShapeFile.convertToFeatureCollection(vl, separateBuilding)
      
      
      
      
      //Creating main window
      //MainWindow win = new MainWindow();
      //Getting 3D map
      //Map3D carte = win.getInterfaceMap3D().getCurrent3DMap();
      //Adding layer to map
      //carte.addLayer(vl);

      System.out.println("tutu titi ");
      
  } catch (CityGMLReadException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
  } catch (JAXBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
  }

}

}


