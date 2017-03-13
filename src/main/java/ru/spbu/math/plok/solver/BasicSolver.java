package ru.spbu.math.plok.solver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbu.math.plok.statutils.Stat;


public class BasicSolver{
	
	private final static Logger log = LoggerFactory.getLogger(BasicSolver.class);
	
	private static final String HINTS_SEPARATOR = "/";
	private static final String ELEMENT_SEPARATOR = ",";
	
	public BasicSolver(){

	}

	public HashMap<String, Object> solvePLTask(String queryHistory) throws IOException {
		if (ELEMENT_SEPARATOR.equals(HINTS_SEPARATOR))
			throw new IllegalStateException("ELEMENT_SEPARATOR == OFFSETS_SEPARATOR!");
		File historyFile = new File(Paths.get(queryHistory).toAbsolutePath().toString());
		HashMap<String, Object> report = analyzeFileData(historyFile);
		log.debug("Custom distribution successfully constructed");
//		float[][] pmf = Stat.buildPmfMatrix(report.get("queries"));
//		Stat.validatePmfConsistency(pmf);
//		ArrayList<Float> means       = Stat.calculateMeans(pmf);
//		ArrayList<Float> disps       = Stat.calculateDs(pmf);
//		ArrayList<Float[]> marginals = Stat.calculateMarginals(pmf);
//		log.debug("Marginal P: {} ", Arrays.toString(marginals.get(0)));
//		log.debug("Marginal L: {} ", Arrays.toString(marginals.get(1)));
//		log.debug("Means: mL={}, mP={}", means.get(0), means.get(1));
//		log.debug("Ds: dL={}, dP={}", disps.get(0), disps.get(1));
//		log.debug("Covariation: cov={}", Stat.calculateCovariation(pmf));
//		log.debug("Corelation: cor={}", Stat.calculateCorelation(pmf));
		return null;
	}
	
	
	private HashMap<String, Object> analyzeFileData(File file) throws IOException {
		String line;
		int lineNumber = 1;
		int validLineNumber = 1;
		int iMax  = Integer.MIN_VALUE;
		int jMax  = Integer.MIN_VALUE;
		int iMin  = Integer.MAX_VALUE;
		int jMin  = Integer.MAX_VALUE;;
		long tBeg = Long.MIN_VALUE;
		long tEnd = Long.MAX_VALUE;
		boolean hintsProvided = false;
		HashMap<String, Object> report  = new HashMap<>();
		ArrayList<UserQuery>    queries = new ArrayList<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(file))){
			while ((line = reader.readLine()) != null){
				if (isValidHistoryLine(line)){
					UserQuery query = getNextUserQuery(line);
					queries.add(query);
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
					validLineNumber++;
				}else if (validLineNumber == 1){
					String[] hints = checkAndParseHints(line);
					 if (hints != null){
						 hintsProvided = true;
						 report.put("iHint", hints[0]);
						 report.put("jHint", hints[0]);
					 }
					validLineNumber++;
				}else{
					log.info("Line {} ignored: {}", validLineNumber, line);
				}
			}
			report.put("iMin", iMin);
			report.put("jMin", jMin);
			report.put("iMax", iMax);
			report.put("jMax", jMax);
			report.put("queries", queries);
			if (!hintsProvided){
				Stat.DISTRIBUTION_TYPE[] hints = guessDistros(queries);
				report.put("iHint", hints[0]);
				report.put("jHint", hints[0]);
			}
		}catch(Exception er){
			log.error("Error at line {}: {}", lineNumber, er);
			report.put("error", er.getMessage());
		}
		return report;
	}

	private Stat.DISTRIBUTION_TYPE[] guessDistros(ArrayList<UserQuery> queries) {
		// TODO Auto-generated method stub
		return new Stat.DISTRIBUTION_TYPE[]{Stat.DISTRIBUTION_TYPE.UNIFORM, Stat.DISTRIBUTION_TYPE.UNIFORM};
	}

	private UserQuery getNextUserQuery(String line) {
		String[] row = line.replace(" ", "").split(ELEMENT_SEPARATOR);
		return new UserQuery(Long.parseLong(row[0]), Integer.parseInt(row[1]), Integer.parseInt(row[2]), Integer.parseInt(row[3]), Integer.parseInt(row[4]));
	}


	private boolean isValidHistoryLine(String line) {
		return line.contains(ELEMENT_SEPARATOR);
	}
	
	private String[] checkAndParseHints(String line){
		if (line.contains(HINTS_SEPARATOR)){
			String[] hints = line.split(HINTS_SEPARATOR);
			if (hints.length != 2)
				throw new IllegalArgumentException("PMF file format exception: first line is in incorrect format");
			return hints;
		}else{
			return null;
		}
	}
	

	
	////////////////////////////////////////
	public static class UserQuery{
		long time;
		int i1;
		int i2;
		int j1;
		int j2;
		public UserQuery(long time, int i1, int i2, int j1, int j2) {
			super();
			this.time = time;
			this.i1 = i1;
			this.i2 = i2;
			this.j1 = j1;
			this.j2 = j2;
		}
		public long getTime() {
			return time;
		}
		public int getI1() {
			return i1;
		}
		public int getI2() {
			return i2;
		}
		public int getJ1() {
			return j1;
		}
		public int getJ2() {
			return j2;
		}
	}

}
