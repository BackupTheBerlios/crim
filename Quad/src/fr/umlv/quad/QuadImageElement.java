/*
 * Cr?? le 5 f?vr. 2004
 */
package fr.umlv.quad;

/**
 * @author cpele
 * 
 * El?ment d'une ImageQuad (pixel de l'image ou noeud de l'arbre)
 */
public class QuadImageElement {
	byte value;
	double variance;
	boolean uni;
	int id;
	
	static int sid=0;
	
	public QuadImageElement() {id=sid++;}
	
	QuadImageElement topLeft;
	QuadImageElement topRight;
	QuadImageElement bottomLeft;
	QuadImageElement bottomRight;
}
