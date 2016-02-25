package fr.ign.cogit.instruction.checker;

import java.awt.Color;
import java.net.URL;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IAggregate;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.sig3d.representation.sample.ObjectCartoon;
import fr.ign.cogit.geoxygene.sig3d.sample.Symbology;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_Aggregate;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiPoint;
import fr.ign.cogit.gru3d.regleUrba.representation.ContrainteDistance;
import fr.ign.cogit.gru3d.regleUrba.representation.ContrainteHauteurRepresentation;

public class UnrespectedRule extends DefaultFeature {
	String message; IFeature feat1; IFeature feat2; int type; IDirectPosition dp1; IDirectPosition dp2;

	public UnrespectedRule(String message, int type, IFeature feat1, IFeature feat2, IDirectPosition dp1, IDirectPosition dp2) {
		super();
		this.message = message;
		this.feat1 = feat1;
		this.feat2 = feat2;
		this.type = type;
		this.dp1 = dp1;
		this.dp2 = dp2;
	}

	public int getType() {
		return type;
	}

	public IDirectPosition getDp1() {
		return dp1;
	}

	public IDirectPosition getDp2() {
		return dp2;
	}

	public String getMessage() {
		return message;
	}

	public IFeature getFeat1() {
		return feat1;
	}

	public IFeature getFeat2() {
		return feat2;
	}

	public String toString(){
		String str = this.getMessage() + " ";
		str += feat1;
		if(this.getFeat2() != null){
			str += "  -  " + this.getFeat2();
		}
		return str;
		
	}
	
	//Pour la démonstration
	//type 0 = non-respect de la bande de fond de parcelle
	//type 1 = non-respect du CES
	//type 2 == non-respect du prospect par rapport à la route
	//type 3 = non respect du prospect par rapport aux limites sep
	// type 4 = non-respect de l'alignement
	public void generateRepresentation(){
		
		if(type == 0){
			URL url = Symbology.class.getClassLoader()
					.getResource("fr/ign/cogit/gtru/images/directionParcelle.png");

			IDirectPositionList dpl = new DirectPositionList();

			dpl.add(dp1);
			dpl.add(dp2);

	

			this.setGeom(new GM_MultiPoint(dpl));
			ContrainteDistance rep = new ContrainteDistance(this, 30, Color.black, 10, url.getPath(), 2.0,
					10);

			this.setRepresentation(rep);
			return;
		}
		
		
		if(type == 1){
			URL url = Symbology.class.getClassLoader().getResource("fr/ign/cogit/gtru/images/ces.png");
			
			
			ContrainteHauteurRepresentation cHRP = new ContrainteHauteurRepresentation(this, 30,
					this.getMessage(),
					Color.black, 10, url.getPath(), 2.0, 1);

			this.setRepresentation(cHRP);
			return ;
		}
		
		if(type == 2 || type == 3){
			this.setRepresentation(new ObjectCartoon(this, Color.red));
			return ;
		}
		
		if(type == 4){
			
			URL url = Symbology.class.getClassLoader().getResource("fr/ign/cogit/gtru/images/alignement.png");
			ContrainteDistance rep = new ContrainteDistance(this, 30, Color.black, 10, url.getPath(), 2.0, 10);

			this.setRepresentation(rep);
			return;
		}
		
		if(type == 5)
		{
			URL url = Symbology.class.getClassLoader()
					.getResource("fr/ign/cogit/gtru/images/directionParcelle.png");
			ContrainteDistance rep = new ContrainteDistance(this, 30, Color.black, 10, url.getPath(), 2.0, 10);

			this.setRepresentation(rep);
			return;
		}
		
		if(type == 6){
	
			
			this.setRepresentation(new ObjectCartoon(this, Color.red));
			return;
		}
		
		if(type ==7){
			URL url = Symbology.class.getClassLoader().getResource("fr/ign/cogit/gtru/images/hauteur.png");

			ContrainteHauteurRepresentation cHRP = new ContrainteHauteurRepresentation(this, 30,
					this.message,
					Color.black, 10, url.getPath(), 2.0, 1);

			this.setRepresentation(cHRP);
			return;
		}
	}

}
