/*
 * Created on 13 avr. 2004
 */
package fr.umlv.quad.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import fr.umlv.quad.QuadError;
import fr.umlv.quad.QuadImage;

/**
 * @author cpele
 */
public class Convert {

	public static void main(String[] args)
		throws IOException, ClassNotFoundException {
		if (args.length < 1)
			throw new QuadError("Pas assez d'arguments");

		String indent= "\t";

		BufferedReader in= new BufferedReader(new InputStreamReader(System.in));

		double sigma= 0;
		double c= 0;

		System.out.print("Voulez vous paramétrer les quadtrees ? (o/n) ");
		System.out.flush();
		String doCompress= in.readLine();
		if (doCompress.equals("o")) {
			System.out.print("Sasissez le seuil de variance : ");
			System.out.flush();
			sigma= Double.parseDouble(in.readLine());
			System.out.print("Saisissez le coefficient : ");
			System.out.flush();
			c= Double.parseDouble(in.readLine());
		}

		System.out.print(
			"Dans quel format voulez sauver les quadtrees ? (pgm/qgm/hgm) ");
		System.out.flush();
		String type= in.readLine();

		for (int i= 0; i < args.length; i++) {
			System.out.println("Traitement de " + args[i]);

			System.out.println(indent + "Construction du quadtree...");
			QuadImage image= new QuadImage(args[i]);
			System.out.println(indent + "Construction du quadtree Ok");

			if (doCompress.equals("o"))
				image.compress(sigma, c);

			String outPath= args[i] + "." + type;
			System.out.println(indent + "Sauvegarde dans " + outPath + "...");
			image.save(outPath);
			System.out.println(indent + "Sauvegarde Ok");

			File file= new File(args[i]);
			File outFile= new File(outPath);
			System.out.print(indent + file.length() / 1024 + "k");
			System.out.print(" => ");
			System.out.println("" + outFile.length() / 1024 + "k");
		}
	}
}
