/*
 * Cr?? le 5 f?vr. 2004
 */
package fr.umlv.quad;

import java.util.Arrays;

/**
 * @author cpele
 * 
 * El?ment d'une ImageQuad (pixel de l'image ou noeud de l'arbre)
 */
public class QuadImageElementDescending {
	private int value;
	private double variance;
	private boolean uni;
	private int id;
	private int width;
	private int height;

	static int sid= 0;

	public QuadImageElementDescending() {
		id= sid++;
	}

	QuadImageElementDescending topLeft;
	QuadImageElementDescending topRight;
	QuadImageElementDescending bottomLeft;
	QuadImageElementDescending bottomRight;

	public Raster toRaster() {
		if (uni==true) {
			int[] array=new int[height*width];
			Arrays.fill(array,value);
			return new Raster(height,width,255,array);
		}
		Raster raster=
			new Raster(
				topLeft.toRaster(),
				topRight.toRaster(),
				bottomLeft.toRaster(),
				bottomRight.toRaster());
		return raster;
	}
	public QuadImageElementDescending getBottomLeft() {
		return bottomLeft;
	}

	public QuadImageElementDescending getBottomRight() {
		return bottomRight;
	}

	public int getId() {
		return id;
	}

	public QuadImageElementDescending getTopLeft() {
		return topLeft;
	}

	public QuadImageElementDescending getTopRight() {
		return topRight;
	}

	public boolean isUni() {
		return uni;
	}

	public int getValue() {
		return value;
	}

	public double getVariance() {
		return variance;
	}

	public void setBottomLeft(QuadImageElementDescending element) {
		bottomLeft= element;
	}

	public void setBottomRight(QuadImageElementDescending element) {
		bottomRight= element;
	}

	public void setTopLeft(QuadImageElementDescending element) {
		topLeft= element;
	}

	public void setTopRight(QuadImageElementDescending element) {
		topRight= element;
	}

	public void setUni(boolean b) {
		uni= b;
	}

	public void setValue(byte b) {
		value= b;
	}

	public void setVariance(double d) {
		variance= d;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int i) {
		height= i;
	}

	public void setWidth(int i) {
		width= i;
	}
}
