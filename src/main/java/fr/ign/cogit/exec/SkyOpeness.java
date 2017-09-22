package fr.ign.cogit.exec;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.calculation.raycasting.IndicatorVisu;
import fr.ign.cogit.geoxygene.sig3d.calculation.raycasting.RayCasting;
import fr.ign.cogit.geoxygene.sig3d.calculation.raycasting.Visibility;
import fr.ign.cogit.geoxygene.sig3d.convert.transform.Extrusion2DObject;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiSurface;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class SkyOpeness {

	protected static final Logger LOGGER = Logger.getLogger(SkyOpeness.class);

	public static void main(String[] args) throws ParseException, IOException {

		if (args == null || args.length == 0) {
			args = new String[] { "-buildings", "/home/mickael/Téléchargements/fusionne/LOD_BUILDING_2012.shp",
					"-points", "/home/mickael/data/mbrasebin/donnees/ecolthematique/part-dieu/trees.shp", "-output",
					"/home/mickael/data/mbrasebin/donnees/ecolthematique/temp/", "-z", "172", "HAUTEUR", "-r", "100",
					"-s", "360", "-id", "gid", "-g3D", "-g2D" };
		}

		// Defining and parsing options
		Options options = configFirstParameters();
		CommandLineParser parser = new DefaultParser();

		CommandLine cmd = parser.parse(options, args);

		// Write help
		if (cmd.hasOption(HELP_ARGS)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("skyopeness", options);
			System.exit(0);
		}

		LOGGER.setLevel(Level.ALL);
		LOGGER.info("---Initialization of option---");

		LOGGER.info("---Parsing option---");

		// Geographic features
		IFeatureCollection<IFeature> featCollBuildings = null;
		IFeatureCollection<IFeature> featCollPoints = null;

		// Input data path
		String buildingFile = cmd.getOptionValue(BUILDING_FILE_ARGS);
		String pointFile = cmd.getOptionValue(POINT_FILE_ARGS);
		String outputFile = cmd.getOptionValue(OUTPUT_FILE_ARGS);

		LOGGER.info("---Importing data---");
		LOGGER.info("Point path : " + pointFile);

		// Data preprocessing
		//// For points the altitude may be set to zero
		if (cmd.hasOption(FORCE_POINT_ZERO_ARGS)) {
			LOGGER.info("Point altitude is set to zero");
			double z = Double.parseDouble(cmd.getOptionValue(FORCE_POINT_ZERO_ARGS));
			featCollPoints = setPointToZero(pointFile, z);
		} else {
			featCollPoints = ShapefileReader.read(pointFile);
		}

		LOGGER.info("Building file path : " + buildingFile);

		//// For buildings, they may be extruded
		if (cmd.hasOption(EXTRUDE_BUILINDGS_ARGS)) {
			String heightAttribute = cmd.getOptionValue(EXTRUDE_BUILINDGS_ARGS);
			LOGGER.info("Extrusion of building with attribute : " + heightAttribute);
			featCollBuildings = prepareBuildingCollection(buildingFile, heightAttribute);
		} else {
			featCollBuildings = ShapefileReader.read(buildingFile);
		}

		LOGGER.info("---Setting parameters---");
		double radius = 500;
		if (cmd.hasOption(RADIUS_ARGS)) {
			radius = Double.parseDouble(cmd.getOptionValue(RADIUS_ARGS));
		}
		LOGGER.info("Radius set to : " + radius + " m");

		int step = 180;
		if (cmd.hasOption(STEP_ARGS)) {
			step = Integer.parseInt(cmd.getOptionValue(STEP_ARGS));
		}

		LOGGER.info("---Export parameters---");

		boolean exportGeom3D = cmd.hasOption(GEOMETRY_3D_ARGS);
		boolean exportGeom2D = cmd.hasOption(GEOMETRY_2D_ARGS);

		if (exportGeom3D) {
			LOGGER.info("---Export 3D geometry---");
		}

		if (exportGeom2D) {
			LOGGER.info("---Export 2D geometry---");
		}

		LOGGER.info("---Export stats---");

		if (cmd.hasOption(ID_ARGS)) {
			ID_ATT = cmd.getOptionValue(ID_ARGS).toString();
		}

		LOGGER.info("Used ID : " + ID_ATT);

		if (cmd.hasOption(OUTPUT_FILE_NAME_ARGS)) {
			OUTPUT_FILE_NAME = cmd.getOptionValue(cmd.getOptionValue(OUTPUT_FILE_NAME_ARGS).toString());
		}
		LOGGER.info("Output folder name : " + OUTPUT_FILE_NAME);

		File outputFolder = new File(outputFile);

		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		run(featCollBuildings, featCollPoints, radius, step, outputFolder, exportGeom2D, exportGeom3D);

	}

	private final static String BUILDING_FILE_ARGS = "buildings";
	private final static String POINT_FILE_ARGS = "" + "points";
	private final static String OUTPUT_FILE_ARGS = "output";
	private final static String FORCE_POINT_ZERO_ARGS = "z";
	private final static String EXTRUDE_BUILINDGS_ARGS = "extrude";
	private final static String RADIUS_ARGS = "r";
	private final static String STEP_ARGS = "s";
	private final static String ID_ARGS = "id";
	private final static String GEOMETRY_3D_ARGS = "g3D";
	private final static String GEOMETRY_2D_ARGS = "g2D";
	private final static String OUTPUT_FILE_NAME_ARGS = "o";
	private final static String HELP_ARGS = "h";

	private static String ID_ATT = "ID";
	private static String OUTPUT_FILE_NAME = "out";

	private static Options configFirstParameters() {
		Options options = new Options();

		Option buildings = new Option(BUILDING_FILE_ARGS, true, "Building file in shapefile");
		buildings.setRequired(true);
		buildings.setArgName("building-file");
		options.addOption(buildings);

		Option points = new Option(POINT_FILE_ARGS, true, "Points in shapefile");
		points.setRequired(true);
		points.setArgName("points-file");
		options.addOption(points);

		Option output = new Option(OUTPUT_FILE_ARGS, true, "Output folder");
		output.setRequired(true);
		output.setArgName("output-folder");
		options.addOption(output);

		Option forcePointZero = new Option(FORCE_POINT_ZERO_ARGS, true, "Force points to be set at z value");
		forcePointZero.setRequired(false);
		options.addOption(forcePointZero);

		Option extrudeBuildings = new Option(EXTRUDE_BUILINDGS_ARGS, true,
				"Extrude buildings according to an attribute");
		extrudeBuildings.setRequired(false);
		extrudeBuildings.setArgName("extrude-attribute");
		options.addOption(extrudeBuildings);

		Option radius = new Option(RADIUS_ARGS, true, "Radius of raycasting (500 m as default value)");
		radius.setArgName("radius");
		radius.setRequired(false);
		options.addOption(radius);

		Option s = new Option(STEP_ARGS, true, "Number of point for 180° (180 as default value).");
		s.setArgName("path");
		s.setRequired(false);
		options.addOption(s);

		Option g = new Option(GEOMETRY_3D_ARGS, false, "Export 3D geometry.");
		g.setRequired(false);
		options.addOption(g);

		Option g2 = new Option(GEOMETRY_2D_ARGS, false, "Export 2D geometry.");
		g2.setRequired(false);
		options.addOption(g2);

		Option id = new Option(ID_ARGS, true, "ID of point feature (ID by default).");
		id.setRequired(true);
		id.setArgName("id");
		options.addOption(id);

		Option outputFile = new Option(OUTPUT_FILE_NAME_ARGS, true,
				"Output filename (without extension) where results are stored.");
		outputFile.setRequired(false);
		outputFile.setArgName("file-out");
		options.addOption(outputFile);

		Option help = new Option(HELP_ARGS, false, "Help");
		help.setRequired(false);
		options.addOption(help);

		return options;

	}

	public static IFeatureCollection<IFeature> prepareBuildingCollection(String buildingFile, String heightAttribute) {

		IFeatureCollection<IFeature> buildings = ShapefileReader.read(buildingFile);

		for (IFeature feat : buildings) {
			Object o = feat.getAttribute(heightAttribute);
			if (o == null) {
				continue;
			}

			double d = Double.parseDouble(o.toString());

			feat.setGeom(Extrusion2DObject.convertFromGeometry(feat.getGeom(), 0, d));

		}
		return buildings;

	}

	public static IFeatureCollection<IFeature> setPointToZero(String pointFile, double z) {

		IFeatureCollection<IFeature> points = ShapefileReader.read(pointFile);

		for (IFeature p : points) {
			p.setGeom(Extrusion2DObject.convertFromGeometry(p.getGeom(), z, z));
		}

		return points;

	}

	public static void run(IFeatureCollection<IFeature> buildingFeatures, IFeatureCollection<IFeature> pointFeatures,
			double rayon, int step, File outputFolder, boolean export2D, boolean export3D) throws IOException {

		if (buildingFeatures.isEmpty()) {
			LOGGER.error("BUILDING FILE DOES NOT EXIST OR IS EMPTY");
			return;
		}

		if (pointFeatures.isEmpty()) {
			LOGGER.error("POINT FILE DOES NOT EXIST OR IS EMPTY");
			return;
		}

		int resultType = RayCasting.TYPE_FIRST_POINT_AND_SPHERE;

		// DefaultParameters
		boolean isSphere = false;
		Visibility.WELL_ORIENTED_FACE = false;
		RayCasting.CHECK_IS_ON_EDGE = true;

		IFeatureCollection<IFeature> featCOut = new FT_FeatureCollection<>();

		int nbTot = pointFeatures.size();
		for (int i = 0; i < nbTot; i++) {

			LOGGER.info("------Begin of point " + (i + 1) + "  /  " + nbTot);
			LOGGER.info("Begin of ray casting");
			// Casting points around current feature
			IFeature currentFeat = pointFeatures.get(i);
			RayCasting rC = new RayCasting(currentFeat.getGeom().coord().get(0), buildingFeatures, step, rayon,
					resultType, isSphere);
			rC.cast();

			LOGGER.info("End of ray casting");
			LOGGER.info("Preparing results");

			// Getting statistics around current feature
			IFeature featOut = prepareRayCastingRecords(rC, currentFeat);

			if (featOut != null) {
				featCOut.add(featOut);
			}

			String id = currentFeat.getAttribute(ID_ATT).toString();

			// Geographic outputs
			File fOutput3D = new File(outputFolder, OUTPUT_FILE_NAME + "_" + id + "_3D.shp");
			File fOutput2D = new File(outputFolder, OUTPUT_FILE_NAME + "_" + id + "_2D.shp");
			IFeatureCollection<IFeature> iFeature2D = new FT_FeatureCollection<>();
			IFeatureCollection<IFeature> iFeature3D = new FT_FeatureCollection<>();

			// If 3D export is activated
			if (export3D) {
				// Generating the geometry
				IFeature feat = new DefaultFeature(new GM_MultiSurface<>(rC.getGeneratedSolid().getFacesList()));
				AttributeManager.addAttribute(feat, ID_ATT, id, "String");
				iFeature3D.add(feat);
			}

			// If 2D export is activated
			if (export2D) {

				// Polygon is generated
				IPolygon pol = rC.getGeneratedPolygon();
				// System.out.println("Pol : " + pol);
				// Adding new feature to 2D exports
				if (pol != null && !pol.isEmpty()) {
					IFeature feat = new DefaultFeature(pol);
					AttributeManager.addAttribute(feat, ID_ATT, id, "String");
					iFeature2D.add(feat);
				}

				if (!iFeature3D.isEmpty()) {
					// Writing 3D features
					LOGGER.info("---Writing 3D geometries---");
					LOGGER.info(fOutput3D.getAbsolutePath());
					ShapefileWriter.write(iFeature3D, fOutput3D.getAbsolutePath());
				}

				if (!iFeature2D.isEmpty()) {
					// Writing 2D features
					LOGGER.info("---Writing 2D geometries---");
					LOGGER.info(fOutput3D.getAbsolutePath());
					ShapefileWriter.write(iFeature2D, fOutput2D.getAbsolutePath());
				}

			}

			LOGGER.info("------End of point " + (i + 1) + "  /  " + nbTot);

		}
		LOGGER.info("Writing output");
		ShapefileWriter.write(featCOut, outputFolder + "/" + OUTPUT_FILE_NAME);

		LOGGER.info("End of processus");

	}

	private static IFeature prepareRayCastingRecords(RayCasting rC, IFeature currentFeature) {

		IFeature feat = null;
		try {
			feat = currentFeature.cloneGeom();
		} catch (CloneNotSupportedException e) {

			e.printStackTrace();
		}

		IndicatorVisu Iv = new IndicatorVisu(rC);

		AttributeManager.addAttribute(feat, "miniRadDis", Iv.getMinimalRadialDistance(), "Double");
		AttributeManager.addAttribute(feat, "maxRadDis", Iv.getMaximalRadialDistance(), "Double");
		AttributeManager.addAttribute(feat, "avgRadDis", Iv.getMoyRadialDistance(), "Double");
		AttributeManager.addAttribute(feat, "varRadDis", Iv.getVarianceRadialDistance(), "Double");
		AttributeManager.addAttribute(feat, "mnRDis2D", Iv.getMaximalRadialDistance2D(), "Double");
		AttributeManager.addAttribute(feat, "avgRDis2D", Iv.getMoyRadialDistance2D(), "Double");
		AttributeManager.addAttribute(feat, "openess", Iv.getOpeness(), "Double");
		AttributeManager.addAttribute(feat, "ratioSph", Iv.getRatioSphere(), "Double");
		AttributeManager.addAttribute(feat, "visSkySurf", Iv.getVisibleSkySurface(), "Double");
		AttributeManager.addAttribute(feat, "visVol", Iv.getVisibleVolume(), "Double");
		AttributeManager.addAttribute(feat, "visVolRa", Iv.getVisibleVolumeRatio(), "Double");
		AttributeManager.addAttribute(feat, "solPeri", Iv.getSolidPerimeter(), "Double");

		return feat;

	}

}
