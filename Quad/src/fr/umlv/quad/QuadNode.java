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
	private boolean plain;
	private int value;
	private int level;

	private QuadNode topLeftChild;
	private QuadNode topRightChild;
	private QuadNode bottomLeftChild;
	private QuadNode bottomRightChild;

	public QuadNode(int level) {
		this.level= level;
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
		int currentWidth,
		int level)
		throws IOException
	{
		this(level);

		/* Condition d'arrêt de la récursion : la région du raster considérée 
		 * est homogène */
		if (raster
			.isPlainDev(
				currentLineOffset,
				currentColumnOffset,
				currentHeight,
				currentWidth, 15)) {
			plain= true;
			value=
				(int)raster.mean(
					currentLineOffset,
					currentColumnOffset,
					currentHeight,
					currentWidth);
			return;
		}

		/*-- Appels récursifs ------------------------------------------*/

		int childRasterHeight= currentHeight / 2;
		int childRasterWidth= currentWidth / 2;

		/* Appel récursif (quart supérieur gauche) */
		topLeftChild=
			new QuadNode(
				raster,
				currentLineOffset,
				currentColumnOffset,
				childRasterHeight,
				childRasterWidth,
				level + 1);

		/* Appel récursif (quart supérieur droit) */
		topRightChild=
			new QuadNode(
				raster,
				currentLineOffset,
				currentColumnOffset + currentWidth / 2,
				childRasterHeight,
				childRasterWidth,
				level + 1);

		/* Appel récursif (quart inférieur gauche) */
		bottomLeftChild=
			new QuadNode(
				raster,
				currentLineOffset + currentHeight / 2,
				currentColumnOffset,
				childRasterHeight,
				childRasterWidth,
				level + 1);

		/* Appel récursif (quart inférieur droit) */
		bottomRightChild=
			new QuadNode(
				raster,
				currentLineOffset + currentHeight / 2,
				currentColumnOffset + currentWidth / 2,
				childRasterHeight,
				childRasterWidth,
				level + 1);

		value= topLeftChild.value;
		plain= false;
	}

	/**
	 * Création du raster correspondant au quadtree dont la racine est le noeud
	 * courant (this)
	 */
	public Raster toRaster(int height, int width, int values) {
		if (plain)
			return new Raster(height, width, values, value);

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

	public void setValue(int b) {
		value= b;
	}

	public void setPlain(boolean plain) {
		this.plain= plain;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int i) {
		level= i;
	}
}
