/*
 * Created on 16 mars 2004
 */
package fr.umlv.quad;

import java.io.File;

/**
 * @author cpele
 */
public class Util {
	public static void makeDir(String path) {
		File filePath= new File(path);
		File fileDir= filePath.getParentFile();
		boolean ok= fileDir.mkdirs();
	}
	
	public static int btoi(byte b) {return 256+b;}
}
