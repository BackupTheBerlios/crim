/*
 * Created on 27 mars 2004
 */
package fr.umlv.quad;

/**
 * @author cpele
 */
public class QuadValue {
	public QuadValue(int value, boolean b) {
		this.plain=b;
		this.value=value;
	}
	
	public static final int NOVALUE=-1;
	
	int value;
	boolean plain;
}
