/*
 * Cr?? le 5 f?vr. 2004
 */
package fr.umlv.quad;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.util.Arrays;

/**
 * @author cpele
 * 
 * El?ment d'une QuadImage (pixel de l'image ou noeud de l'arbre)
 */
public class QuadNode {
	private int value;
	private boolean plain;

	private QuadNode topLeftChild;
	private QuadNode topRightChild;
	private QuadNode bottomLeftChild;
	private QuadNode bottomRightChild;

	public QuadNode() {
	}

	/** 
	 * Création du quadtree à partir du raster qui a été chargé
	 */
	public QuadNode(
		Raster raster,
		int currentLineOffset,
		int currentColumnOffset,
		int currentHeight,
		int currentWidth)
	throws IOException {
		/* Condition d'arrêt de la récursion : la région du raster considérée 
		 * est homogène */
		if (raster
			.isPlainPixel(
				currentLineOffset,
				currentColumnOffset,
				currentHeight,
				currentWidth)) {
			this.setValue(
				(int)raster.mean(
					currentLineOffset,
					currentColumnOffset,
					currentHeight,
					currentWidth));
			this.setPlain(true);
			return;
		} else {
			this.setValue(-1);
			this.setPlain(false);
		}

		/*-- Appels récursifs ------------------------------------------*/

		/* Appel récursif (quart supérieur gauche) */
		int topLeftLineOffset= currentLineOffset;
		int topLeftColumnOffset= currentColumnOffset;
		int topLeftHeight= currentHeight / 2;
		int topLeftWidth= currentWidth / 2;
		this.setTopLeftChild(
			new QuadNode(
				raster,
				topLeftLineOffset,
				topLeftColumnOffset,
				topLeftHeight,
				topLeftWidth));

		/* Appel récursif (quart supérieur droit) */
		int topRightLineOffset= currentLineOffset;
		int topRightColumnOffset= currentColumnOffset + currentWidth / 2;
		int topRightHeight= currentHeight / 2;
		int topRightWidth= currentWidth / 2;
		this.setTopRightChild(
			new QuadNode(
				raster,
				topRightLineOffset,
				topRightColumnOffset,
				topRightHeight,
				topRightWidth));

		/* Appel récursif (quart inférieur gauche) */
		int bottomLeftLineOffset= currentLineOffset + currentHeight / 2;
		int bottomLeftColumnOffset= currentColumnOffset;
		int bottomLeftHeight= currentHeight / 2;
		int bottomLeftWidth= currentWidth / 2;
		this.setBottomLeftChild(
			new QuadNode(
				raster,
				bottomLeftLineOffset,
				bottomLeftColumnOffset,
				bottomLeftHeight,
				bottomLeftWidth));

		/* Appel récursif (quart inférieur droit) */
		int bottomRightLineOffset= currentLineOffset + currentHeight / 2;
		int bottomRightColumnOffset= currentColumnOffset + currentWidth / 2;
		int bottomRightHeight= currentHeight / 2;
		int bottomRightWidth= currentWidth / 2;
		this.setBottomRightChild(
			new QuadNode(
				raster,
				bottomRightLineOffset,
				bottomRightColumnOffset,
				bottomRightHeight,
				bottomRightWidth));
	}

	public Raster toRaster(int height, int width, int values) {
		if (plain == true) {
			int[] array= new int[height * width];
			Arrays.fill(array, value);
			return new Raster(height, width, values, array);
		}
		Raster raster=
			new Raster(
				topLeftChild.toRaster(height / 2, width / 2, values),
				topRightChild.toRaster(height / 2, width / 2, values),
				bottomLeftChild.toRaster(height / 2, width / 2, values),
				bottomRightChild.toRaster(height / 2, width / 2, values));
		return raster;
	}

	public QuadNode getBottomLeftChild() {
		return bottomLeftChild;
	}

	public QuadNode getBottomRightChild() {
		return bottomRightChild;
	}

	public QuadNode getTopLeftChild() {
		return topLeftChild;
	}

	public QuadNode getTopRightChild() {
		return topRightChild;
	}

	public boolean isPlain() {
		return plain;
	}

	public int getValue() {
		return value;
	}

	public void setBottomLeftChild(QuadNode element) {
		bottomLeftChild= element;
	}

	public void setBottomRightChild(QuadNode element) {
		bottomRightChild= element;
	}

	public void setTopLeftChild(QuadNode element) {
		topLeftChild= element;
	}

	public void setTopRightChild(QuadNode element) {
		topRightChild= element;
	}

	public void setPlain(boolean b) {
		plain= b;
	}

	public void setValue(int b) {
		value= b;
	}
}
