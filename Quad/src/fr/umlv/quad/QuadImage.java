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
	private QuadImageBand bands[];
	private int numBands;

	public QuadImage(String path) throws IOException {
		if (path.matches("^(.*)\\.(pgm|ppm)$")) {
			Raster raster=new Raster(path);
			
			numBands=raster.getNumBands();
			bands=new QuadImageBand[numBands];
			for (int i=0; i<numBands; i++) {
				bands[i]=new QuadImageBand(raster.getBand(i));
			}
		} else if (path.matches("^(.*)\\.(qgm|qpm)$")) {
			FileInputStream inputStream= new FileInputStream(path);
			
			loadQgmHeader(inputStream);
			for (int i= 0; i < numBands; i++)
				bands[i].loadQgmData(inputStream);
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
			} else if (typeStr.equals("Q2")) {
				numBands= 3;
				bands= new QuadImageBand[3];
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
		} else
			throw new QuadError(path + ": Format de fichier inconnu");
	}

	private void savePortableMap(String path) throws FileNotFoundException {
		RasterBand[] rasterBands=new RasterBand[numBands];
		for (int i=0; i<numBands; i++) {
			rasterBands[i]=bands[i].toRasterBand();
		}
		Raster raster=new Raster(rasterBands);
		raster.save(path);
	}

	public void saveQuadMap(String path) throws FileNotFoundException {
		PrintStream out= new PrintStream(new FileOutputStream(path));
		
		if (numBands==1) {
			out.println("Q1");
		} else if (numBands==3) {
			out.println("Q2");
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
			bands[i].compress(dev, factor);
		}
	}

}
