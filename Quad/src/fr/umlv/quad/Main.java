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

import sun.awt.image.ImageDecoder;

/**
 * @author cpele
 *
 * Classe principale pour les tests
 */
public class Main {
	public static void main(String[] args)
		throws FileNotFoundException, IOException {
		File imagesDir= new File("images");
		String[] imagePathTab= imagesDir.list();

		for (int i= 0; i < imagePathTab.length; i++) {
			String path= imagePathTab[i];
			if (!path.endsWith(".pgm") || !path.matches("^.*512.*$"))
				continue;

			System.out.println("Traitement de " + path);

			QuadImage image;

			System.out.println("\tCompression");

			System.out.print("\t\tChargement PGM : ");
			System.out.flush();
			image= new QuadImage("images/" + path);
			System.out.println("Ok");

			System.out.print("\t\tSauvegarde QGM : ");
			System.out.flush();
			image.saveCompressed("out/" + path + ".qgm", 10);
			System.out.println("Ok");

			System.out.println("\tDécompression");

			System.out.print("\t\tChargement QGM : ");
			System.out.flush();
			image= new QuadImage("out/" + path + ".qgm");
			System.out.println("Ok");
			
			System.out.print("\t\tSauvegarde PGM : ");
			System.out.flush();
			image.save("out2/" + path + ".qgm.pgm");
			System.out.println("Ok");
		}
	}
}
