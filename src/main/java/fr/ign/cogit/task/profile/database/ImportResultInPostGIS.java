package fr.ign.cogit.task.profile.database;

import java.io.File;
import java.sql.DriverManager;
import java.sql.Statement;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.io.vector.PostgisManager;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;

/**
 * Class to import the Results into the database used to prepare the data for
 * distribution
 * 
 * @author mbrasebin
 *
 */
public class ImportResultInPostGIS {

	public static String TABLE_NAME_RAY = "debug_ray";
	public static String TABLE_OUT_POINT = "out_points";
	public static String TABLE_OUT_PROFILE = "out_profiles";
	public static String TABLE_EXTRA_ROADS_INFO = "info_road";
	public static String PATTERN_DETECTED = "detected_pattern";

	public static String[] allTable = { TABLE_NAME_RAY, TABLE_OUT_POINT, TABLE_OUT_PROFILE, TABLE_EXTRA_ROADS_INFO,
			PATTERN_DETECTED };

	// This attribute is stored in all created table as it allows to join with
	// initial data
	public static String ATT_NAME_ID_ROAD = "id_road";

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// Folder where data is stored (it contains subfolder with id name)
		String folderOut = "/home/mbrasebin/.openmole/ZBOOK-SIGOPT-2016/webui/projects/ProfileDistribution/out2/";

		// Database information
		String host = "localhost";
		String port = "5432";
		String user = "postgres";
		String pw = "postgres";
		String database = "Lyon_Database";
		String schema = "public";

		// ERASE ALL TABLE INS THE DATABASE FROM allTable
		eraseExistingTable(host, port, database, user, pw, schema);
		initTable(host, port, database, user, pw, schema);
		
		
		File[] f = (new File(folderOut)).listFiles();

		
		int count = 0;
		for (File fTemp : f) {
			importDebugLine(fTemp, host, port, database, user, pw, schema);
			importPointOut(fTemp, host, port, database, user, pw, schema);
			importGlobalStats(fTemp, host, port, database, user, pw, schema);
			System.out.println("Number : " + (count ++) + " /  " + f.length);
			break;
		}

	}
	

	

	public static void eraseExistingTable(String host, String port, String database, String user, String pw,
			String schema) throws Exception {

		java.sql.Connection conn;

		try {

			// Création de l'URL de chargement
			String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

			// Connexion
			conn = DriverManager.getConnection(url, user, pw);

			// Requete sur la table contenant les colonnes
			// De géométrie PostGIS
			Statement s = conn.createStatement();

			for (String tableName : allTable) {
				String query = "DROP TABLE IF EXISTS " + schema + "." + tableName;
				s.execute(query);

			}

			s.close();
			conn.close();

		} catch (Exception e) {
			throw e;
		}

	}
	
	
	
	private static void initTable(String host, String port, String database, String user, String pw, String schema) throws Exception {
		// TODO Auto-generated method stub

		java.sql.Connection conn;

		try {

			// Création de l'URL de chargement
			String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

			// Connexion
			conn = DriverManager.getConnection(url, user, pw);

			// Requete sur la table contenant les colonnes
			// De géométrie PostGIS
			Statement s = conn.createStatement();
				
			//ID;UP/DOWN;MIN;MAX;MOY;MED;MORAN
				String query = "CREATE TABLE  " + schema + "." + TABLE_EXTRA_ROADS_INFO+ "  ("+ATT_NAME_ID_ROAD+" varchar(10), DIR  varchar(10), MIN double precision, MAX double precision,MOY double precision,MED double precision );";
				s.execute(query);

			

			s.close();
			conn.close();

		} catch (Exception e) {
			throw e;
		}

	}




	/**
	 * Import the line file into the database
	 * 
	 * @param fTemp
	 * @param host
	 * @param port
	 * @param database
	 * @param user
	 * @param pw
	 * @param schema
	 * @throws Exception
	 */
	private static void importDebugLine(File fTemp, String host, String port, String database, String user, String pw,
			String schema) throws Exception {
		

		
		importInDatabaseWithAttribute(fTemp, host, port, database, user, pw, schema, "debug.shp", TABLE_NAME_RAY);
	}
	
	

	
	
	/**
	 * Import the line file into the database
	 * 
	 * @param fTemp
	 * @param host
	 * @param port
	 * @param database
	 * @param user
	 * @param pw
	 * @param schema
	 * @throws Exception
	 */
	private static void importPointOut(File fTemp, String host, String port, String database, String user, String pw,
			String schema) throws Exception {
		importInDatabaseWithAttribute(fTemp, host, port, database, user, pw, schema, "out_points.shp", TABLE_OUT_POINT);
	}
	
	//ID;UP/DOWN;MIN;MAX;MOY;MED;MORAN
	private static void importGlobalStats(File fTemp, String host, String port, String database, String user, String pw,
			String schema) throws Exception {
		
		java.sql.Connection conn;

	try {

		// Création de l'URL de chargement
		String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

		// Connexion
		conn = DriverManager.getConnection(url, user, pw);

		// Requete sur la table contenant les colonnes
		// De géométrie PostGIS
		Statement s = conn.createStatement();

		for (String tableName : allTable) {
			String query = "COPY "+schema+"."+TABLE_EXTRA_ROADS_INFO+" FROM '"+ fTemp.getAbsolutePath()+"/" + "output.csv" + "' WITH (FORMAT csv)";
			s.execute(query);

		}

		s.close();
		conn.close();

	} catch (Exception e) {
		throw e;
	}
	
	
	}
	
	
	
	
	private static void importInDatabaseWithAttribute(File fTemp, String host, String port, String database, String user, String pw,
			String schema, String shapefileName, String tableName) throws Exception {
		String fName = fTemp.getName();
		String pathToFile = fTemp.getAbsolutePath() +  "/" + shapefileName;
		File fToRead = new File(pathToFile);

		if (fToRead.exists()) {
			IFeatureCollection<IFeature> featC = ShapefileReader.read(pathToFile);
			
			
			if(featC.isEmpty()) {
				System.out.println("File empty : " + pathToFile);
				return;
				
			}

			for (IFeature feat : featC) {
				AttributeManager.addAttribute(feat, ATT_NAME_ID_ROAD, fName, "String");
			}

			PostgisManager.insertInGeometricTable(host, port, database, schema,  tableName, user, pw, featC, true);

		} else {
			System.out.println("File does not exist : " + pathToFile);
		}
	}
}
