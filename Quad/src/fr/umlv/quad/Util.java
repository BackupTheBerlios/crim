/*
 * Created on 16 mars 2004
 */
package fr.umlv.quad;

import java.io.File;

/**
 * @author cpele
 */
public class Util {
	public static int min(int b1, int b2) {
		return (b1 < b2) ? b1 : b2;
	}

	public static int max(int b1, int b2) {
		return (b1 > b2) ? b1 : b2;
	}
	
	public static void makeDir(String path) {
		File filePath= new File(path);
		File fileDir= filePath.getParentFile();
		boolean ok= fileDir.mkdirs();
	}

	public static double log2(int i) {
		return (Math.log(i)/Math.log(2));
	}
}
