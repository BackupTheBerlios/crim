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
	
	int numBands;
	private QuadNode quadRoot[];

	/**
	 * Création d'une image à partir d'un fichier sur le disque
	 * @param chemin : Chemin du fichier
	 */
	public QuadImage(String path) throws FileNotFoundException, IOException {
		numBands=1;
		quadRoot=new QuadNode[numBands];

		if (path.endsWith(".pgm")) {
			importPgm(path);
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
			exportPgm(path);
		} else if (path.endsWith(".qgm")) {
			saveQgm(path);
		} else {
			throw new QuadError(path + ": Type de fichier inconnu");
		}
	}

	/*-------------------------------------------------------------*/
	/*-- Gestion des fichiers PGM ---------------------------------*/
	/*-------------------------------------------------------------*/

	private void exportPgm(String path) throws IOException {
		int width= (int)Math.sqrt(Math.pow(4, numLevels - 1));
		int height= width;
		Raster r= quadRoot[0].toRaster(height, width, maxValue);
		r.save(path);
	}

	/**
	 * Chargement d'une image PGM
	 */
	private void importPgm(String path)
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
		quadRoot[0]=
			new QuadNode(raster, 0, 0, 0, height, width, 0, QuadNode.ROOT);
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
			throw new QuadError("Erreur lors de la lecture du type QGM");

		/* Lecture du nombre de niveaux */
		ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_NUMBER)
			numLevels= (int)tokenizer.nval;
		else
			throw new QuadError("Erreur lors de la lecture du nombre de niveaux");

		/* Lecture du nombre de valeurs */
		ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_NUMBER)
			maxValue= (int)tokenizer.nval;
		else
			throw new QuadError("Erreur lors de la lecture de la valeur maximale");

		/* Lecture de la valeur du marqueur ucode */
		ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_NUMBER)
			ucode= (int)tokenizer.nval;
		else
			throw new QuadError("Erreur lors de la lecture du marqueur ucode");
	}

	private boolean belongsToLastLevel(QuadNode n) {
		return (n.getLevel() == numLevels - 1);
	}

	QuadTokenizer makeTokenizer(InputStream in) {
		return new QuadTokenizerBinGray(in,ucode);
	}

	/**
	 * Lecture des données d'une image QGM au format 
	 * @param inputStream
	 * @throws IOException
	 */
	private void loadQgmData(InputStream inputStream) throws IOException {
		QuadTokenizer tokenizer= makeTokenizer(inputStream);

		/* Utilisation d'une file pour le parcours en largeur */
		List fifou= new ArrayList();
		quadRoot[0]= new QuadNode(0, QuadNode.ROOT);
		fifou.add(quadRoot[0]);

		while (!fifou.isEmpty()) {
			QuadNode currentNode= (QuadNode)fifou.remove(0);
			int value;
			boolean plain;
			int levelFlag;

			/* Utilisation d'un flag qui indique si le noeud est une feuille
			 * (correspond à un pixel) ou un noeud interne.
			 */
			if (belongsToLastLevel(currentNode))
				levelFlag= QuadTokenizer.LEAF;
			else
				levelFlag= QuadTokenizer.INTERNAL;

			/* Le noeud est il homogène et quelle est sa valeur ? */
			QuadValue qv;
			switch (currentNode.getLocation()) {
				case QuadNode.TOPLEFT :
					qv= tokenizer.next(levelFlag, QuadTokenizer.FIRST);
					currentNode.setPlain(qv.plain);
					break;

				case QuadNode.BOTTOMLEFT :
					qv= tokenizer.next(levelFlag, QuadTokenizer.LAST);
					currentNode.setValue(qv.value);
					currentNode.setPlain(qv.plain);
					break;

				default :
					qv= tokenizer.next(levelFlag, QuadTokenizer.NORMAL);
					currentNode.setValue(qv.value);
					currentNode.setPlain(qv.plain);
					break;
			}

			/* Si le noeud est homogène, on ne traite pas ses fils
			 * Sinon, on les ajoute à la file pour les traiter plus tard */
			if (currentNode.isNotPlain()) {
				int nextLevel= currentNode.getLevel() + 1;

				QuadNode topLeftChild=
					new QuadNode(nextLevel, QuadNode.TOPLEFT);
				QuadNode topRightChild=
					new QuadNode(nextLevel, QuadNode.TOPRIGHT);
				QuadNode bottomLeftChild=
					new QuadNode(nextLevel, QuadNode.BOTTOMLEFT);
				QuadNode bottomRightChild=
					new QuadNode(nextLevel, QuadNode.BOTTOMRIGHT);

				topLeftChild.setValue(currentNode.getValue());

				currentNode.setTopLeftChild(topLeftChild);
				currentNode.setTopRightChild(topRightChild);
				currentNode.setBottomLeftChild(bottomLeftChild);
				currentNode.setBottomRightChild(bottomRightChild);

				fifou.add(topLeftChild);
				fifou.add(topRightChild);
				fifou.add(bottomRightChild);
				fifou.add(bottomLeftChild);
			}
		}
	}

	private void saveQgm(String path)
		throws FileNotFoundException {
		saveQgm(new FileOutputStream(path));
	}

	void write(PrintStream out, int value) {
//		out.print("" + value + " ");
		out.write(value);
	}
	
	/**
	 * Sauvegarde du quadtree au format QGM 
	 */
	public void saveQgm(OutputStream outOS)
		throws FileNotFoundException {
		PrintStream out= new PrintStream(outOS);
		out.println("Q1");
		out.println(numLevels + " " + maxValue + " " + ucode);
		int lastLevel= numLevels - 1;

		/* Utilisation d'une file pour le parcours en largeur */
		List fifou= new ArrayList();
		fifou.add(quadRoot[0]);

		while (!fifou.isEmpty()) {
			QuadNode n= (QuadNode)fifou.remove(0);

			int value= n.getValue();
			int level= n.getLevel();
			boolean plain= n.isPlain();
			short location= n.getLocation();

			/* Récupération de la valeur du noeud à traiter
			 */
			if (value == ucode)
				value++;

			/* Si le noeud courant est uniforme, on écrit sa valeur suivie
			 * du marqueur ucode.
			 * La valeur est omise si le noeud est un premier fils.
			 * Le marqueur est omis si le noeud fait partie du dernier niveau.
			 */
			if (plain) {
				if (location == QuadNode.TOPLEFT) {
					if (level != lastLevel) {
						write(out, ucode);
						write(out, ucode);
					}
				} else {
					write(out, value);
					if (level != lastLevel) {
						write(out, ucode);
					}
				}
			}

			/* Si le noeud n'est pas uniforme, on écrit juste sa valeur.
			 * La valeur est omise si le noeud est un premier fils.
			 */
			else {
				if (location != QuadNode.TOPLEFT) {
					write(out, value);
				}

				fifou.add(n.getTopLeftChild());
				fifou.add(n.getTopRightChild());
				fifou.add(n.getBottomRightChild());
				fifou.add(n.getBottomLeftChild());
			}
		}
	}
	
	public void compress(double initialDev, double factor) {
		quadRoot[0].compress(initialDev,factor);
	}
	
}
