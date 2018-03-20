package fr.ign.cogit.indicator3D.transform;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.citygml4j.xml.io.reader.CityGMLReadException;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.convert.FromGeomToSurface;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.Context;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.LoaderCityGML;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_AbstractBoundarySurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_AbstractBuilding;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_Building;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_GroundSurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_RoofSurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_WallSurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.relief.CG_ReliefFeature;
import fr.ign.cogit.geoxygene.sig3d.representation.citygml.representation.CG_StyleGenerator;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiSurface;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class CityGMLToShapeFile {

	public static void main(String[] args) throws CityGMLReadException, JAXBException {
		// cleaning geometry (
		LoaderCityGML.CLEAN_GEOX_GEOM = false;

		// Path to citygml
		String fileName = "ZoneAExporter.gml";

		// CityGML folder
        String folder = "/media/paulchapron/Data/DATA-Bati_3D/Paris/EXPORT_1286-13723/export-CityGML/";
		String path = folder + fileName;

		// Path to save shapefile
		String fileOut = "/tmp/out.shp";

		// Indicate that buildings are splitted between wall and roof
		boolean separateBuilding = true;

		// folder image (but theorically not required)
		String folderImage = folder;
		// We only need geometry
		CG_StyleGenerator.LOAD_TEXTURE = false;

		// !level of detail
		Context.LOD_REP = 2;

		VectorLayer vl = LoaderCityGML.read(new File(path), folderImage, "Layer", false);

		exportShapeFile(vl, fileOut, separateBuilding);

	}

	public static void exportShapeFile(VectorLayer vl, String fileout, boolean separateBuilding) {
		IFeatureCollection<IFeature> featCOut = convertToFeatureCollection(vl, separateBuilding);
		ShapefileWriter.write(featCOut, fileout);

	}
	
	public static IFeatureCollection<IFeature> convertToFeatureCollection(VectorLayer vl, boolean separateBuilding){

		IFeatureCollection<IFeature> featCOut = new FT_FeatureCollection<>();

		for (IFeature feat : vl) {

			if (feat instanceof CG_Building) {

				CG_AbstractBuilding abstractBuilding = (CG_AbstractBuilding) feat;

				for (CG_AbstractBuilding building : abstractBuilding.getConsistsOfBuildingPart()) {

					List<CG_AbstractBoundarySurface> lABS = building.getBoundedBySurfaces();

					if (separateBuilding) {
						for (CG_AbstractBoundarySurface aBS : lABS) {

							IGeometry geom = aBS.getLod2MultiSurface();

							if (aBS instanceof CG_WallSurface) {

								IFeature featOut = new DefaultFeature(geom);

								AttributeManager.addAttribute(featOut, "Type", "W", "String");

							} else if (aBS instanceof CG_RoofSurface) {

								IFeature featOut = new DefaultFeature(geom);
								AttributeManager.addAttribute(featOut, "Type", "R", "String");
								featCOut.add(featOut);

							} else if (aBS instanceof CG_GroundSurface) {

								IFeature featOut = new DefaultFeature(geom);
								AttributeManager.addAttribute(featOut, "Type", "G", "String");
								featCOut.add(featOut);

							} else {
								System.out.println("Class boundingby not found " + aBS.getClass().toString());

							}

						}
					} else {

						IMultiSurface<IOrientableSurface> ims = new GM_MultiSurface<>();

						for (CG_AbstractBoundarySurface aBS : lABS) {

							IGeometry geom = aBS.getLod2MultiSurface();
							List<IOrientableSurface> lOS = FromGeomToSurface.convertGeom(geom);
							ims.addAll(lOS);

						}

						IFeature featOut = new DefaultFeature(ims);

						AttributeManager.addAttribute(featOut, "ID", building.getIdentifiant(), "String");
						featCOut.add(featOut);
					}

				}
			} else if (feat instanceof CG_ReliefFeature) {

				continue;

				/*
				 * CG_ReliefFeature relief = (CG_ReliefFeature) feat;
				 * 
				 * for (CG_AbstractReliefComponent rF :
				 * relief.getReliefComponent()) {
				 * 
				 * if (rF instanceof CG_TINRelief) {
				 * 
				 * CG_TINRelief tin = (CG_TINRelief) rF;
				 * 
				 * GM_TriangulatedSurface tS = tin.getTin();
				 * 
				 * List<IPolygon> lT = tS.getlPolygons();
				 * 
				 * featCOut.add(new DefaultFeature(new GM_MultiSurface<>(lT)));
				 * 
				 * } else { System.out.
				 * println("CG_AbstractReliefComponent class not found : " +
				 * feat.getClass()); }
				 * 
				 * }
				 * 
				 * System.out.println();
				 */

			} else {
				System.out.println("FEATURE class not found : " + feat.getClass());
			}

		}
		
		return featCOut;
	}

}
