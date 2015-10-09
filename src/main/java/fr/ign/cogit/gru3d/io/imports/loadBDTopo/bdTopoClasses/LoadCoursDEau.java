package fr.ign.cogit.gru3d.io.imports.loadBDTopo.bdTopoClasses;

import java.awt.Color;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.sig3d.semantic.DTM;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
/**
 * 
 *        This software is released under the licence CeCILL
 * 
 *        see LICENSE.TXT
 * 
 *        see <http://www.cecill.info/ http://www.cecill.info/
 * 
 * 
 * 
 * @copyright IGN
 * 
 * @author Brasebin Mickaël
 * 
 * @version 1.0
 **/
public class LoadCoursDEau {

  public static VectorLayer load(IPopulation<IFeature> featCol, DTM mnt) {
    int nbElem = featCol.size();

    for (int i = 0; i < nbElem; i++) {
      // On récupère les informations relatives à chaque éléments
      IFeature feat = featCol.get(i);
      IGeometry geom = feat.getGeom();

      int posSOL = 0;
      
      
      
      Object o = feat.getAttribute("pos_sol");
      
      if(o == null){
        o = feat.getAttribute("POS_SOL");
      }


      
      if(o != null){
        posSOL =  Integer.parseInt(o.toString());
      }
  
      
      


      try {
        geom = mnt.mapGeom(geom, 3 * posSOL, true, true);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      feat.setGeom(geom);

    }

    return new VectorLayer(featCol, "Cours d'eau", Color.blue);
  }

}
