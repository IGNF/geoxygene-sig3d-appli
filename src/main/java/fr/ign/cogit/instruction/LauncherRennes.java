package fr.ign.cogit.instruction;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.gui.MainWindow;
import fr.ign.cogit.geoxygene.sig3d.representation.basic.Object1d;
import fr.ign.cogit.geoxygene.sig3d.representation.basic.Object2d;
import fr.ign.cogit.geoxygene.sig3d.semantic.Map3D;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
import fr.ign.cogit.geoxygene.sig3d.util.ColorLocalRandom;
import fr.ign.cogit.instruction.checker.Checker;
import fr.ign.cogit.instruction.checker.UnrespectedRule;
import fr.ign.cogit.simplu3d.demo.structDatabase.LoaderPostGISTest;
import fr.ign.cogit.simplu3d.io.structDatabase.postgis.loader.LoaderBPU;
import fr.ign.cogit.simplu3d.io.structDatabase.postgis.loader.LoaderVersion;
import fr.ign.cogit.simplu3d.io.structDatabase.postgis.storer.BasicStorer;
import fr.ign.cogit.simplu3d.model.AbstractBuilding;
import fr.ign.cogit.simplu3d.model.BasicPropertyUnit;
import fr.ign.cogit.simplu3d.model.CadastralParcel;
import fr.ign.cogit.simplu3d.model.Environnement;
import fr.ign.cogit.simplu3d.model.Road;
import fr.ign.cogit.simplu3d.model.RoofSurface;
import fr.ign.cogit.simplu3d.model.SpecificCadastralBoundary;
import fr.ign.cogit.simplu3d.model.SpecificWallSurface;
import fr.ign.cogit.simplu3d.model.SubParcel;
import fr.ign.cogit.simplu3d.model.SpecificCadastralBoundary.SpecificCadastralBoundaryType;

public class LauncherRennes {

  // Identifiants pour l'environnement par défaut
  public static int idUtilisateur = -1;
  public static int idBPUBase = -1;
  public static int idVersionBase = -1;

  public static Map3D carte;

  public static void main(String[] args) throws Exception {

    // On corrige le nom de la BDD et du MNT pour la zone de Rennes
    BasicStorer.database = "test_simplu3d";
    BasicStorer.host = "172.16.0.87";
    Checker.zMin = 40;

    // On construit la fenêtre principale
    MainWindow fenPrincipale = new MainWindow();
    carte = fenPrincipale.getInterfaceMap3D().getCurrent3DMap();

    // On affiche les données par défaut
    afficheMap(-1, -1);

    // Menu de sélection des versions
    fenPrincipale.getMainMenuBar().add(
        (new LauncherRennes()).generateCombobox());

    // Bouton de vérification des règles
    fenPrincipale.getMainMenuBar().add((new LauncherRennes()).generateButton());

    // Bouton pour charger l'environnement autour d'une BPU
    fenPrincipale.getMainMenuBar().add(
        (new LauncherRennes()).generateButtonBPU());

    // Bouton pour recharger l'environnement par défaut
    fenPrincipale.getMainMenuBar().add(
        (new LauncherRennes()).generateButtonDefaultView());

    // On actualise la fenêtre
    fenPrincipale.setVisible(true);

  }

