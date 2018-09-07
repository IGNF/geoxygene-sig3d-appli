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

/**
 * A code to generate the transformation of the visible sky with a sphere along
 * a linear geometry. A serie of jpeg images are produced and can be used
 * together to produce a gif with for example imagemagick and the command
 * convert -delay 20 -loop 0 *.jpg myimage.gif
 * 
 * 
 * @author mbrasebin
 *
 */
public class SkyViewFactorTransformation {

	public static void main(String[] args) {

		// The folder that contains all the necessary data
		String folderName = "/home/mbrasebin/Documents/Donnees/Exp/Eugene_Million/";
		// Building file
		String buildingFile = folderName + "batiment.shp";
		// Road file
		String roadFile = folderName + "route.shp";

		String folderOut = "/tmp/tmp/";

		// Sampling step in m along the roads
		int step = 1;

		// Number of points for the sky Openess
		int angularStep = 180;

		// The raidus of the sky openess
		double radius = 50;

		// The position of the camera
		IDirectPosition camPos = new DirectPosition(648218.905233, 6860151.300013, 120.0);

		// The position of the point pointed by the camera
		IDirectPosition aimPos = new DirectPosition(648084.9, 6859850.2, 39.0);

		// Building and roads color)
		Color colorBuildings = new Color(64, 0, 0);
		Color colorRoad = new Color(0, 0, 0);

		// The sphere can be generated too
		boolean exportAsShape = false;

		// Reading of the road shapefile
		IFeatureCollection<IFeature> featColl = ShapefileReader.read(roadFile);

		// The sampling of the points where the sky openess will be calculated
		IDirectPositionList points = preparePoints(featColl, step);

		// Importing building as a 3D geometric shapefile
		IFeatureCollection<IFeature> buildings = ShapefileReader.read(buildingFile);

		// Initializing the visualisation
		InterfaceMap3D iMmap = initVisu(buildings, featColl, aimPos, camPos, colorBuildings, colorRoad);

		Map3D m = iMmap.getCurrent3DMap();

		IFeatureCollection<IFeature> featC = new FT_FeatureCollection<>();

		// Counter of the points
		int count = 0;

		int nbPoints = points.size();

		// The idea is to ensure that there will be enough caractere in the file name in
		// order to allow a simple alphbetic ordering
		int nbCaract = (int) (Math.floor(Math.log10(nbPoints))) + 1;

		// Loop on the sampled points
		for (int i = 0; i < nbPoints; i++) {

			IDirectPosition dp = points.get(i);

			// We calculate the sphre
			IFeature feat = generateSphere(dp, buildings, angularStep, radius);

			if (exportAsShape) {
				// We store the sphere in a collection if we want to store it as a shapefile
				featC.add(feat);

			}

			IFeatureCollection<IFeature> featCtemp = new FT_FeatureCollection<>();
			featCtemp.add(feat);

			// The representatino of the sphere is in white with black edges
			feat.setRepresentation(new ObjectCartoon(feat, Color.WHITE));

			// We create a layer with the sphere and add it to the 3DMap
			String layerName = "Layer_" + count;
			VectorLayer vl = new VectorLayer(featCtemp, layerName);
			m.addLayer(vl);

			System.out.println("Points : " + (count++) + "  on " + points.size());
			// We make a pause to ensure that the rendering is done
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// We store the image with a formatted name
			iMmap.screenCapture(folderOut, "img_" + String.format("%0" + nbCaract + "d", count) + ".jpg");
			// We remove the layer
			m.removeLayer(layerName);
			// The pause is to ensure that the layer is properly removed from the 3D scene
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		// We write the shapefile if needed
		if (exportAsShape) {
			ShapefileWriter.write(featC, folderOut + "out.shp");
		}

		System.out.println("------ End ------");
		System.out.println(
				"--You can generate a gif with for example imagemagick and the command convert -delay 20 -loop 0 *.jpg myimage.gif--");
		System.exit(0);

	}

	private static InterfaceMap3D initVisu(IFeatureCollection<IFeature> buildings,
			IFeatureCollection<IFeature> featColl, IDirectPosition aimPos, IDirectPosition camPos, Color colorBuildings,
			Color colorRoad) {

		// Creating of a 3D windows
		MainWindow mw = new MainWindow();

		Map3D map = mw.getInterfaceMap3D().getCurrent3DMap();

		// We add the layers
		VectorLayer vL = new VectorLayer(buildings, "Buildings", colorBuildings);
		VectorLayer vL2 = new VectorLayer(featColl, "Roads", colorRoad);

		map.addLayer(vL);
		map.addLayer(vL2);

		// The sleep is where to ensure that the rendered is done before continuing
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mw.getInterfaceMap3D().zoomOn(aimPos.getX(), aimPos.getY(), aimPos.getZ(), new Vecteur(aimPos, camPos));

		return mw.getInterfaceMap3D();
	}

	public static IFeature generateSphere(IDirectPosition dp, IFeatureCollection<IFeature> buildings, int step,
			double rayon) {
		// Classical configuration
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
		IGeometry geom = Operateurs.echantilloneSansPreservation(street, step);
		IDirectPositionList dpl = geom.coord();

		return dpl;
	}

}
