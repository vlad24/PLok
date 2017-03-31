package ru.spbu.math.plok.solvers.histogramsolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbu.math.plok.model.client.Query;
import ru.spbu.math.plok.model.client.UserChoice.Policy;
import ru.spbu.math.plok.solvers.Solver;


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
	private Histogram<Integer> iLHist;
	private Histogram<Long>    j1Hist;
	private Histogram<Long>    j2Hist;
	private Histogram<Long>    jLHist;
	private Histogram<Double>  jRHist;
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
		try(BufferedReader reader = new BufferedReader(new FileReader(file))){
			while ((line = reader.readLine()) != null){
				lineNumber++;
				if (parser.isValidHistoryLine(line)){
					Query query = parser.getNextUserQuery(line);
					queries.add(query);
					relaxExtremes(query);
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
			buildHistograms(queries);
			if (!hintsProvided){
				guessPolicies(queries);
			}
			log.debug(i1Hist.toString());
			log.debug(i2Hist.toString());
			log.debug(j1Hist.toString());
			log.debug(j2Hist.toString());
			log.debug(iLHist.toString());
			log.debug(jLHist.toString());
			log.debug(jRHist.toString());
			log.debug("Estimated iPolicy: {}", iPolicy);
			log.debug("Estimated jPolicy: {}", jPolicy);

		}catch(Exception er){
			log.error("Error at line {}: {}", lineNumber, er);
			throw er;
		}
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

	private void buildHistograms(List<Query> queries) {
		ArrayList<Integer> i1Data = new ArrayList<Integer>(queries.size());
		ArrayList<Integer> i2Data = new ArrayList<Integer>(queries.size());
		ArrayList<Long>    j1Data = new ArrayList<Long>(queries.size());
		ArrayList<Long>    j2Data = new ArrayList<Long>(queries.size());
		ArrayList<Integer> iLData = new ArrayList<Integer>(queries.size());
		ArrayList<Long>    jLData = new ArrayList<Long>(queries.size());
		ArrayList<Double>  jRData = new ArrayList<Double>(queries.size());
		for (Query query : queries){
			i1Data.add(query.getI1());
			i2Data.add(query.getI2());
			j1Data.add(query.getJ1());
			j2Data.add(query.getJ2());
			iLData.add(query.getILength());
			jLData.add(query.getJLength());
			jRData.add((double)query.getJ2() / query.getTime());	
		}
		i1Hist = new Histogram<Integer>("i1 histogram", i1Data, iMin, iMax );
		j1Hist = new Histogram<Long>   ("j1 histogram", j1Data, jMin, jMax);
		i2Hist = new Histogram<Integer>("i2 histogram", i2Data, iMin, iMax);
		j2Hist = new Histogram<Long>   ("j2 histogram", j2Data, jMin, jMax);
		iLHist = new Histogram<Integer>("index range length histogram",         iLData, 0,   iMax - iMin + 1);
		jLHist = new Histogram<Long>   ("time range length histogram",          jLData, 0L,  jMax - jMin + 1);
		jRHist = new Histogram<Double> ("relative time range length histogram", jRData, 0.0, 1.0);

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
		P = jLHist.getKeyWithMaxOccurence().intValue();
		L = iLHist.getKeyWithMaxOccurence();
	}
}
