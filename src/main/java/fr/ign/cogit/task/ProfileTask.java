package fr.ign.cogit.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

import fr.ign.cogit.exec.ProfileCalculation;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.Profile;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.Profile.SIDE;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.stats.ProfileAutoCorrelation;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.stats.ProfileBasicStats;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.stats.ProfileMoran;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.stats.ProfileMultiDimensionnalCorrelation;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.simplu3d.util.FileLocator;

public class ProfileTask {

	public static void main(String[] args) throws Exception {
		File folderOut = new File("/tmp/test/");
		File roadsFile = new File(
				"/home/mbrasebin/Documents/Code/GeOxygene/geoxygene-sig3d-appli/script/out/81812/road.shp");
		File buildingsFile = new File(
				"/home/mbrasebin/Documents/Code/GeOxygene/geoxygene-sig3d-appli/script/out/81812/buildings.shp");

		double stepXY = 1;
		double stepZ = 1;
		double maxDist = 500;

		String heightAttribute = "HAUTEUR";

		run(folderOut, roadsFile, buildingsFile, stepXY, stepZ, maxDist, heightAttribute);
	}

	public static File runDefault(File folderOut, File folderIn, double stepXY, double stepZ, double maxDist,
			String heightAttribute) throws Exception {
		return run(folderOut, new File(folderIn, "road.shp"), new File(folderIn, "buildings.shp"), stepXY, stepZ,
				maxDist, heightAttribute);

	}

	public static File run(File folderOut, File roadsFile, File buildingsFile, double stepXY, double stepZ,
			double maxDist, String heightAttribute) throws Exception {

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
		IFeatureCollection<IFeature> roads = readShapefile(roadsFile.getParentFile(), roadsFile.getName());

		if (roads == null || roads.isEmpty()) {
			return folderOut;
		}

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

		profile.loadData(false);
		profile.process();

		//Writing point profile
		String fileName = folderOut + "/out_profile.shp";
		profile.exportPoints(fileName);

		
		//Writing points on geographic coordinate system
		IFeatureCollection<IFeature> ft1 = profile.getBuildingSide1();
		IFeatureCollection<IFeature> ft2 = profile.getBuildingSide2();

		IFeatureCollection<IFeature> featCollPointOut = new FT_FeatureCollection<>();
		if (ft1 != null && !ft1.isEmpty()) {
			featCollPointOut.addAll(ft1);
		}

		if (ft2 != null && !ft2.isEmpty()) {
			featCollPointOut.addAll(ft2);
		}

	    List<Double> heightsUP = profile.getHeightAlongRoad(SIDE.UPSIDE);

	    //////////////////////Writing shapefile output
	    

		ShapefileWriter.write(featCollPointOut, folderOut + "/out_points.shp");

		IFeatureCollection<IFeature> featCOut = profile.getFeatOrthoColl();
		ShapefileWriter.write(featCOut, folderOut + "/debug.shp");
		
		
		
		
		/////////////////WRITING STATISTICS
		
	    

	    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(folderOut, "stats.txt"), true));
	    
	    /////////////////////////////////UPPER PROFILE STATS
		
	    
	    if(heightsUP != null && ! heightsUP.isEmpty()){

		    //Moran up
		    ProfileMoran pMUP = new ProfileMoran();
		    pMUP.calculate(heightsUP);
		    double moranProfileUp = pMUP.getMoranProfileFinal();
		   
		    
		    
		    
		    //Basic stats
		    ProfileBasicStats pBSUp = new ProfileBasicStats();
		    pBSUp.calculate(heightsUP);
		    
		    //Height autocorrelation : up
		    ProfileAutoCorrelation pACUp = new ProfileAutoCorrelation();
		    pACUp.calculateACF(heightsUP);
		    pACUp.calculateMethodYin(heightsUP);
		    
		    

		    
		    
		    //Height Depth Autocorrelation
		    ProfileMultiDimensionnalCorrelation pMDCUP = new ProfileMultiDimensionnalCorrelation();
		    pMDCUP.calculate(profile, SIDE.UPSIDE, maxDist, pBSUp.getMax());
		    
		    
		    writer.append("MinUp=" + pBSUp.getMin()+"\n");
		    writer.append("MaxUp=" + pBSUp.getMax()+"\n");
		    writer.append("MoyUp=" + pBSUp.getMoy()+"\n");
		    writer.append("MedUp=" + pBSUp.getMed()+"\n");
		    
		    writer.append("MorabUp="+moranProfileUp +"\n");
		    
		    
		    
		    writer.append("AutocorrelationUp="+  Arrays.toString(pACUp.getTabACF()) +"\n");
		    writer.append("AutocorrelationYinUP=" +  Arrays.toString(pACUp.getTabYIN()) +"\n");
		    
		    writer.append("AutocorrelationMultiPle=" + Arrays.toString( pMDCUP.getTabACF()) +"\n");
	    }
	    
	    /////////////////////////////////DOWN PROFILE STATS
	    
	    List<Double> heightsDown = profile.getHeightAlongRoad(SIDE.DOWNSIDE);
	    
	    
	    if(heightsDown != null && ! heightsDown.isEmpty()){
	    	
		    //Moran downs
		    ProfileMoran pDown = new ProfileMoran();
		    pDown.calculate(heightsDown);
		    double moranProfileDown = pDown.getMoranProfileFinal();
		    

		    
		    
		    //Basic stats2
		    ProfileBasicStats pBSDown = new ProfileBasicStats();
		    pBSDown.calculate(heightsDown);
		    
		    
		    //Height autocorrelation : down
		    ProfileAutoCorrelation pACDown = new ProfileAutoCorrelation();
		    pACDown.calculateACF(heightsDown);
		    pACDown.calculateMethodYin(heightsDown);
		    
		    ProfileMultiDimensionnalCorrelation pMDCDown = new ProfileMultiDimensionnalCorrelation();
		    pMDCDown.calculate(profile, SIDE.DOWNSIDE, maxDist, pBSDown.getMax());
		    
		    
		    writer.append("MinDown=" + pBSDown.getMin()+"\n");
		    writer.append("MaxDown=" + pBSDown.getMax()+"\n");
		    writer.append("MoyDown=" + pBSDown.getMoy()+"\n");
		    writer.append("MedDown=" + pBSDown.getMed()+"\n");


		    writer.append("MoranDown="+moranProfileDown +"\n");
		    
		    
		    writer.append("AutocorrelationUp="+ Arrays.toString(pACDown.getTabACF()) +"\n");
		    writer.append("AutocorrelationYinUP=" + Arrays.toString(pACDown.getTabYIN()) +"\n");
		    
		    writer.append("AutocorrelationMultiPle=" + Arrays.toString(pMDCDown.getTabACF()) +"\n");
	    	
	    }  

		     
		    writer.close();

		return folderOut;
	}

	private static String getFileName(File folder, String filename) {
		File file = FileLocator.findFile(folder, filename);
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
