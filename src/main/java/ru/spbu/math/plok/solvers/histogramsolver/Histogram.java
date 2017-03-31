package ru.spbu.math.plok.solvers.histogramsolver;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author vlad
 * Histogram helper class
 */
class Histogram<K extends Number>{

	private DecimalFormat floatTrimmer = new DecimalFormat("#.###");
	private TreeMap<K, Integer> rawOccs;
	private String name;
	private int observations;
	private int binCount;
	private double min;
	private double max;
	private int[] binnedOccs;
	private double binWidth;

	@Override
	public String toString() {
		return rawDataStringHistogram();
	}

	private String rawDataStringHistogram() {
		StringBuilder b = new StringBuilder("\n");
		b.append(name).append("\n");
		for (Map.Entry<K, Integer> entry : rawOccs.entrySet()){
			b.append(entry.getKey()).append(":\t");
			for (int i = 0; i < entry.getValue(); i++){
				b.append("=");
			}
			b.append("\n");
		}
		return b.toString();
	}




	public Histogram(String name, List<K> data, K min, K max) {
		super();
		this.name = name;
		this.min = min.doubleValue();
		this.max = max.doubleValue();
		this.binCount = calcOptimalBinAmount(data, min, max);
		this.binWidth = 1 + Math.ceil((this.max - this.min) / binCount); 
		this.rawOccs = new TreeMap<K, Integer>();
		this.binnedOccs = new int[binCount];
		this.observations = data.size();
		buildHistogram(data);
	}

	private void buildHistogram(List<K> data) {
		for (K k : data){
			addRaw(k);
			addBinned(k);
		}
		normalize();
	}
	
	private void normalize() {
		for (K k : rawOccs.keySet()){
			//set new
		}
	}

	private int calcOptimalBinAmount(List<K> data, K min, K max) {
		return (int) Math.sqrt(data.size());
	}

	private void addBinned(K k) {
		int bin = binify(k);
		binnedOccs[bin]++;
	}


	private int binify(K k) {
		double x = k.doubleValue();
		return (int)((x - min) / binWidth);
	}

	private void addRaw(K x){
		K key = x;
		if (x instanceof Double){
			key = (K)Double.valueOf(floatTrimmer.format(x.doubleValue()));
		}
		Integer oldValue = rawOccs.get(key);
		int newValue = oldValue == null ? 1 : oldValue + 1;
		rawOccs.put(key, newValue);
	}

	//Raw data
	public K getKeyWithMaxOccurence(){
		K result = null;
		int maxOccurence = Integer.MIN_VALUE;
		for (Map.Entry<K, Integer> entry : rawOccs.entrySet()){
			if (entry.getValue() >= maxOccurence){
				maxOccurence = entry.getValue();
				result = entry.getKey();
			}
		}
		return result;
	}

	public Integer getRawOccurence(K key){
		Integer occurence = rawOccs.get(key);
		return occurence == null ? 0 : occurence;
	}


	public double getRawAvgOccurence(){
		double sum = 0;
		for (Map.Entry<K, Integer> entry : rawOccs.entrySet()){
			sum += entry.getValue().doubleValue();
		}
		return sum/getDistinctObservationsAmount();
	}

	public int getDistinctObservationsAmount(){
		return rawOccs.keySet().size();
	}


}