package fr.ign.cogit.task;

import java.io.File;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.referencing.cs.DefaultAffineCS;

import fr.ign.cogit.exec.ProfileCalculation;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.Profile;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.simplu3d.util.FileLocator;

public class ProfileTask {
	
	
	public static void main(String[] args) throws Exception{
		File folderOut = new File("/tmp/test/");
		File roadsFile = new File("/home/mbrasebin/Documents/Code/GeOxygene/geoxygene-sig3d-appli/script/out/81812/road.shp");
		File buildingsFile = new File("/home/mbrasebin/Documents/Code/GeOxygene/geoxygene-sig3d-appli/script/out/81812/buildings.shp");
		
		double stepXY = 1;
		double stepZ = 1;
		double maxDist = 500;
		
		String heightAttribute = "HAUTEUR";
		
		run(folderOut, roadsFile, buildingsFile, stepXY, stepZ, maxDist, heightAttribute);
	}
	
	
	public static void run(File folderOut, File roadsFile, File buildingsFile, double stepXY, double stepZ, double maxDist, String heightAttribute) throws Exception{
		
		
		
		//Preparing outputfolder
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
	    
	    
	    
		//Reading roads
		IFeatureCollection<IFeature> roads = readShapefile(roadsFile.getParentFile(), roadsFile.getName());
		
		
		if(roads == null ||roads.isEmpty()){
			return;
		}
		
		
		//Reading and extruding buildings
		IFeatureCollection<IFeature> buildings =    ProfileCalculation.prepareBuildingCollection(getFileName(buildingsFile.getParentFile(), buildingsFile.getName()), heightAttribute);
				
		
		if(buildings == null ||buildings.isEmpty()){
			return;
		}
		
		DirectPosition.PRECISION = 10;
		//Preparing profile
		Profile profile = new Profile(roads,
				// Set of contigus roads from which the profil is calculated
				buildings,
				// 3D buildings used

				null);
		
		//Setting attributes
		profile.setXYStep(stepXY);
		profile.setZStep(stepZ);
		profile.setLongCut(maxDist);

		//DEBUG
		profile.setDisplayInit(true);
		
		profile.loadData(false);
		profile.process();
		
		
		
		
		
		
		
		 String fileName = folderOut + "/out_profile.shp";			 
		profile.exportPoints(fileName);
		
		
		
		IFeatureCollection<IFeature> ft1 = profile.getBuildingSide1();
		IFeatureCollection<IFeature> ft2 = profile.getBuildingSide2();
		
		
		IFeatureCollection<IFeature> featCollPointOut = new FT_FeatureCollection<>();
		if(ft1 != null && ! ft1.isEmpty()){
			featCollPointOut.addAll(ft1);
		}
		
		if(ft2 != null && ! ft2.isEmpty()){
			featCollPointOut.addAll(ft2);
		}
		
		
		
		ShapefileWriter.write(featCollPointOut, folderOut + "/out_points.shp");
		
		
		

		
		IFeatureCollection<IFeature> featCOut = profile.getFeatOrthoColl();
		ShapefileWriter.write(featCOut, folderOut + "/debug.shp");
		
		
	}
	
	
	private static String getFileName(File folder, String filename){
		File file = FileLocator.findFile(folder, filename);
		if(file == null){
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
