package ru.spbu.math.plok.solver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbu.math.plok.model.client.Query;
import ru.spbu.math.plok.model.client.UserChoice.Policy;


public class BasicSolver extends Solver{

	private final static Logger log = LoggerFactory.getLogger(BasicSolver.class);
	private HParser parser;
	private int    iMax  = Integer.MIN_VALUE;
	private int    iMin  = Integer.MAX_VALUE;
	private long   jMin  = Long.MAX_VALUE;;
	private long   jMax  = Long.MIN_VALUE;
	private long   tBeg  = Long.MIN_VALUE;
	private long   tEnd  = Long.MAX_VALUE;
	private Policy iPolicy ;
	private Policy jPolicy;
	private int P;
	private int L;

	private Histogram<Integer> i1Hist;
	private Histogram<Integer> i2Hist;
	private Histogram<Integer> indexRangeHistogram;
	private Histogram<Long>    j1Hist;
	private Histogram<Long>    j2Hist;
	private Histogram<Long>    timeRangeLengthHistogram;
	private Histogram<Double>  jRelativeHistogram;
	private ArrayList<Query> queries;


	public BasicSolver(String historyFile){
		super(historyFile);

	}

	public HashMap<String, Object> solvePLTask() throws IOException {
		this.parser = new HParser();
		File historyFile = new File(Paths.get(H).toAbsolutePath().toString());
		HashMap<String, Object> report = new HashMap<>();
		analyzeFileData(historyFile);
		calculatePL();
		log.debug("Calculated P={}, L={}", P , L);
		report.put("P", P);
		report.put("L", L);
		report.put("iMin", iMin);
		report.put("jMin", jMin);
		report.put("iMax", jMax);
		report.put("jMax", jMax);
		report.put("queries", queries);
		return report;
	}


	private void analyzeFileData(File file) throws IOException {
		String line;
		int  lineNumber = 0;
		boolean hintsProvided = false;
		queries = new ArrayList<>();
		initHistograms();
		try(BufferedReader reader = new BufferedReader(new FileReader(file))){
			while ((line = reader.readLine()) != null){
				lineNumber++;
				if (parser.isValidHistoryLine(line)){
					Query query = parser.getNextUserQuery(line);
					queries.add(query);
					relaxExtremes(query);
					updateHistograms(query);
				}else if (parser.isHint(line)){
					log.debug("Hints detected!");
					String[] hints = parser.checkAndParseHints(line);
					if (hints != null){
						hintsProvided = true;
						setPolicies(hints[0], hints[1]);
					}
				}else{
					log.info("Line {} ignored: {}", lineNumber, line);
				}
			}
			closeHistograms();
			if (!hintsProvided){
				guessPolicies(queries);
			}
			log.debug(i1Hist.toString());
			log.debug(i2Hist.toString());
			log.debug(j1Hist.toString());
			log.debug(j2Hist.toString());
			log.debug(indexRangeHistogram.toString());
			log.debug(timeRangeLengthHistogram.toString());
			log.debug(jRelativeHistogram.toString());
			log.debug("Estimated iPolicy: {}", iPolicy);
			log.debug("Estimated jPolicy: {}", jPolicy);

		}catch(Exception er){
			log.error("Error at line {}: {}", lineNumber, er);
			throw er;
		}
	}


	private void closeHistograms() {
		i1Hist.close();
		i2Hist.close();
		indexRangeHistogram.close();
		j1Hist.close();
		j2Hist.close();
		timeRangeLengthHistogram.close();
		jRelativeHistogram.close();

	}

	private void relaxExtremes(Query query) {
		if (query.getTime() <= tBeg)
			tBeg = query.getTime();
		if (query.getTime() >= tEnd)
			tBeg = query.getTime();
		if (query.getI1() <= iMin)
			iMin = query.getI1();
		if (query.getJ1() <= jMin)
			jMin = query.getI2();
		if (query.getI2() >= iMax)
			iMax = query.getI2();
		if (query.getJ2() >= jMax)
			jMax = query.getJ2();
	}

