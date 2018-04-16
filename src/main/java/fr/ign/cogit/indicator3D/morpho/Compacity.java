package fr.ign.cogit.indicator3D.morpho;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ITriangle;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableSurface;
import fr.ign.cogit.geoxygene.sig3d.calculation.Util;
import fr.ign.cogit.geoxygene.sig3d.convert.geom.FromPolygonToTriangle;
import fr.ign.cogit.geoxygene.sig3d.convert.geom.Triangulation2D5;
import fr.ign.cogit.geoxygene.sig3d.geometry.Box3D;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_AbstractBoundarySurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_AbstractBuilding;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_Building;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_BuildingPart;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_RoofSurface;
import fr.ign.cogit.geoxygene.sig3d.model.citygml.building.CG_WallSurface;

public class Compacity {

	public static Double volumeOfCGBuilding(CG_Building oneOfBati) {

		CG_AbstractBuilding absBati = (CG_AbstractBuilding) oneOfBati;
		Double volBati = 0.0;

		Double zMinRoof = Double.POSITIVE_INFINITY;
		Double zMinWall = Double.POSITIVE_INFINITY;

		// aire du toit vue du dessus pour trouver son aire.
		Double roofSurface2D = 0.0;

		ArrayList<CG_BuildingPart> lBatiParts = (ArrayList<CG_BuildingPart>) absBati.getConsistsOfBuildingPart();

		for (CG_AbstractBuilding partie : lBatiParts) {
			System.out.println("partie " + (lBatiParts.indexOf(partie) + 1) + " / " + lBatiParts.size() + "\n");
			List<CG_AbstractBoundarySurface> boundaries = partie.getBoundedBySurfaces();
			System.out.println(boundaries.size() + " boundaries Surfaces");

			for (CG_AbstractBoundarySurface aBS : boundaries) {
				if (aBS instanceof CG_RoofSurface) {
					IMultiSurface<IOrientableSurface> geom = aBS.getLod2MultiSurface();
					System.out.println("### Surface de toit détectée");
					// geom.getList sort les surface orientable à partir de la geométrie
					//Désormais on triangule la surface avant de faire appel à cette fonction
					double volContrib = Util.volumeUnderSurface(convertToTriangleFromList(geom.getList()));

					System.out.println("contribution au volume de la surface de toit :  " + volContrib);
					volBati += volContrib;
					roofSurface2D += geom.area();
					System.out.println("aire 2D de la partie de toit " + geom.area());

					Box3D box = new Box3D(geom);
					zMinRoof = Math.min(zMinRoof, box.getLLDP().getZ());

				}
				if (aBS instanceof CG_WallSurface) {

					IMultiSurface<IOrientableSurface> geom = aBS.getLod2MultiSurface();
					Box3D box = new Box3D(geom);
					zMinWall = Math.min(zMinWall, box.getLLDP().getZ());
				}

			}

			System.out.println("Zmin des surfaces de toit: " + zMinRoof);
			System.out.println("Zmin des surfaces de mur: " + zMinWall);

			// volume compris entre le fond du batiment et le niveau Z=0
			// à retrancher au volumeUnderSurface pour avoir le vrai volume

			System.out.println("volume entre toit et niveau zero " + volBati);
			System.out.println("surface 2D toit applati " + roofSurface2D);
			System.out.println("volume entre surface 2D du toit applatie à   Zmin et le niveau Zero "
					+ (zMinRoof * roofSurface2D));

		}
		return (volBati - (zMinRoof * roofSurface2D));
	}

	public static Double surfaceOfCGBuilding(CG_Building oneOfBati) {

		CG_AbstractBuilding absBati = (CG_AbstractBuilding) oneOfBati;
		ArrayList<CG_BuildingPart> lBatiPart = (ArrayList<CG_BuildingPart>) absBati.getConsistsOfBuildingPart();

		Double totalWallSurf = 0.0;
		Double totalRoofSurf = 0.0;
		for (CG_AbstractBuilding partie : lBatiPart) {
			System.out.println("partie " + (lBatiPart.indexOf(partie) + 1) + " / " + lBatiPart.size() + "\n");
			List<CG_AbstractBoundarySurface> boundaries = partie.getBoundedBySurfaces();
			System.out.println(boundaries.size() + " boundaries Surfaces");

			for (CG_AbstractBoundarySurface aBS : boundaries) {
				if (aBS instanceof CG_WallSurface) {
					IMultiSurface<IOrientableSurface> geom = aBS.getLod2MultiSurface();
					// geom.getList sort les surface orientable à partir de la ImultiSurface
					double surfContribW = Util.areaTriangulatedSurface(convertToTriangle(geom));
					System.out.println("### Surface de mur détectée, contribution : " + surfContribW);
					totalWallSurf += surfContribW;
				}
				if (aBS instanceof CG_RoofSurface) {
					IMultiSurface<IOrientableSurface> geom = aBS.getLod2MultiSurface();
					double surfContribR = Util.areaTriangulatedSurface(convertToTriangle(geom));
					totalRoofSurf += surfContribR;
					System.out.println("### Surface de toit détectée, contribution : " + surfContribR);

				}

			}
			System.out.println("surface de murs " + totalWallSurf);
			System.out.println("surface de toit " + totalRoofSurf);
			System.out.println("surface erxterne " + totalWallSurf + totalRoofSurf);

		}
		return (totalRoofSurf + totalWallSurf);

	}

	// Relative compacity with Sphere as reference Volume
	public Double RelativeCompacitySphere(Double vol, Double surf) {
		// formule approchée : 4.84 * Volume ^2/3 / Aire
		return (4 * Math.PI * Math.pow(3. / (4 * Math.PI), 2.0 / 3.0) * Math.pow(vol, 2. / 3.) / surf);
	}
	// Relative compacity with Cube as reference Volume
    public Double RelativeCompacityCube(Double vol, Double surf) {
        return (6* Math.pow(vol, 2. / 3.) / surf);
    }
    // Relative compacity with hemisphere as reference Volume
    public Double RelativeCompacityDemiSphere(Double vol, Double surf) {
      // formule approchée : 3.83 * Volume ^2/3 / Aire
      return (2 * Math.PI * Math.pow(3. / (2 * Math.PI), 2.0 / 3.0) * Math.pow(vol, 2. / 3.) / surf);
     }

    
	
	
	
	public static List<ITriangle> convertToTriangle(IMultiSurface<IOrientableSurface> ims) {
		return convertToTriangleFromList(ims.getList());
	}

	public static List<ITriangle> convertToTriangleFromList(List<IOrientableSurface> list) {
		
			//On tente d'abord la triangulation rapide et simple (qui marche si la surface est triangulée ou avec 4 côté)
		List<ITriangle> lTriangles = FromPolygonToTriangle.convertAndTriangle(list);

		if (lTriangles == null) {
			//on fait appel à l'artillerie lourde sinon
			lTriangles = Triangulation2D5.triangulateFromList(list).getList();
		}

		return lTriangles;

	}

}
