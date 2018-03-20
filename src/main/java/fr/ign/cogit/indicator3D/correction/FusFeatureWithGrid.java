package fr.ign.cogit.indicator3D.correction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.ICurve;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.convert.FromGeomToLineString;
import fr.ign.cogit.geoxygene.convert.FromGeomToSurface;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.analysis.roof.RoofDetection;
import fr.ign.cogit.geoxygene.sig3d.convert.geom.FromPolygonToLineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiPoint;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiSurface;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;

/**
 * 
 * Class that allows to merge 3D features that was cut due to tiling.
 * 
 * 
 * @author mbrasebin
 *
 */
public class FusFeatureWithGrid {

	public static void main(String[] args) {

		String shapeFileWithCutGeometry = "/home/mbrasebin/tmp/out.shp";
		String tilingFile = "/home/mbrasebin/Documents/Donnees/Paris/dalles.shp";
		String shapeFileOut = "/home/mbrasebin/tmp/merge.shp";

		IFeatureCollection<IFeature> collectionOfCutGeometries = ShapefileReader.read(shapeFileWithCutGeometry);
		IFeatureCollection<IFeature> tilingFeature = ShapefileReader.read(tilingFile);

		// 1 : transforming tiling to ICurve collection
		IFeatureCollection<IFeature> lineTiling = transformToLineTiling(tilingFeature);

		// 2 : merge the geometries
		IFeatureCollection<IFeature> aggregatedLineString = aggregateToLineString(collectionOfCutGeometries, lineTiling,
				true, 0.5);

		ShapefileWriter.write(aggregatedLineString, shapeFileOut);

	}

	/**
	 * Transform a polygonal tiling file to a feature collection for which each
	 * feature has a segment geometry
	 */
	private static IFeatureCollection<IFeature> transformToLineTiling(IFeatureCollection<IFeature> tilingFeature) {
		CarteTopo ct = initializeCarteTopo(tilingFeature);

		IFeatureCollection<IFeature> featC = new FT_FeatureCollection<>();
		featC.addAll(ct.getPopArcs());
		// TODO Auto-generated method stub
		return featC;
	}

	private static CarteTopo initializeCarteTopo(IFeatureCollection<IFeature> tilingFeature) {
		// Initialisation d'une nouvelle CarteTopo
		CarteTopo carteTopo = new CarteTopo("opo");
		carteTopo.setBuildInfiniteFace(false);
		// Récupération des arcs de la carteTopo
		IPopulation<Arc> arcs = carteTopo.getPopArcs();
		// Import des arcs de la collection dans la carteTopo
		for (IFeature feature : tilingFeature) {

			List<ILineString> lLLS = FromPolygonToLineString
					.convertPolToLineStrings((IPolygon) FromGeomToSurface.convertGeom(feature.getGeom()).get(0));

			for (ILineString ls : lLLS) {

				// affectation de la géométrie de l'objet issu de la
				// collection
				// à l'arc de la carteTopo
				for (int i = 0; i < ls.numPoints() - 1; i++) {
					// création d'un nouvel élément
					Arc arc = arcs.nouvelElement();
					arc.setGeometrie(new GM_LineString(ls.getControlPoint(i), ls.getControlPoint(i + 1)));
					// instanciation de la relation entre l'arc créé et
					// l'objet
					// issu de la collection
					arc.addCorrespondant(feature);
				}

			}

		}

		carteTopo.creeNoeudsManquants(0.01);

		carteTopo.fusionNoeuds(0.2);

		carteTopo.decoupeArcs(0.1);
		carteTopo.splitEdgesWithPoints(0.1);

		carteTopo.filtreArcsDoublons();

		// Création de la topologie Arcs Noeuds

		carteTopo.creeTopologieArcsNoeuds(0.2);

		return carteTopo;

	}

	/**
	 * Algorithme permettant de fusionner les objets le long d'une ligne
	 * 
	 * @param featCollIn
	 * @param curve
	 * @return
	 */
	public static IFeatureCollection<IFeature> aggregateToLineString(IFeatureCollection<IFeature> featCollIn,
			IFeatureCollection<IFeature> tileCurve, boolean is3D, double threshold) {

		// Le résultat que l'on renvoie

		// Nous avons besoin des toits pour l'indexation spatiale
		IFeatureCollection<IFeature> featsRoof = null;

		if (is3D) {

			featsRoof = RoofDetection.detectRoof(featCollIn, 0.20, true);

			for (int i = 0; i < featsRoof.size(); i++) {

				IGeometry geom = featsRoof.get(i).getGeom();

				// Pas de toit, on ignore
				if (geom == null || geom.isEmpty()) {
					System.out.println("Pas de toit");
					featsRoof.remove(i);
					featCollIn.remove(i);

					i--;
					continue;

				}

				featsRoof.get(i).setGeom((new GM_MultiPoint(geom.coord())).buffer(1));

			}

		} else {
			featsRoof = new FT_FeatureCollection<IFeature>();
			featsRoof.addAll(featCollIn);
		}
		
		//ShapefileWriter.write(tileCurve, "/home/mbrasebin/tmp/line.shp");

		featsRoof.initSpatialIndex(Tiling.class, true);

		for (IFeature line : tileCurve) {

			ICurve c = (ICurve) FromGeomToLineString.convert(line.getGeom()).get(0);

			mergeForOneCurve(c, featsRoof, featCollIn, threshold);
			
			//System.out.println("Number of roof : " + featsRoof.size());
			//System.out.println("Number of buildings : " + featCollIn.size());
		}

		return featCollIn;

	}