  /**
   * Affiche au sein du viewer 3D les données en fonction d'un numéro de version
   * 
   * @param idVersion l'identifiant de la version à charger
   * @throws Exception
   */
  public static void afficheMap(int idVersion, int idBPU) throws Exception {

    // On supprime les couches
    carte.removeLayer("SCB type 0");
    carte.removeLayer("SCB type 1");
    carte.removeLayer("SCB type 2");
    carte.removeLayer("SCB type 3");
    carte.removeLayer("SCB type 4");
    carte.removeLayer("SCB type 5");
    carte.removeLayer("Toit");
    carte.removeLayer("Murs");
    carte.removeLayer("Pignon");
    carte.removeLayer("Faitage");
    carte.removeLayer("Gouttière");

    // On charge l'environnement depuis la base de données
    Environnement env;

    // Cas : environnement complet de base ou environnement complet avec version
    if (idBPU == -1) {
      carte.removeLayer("Route");
      carte.removeLayer("Parcelles");
      env = LoaderPostGISTest.load(null, idVersion);
    } else { // Cas : environnement centré sur BPU avec ou sans version
      carte.removeLayer("Route");
      carte.removeLayer("Parcelles");
      env = LoaderBPU.load(idVersion, idBPU);
    }

    // On récupère les données qui nous intéressent dans l'environnement
    IFeatureCollection<AbstractBuilding> buildingColl = env.getBuildings();
    IFeatureCollection<Road> roadColl = env.getRoads();
    IFeatureCollection<CadastralParcel> parcelColl = env.getCadastralParcels();
    IFeatureCollection<SubParcel> subParColl = env.getSubParcels();

    // On initialise l'IFC pour les SCB
    IFeatureCollection<SpecificCadastralBoundary> scbCollType0 = new FT_FeatureCollection<SpecificCadastralBoundary>();
    IFeatureCollection<SpecificCadastralBoundary> scbCollType1 = new FT_FeatureCollection<SpecificCadastralBoundary>();
    IFeatureCollection<SpecificCadastralBoundary> scbCollType2 = new FT_FeatureCollection<SpecificCadastralBoundary>();
    IFeatureCollection<SpecificCadastralBoundary> scbCollType3 = new FT_FeatureCollection<SpecificCadastralBoundary>();
    IFeatureCollection<SpecificCadastralBoundary> scbCollType4 = new FT_FeatureCollection<SpecificCadastralBoundary>();
    IFeatureCollection<SpecificCadastralBoundary> scbCollType5 = new FT_FeatureCollection<SpecificCadastralBoundary>();

    // On complète l'IFC des SCB en faisant une boucle sur les sous-parcelles
    for (SubParcel currentSp : subParColl) {

      IFeatureCollection<SpecificCadastralBoundary> scbCollTemp = currentSp
          .getSpecificCadastralBoundaryColl();

      for (SpecificCadastralBoundary currentSCB : scbCollTemp) {
        if (currentSCB.getType() == SpecificCadastralBoundaryType.ROAD) {
          scbCollType0.add(currentSCB);
        } else if (currentSCB.getType() == SpecificCadastralBoundaryType.LAT) {
          scbCollType1.add(currentSCB);
        } else if (currentSCB.getType() ==  SpecificCadastralBoundaryType.BOT) {
          scbCollType2.add(currentSCB);
        } else if (currentSCB.getType() ==  SpecificCadastralBoundaryType.INTRA) {
          scbCollType3.add(currentSCB);
        } else if (currentSCB.getType() ==  SpecificCadastralBoundaryType.UNKNOWN) {
          scbCollType4.add(currentSCB);
        } else if (currentSCB.getType() ==  SpecificCadastralBoundaryType.PUB) {
          scbCollType5.add(currentSCB);
        }
      }

    }

    // On initialise des IFC pour les murs et les toits
    IFeatureCollection<SpecificWallSurface> wallColl = new FT_FeatureCollection<SpecificWallSurface>();
    IFeatureCollection<RoofSurface> roofColl = new FT_FeatureCollection<RoofSurface>();

    // On complète les IFC murs et toits en faisant une boucle sur les BP
    for (AbstractBuilding currentBP : buildingColl) {

      wallColl.addAll(currentBP.getWallSurfaces());
      RoofSurface featRoof = currentBP.getRoof();

      if (currentBP.getIdVersion() == -1) {
        featRoof.setRepresentation(new Object2d(featRoof, Color.red));
      } else {
        featRoof.setRepresentation(new Object2d(featRoof, new Color(237, 145,
            33)));
      }

      roofColl.add(featRoof);

    }

    // On initialise les IFC destinées à contenir les Gutter, Gable et Roofing
    IFeatureCollection<IFeature> featGutter = new FT_FeatureCollection<IFeature>();
    IFeatureCollection<IFeature> featGable = new FT_FeatureCollection<IFeature>();
    IFeatureCollection<IFeature> featRoofing = new FT_FeatureCollection<IFeature>();

    // On défini les représentations et on récupère les données sur les
    // goutières, les faitages et les pignons
    for (RoofSurface featRoof : roofColl) {

      if (featRoof.getGutter() != null && !featRoof.getGutter().isEmpty()) {
        featGutter.add(new DefaultFeature(featRoof.getGutter()));
      }

      if (featRoof.getRoofing() != null && !featRoof.getRoofing().isEmpty()) {
        featRoofing.add(new DefaultFeature(featRoof.getRoofing()));
      }

      if (featRoof.getGable() != null && !featRoof.getGable().isEmpty()) {
        featGable.add(new DefaultFeature(featRoof.getGable()));
      }

    }

    for (IFeature featWall : wallColl) {
      featWall.setRepresentation(new Object2d(featWall, Color.lightGray));
    }

    for (IFeature featParcel : parcelColl) {
      Color c = ColorLocalRandom.getRandomColor(new Color(10, 150, 10), 0, 50,
          0);
      featParcel.setRepresentation(new Object2d(featParcel, c));
    }

    // On construit les couches
    VectorLayer roof = new VectorLayer(roofColl, "Toit");
    VectorLayer road = new VectorLayer(roadColl, "Route", true, Color.gray, 1,
        true);
    VectorLayer wall = new VectorLayer(wallColl, "Murs");
    VectorLayer parcel = new VectorLayer(parcelColl, "Parcelles");

    // On ajoute les couches à la carte (dans l'ordre que l'on souhaite)
    Object1d.width = 4;

    if (!featGable.isEmpty()) {
      VectorLayer vectGutter = new VectorLayer(featGable, "Pignon", Color.blue);
      carte.addLayer(vectGutter);
    }

    if (!featRoofing.isEmpty()) {
      VectorLayer vectGutter = new VectorLayer(featRoofing, "Faitage",
          Color.white);
      carte.addLayer(vectGutter);
    }

    if (!featGutter.isEmpty()) {
      VectorLayer vectGutter = new VectorLayer(featGutter, "Gouttière",
          Color.yellow);
      carte.addLayer(vectGutter);
    }

    carte.addLayer(roof);
    carte.addLayer(wall);

    if (!scbCollType0.isEmpty()) {
      VectorLayer vectSCBType0 = new VectorLayer(scbCollType0, "SCB type 0",
          Color.blue);
      carte.addLayer(vectSCBType0);
    }
    if (!scbCollType1.isEmpty()) {
      VectorLayer vectSCBType1 = new VectorLayer(scbCollType1, "SCB type 1",
          Color.orange);
      carte.addLayer(vectSCBType1);
    }
    if (!scbCollType2.isEmpty()) {
      VectorLayer vectSCBType2 = new VectorLayer(scbCollType2, "SCB type 2",
          Color.pink);
      carte.addLayer(vectSCBType2);
    }
    if (!scbCollType3.isEmpty()) {
      VectorLayer vectSCBType3 = new VectorLayer(scbCollType3, "SCB type 3",
          Color.black);
      carte.addLayer(vectSCBType3);
    }
    if (!scbCollType4.isEmpty()) {
      VectorLayer vectSCBType4 = new VectorLayer(scbCollType4, "SCB type 4",
          Color.cyan);
      carte.addLayer(vectSCBType4);
    }
    if (!scbCollType5.isEmpty()) {
      VectorLayer vectSCBType5 = new VectorLayer(scbCollType5, "SCB type 5",
          Color.magenta);
      carte.addLayer(vectSCBType5);
    }

    carte.addLayer(parcel);
    carte.addLayer(road);

  }

