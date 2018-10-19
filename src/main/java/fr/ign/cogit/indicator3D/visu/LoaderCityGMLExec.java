package fr.ign.cogit.indicator3D.visu;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.citygml4j.xml.io.reader.CityGMLReadException;

import fr.ign.cogit.geoxygene.sig3d.gui.MainWindow;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.Context;
import fr.ign.cogit.geoxygene.sig3d.io.xml.citygmlv2.LoaderCityGML;
import fr.ign.cogit.geoxygene.sig3d.representation.citygml.representation.CG_StyleGenerator;
import fr.ign.cogit.geoxygene.sig3d.semantic.Map3D;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;

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
			
			
			//Remove geoxygene geometry to lighten memory
			LoaderCityGML.CLEAN_GEOX_GEOM = true;

			//CityGML file Name
			String fileName = "ZoneAExporter.gml";

			//FolderName
			//String folder = "/home/mbrasebin/Documents/Donnees/Paris/EXPORT_1296-13718/export-CityGML/";
			String folder = "/media/paulchapron/Data/DATA-Bati_3D/Paris/EXPORT_1296-13718/export-CityGML/";
            
			String path = folder + fileName;
			
				  
		  
			//Folder where image are included
			String folderImage = folder ;

		
			//Texture is loaded or not
			CG_StyleGenerator.LOAD_TEXTURE = false;

			//Level of Detail of the data
			Context.LOD_REP = 2;


        
			
			//Loading CityGLK
			VectorLayer vl = LoaderCityGML.read(new File(path), folderImage, "Layer", true);
			//Creating main window
			MainWindow win = new MainWindow();
			//Getting 3D map
			Map3D carte = win.getInterfaceMap3D().getCurrent3DMap();
			//Adding layer to map
			carte.addLayer(vl);

		} catch (CityGMLReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}