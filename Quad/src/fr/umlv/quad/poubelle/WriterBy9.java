/*
 * Created on 4 avr. 2004
 */
package fr.umlv.quad.poubelle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import fr.umlv.quad.QuadError;

/**
 * @author cpele
 */
public class WriterBy9 {
	private OutputStream out;

	public WriterBy9(OutputStream out) {
		this.out= out;
		buffer= new int[8];
		buflen= 0;
	}

	/* Ecriture de 8 valeurs codées sur 72 bits (9 bits par valeur) */
	public void write(int[] array, int num) throws IOException {
		if (array.length != 8) {
			throw new QuadError("Les valeurs doivent être écrites par groupes de 8");
		}

		int signByte= 0; // Champ de bits (0: positif, 1: négatif)

		int[] bytes= new int[8];
		for (int i= 0; i < 8 && i < num; i++) {
			int value= array[i];
			if (value < -256 || value > 255) {
				throw new QuadError("Les valeurs doivent tenir sur 9 bits");
			}

			if (value < 0) {
				value= -value;
				signByte += (1 << i);
			}
			bytes[i]= value;
		}
		out.write(signByte);

		for (int i= 0; i < 8 && i < num; i++) {
			out.write(bytes[i]);
		}
	}

	private int[] buffer;
	private int buflen;

	public void write(int value) throws IOException {
		buffer[buflen++]= value;

		if (buflen == 8) {
			write(buffer, buflen);
			buflen= 0;
		}
	}

	private void flush() throws IOException {
		write(buffer,buflen);
	}

	public static void main(String[] args) throws IOException {
		int[] array= new int[99];
		for (int i= 0; i < array.length; i++) {
			array[i]= (int)Math.pow(-1, i) * i;
		}

		OutputStream out= new FileOutputStream("/home/cpele/tmp/out9");
		WriterBy9 w= new WriterBy9(out);
		for (int i= 0; i < array.length; i++) {
			w.write(array[i]);
		}
		w.flush();
	}

}
