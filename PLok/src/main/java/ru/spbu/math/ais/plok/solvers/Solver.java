package ru.spbu.math.ais.plok.solvers;

import java.util.Map;

public abstract class Solver {
	
	/**
	 * Solves the PL task
	 * @return P, L, isFilledFromUpFlag
	 * @throws Exception
	 */
	public abstract Map<String, Object> solvePLTask() throws Exception;
	
}
