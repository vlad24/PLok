package ru.spbu.math.plok.solvers.histogramsolver;

import java.text.DecimalFormat;
import java.util.ArrayList;
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
	private int binCount;
	private double min;
	private double max;
	private ArrayList<Bin> bins;
	private double binWidth;

	private boolean isDiscrete;

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
		for (int i = 0; i < binCount; i++){
			b.append(getBinBounds(i).toString()).append(":\t");
			for (int j = 0; j < bins.get(i).getOccurences(); j++){
				b.append("=");
			}
			b.append("\n");
		}
		return b.toString();
	}




	public Histogram(String name, List<K> data, K min, K max) {
		super();
		this.isDiscrete = !(min instanceof Double || min instanceof Float);  
		this.name = name;
		this.min = min.doubleValue();
		this.max = max.doubleValue();
		this.rawOccs = new TreeMap<K, Integer>();
		this.observations = data.size();
		this.bins = new ArrayList<>();
		buildRawHistogram(data);
		buildEquiWidthBinHistogram(data);
		normalizeToPercents();
	}

	private void buildEquiWidthBinHistogram(List<K> data) {
		if (isDiscrete){
			int distincts = new HashSet<K>(data).size();
			this.binWidth = Math.max(1, Math.ceil((max - min) / Math.ceil(Math.sqrt(2 + distincts))));
			this.binCount = (int) Math.ceil((max - min) / binWidth);
		}else{
			this.binCount = (int) Math.sqrt(data.size()); 
			this.binWidth = Math.ceil((this.max - this.min) / binCount);
			for (int i = 0; i < binCount; i++){
				bins.add(new Bin(i, min + i * binWidth, min + (i + 1) * binWidth, 0));
			}
			for (K k : data){
				bins.get(toBinId(k)).add();
			}
		}
	}
	
	private int toBinId(K k) {
		double kAsDouble = k.doubleValue();
		return (int)((kAsDouble - min) / binWidth);
	}

	private void buildRawHistogram(List<K> data) {
		for (K k : data){
			K key = (!isDiscrete) ? k : (K)Double.valueOf(floatTrimmer.format(k.doubleValue()));
			Integer oldValue = rawOccs.get(key);
			int newValue = oldValue == null ? 1 : oldValue + 1;
			rawOccs.put(key, newValue);
		}
	}
	
	private void normalizeToPercents() {
		log.debug("Normalizing histograms...");
		int percentage = 0;
		for (K k : rawOccs.keySet()){
			percentage = (int)((100.0 * rawOccs.get(k)) / observations);
			rawOccs.put(k, percentage);
		}
		for (Bin bin : bins){
			percentage = (int)((100.0 * bin.getOccurences()) / observations);
			bin.setOccurences(percentage);
		}
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
		for (int j = 0; j < binCount; j++){
			if (bins.get(j).getOccurences() > maxOcc){
				maxOcc = bins.get(j).getOccurences();
				maxBin = j;
			}
		}
		return maxBin;
	}

	public Bin getBin(int binId){
		return bins.get(binId);
	}
	
	public ArrayList<Bin> getBins(){
		return bins;
	}
	
	public Integer getRawCount(K key){
		Integer occurence = rawOccs.get(key);
		return occurence == null ? 0 : occurence;
	}


	public int getDistinctObservationsAmount(){
		return rawOccs.keySet().size();
	}

	public int getAmountOfBins() {
		return binCount;
	}
	
	public boolean isFlatEnough(){
		for (int i = 1; i < bins.size(); i++) {
			if (bins.get(i).getOccurences() - bins.get(i - 1).getOccurences() > 15){
				return false;
			}
		}
		return true;
	}


}