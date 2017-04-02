package ru.spbu.math.plok.solvers.histogramsolver;

public class Bin {
	int id;
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	Double left;
	Double right;
	int occurences;
	
	@Override
	public String toString() {
		return "Bin[" + left + "," + right + "]";
	}
	
	public Bin(int id, double left, double right, int occurences) {
		super();
		this.id = id;
		this.left = left;
		this.right = right;
		this.occurences = occurences;
	}
	
	public double getLeft() {
		return left;
	}
	
	public void setLeft(double left) {
		this.left = left;
	}
	
	public int getOccurences() {
		return occurences;
	}
	
	public double getRight() {
		return right;
	}
	
	public void setRight(double right) {
		this.right = right;
	}
	
	public void setOccurences(int occurences) {
		this.occurences = occurences;
	}

	public void add() {
		occurences++;		
	}
}
