package ru.spbu.math.ais.plok.solvers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbu.math.ais.plok.MapKeyNames;
import ru.spbu.math.ais.plok.model.client.Query;
import ru.spbu.math.ais.plok.solvers.histogramsolver.HistogramSolver;
import ru.spbu.math.ais.plok.solvers.histogramsolver.UserChoice.Policy;

public class HistoryPreprocessor {
	private final static Logger log = LoggerFactory.getLogger(HistogramSolver.class);


	public static final String ELEMENT_SEPARATOR        = ",";
	public static final String HINTS_SEPARATOR          = "/";

	public static final String HEAD_STRING_INDICATOR    = "@";
	public static final String HINT_STRING_INDICATOR    = "!";
	public static final String COMMENT_STRING_INDICATOR = "#";

	private String filePath;
	private HistoryAnalysisReport report;

	public HistoryPreprocessor(String file) {
		this.filePath  = file;
		this.report = new HistoryAnalysisReport();
	}

	public HistoryAnalysisReport analyzeHistory() throws IOException {
		String line;
		boolean hintsProvided = false;
		long prevTime = 0;
		long timeStepSum = 0;
		List<Query> queries = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			while ((line = reader.readLine()) != null) {
				if (isValidHistoryLine(line)) {
					Query query = getNextUserQuery(line);
					timeStepSum += query.getTime() - prevTime; 
					prevTime = query.getTime();
					queries.add(query);
					relaxExtremes(query);
				} else if (isHint(line)) {
					String[] hints = checkAndParseHints(line);
					if (hints != null) {
						hintsProvided = true;
					}
					hintsProvided = true;
					int hintPos = 0;
					report.addHint(MapKeyNames.I_POLICY_KEY, hints[hintPos++]);
					report.addHint(MapKeyNames.J_POLICY_KEY, hints[hintPos++]);
					if (report.getHints().get(MapKeyNames.I_POLICY_KEY).equals(Policy.HOT_RANGES.toString())){
						report.addHint(MapKeyNames.I_POLICY_HR_RANGES_KEY, hints[hintPos++]);
					}
					if (report.getHints().get(MapKeyNames.J_POLICY_KEY).equals(Policy.RECENT_TRACKING.toString())){
						report.addHint(MapKeyNames.J_POLICY_RT_WINDOW_KEY, hints[hintPos++]);
					}
				} else if (isHead(line)){
					String[] headParts = line.substring(1).split(ELEMENT_SEPARATOR); 
					report.setN(Integer.parseInt(headParts[0]));
				}
			}
			assert (queries.size() >= 2);
			report.setTimeStep(timeStepSum / (queries.size() - 1));
			report.setQueries(queries);
			report.setHintsProvided(hintsProvided);
		} catch (IOException er) {
			throw er;
		}
		log.debug("History analyzed: {}", report);
		return report;
	}


	public boolean isValidHistoryLine(String line) {
		return !isHint(line) && !isComment(line) && line.contains(ELEMENT_SEPARATOR);
	}

	private String[] checkAndParseHints(String line){
		if (line.contains(HINTS_SEPARATOR)){
			String[] hints = line.substring(1).split(HINTS_SEPARATOR);
			return hints;
		}else{
			throw new IllegalArgumentException("Hint format exception: no hints separator " + HINTS_SEPARATOR);
		}
	}


	private Query getNextUserQuery(String line) {
		String[] row = line.replace(" ", "").split(ELEMENT_SEPARATOR);
		return new Query(Long.parseLong(row[0]), Integer.parseInt(row[1]), Integer.parseInt(row[2]), Integer.parseInt(row[3]), Integer.parseInt(row[4]));
	}

	private boolean isComment(String line) {
		return line.startsWith(COMMENT_STRING_INDICATOR);
	}

	private boolean isHead(String line) {
		return line.startsWith(HEAD_STRING_INDICATOR);
	}

	private boolean isHint(String line) {
		return line.startsWith(HINT_STRING_INDICATOR);
	}

	private void relaxExtremes(Query query) {
		if (query.getTime() <= report.gettBeg())
			report.settBeg(query.getTime());
		if (query.getTime() >= report.gettEnd())
			report.settEnd(query.getTime());
		if (query.getI1() <= report.getiMin())
			report.setiMin(query.getI1());
		if (query.getJ1() <= report.getjMin())
			report.setjMin(query.getJ1());
		if (query.getI2() >= report.getiMax())
			report.setiMax(query.getI2());
		if (query.getJ2() >= report.getjMax())
			report.setjMax(query.getJ2());
	}

}


