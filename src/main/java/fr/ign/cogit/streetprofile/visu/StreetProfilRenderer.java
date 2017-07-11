package fr.ign.cogit.streetprofile.visu;

import java.awt.Color;

import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.analysis.roof.RoofDetection;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.BuildingProfileTools;
import fr.ign.cogit.geoxygene.sig3d.analysis.streetprofile.Profile;
import fr.ign.cogit.geoxygene.sig3d.gui.MainWindow;
import fr.ign.cogit.geoxygene.sig3d.representation.basic.Object0d;
import fr.ign.cogit.geoxygene.sig3d.representation.sample.ObjectCartoon;
import fr.ign.cogit.geoxygene.sig3d.semantic.Map3D;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
import fr.ign.cogit.geoxygene.sig3d.util.ColorRandom;

public class StreetProfilRenderer {

	private static Logger logger = Logger.getLogger(StreetProfilRenderer.class);

	private boolean displayInit = false;
	Map3D map3d;

	public StreetProfilRenderer() {

	}

	/**
	 * Méthode affichant la scène. A appeler avant updateDisplay
	 */
	public void display(Profile profile) {

		IFeatureCollection<IFeature> parcelle = profile.getParcels();
		IFeatureCollection<IFeature> bati = profile.getBuildings();

		logger.info("-------------------------------------------");
		logger.info("Affichage graphique");
		logger.info("-------------------------------------------");

		if (bati == null) {
			logger.error("Erreur : les données bâtis doivent être chargées avant affichage");
			return;
		}

		if (parcelle == null) {
			logger.error("Erreur : les données parcellaires doivent être chargées avant affichage");
			return;
		}

		for (IFeature feat : bati) {

			feat.setRepresentation(new ObjectCartoon(feat, Color.lightGray));
		}

		for (IFeature p : parcelle) {
			p.setRepresentation(new ObjectCartoon(p, ColorRandom.getRandomColor()));
		}

		MainWindow fenetre = new MainWindow();
		fenetre.getInterfaceMap3D().removeLight(0);
		map3d = fenetre.getInterfaceMap3D().getCurrent3DMap();

		map3d.addLayer(new VectorLayer(bati, "Buildings"));
		map3d.addLayer(new VectorLayer(profile.getRoadsProfiled(), "Roads", Color.RED));
		map3d.addLayer(new VectorLayer(parcelle, "Parcels"));

		displayInit = true;

	}

	/**
	 * Méthode de rafraîchissement de l'affichage graphique. A appeler après
	 * display() et process()
	 */
	public void updateDisplay(Profile profile) {

		if (map3d == null) {

			logger.error("Erreur : la méthode display doit avoir été appelée pour pouvoir rafraîchir l'affichage");
			return;

		}

		double Max = 0;
		double Min = Double.POSITIVE_INFINITY;
		double opa = 1;

		for (IFeature ftpp2 : profile.getBuildingSide1()) {

			double A = (Double) ftpp2.getAttribute("Distance");

			if (A > Max) {
				Max = A;
			}
			if (A < Min) {
				Min = A;
			}

		}
		for (IFeature ftpp1 : profile.getBuildingSide2()) {

			double A = (Double) ftpp1.getAttribute("Distance");

			if (A > Max) {
				Max = A;
			}
			if (A < Min) {
				Min = A;
			}

			if (displayInit) {
				Color degr = BuildingProfileTools.degrade(Min, Max, A);
				ftpp1.setRepresentation(new Object0d(ftpp1, true, degr, opa, true));
			}

		}

		// Affectation des représentations
		for (IFeature ftpp2 : profile.getBuildingSide1()) {

			double A = (Double) ftpp2.getAttribute("Distance");

			Color degr = BuildingProfileTools.degrade(Min, Max, A);

			ftpp2.setRepresentation(new Object0d(ftpp2, true, degr, opa, true));

		}
		for (IFeature ftpp1 : profile.getBuildingSide2()) {

			double A = (Double) ftpp1.getAttribute("Distance");

			Color degr = BuildingProfileTools.degrade(Min, Max, A);

			ftpp1.setRepresentation(new Object0d(ftpp1, true, degr, opa, true));

		}

		map3d.addLayer(new VectorLayer(profile.getFeatOrthoColl(), "Vecteur Ortho", new Color(155, 2, 200)));

		map3d.addLayer(new VectorLayer(profile.getBuildingSide1(), "batiments proches"));
		map3d.addLayer(new VectorLayer(profile.getBuildingSide2(), "batiments proches1"));

		map3d.addLayer(
				new VectorLayer(RoofDetection.detectRoof(profile.getBuildings(), 0.2, true), "Toits", Color.yellow));

	}
}
