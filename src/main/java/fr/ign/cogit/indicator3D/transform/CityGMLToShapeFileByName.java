package fr.ign.cogit.indicator3D.transform;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.citygml4j.xml.io.reader.CityGMLReadException;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.Context;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.LoaderCityGML;
import fr.ign.cogit.geoxygene.sig3d.representation.citygml.representation.CG_StyleGenerator;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class CityGMLToShapeFileByName {

	
	public static void main(String[] args) throws CityGMLReadException, JAXBException {
		// cleaning geometry (
		LoaderCityGML.CLEAN_GEOX_GEOM = false;
	
		
		String[] dir ={"EXPORT_1303-13733", "EXPORT_1304-13733","EXPORT_1303-13732", "EXPORT_1304-13732"};

		// Path to citygml
		String fileName = "ZoneAExporter.gml";

		// CityGML folder
		String tileFolder = "/home/mbrasebin/Documents/Donnees/Paris/";



		// Path to save shapefile
		String fileOut = "/home/mbrasebin/tmp/out.shp";

		// Indicate that buildings are splitted between wall and roof
		boolean separateBuilding = false;


		// We only need geometry
		CG_StyleGenerator.LOAD_TEXTURE = false;

		// !level of detail
		Context.LOD_REP = 2;
		
		IFeatureCollection<IFeature> collOut = new FT_FeatureCollection<>();
		
		
		for(int i=0; i < dir.length;i++){
			String path = tileFolder + dir[i] + "/export-CityGML/" + fileName;
			VectorLayer vlTemp= LoaderCityGML.read(new File(path), null, "Layer", false);
			
			IFeatureCollection<IFeature> collTemp = CityGMLToShapeFile.convertToFeatureCollection(vlTemp,separateBuilding);
			
			
			System.out.println("Number of features temp : " + collTemp.size());
			
			
			collOut.addAll(collTemp);
		
		}

		System.out.println("Number of features : " + collOut.size());

		ShapefileWriter.write(collOut, fileOut);

	}
}
