/*
 * Created on 20 mars 2004
 */
package fr.umlv.quad;

import java.util.Arrays;

/**
 * @author cpele
 */
public class QuadImageElement {
	int value;
	double stddev;
	boolean plain;
	QuadImageElement topLeft;
	QuadImageElement topRight;
	QuadImageElement bottomLeft;
	QuadImageElement bottomRight;

	public Raster toRaster(int height, int width) {
		if (plain == true) {
			int[] array= new int[height * width];
			Arrays.fill(array, value);
			return new Raster(height, width, 255, array);
		}
		Raster raster=
			new Raster(
				topLeft.toRaster(height / 2, width / 2),
				topRight.toRaster(height / 2, width / 2),
				bottomLeft.toRaster(height / 2, width / 2),
				bottomRight.toRaster(height / 2, width / 2));
		return raster;
	}
}
