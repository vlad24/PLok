package ru.spbu.math.plok.model.client.distributions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;


public class CustomDistribution extends Distribution{
	private final static Logger log = LoggerFactory.getLogger(CustomDistribution.class);
	private static final float SMALL_FLOAT = 0.0001f;
	private static final String OFFSETS_SEPARATOR = "/";
	private static final String ELEMENT_SEPARATOR = ",";
	private int pOffset;
	private int lOffset;
	private float[][] pmf;
	private float [] marginalP;
	private float [] marginalL;
	private float meanP; 
	private float meanL; 
	private float dL;
	private float dP;
	private float covariation;
	private float correlation; 
	

	
	@Inject
	public CustomDistribution(@Named("V") String v) throws IOException {
		super(v);
		if (ELEMENT_SEPARATOR.equals(OFFSETS_SEPARATOR))
			throw new IllegalStateException("ELEMENT_SEPARATOR == OFFSETS_SEPARATOR!");
		File matrixFile = new File(Paths.get(v).toAbsolutePath().toString());
		log.debug("Reading {}-separated matrix from {}", ELEMENT_SEPARATOR, matrixFile.getAbsolutePath());
		List<List<String>> fileData = getFileData(matrixFile);
		//log.debug("File data got : {} ", fileData);
		pmf = buildPmfMatrix(fileData);
		calculateMarginals(pmf);
		calculateMeans();
		calculateDs();
		calculateCovariation();
		calculateCorelation();
		log.debug("Custom distribution successfully constructed");
		//log.debug("PMF: {} ", getPmf());
		log.debug("Marginal P: {} ", getMarginalP());
		log.debug("Marginal L: {} ", getMarginalL());
		log.debug("Means: mL={}, mP={}", meanL, meanP);
		log.debug("Ds: dL={}, dP={}", dL, dP);
		log.debug("Covariation: cov={}", covariation);
		log.debug("Corelation: cor={}", correlation);
	}
	
	private void calculateMeans() {
		for (int i = 0; i < marginalP.length; i++){
			meanP += marginalP[i] * (pOffset + i);
		}
		for (int j = 0; j < marginalL.length; j++){
			meanL += marginalL[j] * (lOffset + j);
		}
	}
	
	private void calculateDs() {
		dL = 0; 
		dP = 0;
		for (int i = 0; i < marginalP.length; i++){
			dP += marginalP[i] * Math.pow((pOffset + i) - meanP, 2);
		}
		for (int i = 0; i < marginalL.length; i++){
			dL += marginalL[i] * Math.pow((lOffset + i) - meanL, 2);
		}
	}
	
	private void calculateMarginals(float[][] pmfMatrix) {
		marginalL = new float[pmfMatrix.length];
		marginalP = new float[pmfMatrix[0].length];
		for (int i = 0; i < pmfMatrix.length; i++) {
			for (int j = 0; j < pmfMatrix[i].length; j++) {
				marginalL[i] += pmfMatrix[i][j];
				marginalP[j] += pmfMatrix[i][j];
			}
		}
	}
	
	private void calculateCovariation(){
		float expectedPL = 0;
		for (int i = 0; i < pmf.length; i++){
			for (int j = 0; j < pmf[i].length; j++){
				expectedPL += (lOffset + i) * (pOffset + j) * pmf[i][j];
			}
		}
		covariation = expectedPL - meanL * meanP;
	}
	
	private void calculateCorelation() {
		correlation = covariation / (dL * dP); 
	}
	
	
	private float[][] buildPmfMatrix(List<List<String>> fileData) {
		if (fileData != null && !fileData.isEmpty()){
			int rowAmount = fileData.size();
			int columnAmount = fileData.get(0).size();
			float[][] pmfMatrix = new float[rowAmount][columnAmount];
			for (int i = 0; i < rowAmount; i++) {
				List<String> row = fileData.get(i);
				pmfMatrix[i] = new float[columnAmount];
				for (int j = 0; j < columnAmount; j++){
					pmfMatrix[i][j] = Float.parseFloat(row.get(j));
				}
			}
			validatePmfConsistency(pmfMatrix);
			return pmfMatrix;
		}else{
			throw new IllegalArgumentException("Empty or null file data!");
		}
	}

	private void validatePmfConsistency(float[][] pmfMatrix) {
		float sum = 0;
		for (int i = 0; i < pmfMatrix.length ; i++){
			for (int j = 0; j < pmfMatrix[i].length ; j++){
				if (1 + SMALL_FLOAT < pmfMatrix[i][j])
					throw new IllegalArgumentException("Error at " + i + " " + j + " matrix element. More than 1.");
				if (pmfMatrix[i][j] < 0)
					throw new IllegalArgumentException("Error at " + i + " " + j + " matrix element. Less than 0.");
				sum += pmfMatrix[i][j];
			}
		}
		if (Math.abs(1 - sum) > SMALL_FLOAT){
			throw new IllegalArgumentException("Sum of matrix elements not equal to 1. Instead: " + sum);
		}
	}

	private boolean checkAndParseOffets(String line){
		if (line.contains(OFFSETS_SEPARATOR)){
			String[] offsets = line.split(OFFSETS_SEPARATOR);
			if (offsets.length != 2)
				throw new IllegalArgumentException("PMF file format exception: first line is in incorrect format");
			pOffset = Integer.parseInt(offsets[0]);
			lOffset = Integer.parseInt(offsets[1]);
			return true;
		}else{
			pOffset = 0;
			lOffset = 0;
			return false;
		}
	}

	private List<List<String>> getFileData(File file) throws IOException {
		String line;
		List<List<String>> fileData = new ArrayList<>();
		int lineNumber = 1;
		int validLineNumber = 1;
		try(BufferedReader reader = new BufferedReader(new FileReader(file))){
			while ((line = reader.readLine()) != null){
				if (isValidForPmfParsing(line)){
					String[] row = line.replace(" ", "").split(ELEMENT_SEPARATOR);
					fileData.add(Arrays.asList(row));
					validLineNumber++;
				}else if (validLineNumber == 1 && checkAndParseOffets(line)){
					log.info("Initialized offsets for P ({}), and L ({}) from line {}", pOffset, lOffset, lineNumber);
					validLineNumber++;
				}else{
					log.info("Line {} ignored: {}", validLineNumber, line);
				}
			}
		}catch(Exception er){
			log.error("Error at line {}: {}", lineNumber, er);
		}
		return fileData;
	}

	private boolean isValidForPmfParsing(String line) {
		return line.contains(ELEMENT_SEPARATOR);
	}

	public float[][] getPmf() {
		return pmf;
	}

	public float [] getMarginalP() {
		return marginalP;
	}


	public float [] getMarginalL() {
		return marginalL;
	}


	@Override
	public long getRandomP(long from, long to) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRandomL(int from, int to) {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getMeanL() {
		return meanL;
	}

	public float getMeanP() {
		return meanP;
	}

}
