package ru.spbu.math.ais.plok.solvers.histogramsolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbu.math.ais.plok.MapKeyNames;
import ru.spbu.math.ais.plok.model.client.Query;
import ru.spbu.math.ais.plok.solvers.HistoryAnalysisReport;
import ru.spbu.math.ais.plok.solvers.Solver;
import ru.spbu.math.ais.plok.solvers.histogramsolver.UserChoice.Policy;
import ru.spbu.math.ais.plok.utils.NumbersUtils;
import ru.spbu.math.ais.plok.utils.structures.Pair;
import ru.spbu.math.ais.plok.utils.structures.SegmentTreeLazy;
import ru.spbu.math.ais.plok.utils.structures.Triplet;

public class HistogramSolver extends Solver {

	private final static Logger log = LoggerFactory.getLogger(HistogramSolver.class);

	private static final int ISLAND_WIDTH_THRESHOLD  = 4;
	private static final int ISLAND_HEIGHT_THRESHOLD = 15;
	//private static final int FLAT_THRESHOLD          = 15;
	private static final int J_THRESHOLD             = 15;

	private static final double BLOCK_SPACE_TRHESH = 0.3;


	private int cacheUnitSize;
	private HistoryAnalysisReport hReport;
	private Policy iPolicy;
	private Policy jPolicy;
	private Map<String, Object> policiesParams;
	private int P;
	private int L;

	private Histogram<Integer> i1Hist;
	private Histogram<Long>    j1Hist;
	private Histogram<Integer> iLHist;
	private Histogram<Long>    jLHist;
	private Histogram<Long>    iAHist;
	private Histogram<Double>  jRHist;
	private Comparator<Triplet<Integer>> heightComparator;


	public HistogramSolver(HistoryAnalysisReport hReport, int cacheSizeInUnits) {
		this.cacheUnitSize = cacheSizeInUnits;
		this.policiesParams = new LinkedHashMap<>();
		heightComparator = new Comparator<Triplet<Integer>>() {
			@Override
			public int compare(Triplet<Integer> o1, Triplet<Integer> o2) {
				return Integer.compare(o1.getSecond(), o2.getSecond());
			}
		};
		this.hReport = hReport;
	}

