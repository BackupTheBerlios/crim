/*
 * Créé le 5 févr. 2004
 */
package fr.umlv.quad;

/**
 * @author cpele
 * 
 * Elément d'une ImageQuad (pixel de l'image ou noeud de l'arbre)
 */
public class QuadImageElement {
	byte value;
	double variance;
	boolean uni;
	QuadImageElement next;
}
