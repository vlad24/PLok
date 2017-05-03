package ru.spbu.math.ais.plok.solvers;

import java.util.HashMap;
import java.util.LinkedHashMap;

import ru.spbu.math.ais.plok.MapKeyNames;

public class MockSolver extends Solver {

	private int P;
	private int L;

	public MockSolver(int P, int L) {
		this.P = P;
		this.L = L;
	}

	@Override
	public HashMap<String, Object> solvePLTask() throws Exception {
		LinkedHashMap<String, Object> solution = new LinkedHashMap<>();
		solution.put(MapKeyNames.P_KEY,         		P);
		solution.put(MapKeyNames.L_KEY,                 L);
		solution.put(MapKeyNames.IS_FILLED_FROM_UP_KEY, true);
		return solution;
	}

}
