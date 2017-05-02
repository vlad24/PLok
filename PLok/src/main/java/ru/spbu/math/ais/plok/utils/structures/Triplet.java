package ru.spbu.math.ais.plok.utils.structures;

public class Triplet<T> {
	
	private T first;
	private T second;
	private T third;
	
	public Triplet(T first, T second, T third) {
		super();
		this.first  = first;
		this.second = second;
		this.third  = third;
	}
	@Override
	public String toString() {
		return "[" + first + ", " + second + ",  " + third + "]";
	}
	public T getFirst() {
		return first;
	}
	public void setFirst(T first) {
		this.first = first;
	}
	public T getSecond() {
		return second;
	}
	public void setSecond(T second) {
		this.second = second;
	}
	public T getThird() {
		return third;
	}
	public void setThird(T third) {
		this.third = third;
	}
}
