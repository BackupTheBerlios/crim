package fr.umlv.quad.huffman;

public class TreeOps {
	private String[] codeTable= new String[256];

	public void loadLeavesCodes(Node root) {
		if (root.isLeaf()) {
			codeTable[root.getPosition()]= root.getCode();
			return;
		}

		root.getLeftChild().setCode(root.getCode() + "0");
		loadLeavesCodes(root.getLeftChild());

		root.getRightChild().setCode(root.getCode() + "1");
		loadLeavesCodes(root.getRightChild());

	}

	public String[] getCorrespTable() {
		return codeTable;
	}

	public void resetCorrespTable() {
		codeTable= new String[256];
	}
}