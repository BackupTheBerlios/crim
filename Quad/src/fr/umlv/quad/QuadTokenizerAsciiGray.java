/*
 * Created on 26 mars 2004
 */
package fr.umlv.quad;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;

/**
 * @author cpele
 */
public class QuadTokenizerAsciiGray extends QuadTokenizer {
	private StreamTokenizer tokenizer;

	public QuadTokenizerAsciiGray(InputStream in, int ucode) {
		super(ucode);
		this.tokenizer= new StreamTokenizer(in);
	}

	protected int nextValue() throws IOException {
		if (buffer.size() == 0) {
			int ttype= tokenizer.nextToken();
			if (ttype == tokenizer.TT_NUMBER) {
				return (int)tokenizer.nval;
			} if (ttype==tokenizer.TT_EOF) {
				return EOF;
			} else {
				throw new QuadError("Format de fichier incorrect");
			}
		} else {
			return ((Integer)buffer.remove(0)).intValue();
		}
	}
}
