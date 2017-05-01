package ru.spbu.math.plok.solvers.histogramsolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbu.math.plok.MapKeyNames;
import ru.spbu.math.plok.bench.UserConfiguration;
import ru.spbu.math.plok.model.client.Query;
import ru.spbu.math.plok.solvers.HistoryAnalysisReport;
import ru.spbu.math.plok.solvers.Solver;
import ru.spbu.math.plok.solvers.histogramsolver.UserChoice.Policy;
import ru.spbu.math.plok.utils.structures.Pair;
import ru.spbu.math.plok.utils.structures.SegmentTreeLazy;
import ru.spbu.math.plok.utils.structures.Triplet;

public class HistogramSolver extends Solver {

	private final static Logger log = LoggerFactory.getLogger(HistogramSolver.class);

	private static final int ISLAND_WIDTH_THRESHOLD = 4;
	private static final int ISLAND_HEIGHT_THRESHOLD = 15;
	private static final int FLAT_THRESHOLD = 15;
	private static final int J_THRESHOLD = 15;


	private int cacheUnitSize;
	private HistoryAnalysisReport hReport;
	private Policy iPolicy;
	private Policy jPolicy;
	private Map<String, Object> policiesParams;
	private int L;
	private int P;

	private Comparator<Triplet<Integer>> heightComparator;
	private Histogram<Integer> i1Hist;
	private Histogram<Long>    j1Hist;
	private Histogram<Integer> iLHist;
	private Histogram<Long>    jLHist;
	private Histogram<Long>    iAHist;
	private Histogram<Double>  jRHist;


	public HistogramSolver(HistoryAnalysisReport hReport, int cacheSizeInUnits) {
		this.cacheUnitSize = cacheSizeInUnits;
		this.policiesParams = new HashMap<>();
		heightComparator = new Comparator<Triplet<Integer>>() {
			@Override
			public int compare(Triplet<Integer> o1, Triplet<Integer> o2) {
				return Integer.compare(o1.getSecond(), o2.getSecond());
			}
		};
		this.hReport = hReport;
	}

	public HashMap<String, Object> solvePLTask() throws IOException {
		HashMap<String, Object> report = new HashMap<>();
		buildHistograms();
		printAllHistograms();
		if (hReport.areHintsProvided()){
			setPolicies(hReport.getHints().get(MapKeyNames.I_POLICY_KEY), hReport.getHints().get(MapKeyNames.J_POLICY_KEY));
		}else{
			guessPolicies();
			log.debug("Estimated iPolicy: {}", iPolicy);
			log.debug("Estimated jPolicy: {}", jPolicy);
		}
		calculatePL();
		log.debug("Calculated P={}, L={}", P, L);
		report.put(MapKeyNames.P_KEY,                 P);
		report.put(MapKeyNames.L_KEY,                 L);
		report.put(MapKeyNames.IS_FILLED_FROM_UP_KEY, true);
		report.put(MapKeyNames.I_POLICY_KEY,          iPolicy);
		report.put(MapKeyNames.J_POLICY_KEY,          jPolicy);
		report.put(MapKeyNames.POLICIES_PARAMS,       policiesParams);
		return report;
	}

	private void printAllHistograms() {
		log.debug(i1Hist.toString());
		log.debug(j1Hist.toString());
		log.debug(iLHist.toString());
		log.debug(jLHist.toString());
		log.debug(jRHist.toString());
		log.debug(iAHist.toString());
	}

