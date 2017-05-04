package ru.spbu.math.ais.plok.solvers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.spbu.math.ais.plok.model.client.Query;

public class HistoryAnalysisReport {
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder("HistoryAnalysisReport[")
		.append("N=") .append(N)
		.append(", tBeg=").append(tBeg)
		.append(", tEnd=").append(tEnd)
		.append(", iMin=").append(iMin)
		.append(", jMin=").append(jMin)
		.append(", iMax=").append(iMax)
		.append(", jMax=").append(jMax)
		.append(", timeStep=").append(timeStep)
		.append(", hints= ").append(hints)
		.append(", queries=").append(queries.subList(0, Math.min(2, queries.size())))
		.append("... ]");
		return stringBuilder.toString();
	}
	private int  N;
	private long tBeg;
	private long tEnd;
	private int  iMin;
	private long jMin;
	private int  iMax;
	private long jMax;
	private long timeStep;
	private boolean areHintsProvided;
	private List<Query> queries;
	private Map<String, String> hints;
	
	public HistoryAnalysisReport() {
		tBeg = Long.MAX_VALUE;
		tEnd = Long.MIN_VALUE;
		iMin = Integer.MAX_VALUE;
		jMin = Integer.MAX_VALUE;
		iMax = Integer.MIN_VALUE;
		jMax = Integer.MIN_VALUE;
		areHintsProvided = false;
		queries = new ArrayList<Query>();
		hints = new HashMap<>();
		
	}
	public int getN() {
		return N;
	}
	public void setN(int n) {
		N = n;
	}
	public long gettBeg() {
		return tBeg;
	}
	public void settBeg(long tBeg) {
		this.tBeg = tBeg;
	}
	public long gettEnd() {
		return tEnd;
	}
	public void settEnd(long tEnd) {
		this.tEnd = tEnd;
	}
	public int getiMin() {
		return iMin;
	}
	public void setiMin(int iMin) {
		this.iMin = iMin;
	}
	public long getjMin() {
		return jMin;
	}
	public void setjMin(long jMin) {
		this.jMin = jMin;
	}
	public int getiMax() {
		return iMax;
	}
	public void setiMax(int iMax) {
		this.iMax = iMax;
	}
	public long getjMax() {
		return jMax;
	}
	public void setjMax(long jMax) {
		this.jMax = jMax;
	}
	public boolean areHintsProvided() {
		return areHintsProvided;
	}
	public void setHintsProvided(boolean areHintsProvided) {
		this.areHintsProvided = areHintsProvided;
	}
	public List<Query> getQueries() {
		return queries;
	}
	public void setQueries(List<Query> queries) {
		this.queries = queries;
	}
	public Map<String, String> getHints() {
		return hints;
	}
	public void addHint(String key, String hint) {
		this.hints.put(key, hint);
	}
	public long getTimeStep() {
		return timeStep;
	}
	public void setTimeStep(long timeStep) {
		this.timeStep = timeStep;
	}
	
}