	public static void mergeForOneCurve(ICurve curve, IFeatureCollection<IFeature> featsRoof,
			IFeatureCollection<IFeature> featCollIn, double threshold) {
		
		IGeometry buffer = curve.buffer(threshold);

		// On récupère les éléments de toit ssur la ligne
		Collection<IFeature> featRoofConcerned = featsRoof.select(buffer);// getIntersected(curve.buffer(0.7),featsRoof);

		// On ajoute les éléments pour former une collection
		IFeatureCollection<IFeature> featIntersected = new FT_FeatureCollection<IFeature>();

		IFeatureCollection<IFeature> featRoofIntersected = new FT_FeatureCollection<IFeature>();
		featRoofIntersected.addAll(featRoofConcerned);

		for (IFeature featToTreet : featRoofConcerned) {

			featIntersected.add(featCollIn.get(featsRoof.getElements().indexOf(featToTreet)));

		}

		for (IFeature featTemp : featIntersected) {

			featCollIn.remove(featTemp);

		}
		
		
		for (IFeature featToTreet : featRoofConcerned) {
			featsRoof.remove(featToTreet);
			
		}

		// On regarde quels éléments doivent fusionner entre eux
		boolean fus = true;

		boucleWhile: while (fus) {

			int nbConcerned = featRoofIntersected.size();

			for (int i = 0; i < nbConcerned; i++) {

				IFeature feat1 = featRoofIntersected.get(i);

				for (int j = i + 1; j < nbConcerned; j++) {
					IFeature feat2 = featRoofIntersected.get(j);

					if (feat1.getGeom().intersects(feat2.getGeom())) {
						
						IGeometry geomInter1 = feat1.getGeom().intersection(buffer);
						IGeometry geomInter2 = feat2.getGeom().intersection(buffer);
						
						if(geomInter1.distance(geomInter2) >threshold) {
							continue;
						}
						
						

						IMultiSurface<IOrientableSurface> mS = tryFusion(featIntersected.get(i).getGeom(),
								featIntersected.get(j).getGeom());

						IFeature featI = featIntersected.get(i);

						featIntersected.remove(j);
						featIntersected.remove(i);

						featRoofIntersected.remove(j);
						featRoofIntersected.remove(i);
						featI.setGeom(mS);
						featIntersected.add(featI);
						featRoofIntersected.add(new DefaultFeature(
								(new GM_MultiPoint(RoofDetection.detectRoof(mS, 0.2, true).coord())).buffer(0.5)));

						continue boucleWhile;

					}

				}

			}

			fus = false;
		}

		
		featsRoof.addAll(featRoofIntersected);
		featCollIn.addAll(featIntersected);

	}

	public static IFeatureCollection<IFeature> getIntersected(IGeometry geom,
			IFeatureCollection<IFeature> featToTreat) {

		IFeatureCollection<IFeature> featCollOutCollection = new FT_FeatureCollection<IFeature>();

		for (IFeature feat : featToTreat) {

			if (isIntersected(geom, feat)) {
				featCollOutCollection.add(feat);
			}

		}

		return featCollOutCollection;

	}

	@SuppressWarnings("unchecked")
	public static boolean isIntersected(IGeometry geom, IFeature feat) {

		IGeometry geom2 = feat.getGeom();

		List<IOrientableSurface> iOS = new ArrayList<IOrientableSurface>();

		if (geom2 instanceof IMultiSurface<?>) {
			iOS.addAll((GM_MultiSurface<IOrientableSurface>) geom2);
		} else if (geom2 instanceof IOrientableSurface) {
			iOS.add((IOrientableSurface) geom2);
		}

		for (IOrientableSurface surf : iOS) {

			if (surf.intersects(geom)) {
				return true;
			}

		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public static IMultiSurface<IOrientableSurface> tryFusion(IGeometry geom1, IGeometry geom2) {
		IMultiSurface<IOrientableSurface> mS = new GM_MultiSurface<IOrientableSurface>();

		if (geom1 instanceof IMultiSurface<?>) {

			mS.addAll((IMultiSurface<IOrientableSurface>) geom1);

		} else if (geom1 instanceof IPolygon) {

			mS.add((IPolygon) geom1);

		} else {
			System.out.println("Problem : geom1");
		}

		if (geom2 instanceof IMultiSurface<?>) {

			mS.addAll((IMultiSurface<IOrientableSurface>) geom2);

		} else if (geom2 instanceof IPolygon) {

			mS.add((IPolygon) geom2);

		} else {
			System.out.println("Problem : geom2");
		}

		return mS;
	}
}
