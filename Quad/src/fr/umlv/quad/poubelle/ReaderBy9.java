/*
 * Created on 4 avr. 2004
 */
package fr.umlv.quad.poubelle;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import fr.umlv.quad.QuadError;

/**
 * @author cpele
 */
public class ReaderBy9 {
	private InputStream in;
	
	public ReaderBy9(InputStream in) {
		this.in=in;
		buffer=new int[8];
		bufoff=8;
	}

	/* Lecture de 8 valeurs codées sur 72 bits (9 bits par valeur) */
	public int read(int[] array) throws IOException {
		if (array.length != 8) {
			throw new QuadError("Les valeurs doivent être lues par groupes de 8");
		}

		int[] bytes= new int[9];

		int signByte= in.read();
		if (signByte==-1)
			return 0;
		
		int i=0;
		for (i= 0; i < 8; i++) {
			bytes[i]= in.read();
			if (bytes[i] == -1) {
				break;
			}
		}
		int numRead=i;

		for (i= 0; i < numRead; i++) {
			int value= bytes[i];
			if (0 != ((1 << i) & signByte)) {
				value= -value;
			}
			array[i]= value;
		}
		return numRead;
	}
	
	private int[] buffer;
	private int bufoff;

	public int read() throws IOException {
		if (bufoff == 8) {
			int n=read(buffer);
			if (n==0)
				return -257;
			bufoff= 8-n;
		}
		return buffer[bufoff++];
	}

	public static void main(String[] args) throws IOException {
		InputStream in= new FileInputStream("/home/cpele/tmp/out9");
		ReaderBy9 r=new ReaderBy9(in);
		
		while (true) {
			int v=r.read();
			if (v==-257)
				break;
			System.out.print(v + " ");
		}
		System.out.println();
	}

}
