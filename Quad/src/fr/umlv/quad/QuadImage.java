/*
 * Créé le 5 févr. 2004
 */
package fr.umlv.quad;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christophe Pelé
 *
 * Image en niveaux de gris représentée par un quadtree hiérarchique
 */
public class QuadImage {
	private int numLevels;
	private int maxValue;
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
			savePgm(path);
		} else if (path.endsWith(".qgm")) {
			saveQgmAscii(path);
		} else {
			throw new QuadError(path + ": Type de fichier inconnu");
		}
	}

	/*-------------------------------------------------------------*/
	/*-- Gestion des fichiers PGM ---------------------------------*/
	/*-------------------------------------------------------------*/

	private void savePgm(String path) throws IOException {
		int width= (int)Math.sqrt(Math.pow(4, numLevels - 1));
		int height= width;
		Raster r= quadRoot.toRaster(height, width, maxValue);
		r.save(path);
	}

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

		/* Erreur si l'image n'est pas carrée ou si sa taille n'est pas de la 
		 * forme 2^n * 2^n
		 */
		if (width != height || numLevels != (int)numLevels)
			throw new QuadError(
				path
					+ ": L'image doit être carrée et sa taille de la forme 2^n * 2^n");

		this.numLevels= (int)numLevels;

		maxValue= raster.values();
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
	 * Chargement de l'en-tête du fichier QGM
	 */
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
			maxValue= (int)tokenizer.nval;
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

	/**
	 * Lecture des données d'une image QGM au format ASCII
	 * @param inputStream
	 * @throws IOException
	 */
	private void loadQgmDataAscii(InputStream inputStream) throws IOException {
		StreamTokenizer tokenizer= new StreamTokenizer(inputStream);
		List fifou= new ArrayList(); // la file
		quadRoot= new QuadNode();
		fifou.add(quadRoot);

		while (!fifou.isEmpty()) {
			Object o= fifou.remove(0);
			QuadNode n;
			int value;

			if (o instanceof Integer) {
				value=((Integer)o).intValue();
				n=(QuadNode)fifou.remove(0);
			} else {
				value= Util.nextValue(tokenizer);
				n= (QuadNode)o;
			}

			int possibleUcode= Util.nextValue(tokenizer);
			if (possibleUcode == ucode) {
				n.setPlain(true);
				n.setValue(value);
				System.out.print(""+value+"(u) ");
			} else {
				tokenizer.pushBack();

				n.setPlain(false);
				n.setValue(value);
				System.out.print(""+value+" ");

				n.setTopLeftChild(new QuadNode());
				n.setTopRightChild(new QuadNode());
				n.setBottomLeftChild(new QuadNode());
				n.setBottomRightChild(new QuadNode());

				fifou.add(new Integer(value));
				fifou.add(n.getTopLeftChild());
				fifou.add(n.getTopRightChild());
				fifou.add(n.getBottomRightChild());
				fifou.add(n.getBottomLeftChild());
			}
		}
	}

	private void saveQgmAscii(String path) throws FileNotFoundException {
		saveQgmAscii(new FileOutputStream(path));
	}

	/**
	 * Sauvegarde du quadtree au format QGM Ascii
	 * Attention : le marqueur ucode est écrit _avant_ l'écriture de la
	 * valeur d'un noeud uniforme
	 */
	public void saveQgmAscii(OutputStream outOS) throws FileNotFoundException {
		PrintStream out= new PrintStream(outOS);
		out.println("Q3");
		out.println(numLevels + " " + maxValue + " " + ucode);

		List fifou= new ArrayList();
		fifou.add(quadRoot);

		while (!fifou.isEmpty()) {
			QuadNode n= (QuadNode)fifou.remove(0);
			boolean thisIsAFirstChild= false;

			/* Si le noeud courant est null, c'est que l'élément suivant de la
			 * file est un premier fils (voir plus loin).  Sa valeur n'est 
			 * alors pas écrite dans le fichier puisque c'est la même que celle 
			 * de son ancêtre
			 */
			if (n == null) {
				n= (QuadNode)fifou.remove(0);
				thisIsAFirstChild= true;
			}

			int value= n.getValue();
			if (value == ucode)
				value++;

			if (n.isPlain()) {
				if (!thisIsAFirstChild)
					out.print("" + value + " " + ucode + " ");
				else
					out.print("" + ucode + " ");
			} else {
				QuadNode node= (QuadNode)n;
				if (!thisIsAFirstChild)
					out.print("" + value + " ");

				/* La présence d'un élément null dans la file indiquera que
				 * l'élément suivant est le premier fils de ce noeud
				 */
				fifou.add(null);
				fifou.add(node.getTopLeftChild());

				fifou.add(node.getTopRightChild());
				fifou.add(node.getBottomRightChild());
				fifou.add(node.getBottomLeftChild());
			}
		}
	}

}