  /**
   * Génère une liste déroulante permettant de sélectionner la version à charger
   * dans l'interface
   * 
   * @return
   * @throws Exception
   */
  private JComboBox<Version> generateCombobox() throws Exception {

    // On génére les éléments à partir de la requête
    Vector<Version> lVersion = new Vector<>();
    lVersion.add(new Version(-1, "Données par défaut"));

    // On récupère les numéros de version pour l'utilisateur donné
    List<Integer> listIdVersion = LoaderVersion
        .retrieveListIdVersionWithTableVersion(BasicStorer.host, BasicStorer.port,
            BasicStorer.database, BasicStorer.user, BasicStorer.pw, idUtilisateur);

    // On ajoute les versions trouvées à la liste
    for (int j = 0; j < listIdVersion.size(); j++) {
      int id = listIdVersion.get(j);
      String nom = "Version " + Integer.toString(id);
      lVersion.add(new Version(id, nom));
    }

    // On construit le menu déroulant
    JComboBox<Version> comb = new JComboBox<>(lVersion);

    comb.setName("Menu_Version");
    comb.setSize(50, 20);
    comb.setSelectedIndex(0);
    comb.setVisible(true);
    comb.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        JComboBox<Version> cb = (JComboBox<Version>) e.getSource();
        Version selectedLine = (Version) cb.getSelectedItem();

        System.out.println("Version n° " + selectedLine.getID());
        System.out.println("Version nom " + selectedLine.getNom());

        int index = cb.getSelectedIndex();

        System.out.println("Selected index " + index);

        // On lance le chargement
        try {
          idVersionBase = selectedLine.getID();
          afficheMap(selectedLine.getID(), idBPUBase);
        } catch (Exception e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

      }
    });
    return comb;

  }

  /**
   * Génère un bouton pour la vérification des règles d'urbanisme sur la BPU
   * sélectionnée
   * 
   * @return
   */
  private JButton generateButton() {

    JButton jB = new JButton("Vérifier les règles sur la sélection");

    jB.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {

        IFeatureCollection<IFeature> featC = carte.getIMap3D().getSelection();
        IFeatureCollection<BasicPropertyUnit> bpuColl = new FT_FeatureCollection<BasicPropertyUnit>();

        for (IFeature feat : featC) {
          if (feat instanceof CadastralParcel) {
            CadastralParcel cad = (CadastralParcel) feat;
            BasicPropertyUnit bpu = cad.getbPU();
            bpuColl.add(bpu);
          }
        }

        try {
          List<UnrespectedRule> lUNR = Checker.checkSelection(bpuColl);

          List<UnrespectedRule> lUNRRep = new ArrayList<>();

          for (UnrespectedRule uR : lUNR) {

            if (uR == null)
              continue;

            uR.generateRepresentation();

            if (uR.getRepresentation() != null) {
              lUNRRep.add(uR);
            }
          }

          IFeatureCollection<IFeature> featCM = new FT_FeatureCollection<>();
          featCM.addAll(lUNRRep);

          carte.removeLayer("Non-resp");
          carte.addLayer(new VectorLayer(featCM, "Non-resp"));

        } catch (Exception e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

      }
    });

    return jB;

  }

  /**
   * Génère un bouton pour charger l'environnement autour d'une BPU
   * @return
   */
  private JButton generateButtonBPU() {

    JButton jButtonBPU = new JButton("Centrer sur la BPU");

    jButtonBPU.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {

        IFeatureCollection<IFeature> featC = carte.getIMap3D().getSelection();
        IFeatureCollection<BasicPropertyUnit> bpuColl = new FT_FeatureCollection<BasicPropertyUnit>();

        for (IFeature feat : featC) {
          if (feat instanceof CadastralParcel) {
            CadastralParcel cad = (CadastralParcel) feat;
            BasicPropertyUnit bpu = cad.getbPU();
            bpuColl.add(bpu);
          }
        }

        int idBPUextract = bpuColl.get(0).getId();
        idBPUBase = idBPUextract;

        try {
          afficheMap(idVersionBase, idBPUextract);
        } catch (Exception e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

      }
    });

    return jButtonBPU;
  }

  /**
   * Génère un bouton pour recharger l'environnement par défaut
   * @return
   */
  private JButton generateButtonDefaultView() {

    JButton jButtonDef = new JButton("Revenir à la vue de base");

    jButtonDef.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {

        try {
          idBPUBase = -1;
          idVersionBase = -1;
          afficheMap(idBPUBase, idVersionBase);
        } catch (Exception e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

      }

    });

    return jButtonDef;
  }

  public class Version {

    private int id;
    private String nom;

    public Version(int id, String nom) {
      this.id = id;
      this.nom = nom;
    }

    public int getID() {
      return id;
    }

    public String getNom() {
      return nom;
    }

    public String toString() {
      return this.getNom();
    }

  }

}
