package ru.spbu.math.plok.solvers;

import java.util.HashMap;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public abstract class Solver {
	
	protected String H;
	
	public Solver(String historyFile){
		H = historyFile;
	}
	public abstract HashMap<String, Object> solvePLTask() throws Exception;
	
}
