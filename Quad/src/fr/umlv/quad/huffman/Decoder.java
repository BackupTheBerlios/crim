package fr.umlv.quad.huffman;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Decoder {
	public Decoder(String inPath, String outPath)
		throws FileNotFoundException, IOException {
		bytesWritten= 0;
		ois= new ObjectInputStream(new FileInputStream(inPath));
		dos= new DataOutputStream(new FileOutputStream(outPath));
	}

	private ObjectInputStream ois;
	private DataOutputStream dos;
	private FileHeader fileHeader;
	private Node huffTree;
	private long bytesWritten;
	private Node currentByteValue;

	private void readHeader() throws IOException, ClassNotFoundException {
		fileHeader= (FileHeader)ois.readObject();
		huffTree= fileHeader.getHuffmanTree();
		currentByteValue= huffTree;
	}

	private void decode(byte[] data) throws IOException {
		byte toWrite;
		int posi;

		for (int i= 0; i < data.length; i++) {
			byte leByte= data[i];
			for (int j= 7; j >= 0; j--) {
				if (((leByte >>> j) & 0x1) == 0)
					currentByteValue= currentByteValue.getLeftChild();
				else
					currentByteValue= currentByteValue.getRightChild();

				if (currentByteValue.isLeaf())
					{
					posi= currentByteValue.getPosition();
					
					toWrite= (byte) (posi >= 128 ? posi - 256 : posi);
					dos.writeByte(toWrite); // On l'ecrit
					bytesWritten++;

					if (bytesWritten == fileHeader.getNumBytes())
						return;
					currentByteValue= huffTree;
				}
			}
		}
	}

	public void decode() throws IOException, ClassNotFoundException {
		byte[] data;

		readHeader();

		int numBytesLeft= ois.available();

		while (numBytesLeft > 0) {
			data= new byte[numBytesLeft];
			ois.read(data);
			decode(data);
			numBytesLeft= ois.available();
		}
		ois.close();
		dos.flush();
		dos.close();
	}

}