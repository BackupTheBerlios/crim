package fr.umlv.quad.huffman;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FrequencyTableLoader {
	private File fichier;
	private byte words[];

	public FrequencyTableLoader(File file) {
		fichier= file;
	}

	public void loadFrequencyTable(Node[] tabFreq) throws IOException {
		DataInputStream dis= new DataInputStream(new FileInputStream(fichier));
		int capacite= dis.available();
		while (capacite > 0) {
			words= new byte[capacite];
			dis.read(words);
			computeFreq(words, tabFreq);
			capacite= dis.available();
		}
	}

	private void computeFreq(byte[] tab, Node[] tabFreq) {
		for (int i= 0; i < tab.length; i++) {
			if (tab[i] < 0)
				tabFreq[tab[i] + 256].incFreq();
			else
				tabFreq[tab[i]].incFreq();
		}
	}
}