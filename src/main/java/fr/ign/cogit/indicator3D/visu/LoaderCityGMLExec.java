package fr.ign.cogit.indicator3D.visu;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.citygml4j.xml.io.reader.CityGMLReadException;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.convert.FromGeomToSurface;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.gui.MainWindow;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.Context;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.LoaderCityGML;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_AbstractBoundarySurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_AbstractBuilding;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_Building;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_GroundSurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_RoofSurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_WallSurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.relief.CG_AbstractReliefComponent;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.relief.CG_ReliefFeature;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.relief.CG_TINRelief;
import fr.ign.cogit.geoxygene.sig3d.representation.citygml.representation.CG_StyleGenerator;
import fr.ign.cogit.geoxygene.sig3d.semantic.Map3D;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_TriangulatedSurface;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiSurface;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

/**
 * 
 * Class to load CityGML adapted to 3D Data
 * 
 * @author mbrasebin
 *
 */
public class LoaderCityGMLExec {

	public static void main(String[] args) {

		try {

			LoaderCityGML.CLEAN_GEOX_GEOM = false;

			String fileName = "ZoneAExporter.gml";

			String folder = "/home/mbrasebin/Documents/Donnees/Paris/EXPORT_1288-13725/export-CityGML/";

			String path = folder + fileName;
			String folderImage = folder;

			CG_StyleGenerator.LOAD_TEXTURE = true;

			Context.LOD_REP = 2;

			VectorLayer vl = LoaderCityGML.read(new File(path), folderImage, "Layer");

			MainWindow win = new MainWindow();
			Map3D carte = win.getInterfaceMap3D().getCurrent3DMap();

			System.out.println(vl.size());

			carte.addLayer(vl);

		} catch (CityGMLReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void exportShapeFile(VectorLayer vl, String fileout, boolean separateBuilding) {

		IFeatureCollection<IFeature> featCOut = new FT_FeatureCollection<>();

		int count = 0;

		for (IFeature feat : vl) {

			if (feat instanceof CG_Building) {

				CG_AbstractBuilding building = (CG_AbstractBuilding) feat;

				if (building.isSetConsistsOfBuildingPart()) {
					building = building.getConsistsOfBuildingPart().get(0);

					if (building.getConsistsOfBuildingPart().size() > 1) {
						System.out.println("CAS NON PRIS EN COMPTE PAR FLEMME");
						System.exit(0);
					}

				}

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

			} else if (feat instanceof CG_ReliefFeature) {

				CG_ReliefFeature relief = (CG_ReliefFeature) feat;

				for (CG_AbstractReliefComponent rF : relief.getReliefComponent()) {

					if (rF instanceof CG_TINRelief) {

						CG_TINRelief tin = (CG_TINRelief) rF;

						GM_TriangulatedSurface tS = tin.getTin();

						List<IPolygon> lT = tS.getlPolygons();

						featCOut.add(new DefaultFeature(new GM_MultiSurface<>(lT)));

					} else {
						System.out.println("CG_AbstractReliefComponent class not found : " + feat.getClass());
					}

				}

				System.out.println();

			} else {
				System.out.println("FEATURE class not found : " + feat.getClass());
			}

		}
		ShapefileWriter.write(featCOut, fileout);

	}

}