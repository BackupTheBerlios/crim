/*
 * Created on 5 févr. 2004
 */
package fr.umlv.quad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

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
			String name= "CatLogo";
			String size= "128";
			String ext= ".ppm";
			if (!path.endsWith(ext)
				|| !path.matches("^(.*)" + name + "(.*)\\." + size + "\\.(.*)$"))
				continue;

			System.out.println("Traitement de " + path);

			try {
				QuadImage image;

				System.out.println("\tCompression");

				System.out.print("\t\tChargement PGM : ");
				System.out.flush();
				image= new QuadImage(project + "/images/" + path);
				System.out.println("Ok");

				System.out.print("\t\tCompression du quadtree : ");
				System.out.flush();
				image.compress(.0001, 8, 1);
				image.compress(.0001, 8, 2);
				System.out.println("Ok");

				System.out.print("\t\tSauvegarde QGM : ");
				System.out.flush();
				image.save(project + "/out/" + path + ".qgm");
				System.out.println("Ok");

				System.out.println("\tDécompression");

				System.out.print("\t\tChargement QGM : ");
				System.out.flush();
				image= new QuadImage(project + "/out/" + path + ".qgm");
				System.out.println("Ok");

				System.out.print("\t\tSauvegarde PGM : ");
				System.out.flush();
				image.save(project + "/out2/" + path + ".qgm.pgm");
				System.out.println("Ok");
			} catch (Exception e) {
				System.out.println();
				e.printStackTrace();
			}
		}
	}
}
