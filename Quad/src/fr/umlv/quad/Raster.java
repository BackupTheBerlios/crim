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
public class Raster {
	private int width;
	private int height;
	private int values;
	private int[] array;

	/**
	 * Cr�ation d'un raster vide
	 * @param width
	 * @param height
	 * @param values
	 */
	public Raster(int width, int height, int values) {
		this.width= width;
		this.height= height;
		this.values= values;
		array= new int[width * height];
	}

	/**
	 * Cr�ation d'un raster � partir d'un tableau de valeurs
	 * @param width
	 * @param height
	 * @param values
	 * @param intArray
	 */
	public Raster(int width, int height, int values, int[] intArray) {
		this(width, height, values);
		load(intArray);
	}

	/**
	 * Chargement de valeurs dans le raster
	 * @param intArray
	 */
	public void load(int[] intArray) {
		System.arraycopy(intArray, 0, this.array, 0, this.array.length);
	}

	/**
	 * Chargement d'un raster � partir d'un fichier de n'importe quel type
	 * @param path
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public Raster(String path) throws NumberFormatException, IOException {
		if (path.endsWith(".raw")) {
			BufferedReader inReader=
				new BufferedReader(new InputStreamReader(System.in));

			System.out.println("Param?tres de l'image : ");
			System.out.print("	Hauteur : ");
			int height= Integer.parseInt(inReader.readLine());
			System.out.print("	Largeur : ");
			int width= Integer.parseInt(inReader.readLine());
			System.out.print("	Nombre de valeurs : ");
			int values= Integer.parseInt(inReader.readLine());

			loadFromRaw(path);
		} else if (path.endsWith(".pgm")) {
			loadFromPgm(path);
		} else {
			throw new QuadError(path + ": Extension de fichier incorrecte");
		}
	}

	/**
	 * Cr�ation d'un raster � partir de quatre sous-rasters
	 * @param topLeft
	 * @param topRight
	 * @param bottomLeft
	 * @param bottomRight
	 */
	public Raster(
		Raster topLeft,
		Raster topRight,
		Raster bottomLeft,
		Raster bottomRight) {
		this(topLeft.height * 2, topLeft.width * 2, 255);
		checkDimensions(topLeft, topRight, bottomLeft, bottomRight);
		for (int i= 0; i < topLeft.height; i++) {
			for (int j= 0; j < topLeft.width; j++) {
				int value= topLeft.pixel(i, j);
				pixel(i, j, value);
			}
		}
		for (int i= 0; i < topRight.height; i++) {
			for (int j= 0; j < topRight.width; j++) {
				int value= topRight.pixel(i, j);
				pixel(i, j + width / 2, value);
			}
		}
		for (int i= 0; i < bottomLeft.height; i++) {
			for (int j= 0; j < bottomLeft.width; j++) {
				int value= bottomLeft.pixel(i, j);
				pixel(i + height / 2, j, value);
			}
		}
		for (int i= 0; i < bottomRight.height; i++) {
			for (int j= 0; j < bottomRight.width; j++) {
				int value= bottomRight.pixel(i, j);
				pixel(i + height / 2, j + width / 2, value);
			}
		}
	}

	/**
	 * Pr�dicat de comparaison des dimensions de deux rasters
	 * @param other
	 * @return
	 */
	public boolean hasSameDimensions(Raster other) {
		return (
			height == other.height
				&& width == other.width
				&& values == other.values);
	}

	/**
	 * V�rification que les dimensions des sous-rasters correspondent � celles
	 * du raster
	 */
	private void checkDimensions(
		Raster topLeft,
		Raster topRight,
		Raster bottomLeft,
		Raster bottomRight) {
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

	/**
	 * Charge une image Pgm (n&b)
	 * @param path : Chemin du fichier image
	 */
	private void loadFromPgm(String path)
		throws FileNotFoundException, IOException {
		FileInputStream inputStream= new FileInputStream(path);

		loadHeaderFromPgm(inputStream);
		loadArray(inputStream);
	}

	/**
	 * Charge une image brute
	 * 
	 * @param path : Nom du fichier image
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadFromRaw(String path)
		throws FileNotFoundException, IOException {
		loadArray(new FileInputStream(path));
	}

	/**
	 * Charge l'en-t?te d'une image pgm
	 * 
	 * @param inputStream : Le flux correspondant ? l'image
	 * @throws IOException
	 */
	private void loadHeaderFromPgm(InputStream inputStream)
		throws IOException {
		StreamTokenizer tokenizer= new StreamTokenizer(inputStream);

		/* Lecture du type PGM */
		String typeStr;
		int ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_WORD)
			typeStr= tokenizer.sval;

		/* Lecture de la largeur */
		ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_NUMBER)
			width= (int)tokenizer.nval;

