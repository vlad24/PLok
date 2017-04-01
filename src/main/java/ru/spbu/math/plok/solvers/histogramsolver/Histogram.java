package ru.spbu.math.plok.solvers.histogramsolver;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbu.math.plok.utils.Pair;

/**
 * @author vlad
 * Histogram helper class
 */
class Histogram<K extends Number>{
	private final static Logger log = LoggerFactory.getLogger(Histogram.class);

	private DecimalFormat floatTrimmer = new DecimalFormat("#.###");
	private TreeMap<K, Integer> rawOccs;
	private String name;
	private int observations;
	private int binAmount;
	private double min;
	private double max;
	private int[] binnedOccs;
	private double binWidth;

	private boolean isDiscrete;
	//private HashMap<Integer, Pair<K>> binsBounds;

	@Override
	public String toString() {
		return new StringBuilder().append(toStringAsRaw())
				.append("\n_______________________________\n")
				.append(toStringAsBins()).append("\n").toString();
	}

	public String toStringAsRaw() {
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
	
	private String toStringAsBins() {
		StringBuilder b = new StringBuilder("\n");
		b.append(name).append("\n");
		for (int i = 0; i < binAmount; i++){
			b.append(getBinBounds(i).toString()).append(":\t");
			for (int j = 0; j < binnedOccs[i]; j++){
				b.append("=");
			}
			b.append("\n");
		}
		return b.toString();
	}




	public Histogram(String name, List<K> data, K min, K max, boolean isDiscrete) {
		super();
		this.name = name;
		this.isDiscrete = isDiscrete;
		this.min = min.doubleValue();
		this.max = max.doubleValue();
		computeOptimalBins(data, min, max);
		log.debug("Min {}, max {}. For {} bin amount is {}. Bin width is {}", min, max, data.size(), binAmount, binWidth);
		this.rawOccs = new TreeMap<K, Integer>();
		this.binnedOccs = new int[binAmount];
		this.observations = data.size();
		//this.binsBounds = new HashMap<Integer, Pair<K>>();
		buildHistogram(data);
	}


	private void buildHistogram(List<K> data) {
		for (K k : data){
			addRaw(k);
			addBinned(k);
		}
		normalizeToPercents();
	}
	
	private void normalizeToPercents() {
		log.debug("Normalizing histograms...");
		int percentage = 0;
		for (K k : rawOccs.keySet()){
			percentage = (int)((100.0 * rawOccs.get(k)) / observations);
			rawOccs.put(k, percentage);
		}
		for (int i = 0; i < binnedOccs.length; i++){
			percentage = (int)((100.0 * binnedOccs[i]) / observations);
			binnedOccs[i] = percentage;
		}
	}

	private void computeOptimalBins(List<K> data, K min, K max) {
		if (!isDiscrete){
			this.binAmount = (int)Math.sqrt(data.size());
		}else{
			int distincts = new HashSet<K>(data).size();
			this.binAmount = (int)Math.sqrt(2 + distincts);
		}
		this.binWidth = (this.max - this.min) / binAmount;
	}
	
	private int toBinId(K k) {
		double kAsDouble = k.doubleValue();
		int binId = (int)((kAsDouble - min) / binWidth);
		log.debug("Bin id for {} is {}", k, binId);
		return binId;
	}

	private void addBinned(K k) {
		int binId = toBinId(k);
		binnedOccs[binId]++;
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
	public K getMaxCountRaw(){
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
	
	public Pair<Double> getBinBounds(int id){
		return new Pair<Double>(id * binWidth, (id + 1) * binWidth);
	}
	
	public int getMaxCountBinId(){
		int maxOcc = Integer.MAX_VALUE;
		int maxBin = -1;
		for (int j = 0; j < binAmount; j++){
			if (binnedOccs[j] > maxOcc){
				maxOcc = binnedOccs[j];
				maxBin = j;
			}
		}
		return maxBin;
	}

	public Integer getBinCount(int binId){
		return binnedOccs[binId];
	}
	
	public Integer getRawCount(K key){
		Integer occurence = rawOccs.get(key);
		return occurence == null ? 0 : occurence;
	}


//	public double getRawAvg(){
//		double sum = 0;
//		for (Map.Entry<K, Integer> entry : rawOccs.entrySet()){
//			sum += entry.getValue().doubleValue();
//		}
//		return sum / getDistinctObservationsAmount();
//	}

	public int getDistinctObservationsAmount(){
		return rawOccs.keySet().size();
	}

	public int getAmountOfBins() {
		return binAmount;
	}
	
	public boolean isFlatEnough(){
		for (int i = 1; i < binnedOccs.length; i++) {
			if (binnedOccs[i] - binnedOccs[i - 1] > 15){
				return false;
			}
		}
		return true;
	}


}