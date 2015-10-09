package fr.ign.cogit.gru3d.io.exports;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.sig3d.calculation.raycasting.IndicatorVisu;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
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
public class ExportStatistiques {
  
  
  private static Logger LOGGER = Logger.getLogger(ExportStatistiques.class);

  public static void exportIndicatorVisu(List<IndicatorVisu> lVis, String file)
      throws IOException {
    FileWriter fw = new FileWriter(file);

    LOGGER.info("ExportCommencé");

    // //////Ecriture des en-têtes

    fw.write("DistanceMoyenne;DistanceMinimale;DistanceMaximale;Variance;Distance2DMoyenne;Distance2DMinimale;Distance2DMaximale;Ouverture;VolumeVisible;VisibleSSurface\n");

    int nbVis = lVis.size();

    for (int i = 0; i < nbVis; i++) {

      IndicatorVisu vis = lVis.get(i);

      fw.write(vis.getMoyRadialDistance() + ";");
      fw.write(vis.getMinimalRadialDistance() + ";");
      fw.write(vis.getMaximalRadialDistance() + ";");
      fw.write(vis.getVarianceRadialDistance() + ";");

      fw.write(vis.getMoyRadialDistance2D() + ";");
      fw.write(vis.getMinimalRadialDistance2D() + ";");
      fw.write(vis.getMaximalRadialDistance2D() + ";");

      fw.write(vis.getOpeness() + ";");
      fw.write(vis.getVisibleVolume() + ";");
      fw.write(vis.getVisibleSkySurface() + "\n");

    }

    fw.flush();
    fw.close();

    LOGGER.info("ExportTerminé");

  }

  public static void exportIndicatorVisu(List<IndicatorVisu> lVis,
      IFeatureCollection<IFeature> featColl, String file) throws IOException {

    int nbVis = lVis.size();

    for (int i = 0; i < nbVis; i++) {

      DefaultFeature feat = (DefaultFeature) featColl.get(i);
      IndicatorVisu vis = lVis.get(i);

      
      
      AttributeManager.addAttribute(feat, "DMoy", vis.getMoyRadialDistance(),
          "Double");
      AttributeManager.addAttribute(feat, "DMin", vis.getMinimalRadialDistance(),
          "Double");
      AttributeManager.addAttribute(feat, "DMax", vis.getMaximalRadialDistance(),
          "Double");
      AttributeManager.addAttribute(feat, "Variance",
          vis.getVarianceRadialDistance(), "Double");
      AttributeManager.addAttribute(feat, "D2DMoy", vis.getMoyRadialDistance2D(),
          "Double");
      AttributeManager.addAttribute(feat, "D2DMin",
          vis.getMinimalRadialDistance2D(), "Double");
      AttributeManager.addAttribute(feat, "D2DMax",
          vis.getMaximalRadialDistance2D(), "Double");
      
      
      
      /*
      AddAttribute.addAttribute(feat, "Ouverture", vis.getOpeness(), "Double");
      AddAttribute.addAttribute(feat, "VVisible", vis.getVisibleVolume(),
          "Double");
      AddAttribute.addAttribute(feat, "SVisible", vis.getVisibleVolume(),
          "Double");
      AddAttribute.addAttribute(feat, "SolidP", vis.getSolidPerimeter(),
          "Double");
      
      */
      
      // addAttribute(feat,"AireISOV",vis.getAireIsovist(),"Double" );
      // addAttribute(feat,"Permieter",vis.getPermietreIsovist(),"Double" );

      AttributeManager.addAttribute(feat, "RatioHit", vis.getRatioSphere(), "Double");
      AttributeManager
          .addAttribute(feat, "RatioHitDist", vis.getRatioSphere2(), "Double");
      
      AttributeManager.addAttribute(feat, "NB_Points", vis.getCast()
          .getNbPointsCouronnes(), "Double");
      
      AttributeManager.addAttribute(feat, "NB_Hits", vis.getNbHit()
          , "Integer");
      
      AttributeManager.addAttribute(feat, "NB_Rays", vis.getCast().getDpGenerated().size()
          , "Integer");
      
      AttributeManager.addAttribute(feat, "Rayon", vis.getCast().getRayon(),
          "Double");

    }

    fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter
        .write(featColl, file);

  }

}
