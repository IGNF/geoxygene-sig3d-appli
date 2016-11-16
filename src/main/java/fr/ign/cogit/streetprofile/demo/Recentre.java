package  fr.ign.cogit.streetprofile.demo;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class Recentre {

  
  /**
   * 
   * Classe permettant de recentrer les points lors qu'ils sont trop éloignés de l'axe central
   * 
   * @author MFund
   * @author MBrasebin
   * @author JPerret
   * @author YMeneroux
   *
   */
  public static void main(String[] args){
    String donnnespoints = BuildingProfilExecParamters.FILE_OUT_POINTS_DEMO;
    String donneesPointsOut = BuildingProfilExecParamters.FOLDER_OUT + "points_rencetre.shp";
    
    
    IFeatureCollection<IFeature> points = ShapefileReader.read(donnnespoints);
    
    int shift = 100;
    
    for(IFeature point: points){
      
      
     
      double valP =  Double.parseDouble(point.getAttribute("Y").toString());
      
      
      double signumValP = Math.signum(valP);
      
      
      
      
      point.setAttribute(point.getFeatureType().getFeatureAttributeByName("Y"), (Object)( valP - signumValP * shift));
        
      
      
      
      point.getGeom().coord().get(0).setY(point.getGeom().coord().get(0).getY() - signumValP * shift);
      
      
      
    }
    
    ShapefileWriter.write(points, donneesPointsOut);
    
    
  }
}
