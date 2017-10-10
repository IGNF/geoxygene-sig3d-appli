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
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.BuildingProfileParameters;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.Profile;
import fr.ign.cogit.geoxygene.sig3d.convert.transform.Extrusion2DObject;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;


public class ProfileCalculation {
	protected static final Logger LOGGER = Logger.getLogger(SkyOpeness.class);

	public static void main(String[] args) throws ParseException, IOException {

		if (args == null || args.length == 0) {
			args = new String[] { "-buildings", "/home/mickael/data/mbrasebin/donnees/Marina/Bati.shp", "-trajectory",
					"/home/mickael/data/mbrasebin/donnees/Marina/Route.shp", "-output", "/home/mickael/temp/", "-sXY",
					"10", "-sZ", "10", "-d", "200" };
		}

		// Defining and parsing options
		Options optionsHelp = configHelpParameters();
		Options options = configFirstParameters();

		CommandLineParser parserHelp = new DefaultParser();

		CommandLine cmdHelp = parserHelp.parse(optionsHelp, args, true);
		// Write help
		if (cmdHelp.hasOption(HELP_ARGS)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Building profile calculation", options);
			System.exit(0);
		}

		CommandLineParser parser = new DefaultParser();

		CommandLine cmd = parser.parse(options, args);

		LOGGER.setLevel(Level.ALL);
		LOGGER.info("---Initialization of option---");

		LOGGER.info("---Parsing option---");

		// Geographic features
		IFeatureCollection<IFeature> featCollBuildings = null;
		IFeatureCollection<IFeature> featCollTrajectory = null;

		// Input data path
		String buildingFile = cmd.getOptionValue(BUILDING_FILE_ARGS);
		String trajectoryFile = cmd.getOptionValue(ROAD_FILE_ARGS);
		String outputFile = cmd.getOptionValue(OUTPUT_FILE_ARGS);

		LOGGER.info("---Importing data---");
		LOGGER.info("Trajectory path : " + trajectoryFile);

		String parcelFileName = null;
		if (cmd.hasOption(PARCEL_FILE_ARGS)) {
			parcelFileName = cmd.getOptionValue(PARCEL_FILE_ARGS);
		}

		featCollTrajectory = ShapefileReader.read(trajectoryFile);

		LOGGER.info("Parcel file : " + parcelFileName);

		// Data preprocessing

		LOGGER.info("Building file path : " + buildingFile);

		//// For buildings, they may be extruded
		if (cmd.hasOption(EXTRUDE_BUILINDGS_ARGS)) {
			String heightAttribute = cmd.getOptionValue(EXTRUDE_BUILINDGS_ARGS);
			LOGGER.info("Extrusion of building with attribute : " + heightAttribute);
			featCollBuildings = prepareBuildingCollection(buildingFile, heightAttribute);
		} else {
			featCollBuildings = ShapefileReader.read(buildingFile);
		}

		LOGGER.info("Number of buildings : " + featCollBuildings.size());

		LOGGER.info("---Setting parameters---");

		double radius = 50;
		if (cmd.hasOption(MAX_DIST_ARGS)) {
			radius = Double.parseDouble(cmd.getOptionValue(MAX_DIST_ARGS));
		}
		LOGGER.info("Maximal distance : " + radius + " m");

		double stepXY = 4;
		if (cmd.hasOption(XY_STEP_ARGS)) {
			stepXY = Double.parseDouble(cmd.getOptionValue(XY_STEP_ARGS));
		}

		double stepZ = 4;
		if (cmd.hasOption(Z_STEP_ARGS)) {
			stepZ = Double.parseDouble(cmd.getOptionValue(Z_STEP_ARGS));
		}

		LOGGER.info("Planimetric step : " + stepXY + "  Altimetric step " + stepZ);

		LOGGER.info("---Export stats---");

		if (cmd.hasOption(ID_ARGS)) {
			ID_ATT = cmd.getOptionValue(ID_ARGS).toString();
			LOGGER.info("Used ID : " + ID_ATT);
		}

		if (cmd.hasOption(OUTPUT_FILE_NAME_ARGS)) {
			OUTPUT_FILE_NAME = cmd.getOptionValue(cmd.getOptionValue(OUTPUT_FILE_NAME_ARGS).toString());
		}
		LOGGER.info("Output folder name : " + OUTPUT_FILE_NAME);

		File outputFolder = new File(outputFile);

		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		// String folderOut, String roadFile, IFeatureCollection<IFeature>
		// buildings, String parcelFile, double stepXY, double stepZ, String
		// fileOutPoint, double maxDist, String attID

		run(OUTPUT_FILE_NAME, featCollTrajectory, featCollBuildings, parcelFileName, stepXY, stepZ, outputFolder,
				radius, ID_ATT);

	}

