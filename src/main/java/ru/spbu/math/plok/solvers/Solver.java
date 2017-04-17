package ru.spbu.math.plok.solvers;

import java.util.HashMap;

public abstract class Solver {
	
	public static final String P_KEY       = "P";
	public static final String L_KEY       = "L";
	public static final String I_MIN_KEY    = "iMin";
	public static final String I_MAX_KEY    = "iMax";
	public static final String J_MIN_KEY    = "jMin";
	public static final String J_MAX_KEY    = "jMax";
	public static final String QUERIES_KEY = "queries";
	public static final String I_POLICY_KEY = "iPolicy";
	public static final String J_POLICY_KEY = "jPolicy";
	public static final String I_POLICY_HR_RANGES_KEY = "iPolicy_HR_ranges";
	public static final String J_POLICY_RT_WINDOW_KEY = "jPolicy_RT_window";
	public static final String POLICIES_PARAMS = "policies_params";
	
	
	
	protected String H;
	
	public Solver(String historyFile){
		H = historyFile;
	}
	public abstract HashMap<String, Object> solvePLTask() throws Exception;
	
}
