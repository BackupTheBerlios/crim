/*
 * Created on 16 mars 2004
 */
package fr.umlv.quad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.StreamTokenizer;

/**
 * @author cpele
 */
public class Util {
	private static double log4= Math.log(4.);
	private static double log2= Math.log(2.);

	public static int min(int b1, int b2) {
		return (b1 < b2) ? b1 : b2;
	}

	public static int max(int b1, int b2) {
		return (b1 > b2) ? b1 : b2;
	}

	public static void makeDir(String path) {
		File filePath= new File(path);
		File fileDir= filePath.getParentFile();
		fileDir.mkdirs();
	}

	public static double log4(double i) {
		return (Math.log(i) / log4);
	}

	public static double log2(double i) {
		return (Math.log(i) / log2);
	}

	public static void rename(String oldPath, String newDirPath) {
		File oldFile= new File(oldPath);
		File newFileBase= new File(oldFile.getName());

		File newFile= new File(newDirPath + '/' + newFileBase);
		new File(newDirPath).mkdirs();

		oldFile.renameTo(newFile);
	}

	public static void printBin(PrintStream out, int value) {
		out.print("(lsb ");
		for (int i= 0; i < 8; i++) {
			out.print((value >> i) % 2);
		}
		out.print(" msb)");
	}

}
