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
public class QuadImageChannel {
	public QuadImageChannel() {
		numLevels= maxValue= ucode= 0;
		quadRoot= null;
	}

	public QuadImageChannel(RasterChannel raster) throws IOException {
		ucode= raster.ucode();

		int width= raster.width();
		int height= raster.height();

		/* Calcul du nombre de niveaux dans le quadtree */
		double numLevels= Util.log4(width * height) + 1;

		/* Erreur si l'image n'est pas carrée ou si sa taille n'est pas de la 
		 * forme 2^n * 2^n
		 */
		if (width != height || numLevels != (int)numLevels)
			throw new QuadError(": L'image doit être carrée et sa taille de la forme 2^n * 2^n");

		this.numLevels= (int)numLevels;

		maxValue= raster.values();
		quadRoot= new QuadNode(raster, 0, 0, height, width, 0, QuadNode.ROOT);
	}

	private int numLevels;
	private int maxValue;
	private int ucode;
	private QuadNode quadRoot;

	public RasterChannel toRasterChannel() {
		int height= (int)Math.pow(2, numLevels-1);
		int width= height;
		int values= 255;
		return quadRoot.toRasterBand(height, width, values);
	}

	private boolean belongsToLastLevel(QuadNode n) {
		return (n.getLevel() == numLevels - 1);
	}

	QuadValueReader makeTokenizer(InputStream in) {
		return new QuadValueReaderBin(in, ucode);
	}

	/**
	 * Lecture des données d'une image QGM au format 
	 * @param inputStream
	 * @throws IOException
	 */
	void loadQgmData(InputStream inputStream) throws IOException {
		QuadValueReader tokenizer= makeTokenizer(inputStream);

		/* Utilisation d'une file pour le parcours en largeur */
		List fifou= new ArrayList();
		quadRoot= new QuadNode(0, QuadNode.ROOT);
		fifou.add(quadRoot);

		while (!fifou.isEmpty()) {
			QuadNode currentNode= (QuadNode)fifou.remove(0);
			int levelFlag;

			/* Utilisation d'un flag qui indique si le noeud est une feuille
			 * (correspond à un pixel) ou un noeud interne.
			 */
			if (belongsToLastLevel(currentNode))
				levelFlag= QuadValueReader.LEAF;
			else
				levelFlag= QuadValueReader.INTERNAL;

			/* Le noeud est il homogène et quelle est sa valeur ? */
			QuadValue qv;
			switch (currentNode.getLocation()) {
				case QuadNode.TOPLEFT :
					qv= tokenizer.next(levelFlag, QuadValueReader.FIRST);
					currentNode.setPlain(qv.plain);
					break;

				case QuadNode.BOTTOMLEFT :
					qv= tokenizer.next(levelFlag, QuadValueReader.LAST);
					currentNode.setValue(qv.value);
					currentNode.setPlain(qv.plain);
					break;

				default :
					qv= tokenizer.next(levelFlag, QuadValueReader.NORMAL);
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

	public static void write(PrintStream out, double value) {
		//		out.print("" + value + " ");
		out.write((int)value);
	}

	/**
	 * Sauvegarde du quadtree au format QGM 
	 */
	public void saveQgmData(OutputStream outOS) throws FileNotFoundException {
		PrintStream out= new PrintStream(outOS);
		int lastLevel= numLevels - 1;

		/* Utilisation d'une file pour le parcours en largeur */
		List fifou= new ArrayList();
		fifou.add(quadRoot);

		while (!fifou.isEmpty()) {
			QuadNode n= (QuadNode)fifou.remove(0);

			double value= n.getValue();
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
		quadRoot.compress(initialDev, factor);
	}

	public int getMaxValue() {
		return maxValue;
	}

	public int getNumLevels() {
		return numLevels;
	}

	public QuadNode getQuadRoot() {
		return quadRoot;
	}

	public int getUcode() {
		return ucode;
	}

	public void setMaxValue(int i) {
		maxValue= i;
	}

	public void setNumLevels(int i) {
		numLevels= i;
	}

	public void setQuadRoot(QuadNode node) {
		quadRoot= node;
	}

	public void setUcode(int i) {
		ucode= i;
	}

}
