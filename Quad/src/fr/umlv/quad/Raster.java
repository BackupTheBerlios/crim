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

	public Raster(int height, int width, int values, int numBands) {
		this.height= height;
		this.width= width;
		this.values= values;
		this.numBands= numBands;

		bands= new RasterBand[numBands];
		for (int i= 0; i < numBands; i++) {
			bands[i]= new RasterBand(height, width, values);
		}
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
					out.write((int)pixel(i, j, k));
				}
			}
		}
	}

	private double pixel(int line, int column, int band) {
		return bands[band].pixel(line, column);
	}

	private void pixel(int line, int column, int band, double value) {
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
		} else if (typeStr.equals("P6")) {
			numBands= 3;
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

		bands= new RasterBand[numBands];
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

	public Raster toRgb() {
		if (numBands == 1) {
			throw new QuadError("Conversion impossible pour une image en niveaux de gris");
		}

		Raster rasterRgb= new Raster(height, width, values, numBands);
		double lumR= .299;
		double lumG= .587;
		double lumB= .114;

		for (int i= 0; i < height; i++) {
			for (int j= 0; j < width; j++) {
				double Y= pixel(i, j, 0);
				double Cb= pixel(i, j, 1);
				double Cr= pixel(i, j, 2);

				double R= Y + (Cr - 128) * (2 - 2 * lumR);
				double B= Y + (Cb - 128) * (2 - 2 * lumB);
				double G= (Y - lumB * B - lumR * R) / lumG;

				R= Math.rint(R);
				if (R < 0)
					R= 0;
				if (R > 255)
					R= 255;

				G= Math.rint(G);
				if (G < 0)
					G= 0;
				if (G > 255)
					G= 255;

				B= Math.rint(B);
				if (B < 0)
					B= 0;
				if (B > 255)
					B= 255;

				rasterRgb.pixel(i, j, 0, R);
				rasterRgb.pixel(i, j, 1, G);
				rasterRgb.pixel(i, j, 2, B);
			}
		}
		return rasterRgb;
	}

	public Raster toYcbcr() {
		if (numBands == 1) {
			throw new QuadError("Conversion impossible pour une image en niveaux de gris");
		}

		Raster rasterYcbcr= new Raster(height, width, values, numBands);
		double lumR= .299;
		double lumG= .587;
		double lumB= .114;

		for (int i= 0; i < height; i++) {
			for (int j= 0; j < width; j++) {
				double R= pixel(i, j, 0);
				double G= pixel(i, j, 1);
				double B= pixel(i, j, 2);

				double Y= lumR * R + lumG * G + lumB * B;
				double Cb= (B - Y) / (2 - 2 * lumB) + 128;
				double Cr= (R - Y) / (2 - 2 * lumR) + 128;

				rasterYcbcr.pixel(i, j, 0, Y);
				rasterYcbcr.pixel(i, j, 1, Cb);
				rasterYcbcr.pixel(i, j, 2, Cr);
			}
		}
		return rasterYcbcr;
	}

}
