package fr.umlv.quad.huffman;

public class Heap {
	private Node[] heapArray;
	private int maxSize;
	private int currentSize;

	public Heap(int mx) {
		maxSize= mx;
		currentSize= 0;
		heapArray= new Node[maxSize];
	}

	public int getCurrentSize() {
		return currentSize;
	}

	public Node remove() {
		Node root= heapArray[0];
		heapArray[0]= heapArray[--currentSize];
		trickleDown(0);
		return root;
	}

	public void trickleDown(int index) {
		int smallerChild;
		Node top= heapArray[index];
		while (index < currentSize / 2) {
			int leftChild= 2 * index + 1;
			int rightChild= leftChild + 1;
			if (rightChild < currentSize
				&& heapArray[leftChild].getFrequency()
					> heapArray[rightChild].getFrequency())
				smallerChild= rightChild;
			else
				smallerChild= leftChild;
			if (top.getFrequency() < heapArray[smallerChild].getFrequency())
				break;
			heapArray[index]= heapArray[smallerChild];
			index= smallerChild;
		}
		heapArray[index]= top;
	}

	public void trickleUp(int index) {
		int parent= (index - 1) / 2;
		Node temp= heapArray[index];

		while (index > 0
			&& heapArray[parent].getFrequency() > temp.getFrequency()) {
			heapArray[index]= heapArray[parent];
			index= parent;
			parent= (parent - 1) / 2;
		}

		heapArray[index]= temp;
	}

	public void append(Node newNode) {
		if (currentSize < maxSize)
			heapArray[currentSize++]= newNode;
	}

	public void insertOrdered(Node newNode) {
		append(newNode);
		trickleUp(currentSize - 1);
	}

	public void sort() {
		for (int j= currentSize / 2 - 1; j >= 0; j--)
			trickleDown(j);
	}
}