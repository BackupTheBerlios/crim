/*
 * Créé le 5 févr. 2004
 */
package fr.umlv.quad;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Christophe Pelé
 *
 * Image en niveaux de gris représentée par un quadtree hiérarchique
 */
public class QuadImage {
	private int height;
	private int width;
	private int values;
	private int ucode;
	private QuadImageElement quadRoot;
	private ArrayList levels;
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

	private void loadPgm(String path)
		throws NumberFormatException, IOException {
		Raster raster= new Raster(path);
		ucode= raster.ucode();
		width= raster.width();
		height= raster.height();
		values= raster.values();
		quadRoot= new QuadImageElement();
		initLevels();
		buildQuadTree(quadRoot, 0, raster, 0, 0, height, width);
	}

	private void loadQgm(String path) throws IOException {
		FileInputStream inputStream= new FileInputStream(path);
		loadQgmHeader(inputStream);
		initLevels();
		loadQgmLevels(inputStream);
		buildQuadTreeFromLevels();
	}

	private void buildQuadTreeFromLevels() {
	}

	private void initLevels() {
		levels= new ArrayList();
		int numLevels= (int)Util.log2(width * height);
		for (int i= 0; i < numLevels; i++) {
			levels.add(new ArrayList());
		}
	}

	private void loadQgmLevels(FileInputStream inputStream) throws IOException {
		StreamTokenizer tokenizer= new StreamTokenizer(inputStream);
		int levelNum= 0;

		while (tokenizer.ttype != tokenizer.TT_EOF) {
			ArrayList level= (ArrayList)levels.get(levelNum);
			int ttype= tokenizer.nextToken();

			if (ttype == tokenizer.TT_NUMBER) {
				QuadImageElement qie= new QuadImageElement();
				int value= (int)tokenizer.nval;

				if (value == ucode) {
					qie.setValue(-1);
					qie.setPlain(false);
				} else {
					qie.setValue(value);
					qie.setPlain(true);
				}
				level.add(qie);
			} else
				throw new QuadError("Erreur lors de la lecture d'un fichier QGM");
				
			levelNum++;
		}
	}

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

		/* Lecture de la largeur */
		ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_NUMBER)
			width= (int)tokenizer.nval;
		else
			throw new QuadError(
				path + ": Erreur lors de la lecture de la largeur");

		/* Lecture de la hauteur */
		ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_NUMBER)
			height= (int)tokenizer.nval;
		else
			throw new QuadError(
				path + ": Erreur lors de la lecture de la hauteur");

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

	/** 
	 * Création du quadtree à partir du raster qui a été chargé
	 */
	private void buildQuadTree(
		QuadImageElement currentQIE,
		int levelNum,
		Raster raster,
		int currentLineOffset,
		int currentColumnOffset,
		int currentHeight,
		int currentWidth)
		throws IOException {
		ArrayList level= (ArrayList)levels.get(levelNum);
		level.add(currentQIE);

		/* Condition d'arrêt de la récursion */
		if (raster
			.isPlain(
				currentLineOffset,
				currentColumnOffset,
				currentHeight,
				currentWidth)) {
			currentQIE.setValue(
				(int)raster.defaultValue(
					currentLineOffset,
					currentColumnOffset,
					currentHeight,
					currentWidth));
			currentQIE.setPlain(true);
			return;
		} else {
			currentQIE.setValue(-1);
			currentQIE.setPlain(false);
		}

		/*-- Appels récursifs ------------------------------------------*/

		currentQIE.setTopLeft(new QuadImageElement());
		currentQIE.setTopRight(new QuadImageElement());
		currentQIE.setBottomLeft(new QuadImageElement());
		currentQIE.setBottomRight(new QuadImageElement());

		/* Appel récursif (quart supérieur gauche) */
		int topLeftLineOffset= currentLineOffset;
		int topLeftColumnOffset= currentColumnOffset;
		int topLeftHeight= currentHeight / 2;
		int topLeftWidth= currentWidth / 2;
		buildQuadTree(
			currentQIE.getTopLeft(),
			levelNum + 1,
			raster,
			topLeftLineOffset,
			topLeftColumnOffset,
			topLeftHeight,
			topLeftWidth);

		/* Appel récursif (quart supérieur droit) */
		int topRightLineOffset= currentLineOffset;
		int topRightColumnOffset= currentColumnOffset + currentWidth / 2;
		int topRightHeight= currentHeight / 2;
		int topRightWidth= currentWidth / 2;
		buildQuadTree(
			currentQIE.getTopRight(),
			levelNum + 1,
			raster,
			topRightLineOffset,
			topRightColumnOffset,
			topRightHeight,
			topRightWidth);

		/* Appel récursif (quart inférieur gauche) */
		int bottomLeftLineOffset= currentLineOffset + currentHeight / 2;
		int bottomLeftColumnOffset= currentColumnOffset;
		int bottomLeftHeight= currentHeight / 2;
		int bottomLeftWidth= currentWidth / 2;
		buildQuadTree(
			currentQIE.getBottomLeft(),
			levelNum + 1,
			raster,
			bottomLeftLineOffset,
			bottomLeftColumnOffset,
			bottomLeftHeight,
			bottomLeftWidth);

		/* Appel récursif (quart inférieur droit) */
		int bottomRightLineOffset= currentLineOffset + currentHeight / 2;
		int bottomRightColumnOffset= currentColumnOffset + currentWidth / 2;
		int bottomRightHeight= currentHeight / 2;
		int bottomRightWidth= currentWidth / 2;
		buildQuadTree(
			currentQIE.getBottomRight(),
			levelNum + 1,
			raster,
			bottomRightLineOffset,
			bottomRightColumnOffset,
			bottomRightHeight,
			bottomRightWidth);
	}

	public void save(String path) throws IOException {
		/* Génération d'une image pgm */
		if (path.endsWith(".pgm")) {
			Raster r= quadRoot.toRaster(height, width, values);
			r.save(path);
		} else if (path.endsWith(".qgm")) {
			saveCompressedAscii(path);
		} else {
			throw new QuadError(path + ": Type de fichier inconnu");
		}
	}

	private void saveCompressedAscii(String path)
		throws FileNotFoundException {
		PrintStream out= new PrintStream(new FileOutputStream(path));
		out.println("Q2");
		out.println(height + " " + width + " " + values + " " + ucode);

		Iterator i= levels.iterator();
		while (i.hasNext()) {
			ArrayList level= (ArrayList)i.next();
			Iterator j= level.iterator();
			while (j.hasNext()) {
				QuadImageElement current= (QuadImageElement)j.next();
				int value= current.getValue();

				if (current.isPlain()) {
					out.print(ucode);
					out.print(" ");
				}

				if (value == ucode)
					out.print(value + 1);
				else
					out.print(value);
				if (j.hasNext())
					out.print(" ");
			}
			out.println();
		}
	}
}
