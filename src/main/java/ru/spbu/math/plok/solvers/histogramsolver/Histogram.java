package ru.spbu.math.plok.solvers.histogramsolver;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spbu.math.plok.utils.Pair;
import ru.spbu.math.plok.utils.Triplet;

/**
 * @author vlad
 * Histogram helper class
 */
class Histogram<K extends Number>{
	private static final double SMALL_VALUE = 0.001;
	private static final double DISCRETE_BIN_OFFSET = 0.5;
	private static final double CONTINUOUS_BIN_OFFSET = SMALL_VALUE;

	private final static Logger log = LoggerFactory.getLogger(Histogram.class);

	static enum ValueType{
		PERCENTAGE, RAW
	}


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
	private TreeMap<K,Double> rawOccs;
	private ValueType valueType = ValueType.RAW;


	public Histogram(String name, List<K> data, K min, K max) {
		super();
		this.isDiscrete   = !(min instanceof Double || min instanceof Float);  
		this.name         = name;
		this.min          = min.doubleValue();
		this.max          = max.doubleValue();
		this.observations = data.size();
		this.rawOccs      = new TreeMap<K, Double>();
		this.bins         = new ArrayList<>();
		log.debug("Constructing {} for data: {}", name, data);
		buildRawHistogram(data);
		buildEquiWidthBinHistogram(data);
		log.debug("Raw histograms built");
	}

	private void buildEquiWidthBinHistogram(List<K> data) {
		binOffset = 0;
		if (isDiscrete){
			binOffset = DISCRETE_BIN_OFFSET;
			int distincts = new HashSet<K>(data).size();
			this.binWidth = Math.max(1, (max - min) / Math.ceil(Math.sqrt(2 + distincts)));
			this.binCount = (int) Math.ceil((max - min + 1) / binWidth);
			log.debug("Distinct discrete values: {} ", distincts);
		}else{
			binOffset = CONTINUOUS_BIN_OFFSET;
			this.binCount = (int) Math.sqrt(data.size()); 
			this.binWidth = SMALL_VALUE + (this.max - this.min) / binCount;
		}
		log.debug("Min Max  :    {} {} ", min, max);
		log.debug("Bin width:       {} ", binWidth);
		log.debug("Bin count:       {} ", binCount);
		for (int i = 0; i < binCount; i++){
			double left  = -binOffset + min + i * binWidth;
			double right = left + binWidth;
			if (left > max + SMALL_VALUE){
				binCount = bins.size();
				break;
			}
			bins.add(new Bin(i, left, right, 0));
			log.debug("{}th bin {} inited", i, bins.get(i));
		}
		for (K k : data){
			int id = binify(k);
			Bin targetBin = bins.get(id);
			assert (targetBin.getLeft() <= k.doubleValue()) && (k.doubleValue() <= targetBin.getRight());
			targetBin.incrementValue();
		}
		log.debug("All bins have been filled");
	}
	
	private void buildRawHistogram(List<K> data) {
		for (K k : data){
			@SuppressWarnings("unchecked")
			K key = (isDiscrete)?  k : (K)Double.valueOf(floatTrimmer.format(k.doubleValue()));
			Double oldValue = rawOccs.get(key);
			rawOccs.put(key, (oldValue == null)? 1 : oldValue + 1);
		}
	}
	
	public int getAmountOfBins() {
		return binCount;
	}

	public int getLastNonZeroBin() {
		int result = 0;
		for (Bin bin : bins){
			if (bin.getValue() > SMALL_VALUE){
				result = bin.getId();
			}
		}
		return result;
	}
	
	public double[] getLeftDerivatives(){
		double[] leftDers  = new double[binCount];
		leftDers[0]             = SMALL_VALUE;
		for (int i = 1; i < binCount; i++){
			leftDers[i]  = (bins.get(i).getValue() - bins.get(i - 1).getValue()) / binWidth;
		}
		log.debug(Arrays.toString(leftDers));
		return leftDers;
	}
	
	public double[] getRightDerivatives(){
		double[] rightDers = new double[binCount];
		for (int i = 0; i < binCount - 1; i++){
			rightDers[i] = (bins.get(i).getValue() - bins.get(i + 1).getValue()) / binWidth;
		}
		rightDers[binCount - 1] = SMALL_VALUE;
		log.debug(Arrays.toString(rightDers));
		return rightDers;
	}
	
