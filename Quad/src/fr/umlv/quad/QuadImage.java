/*
 * Created on 3 avr. 2004
 */
package fr.umlv.quad;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StreamTokenizer;

/**
 * @author cpele
 */
public class QuadImage {
	public final static int RGB= 0;
	public final static int YCBCR= 1;

	private QuadImageBand bands[];
	private int numBands;
	private int colorspace= YCBCR;
	
	public QuadImage(String path) throws IOException {
		if (path.matches("^(.*)\\.(pgm|ppm)$")) {
			Raster rasterRgb= new Raster(path);
			Raster raster;

			if (colorspace == YCBCR)
				raster= rasterRgb.toYcbcr();
			else if (colorspace == RGB)
				raster= rasterRgb;
			else
				throw new QuadError("Espace de couleurs inconnu");

			numBands= raster.getNumBands();
			bands= new QuadImageBand[numBands];
			for (int i= 0; i < numBands; i++) {
				bands[i]= new QuadImageBand(raster.getBand(i));
			}
		} else if (path.matches("^(.*)\\.(qgm|qpm)$")) {
			FileInputStream inputStream= new FileInputStream(path);

			loadQgmHeader(inputStream);
			for (int i= 0; i < numBands; i++)
				bands[i].loadQgmData(inputStream);
		} else if (path.matches("^(.*)\\.(hgm|hpm)$")) {
			loadHuffman(path);
		} else {
			throw new QuadError(path + ": Format de fichier inconnu");
		}
	}

	/*-------------------------------------------------------------*/
	/*-- Gestion des fichiers QGM ---------------------------------*/
	/*-------------------------------------------------------------*/

	/**
	 * Chargement de l'en-tête du fichier QGM
	 */
	private void loadQgmHeader(FileInputStream inputStream)
		throws IOException {
		StreamTokenizer tokenizer= new StreamTokenizer(inputStream);

		/* Lecture du type QGM */
		String typeStr;
		int ttype= tokenizer.nextToken();
		if (ttype == tokenizer.TT_WORD) {
			typeStr= tokenizer.sval;

			if (typeStr.equals("Q1")) {
				numBands= 1;
				bands= new QuadImageBand[1];
				colorspace= RGB;
			} else if (typeStr.equals("Q2")) {
				numBands= 3;
				bands= new QuadImageBand[3];
				colorspace= RGB;
			} else if (typeStr.equals("Q3")) {
				numBands= 3;
				bands= new QuadImageBand[3];
				colorspace= YCBCR;
			} else {
				throw new QuadError("Type inconnu: " + typeStr);
			}
		} else
			throw new QuadError("Erreur lors de la lecture du type QGM");

		/* Lecture des paramètres de chaque composante */
		for (int i= 0; i < numBands; i++) {
			QuadImageBand quad= new QuadImageBand();
			bands[i]= quad;

			/* Lecture du nombre de niveaux */
			ttype= tokenizer.nextToken();
			if (ttype == tokenizer.TT_NUMBER)
				quad.setNumLevels((int)tokenizer.nval);
			else
				throw new QuadError(
					"Erreur lors de la lecture du nombre de niveaux pour la composante "
						+ i);

			/* Lecture du nombre de valeurs */
			ttype= tokenizer.nextToken();
			if (ttype == tokenizer.TT_NUMBER)
				quad.setMaxValue((int)tokenizer.nval);
			else
				throw new QuadError(
					"Erreur lors de la lecture de la valeur maximale pour la composante "
						+ i);

			/* Lecture de la valeur du marqueur ucode */
			ttype= tokenizer.nextToken();
			if (ttype == tokenizer.TT_NUMBER)
				quad.setUcode((int)tokenizer.nval);
			else
				throw new QuadError(
					"Erreur lors de la lecture du marqueur ucode pour la composante "
						+ i);
		}
	}

	public void save(String path) throws FileNotFoundException {
		if (path.matches("^(.*)\\.(pgm|ppm)$")) {
			savePortableMap(path);
		} else if (path.matches("^(.*)\\.(qgm|qpm)$")) {
			saveQuadMap(path);
		} else if (path.matches("^(.*)\\.(hgm|hpm)$")) {
			saveHuffman(path);
		} else
			throw new QuadError(path + ": Format de fichier inconnu");
	}

	private void savePortableMap(String path) throws FileNotFoundException {
		RasterBand[] rasterBands= new RasterBand[numBands];
		for (int i= 0; i < numBands; i++) {
			rasterBands[i]= bands[i].toRasterBand();
		}

		Raster raster= new Raster(rasterBands);
		Raster rasterRgb;
		if (colorspace == RGB)
			rasterRgb= raster;
		else if (colorspace == YCBCR)
			rasterRgb= raster.toRgb();
		else
			throw new QuadError("Espace de couleurs inconnu");

		rasterRgb.save(path);
	}

	public void saveQuadMap(String path) throws FileNotFoundException {
		PrintStream out= new PrintStream(new FileOutputStream(path));

		if (numBands == 1) {
			out.println("Q1");
		} else if (numBands == 3) {
			if (colorspace == RGB)
				out.println("Q2");
			else if (colorspace == YCBCR)
				out.println("Q3");
			else
				throw new QuadError("Espace de couleurs inconnu");
		} else
			throw new QuadError("Quoi ?! On ne devrait pas arriver là ! C'est un bug !");

		for (int i= 0; i < numBands; i++) {
			int numLevels= bands[i].getNumLevels();
			int maxValue= bands[i].getMaxValue();
			int ucode= bands[i].getUcode();
			out.println(numLevels + " " + maxValue + " " + ucode);
		}
		for (int i= 0; i < numBands; i++) {
			bands[i].saveQgmData(out);
		}
	}

	public void compress(double dev, double factor) {
		for (int i= 0; i < numBands; i++) {
			compress(dev, factor, i);
		}
	}

	public void compress(double dev, double factor, int band) {
		bands[band].compress(dev, factor);
	}

	public void compress(double dev[], double factor[]) {
		for (int i= 0; i < numBands; i++) {
			compress(dev[i], factor[i], i);
		}
	}

	/*---------------------------------------------------------------*/
	/*-- Gestion des fichiers codés avec l'algo de huffman ----------*/
	/*---------------------------------------------------------------*/

	private void loadHuffman(String path) {
		// TODO Auto-generated method stub
		
	}

	private void saveHuffman(String path) {
		// TODO Auto-generated method stub
		
	}

}
