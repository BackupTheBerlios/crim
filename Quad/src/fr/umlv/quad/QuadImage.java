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
			loadQgm(path);
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
			saveQgm(path);
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
	private void loadQgm(String path) throws IOException {
		FileInputStream inputStream= new FileInputStream(path);
		loadQgmHeader(inputStream);
		loadQgmData(inputStream);
	}

	/**
	 * Chargement de l'en-tête du fichier QGM
	 */
	private void loadQgmHeader(FileInputStream inputStream)
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
	 * Lecture des données d'une image QGM au format 
	 * @param inputStream
	 * @throws IOException
	 */
	private void loadQgmData(InputStream inputStream) throws IOException {
		QuadTokenizer tokenizer= new QuadTokenizerAscii(inputStream, ucode);
		List fifou= new ArrayList(); // la file
		quadRoot= new QuadNode();
		fifou.add(quadRoot);

		while (!fifou.isEmpty()) {
			Object o= fifou.remove(0);
			QuadNode n;
			int value;
			boolean plain;

			if (o == null) {
				n= (QuadNode)fifou.remove(0);
				tokenizer.next(QuadTokenizer.CT_LAST);

				value= tokenizer.value();
				plain= tokenizer.plain();
				n.setValue(value);
				n.setPlain(plain);
			} else if (o instanceof Integer) {
				Integer i= (Integer)o;

				n= (QuadNode)fifou.remove(0);
				tokenizer.next(QuadTokenizer.CT_FIRST);

				value= i.intValue();
				plain= tokenizer.plain();
				n.setValue(value);
				n.setPlain(plain);
			} else {
				n= (QuadNode)o;
				tokenizer.next(QuadTokenizer.CT_NORMAL);

				value= tokenizer.value();
				plain= tokenizer.plain();
				n.setValue(value);
				n.setPlain(plain);
			}

			System.out.println(""+value+": "+plain);

			if (!n.isPlain()) {
				n.setTopLeftChild(new QuadNode());
				n.setTopRightChild(new QuadNode());
				n.setBottomLeftChild(new QuadNode());
				n.setBottomRightChild(new QuadNode());

				fifou.add(new Integer(value));
				fifou.add(n.getTopLeftChild());
				fifou.add(n.getTopRightChild());
				fifou.add(n.getBottomRightChild());
				fifou.add(null);
				fifou.add(n.getBottomLeftChild());
			}
		}
	}

	private void saveQgm(String path) throws FileNotFoundException {
		saveQgm(new FileOutputStream(path));
	}

	/**
	 * Sauvegarde du quadtree au format QGM 
	 */
	public void saveQgm(OutputStream outOS) throws FileNotFoundException {
		PrintStream out= new PrintStream(outOS);
		out.println("Q1");
		out.println(numLevels + " " + maxValue + " " + ucode);

		List fifou= new ArrayList();
		fifou.add(quadRoot);

		while (!fifou.isEmpty()) {
			boolean thisIsAFirstChild= false;
			QuadNode n= (QuadNode)fifou.remove(0);

			if (n == null) {
				thisIsAFirstChild= true;
				n= (QuadNode) (fifou.remove(0));
			}

			/* Si le noeud courant est uniforme, on écrit sa valeur suivie
			 * du marqueur ucode.
			 */
			if (n.isPlain()) {
				if (thisIsAFirstChild) {
					out.print("" + ucode + " ");
					out.print("" + ucode + " ");
				} else {
					out.print("" + n.getValue() + " ");
					out.print("" + ucode + " ");
				}
			}

			/* Si le noeud n'est pas uniforme, on écrit juste sa valeur.
			 */
			else {
				if (!thisIsAFirstChild)
					out.print("" + n.getValue() + " ");

				fifou.add(null);
				fifou.add(n.getTopLeftChild());
				fifou.add(n.getTopRightChild());
				fifou.add(n.getBottomRightChild());
				fifou.add(n.getBottomLeftChild());
			}
		}
	}
}
