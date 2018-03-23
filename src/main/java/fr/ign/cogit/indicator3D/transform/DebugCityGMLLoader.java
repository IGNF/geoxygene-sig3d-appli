package fr.ign.cogit.indicator3D.transform;

import java.io.File;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.Context;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.LoaderCityGML;
import fr.ign.cogit.geoxygene.sig3d.representation.citygml.representation.CG_StyleGenerator;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class DebugCityGMLLoader {
	public static void main(String[] args) {
		// Root folder where the tiles are contained
		String tileFolder = "/home/mbrasebin/Documents/Donnees/Paris/";
		// TileFile
		String tileFile = tileFolder + "dalles.shp";
		String nameAttFile = "NomFich";
		String fileCityGMLName = "ZoneAExporter.gml";
		// Zone to keep (First feature used)


		// ShapefileOut
		String outShapeFile = "/tmp/out.shp";

		////// Parametering CityGMLReader
		LoaderCityGML.CLEAN_GEOX_GEOM = false;
		CG_StyleGenerator.LOAD_TEXTURE = true;
		Context.LOD_REP = 2;

		// Converter parameter
	

		// Feature collection out
		IFeatureCollection<IFeature> featCollOut = new FT_FeatureCollection<>();

		// Tile Collection
		IFeatureCollection<IFeature> featTileCollection = ShapefileReader.read(tileFile);
		// Initialising spatil index

		int count = 0;
		for (IFeature tile : featTileCollection) {

			String fileName = tile.getAttribute(nameAttFile).toString();

			String folder = tileFolder + fileName + "/export-CityGML/";

			String cityGMLFile = folder + fileCityGMLName;

			System.out.println("--------------------------------");
			System.out.println((count++) + "/" + (featTileCollection.size()) + "    --  " + fileName);
			System.out.println("--------------------------------");

			File f = new File(cityGMLFile);

			if (!f.exists()) {
				System.out.println("File does not exist : " + cityGMLFile);
				continue;
			}

			try {
				 LoaderCityGML.read(new File(cityGMLFile), folder, "Layer", true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(f.getAbsolutePath());
				System.exit(1);
			}
		}

		ShapefileWriter.write(featCollOut, outShapeFile);
	}
}
