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
public class QuadImageElement {
	private byte value;
	private double variance;
	private boolean uni;
	private int id;
	private int width;
	private int height;

	static int sid= 0;

	public QuadImageElement() {
		id= sid++;
	}

	QuadImageElement topLeft;
	QuadImageElement topRight;
	QuadImageElement bottomLeft;
	QuadImageElement bottomRight;

	public Raster toRaster() {
		if (uni==true) {
			byte[] byteArray=new byte[height*width];
			Arrays.fill(byteArray,value);
			return new Raster(height,width,255,byteArray);
		}
		Raster raster=
			new Raster(
				topLeft.toRaster(),
				topRight.toRaster(),
				bottomLeft.toRaster(),
				bottomRight.toRaster());
		return raster;
	}
	public QuadImageElement getBottomLeft() {
		return bottomLeft;
	}

	public QuadImageElement getBottomRight() {
		return bottomRight;
	}

	public int getId() {
		return id;
	}

	public QuadImageElement getTopLeft() {
		return topLeft;
	}

	public QuadImageElement getTopRight() {
		return topRight;
	}

	public boolean isUni() {
		return uni;
	}

	public byte getValue() {
		return value;
	}

	public double getVariance() {
		return variance;
	}

	public void setBottomLeft(QuadImageElement element) {
		bottomLeft= element;
	}

	public void setBottomRight(QuadImageElement element) {
		bottomRight= element;
	}

	public void setTopLeft(QuadImageElement element) {
		topLeft= element;
	}

	public void setTopRight(QuadImageElement element) {
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