	private void buildHistograms() {
		ArrayList<Integer> i1Data = new ArrayList<Integer>(hReport.getQueries().size());
		ArrayList<Long>    j1Data = new ArrayList<Long>(hReport.getQueries().size());
		ArrayList<Integer> iLData = new ArrayList<Integer>(hReport.getQueries().size());
		ArrayList<Long>    jLData = new ArrayList<Long>(hReport.getQueries().size());
		SegmentTreeLazy    iAData = new SegmentTreeLazy(new long[1 + hReport.getiMax()]);
		ArrayList<Double>  jRData = new ArrayList<Double>(hReport.getQueries().size());
		long sumIQ = 0;
		for (Query query : hReport.getQueries()) {
			i1Data.add(query.getI1());
			j1Data.add(query.getJ1());
			iLData.add(query.getILength());
			jLData.add(query.getJLength());
			jRData.add((double) query.getJ2() / query.getTime());
			iAData.incrementAt(query.getI1(), query.getI2());
			sumIQ += query.getILength();
			assert (iAData.getSumAt(0, hReport.getiMax()) == sumIQ);
		}
		i1Hist = new Histogram<Integer>("I1 HISTOGRAM", i1Data, hReport.getiMin(), hReport.getiMax());
		j1Hist = new Histogram<Long>("J1 HISTOGRAM",    j1Data, hReport.getjMin(), hReport.getjMax());
		iLHist = new Histogram<Integer>("INDEX RANGE LENGTH HISTOGRAM", iLData, 0, hReport.getiMax() - hReport.getiMin() + 1);
		jLHist = new Histogram<Long>("TIME RANGE LENGTH HISTOGRAM", jLData, 0L, hReport.getjMax() - hReport.getjMin() + 1);
		jRHist = new Histogram<Double>("RELATIVE TIME RANGE LENGTH HISTOGRAM", jRData, 0.0, 1.0);
		iAData.commitUpdates();
		iAHist = new Histogram<Long>("ABSOLUTE I HISTOGRAM", Arrays.asList(iAData.getCleanLeafLine()), null, null);
		i1Hist.normalizeToPercents();
		j1Hist.normalizeToPercents();
		iLHist.normalizeToPercents();
		jLHist.normalizeToPercents();
		jRHist.normalizeToPercents();
		iAHist.normalizeToPercents();
	}

	private void setPolicies(String iHint, String jHint) {
		for (Policy policy : Policy.values()) {
			if (iHint.equalsIgnoreCase(policy.toString())) {
				iPolicy = policy;
			}
			if (jHint.equalsIgnoreCase(policy.toString())) {
				jPolicy = policy;
			}
		}
	}

	private void guessPolicies() {
		log.debug("Guessing policies...");
		guessIPolicy();
		guessJPolicy();
	}

	private void guessJPolicy() {
		Bin maxBin = jRHist.getMaxBin();
		int lastNzId = jRHist.getLastNonZeroBin();
		if (maxBin.getId() == lastNzId && maxBin.getValue() - jRHist.getBin(lastNzId - 1).getValue() > J_THRESHOLD) {
			this.jPolicy = Policy.RECENT_TRACKING;
			Long rtWindow = jLHist.getMaxRaw();
			log.debug("RT window:{}", rtWindow);
			this.policiesParams.put(MapKeyNames.J_POLICY_RT_WINDOW_KEY, rtWindow);
		} else {
			this.jPolicy = Policy.FULL_TRACKING;
		}

	}

	private void guessIPolicy() {
		List<Triplet<Integer>> islands = this.iAHist.getIslands();
		islands.sort(heightComparator);
		log.debug("Got islands for iAHist:" + islands);
		List<Pair<Integer>> hotRanges = new ArrayList<>();
		for (Triplet<Integer> island : islands) {
			if (island.getSecond() > ISLAND_HEIGHT_THRESHOLD){
				if (island.getThird()- island.getFirst() > ISLAND_WIDTH_THRESHOLD){
					int a = ((Long) iAHist.getMaxRawForBin(island.getSecond() - ISLAND_HEIGHT_THRESHOLD / 2)).intValue();
					int b = ((Long) iAHist.getMaxRawForBin(island.getSecond() + ISLAND_HEIGHT_THRESHOLD / 2)).intValue();
					hotRanges.add(new Pair<Integer>(a, b));
				}
			}
		}
		if (!hotRanges.isEmpty()){
			this.iPolicy = Policy.HOT_RANGES;
			policiesParams.put(MapKeyNames.I_POLICY_HR_RANGES_KEY, hotRanges);
		}else{
			this.iPolicy = Policy.FULL_TRACKING;
		}

	}

	private void calculatePL() {
		// TODO implement smarter! Account cacheUnit size and policies
		log.debug("Calculating P and L for {} cache unit size and {} and {} policies", cacheUnitSize, iPolicy, jPolicy);
		P = jLHist.getMaxRaw().intValue();
		L = iLHist.getMaxRaw();
	}
}
