/*
 * Créé le 5 févr. 2004
 */
package fr.umlv.quad;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Christophe Pelé
 *
 * Image en niveaux de gris représentée par un quadtree hiérarchique
 */
public class QuadImage {
	private int numLevels;
	private int values;
	private int ucode;
	private QuadNode quadRoot;
	private final String path;

	/**
	 * Création d'une image à partir d'un fichier sur le disque
	 * @param chemin : Chemin du fichier
	 */
	public QuadImage(String path) throws FileNotFoundException, IOException {
		this.path= path;
		if (path.endsWith(".pgm")) {
			loadPgm(path);
		} else if (path.endsWith(".qgm")) {
			loadQgmAscii(path);
		} else {
			throw new QuadError(path + ": Format de fichier inconnu");
		}
	}

	/**
	 * Sauvegarde d'une image dans un fichier
	 * @param path
	 * @throws IOException
	 */
	public void save(String path) throws IOException {
		/* Génération d'une image pgm */
		if (path.endsWith(".pgm")) {
			int width= (int)Math.sqrt((int)Math.pow(4, numLevels));
			int height= width;

			Raster r= quadRoot.toRaster(height, width, values);
			r.save(path);
		} else if (path.endsWith(".qgm")) {
			saveQgmAscii(path);
		} else {
			throw new QuadError(path + ": Type de fichier inconnu");
		}
	}

	/*-------------------------------------------------------------*/
	/*-- Gestion des fichiers PGM ---------------------------------*/
	/*-------------------------------------------------------------*/

	/**
	 * Chargement d'une image PGM
	 */
	private void loadPgm(String path)
		throws NumberFormatException, IOException {
		Raster raster= new Raster(path);
		ucode= raster.ucode();

		int width= raster.width();
		int height= raster.height();

		/* Calcul du nombre de niveaux dans le quadtree */
		double numLevels= Util.log4(width * height) + 1;
		if (width != height || numLevels != (int)numLevels)
			throw new QuadError(
				path
					+ ": L'image doit être carrée et sa taille de la forme 2^n * 2^n");
		this.numLevels= (int)numLevels;

		values= raster.values();
		quadRoot= new QuadNode(raster, 0, 0, height, width);
	}

	/*-------------------------------------------------------------*/
	/*-- Gestion des fichiers QGM ---------------------------------*/
	/*-------------------------------------------------------------*/

	/**
	 * Chargement d'une image QGM ASCII (Q2)
	 */
	private void loadQgmAscii(String path) throws IOException {
		FileInputStream inputStream= new FileInputStream(path);
		loadQgmHeaderAscii(inputStream);
		loadQgmDataAscii(inputStream);
	}

	/**
	 * Lecture de la prochaine valeur depuis un fichier
	 * @param tokenizer : Le tokenizer correspondant au fichier
	 * @return : La valeur lue ou bien -1 en cas de fin de fichier
	 * @throws IOException
	 */
	private int nextValue(StreamTokenizer tokenizer) throws IOException {
		tokenizer.nextToken();
		if (tokenizer.ttype == tokenizer.TT_NUMBER) {
			return (int)tokenizer.nval;
		} else if (tokenizer.ttype == tokenizer.TT_EOF) {
			return -1;
		} else
			throw new QuadError(path + ": Format du fichier incorrect");
	}

	/**
	 * Lecture des données d'une image QGM au format ASCII (Q2)
	 * @param inputStream
	 * @throws IOException
	 */
	private void loadQgmDataAscii(InputStream inputStream)
	throws IOException {
		StreamTokenizer tokenizer= new StreamTokenizer(inputStream);
		List fifou= new ArrayList();
		quadRoot=new QuadNode();
		fifou.add(quadRoot);

		while (!fifou.isEmpty()) {
			QuadNode n= (QuadNode)fifou.remove(0);

			int value= nextValue(tokenizer);
			if (value == ucode) {
				value=nextValue(tokenizer);
				n.setValue(value);
				n.setPlain(true);
			} else {
				n.setValue(value);
				n.setPlain(false);
				
				n.setTopLeftChild(new QuadNode());
				n.setTopRightChild(new QuadNode());
				n.setBottomRightChild(new QuadNode());
				n.setBottomLeftChild(new QuadNode());
				
				fifou.add(n.getTopLeftChild());
				fifou.add(n.getTopRightChild());
				fifou.add(n.getBottomRightChild());
				fifou.add(n.getBottomLeftChild());
			}
		}
	}

	private void loadQgmHeaderAscii(FileInputStream inputStream)
		throws IOException {
		StreamTokenizer tokenizer= new StreamTokenizer(inputStream);

		/* Lecture du type PGM */
		String typeStr;
		int ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_WORD)
			typeStr= tokenizer.sval;
		else
			throw new QuadError(
				path + ": Erreur lors de la lecture du type QGM");

		/* Lecture du nombre de niveaux */
		ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_NUMBER)
			numLevels= (int)tokenizer.nval;
		else
			throw new QuadError(path + ": Erreur lors du nombre de niveaux");

		/* Lecture du nombre de valeurs */
		ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_NUMBER)
			values= (int)tokenizer.nval;
		else
			throw new QuadError(
				path + ": Erreur lors de la lecture de la valeur maximale");

		/* Lecture de la valeur du marqueur ucode */
		ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_NUMBER)
			ucode= (int)tokenizer.nval;
		else
			throw new QuadError(
				path + ": Erreur lors de la lecture du marqueur ucode");
	}

	private void saveQgmAscii(String path) throws FileNotFoundException {
		PrintStream out= new PrintStream(new FileOutputStream(path));
		out.println("Q2");
		out.println(numLevels + " " + values + " " + ucode);

		List fifou= new ArrayList();
		fifou.add(quadRoot);

		while (!fifou.isEmpty()) {
			QuadNode n= (QuadNode)fifou.remove(0);

			int value= n.getValue();
			if (value == ucode)
				value++;

			if (n.isPlain()) {
				out.print("" + ucode + " " + value + " ");
			} else {
				out.print("" + value + " ");
				fifou.add(n.getTopLeftChild());
				fifou.add(n.getTopRightChild());
				fifou.add(n.getBottomRightChild());
				fifou.add(n.getBottomLeftChild());
			}
		}
	}
}
