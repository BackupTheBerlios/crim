/*
 * Created on 5 févr. 2004
 */
package fr.umlv.quad.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import fr.umlv.quad.QuadImage;

/**
 * @author cpele
 *
 * Classe principale pour les tests
 */
public class Main {
	public static void main(String[] args)
		throws FileNotFoundException, IOException {
		String project= "/home/cpele/eclipse/Quad";
		File imagesDir= new File(project + "/images");
		String[] imagePathTab= imagesDir.list();

		for (int i= 0; i < imagePathTab.length; i++) {
			String path= imagePathTab[i];
			String name= "(Lena|Mire_HSV|Clown)";
			String size= "";
			String ext= "(ppm)";
			if (!path.matches("^.*" + name + ".*" + size + ".*" + ext + "$"))
				continue;

			System.out.println("Traitement de " + path);

			try {
				QuadImage image;

				System.out.println("\tCompression");

				System.out.println("\t\tChargement PGM...");
				image= new QuadImage(project + "/images/" + path);
				System.out.println("\t\tChargement PGM Ok");

				System.out.println("\t\tCompression du quadtree...");
				if (image.getNumChannels() == 1) {
					image.compress(.0001, 8, 0);
				} else {
					image.compress(.0001, 2, 0);
					image.compress(.0001, 8, 1);
					image.compress(.0001, 8, 2);
				}
				System.out.println("\t\tCompression du quadtree Ok");

//				System.out.println("\t\tSauvegarde HGM...");
//				image.save(project + "/out/" + path + ".hgm");
//				System.out.println("\t\tSauvegarde HGM Ok");

				System.out.println("\t\tSauvegarde QGM...");
				image.save(project + "/out/" + path + ".qgm");
				System.out.println("\t\tSauvegarde QGM Ok");

				System.out.println("\tDécompression");

//				System.out.println("\t\tChargement HGM...");
//				image= new QuadImage(project + "/out/" + path + ".hgm");
//				System.out.println("\t\tChargement HGM Ok");

//				System.out.println("\t\tSauvegarde PGM...");
//				image.save(project + "/out2/" + path + ".hgm.pgm");
//				System.out.println("\t\tSauvegarde PGM Ok");

				System.out.println("\t\tChargement QGM...");
				image= new QuadImage(project + "/out/" + path + ".qgm");
				System.out.println("\t\tChargement QGM Ok");

				System.out.println("\t\tSauvegarde PGM...");
				image.save(project + "/out2/" + path + ".qgm.pgm");
				System.out.println("\t\tSauvegarde PGM Ok");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
