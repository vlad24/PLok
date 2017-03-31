package ru.spbu.math.plok.solvers.histogramsolver;

import ru.spbu.math.plok.model.client.Query;

public class HParser {
	
	private static final String HINTS_SEPARATOR       = "/";
	private static final String ELEMENT_SEPARATOR     = ",";
	private static final String HINT_STRING_INDICATOR = "!";
	
	public Query getNextUserQuery(String line) {
		String[] row = line.replace(" ", "").split(ELEMENT_SEPARATOR);
		return new Query(Long.parseLong(row[0]), Integer.parseInt(row[1]), Integer.parseInt(row[2]), Integer.parseInt(row[3]), Integer.parseInt(row[4]));
	}


	public boolean isValidHistoryLine(String line) {
		return !isHint(line) && line.contains(ELEMENT_SEPARATOR);
	}

	public String[] checkAndParseHints(String line){
		if (line.contains(HINTS_SEPARATOR)){
			String[] hints = line.substring(1).split(HINTS_SEPARATOR);
			if (hints.length != 2)
				throw new IllegalArgumentException("Hint format exception: not correct number of hints");
			return hints;
		}else{
			throw new IllegalArgumentException("Hint format exception: no hints separator " + HINTS_SEPARATOR);
		}
	}


	public boolean isHint(String line) {
		return line.startsWith(HINT_STRING_INDICATOR);
	}
}