	public Map<String, Object> solvePLTask() throws IOException {
		Map<String, Object> report = new LinkedHashMap<>();
		buildHistograms();
		printAllHistograms();
		if (hReport.areHintsProvided()){
			setPolicies(hReport.getHints().get(MapKeyNames.I_POLICY_KEY), hReport.getHints().get(MapKeyNames.J_POLICY_KEY));
			this.policiesParams.put(MapKeyNames.I_POLICY_HR_RANGES_KEY, hReport.getHints().get(MapKeyNames.I_POLICY_HR_RANGES_KEY));
			this.policiesParams.put(MapKeyNames.J_POLICY_RT_WINDOW_KEY, Long.valueOf(hReport.getHints().get(MapKeyNames.J_POLICY_RT_WINDOW_KEY)));
		}else{
			guessPolicies();
		}
		calculatePL();
		report.put(MapKeyNames.P_KEY,                 P);
		report.put(MapKeyNames.L_KEY,                 L);
		report.put(MapKeyNames.I_POLICY_KEY,          iPolicy);
		report.put(MapKeyNames.J_POLICY_KEY,          jPolicy);
		report.put(MapKeyNames.POLICIES_PARAMS_KEY,   policiesParams);
		report.put(MapKeyNames.IS_FILLED_FROM_UP_KEY, true);
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
		iAData.commitUpdates();
		i1Hist = new Histogram<Integer>("I1 HISTOGRAM",                         i1Data, hReport.getiMin(), hReport.getiMax());
		j1Hist = new Histogram<Long>   ("J1 HISTOGRAM",                         j1Data, hReport.getjMin(), hReport.getjMax());
		iLHist = new Histogram<Integer>("INDEX RANGE LENGTH HISTOGRAM",         iLData, 0                , hReport.getiMax() - hReport.getiMin() + 1);
		jLHist = new Histogram<Long>   ("TIME RANGE LENGTH HISTOGRAM",          jLData, 0L               , hReport.getjMax() - hReport.getjMin() + 1);
		jRHist = new Histogram<Double> ("RELATIVE TIME RANGE LENGTH HISTOGRAM", jRData, 0.0              , 1.0);
		iAHist = new Histogram<Long>   ("ABSOLUTE I HISTOGRAM",                 Arrays.asList(iAData.getCleanLeafLine()), null, null);
		i1Hist.normalizeToPercents();
		j1Hist.normalizeToPercents();
		iLHist.normalizeToPercents();
		jLHist.normalizeToPercents();
		jRHist.normalizeToPercents();
		iAHist.normalizeToPercents();
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
			log.debug("J RecentTrackWindow:{}", rtWindow);
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

	private void calculatePL() {
		log.debug("Calculating P and L for {} cache unit size and {} and {} policies", cacheUnitSize, iPolicy, jPolicy);
		int N = hReport.getN();
		double sqrtK = Math.sqrt(cacheUnitSize);
		if (iPolicy.equals(Policy.FULL_TRACKING) && jPolicy.equals(Policy.FULL_TRACKING)){
			L = iLHist.getExpectedRaw().intValue();
			P = jLHist.getExpectedRaw().intValue();
			while (L >= cacheUnitSize){
				L /= 2;
			}
			P = Math.min(P, cacheUnitSize / L);
		}else if (iPolicy.equals(Policy.FULL_TRACKING) && jPolicy.equals(Policy.RECENT_TRACKING)){
			L = iLHist.getExpectedRaw().intValue();
			long boundP = Double.valueOf(Math.min(sqrtK, cacheUnitSize / L)).longValue();
			Long w = Long.valueOf(hReport.getHints().get(MapKeyNames.J_POLICY_RT_WINDOW_KEY));
			P = (int) Math.max(hReport.getTimeStep(), w);
			while (P < boundP) {
				P *= 2;
			}
		}else if (iPolicy.equals(Policy.HOT_RANGES) && jPolicy.equals(Policy.FULL_TRACKING)){
			List<Pair<Integer>> hRanges = parseRanges(N);
			hRanges.sort(new Comparator<Pair<Integer>>() {
				public int compare(Pair<Integer> o1, Pair<Integer> o2) {
					return (o1.getSecond() - o1.getFirst()) - (o2.getSecond() - o2.getFirst());
				}
			});
			Pair<Integer> shortestRange = hRanges.get(0);
			int smallestFactor = Math.max(NumbersUtils.getFactors(shortestRange.getSecond() - shortestRange.getFirst()).get(0), 2);
			L = Math.min(N / 2,  Math.max(smallestFactor / 2, 2));
			P = 2;
			while (P * L >= cacheUnitSize) {
				L /= 2;
			}
		}else if (iPolicy.equals(Policy.HOT_RANGES) && jPolicy.equals(Policy.RECENT_TRACKING)){
			Long r = hReport.getTimeStep();
			Long w = Long.valueOf(hReport.getHints().get(MapKeyNames.J_POLICY_RT_WINDOW_KEY));
			long realW = Math.max(r, w);
			List<Pair<Integer>> hRanges = parseRanges(N);
			Pair<Double> result = getOptimalL4HotRanges(N, hRanges, 2.f);
			L = result.getFirst().intValue();
			float cacheCapacityMeasure = (realW * L) / ((float)cacheUnitSize);
			if (Double.compare(cacheCapacityMeasure, BLOCK_SPACE_TRHESH) > 0) {
				P = (int) realW;
			}else {
				P = (int) Double.valueOf(sqrtK).longValue();
			}
			while (P*L >= cacheUnitSize) {
				P /= 2;
			}
		}
		assert P*L <= cacheUnitSize;
	}

	private Pair<Double> getOptimalL4HotRanges(int n, List<Pair<Integer>> hRanges, float alpha) {
		List<Pair<Integer>> hrs = new ArrayList<Pair<Integer>>(hRanges);
		Pair<Integer> shortestRange = Collections.min(hRanges, new Comparator<Pair<Integer>>() {
			@Override
			public int compare(Pair<Integer> o1, Pair<Integer> o2) {
				return (o1.getSecond() - o1.getFirst()) - (o2.getSecond() - o2.getFirst());
			}
		});
		int minL = shortestRange.getSecond() - shortestRange.getFirst();
		double maxPrtn = Integer.MIN_VALUE;
		int bestL = n;
		for (int l = minL; l < n; l++) {
			double prtn = 0; 
			log.trace("----Take L={}", l);
			for (int i = 0; i < Math.ceil(n/l); i++) {
				int left = i * l;
				int right = left + l;
				int hrn = 0;
				float full = 0;
				for (Pair<Integer> hr : hrs) {
					if (left <= hr.getFirst() && hr.getSecond() <= right) {
						hrn++;
						full += (1.0 / l) * (hr.getSecond() - hr.getFirst());
					}
				}
				log.trace("In [{},{}] there are {} hrs, full {}", left, right, hrn, full);
				prtn = Math.pow(hrn, alpha) * full;
				if (prtn >= maxPrtn) {
					maxPrtn = prtn;
					bestL = l;
				}
			}
		}
		return new Pair<Double>((double) bestL, maxPrtn);
		
	}

	private List<Pair<Integer>> parseRanges(int N) {
		if(!hReport.getHints().containsKey(MapKeyNames.I_POLICY_HR_RANGES_KEY)) {
			throw new IllegalArgumentException("HRs not provided");
		}
		String[] ranges = ((String) hReport.getHints().get(MapKeyNames.I_POLICY_HR_RANGES_KEY)).split(",");
		List<Pair<Integer>> hRanges = new ArrayList<>();
		for (String string : ranges) {
			String[] points = string.split("-");
			Integer i1 = (int)((Float.parseFloat(points[0]) / 100.0) * (N - 1)); 
			Integer i2 = (int)((Float.parseFloat(points[1]) / 100.0) * (N - 1));
			hRanges.add(new Pair<Integer>(i1, i2));
		}
		return hRanges;
	}
}
