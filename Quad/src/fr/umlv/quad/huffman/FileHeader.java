package fr.umlv.quad.huffman;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class FileHeader implements Serializable {
	private String originalFileName;
	private long numBytes;
	private Node huffmanTree;

	public FileHeader(String name, long nbBytes, Node huffTree) {
		originalFileName= name;
		numBytes= nbBytes;
		this.huffmanTree= huffTree;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}
	public long getNumBytes() {
		return numBytes;
	}
	public Node getHuffmanTree() {
		return huffmanTree;
	}

	public long headerLength() throws FileNotFoundException, IOException {
		long size= -1;

		File temp= File.createTempFile("header", ".tmp");
		ObjectOutputStream oos=
			new ObjectOutputStream(new FileOutputStream(temp));
		oos.writeObject(this);
		oos.flush();
		oos.close();
		size= temp.length();
		temp.delete();
		return size;
	}
}