	private void initHistograms() {
		i1Hist = new Histogram<Integer>("i1 histogram");
		j1Hist = new Histogram<Long>   ("j1 histogram");
		i2Hist = new Histogram<Integer>("i2 histogram");
		j2Hist = new Histogram<Long>   ("j2 histogram");
		indexRangeHistogram = new Histogram<Integer>("index range length histogram");
		timeRangeLengthHistogram = new Histogram<Long>   ("time range length histogram");
		jRelativeHistogram = new Histogram<Double> ("relative time range length histogram");

	}

	private void updateHistograms(Query query) {
		i1Hist.registerNew(query.getI1());
		i2Hist.registerNew(query.getI2());
		indexRangeHistogram.registerNew(query.getILength());
		j1Hist.registerNew(query.getJ1());
		j2Hist.registerNew(query.getJ2());
		timeRangeLengthHistogram.registerNew(query.getJLength());
		jRelativeHistogram.registerNew((double)query.getJ2() / query.getTime());
	}


	private void setPolicies(String iHint, String jHint) {
		for (Policy policy : Policy.values()){
			if (iHint.equalsIgnoreCase(policy.toString())){
				iPolicy = policy;
			}
			if (jHint.equalsIgnoreCase(policy.toString())){
				jPolicy = policy;
			}
		}
	}

	private void guessPolicies(ArrayList<Query> queries) {
		// TODO implement
		this.iPolicy = Policy.FullTrack;
		this.jPolicy = Policy.FullTrack;
	}

	private void calculatePL() {
		//TODO implement smarter!
		P = timeRangeLengthHistogram.getKeyWithMaxOccurence().intValue();
		L = indexRangeHistogram.getKeyWithMaxOccurence();
	}




	/**
	 * @author vlad
	 * Histogram helper class
	 */
	private static class Histogram<K extends Number>{


		private TreeMap<K, Integer> occurences;
		private DecimalFormat floatTrimmer = new DecimalFormat("#.###");
		private String name;
		private Double avgOcc;
		private boolean closed;
		private K keyWithMaxOcc;
		private int occSum;

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder("\n");
			b.append(name).append("\n");
			for (Map.Entry<K, Integer> entry : occurences.entrySet()){
				b.append(entry.getKey()).append(":\t");
				for (int i = 0; i < entry.getValue(); i++){
					b.append("=");
				}
				b.append("\n");
			}
			return b.toString();
		}


		public Histogram(String name) {
			super();
			this.name = name;
			this.closed = false;
			occurences = new TreeMap<K, Integer>();
			this.occSum = 0;
		}

		public void registerNew(K x){
			if (!closed){
				K key = x;
				if (x instanceof Double){
					key = (K)Double.valueOf(floatTrimmer.format(x.doubleValue()));
				}
				Integer oldValue = occurences.get(key);
				int newValue = oldValue == null ? 1 : oldValue + 1;
				occurences.put(key, newValue);
				occSum++;
			}
		}

		public K getKeyWithMaxOccurence(){
			if (closed){
				if (keyWithMaxOcc == null){
					K result = null;
					int maxOccurence = Integer.MIN_VALUE;
					for (Map.Entry<K, Integer> entry : occurences.entrySet()){
						if (entry.getValue() >= maxOccurence){
							maxOccurence = entry.getValue();
							result = entry.getKey();
						}
					}
					keyWithMaxOcc = result;
				}
				return keyWithMaxOcc;
			}else{
				return null; 
			}
		}

		public Integer getOccurence(K key){
			if (closed){
				Integer occurence = occurences.get(key);
				return occurence == null ? 0 : occurence;
			}else{
				return null;
			}
		}


		public double getAvg(){
			if (closed){
				if (avgOcc == null){
					double sum = 0;
					for (Map.Entry<K, Integer> entry : occurences.entrySet()){
						sum += entry.getValue().doubleValue();
					}
					avgOcc = sum/getColumnsAmount();
				}
				return avgOcc;
			}else{
				return Double.MIN_VALUE;
			}
		}

		public int getColumnsAmount(){
			return occurences.keySet().size();
		}

		public void close(){
			closed = true;
			//TODO add normalization using occSum
		}

	}
}
