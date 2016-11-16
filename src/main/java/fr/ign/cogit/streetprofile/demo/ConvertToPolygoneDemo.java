package  fr.ign.cogit.streetprofile.demo;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.streetprofile.model.ConvertPointToPolygon;


/**
 * 
 * Classe permettant de transformer des points projetés en polygones
 * 
 * @author MFund
 * @author MBrasebin
 * @author JPerret
 * @author YMeneroux
 *
 */
public class ConvertToPolygoneDemo {
  
  
  

  public static void main(String[] args) {

    // Le rayon du swingingArm
    double radius = 0.01;

    //On transforme les points en polygones (liés entre eux en fonction de l'attribut BuildingProfilConstantParameters.ID)
    IFeatureCollection<IFeature> featCOut = ConvertPointToPolygon.convert(
        ShapefileReader.read(BuildingProfilExecParamters.FILE_OUT_POINTS_DEMO),
        radius);

    //On sauvegarde les polygones créés
    ShapefileWriter.write(featCOut,
        BuildingProfilExecParamters.FILE_OUT_POLYGON_DEMO);

  }


}
