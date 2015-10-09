package fr.ign.cogit.gru3d.regleUrba.reglesUrbanismes.regles;
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
public abstract class Texture {

  private static String[] lURLTextures = null;// A compléter
  private static String[] lNomsTextures = { "Brique", "Bois", "Pierre",
      "Crépi blanc", "Crépi rose", "Crépi vert", "Ardoise", "Tuile rouge",
      "Tuile brune"

  };

  public static String[] getlURLTextures() {
    return Texture.lURLTextures;
  }

  public static String[] getlNomsTextures() {
    return Texture.lNomsTextures;
  }

}
