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

	/**
	 * Cr?ation d'une image ? partir d'un fichier sur le disque
	 * @param chemin : Chemin du fichier
	 */
	public QuadImage(String path) throws FileNotFoundException, IOException {
		Raster raster= new Raster(path);
		int width= raster.width();
		int height= raster.height();
		n= 0;
		quadRoot= new QuadImageElement();
		buildQuadTreeWithOffsets(quadRoot, raster, 0, 0, height, width);
	}

	private double log2(double d) {
		return Math.log(d) / Math.log(2);
	}

	/** 
	 * Cr?ation du quadtree ? partir du raster qui a ?t? charg?
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

		if (currentWidth <= 1 || currentHeight <= 1) {
			n++;
			if (n % 50000 == 0)
				System.out.println(n);

			currentQIE.setValue(
				raster.defaultValue(
					currentLineOffset,
					currentColumnOffset,
					currentHeight,
					currentWidth));
			currentQIE.setUni(true);
			currentQIE.setVariance(0);

			return;
		} else {
			currentQIE.setValue((byte)0);
			currentQIE.setUni(false);
			currentQIE.setVariance(0);
		}

		currentQIE.topLeft= new QuadImageElement();
		currentQIE.topRight= new QuadImageElement();
		currentQIE.bottomLeft= new QuadImageElement();
		currentQIE.bottomRight= new QuadImageElement();

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

	/** 
	 * Cr?ation du quadtree ? partir du raster qui a ?t? charg?
	 */
	private void buildQuadTreeWithSubRasters(
		QuadImageElement currentQIE,
		Raster currentRaster)
		throws IOException {
		currentQIE.setValue(currentRaster.defaultValue());
		currentQIE.setUni(false);
		currentQIE.setVariance(0);

		int width= currentRaster.width();
		int height= currentRaster.height();
		int values= currentRaster.values();

		if (currentRaster.numPixels() <= 1) {
			n++;
			if (n % 50000 == 0)
				System.out.println(n);
			return;
		}

		Raster topLeftRaster= currentRaster.subRaster(0, 0);
		Raster topRightRaster= currentRaster.subRaster(0, width / 2);
		Raster bottomRightRaster=
			currentRaster.subRaster(height / 2, width / 2);
		Raster bottomLeftRaster= currentRaster.subRaster(height / 2, 0);

		currentQIE.topLeft= new QuadImageElement();
		currentQIE.topRight= new QuadImageElement();
		currentQIE.bottomLeft= new QuadImageElement();
		currentQIE.bottomRight= new QuadImageElement();
		buildQuadTreeWithSubRasters(currentQIE.topLeft, topLeftRaster);
		buildQuadTreeWithSubRasters(currentQIE.bottomLeft, bottomLeftRaster);
		buildQuadTreeWithSubRasters(currentQIE.topRight, topRightRaster);
		buildQuadTreeWithSubRasters(currentQIE.bottomRight, bottomRightRaster);
	}

	public void save(String path) throws IOException {
		if (path.endsWith(".dot")) {
			Util.makeDir(path);
			FileOutputStream outOS= new FileOutputStream(path);
			PrintStream out= new PrintStream(outOS);
			out.println("digraph shells {");
			out.println("node [fontsize=20, shape = box];");
			exportToDotRec(quadRoot, out);
			out.println("}");
		} else if (path.endsWith(".pgm")) {
			Raster r= quadRoot.toRaster();
			r.save(path);
		} else {
			throw new QuadError(path + ": Type de fichier inconnu");
		}
	}

	private void exportToDotRec(
		QuadImageElement quadElement,
		PrintStream out) {
		if (quadElement.bottomLeft != null) {
			out.println(
				"" + quadElement.getId() + " -> " + quadElement.getBottomLeft().getId());
			exportToDotRec(quadElement.bottomLeft, out);
		}
		if (quadElement.bottomRight != null) {
			out.println(
				"" + quadElement.getId() + " -> " + quadElement.getBottomRight().getId());
			exportToDotRec(quadElement.bottomRight, out);
		}
		if (quadElement.topLeft != null) {
			out.println("" + quadElement.getId() + " -> " + quadElement.getTopLeft().getId());
			exportToDotRec(quadElement.topLeft, out);
		}
		if (quadElement.topRight != null) {
			out.println("" + quadElement.getId() + " -> " + quadElement.getTopRight().getId());
			exportToDotRec(quadElement.topRight, out);
		}
	}
}
