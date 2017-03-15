package ru.spbu.math.plok.solver;

import java.util.HashMap;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public abstract class Solver {
	
	protected String H;
	
	@Inject
	public Solver(@Named("H") String historyFile){
		
	}
	public abstract HashMap<String, Object> solvePLTask() throws Exception;
	
}
