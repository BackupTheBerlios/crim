/*
 * Créé le 5 févr. 2004
 */
package fr.umlv.quad;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Christophe Pelé
 *
 * Image en niveaux de gris représentée par un quadtree hiérarchique
 */
public class QuadImageDescending {
	private QuadImageElementDescending quadRoot;
	private int n= 0;
	private int pixels= 0;

	/**
	 * Création d'une image à partir d'un fichier sur le disque
	 * @param chemin : Chemin du fichier
	 */
	public QuadImageDescending(String path) throws FileNotFoundException, IOException {
		Raster raster= new Raster(path);
		int width= raster.width();
		int height= raster.height();
		pixels= width * height;
		n= 0;
		quadRoot= new QuadImageElementDescending();
		System.out.print("Construction du quadtree: ");
		System.out.flush();
		buildQuadTreeWithOffsets(quadRoot, raster, 0, 0, height, width);
		System.out.println("Ok");

	}

	/** 
	 * Création du quadtree à partir du raster qui a été chargé
	 * 1ère méthode : le raster reste le même et on indique les coordonnées 
	 * du sous-raster pour l'appel récursif
	 */
	private void buildQuadTreeWithOffsets(
		QuadImageElementDescending currentQIE,
		Raster raster,
		int currentLineOffset,
		int currentColumnOffset,
		int currentHeight,
		int currentWidth)
		throws IOException {
		int values= raster.values();

		/* Condition d'arrêt de la récursion */
		if (raster
			.isUni(
				currentLineOffset,
				currentColumnOffset,
				currentHeight,
				currentWidth)) 
		{
//		if (currentWidth <= 2 || currentHeight <= 2) {
			/* Progression du traitement */
			//			n++;
			//			if (n % (pixels / 60) == 0) {
			//				System.out.print('#');
			//				System.out.flush();
			//			}

			currentQIE.setValue(
				(byte)raster.defaultValue(
					currentLineOffset,
					currentColumnOffset,
					currentHeight,
					currentWidth));
			currentQIE.setUni(true);
			currentQIE.setVariance(0);
			currentQIE.setHeight(currentHeight);
			currentQIE.setWidth(currentWidth);
			return;
		} else {
			currentQIE.setValue((byte)0);
			currentQIE.setUni(false);
			currentQIE.setVariance(0);
			currentQIE.setHeight(currentHeight);
			currentQIE.setWidth(currentWidth);
		}

		/*-- Appels récursifs ------------------------------------------*/

		currentQIE.topLeft= new QuadImageElementDescending();
		currentQIE.topRight= new QuadImageElementDescending();
		currentQIE.bottomLeft= new QuadImageElementDescending();
		currentQIE.bottomRight= new QuadImageElementDescending();

		/* Appel récursif (quart supérieur gauche) */
		int topLeftLineOffset= currentLineOffset;
		int topLeftColumnOffset= currentColumnOffset;
		int topLeftHeight= currentHeight / 2;
		int topLeftWidth= currentWidth / 2;
		buildQuadTreeWithOffsets(
			currentQIE.topLeft,
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
		buildQuadTreeWithOffsets(
			currentQIE.topRight,
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
		buildQuadTreeWithOffsets(
			currentQIE.bottomLeft,
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
		buildQuadTreeWithOffsets(
			currentQIE.bottomRight,
			raster,
			bottomRightLineOffset,
			bottomRightColumnOffset,
			bottomRightHeight,
			bottomRightWidth);
	}

	public void save(String path) throws IOException {
		/* Génération d'une image pgm */
		if (path.endsWith(".pgm")) {
			Raster r= quadRoot.toRaster();
			r.save(path);
		} else {
			throw new QuadError(path + ": Type de fichier inconnu");
		}
	}
}
