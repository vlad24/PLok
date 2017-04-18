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

import javax.swing.JRootPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbu.math.plok.MapKeyNames;
import ru.spbu.math.plok.model.client.Query;
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

	private Comparator<Triplet<Integer>> heightComparator;

	public HistogramSolver(int N, String historyFile, int cacheUnitSize) {
		super(N, historyFile);
		this.cacheUnitSize = cacheUnitSize;
		this.policiesParams = new HashMap<>();
		heightComparator = new Comparator<Triplet<Integer>>() {
			@Override
			public int compare(Triplet<Integer> o1, Triplet<Integer> o2) {
				return Integer.compare(o1.getSecond(), o2.getSecond());
			}
		};
	}

	public HashMap<String, Object> solvePLTask() throws IOException {
		this.parser = new HParser();
		File historyFile = new File(Paths.get(H).toAbsolutePath().toString());
		HashMap<String, Object> report = new HashMap<>();
		analyzeFileData(historyFile);
		calculatePL();
		log.debug("Calculated P={}, L={}", P, L);
		report.put(MapKeyNames.P_KEY,                 P);
		report.put(MapKeyNames.L_KEY,                 L);
		report.put(MapKeyNames.IS_FILLED_FROM_UP_KEY, true);
		report.put(MapKeyNames.I_MIN_KEY,             iMin);
		report.put(MapKeyNames.J_MIN_KEY,             jMin);
		report.put(MapKeyNames.I_MAX_KEY,             iMax);
		report.put(MapKeyNames.J_MAX_KEY,             jMax);
		report.put(MapKeyNames.QUERIES_KEY,           queries);
		report.put(MapKeyNames.I_POLICY_KEY,          iPolicy);
		report.put(MapKeyNames.J_POLICY_KEY,          jPolicy);
		report.put(MapKeyNames.POLICIES_PARAMS,       policiesParams);
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
			printAllHistograms();
			if (!hintsProvided) {
				guessPolicies();
			}
			log.debug("Estimated iPolicy: {}", iPolicy);
			log.debug("Estimated jPolicy: {}", jPolicy);
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
			Long rtWindow = jLHist.getMaxRaw();
			log.debug("RT:{}", rtWindow);
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
