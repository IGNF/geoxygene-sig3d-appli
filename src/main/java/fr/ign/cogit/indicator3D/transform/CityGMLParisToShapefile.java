package fr.ign.cogit.indicator3D.transform;

import java.io.File;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import org.citygml4j.xml.io.reader.CityGMLReadException;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.Context;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.LoaderCityGML;
import fr.ign.cogit.geoxygene.sig3d.representation.citygml.representation.CG_StyleGenerator;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class CityGMLParisToShapefile {
	
	
	public static void main(String[] args) throws CityGMLReadException, JAXBException{
		
		//Root folder where the tiles are contained
		String tileFolder = "/home/mbrasebin/Documents/Donnees/Paris/";
		//TileFile
		String tileFile = tileFolder + "dalles.shp";
		String nameAttFile = "NomFich";
		String fileCityGMLName = "ZoneAExporter.gml";
		//Zone to keep (First feature used)
		String cutFile = "/home/mbrasebin/Documents/Donnees/Paris/cut.shp";
		
		
		//ShapefileOut
		String outShapeFile = "/tmp/out.shp";
		
		//////Parametering CityGMLReader
		LoaderCityGML.CLEAN_GEOX_GEOM = false;
		CG_StyleGenerator.LOAD_TEXTURE = false;
		Context.LOD_REP = 2;
		
		//Converter parameter
		boolean separateBuilding = true;
		
		//Feature collection out
		IFeatureCollection<IFeature> featCollOut = new FT_FeatureCollection<>();
		
		//Tile Collection
		IFeatureCollection<IFeature> featTileCollection = ShapefileReader.read(tileFile);
		//Initialising spatil index
		featTileCollection.initSpatialIndex(Tiling.class, false);
		
		//Cut collection
		IFeatureCollection<IFeature> cutCollection = ShapefileReader.read(cutFile);
		
		//We select the tiles that intersects the cut
		Collection<IFeature> collTileSelectd = featTileCollection.select(cutCollection.get(0).getGeom());
		
		System.out.println("Number of concerned tile ; " + collTileSelectd);
		
		for(IFeature feat : collTileSelectd){
			String fileName = feat.getAttribute(nameAttFile).toString();
			
			String cityGMLFile = tileFolder+ fileName + "/export-CityGML/" +fileCityGMLName ;
			
			File f = new File(cityGMLFile);
			
			if(!f.exists()){
				System.out.println("File does not exist : " + cityGMLFile);
				continue;
			}
			
			VectorLayer vl = LoaderCityGML.read(new File(cityGMLFile), null, "Layer");
			IFeatureCollection<IFeature> currentFeatureCollection= CityGMLToShapeFile.convertToFeatureCollection(vl, separateBuilding);
			if(currentFeatureCollection != null){
				featCollOut.addAll(currentFeatureCollection);
			}
			
		}
		
		ShapefileWriter.write(featCollOut, outShapeFile);
	}

}
