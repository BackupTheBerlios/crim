/*
 * Created on 25 mars 2004
 */
package fr.umlv.quad;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cpele
 */
public class QuadValueReaderBin extends QuadValueReader {
	private InputStream in;

	public QuadValueReaderBin(InputStream in, int ucode) {
		super(ucode);
		this.in= in;
	}

	protected int nextValue() throws IOException {
		if (buffer.size() == 0) {
			int value= in.read();
			if (value==-1)
				return EOF;
			return value;
		}
		return ((Integer)buffer.remove(0)).intValue();
	}
}
