/*
 * Created on 15 mars 2004
 */
package fr.umlv.quad;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.Arrays;

/**
 * @author cpele
 */
public class RasterBand {
	private int width;
	private int height;
	private int values;
	private int[] array;

	/**
	 * Création d'un raster vide
	 * @param width
	 * @param height
	 * @param values
	 */
	public RasterBand(int height, int width, int values) {
		this.width= width;
		this.height= height;
		this.values= values;
		array= new int[width * height];
	}

	/**
	 * Création d'un raster à partir d'un tableau de valeurs
	 * @param width
	 * @param height
	 * @param values
	 * @param intArray
	 */
	public RasterBand(int height, int width, int values, int[] intArray) {
		this(height, width, values);
		load(intArray);
	}

	/**
	 * Création d'un raster à partir d'une valeur par défaut
	 */
	public RasterBand(int height, int width, int values, int defaultValue) {
		this(height, width, values);
		Arrays.fill(array, defaultValue);
	}

	/**
	 * Chargement de valeurs dans le raster
	 * @param intArray
	 */
	public void load(int[] intArray) {
		System.arraycopy(intArray, 0, this.array, 0, this.array.length);
	}

	public void loadPgmData(InputStream in) throws IOException {
		for (int i= 0; i < height; i++) {
			for (int j= 0; j < width; j++) {
				int value= in.read();
				if (value == -1)
					throw new QuadError("Fin de fichier inattendue");
				pixel(i, j, value);
			}
		}
	}

	/**
	 * Création d'un raster à partir de quatre sous-rasters
	 * @param topLeft
	 * @param topRight
	 * @param bottomLeft
	 * @param bottomRight
	 */
	public RasterBand(
		RasterBand topLeft,
		RasterBand topRight,
		RasterBand bottomLeft,
		RasterBand bottomRight) {
		this(topLeft.height * 2, topLeft.width * 2, 255);
		checkDimensions(topLeft, topRight, bottomLeft, bottomRight);
		for (int i= 0; i < topLeft.height; i++) {
			for (int j= 0; j < topLeft.width; j++) {
				int value= topLeft.pixel(i, j);
				pixel(i, j, value);

				value= topRight.pixel(i, j);
				pixel(i, j + width / 2, value);

				value= bottomLeft.pixel(i, j);
				pixel(i + height / 2, j, value);

				value= bottomRight.pixel(i, j);
				pixel(i + height / 2, j + width / 2, value);
			}
		}
	}

	/**
	 * Test d'égalité des dimensions de deux rasters
	 * @param other
	 * @return
	 */
	public boolean hasSameDimensions(RasterBand other) {
		return (
			height == other.height
				&& width == other.width
				&& values == other.values);
	}

	/**
	 * Test de la validité des dimensions des sous-rasters par rapport à celles
	 * du raster
	 */
	private void checkDimensions(
		RasterBand topLeft,
		RasterBand topRight,
		RasterBand bottomLeft,
		RasterBand bottomRight) {
		boolean ok=
			(values == topLeft.values
				&& height == topLeft.height * 2
				&& width == topLeft.width * 2
				&& topLeft.hasSameDimensions(topRight)
				&& topLeft.hasSameDimensions(bottomLeft)
				&& topLeft.hasSameDimensions(bottomRight));
		if (!ok)
			throw new QuadError("Les quatre sous-rasters n'ont pas la bonne taille");
	}

	/*-- Chargement d'images ---------------------------------*/

	/*-- Getters et Setters ---------------------------------------*/

	public int height() {
		return height;
	}
	public int values() {
		return values;
	}
	public int width() {
		return width;
	}
	public void height(int i) {
		height= i;
	}
	public void values(int i) {
		values= i;
	}
	public void width(int i) {
		width= i;
	}
	public int[] getArray() {
		return array;
	}

	/*-- Gestion des pixels ---------------------------------------*/

	/**
	 * Récupération de la valeur d'un pixel
	 */
	public int pixel(int line, int column) {
		checkPixelCoords(line, column);
		return array[line * width + column];
	}

