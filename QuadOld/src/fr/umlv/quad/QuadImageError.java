/*
 * Cr?? le 5 f?vr. 2004
 *
 */
package fr.umlv.quad;

/**
 * @author cpele
 *
 * Exception lev?e quand on tente de charger une image de type inconnu
 */
public class QuadImageError extends RuntimeException {
	private Exception internalError;

	public QuadImageError(String msg) {
		super(msg);
	}

	public QuadImageError(String chemin, Exception e) {
		this(chemin);
		this.internalError=e;
	}
	
	public Exception getInternalError() {
		return internalError;
	}
}
