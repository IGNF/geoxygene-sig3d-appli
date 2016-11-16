package  fr.ign.cogit.streetprofile.demo;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.streetprofile.model.BuildingProfileParameters;

/**
 * Classe permettant d'ajouter un identifiant aux bâtiments (identifiant
 * apparaissant sur les points et nécessaire à la création de polygones)
 * 
 * 
 * @author MBrasebin
 * 
 */
public class Identifiant {

  /**
   * Il s'agit d'une classe exécutable, il est nécessaire de définir fileOut et le fichier en entrée (ici le fichier de démo)
   * 
   */
  public static void main(String[] args) {
    String fileOut = "";

    // Le but ici est de récupérer l'identifiant dans mes shapefiles
    String donneesshp = BuildingProfilExecParamters.FILE_IN_BUILDING_DEMO;
    IFeatureCollection<IFeature> idcoll = ShapefileReader.read(donneesshp);

    IFeature ftid = null;
    // new DefaultFeature();
    for (int i = 0; i < idcoll.size(); i++) {
      ftid = idcoll.get(i);
      Object Oid = i;
      // objet à compléter
      AttributeManager.addAttribute(ftid,
          BuildingProfileParameters.ID, Oid, "String");
      // idcoll.add(ftid);
    }

    // Export du shape
    ShapefileWriter.write(idcoll, fileOut);
    //

  }
}
