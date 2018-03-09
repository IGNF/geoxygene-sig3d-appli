package fr.ign.cogit.demo3d.color;

import java.awt.Color;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.convert.transform.Extrusion2DObject;
import fr.ign.cogit.geoxygene.sig3d.gui.MainWindow;
import fr.ign.cogit.geoxygene.sig3d.representation.noLight.Object3dNoLightEffect;
import fr.ign.cogit.geoxygene.sig3d.semantic.Map3D;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
import fr.ign.cogit.geoxygene.spatial.geomroot.GM_Object;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;


/**
 * 
 * Representation of 3D chromatic circles
 * 
 * @author mbrasebin
 * @author choarau
 *
 */
public class DisplayChromaticCircles {

	public static void main(String[] args) {

		String path = DisplayChromaticCircles.class.getClassLoader().getResource("./circles/BUARD_CERCLES_area.shp")
				.getFile();

		IPopulation<IFeature> popFeat = ShapefileReader.read(path);

		IFeatureCollection<IFeature> featColl = new FT_FeatureCollection<IFeature>();

	

		for (IFeature featTemp : popFeat) {
		

			// double zMax =
			// Double.parseDouble(featTemp.getAttribute("MaxRGB").toString());
			double zMax = Double.parseDouble(featTemp.getAttribute("SumRGB").toString());

			featTemp.setGeom(Extrusion2DObject.convertFromGeometry((GM_Object) featTemp.getGeom(), 0, 0.5 * zMax));

			int r = (int) Double.parseDouble(featTemp.getAttribute("R").toString());
			int v = (int) Double.parseDouble(featTemp.getAttribute("V").toString());
			int b = (int) Double.parseDouble(featTemp.getAttribute("B").toString());

			featTemp.setRepresentation(new Object3dNoLightEffect(featTemp, new Color(r, v, b)));

			featColl.add(featTemp);

		}

		MainWindow mainWindow = new MainWindow();

		Map3D map = mainWindow.getInterfaceMap3D().getCurrent3DMap();

		mainWindow.getInterfaceMap3D().removeLight(0);

		VectorLayer vl = new VectorLayer(featColl, "Cercle chromatique");

		map.addLayer(vl);

	}
}
