/*
 * Created on 5 févr. 2004
 */
package fr.umlv.quad;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author cpele
 *
 * Classe principale pour les tests
 */
public class Main {
	public static void main(String[] args)
	throws FileNotFoundException,IOException
	{
		System.out.print("Chargement de l'image: ");
		System.out.flush();
		QuadImage image;
//		image=new QuadImage("images/galaxie.1024.pgm");
//		image=new QuadImage("images/Boat.512.pgm");
//		image=new QuadImage("images/buzz.512.pgm");
//		image=new QuadImage("images/Boat.256.qgm");
//		image=new QuadImage("images/Boat.128.qgm");
//		image=new QuadImage("images/Boat.64.pgm");
//		image=new QuadImage("images/Boat.16.pgm");
		image=new QuadImage("images/Boat.4.qgm");
//		image=new QuadImage("images/Boat.2.qgm");
//		image=new QuadImage("images/black.512.pgm");
//		image=new QuadImage("images/lena.512.pgm");
//		image=new QuadImage("images/chromosome.512.2.pgm");
		System.out.println("Ok");

		System.out.print("Sauvegarde de l'image:");
		System.out.flush();
		image.save("out/image.pgm");
		System.out.print(" PGM");
//		image.saveQgmAscii(System.out);
		image.save("out/image.qgm");
//		image.save("images/Boat.4.qgm");
		System.out.print(" QGM");
		System.out.println(".");
//		image.save("out/save.dot");
	}
}
