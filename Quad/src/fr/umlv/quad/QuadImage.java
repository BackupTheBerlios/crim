/*
 * Created on 3 avr. 2004
 */
package fr.umlv.quad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StreamTokenizer;

import fr.umlv.quad.huffman.Decoder;
import fr.umlv.quad.huffman.Encoder;

/**
 * @author cpele
 */
public class QuadImage {
	public final static int RGB= 0;
	public final static int YCBCR= 1;

	private QuadImageChannel channels[];
	private int numChannels;
	private int colorspace= RGB;

	public QuadImage(String path) throws IOException, ClassNotFoundException {
		if (path.matches("^(.*)\\.(pgm|ppm)$")) {
			Raster rasterRgb= new Raster(path);
			Raster raster;
			
			if (rasterRgb.getNumChannels()==1) {
				raster= rasterRgb;
			} else if (rasterRgb.getNumChannels() == 3) {
				if (colorspace == YCBCR)
					raster= rasterRgb.toYcbcr();
				else if (colorspace == RGB)
					raster= rasterRgb;
				else
					throw new QuadError("Espace de couleurs inconnu");
			} else
				throw new QuadError("Nombre de canaux inconnu");

			numChannels= raster.getNumChannels();
			channels= new QuadImageChannel[numChannels];
			for (int i= 0; i < numChannels; i++) {
				channels[i]= new QuadImageChannel(raster.getBand(i));
			}
		} else if (path.matches("^(.*)\\.(qgm|qpm)$")) {
			loadQgm(path);
		} else if (path.matches("^(.*)\\.(hgm|hpm)$")) {
			loadHuffman(path);
		} else {
			throw new QuadError(path + ": Format de fichier inconnu");
		}
	}

	/*-------------------------------------------------------------*/
	/*-- Gestion des fichiers QGM ---------------------------------*/
	/*-------------------------------------------------------------*/

	private void loadQgm(String path) throws IOException {
		FileInputStream inputStream= new FileInputStream(path);

		loadQgmHeader(inputStream);
		for (int i= 0; i < numChannels; i++)
			channels[i].loadQgmData(inputStream);
	}

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
				numChannels= 1;
				channels= new QuadImageChannel[1];
				colorspace= RGB;
			} else if (typeStr.equals("Q2")) {
				numChannels= 3;
				channels= new QuadImageChannel[3];
				colorspace= RGB;
			} else if (typeStr.equals("Q3")) {
				numChannels= 3;
				channels= new QuadImageChannel[3];
				colorspace= YCBCR;
			} else {
				throw new QuadError("Type inconnu: " + typeStr);
			}
		} else
			throw new QuadError("Erreur lors de la lecture du type QGM");

		/* Lecture des paramètres de chaque composante */
		for (int i= 0; i < numChannels; i++) {
			QuadImageChannel quad= new QuadImageChannel();
			channels[i]= quad;

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

	public void save(String path) throws IOException {
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
		RasterChannel[] rasterChannels= new RasterChannel[numChannels];
		for (int i= 0; i < numChannels; i++) {
			rasterChannels[i]= channels[i].toRasterChannel();
		}

		Raster raster= new Raster(rasterChannels);
		Raster rasterRgb;
		if (colorspace == RGB)
			rasterRgb= raster;
		else if (colorspace == YCBCR)
			rasterRgb= raster.toRgb();
		else
			throw new QuadError("Espace de couleurs inconnu");

		rasterRgb.save(path);
	}

	private void saveQuadMap(String path) throws FileNotFoundException {
		PrintStream out= new PrintStream(new FileOutputStream(path));

		if (numChannels == 1) {
			out.println("Q1");
		} else if (numChannels == 3) {
			if (colorspace == RGB)
				out.println("Q2");
			else if (colorspace == YCBCR)
				out.println("Q3");
			else
				throw new QuadError("Espace de couleurs inconnu");
		} else
			throw new QuadError("Quoi ?! On ne devrait pas arriver là ! C'est un bug !");

		for (int i= 0; i < numChannels; i++) {
			int numLevels= channels[i].getNumLevels();
			int maxValue= channels[i].getMaxValue();
			int ucode= channels[i].getUcode();
			out.println(numLevels + " " + maxValue + " " + ucode);
		}
		for (int i= 0; i < numChannels; i++) {
			channels[i].saveQgmData(out);
		}
	}

	public void compress(double dev, double factor) {
		for (int i= 0; i < numChannels; i++) {
			compress(dev, factor, i);
		}
	}

	public void compress(double dev, double factor, int band) {
		channels[band].compress(dev, factor);
	}

	public void compress(double dev[], double factor[]) {
		for (int i= 0; i < numChannels; i++) {
			compress(dev[i], factor[i], i);
		}
	}

	/*---------------------------------------------------------------*/
	/*-- Gestion des fichiers codés avec l'algo de huffman ----------*/
	/*---------------------------------------------------------------*/

	private void loadHuffman(String path)
		throws IOException, ClassNotFoundException {
		String baseName= new File(path).getName();
		File tmpQgmFile= File.createTempFile(baseName, ".qgm");
		String tmpQgm= tmpQgmFile.getCanonicalPath();

		Decoder decoder= new Decoder(path, tmpQgm);
		decoder.decode();
		loadQgm(tmpQgm);
		tmpQgmFile.delete();
	}

	private void saveHuffman(String path) throws IOException {
		String baseName= new File(path).getName();
		File tmpQgmFile= File.createTempFile(baseName, ".qgm");
		String tmpQgm= tmpQgmFile.getCanonicalPath();

		saveQuadMap(tmpQgm);
		Encoder encoder= new Encoder(tmpQgm, path);
		encoder.encode();
		tmpQgmFile.delete();
	}

	public int getNumChannels() {
		return numChannels;
	}

}
