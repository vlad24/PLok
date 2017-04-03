package ru.spbu.math.plok.solvers.histogramsolver;

import java.text.DecimalFormat;

public class Bin {
	
	static enum ValueType{
		PERCENTAGE, RAW
	}
	
	private int value;
	private int id;
	private Double left;
	private Double right;
	private ValueType valueType;
	
	@Override
	public String toString() {
		DecimalFormat format = new DecimalFormat("#.##");
		return new StringBuilder()
		.append("B[")
		.append(format.format(left)).append(",").append(format.format(right))
		.append("# ").append(value)
		.append(" ").append((valueType == ValueType.PERCENTAGE)? "%" : "pcs")
		.append("]")
		.toString()
		;
	}
	
	public Bin(int id, double left, double right, int value) {
		super();
		this.id = id;
		this.left = left;
		this.right = right;
		this.value = value;
		this.valueType = ValueType.RAW;
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
	
	public int getOccurences() {
		return value;
	}
	
	public double getRight() {
		return right;
	}
	
	public void setRight(double right) {
		this.right = right;
	}
	
	public void setValue(int occurences) {
		this.value = occurences;
	}

	public ValueType getValueType() {
		return valueType;
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

	public void incrementValue() {
		value++;		
	}
}
