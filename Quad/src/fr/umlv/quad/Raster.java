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
	private byte[] byteArray;

	public Raster(int width, int height, int values) {
		this.width= width;
		this.height= height;
		this.values= values;
		byteArray= new byte[width * height];
	}

	public Raster(int width, int height, int values, byte[] byteArray) {
		this(width, height, values);
		load(byteArray);
	}

	public void load(byte[] byteArray) {
		System.arraycopy(
			byteArray,
			0,
			this.byteArray,
			0,
			this.byteArray.length);
	}

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

	public Raster(
		Raster topLeft,
		Raster topRight,
		Raster bottomLeft,
		Raster bottomRight) {
		this(topLeft.height * 2, topLeft.width * 2, 255);
		checkDimensions(topLeft, topRight, bottomLeft, bottomRight);
		for (int i= 0; i < topLeft.height; i++) {
			for (int j= 0; j < topLeft.width; j++) {
				byte value= topLeft.pixel(i, j);
				pixel(i, j, value);
			}
		}
		for (int i= 0; i < topRight.height; i++) {
			for (int j= 0; j < topRight.width; j++) {
				byte value= topRight.pixel(i, j);
				pixel(i, j + width / 2, value);
			}
		}
		for (int i= 0; i < bottomLeft.height; i++) {
			for (int j= 0; j < bottomLeft.width; j++) {
				byte value= bottomLeft.pixel(i, j);
				pixel(i + height / 2, j, value);
			}
		}
		for (int i= 0; i < bottomRight.height; i++) {
			for (int j= 0; j < bottomRight.width; j++) {
				byte value= bottomRight.pixel(i, j);
				pixel(i + height / 2, j + width / 2, value);
			}
		}
	}

	public boolean hasSameDimensions(Raster other) {
		return (
			height == other.height
				&& width == other.width
				&& values == other.values);
	}

	private void checkDimensions(
		Raster topLeft,
		Raster topRight,
		Raster bottomLeft,
		Raster bottomRight) {
		boolean ok=
			(topLeft.hasSameDimensions(topRight)
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
		loadRaster(inputStream);
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
		loadRaster(new FileInputStream(path));
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
	private void loadRaster(InputStream inputStream)
		throws FileNotFoundException, IOException {
		byteArray= new byte[height * width];
		int n= inputStream.read(byteArray);
		if (n < height * width)
			throw new QuadError(
				"Fin de fichier inattendue ? l'octet n?" + n + " du raster");
	}

	/*-- Sauvegarde d'images ---------------------------------*/

	public void save(String path) throws IOException {
		if (path.endsWith(".pgm"))
			saveAsPgm(path);
		else
			throw new QuadError("Type de fichier inconnu");
	}

	private void saveAsPgm(String path) throws IOException {
		OutputStream out= new FileOutputStream(path);
		PrintStream outPS= new PrintStream(out);
		outPS.println("P5");
		outPS.println("" + width + " " + height);
		outPS.println(values);
		outPS.flush();
		outPS.write(byteArray);
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

	public byte pixel(int line, int column) {
		checkPixelCoords(line, column);
		return byteArray[line * width + column];
	}

	public void pixel(int line, int column, byte value) {
		checkPixelCoords(line, column);
		byteArray[line * width + column]= value;
	}

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

	public int numPixels() {
		return byteArray.length;
	}

	public byte defaultValue() {
		return mean();
	}

	private byte mean() {
		double mean= 0;
		int numPixels= numPixels();
		for (int i= 0; i < numPixels; i++) {
			mean += byteArray[i];
		}
		mean /= numPixels;
		return (byte)mean;
	}
	public byte defaultValue(
		int lineOffset,
		int columnOffset,
		int height,
		int width) {
		return mean(lineOffset, columnOffset, height, width);
	}

	private byte mean(
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
		return (byte)mean;
	}

	public Raster subRaster(int lineOffset, int columnOffset) {
		int width= this.width();
		int height= this.height();
		int values= this.values();

		Raster subRaster= new Raster(width / 2, height / 2, values);
		for (int i= lineOffset; i < height / 2 + lineOffset; i++) {
			for (int j= columnOffset; j < width / 2 + columnOffset; j++) {
				byte value= this.pixel(i, j);
				subRaster.pixel(i - lineOffset, j - columnOffset, value);
			}
		}
		return subRaster;
	}
}
