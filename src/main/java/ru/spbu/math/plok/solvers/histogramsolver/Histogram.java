package ru.spbu.math.plok.solvers.histogramsolver;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbu.math.plok.solvers.histogramsolver.Bin.ValueType;

/**
 * @author vlad
 * Histogram helper class
 */
class Histogram<K extends Number>{
	private final static Logger log = LoggerFactory.getLogger(Histogram.class);

	private int binCount;
	private ArrayList<Bin> bins;
	private double binWidth;
	private double binOffset;
	
	private DecimalFormat floatTrimmer = new DecimalFormat("#.###");
	private boolean isDiscrete;
	private double max;
	private double min;
	private String name;
	private int observations;

	private TreeMap<K, Integer> rawOccs;


	public Histogram(String name, List<K> data, K min, K max) {
		super();
		this.isDiscrete   = !(min instanceof Double || min instanceof Float);  
		this.name         = name;
		this.min          = min.doubleValue();
		this.max          = max.doubleValue();
		this.observations = data.size();
		this.rawOccs      = new TreeMap<K, Integer>();
		this.bins         = new ArrayList<>();
		log.debug("Constructing {} for data: {}", name, data);
		buildRawHistogram(data);
		buildEquiWidthBinHistogram(data);
		normalizeToPercents();
	}

	private void buildEquiWidthBinHistogram(List<K> data) {
		binOffset = 0;
		if (isDiscrete){
			binOffset = 0.5;
			int distincts = new HashSet<K>(data).size();
			this.binWidth = Math.max(1, (max - min) / Math.ceil(Math.sqrt(2 + distincts)));
			this.binCount = (int) Math.ceil((max - min + 1) / binWidth);
			log.debug("Distinct values: {} ", distincts);
			log.debug("Bin width:       {} ", binWidth);
			log.debug("Bin count:       {} ", binCount);
		}else{
			this.binCount = (int) Math.sqrt(data.size()); 
			this.binWidth = Math.ceil((this.max - this.min) / binCount);
		}
		for (int i = 0; i < binCount; i++){
			double left  = -binOffset + min + i * binWidth;
			double right = left + binWidth;
			if (left > max)
				break;
			bins.add(new Bin(i, left, right, 0));
			log.debug("{}th bin {} inited", i, bins.get(i));
		}
		for (K k : data){
			int id = binify(k);
			bins.get(id).incrementValue();
		}
		log.debug("All bins have been filled");
	}
	
	private void buildRawHistogram(List<K> data) {
		for (K k : data){
			@SuppressWarnings("unchecked")
			K key = (isDiscrete)?  k : (K)Double.valueOf(floatTrimmer.format(k.doubleValue()));
			Integer oldValue = rawOccs.get(key);
			rawOccs.put(key, (oldValue == null)? 1 : oldValue + 1);
		}
	}
	
	public int getAmountOfBins() {
		return binCount;
	}

	public Bin getBin(int binId){
		return bins.get(binId);
	}
	
	public ArrayList<Bin> getBins(){
		return bins;
	}
	
	public int getDistinctObservationsAmount(){
		return rawOccs.keySet().size();
	}

	
	//Raw data
	
	
	public int getMaxCountBinId(){
		int maxOcc = Integer.MAX_VALUE;
		int maxBin = -1;
		for (int j = 0; j < binCount; j++){
			if (bins.get(j).getOccurences() > maxOcc){
				maxOcc = bins.get(j).getOccurences();
				maxBin = j;
			}
		}
		return maxBin;
	}
	
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
	
	public Integer getRawCount(K key){
		Integer occurence = rawOccs.get(key);
		return occurence == null ? 0 : occurence;
	}

	public boolean isFlatEnough(){
		for (int i = 1; i < bins.size(); i++) {
			if (bins.get(i).getOccurences() - bins.get(i - 1).getOccurences() > 15){
				return false;
			}
		}
		return true;
	}
	
	private void normalizeToPercents() {
		log.debug("Normalizing histograms...");
		int percentage = 0;
		for (K k : rawOccs.keySet()){
			percentage = (int)((100.0 * rawOccs.get(k)) / observations);
			rawOccs.put(k, percentage);
		}
		int maxBinId      = -1;
		int maxPercentage = -1;
		int percentageSum = 0;
		for (Bin bin : bins){
			percentage = (int)((100.0 * bin.getOccurences()) / observations);
			bin.setValueType(ValueType.PERCENTAGE);
			bin.setValue(percentage);
			if (percentage > maxPercentage){
				maxPercentage = percentage;
				maxBinId = bin.getId();
			}
			percentageSum += percentage;
		}
		if (percentageSum < 100){
			assert (percentageSum == 99);
			getBin(maxBinId).incrementValue();
		}
	}
	
	private int binify(K k) {
		double kAsDouble = k.doubleValue();
		return (int)((kAsDouble - (min - binOffset)) / binWidth);
	}


	@Override
	public String toString() {
		return new StringBuilder()
				.append("\n\n\n")
				.append(toStringAsRaw())
				.append("\n'''''''''''''''''''''''''''''''''''''''''''''''\n")
				.append(toStringAsBins()).append("\n")
				.append("\n\n\n")
				.toString();
	}

	private String toStringAsBins() {
		StringBuilder result = new StringBuilder();
		result.append("\n").append(name).append(" ").append("[EQU_WIDTH_BINNED]")
		.append(isDiscrete? "{discrete}" : "{continuous}")
		.append("\n");
		for (Bin bin : bins){
			result.append(bin.toString()).append(":\t");
			for (int j = 0; j < bin.getOccurences(); j++){
				result.append("=");
			}
			result.append("\n");
		}
		return result.toString();
	}
	
	public String toStringAsRaw() {
		StringBuilder result = new StringBuilder();
		result.append("\n").append(name).append(" ").append("[RAW]")
		.append(isDiscrete? "{discrete}" : "{continuous}")
		.append("\n");
		for (Map.Entry<K, Integer> entry : rawOccs.entrySet()){
			result.append(entry.getKey()).append(":\t");
			for (int i = 0; i < entry.getValue(); i++){
				result.append("=");
			}
			result.append("\n");
		}
		return result.toString();
	}


}