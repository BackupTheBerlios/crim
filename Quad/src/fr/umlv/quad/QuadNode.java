/*
 * Créé le 5 févr. 2004
 */
package fr.umlv.quad;

import java.io.IOException;

/**
 * @author cpele
 * 
 * Elément d'une QuadImage (pixel de l'image ou noeud de l'arbre)
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
	 * Création récursive du quadtree à partir de la région d'un raster
	 * @param raster : Le raster
	 * @param currentLineOffset : Ligne où débute la région
	 * @param currentColumnOffset : Colonne où débute la région
	 * @param currentHeight : Hauteur de la région
	 * @param currentWidth : Largeur de la région
	 * @throws IOException
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
//		 if (raster
//			 .isPlainPixel(
//				 currentLineOffset,
//				 currentColumnOffset,
//				 currentHeight,
//				 currentWidth)) {
		if (raster
			.isPlainStddev(
				currentLineOffset,
				currentColumnOffset,
				currentHeight,
				currentWidth, 15)) {
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

		int childRasterHeight= currentHeight / 2;
		int childRasterWidth= currentWidth / 2;

		/* Appel récursif (quart supérieur gauche) */
		this.setTopLeftChild(
			new QuadNode(
				raster,
				currentLineOffset,
				currentColumnOffset,
				childRasterHeight,
				childRasterWidth));

		/* Appel récursif (quart supérieur droit) */
		this.setTopRightChild(
			new QuadNode(
				raster,
				currentLineOffset,
				currentColumnOffset + currentWidth / 2,
				childRasterHeight,
				childRasterWidth));

		/* Appel récursif (quart inférieur gauche) */
		this.setBottomLeftChild(
			new QuadNode(
				raster,
				currentLineOffset + currentHeight / 2,
				currentColumnOffset,
				childRasterHeight,
				childRasterWidth));

		/* Appel récursif (quart inférieur droit) */
		this.setBottomRightChild(
			new QuadNode(
				raster,
				currentLineOffset + currentHeight / 2,
				currentColumnOffset + currentWidth / 2,
				childRasterHeight,
				childRasterWidth));
	}

	/**
	 * Création du raster correspondant au quadtree dont la racine est le noeud
	 * courant (this)
	 */
	public Raster toRaster(int height, int width, int values) {
		if (plain == true) {
			return new Raster(height, width, values, this.value);
		}
		int subRasterHeight= height / 2;
		int subRasterWidth= width / 2;
		return new Raster(
			topLeftChild.toRaster(subRasterHeight, subRasterWidth, values),
			topRightChild.toRaster(subRasterHeight, subRasterWidth, values),
			bottomLeftChild.toRaster(subRasterHeight, subRasterWidth, values),
			bottomRightChild.toRaster(subRasterHeight, subRasterWidth, values));
	}

	/*-- Getters & setters -----------------------------------------*/

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