		/* Lecture de la hauteur */
		ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_NUMBER)
			height= (int)tokenizer.nval;

		/* Lecture du nombre de valeurs */
		ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_NUMBER)
			values= (int)tokenizer.nval;
	}

	/**
	 * Charge le contenu brut d'une image (raster)
	 * 
	 * @param inputStream : Flux correspondant ? l'image
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadArray(InputStream inputStream)
		throws FileNotFoundException, IOException {
		array= new int[height * width];
		int n= readArray(inputStream, array);
		if (n < height * width)
			throw new QuadError(
				"Fin de fichier inattendue ? l'octet n?" + n + " du raster");
	}

	/**
	 * Lecture d'un tableau de valeurs depuis un flux
	 * @param in
	 * @param array
	 * @return
	 * @throws IOException
	 */
	private static int readArray(InputStream in, int[] array)
		throws IOException {
		int i= 0;
		for (i= 0; i < array.length; i++) {
			array[i]= in.read();
			if (array[i] == -1)
				break;
		}
		return i;
	}

	/**
	 * Ecriture du tableau de valeur dans un flux
	 * @param out
	 * @param array
	 * @throws IOException
	 */
	private static void writeArray(OutputStream out, int[] array)
		throws IOException {
		for (int i= 0; i < array.length; i++) {
			out.write(array[i]);
		}
	}

	/*-- Sauvegarde d'images ---------------------------------*/

	/**
	 * Sauvegarde vers une image de n'importe quel type
	 */
	public void save(String path) throws IOException {
		if (path.endsWith(".pgm"))
			saveAsPgm(path);
		else
			throw new QuadError("Type de fichier inconnu");
	}

	/**
	 * Sauvegarde de l'image dans un fichier pgm
	 * @param path
	 * @throws IOException
	 */
	private void saveAsPgm(String path) throws IOException {
		OutputStream out= new FileOutputStream(path);
		PrintStream outPS= new PrintStream(out);
		outPS.println("P5");
		outPS.println("" + width + " " + height);
		outPS.println(values);
		outPS.flush();
		writeArray(outPS, array);
	}

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

	/*-- Gestion des pixels ---------------------------------------*/

	/**
	 * R�cup�ration de la valeur d'un pixel
	 */
	public int pixel(int line, int column) {
		checkPixelCoords(line, column);
		return array[line * width + column];
	}

	/**
	 * Mise � jour de la valeur d'un pixel
	 * @param line
	 * @param column
	 * @param value
	 */
	public void pixel(int line, int column, int value) {
		checkPixelCoords(line, column);
		array[line * width + column]= value;
	}

	/**
	 * V�rifie la validit� des coordonn�es d'un pixel
	 * @param line
	 * @param column
	 */
	private void checkPixelCoords(int line, int column) {
		boolean ok= true;
		StringBuffer msgBuf= new StringBuffer();

		if (line >= height || line < 0) {
			msgBuf.append(
				"Impossible d'aller � la ligne demand�e (" + line + ")");
			ok= false;
		}
		if (column >= width || column < 0) {
			msgBuf.append(
				"\nImpossible d'aller � la colonne demand�e ("
					+ column
					+ ")");
			ok= false;
		}
		if (!ok)
			throw new QuadError(msgBuf.toString());
	}

	/*-- Traitements -----------------------------------------------*/

	public int numPixels() {
		return array.length;
	}

	public double defaultValue(
		int lineOffset,
		int columnOffset,
		int height,
		int width)
	{
		return mean(lineOffset, columnOffset, height, width);
//		return pixel(lineOffset, columnOffset);
	}

	private double mean(
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
			return Double.NaN;
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

	public Raster subRaster(int lineOffset, int columnOffset) {
		int width= this.width();
		int height= this.height();
		int values= this.values();

		Raster subRaster= new Raster(width / 2, height / 2, values);
		for (int i= lineOffset; i < height / 2 + lineOffset; i++) {
			for (int j= columnOffset; j < width / 2 + columnOffset; j++) {
				int value= this.pixel(i, j);
				subRaster.pixel(i - lineOffset, j - columnOffset, value);
			}
		}
		return subRaster;
	}

	public boolean isUni(
		int lineOffset,
		int columnOffset,
		int height,
		int width) {
		if (height * width <= 1)
			return true;
		double stddev= stddev(lineOffset, columnOffset, height, width);
		if (stddev < 15)
			return true;
		return false;
	}

	private int max(int b1, int b2) {
		return (b1 > b2) ? b1 : b2;
	}

	private int min(int b1, int b2) {
		return (b1 < b2) ? b1 : b2;
	}

	public int max() {
		int max= 0;
		for (int i= 0; i < array.length; i++) {
			max= max(array[i], max);
		}
		return max;
	}

	public int min() {
		int min= values;
		for (int i= 0; i < array.length; i++) {
			min= min(array[i], min);
		}
		return min;
	}

	public static void main(String[] args) throws IOException {
		Raster r= new Raster("images/Boat.512.pgm");
		r.save("out/image.pgm");
		double stddev2= r.stddev(0, 0, r.height(), r.width());
		double mean2= r.mean(0, 0, r.height(), r.width());
		System.out.println("stddev(...): " + stddev2);
		System.out.println("mean(...): " + mean2);
	}

	public int[] getArray() {
		return array;
	}
}
