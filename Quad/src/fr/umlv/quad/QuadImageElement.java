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
 * El?ment d'une ImageQuad (pixel de l'image ou noeud de l'arbre)
 */
public class QuadImageElement {
	private int value;
	private boolean plain;

	private QuadImageElement topLeft;
	private QuadImageElement topRight;
	private QuadImageElement bottomLeft;
	private QuadImageElement bottomRight;
	
	public QuadImageElement() {}

	public Raster toRaster(int height, int width, int values) {
		if (plain == true) {
			int[] array= new int[height * width];
			Arrays.fill(array, value);
			return new Raster(height, width, values, array);
		}
		Raster raster=
			new Raster(
				topLeft.toRaster(height / 2, width / 2, values),
				topRight.toRaster(height / 2, width / 2, values),
				bottomLeft.toRaster(height / 2, width / 2, values),
				bottomRight.toRaster(height / 2, width / 2, values));
		return raster;
	}

	public QuadImageElement getBottomLeft() {
		return bottomLeft;
	}

	public QuadImageElement getBottomRight() {
		return bottomRight;
	}

	public QuadImageElement getTopLeft() {
		return topLeft;
	}

	public QuadImageElement getTopRight() {
		return topRight;
	}

	public boolean isPlain() {
		return plain;
	}

	public int getValue() {
		return value;
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

	public void setPlain(boolean b) {
		plain= b;
	}

	public void setValue(int b) {
		value= b;
	}
}
