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
	public static final short ROOT= -1;
	public static final short TOPLEFT= 0;
	public static final short TOPRIGHT= 1;
	public static final short BOTTOMLEFT= 2;
	public static final short BOTTOMRIGHT= 3;

	private boolean plain;
	private int value;
	private short level;
	private double stddev;
	private short location;

	private QuadNode topLeftChild;
	private QuadNode topRightChild;
	private QuadNode bottomLeftChild;
	private QuadNode bottomRightChild;

	public QuadNode(int level, short location) {
		this.level= (short)level;
		this.location= location;
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
		RasterBand raster,
		int currentLineOffset,
		int currentColumnOffset,
		int currentHeight,
		int currentWidth,
		int level,
		short location)
		throws IOException {
		this(level, location);

		/* Condition d'arrêt de la récursion : la taille du raster est 
		 * inférieure à 1x1 */
		if (currentHeight * currentWidth <= 1) {
			plain= true;
			value= raster.pixel(currentLineOffset, currentColumnOffset);
			stddev= 0;
			return;
		}

		/*-- Appels récursifs ------------------------------------------*/

		int childRasterHeight= currentHeight / 2;
		int childRasterWidth= currentWidth / 2;
		int bottomOffset= currentLineOffset + currentHeight / 2;
		int rightOffset= currentColumnOffset + currentWidth / 2;

		short nextLevel= (short) (level + 1);

		/* Appel récursif (quart supérieur gauche) */
		topLeftChild=
			new QuadNode(
				raster,
				currentLineOffset,
				currentColumnOffset,
				childRasterHeight,
				childRasterWidth,
				nextLevel,
				TOPLEFT);

		/* Appel récursif (quart supérieur droit) */
		topRightChild=
			new QuadNode(
				raster,
				currentLineOffset,
				rightOffset,
				childRasterHeight,
				childRasterWidth,
				nextLevel,
				TOPRIGHT);

		/* Appel récursif (quart inférieur gauche) */
		bottomLeftChild=
			new QuadNode(
				raster,
				bottomOffset,
				currentColumnOffset,
				childRasterHeight,
				childRasterWidth,
				nextLevel,
				BOTTOMLEFT);

		/* Appel récursif (quart inférieur droit) */
		bottomRightChild=
			new QuadNode(
				raster,
				bottomOffset,
				rightOffset,
				childRasterHeight,
				childRasterWidth,
				nextLevel,
				BOTTOMRIGHT);

		/* Le noeud correspond à une zone uniforme si ses quatre fils sont
		 * uniformes et si leurs valeurs sont égales
		 */
		value= topLeftChild.value;
		if (topLeftChild.plain
			&& topRightChild.plain
			&& bottomLeftChild.plain
			&& bottomRightChild.plain
			&& topLeftChild.value == topRightChild.value
			&& topLeftChild.value == bottomLeftChild.value
			&& topLeftChild.value == bottomRightChild.value) {
			plain= true;
		}

		/*--------------------------------------------------------------*/
		/*-- Calcul de la variance (stddev=starndard deviation) --------*/
		/*--------------------------------------------------------------*/

		/* Calcul de la moyenne des fils */
		double m=
			(topLeftChild.value
				+ topRightChild.value
				+ bottomLeftChild.value
				+ bottomRightChild.value)
				/ 4.;

		/* Calcul des différences */
		double topLeftChildValue_m= topLeftChild.value - m;
		double bottomLeftChildValue_m= bottomLeftChild.value - m;
		double topRightChildValue_m= topRightChild.value - m;
		double bottomRightChildValue_m= bottomRightChild.value - m;

		/* Calcul du premier terme */
		double stddev1=
			(topLeftChildValue_m * topLeftChildValue_m
				+ bottomLeftChildValue_m * bottomLeftChildValue_m
				+ topRightChildValue_m * topRightChildValue_m
				+ bottomRightChildValue_m * bottomRightChildValue_m)
				/ 4.;

		/* Calcul du deuxième terme */
		double stddev2=
			(topLeftChild.stddev
				+ topRightChild.stddev
				+ bottomLeftChild.stddev
				+ bottomRightChild.stddev)
				/ 16.;

		/* Somme des termes pour obtenir la variance */
		stddev= stddev1 + stddev2;
	}

	/**
	 * Création du raster correspondant au quadtree dont la racine est le noeud
	 * courant (this)
	 */
	public RasterBand toRasterBand(int height, int width, int values) {
		if (plain)
			return new RasterBand(height, width, values, value);

		int subRasterHeight= height / 2;
		int subRasterWidth= width / 2;
		return new RasterBand(
			topLeftChild.toRasterBand(subRasterHeight, subRasterWidth, values),
			topRightChild.toRasterBand(subRasterHeight, subRasterWidth, values),
			bottomLeftChild.toRasterBand(subRasterHeight, subRasterWidth, values),
			bottomRightChild.toRasterBand(subRasterHeight, subRasterWidth, values));
	}

	public void compress(double currentDev, double factor) {
		if (stddev < currentDev) {
			plain= true;
			return;
		}
		double nextDev= currentDev * factor;
		topLeftChild.compress(nextDev, factor);
		topRightChild.compress(nextDev, factor);
		bottomLeftChild.compress(nextDev, factor);
		bottomRightChild.compress(nextDev, factor);
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

	public boolean isNotPlain() {
		return !plain;
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
		level= (short)i;
	}

	public boolean isPlain(double stddev) {
		if (stddev == 0)
			return plain;
		if (this.stddev < stddev)
			return true;
		return false;
	}
	public short getLocation() {
		return location;
	}

	public void setLocation(short s) {
		location= s;
	}

}
