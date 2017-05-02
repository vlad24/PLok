package ru.spbu.math.ais.plok.solvers;

import java.util.HashMap;

import ru.spbu.math.ais.plok.MapKeyNames;
import ru.spbu.math.ais.plok.bench.UserConfiguration;

public class MockSolver extends Solver {

	private int P;
	private int L;

	public MockSolver(int P, int L) {
		this.P = P;
		this.L = L;
	}

	@Override
	public HashMap<String, Object> solvePLTask() throws Exception {
		HashMap<String, Object> solution = new HashMap<>();
		solution.put(MapKeyNames.P_KEY,         		P);
		solution.put(MapKeyNames.L_KEY,                 L);
		solution.put(MapKeyNames.IS_FILLED_FROM_UP_KEY, true);
		return solution;
	}

}
