/*
 * Created on 16 mars 2004
 */
package fr.umlv.quad;

import java.io.File;

/**
 * @author cpele
 */
public class Util {
	private static double log4=Math.log(4.);
	private static double log2=Math.log(2.);
	
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

	public static double log4(double i) {
		return (Math.log(i)/log4);
	}

	public static double log2(double i) {
		return (Math.log(i)/log2);
	}
}
