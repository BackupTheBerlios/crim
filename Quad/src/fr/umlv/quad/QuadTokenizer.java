/*
 * Created on 26 mars 2004
 */
package fr.umlv.quad;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author cpele
 */
public abstract class QuadTokenizer {
	public static final int CT_NORMAL= 0;
	public static final int CT_FIRST= 1;
	public static final int CT_LAST= 2;

	private boolean plain;
	private int value;
	private int ucode;
	protected ArrayList buffer;

	public QuadTokenizer(int ucode) {
		this.ucode= ucode;
		buffer= new ArrayList();
	}

	public void next(int childType) throws IOException {
		/* Cas où le noeud courant est un premier fils */
		if (childType == CT_FIRST) {
			int nextValue1= nextValue();
			int nextValue2= nextValue();

			if (ucode == nextValue1 && ucode == nextValue2) {
				plain= true;
				return;
			} else {
				pushBack(nextValue1);
				pushBack(nextValue2);
				plain= false;
				return;
			}
		}

		/* Cas où le noeud courant est un dernier fils */
		else if (childType == CT_LAST) {
			value= nextValue();

			int nextValue1= nextValue();

			/* Cas .V X X */
			if (nextValue1 != ucode) {
				pushBack(nextValue1);
				plain= false;
				return;
			}

			int nextValue2= nextValue();
			int nextValue3= nextValue();
			
			/* 
			 * Cas où le noeud n'est pas homogène :
			 * .V X X
			 * .U U V
			 * Cas où le noeud est homogène : 
			 *  U.V X
			 *  U.U U
			 */

			/* Cas .U U V */
			if (ucode == nextValue1
				&& ucode == nextValue2
				&& ucode != nextValue3) {
				pushBack(nextValue1);
				pushBack(nextValue2);
				pushBack(nextValue3);
				plain= false;
				return;
			}

			/* Cas U.V X */
			if (ucode == nextValue1 && ucode != nextValue2) {
				pushBack(nextValue2);
				pushBack(nextValue3);
				plain= true;
				return;
			}

			/* Cas U.U U */
			if (ucode == nextValue1
				&& ucode == nextValue2
				&& ucode == nextValue3) {
				pushBack(nextValue2);
				pushBack(nextValue3);
				plain= true;
				return;
			}

			throw new QuadError("On ne devrait pas arriver ici, c'est un bug !");
		}

		/* Cas général */
		else {
			/* Lecture de la valeur du noeud courant */
			value= nextValue();

			/* Lecture de la prochaine valeur */
			int nextValue= nextValue();

			/* Si la prochaine valeur vaut ucode, alors ce noeud est uniforme */
			if (nextValue == ucode) {
				plain= true;
				return;
			}
			/* Sinon c'est la valeur du prochain noeud, on repositionne le flux
			 * pour que le prochain appel à get() retourne cette valeur
			 */
			else {
				pushBack(nextValue);
				plain= false;
				return;
			}
		}
	}

	private void pushBack(int value) {
		buffer.add(new Integer(value));
	}

	/**
	 * Doit lire la prochaine valeur dans le flux.
	 * @return : La valeur lue, ou -1 si la fin du fichier est rencontrée
	 * @throws IOException
	 */
	protected abstract int nextValue() throws IOException;

	/*-- Getters & Setters -----------------------------------------------*/

	public boolean plain() {
		return plain;
	}

	public int value() {
		return value;
	}
}