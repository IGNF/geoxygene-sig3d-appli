package fr.ign.cogit.task.profile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import fr.ign.cogit.exec.ProfileCalculation;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.Profile;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.pattern.Pattern;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.pattern.ProfilePatternDetector;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.stats.ProfileAutoCorrelation;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.stats.ProfileBasicStats;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.stats.ProfileMoran;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.stats.ProfileMultiDimensionnalCorrelation;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class ProfileTask {

	public static void main(String[] args) throws Exception {
		File folderOut = new File("/home/mbrasebin/tmp/test/");
		File roadsFile = new File(
				"/home/mbrasebin/.openmole/ZBOOK-SIGOPT-2016/webui/projects/ProfileDistribution/data/44674/road.shp");
		File buildingsFile = new File(
				"/home/mbrasebin/.openmole/ZBOOK-SIGOPT-2016/webui/projects/ProfileDistribution/data/44674/buildings.shp");

		String dirName = "44674";

		double stepXY = 1;
		double stepZ = 1;
		double maxDist = 500;

		double correlationThreshold = 0.8;

		int minimalPeriod = 20;

		String heightAttribute = "HAUTEUR";

		run(folderOut, roadsFile, buildingsFile, stepXY, stepZ, maxDist, correlationThreshold, minimalPeriod,
				heightAttribute, dirName);
	}

	public static File runDefault(File folderOut, File folderIn, double stepXY, double stepZ, double maxDist,
			double correlationThreshold, int minimalPeriod, String heightAttribute, String dirName) throws Exception {
		return run(folderOut, new File(folderIn, "road.shp"), new File(folderIn, "buildings.shp"), stepXY, stepZ,
				maxDist, correlationThreshold, minimalPeriod, heightAttribute, dirName);

	}

	public static File run(File folderOut, File roadsFile, File buildingsFile, double stepXY, double stepZ,
			double maxDist, double correlationThreshold, int minimalPeriod, String heightAttribute, String dirName)
			throws Exception {

		// Preparing outputfolder
		System.out.println("folder out = " + folderOut);
		if (!folderOut.exists()) {
			folderOut.mkdirs();
			if (folderOut.exists())
				System.out.println("I had to create it though");
			else {
				System.out.println("I could not create it...");
				throw new Exception("Could not create temp directory");
			}
		} else {
			System.out.println("We're all good!");
		}

		// Reading roads
		System.out.println("Reading roads ");
		IFeatureCollection<IFeature> roads = readShapefile(roadsFile.getParentFile(), roadsFile.getName());

		if (roads == null || roads.isEmpty()) {
			return folderOut;
		}

		System.out.println("Reading buildings");

		// Reading and extruding buildings
		IFeatureCollection<IFeature> buildings = ProfileCalculation.prepareBuildingCollection(
				getFileName(buildingsFile.getParentFile(), buildingsFile.getName()), heightAttribute);

		if (buildings == null || buildings.isEmpty()) {
			return folderOut;
		}

		DirectPosition.PRECISION = 10;
		// Preparing profile
		Profile profile = new Profile(roads,
				// Set of contigus roads from which the profil is calculated
				buildings,
				// 3D buildings used

				null);

		// Setting attributes
		profile.setXYStep(stepXY);
		profile.setZStep(stepZ);
		profile.setLongCut(maxDist);

		profile.setDisplayInit(true);

		System.out.println("Loading data");
		profile.loadData(false);
		System.out.println("Processing");
		profile.process();

		System.out.println("Writing output");
		// Writing point profile
		String fileName = folderOut + "/outprofile.shp";
		profile.exportPoints(fileName);

		// Writing points on geographic coordinate system
		IFeatureCollection<IFeature> ft1 = profile.getBuildingSide1();
		IFeatureCollection<IFeature> ft2 = profile.getBuildingSide2();

		IFeatureCollection<IFeature> featCollPointOut = new FT_FeatureCollection<>();
		if (ft1 != null && !ft1.isEmpty()) {
			featCollPointOut.addAll(ft1);
		}

		if (ft2 != null && !ft2.isEmpty()) {
			featCollPointOut.addAll(ft2);
		}

		////////////////////// Writing shapefile output

		System.out.println("Export points");
		ShapefileWriter.write(featCollPointOut, folderOut + "/outpoints.shp");

		System.out.println("Export debug");
		IFeatureCollection<IFeature> featCOut = profile.getFeatOrthoColl();
		ShapefileWriter.write(featCOut, folderOut + "/debug.shp");

		///////////////// WRITING STATISTICS

		////////////// GLOBAL STATISTICS

		///////////////////////////////// UPPER PROFILE STATS

		System.out.println("Export global upper");
		writeGlobalStats(profile, Profile.SIDE.UPSIDE, folderOut, dirName, maxDist);

		///////////////////////////////// DOWN PROFILE STATS
		System.out.println("Export global down");
		writeGlobalStats(profile, Profile.SIDE.DOWNSIDE, folderOut, dirName, maxDist);

		////////////// LOCAL STATISTICS

		///////////////////////////////// UPPER PROFILE STATS

		System.out.println("Export local upper");
		writeLocalStats(profile, Profile.SIDE.UPSIDE, folderOut, dirName, minimalPeriod, correlationThreshold);

		///////////////////////////////// DOWN PROFILE STATS
		System.out.println("Export local dower");
		writeLocalStats(profile, Profile.SIDE.DOWNSIDE, folderOut, dirName, minimalPeriod, correlationThreshold);

		System.out.println("Taks end");
		return folderOut;
	}

	public static void writeLocalStats(Profile profile, Profile.SIDE s, File folderOut, String dirName,
			int minimalPeriod, double correlationThreshold) throws IOException {

		ProfilePatternDetector pPD = new ProfilePatternDetector(minimalPeriod);
		// HEADER : "dirName;begin;length;repeat;correlation"

		BufferedWriter writerPattern = new BufferedWriter(new FileWriter(new File(folderOut, "patternOut.csv"), true));

		HashMap<Integer, List<Pattern>> patternListUp = pPD.patternDetector(profile, s, correlationThreshold);

		if (!patternListUp.isEmpty()) {

			for (Integer length : patternListUp.keySet()) {

				List<Pattern> lP = patternListUp.get(length);

				for (Pattern p : lP) {

					writerPattern.append(dirName + ";");
					writerPattern.append(s + ";");
					writerPattern.append(p.getIndexBegin() + ";");
					writerPattern.append(p.getLength() + ";");
					writerPattern.append(p.getRepeat() + ";");
					writerPattern.append(p.getCorrelationScore() + "\n");

				}

			}

		}

		writerPattern.close();

	}

	public static void writeGlobalStats(Profile profile, Profile.SIDE s, File folderOut, String dirName, double maxDist)
			throws IOException {

		List<Double> heights = profile.getHeightAlongRoad(s);

		if (heights == null || heights.isEmpty()) {
			return;
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(folderOut, "output.csv"), true));

		// HEADER = "dirname;side;minH;maxH;avgH;medH;moran;"

		// Identifing information
		writer.append(dirName + ";");
		writer.append(s + ";");

		// Moran up
		ProfileMoran pM = new ProfileMoran();
		pM.calculate(heights);
		double moranProfileValue = pM.getMoranProfileFinal();

		// Basic stats
		ProfileBasicStats pBS = new ProfileBasicStats();
		pBS.calculate(heights);

		// Height autocorrelation : up
		ProfileAutoCorrelation pAC = new ProfileAutoCorrelation();
		pAC.calculateACF(heights);
		pAC.calculateMethodYin(heights);

		// Height Depth Autocorrelation
		ProfileMultiDimensionnalCorrelation pMDC = new ProfileMultiDimensionnalCorrelation();
		pMDC.calculate(profile, s, maxDist, pBS.getMax());

		writer.append(pBS.getMin() + ";");
		writer.append(pBS.getMax() + ";");
		writer.append(pBS.getMoy() + ";");
		writer.append(pBS.getMed() + ";");

		writer.append(moranProfileValue + " \n");

		writer.close();
	}

	private static File findFile(File folder, String filename){
		for ( File file : folder.listFiles() ){
			if ( file.getName().matches("(?i)"+filename)){
				return file;
			}
		}
		return null;
	}
		
		
	private static String getFileName(File folder, String filename) {
		File file = findFile(folder, filename);
		if (file == null) {
			return null;
		}
		return file.toString();
	}

	private static IFeatureCollection<IFeature> readShapefile(File folder, String filename) {
		String name = getFileName(folder, filename);
		if (null == name) {

			return new FT_FeatureCollection<>();
		}

		return ShapefileReader.read(name);
	}

}
