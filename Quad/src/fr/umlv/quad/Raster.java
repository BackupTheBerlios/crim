/*
 * Created on 4 avr. 2004
 */
package fr.umlv.quad;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StreamTokenizer;

/**
 * @author cpele
 */
public class Raster {
	public Raster(RasterBand[] rasterBands) {
		numBands= rasterBands.length;
		if (numBands != 1 && numBands != 3)
			throw new QuadError("Le nombre de composantes doit valoir 1 ou 3");
		bands= rasterBands;

		width= rasterBands[0].width();
		height= rasterBands[0].height();
		values= rasterBands[0].values();
	}

	public Raster(String path) throws IOException {
		if (path.matches("^(.*)\\.(pgm|ppm)$")) {
			FileInputStream in= new FileInputStream(path);
			loadPgmHeader(in);

				for (int j= 0; j < height; j++) {
					for (int k= 0; k < width; k++) {
						for (int i= 0; i < numBands; i++) {
						int value= in.read();
						if (value == -1)
							throw new QuadError(
								path + ": Fin de fichier inattendue");
						bands[i].pixel(j, k, value);
					}
				}
			}
		} else {
			throw new QuadError(path + ": Format de fichier inconnu");
		}
	}

	private int numBands;
	private RasterBand[] bands;
	private int width, height, values;

	public void save(String path) throws FileNotFoundException {
		PrintStream out= new PrintStream(new FileOutputStream(path));

		if (numBands == 1) {
			out.println("P5");
		} else if (numBands == 3) {
			out.println("P6");
		} else {
			throw new QuadError("On ne devrait pas arriver là, c'est un bug pourri, fuyez !");
		}

		out.println("" + width + " " + height + " " + values);

		for (int i= 0; i < height; i++) {
			for (int j= 0; j < width; j++) {
				for (int k= 0; k < numBands; k++) {
					out.write(pixel(i, j, k));
				}
			}
		}
	}

	private int pixel(int line, int column, int band) {
		return bands[band].pixel(line, column);
	}

	private void pixel(int line, int column, int band, int value) {
		bands[band].pixel(line, column, value);
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
			throw new QuadError("Erreur lors de la lecture du type PGM");
		if (typeStr.equals("P5")) {
			numBands= 1;
			bands= new RasterBand[1];
		} else if (typeStr.equals("P6")) {
			numBands= 3;
			bands= new RasterBand[3];
		} else {
			throw new QuadError("Type de fichier inconnu: " + typeStr);
		}

		/* Lecture de la largeur */
		ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_NUMBER)
			width= (int)tokenizer.nval;
		else
			throw new QuadError("Erreur lors de la lecture de la largeur");

		/* Lecture de la hauteur */
		ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_NUMBER)
			height= (int)tokenizer.nval;
		else
			throw new QuadError("Erreur lors de la lecture de la hauteur");

		/* Lecture du nombre de valeurs */
		ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_NUMBER)
			values= (int)tokenizer.nval;
		else
			throw new QuadError("Erreur lors de la lecture de la valeur maximale");

		for (int i= 0; i < numBands; i++) {
			bands[i]= new RasterBand(height, width, values);
		}
	}

	/*-- Sauvegarde d'images ---------------------------------*/

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
	}

	public RasterBand getBand(int i) {
		if (i < numBands)
			return bands[i];
		throw new QuadError("Impossible de récupérer la composante " + i);
	}

	public int getNumBands() {
		return numBands;
	}

}
