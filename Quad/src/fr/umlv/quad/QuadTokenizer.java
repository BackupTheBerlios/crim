/*
 * Created on 25 mars 2004
 */
package fr.umlv.quad;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author cpele
 */
public class QuadTokenizer {
	private boolean plain;
	private int value;
	private int ucode;
	private InputStream in;

	public QuadTokenizer(InputStream in, int ucode) {
		this.in= in;
		this.ucode= ucode;
	}

	/*--------------------------------------------------------------------*/

	public void next(
		boolean currentIsFirstChild,
		boolean currentIsLastChild)
		throws IOException {
		/* Cas où le noeud courant est un premier fils */
		if (currentIsFirstChild) {
			in.mark(2);
			int nextValue1= in.read();
			int nextValue2= in.read();

			if (ucode == nextValue1 && ucode == nextValue2) {
				plain= true;
				return;
			}

			in.reset();
			plain= false;
			return;
		}

		/* Cas où le noeud courant est un dernier fils */
		else if (currentIsLastChild) {
			value= in.read();
			in.mark(3);

			int nextValue1= in.read();
			int nextValue2= in.read();
			int nextValue3= in.read();

			/* 
			 * Cas où le noeud n'est pas homogène :
			 * .V X X
			 * .U U V
			 * Cas où le noeud est homogène : 
			 *  U.V X
			 *  U.U U
			 */

			/* Cas .V X X */
			if (nextValue1 != ucode) {
				in.reset();
				plain= false;
				return;
			}

			/* Cas .U U V */
			if (ucode == nextValue1
				&& ucode == nextValue2
				&& ucode != nextValue3) {
				in.reset();
				plain= false;
				return;
			}

			/* Cas U.V X */
			if (ucode == nextValue1 && ucode != nextValue2) {
				in.reset();
				in.read();
				plain= true;
				return;
			}

			/* Cas U.U U */
			if (ucode == nextValue1
				&& ucode == nextValue2
				&& ucode == nextValue3) {
				in.reset();
				in.read();
				plain= false;
				return;
			}

			throw new QuadError("On ne devrait pas arriver ici, c'est un bug !");
		}

		/* Cas général */
		else {
			/* Lecture de la valeur du noeud courant */
			value= in.read();
			in.mark(1);

			/* Lecture de la prochaine valeur */
			int nextValue= in.read();

			/* Si la prochaine valeur vaut ucode, alors ce noeud est uniforme */
			if (nextValue == ucode) {
				plain=true;
				return;
			}
			/* Sinon c'est la valeur du prochain noeud, on repositionne le flux
			 * pour que le prochain appel à get() retourne cette valeur
			 */
			else {
				in.reset();
				plain=false;
				return;
			}
		}
	}

	/*-- Getters & Setters -----------------------------------------------*/

	public boolean plain() {
		return plain;
	}

	public int value() {
		return value;
	}
}
