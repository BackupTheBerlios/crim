package fr.umlv.quad.huffman;

import java.io.Serializable;

public class Node implements Serializable {
	private Node leftChild;
	private Node rightChild;
	private int position;

	private transient int frequency;
	private transient String code;

	public Node() {
		frequency= 0;
		code= "";
		position= -1;
	}

	public Node(int pos) {
		frequency= 0;
		position= pos;
		code= "";
	}

	public void incFreq() {
		frequency++;
	}

	public boolean isLeaf() {
		if (leftChild == null && rightChild == null)
			return true;
		else
			return false;
	}

	public void setLeftChild(Node child) {
		leftChild= child;
	}
	public void setRightChild(Node child) {
		rightChild= child;
	}

	public void setFreq(int value) {
		frequency= value;
	}

	public void setPosition(int lePosi) {
		position= lePosi;
	}
	public void setCode(String leCode) {
		code= leCode;
	}

	public Node getLeftChild() {
		return leftChild;
	}
	public Node getRightChild() {
		return rightChild;
	}

	public int getFrequency() {
		return frequency;
	}

	public int getPosition() {
		return position;
	}
	public String getCode() {
		return code;
	}
}