package ru.spbu.math.ais.plok.solvers;

import java.util.HashMap;
import java.util.LinkedHashMap;

import ru.spbu.math.ais.plok.MapKeyNames;

public class MockSolver extends Solver {

	private int P;
	private int L;
	private HistoryAnalysisReport hReport;

	public MockSolver(HistoryAnalysisReport hReport, int P, int L) {
		this.P = P;
		this.L = L;
		this.hReport = hReport;
		assert (hReport.areHintsProvided());
	}

	@Override
	public HashMap<String, Object> solvePLTask() throws Exception {
		LinkedHashMap<String, Object> solution = new LinkedHashMap<>();
		solution.put(MapKeyNames.P_KEY,         		P);
		solution.put(MapKeyNames.L_KEY,                 L);
		solution.put(MapKeyNames.L_KEY,                 L);
		solution.put(MapKeyNames.IS_FILLED_FROM_UP_KEY, true);
		solution.put(MapKeyNames.I_POLICY_KEY,          hReport.getHints().get(MapKeyNames.I_POLICY_KEY));
		solution.put(MapKeyNames.J_POLICY_KEY,          hReport.getHints().get(MapKeyNames.J_POLICY_KEY));
		LinkedHashMap<String, Object> policiesParams = new LinkedHashMap<>();
		if (hReport.getHints().containsKey(MapKeyNames.J_POLICY_RT_WINDOW_KEY))
			policiesParams.put(MapKeyNames.J_POLICY_RT_WINDOW_KEY, hReport.getHints().get(MapKeyNames.J_POLICY_RT_WINDOW_KEY));
		if (hReport.getHints().containsKey(MapKeyNames.I_POLICY_HR_RANGES_KEY))
			policiesParams.put(MapKeyNames.I_POLICY_HR_RANGES_KEY, hReport.getHints().get(MapKeyNames.I_POLICY_HR_RANGES_KEY));
		solution.put(MapKeyNames.POLICIES_PARAMS_KEY,   policiesParams);
		return solution;
	}

}
