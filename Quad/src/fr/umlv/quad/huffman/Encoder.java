package fr.umlv.quad.huffman;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Encoder {
	private String[] tabCorresp;
	private DataInputStream dataInput;

	private static Node[] loadFreqencyTable(File inputFile)
		throws IOException {
		Node[] tabFreq= new Node[256];

		FrequencyTableLoader freqReader= new FrequencyTableLoader(inputFile);
		for (int i= 0; i < tabFreq.length; i++) {
			tabFreq[i]= new Node(i);
		}
		freqReader.loadFrequencyTable(tabFreq);
		return tabFreq;
	}

	private static Node buildHuffmanTree(Node[] tabFreq) {
		Heap priorityQ= new Heap(256);
		for (int i= 0; i < tabFreq.length; i++) {
			if (tabFreq[i].getFrequency() > 0)
				priorityQ.append(tabFreq[i]);
		}
		priorityQ.sort();

		while (priorityQ.getCurrentSize() >= 2) {
			Node temp= new Node();
			temp.setLeftChild(priorityQ.remove());
			temp.setRightChild(priorityQ.remove());
			temp.setFreq(
				temp.getRightChild().getFrequency()
					+ temp.getLeftChild().getFrequency());
			priorityQ.insertOrdered(temp);
		}

		Node huffmanTree= priorityQ.remove();

		priorityQ= null;
		return huffmanTree;
	}

	private static String[] getCorresp(Node huffmanTree) {
		TreeOps treeOps= new TreeOps();

		treeOps.loadLeavesCodes(huffmanTree);
		String[] tabCorresp= treeOps.getCorrespTable();

		treeOps= null;
		return tabCorresp;
	}

	public Encoder(String inPath, String outPath) throws IOException {
		File inputFile= new File(inPath);

		Node[] tabFreq= loadFreqencyTable(inputFile);
		Node huffmanTree= buildHuffmanTree(tabFreq);
		tabCorresp= getCorresp(huffmanTree);

		ObjectOutputStream objOut=
			new ObjectOutputStream(new FileOutputStream(outPath));
		FileHeader outputFileHeader=
			new FileHeader(
				inputFile.getName(),
				inputFile.length(),
				huffmanTree);
		dataInput= new DataInputStream(new FileInputStream(inputFile));

		buffer= "";
		oos= objOut;
		fileHeader= outputFileHeader;
	}

	public void encode() throws IOException {
		encodeFile(dataInput, tabCorresp);
	}

	private String buffer;
	private ObjectOutputStream oos;
	private FileHeader fileHeader;
	private long bitWritten;

	private void writeHeader() throws IOException {
		oos.writeObject(fileHeader);
		oos.flush();
	}

	private void writeEncoded(String code) throws IOException {
		byte byteToWrite= 0;

		buffer += code;

		while (buffer.length() >= 8) {
			for (int i= 7; i >= 0; i--) {
				byteToWrite <<= 1;
				if (buffer.charAt(0) == '1')
					byteToWrite++;
				buffer= buffer.substring(1);
			}

			oos.writeByte(byteToWrite);
			byteToWrite= 0;

		}
	}

	private void writeEOF() throws IOException {
		byte finalByte= 0;

		for (int i= 7; i >= 0; i--) {
			finalByte <<= 1;

			if (buffer.length() > 0)
				{
				if (buffer.charAt(0) == '1')
					finalByte++;
				buffer= buffer.substring(1);
			}
		}

		oos.writeByte(finalByte);
		oos.flush();
		oos.close();

	}

	public void encodeFile(DataInputStream dataInput, String[] corresp)
		throws IOException {
		byte[] data;

		writeHeader();

		int numBytesLeft= dataInput.available();
		while (numBytesLeft > 0) {
			data= new byte[numBytesLeft];
			dataInput.read(data);

			for (int i= 0; i < data.length; i++)
				writeEncoded(convert(data[i], corresp));

			numBytesLeft= dataInput.available();
		}

		writeEOF();
		dataInput.close();
	}

	private String convert(byte info, String[] corresp) {
		int posi= (info < 0 ? info + 256 : info);
		return corresp[posi];
	}
}