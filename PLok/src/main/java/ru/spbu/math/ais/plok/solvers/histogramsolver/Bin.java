package ru.spbu.math.ais.plok.solvers.histogramsolver;

public class Bin {
	
	private double value;
	private int id;
	private Double left;
	private Double right;
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append("B[")
		.append(String.format("%.2f", left))
		.append(",")
		.append(String.format("%.2f", right))
		.append("# ").append(String.format("%.2f", value))
		.append("]")
		.toString()
		;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bin other = (Bin) obj;
		if (id != other.id)
			return false;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
			return false;
		return true;
	}

	public Bin(int id, double left, double right, int value) {
		super();
		this.id = id;
		this.left = left;
		this.right = right;
		this.value = value;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public double getLeft() {
		return left;
	}
	
	public void setLeft(double left) {
		this.left = left;
	}
	
	public double getValue() {
		return value;
	}
	
	public double getRight() {
		return right;
	}
	
	public void setRight(double right) {
		this.right = right;
	}
	
	public void setValue(double occurences) {
		this.value = occurences;
	}

	public void incrementValue() {
		value++;		
	}
}
