/*
 * Cr?? le 5 f?vr. 2004
 *
 */
package fr.umlv.quad;

/**
 * @author cpele
 *
 * Exception levée quand on tente de charger une image de type inconnu
 */
public class QuadError extends RuntimeException {
	private Exception internalError;

	public QuadError(String msg) {
		super(msg);
	}

	public QuadError(String chemin, Exception e) {
		this(chemin);
		this.internalError= e;
	}

	public Exception getInternalError() {
		return internalError;
	}
}
