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

import ru.spbu.math.plok.model.client.Query;
import ru.spbu.math.plok.model.client.UserChoice.Policy;


public class BasicSolver extends Solver{
	
	private final static Logger log = LoggerFactory.getLogger(BasicSolver.class);
	private HParser parser;
	
	
	public BasicSolver(String historyFile){
		super(historyFile);

	}

	public HashMap<String, Object> solvePLTask() throws IOException {
		this.parser = new HParser();
		File historyFile = new File(Paths.get(H).toAbsolutePath().toString());
		HashMap<String, Object> report = analyzeFileData(historyFile);
		//Change!
		report.put("P", 2);
		report.put("L", 3);
		return report;
	}
	
	
	private HashMap<String, Object> analyzeFileData(File file) throws IOException {
		String line;
		int lineNumber = 0;
		int iMax       = Integer.MIN_VALUE;
		int iMin       = Integer.MAX_VALUE;
		long jMin      = Long.MAX_VALUE;;
		long jMax      = Long.MIN_VALUE;
		long tBeg      = Long.MIN_VALUE;
		long tEnd      = Long.MAX_VALUE;
		boolean hintsProvided = false;
		HashMap<String, Object> report  = new HashMap<>();
		ArrayList<Query>    queries = new ArrayList<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(file))){
			while ((line = reader.readLine()) != null){
				lineNumber++;
				if (parser.isValidHistoryLine(line)){
					Query query = parser.getNextUserQuery(line);
					queries.add(query);
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
				}else if (parser.isHint(line)){
					String[] hints = parser.checkAndParseHints(line);
					if (hints != null){
						hintsProvided = true;
						report.put("iHint", hints[0]);
						report.put("jHint", hints[1]);
					}
				}else{
					log.info("Line {} ignored: {}", lineNumber, line);
				}
			}
			report.put("iMin", iMin);
			report.put("iMax", iMax);
			report.put("jMin", jMin);
			report.put("jMax", jMax);
			report.put("queries", queries);
			if (!hintsProvided){
				 guessPolicies(queries, report);
			}
		}catch(Exception er){
			log.error("Error at line {}: {}", lineNumber, er);
			report.put("error", er.getMessage());
		}
		return report;
	}

	private void guessPolicies(ArrayList<Query> queries, HashMap<String, Object> report) {
		// TODO Implement!
		report.put("iPolicy",Policy.FullTrack);
		report.put("jPolicy",Policy.FullTrack);
	}


}
