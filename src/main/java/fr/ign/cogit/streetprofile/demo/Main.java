package fr.ign.cogit.streetprofile.demo;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.Profile;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.streetprofile.visu.StreetProfilRenderer;

/**
 * Classe de démonstration permettant d'exploiter les résultats du stage de
 * Marina Fund (recherche.ign.fr/labos/cogit/publiCOGITDetail.php?idpubli=5214)
 * 
 * Classe de test des fonctions de tracé de séquences urbaine Stage de Marina
 * Fund : USAGE D'INDICATEUR 3D ET AMENAGEMENT URBAIN
 * 
 * Cette classe utilise les fichiers de démonstration et les paramètres définis
 * dans BuildingProfilDemoParamters
 * 
 * 
 * Test class to generate data from Marin Fund trainingship
 * (recherche.ign.fr/labos/cogit/publiCOGITDetail.php?idpubli=5214)
 * 
 * This class use data declared in BuildingProfilDemoParamters class
 * 
 * @author MFund
 * @author YMeneroux
 * @author MBrasebin
 * @author JPerret
 * 
 */
public class Main {

	private static boolean VIEW_DATA = false;

	public static void main(String[] args) {

		// Settings of out folder
		BuildingProfilExecParamters.FOLDER_OUT = "/home/mickael/temp/";

		double stepZ = 4;
		double stepXY = 4;

		double circleRadius = 100.0;

		double maxDist = 50;

		run(BuildingProfilExecParamters.FOLDER_OUT, BuildingProfilExecParamters.FILE_IN_ROADS_DEMO,
				BuildingProfilExecParamters.FILE_IN_BUILDING_DEMO, BuildingProfilExecParamters.FILE_IN_PARCELS_DEMO, stepXY, stepZ,
				BuildingProfilExecParamters.FILE_OUT_POINTS_DEMO, BuildingProfilExecParamters.FILE_OUT_POLYGON_DEMO,
				circleRadius, maxDist);
	}

	public static void run(String folderOut, String roadFile, String buildingFile, String parcelFile, double stepXY,
			double stepZ, String fileOutPoint, String fileOutPolygon, double circleRadius, double maxDist) {

		// Mandatory due to precision trunk in Geoxygene core
		DirectPosition.PRECISION = 10;

		IFeatureCollection<IFeature> parcelles = null;

		if (parcelFile != null) {
			parcelles = ShapefileReader.read(parcelFile);
		}
		Profile profile = new Profile(ShapefileReader.read(roadFile),
				// Set of contigus roads from which the profil is calculated
				ShapefileReader.read(buildingFile),
				// 3D buildings used
			
				parcelles
		);
		profile.setXYStep(stepXY);
		profile.setZStep(stepZ);
		profile.setLongCut(maxDist);
		// Data loading, if parcels have no z they are translated to the minimal
		// z of the scene
		profile.loadData();

		// This lines allows the visualisation of the scene
		StreetProfilRenderer sPR = new StreetProfilRenderer();
		if (VIEW_DATA) {

			sPR.display(profile);
		}

		// Calculation of the profilLe
		// The results may be acccessible by getPproj method
		// They are represented by 2D points with X = curvilinear abscissa et Y
		// = height
		// (the value is positive or negative according to the side of the orad)
		// height is measured according to an origin based on minimal height but
		// my be
		// parametrized by profile.setYProjectionShifting
		profile.process();

		// Update in the visualisation if available
		if (VIEW_DATA) {
			sPR.updateDisplay(profile);
		}

		// Point export
		profile.exportPoints(folderOut + fileOutPoint);

		// Point export as circle
		profile.exportAsCircle(folderOut + fileOutPolygon, circleRadius);

	}

}
