/*
 * Created on 20 mars 2004
 */
package fr.umlv.quad.ascending;

import java.awt.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import fr.umlv.quad.QuadError;
import fr.umlv.quad.Raster;
import fr.umlv.quad.ascending.*;

/**
 * @author cpele
 */
public class QuadImage {
	private double numLevels;

	private QuadImageElement quadRoot;

	public QuadImage(String path) throws NumberFormatException, IOException {
		ArrayList levels= new ArrayList();

		Raster raster= new Raster(path);
		int[] array= raster.getArray();

		/* Construction du niveau n (feuilles du quadtree) */
		QuadImageElement[] currentLevel= new QuadImageElement[array.length];
		for (int i= 0; i < array.length / 4; i++) {
			for (int j= 0; j < 2; j++) {
				for (int k= 0; k < 2; k++) {
					int curLevelIdx= 4 * i + 2 * j + k;
					currentLevel[curLevelIdx]= new QuadImageElement();

					currentLevel[curLevelIdx].topLeft= null;
					currentLevel[curLevelIdx].topRight= null;
					currentLevel[curLevelIdx].bottomRight= null;
					currentLevel[curLevelIdx].bottomLeft= null;

					currentLevel[curLevelIdx].value= array[i];
					currentLevel[curLevelIdx].plain= true;
				}
			}
		}
		levels.add(currentLevel);

		/* Construction des autres niveaux */
		do {
			QuadImageElement[] prevLevel= currentLevel;
			currentLevel= new QuadImageElement[prevLevel.length / 4];
			for (int i= 0; i < currentLevel.length; i++) {
				currentLevel[i]= new QuadImageElement();
				currentLevel[i].topLeft= prevLevel[i * 4];
				currentLevel[i].topRight= prevLevel[i * 4 + 1];
				currentLevel[i].bottomRight= prevLevel[i * 4 + 2];
				currentLevel[i].bottomLeft= prevLevel[i * 4 + 3];
				int mean= 0;
				for (int j= 0; j < 4; j++) {
					mean += prevLevel[i * 4 + j].value;
				}
				mean /= 4;
				currentLevel[i].value= mean;
				currentLevel[i].plain= false;
			}
			levels.add(currentLevel);
		} while (currentLevel.length > 1);

		quadRoot= currentLevel[0];
		numLevels= levels.size();
	}

	public void save(String path) throws IOException {
		/* Génération d'une image pgm */
		if (path.endsWith(".pgm")) {
			/* On sait que l'image est carrée et on connaît son nombre de 
			 * pixels */
			int width= (int)Math.sqrt(numLevels);
			int height= width;

			Raster r= quadRoot.toRaster(height, width);
			r.save(path);
		} else if (path.endsWith(".dot")) {
		} else {
			throw new QuadError(path + ": Type de fichier inconnu");
		}

	}
}