	/**
	 * Mise à jour de la valeur d'un pixel
	 * @param line
	 * @param column
	 * @param value
	 */
	public void pixel(int line, int column, int value) {
		checkPixelCoords(line, column);
		array[line * width + column]= value;
	}

	/**
	 * Vérifie la validité des coordonnées d'un pixel
	 * @param line
	 * @param column
	 */
	private void checkPixelCoords(int line, int column) {
		boolean ok= true;
		StringBuffer msgBuf= new StringBuffer();

		if (line >= height || line < 0) {
			msgBuf.append(
				"Impossible d'aller à la ligne demandée (" + line + ")");
			ok= false;
		}
		if (column >= width || column < 0) {
			msgBuf.append(
				"\nImpossible d'aller à la colonne demandée (" + column + ")");
			ok= false;
		}
		if (!ok)
			throw new QuadError(msgBuf.toString());
	}

	/*-- Traitements -----------------------------------------------*/

	/**
	 * Calcul de la moyenne d'une partie de l'image
	 */
	public double mean(
		int lineOffset,
		int columnOffset,
		int height,
		int width) {
		double mean= 0;
		for (int i= 0; i < height; i++) {
			for (int j= 0; j < width; j++) {
				mean += pixel(i + lineOffset, j + columnOffset);
			}
		}
		mean /= height * width;
		return mean;
	}

	/**
	 * Calcul de la variance dans une partie de l'image
	 */
	private double stddev(
		int lineOffset,
		int columnOffset,
		int height,
		int width) {
		//	   sd is sqrt of sum of (values-mean) squared divided by n - 1

		//	   Calculate the mean
		double mean= 0;
		final int n= height * width;
		if (n <= 1)
			throw new QuadError("Calcul de la variance impossible pour moins de deux valeurs");
		for (int i= 0; i < height; i++) {
			for (int j= 0; j < width; j++) {
				mean += pixel(i + lineOffset, j + columnOffset);
			}
		}
		mean /= n;

		//	   calculate the sum of squares
		double sum= 0;
		for (int i= 0; i < height; i++) {
			for (int j= 0; j < width; j++) {
				final double v= pixel(i + lineOffset, j + columnOffset) - mean;
				sum += v * v;
			}
		}
		return Math.sqrt(sum / (n - 1));
	}

	/**
	 * Création d'un nouveau Raster correspondant à une partie de l'image
	 * @param lineOffset
	 * @param columnOffset
	 * @param height
	 * @param width
	 * @return
	 */
	public RasterBand subRaster(
		int lineOffset,
		int columnOffset,
		int height,
		int width) {
		RasterBand subRaster= new RasterBand(height, width, values);

		for (int i= lineOffset; i < height + lineOffset; i++) {
			for (int j= columnOffset; j < width + columnOffset; j++) {
				int value= this.pixel(i, j);
				subRaster.pixel(i - lineOffset, j - columnOffset, value);
			}
		}
		return subRaster;
	}

	/**
	 * Calcul de la valeur la moins courante dans l'image
	 * @return
	 */
	public int ucode() {
		/* Calcul de l'histogramme (histo[i]='nombre de pixels de valeur i') */
		int[] histo= new int[values + 1];
		Arrays.fill(histo, 0);
		for (int i= 0; i < array.length; i++) {
			histo[array[i]]++;
		}

		/* Calcul de la couleur ucode ayant le moins d'occurences dans 
		 * l'image */
		int minOcc= array.length;
		int ucode= 0;
		for (int i= 0; i < histo.length; i++) {
			if (histo[i] < minOcc) {
				minOcc= histo[i];
				ucode= i;
			}
		}
		return ucode;
	}

	public int getHeight() {
		return height;
	}

	public int getValues() {
		return values;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int i) {
		height= i;
	}

	public void setValues(int i) {
		values= i;
	}

	public void setWidth(int i) {
		width= i;
	}

}
