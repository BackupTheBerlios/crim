/*
 * Created on 16 mars 2004
 */
package fr.umlv.quad;

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;

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

	/**
	 * Lecture de la prochaine valeur depuis un fichier
	 * @param tokenizer : Le tokenizer correspondant au fichier
	 * @return : La valeur lue ou bien -1 en cas de fin de fichier
	 * @throws IOException
	 */
	public static int nextValue(StreamTokenizer tokenizer)
		throws IOException {
		tokenizer.nextToken();
		if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
			return (int)tokenizer.nval;
		} else if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			return -1;
		} else
			throw new QuadError("Format du fichier incorrect");
	}
}
