/*
 * Created on 5 f?vr. 2004
 */
package fr.umlv.quad;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Timer;

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
//		QuadImage image=new QuadImage("images/galaxie.1024.pgm");
//		QuadImage image=new QuadImage("images/Boat.512.pgm");
		QuadImage image=new QuadImage("images/buzz.512.pgm");
//		QuadImage image=new QuadImage("images/Boat.64.pgm");
//		QuadImage image=new QuadImage("images/Boat.16.pgm");
//		QuadImage image=new QuadImage("images/Boat.4.pgm");
//		QuadImage image=new QuadImage("images/black.512.pgm");
//		QuadImage image=new QuadImage("images/black.512.raw");
//		QuadImage image=new QuadImage("images/chromosome.512.2.pgm");
		System.out.println("Ok");

		System.out.print("Sauvegarde de l'image: ");
		System.out.flush();
		image.save("out/image.pgm");
		image.save("out/image.qgm");
		System.out.println("Ok");
//		image.save("out/save.dot");
	}
}
