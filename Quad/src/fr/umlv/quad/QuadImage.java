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

	/**
	 * Création d'une image à partir d'un fichier sur le disque
	 * @param chemin : Chemin du fichier
	 */
	public QuadImage(String path) throws FileNotFoundException, IOException {
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
		Raster r= quadRoot.toRaster(height, width, maxValue);
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
		quadRoot= new QuadNode(raster, 0, 0, height, width, 0);
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

	/**
	 * Lecture des données d'une image QGM au format 
	 * @param inputStream
	 * @throws IOException
	 */
	private void loadQgmData(InputStream inputStream) throws IOException {
		QuadTokenizer tokenizer= new QuadTokenizerBin(inputStream, ucode);

		/* Utilisation d'une file pour le parcours en largeur */
		List fifou= new ArrayList();
		quadRoot= new QuadNode(0);
		fifou.add(quadRoot);

		while (!fifou.isEmpty()) {
			Object o= fifou.remove(0);
			QuadNode currentNode;
			int value;
			boolean plain;

			/* Si l'élément courant de la file est nul, alors le prochain 
			 * élément est un dernier fils (voir pourquoi plus loin)
			 */
			if (o == null) {
				currentNode= (QuadNode)fifou.remove(0);
				QuadValue qv;
				if (belongsToLastLevel(currentNode)) {
					qv= tokenizer.next(QuadTokenizer.PIXEL_NORMAL_CHILD_NODE);
				} else {
					qv= tokenizer.next(QuadTokenizer.INTERNAL_LAST_CHILD_NODE);
				}

				currentNode.setValue(qv.value);
				currentNode.setPlain(qv.plain);
			}

			/* Si l'élément courant est un Integer, alors l'élément suivant est
			 * un premier fils (voir pourquoi plus loin).  La valeur de ce fils
			 * est égale à celle de son ancêtre, qui est stockée dans l'Integer
			 */
			else if (o instanceof Integer) {
				Integer i= (Integer)o;

				currentNode= (QuadNode)fifou.remove(0);
				QuadValue qv;

				if (belongsToLastLevel(currentNode)) {
					qv= tokenizer.next(QuadTokenizer.PIXEL_FIRST_CHILD_NODE);
				} else {
					qv= tokenizer.next(QuadTokenizer.INTERNAL_FIRST_CHILD_NODE);
				}

				currentNode.setValue(i.intValue());
				currentNode.setPlain(qv.plain);
			} else {
				currentNode= (QuadNode)o;
				QuadValue qv;
				if (belongsToLastLevel(currentNode)) {
					qv= tokenizer.next(QuadTokenizer.PIXEL_NORMAL_CHILD_NODE);
				} else {
					qv=
						tokenizer.next(
							QuadTokenizer.INTERNAL_NORMAL_CHILD_NODE);
				}

				currentNode.setValue(qv.value);
				currentNode.setPlain(qv.plain);
			}

			//			System.out.println("value: " + value + " plain: " + plain);

			if (!currentNode.isPlain()) {
				currentNode.setTopLeftChild(
					new QuadNode(currentNode.getLevel() + 1));
				currentNode.setTopRightChild(
					new QuadNode(currentNode.getLevel() + 1));
				currentNode.setBottomLeftChild(
					new QuadNode(currentNode.getLevel() + 1));
				currentNode.setBottomRightChild(
					new QuadNode(currentNode.getLevel() + 1));

				fifou.add(new Integer(currentNode.getValue()));
				fifou.add(currentNode.getTopLeftChild());
				fifou.add(currentNode.getTopRightChild());
				fifou.add(currentNode.getBottomRightChild());
				fifou.add(null);
				fifou.add(currentNode.getBottomLeftChild());
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

		/* Utilisation d'une file pour le parcours en largeur */
		List fifou= new ArrayList();
		fifou.add(quadRoot);

		while (!fifou.isEmpty()) {
			boolean thisIsAFirstChild= false;
			QuadNode n= (QuadNode)fifou.remove(0);

			/* Le noeud est un premier fils si le prochain objet de la file
			 * est nul (voir plus loin pourquoi)
			 */
			if (n == null) {
				thisIsAFirstChild= true;
				n= (QuadNode) (fifou.remove(0));
			}

			/* Récupération de la valeur du noeud à traiter
			 */
			int value= n.getValue();
			if (value == ucode)
				value++;

			/* Si le noeud courant est uniforme, on écrit sa valeur suivie
			 * du marqueur ucode.
			 * La valeur est omise si le noeud est un premier fils.
			 * Le marqueur est omis si le noeud fait partie du dernier niveau.
			 */
			if (n.isPlain()) {
				if (thisIsAFirstChild) {
					if (n.getLevel() != numLevels - 1) {
//						out.print("" + ucode + " ");
//						out.print("" + ucode + " ");
						out.write(ucode);
						out.write(ucode);
					}
				} else {
//					out.print("" + value + " ");
					out.write(value);
					if (n.getLevel() != numLevels - 1) {
//						out.print("" + ucode + " ");
						out.write(ucode);
					}
				}
			}

			/* Si le noeud n'est pas uniforme, on écrit juste sa valeur.
			 * La valeur est omise si le noeud est un premier fils.
			 */
			else {
				if (!thisIsAFirstChild) {
//					out.print("" + value + " ");
					out.write(value);
				}

				/* On indique la position du premier fils en insérant un objet
				 * nul juste avant.
				 */
				fifou.add(null);
				fifou.add(n.getTopLeftChild());

				fifou.add(n.getTopRightChild());
				fifou.add(n.getBottomRightChild());
				fifou.add(n.getBottomLeftChild());
			}
		}
	}

}
