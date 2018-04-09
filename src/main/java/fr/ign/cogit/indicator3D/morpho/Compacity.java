package fr.ign.cogit.indicator3D.morpho;


import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_Building;
import  fr.ign.cogit.geoxygene.sig3d.calculation.Util;

  

public class Compacity {
  
  // Relative compacity with Sphere as reference Volume
  public Double RelativeCompacitySphere(Double vol, Double surf) {
    
     
    // formule approchée : 4.84 * Volume ^2/3 / Aire
 
    return (4*Math.PI * Math.pow(3./(4*Math.PI), 2.0/3.0)  * Math.pow(vol, 2./3.)  / surf);
    return 0.0;
  }
  




}
