package ru.spbu.math.ais.plok.utils;

import java.util.ArrayList;
import java.util.List;

public class NumbersUtils {

	public static List<Integer> getFactors(int x){
		List<Integer> factors = new ArrayList<Integer>();
		for (int i = 2; i < x / 2; i++) {
			if (x % i == 0) {
				factors.add(i);
			}
		}
		return factors;
	}
	
}
