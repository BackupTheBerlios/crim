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
	public static final int INTERNAL_NORMAL_CHILD_NODE= 0;
	public static final int INTERNAL_FIRST_CHILD_NODE= 1;
	public static final int INTERNAL_LAST_CHILD_NODE= 2;
	public static final int PIXEL_NORMAL_CHILD_NODE= 3;
	public static final int PIXEL_FIRST_CHILD_NODE= 4;

	protected static final int EOF= -1;

	private int ucode;
	protected ArrayList buffer;

	public QuadTokenizer(int ucode) {
		this.ucode= ucode;
		buffer= new ArrayList();
	}

	/**
	 * Récupération des paramètres du prochain noeud codé dans le fichier
	 * (valeur et homogénéité).
	 * La façon le décoder ces paramètres dépend du type de noeud (premier fils,
	 * noeud du dernier niveau...)
	 * @param nodeType : Le type du noeud
	 * @return : Une structure indiquant l'homogénéité et la valeur du noeud
	 * @throws IOException
	 */
	public QuadValue next(int nodeType) throws IOException {
		int value;

		/* Cas où le noeud courant fait partie du dernier niveau 
		 * On ne cherche pas à vérifier le marqueur ucode 
		 */
		if (nodeType == PIXEL_NORMAL_CHILD_NODE) {
			value= nextValue();
			if (value == EOF)
				throw new QuadError("Format de fichier invalide");
			return new QuadValue(value, true);
		}

		/* Cas où le noeud courant fait partie du dernier niveau et est un
		 * premier fils
		 * On ne vérifie pas le marqueur ucode
		 * La valeur du noeud n'est pas dans le fichier
		 */
		else if (nodeType == PIXEL_FIRST_CHILD_NODE) {
			return new QuadValue(QuadValue.NOVALUE, true);
		}

		/* Cas où le noeud courant est un premier fils 
		 * La valeur du noeud n'est pas dans le fichier
		 */
		else if (nodeType == INTERNAL_FIRST_CHILD_NODE) {
			int nextValue1= nextValue();
			int nextValue2= nextValue();

			if (nextValue1 == EOF || nextValue2 == EOF) {
				return new QuadValue(QuadValue.NOVALUE, false);
			} else if (ucode == nextValue1 && ucode == nextValue2) {
				return new QuadValue(QuadValue.NOVALUE, true);
			} else {
				pushBack(nextValue1);
				pushBack(nextValue2);
				return new QuadValue(QuadValue.NOVALUE, false);
			}
		}

		/* Cas où le noeud courant est un dernier fils
		 * Dans ce cas, le noeud est uniforme si la valeur est suivie d'un ou
		 * trois marqueurs ucode.  Si la valeur est suivie de deux marqueurs,
		 * c'est que le noeud suivant est un premier fils uniforme.
		 */
		else if (nodeType == INTERNAL_LAST_CHILD_NODE) {
			value= nextValue();
			if (value == EOF)
				throw new QuadError("Type du fichier invalide");

			int nextValue1= nextValue();

			if (nextValue1 == EOF) {
				return new QuadValue(value, false);
			}

			/* 
			 * U: marqueur ucode
			 * V: valeur
			 * X: un des deux
			 * 
			 * Cas où le noeud n'est pas homogène :
			 * .V X X
			 * .U U V
			 * Cas où le noeud est homogène : 
			 *  U.V X
			 *  U.U U
			 */

			/* Si la première valeur suivante n'est pas ucode, alors le noeud 
			 * n'est pas uniforme
			 * Cas .V X X
			 */
			if (nextValue1 != ucode) {
				pushBack(nextValue1);
				return new QuadValue(value, false);
			}

			int nextValue2= nextValue();

			/* Si la première valeur suivante seule est ucode, et que la fin
			 * du fichier arrive ensuite, alors le noeud est uniforme
			 * Cas .U V
			 */
			if (nextValue1 == ucode && nextValue2 == EOF) {
				return new QuadValue(value, true);
			}

			/* Si la première valeur suivante est ucode et que la deuxième
			 * suivante ne l'est pas, alors le noeud est uniforme
			 * Cas U.V X 
			 */
			if (ucode == nextValue1 && ucode != nextValue2) {
				pushBack(nextValue2);
				return new QuadValue(value, true);
			}

			int nextValue3= nextValue();

			/* Cas .U U V */
			if (ucode == nextValue1
				&& ucode == nextValue2
				&& ucode != nextValue3) {
				pushBack(nextValue1);
				pushBack(nextValue2);
				pushBack(nextValue3);
				return new QuadValue(value, false);
			}

			/* Cas U.U U */
			if (ucode == nextValue1
				&& ucode == nextValue2
				&& ucode == nextValue3) {
				pushBack(nextValue2);
				pushBack(nextValue3);
				return new QuadValue(value, true);
			}
			
			/* Normalement ici, tous les cas ont été examinés
			 */
			throw new QuadError("On ne devrait pas arriver ici, c'est un bug !");
		}

		/* Cas général */
		else {
			/* Lecture de la valeur du noeud courant */
			value= nextValue();
			if (value == EOF)
				throw new QuadError("Type du fichier invalide");

			/* Lecture de la prochaine valeur */
			int nextValue= nextValue();

			/* Si la prochaine valeur vaut ucode, alors ce noeud est uniforme */
			if (nextValue == ucode) {
				return new QuadValue(value, true);
			}
			/* Sinon c'est la valeur du prochain noeud, on repositionne le flux
			 * pour que le prochain appel à nextValue() retourne cette valeur
			 */
			else {
				pushBack(nextValue);
				return new QuadValue(value, false);
			}
		}
	}

	private void pushBack(int value) {
		buffer.add(new Integer(value));
	}

	/**
	 * Doit lire la prochaine valeur dans le flux.
	 * @return : La valeur lue, ou EOF si la fin du fichier est rencontrée
	 * @throws IOException
	 */
	protected abstract int nextValue() throws IOException;
}