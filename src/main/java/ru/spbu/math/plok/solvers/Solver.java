package ru.spbu.math.plok.solvers;

import java.util.Map;

import ru.spbu.math.plok.bench.UserConfiguration;

public abstract class Solver {
	
	/**
	 * Solves the PL task
	 * @return P, L, isFilledFromUpFlag
	 * @throws Exception
	 */
	public abstract Map<String, Object> solvePLTask() throws Exception;
	
}
