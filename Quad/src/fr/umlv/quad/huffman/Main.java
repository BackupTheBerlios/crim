package fr.umlv.quad.huffman;

import java.io.IOException;

public class Main {
	private static final double KO_SIZE= 1024.00;
	public static void main(String[] args)
		throws IOException, ClassNotFoundException {
		String inPath="/home/cpele/eclipse/Quad/images/Lena.512.ppm";
		String outPath= "/home/cpele/eclipse/Quad/out/Lena.512.ppm.hcf";
		
		System.out.println("Encodage...");
		Encoder encoder=new Encoder(inPath, outPath);
		encoder.encode();
		System.out.println("Encodage Ok");
		
		inPath="/home/cpele/eclipse/Quad/out/Lena.512.ppm.hcf";
		outPath="/home/cpele/eclipse/Quad/out2/Lena.512.ppm.hcf.ppm";
		
		System.out.println("Décodage...");
		Decoder decoder=new Decoder(inPath,outPath);
		decoder.decode();
		System.out.println("Décodage Ok");
	}

}