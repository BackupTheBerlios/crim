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

	int numBands;

	private int[] array;
	private final String path;

	/**
	 * Création d'un raster vide
	 * @param width
	 * @param height
	 * @param values
	 */
	public Raster(int height, int width, int numBands, int values) {
		this.width= width;
		this.height= height;
		this.values= values;
		this.numBands= numBands;
		array= new int[width * height * numBands];
		path= null;
	}

	/**
	 * Création d'un raster à partir d'un tableau de valeurs
	 * @param width
	 * @param height
	 * @param values
	 * @param intArray
	 */
	public Raster(
		int height,
		int width,
		int values,
		int numBands,
		int[] intArray) {
		this(height, width, numBands, values);
		load(intArray);
	}

	/**
	 * Création d'un raster à partir d'une valeur par défaut
	 */
	public Raster(
		int height,
		int width,
		int values,
		int numBands,
		int defaultValue) {
		this(height, width, numBands, values);
		Arrays.fill(array, defaultValue);
	}

	/**
	 * Chargement de valeurs dans le raster
	 * @param intArray
	 */
	public void load(int[] intArray) {
		System.arraycopy(intArray, 0, this.array, 0, this.array.length);
	}

	/**
	 * Chargement d'un raster à partir d'un fichier
	 * @param path
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public Raster(String path) throws NumberFormatException, IOException {
		this.path= path;

		if (path.endsWith(".raw")) {
			BufferedReader inReader=
				new BufferedReader(new InputStreamReader(System.in));

			System.out.println("Paramètres de l'image : ");
			System.out.print("	Hauteur : ");
			int height= Integer.parseInt(inReader.readLine());
			System.out.print("	Largeur : ");
			int width= Integer.parseInt(inReader.readLine());
			System.out.print("	Nombre de valeurs : ");
			int values= Integer.parseInt(inReader.readLine());

			loadRaw(path);
		} else if (path.endsWith(".pgm")) {
			loadPgm(path);
		} else {
			throw new QuadError(path + ": Extension de fichier incorrecte");
		}
	}

	/**
	 * Création d'un raster à partir de quatre sous-rasters
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
		this(topLeft.height * 2, topLeft.width * 2, topLeft.numBands, 255);
		checkDimensions(topLeft, topRight, bottomLeft, bottomRight);
		for (int i= 0; i < topLeft.height; i++) {
			for (int j= 0; j < topLeft.width; j++) {
				for (int k= 0; k < topLeft.numBands; k++) {
					pixel(i, j, k, topLeft.pixel(i, j, k));
					pixel(i, j + width / 2, k, topRight.pixel(i, j, k));
					pixel(i + height / 2, j, k, bottomLeft.pixel(i, j, k));
					pixel(
						i + height / 2,
						j + width / 2,
						k,
						bottomRight.pixel(i, j, k));
				}
			}
		}
	}

	/**
	 * Test d'égalité des dimensions de deux rasters
	 * @param other
	 * @return
	 */
	public boolean hasSameDimensions(Raster other) {
		return (
			height == other.height
				&& width == other.width
				&& numBands == other.numBands
				&& values == other.values);
	}

	/**
	 * Test de la validité des dimensions des sous-rasters par rapport à celles
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
				&& numBands == topLeft.numBands
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
	private void loadPgm(String path)
		throws FileNotFoundException, IOException {
		FileInputStream inputStream= new FileInputStream(path);

		loadPgmHeader(inputStream);
		loadArray(inputStream);
	}

	/**
	 * Charge une image brute
	 * @param path : Nom du fichier image
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadRaw(String path)
		throws FileNotFoundException, IOException {
		loadArray(new FileInputStream(path));
	}

	/**
	 * Charge l'en-tête d'une image pgm
	 * @param inputStream : Le flux correspondant ? l'image
	 * @throws IOException
	 */
	private void loadPgmHeader(InputStream inputStream) throws IOException {
		StreamTokenizer tokenizer= new StreamTokenizer(inputStream);

		/* Lecture du type PGM */
		String typeStr;
		int ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_WORD)
			typeStr= tokenizer.sval;
		else
			throw new QuadError(
				path + ": Erreur lors de la lecture du type PGM");
		if (!typeStr.equals("P5"))
			throw new QuadError(
				path
					+ ": Seules les images en niveaux de gris au format binaire sont gérées");

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
	}

	/**
	 * Charge le contenu brut d'une image (raster)
	 * @param inputStream : Flux correspondant à l'image
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadArray(InputStream inputStream)
		throws FileNotFoundException, IOException {
		array= new int[height * width];
		int n= readArray(inputStream, array);
		if (n < height * width)
			throw new QuadError(
				"Fin de fichier inattendue à l'octet n°" + n + " du raster");
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
			savePgm(path);
		else
			throw new QuadError("Type de fichier inconnu");
	}

	/**
	 * Sauvegarde de l'image dans un fichier pgm
	 * @param path
	 * @throws IOException
	 */
	private void savePgm(String path) throws IOException {
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
	public int[] getArray() {
		return array;
	}

	/*-- Gestion des pixels ---------------------------------------*/

	/**
	 * Récupération de la valeur d'un pixel
	 */
	public int pixel(int line, int column, int band) {
		checkPixelCoords(line, column, band);
		return array[(line * width + column) * numBands + band];
	}

	/**
	 * Mise à jour de la valeur d'un pixel
	 * @param line
	 * @param column
	 * @param value
	 */
	public void pixel(int line, int column, int band, int value) {
		checkPixelCoords(line, column, band);
		array[(line * width + column) * numBands + band]= value;
	}

	/**
	 * Vérifie la validité des coordonnées d'un pixel
	 * @param line
	 * @param column
	 */
	private void checkPixelCoords(int line, int column, int band) {
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
		if (band >= numBands || band < 0) {
			msgBuf.append(
				"\nImpossible d'aller à la composante demandée (" + band + ")");
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
		int width,
		int band) {
		double mean= 0;
		for (int i= 0; i < height; i++) {
			for (int j= 0; j < width; j++) {
				mean += pixel(i + lineOffset, j + columnOffset, band);
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
		int width,
		int band) {
		//	   sd is sqrt of sum of (values-mean) squared divided by n - 1

		//	   Calculate the mean
		double mean= 0;
		final int n= height * width;
		if (n <= 1)
			throw new QuadError("Calcul de la variance impossible pour moins de deux valeurs");
		for (int i= 0; i < height; i++) {
			for (int j= 0; j < width; j++) {
				mean += pixel(i + lineOffset, j + columnOffset, band);
			}
		}
		mean /= n;

		//	   calculate the sum of squares
		double sum= 0;
		for (int i= 0; i < height; i++) {
			for (int j= 0; j < width; j++) {
				final double v=
					pixel(i + lineOffset, j + columnOffset, band) - mean;
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
	public Raster subRaster(
		int lineOffset,
		int columnOffset,
		int height,
		int width) {
		Raster subRaster= new Raster(height, width, numBands, values);

		for (int i= lineOffset; i < height + lineOffset; i++) {
			for (int j= columnOffset; j < width + columnOffset; j++) {
				for (int k= 0; k < numBands; k++) {
					int value= this.pixel(i, j, k);
					subRaster.pixel(i - lineOffset, j - columnOffset, k, value);
				}
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
}
