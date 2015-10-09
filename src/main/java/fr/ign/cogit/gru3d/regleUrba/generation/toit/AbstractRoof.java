package fr.ign.cogit.gru3d.regleUrba.generation.toit;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiSurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_RoofSurface;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiSurface;


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
 *
 * @TODO : vérifier la compatibilité avec CG_RoofSurface
 * 
 *
 */
public abstract class AbstractRoof extends CG_RoofSurface implements IRoof {
  
  
  

  // Génération du bâtiment
  public IMultiSurface<IPolygon> generateBuilding(double zMin) {

    IMultiSurface<IPolygon> mSOut = new GM_MultiSurface<IPolygon>();

    IMultiSurface<IPolygon> mS1 = this.generateWall(zMin);

    if (mS1 == null) {
      return null;
    }

    IMultiSurface<IPolygon> mS2 = this.getRoof();
    
    if (mS2 == null) {
      return null;
    }

    mSOut.addAll(mS1);
    mSOut.addAll(mS2);

    return mSOut;

  }

}
