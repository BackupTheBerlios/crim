/*
 * Cr?? le 5 f?vr. 2004
 */
package fr.umlv.quad;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Christophe Pel?
 *
 * Image en niveaux de gris repr?sent?e par un quadtree hi?rarchique
 */
public class QuadImage {
	private QuadImageElement quadTab[];
	private int numLevels;
	private Raster raster;

	/**
	 * Cr?ation d'une image ? partir d'un fichier sur le disque
	 * @param chemin : Chemin du fichier
	 */
	public QuadImage(String path) throws FileNotFoundException, IOException {
		raster= new Raster(path);
		int width= raster.width();
		int height= raster.height();
		numLevels= 1 + (int) (log2(width * height) / 2.);
		quadTab= new QuadImageElement[width * height * numLevels];
		buildQuadTreeLevel(numLevels - 1, raster);
	}

	double log2(double d) {
		return Math.log(d) / Math.log(2);
	}

	private Raster buildSubRaster(
		Raster raster,
		int lineOffset,
		int columnOffset)
	{
		int width= raster.width();
		int height= raster.height();
		int values= raster.values();

		Raster subRaster= new Raster(width/2, height/2, values);
		for (int i= lineOffset; i < height / 2 + lineOffset; i++) {
			for (int j= columnOffset; j < width / 2 + columnOffset; j++) {
				byte value= raster.pixel(i, j);
				subRaster.pixel(i - lineOffset, j - columnOffset, value);
			}
		}
		return subRaster;
	}
	
	private int n=0;

	/** 
	 * Cr?ation du quadtree ? partir du raster qui a ?t? charg?
	 */
	private void buildQuadTreeLevel(int level, Raster raster)
	throws IOException
	{
		if (level==1) {
			if (n%1000==0) 
				System.out.println(n++);
			n++;
			return;
		}
		int width= raster.width();
		int height= raster.height();
		int values= raster.values();

		Raster topLeftRaster;
		Raster topRightRaster;
		Raster bottomLeftRaster;
		Raster bottomRightRaster;

		raster.save("/home/cpele/tmp/raster"+level+".pgm");

		topLeftRaster= buildSubRaster(raster, 0, 0);
		topRightRaster= buildSubRaster(raster, 0, width / 2);
		bottomRightRaster= buildSubRaster(raster, height / 2, width / 2);
		bottomLeftRaster= buildSubRaster(raster, height / 2, 0);

//		topLeftRaster.save("/home/cpele/tmp/topleft"+level+".pgm");
//		topRightRaster.save("/home/cpele/tmp/topright"+level+".pgm");
//		bottomLeftRaster.save("/home/cpele/tmp/bottomleft"+level+".pgm");
//		bottomRightRaster.save("/home/cpele/tmp/bottomright"+level+".pgm");

		buildQuadTreeLevel(level-1,topLeftRaster);
		buildQuadTreeLevel(level-1,bottomLeftRaster);
		buildQuadTreeLevel(level-1,topRightRaster);
		buildQuadTreeLevel(level-1,bottomRightRaster);
	}

}
