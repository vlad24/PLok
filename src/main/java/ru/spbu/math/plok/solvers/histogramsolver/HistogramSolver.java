package ru.spbu.math.plok.solvers.histogramsolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbu.math.plok.model.client.Query;
import ru.spbu.math.plok.solvers.Solver;
import ru.spbu.math.plok.solvers.histogramsolver.UserChoice.Policy;
import ru.spbu.math.plok.utils.structures.SegmentTreeLazy;
import ru.spbu.math.plok.utils.structures.Triplet;

public class HistogramSolver extends Solver {

	private final static Logger log = LoggerFactory.getLogger(HistogramSolver.class);

	@SuppressWarnings("unused")
	private static final double ISLAND_THRESHOLD = 1.5;
	private static final int FLAT_THRESHOLD = 15;
	private static final int J_THRESHOLD = 15;

	private HParser parser;
	private int cacheUnitSize;
	private int iMax = Integer.MIN_VALUE;
	private int iMin = Integer.MAX_VALUE;
	private long jMin = Long.MAX_VALUE;;
	private long jMax = Long.MIN_VALUE;
	private long tBeg = Long.MIN_VALUE;
	private long tEnd = Long.MAX_VALUE;
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
	private ArrayList<Query>   queries;

	public HistogramSolver(String historyFile, int cacheUnitSize) {
		super(historyFile);
		this.cacheUnitSize = cacheUnitSize;
		this.policiesParams = new HashMap<>();
	}

	public HashMap<String, Object> solvePLTask() throws IOException {
		this.parser = new HParser();
		File historyFile = new File(Paths.get(H).toAbsolutePath().toString());
		HashMap<String, Object> report = new HashMap<>();
		analyzeFileData(historyFile);
		calculatePL();
		log.debug("Calculated P={}, L={}", P, L);
		report.put(P_KEY, P);
		report.put(L_KEY, L);
		report.put(I_MIN_KEY, iMin);
		report.put(J_MIN_KEY, jMin);
		report.put(I_MAX_KEY, iMax);
		report.put(J_MAX_KEY, jMax);
		report.put(QUERIES_KEY, queries);
		report.put(POLICIES_PARAMS, policiesParams);
		return report;
	}

	private void analyzeFileData(File file) throws IOException {
		String line;
		int lineNumber = 0;
		boolean hintsProvided = false;
		queries = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				if (parser.isValidHistoryLine(line)) {
					Query query = parser.getNextUserQuery(line);
					queries.add(query);
					relaxExtremes(query);
				} else if (parser.isHint(line)) {
					log.debug("Hints detected!");
					String[] hints = parser.checkAndParseHints(line);
					if (hints != null) {
						hintsProvided = true;
						setPolicies(hints[0], hints[1]);
					}
				} else {
					log.info("Line {} ignored: {}", lineNumber, line);
				}
			}
			buildHistograms();
			if (!hintsProvided) {
				guessPolicies();
			}
			printAllHistograms();
		} catch (IOException er) {
			log.error("Error at line {}: {}", lineNumber, er);
			throw er;
		}
	}

	private void printAllHistograms() {
		log.debug(i1Hist.toString());
		log.debug(j1Hist.toString());
		log.debug(iLHist.toString());
		log.debug(jLHist.toString());
		log.debug(jRHist.toString());
		log.debug(iAHist.toString());
		log.debug("Estimated iPolicy: {}", iPolicy);
		log.debug("Estimated jPolicy: {}", jPolicy);
	}

	private void relaxExtremes(Query query) {
		if (query.getTime() <= tBeg)
			tBeg = query.getTime();
		if (query.getTime() >= tEnd)
			tBeg = query.getTime();
		if (query.getI1() <= iMin)
			iMin = query.getI1();
		if (query.getJ1() <= jMin)
			jMin = query.getJ1();
		if (query.getI2() >= iMax)
			iMax = query.getI2();
		if (query.getJ2() >= jMax)
			jMax = query.getJ2();
	}

	private void buildHistograms() {
		ArrayList<Integer> i1Data = new ArrayList<Integer>(queries.size());
		ArrayList<Long>    j1Data = new ArrayList<Long>(queries.size());
		ArrayList<Integer> iLData = new ArrayList<Integer>(queries.size());
		ArrayList<Long>    jLData = new ArrayList<Long>(queries.size());
		SegmentTreeLazy    iAData = new SegmentTreeLazy(new long[1 + iMax]);
		ArrayList<Double>  jRData = new ArrayList<Double>(queries.size());
		long sumIQ = 0;
		for (Query query : queries) {
			i1Data.add(query.getI1());
			j1Data.add(query.getJ1());
			iLData.add(query.getILength());
			jLData.add(query.getJLength());
			jRData.add((double) query.getJ2() / query.getTime());
			iAData.incrementAt(query.getI1(), query.getI2());
			sumIQ += query.getILength();
			assert (iAData.getSumAt(0, iMax) == sumIQ);
		}
		i1Hist = new Histogram<Integer>("I1 HISTOGRAM", i1Data, iMin, iMax);
		j1Hist = new Histogram<Long>("J1 HISTOGRAM", j1Data, jMin, jMax);
		iLHist = new Histogram<Integer>("INDEX RANGE LENGTH HISTOGRAM", iLData, 0, iMax - iMin + 1);
		jLHist = new Histogram<Long>("TIME RANGE LENGTH HISTOGRAM", jLData, 0L, jMax - jMin + 1);
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
			this.policiesParams.put(J_POLICY_RT_WINDOW_KEY, jRHist.getMaxRawForBin(maxBin.getId()));
		} else {
			this.jPolicy = Policy.FULL_TRACKING;
		}

	}

	private void guessIPolicy() {
		if (isFlatEnough(i1Hist) && isFlatEnough(iLHist)) {
			this.iPolicy = Policy.FULL_TRACKING;
		} else {
			List<Triplet<Integer>> islands = this.iLHist.getIslands();
			islands.sort(new Comparator<Triplet<Integer>>() {
				@Override
				public int compare(Triplet<Integer> o1, Triplet<Integer> o2) {
					return Integer.compare(o1.getSecond(), o2.getSecond());
				}
			});
//			for (Triplet<Integer> island : islands) {
//				int left = island.getFirst();
//				int top = island.getSecond();
//				int right = island.getThird();
//				if (left != right){
//					double maxEdge = Math.max(iLHist.getBin(left).getValue(),
//							iLHist.getBin(right).getValue());
//					if (iLHist.getBin(top).getValue() / maxEdge >=
//							ISLAND_THRESHOLD ){
//
//					}
//				}
//			}
			this.iPolicy = Policy.HOT_RANGES;
			policiesParams.put(I_POLICY_HR_RANGES_KEY, islands);
		}

	}

	public boolean isFlatEnough(Histogram<? extends Number> histogram) {
		List<Bin> bins = histogram.getBins();
		for (int i = 1; i < bins.size(); i++) {
			if (bins.get(i).getValue() - bins.get(i - 1).getValue() > FLAT_THRESHOLD) {
				return false;
			}
		}
		return true;
	}

	private void calculatePL() {
		// TODO implement smarter! Account cacheUnit size and policies
		log.debug("Calculating P and L for {} cache unit size and {} and {} policies", cacheUnitSize, iPolicy, jPolicy);
		P = jLHist.getMaxRaw().intValue();
		L = iLHist.getMaxRaw();
	}
}