	private final static String BUILDING_FILE_ARGS = "buildings"; // OK
	private final static String ROAD_FILE_ARGS = "" + "trajectory"; // OK
	private final static String OUTPUT_FILE_ARGS = "output"; // OK
	private final static String PARCEL_FILE_ARGS = "parcels"; // OK
	private final static String EXTRUDE_BUILINDGS_ARGS = "extrude";
	private final static String MAX_DIST_ARGS = "d";

	private final static String XY_STEP_ARGS = "sXY";
	private final static String Z_STEP_ARGS = "sZ";

	private final static String ID_ARGS = "id";
	private final static String OUTPUT_FILE_NAME_ARGS = "o";
	private final static String HELP_ARGS = "h";

	private static String ID_ATT = "ID";
	private static String OUTPUT_FILE_NAME = "out";

	private static Options configHelpParameters() {
		Options options = new Options();

		Option help = new Option(HELP_ARGS, false, "Help");
		help.setRequired(false);
		options.addOption(help);

		return options;

	}

	private static Options configFirstParameters() {
		Options options = new Options();

		Option buildings = new Option(BUILDING_FILE_ARGS, true, "Building file in shapefile");
		buildings.setRequired(true);
		buildings.setArgName("building-file");
		options.addOption(buildings);

		Option points = new Option(ROAD_FILE_ARGS, true, "Shapefile with the considered trajectory");
		points.setRequired(true);
		points.setArgName("trajectory-file");
		options.addOption(points);

		Option output = new Option(OUTPUT_FILE_ARGS, true, "Output folder");
		output.setRequired(true);
		output.setArgName("output-folder");
		options.addOption(output);

		Option parcels = new Option(PARCEL_FILE_ARGS, true,
				"Parcel file that allow filter buildings to keep only building on the front parcel");
		parcels.setRequired(false);
		parcels.setArgName("parcel-file");
		options.addOption(parcels);

		Option extrudeBuildings = new Option(EXTRUDE_BUILINDGS_ARGS, true,
				"Extrude buildings according to an attribute");
		extrudeBuildings.setRequired(false);
		extrudeBuildings.setArgName("extrude-attribute");
		options.addOption(extrudeBuildings);

		Option radius = new Option(MAX_DIST_ARGS, true, "Maximal distance (50 m by default)");
		radius.setArgName("maximaldistance");
		radius.setRequired(false);
		options.addOption(radius);

		Option s = new Option(XY_STEP_ARGS, true, "Planimetric step (4 m by default)");
		s.setArgName("StepXY");
		s.setRequired(false);
		options.addOption(s);

		Option sZ = new Option(Z_STEP_ARGS, true, "Altimetric step (4 m by default)");
		sZ.setArgName("StepZ");
		sZ.setRequired(false);
		options.addOption(sZ);

		Option id = new Option(ID_ARGS, true, "ID of building feature (ID by default).");
		id.setRequired(false);
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

	public static void run(String outFileName, IFeatureCollection<IFeature> trajectories,
			IFeatureCollection<IFeature> buildings, String parcelFile, double stepXY, double stepZ, File outputFolder,
			double maxDist, String attID) {

		BuildingProfileParameters.ID = attID;
		// Mandatory due to precision trunk in Geoxygene core
		DirectPosition.PRECISION = 10;

		IFeatureCollection<IFeature> parcelles = null;

		if (parcelFile != null) {
			parcelles = ShapefileReader.read(parcelFile);
		}

		Profile profile = new Profile(trajectories,
				// Set of contigus roads from which the profil is calculated
				buildings,
				// 3D buildings used

				parcelles);
		profile.setXYStep(stepXY);
		profile.setZStep(stepZ);
		profile.setLongCut(maxDist);

		//StreetProfilRenderer sPR = new StreetProfilRenderer();

		//sPR.display(profile);

		// Data loading, if parcels have no z they are translated to the minimal
		// z of the scene
		profile.loadData(false);

		// Calculation of the profilLe
		// The results may be acccessible by getPproj method
		// They are represented by 2D points with X = curvilinear abscissa et Y
		// = height
		// (the value is positive or negative according to the side of the orad)
		// height is measured according to an origin based on minimal height but
		// my be
		// parametrized by profile.setYProjectionShifting
		profile.process();

		//sPR.updateDisplay(profile);

		System.out.println("Updated");

		// Point export
		profile.exportPoints(outputFolder + "/" + outFileName);

	}

}
