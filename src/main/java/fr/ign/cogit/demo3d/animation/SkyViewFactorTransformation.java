package fr.ign.cogit.demo3d.animation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.contrib.geometrie.Vecteur;
import fr.ign.cogit.geoxygene.convert.FromGeomToLineString;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.calculation.raycasting.RayCasting;
import fr.ign.cogit.geoxygene.sig3d.gui.InterfaceMap3D;
import fr.ign.cogit.geoxygene.sig3d.gui.MainWindow;
import fr.ign.cogit.geoxygene.sig3d.representation.sample.ObjectCartoon;
import fr.ign.cogit.geoxygene.sig3d.semantic.Map3D;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiSurface;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class SkyViewFactorTransformation {

	public static void main(String[] args) {

		// The folder that contains all the necessary data
		String folderName = "/home/mickael/data/mbrasebin/donnees/Eugene_Million/";
		// Building file
		String buildingFile = folderName + "batiment.shp";
		// Road file
		String roadFile = folderName + "route.shp";

		String folderOut = "/tmp/tmp/";

		// Sampling step along the roads
		int step = 25;

		int angularStep = 180;

		double rayon = 50;
		IDirectPosition camPos = new DirectPosition(648084.9, 6859850.2, 39.0);
		IDirectPosition aimPos = new DirectPosition(648218.905233, 6860151.300013, 120.0);

		// Reading of the road shapefile
		IFeatureCollection<IFeature> featColl = ShapefileReader.read(roadFile);

		// Sampled point
		IDirectPositionList points = preparePoints(featColl, step);

		IFeatureCollection<IFeature> buildings = ShapefileReader.read(buildingFile);

		InterfaceMap3D iMmap = initVisu(buildings, featColl, camPos, aimPos);

		Map3D m = iMmap.getCurrent3DMap();

		int count = 0;

		IFeatureCollection<IFeature> featC = new FT_FeatureCollection<>();

		boolean exportAsShape = false;

		for (int i = 0; i < points.size(); i++) {

			IDirectPosition dp = points.get(i);

			IFeature feat = generateSphere(dp, buildings, angularStep, rayon);

			if (exportAsShape) {

				featC.add(feat);

			}

			IFeatureCollection<IFeature> featCtemp = new FT_FeatureCollection<>();
			featCtemp.add(feat);

			feat.setRepresentation(new ObjectCartoon(feat, Color.WHITE));

			
			String layerName = "Layer_" + count;
			VectorLayer vl = new VectorLayer(featCtemp, layerName);
			m.addLayer(vl);

			System.out.println("Points : " + (count++) + "  on " + points.size());

			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			
			iMmap.screenCapture(folderOut, "img_" + count + ".jpg");
			
			m.removeLayer(layerName);
			

			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}

		if (exportAsShape) {
			ShapefileWriter.write(featC, folderOut + "out.shp");
		}

	}

	private static InterfaceMap3D initVisu(IFeatureCollection<IFeature> buildings,
			IFeatureCollection<IFeature> featColl, IDirectPosition camPos, IDirectPosition aimPos) {

		MainWindow mw = new MainWindow();

		Color colorBuildings = new Color(64, 0, 0);
		Color colorRoad = new Color(0, 0, 0);

		Map3D map = mw.getInterfaceMap3D().getCurrent3DMap();

		VectorLayer vL = new VectorLayer(buildings, "Buildings", colorBuildings);
		VectorLayer vL2 = new VectorLayer(featColl, "Roads", colorRoad);

		map.addLayer(vL);
		map.addLayer(vL2);

		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mw.getInterfaceMap3D().zoomOn(camPos.getX(), camPos.getY(), camPos.getZ(), new Vecteur(camPos, aimPos));

		return mw.getInterfaceMap3D();
	}

	public static IFeature generateSphere(IDirectPosition dp, IFeatureCollection<IFeature> buildings, int step,
			double rayon) {

		RayCasting.EPSILON = 0.01;
		int resultType = RayCasting.TYPE_FIRST_POINT_AND_SPHERE;

		RayCasting rC = new RayCasting(dp, buildings, step, rayon, resultType, false);
		rC.cast();

		// Generating the geometry
		IFeature feat = new DefaultFeature(new GM_MultiSurface<>(rC.getGeneratedSolid().getFacesList()));

		return feat;

	}

	/**
	 * Sampling of points on a road
	 * 
	 * @param roadFile
	 * @param step
	 * @return
	 */
	public static IDirectPositionList preparePoints(IFeatureCollection<IFeature> featColl, double step) {

		// Listing the lines
		List<ILineString> routeunique = new ArrayList<ILineString>();

		for (IFeature ft1 : featColl) {

			routeunique.add((ILineString) FromGeomToLineString.convert(ft1.getGeom()).get(0));

		}

		// Merging the line into one
		ILineString street = Operateurs.union(routeunique);

		// Sampling and getting the points
		IGeometry geom = Operateurs.echantillone(street, step);
		IDirectPositionList dpl = geom.coord();

		return dpl;
	}

}