	public List<Bin> getLocalMaximas(){
		double[] leftDer  = getLeftDerivatives();
		double[] rightDer = getRightDerivatives();
		List<Bin> maximas = new ArrayList<>(binCount);
		for (Bin bin : bins){
			if (leftDer[bin.getId()] > 0 && rightDer[bin.getId()] > 0){
				maximas.add(bin);
			}
		}
		return maximas;
		
	}
	
	public List<Triplet<Integer>> getIslands(){
		double[] leftDer  = getLeftDerivatives();
		List<Triplet<Integer>> islands = new ArrayList<Triplet<Integer>>();
		int beg = 0;
		int end = 0;
		int cur = beg;
		int top = 0;
		while (cur < bins.size()) {
			beg = cur;
			while (cur < leftDer.length && leftDer[cur] > 0 ){
				cur++;
			}
			top = cur - 1;
			while (cur < leftDer.length && leftDer[cur] <= 0){
				cur++;
			}
			end = cur - 1;
			//validate island
			islands.add(new Triplet<Integer>(beg, top,  end));
		}
		return islands;
	}
	
	public Bin getBin(int binId){
		return bins.get(binId);
	}
	
	public List<Bin> getBins(){
		return bins;
	}
	
	public int getDistinctObservationsAmount(){
		return rawOccs.keySet().size();
	}

	
	//Raw data
	
	public Bin getMaxBin(){
		double maxOcc = Double.MIN_VALUE;
		Bin maxBin = null;
		for (Bin bin : bins){
			if (Double.compare(bin.getValue(), maxOcc) > 0){
				maxOcc = bin.getValue();
				maxBin = bin;
			}
		}
		return maxBin;
	}
	
	public K getMaxRaw(){
		K result = null;
		double maxOccurence = Double.MIN_VALUE;
		for (Map.Entry<K, Double> entry : rawOccs.entrySet()){
			if (entry.getValue() >= maxOccurence){
				maxOccurence = entry.getValue();
				result = entry.getKey();
			}
		}
		return result;
	}
	
	public K getMaxRawForBin(int binId){
		K lefter  = rawOccs.ceilingKey((K)Double.valueOf(getBin(binId).getLeft()));
		K righter = rawOccs.floorKey((K)Double.valueOf(getBin(binId).getRight() + SMALL_VALUE));
		K result = null;
		double maxOccurence = Double.MIN_VALUE;
		for (Map.Entry<K, Double> entry : rawOccs.subMap(lefter, righter).entrySet()){
			if (entry.getValue() >= maxOccurence){
				maxOccurence = entry.getValue();
				result = entry.getKey();
			}
		}
		return result;
	}
	
	
	public double getRawValue(K key){
		Double occurence = rawOccs.get(key);
		return occurence == null ? 0 : occurence;
	}

	public void normalizeToPercents() {
		log.debug("Normalizing {} to percents", name);
		double percentage = 0;
		for (K k : rawOccs.keySet()){
			percentage = (100.0 * rawOccs.get(k)) / observations;
			rawOccs.put(k, percentage);
		}
		log.debug("Raw representation of {} normalized", name);
		for (Bin bin : bins){
			percentage = (100.0 * bin.getValue()) / observations;
			bin.setValue(percentage);
		}
		log.debug("Binned representation of {} normalized", name);
		this.valueType = ValueType.PERCENTAGE;
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
			if (bin.getValue() < 1){
				continue;
			}
			result.append(bin.toString())
			.append(valueType == ValueType.PERCENTAGE ? "%" : "pcs")
			.append(":\t\t|");
			for (int j = 1; j <= bin.getValue(); j++){
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
		for (Map.Entry<K, Double> entry : rawOccs.entrySet()){
			if (entry.getValue() < 1){
				continue;
			}
			result.append(entry.getKey())
			.append("[").append(entry.getValue()).append("]")
			.append(valueType == ValueType.PERCENTAGE ? "%" : "pcs")
			.append(":\t\t|");
			for (int i = 1; i <= entry.getValue(); i++){
				result.append("=");
			}
			result.append("\n");
		}
		return result.toString();
	}



}