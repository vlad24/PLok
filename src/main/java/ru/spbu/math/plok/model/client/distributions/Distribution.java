package ru.spbu.math.plok.model.client.distributions;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public abstract class Distribution {
	

	public final static String DISTR_UNI    =  "$uni";
	public final static String DISTR_EXP    =  "$exp";
	public final static String DISTR_NORM   =  "$norm";
	public final static String DISTR_CUSTOM =  "$custom";
	
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
		}else{
			this.type = DISTR_CUSTOM;
		}
	}

	public abstract long getRandomP(long from, long to);
	
	public abstract int getRandomL(int from, int to);
	
	

}
