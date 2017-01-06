package ru.spbu.math.plok.model.client;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public abstract class Distribution {
	

	public static String DISTR_UNI = "uni";
	public static String DISTR_EXP = "exp";
	public static String DISTR_NORM = "norm";
	private String type;
	
	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return this.type;
	}
	
	@Inject
	public Distribution(@Named("V")String v) {
		if (v.equalsIgnoreCase(DISTR_EXP) || v.equalsIgnoreCase(DISTR_NORM) || v.equalsIgnoreCase(DISTR_UNI)){
			this.type = v.toLowerCase();
		}
	}

	public abstract long getRandomLong(long from, long to);
	
	public abstract int getRandomInt(int from, int to);
	
	

}
