package ru.spbu.math.plok.solvers;

import java.util.HashMap;

public abstract class Solver {
	
	protected int N;
	protected String H;
	
	public Solver(int vectorLength, String historyFile){
		N = vectorLength;
		H = historyFile;
	}
	
	/**
	 * Solves the PL task
	 * @return P, L, isFilledFromUpFlag
	 * @throws Exception
	 */
	public abstract HashMap<String, Object> solvePLTask() throws Exception;
	
}
