package ru.spbu.math.plok.solver;

import ru.spbu.math.plok.model.client.Query;

public class HParser {
	
	private static final String HINTS_SEPARATOR = "/";
	private static final String ELEMENT_SEPARATOR = ",";
	
	public Query getNextUserQuery(String line) {
		String[] row = line.replace(" ", "").split(ELEMENT_SEPARATOR);
		return new Query(Long.parseLong(row[0]), Integer.parseInt(row[1]), Integer.parseInt(row[2]), Integer.parseInt(row[3]), Integer.parseInt(row[4]));
	}


	public boolean isValidHistoryLine(String line) {
		return line.contains(ELEMENT_SEPARATOR);
	}

	public String[] checkAndParseHints(String line){
		if (line.contains(HINTS_SEPARATOR)){
			String[] hints = line.split(HINTS_SEPARATOR);
			if (hints.length != 2)
				throw new IllegalArgumentException("PMF file format exception: first line is in incorrect format");
			return hints;
		}else{
			return null;
		}
	}
}


