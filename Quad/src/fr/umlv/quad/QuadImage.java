/*
 * Cr?? le 5 f?vr. 2004
 */
package fr.umlv.quad;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Christophe Pel?
 *
 * Image en niveaux de gris repr?sent?e par un quadtree hi?rarchique
 */
public class QuadImage {
	private QuadImageElement quadRoot;
	private int n= 0;
	private int pixels= 0;

	/**
	 * Cr?ation d'une image ? partir d'un fichier sur le disque
	 * @param chemin : Chemin du fichier
	 */
	public QuadImage(String path) throws FileNotFoundException, IOException {
		Raster raster= new Raster(path);
		int width= raster.width();
		int height= raster.height();
		pixels= width * height;
		n= 0;
		quadRoot= new QuadImageElement();
		System.out.print("Construction du quadtree: ");
		System.out.flush();
		buildQuadTreeWithOffsets(quadRoot, raster, 0, 0, height, width);
		System.out.println("Ok");

	}

	/** 
	 * Cr?ation du quadtree ? partir du raster qui a ?t? charg?
	 * 1�re m�thode : le raster reste le m�me et on indique les coordonn�es 
	 * du sous-raster pour l'appel r�cursif
	 */
	private void buildQuadTreeWithOffsets(
		QuadImageElement currentQIE,
		Raster raster,
		int currentLineOffset,
		int currentColumnOffset,
		int currentHeight,
		int currentWidth)
		throws IOException {
		int values= raster.values();

		/* Condition d'arr�t de la r�cursion */
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

		/*-- Appels r�cursifs ------------------------------------------*/

		currentQIE.topLeft= new QuadImageElement();
		currentQIE.topRight= new QuadImageElement();
		currentQIE.bottomLeft= new QuadImageElement();
		currentQIE.bottomRight= new QuadImageElement();

		/* Appel r�cursif (quart sup�rieur gauche) */
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

		/* Appel r�cursif (quart sup�rieur droit) */
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

		/* Appel r�cursif (quart inf�rieur gauche) */
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

		/* Appel r�cursif (quart inf�rieur droit) */
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
		/* G�n�ration d'une image pgm */
		if (path.endsWith(".pgm")) {
			Raster r= quadRoot.toRaster();
			r.save(path);
		} else {
			throw new QuadError(path + ": Type de fichier inconnu");
		}
	}
}
