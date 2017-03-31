package ru.spbu.math.plok.utils;

public class Pair<T> {
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
	public void setFirst(T first) {
		this.first = first;
	}
	T first;
	T second;
}
