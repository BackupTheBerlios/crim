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
	private int numLevels;
	private Raster raster;
	private int n= 0;

	/**
	 * Cr?ation d'une image ? partir d'un fichier sur le disque
	 * @param chemin : Chemin du fichier
	 */
	public QuadImage(String path) throws FileNotFoundException, IOException {
		raster= new Raster(path);
		int width= raster.width();
		int height= raster.height();
		numLevels= 1 + (int) (log2(width * height) / 2.);
		n= 0;
		quadRoot= new QuadImageElement();
		buildQuadTree(quadRoot, raster);
	}

	double log2(double d) {
		return Math.log(d) / Math.log(2);
	}

	private Raster buildSubRaster(
		Raster raster,
		int lineOffset,
		int columnOffset) {
		int width= raster.width();
		int height= raster.height();
		int values= raster.values();

		Raster subRaster= new Raster(width / 2, height / 2, values);
		for (int i= lineOffset; i < height / 2 + lineOffset; i++) {
			for (int j= columnOffset; j < width / 2 + columnOffset; j++) {
				byte value= raster.pixel(i, j);
				subRaster.pixel(i - lineOffset, j - columnOffset, value);
			}
		}
		return subRaster;
	}

	/** 
	 * Cr?ation du quadtree ? partir du raster qui a ?t? charg?
	 */
	private void buildQuadTree(QuadImageElement currentQIE, Raster raster)
		throws IOException {
		currentQIE.value= raster.defaultValue();
		currentQIE.uni= false;
		currentQIE.variance= 0;

		int width= raster.width();
		int height= raster.height();
		int values= raster.values();

		if (raster.numPixels() <= 1) {
			n++;
			if (n % 50000 == 0)
				System.out.println(n);
			return;
		}

		Raster topLeftRaster= buildSubRaster(raster, 0, 0);
		Raster topRightRaster= buildSubRaster(raster, 0, width / 2);
		Raster bottomRightRaster= buildSubRaster(raster, height / 2, width / 2);
		Raster bottomLeftRaster= buildSubRaster(raster, height / 2, 0);

		currentQIE.topLeft= new QuadImageElement();
		currentQIE.topRight= new QuadImageElement();
		currentQIE.bottomLeft= new QuadImageElement();
		currentQIE.bottomRight= new QuadImageElement();
		buildQuadTree(currentQIE.topLeft, topLeftRaster);
		buildQuadTree(currentQIE.bottomLeft, bottomLeftRaster);
		buildQuadTree(currentQIE.topRight, topRightRaster);
		buildQuadTree(currentQIE.bottomRight, bottomRightRaster);
	}

	public void save(String path) throws FileNotFoundException {
		if (path.endsWith(".dot")) {
			Util.makeDir(path);
			FileOutputStream outOS= new FileOutputStream(path);
			PrintStream out= new PrintStream(outOS);
			out.println("digraph shells {");
			out.println("node [fontsize=20, shape = box];");
			exportRec(quadRoot, out);
			out.println("}");
		} else {
			throw new QuadError(path + ": Type de fichier inconnu");
		}
	}

	private void exportRec(QuadImageElement quadElement, PrintStream out) {
		if (quadElement.bottomLeft != null) {
			out.println("" + quadElement.id + " -> " + quadElement.bottomLeft.id);
			exportRec(quadElement.bottomLeft, out);
		}
		if (quadElement.bottomRight != null) {
			out.println(
				"" + quadElement.id + " -> " + quadElement.bottomRight.id);
			exportRec(quadElement.bottomRight, out);
		}
		if (quadElement.topLeft != null) {
			out.println("" + quadElement.id + " -> " + quadElement.topLeft.id);
			exportRec(quadElement.topLeft, out);
		}
		if (quadElement.topRight != null) {
			out.println("" + quadElement.id + " -> " + quadElement.topRight.id);
			exportRec(quadElement.topRight, out);
		}
	}
}
