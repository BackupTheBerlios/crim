/*
 * Créé le 5 févr. 2004
 */
package fr.umlv.quad;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

/**
 * @author Christophe Pelé
 *
 * Image en niveaux de gris représentée par un quadtree hiérarchique
 */
public class QuadImage {
	private QuadImageElement level[][];
	private int width,height,values;
	private byte[] raster;
	
	/**
	 * Création d'une image à partir d'un fichier sur le disque
	 * @param chemin : Chemin du fichier
	 */
	public QuadImage(String path) throws FileNotFoundException,IOException {
		if (path.endsWith(".raw")) {
			BufferedReader inReader=
				new BufferedReader(new InputStreamReader(System.in));

			System.out.println("Paramètres de l'image : ");
			System.out.print("	Hauteur : ");
			int height=Integer.parseInt(inReader.readLine());
			System.out.print("	Largeur : ");
			int width=Integer.parseInt(inReader.readLine());
			System.out.print("	Nombre de valeurs : ");
			int values=Integer.parseInt(inReader.readLine());

			loadRasterFromRaw(path);
		} else if (path.endsWith(".pgm")) {
			loadRasterFromPgm(path);
		} else {
			throw new QuadImageError(
				path+": Extension de fichier incorrecte");
		}
		buildQuadTreeFromRaster();
	}

	/** 
	 * Création du quadtree à partir du raster qui a été chargé
	 */
	private void buildQuadTreeFromRaster() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Charge une image Pgm (n&b)
	 * @param path : Chemin du fichier image
	 */
	private void loadRasterFromPgm(String path)
	throws FileNotFoundException,IOException
	{
		FileInputStream inputStream=new FileInputStream(path);

		loadHeaderFromPgm(inputStream);
		loadRaster(inputStream);
	}

	/**
 	 * Charge une image brute
 	 * 
	 * @param path : Nom du fichier image
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadRasterFromRaw(String path)
	throws FileNotFoundException,IOException
	{
		loadRaster(new FileInputStream(path));
	}

	/**
	 * Charge l'en-tête d'une image pgm
	 * 
	 * @param inputStream : Le flux correspondant à l'image
	 * @throws IOException
	 */
	private void loadHeaderFromPgm(InputStream inputStream)
	throws IOException
	{
		StreamTokenizer tokenizer=new StreamTokenizer(inputStream);
					
		/* Lecture du type PGM */
		String typeStr;
		int ttype=tokenizer.nextToken();
		if (ttype==tokenizer.TT_WORD)
			typeStr=tokenizer.sval;

		/* Lecture de la largeur */
		ttype=tokenizer.nextToken();
		if (ttype==tokenizer.TT_NUMBER)
			width=(int)tokenizer.nval;			
		
		/* Lecture de la hauteur */
		ttype=tokenizer.nextToken();
		if (ttype==tokenizer.TT_NUMBER)
			height=(int)tokenizer.nval;			
		
		/* Lecture du nombre de valeurs */
		ttype=tokenizer.nextToken();
		if (ttype==tokenizer.TT_NUMBER)
			values=(int)tokenizer.nval;	
	}

	/**
 	 * Charge le contenu brut d'une image (raster)
 	 * 
	 * @param inputStream : Flux correspondant à l'image
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadRaster(InputStream inputStream)
	throws FileNotFoundException,IOException
	{
		raster=new byte[height*width];
		int n=inputStream.read(raster);
		if (n<height*width)
			throw new QuadImageError(
				"Fin de fichier inattendue à l'octet n°"+n+" du raster");
	}
}
