package fr.ign.cogit.demo3d.color;

import java.awt.Color;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.convert.transform.Extrusion2DObject;
import fr.ign.cogit.geoxygene.sig3d.gui.MainWindow;
import fr.ign.cogit.geoxygene.sig3d.representation.noLight.Object3dNoLightEffect;
import fr.ign.cogit.geoxygene.sig3d.semantic.Map3D;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;


/**
 * 
 * Display a set of colors in 3D from the same family (here blue with B variable)
 * 
 * @author mbrasebin
 * @author choarau
 *
 */
public class DisplayConventionnalRules {

	public static void main(String[] args){
		String path = DisplayConventionnalRules.class.getClassLoader()
            .getResource("./circles/BUARD_CERCLES_area.shp").getFile();
		
		IPopulation<IFeature> popFeat = ShapefileReader.read(path);
		FT_FeatureCollection<IFeature> featColl = new FT_FeatureCollection<IFeature> ();
		
		int nbElem = popFeat.size();
		System.out.println(nbElem);
		for (int i = 0; i < nbElem; i++) {
			IFeature featTemp = popFeat.get(i);
			featTemp.setGeom(Extrusion2DObject.convertFromGeometry(featTemp.getGeom(),0,5));
			String colorKey = featTemp.getAttribute("NOM").toString();
			
			
			System.out.println(featTemp.getGeom().coord().size());
			
			if(colorKey.contains("B") && !colorKey.contains("L")){
				System.out.println("On a une couleur bleue!");
//			if(colorKey.contains("V") && !colorKey.contains("I") && !colorKey.contains("B")){
//				System.out.println("On a une couleur verte!");
//			if(colorKey.contains("0") || colorKey.contains("1") || colorKey.contains("2")){
//				System.out.println("On a une couleur claire!");
				int r = (int)Double.parseDouble(featTemp.getAttribute("R").toString());			
				int v = (int) Double.parseDouble(featTemp.getAttribute("V").toString());
				int b =  (int)Double.parseDouble(featTemp.getAttribute("B").toString());
				
			
				
				featTemp.setRepresentation(new Object3dNoLightEffect(featTemp, new Color(r, v, b)));
			} else {
				featTemp.setRepresentation(new Object3dNoLightEffect(featTemp, new Color(100, 100, 100)));
			}

			featColl.add(featTemp);
		}
		
		MainWindow mainWindow = new MainWindow();     
        Map3D map =  mainWindow.getInterfaceMap3D().getCurrent3DMap();
        
        mainWindow.getInterfaceMap3D().removeLight(0);
        
		VectorLayer vl = new VectorLayer(featColl,"Cercle chromatique");

		map.addLayer(vl);
	}
}
