package ru.spbu.math.plok.utils.structures;

public class Pair<T> {
	
	private T first;
	private T second;
	
	public Pair(T first, T second) {
		super();
		this.first = first;
		this.second = second;
	}
	public T getFirst() {
		return first;
	}
	public T getSecond() {
		return second;
	}
	public void setSecond(T second) {
		this.second = second;
	}
	@Override
	public String toString() {
		return "[" + first + ", " + second + "]";
	}
	public void setFirst(T first) {
		this.first = first;
	}
}
