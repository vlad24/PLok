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
	private static final float SMALL_FLOAT = 0.000001f;
	private static final String OFFSETS_SEPARATOR = "/";
	private static final String ELEMENT_SEPARATOR = ";";
	private float[][] pmf;
	private int pOffset;
	private int lOffset;
	private float [] marginalP;
	private float [] marginalL;

	@Inject
	public CustomDistribution(@Named("V") String v) throws IOException {
		super(v);
		if (ELEMENT_SEPARATOR.equals(OFFSETS_SEPARATOR))
			throw new IllegalStateException("ELEMENT_SEPARATOR == OFFSETS_SEPARATOR!");
		File matrixFile = new File(Paths.get(v).toAbsolutePath().toString());
		log.debug("Reading {}-separated matrix from {}", ELEMENT_SEPARATOR, matrixFile.getAbsolutePath());
		List<List<String>> fileData = getFileData(matrixFile);
		log.debug("File data got : {} ", fileData);
		if (!fileData.isEmpty()){
			pmf = buildPmfMatrix(fileData);
			calculateMarginals(pmf);
		}else{
			throw new IllegalArgumentException("Empty file got!");
		}
		log.debug("Custom distribution successfully constructed");
		log.debug("PMF: {} ", getPmf());
		log.debug("Marginal P: {} ", getMarginalP());
		log.debug("Marginal L: {} ", getMarginalL());
		System.exit(1);
	}

	private void calculateMarginals(float[][] pmfMatrix) {
		marginalL = new float[pmfMatrix.length];
		marginalP = new float[pmfMatrix.length];
		for (int i = 0; i < pmfMatrix.length; i++) {
			for (int j = 0; j < pmfMatrix.length; j++) {
				marginalL[i] += pmfMatrix[i][j];
				marginalP[j] += pmfMatrix[i][j];
			}
		}
		
	}

	private float[][] buildPmfMatrix(List<List<String>> fileData) {
		int rowAmount = fileData.size();
		float[][] pmfMatrix = new float[rowAmount][rowAmount];
		for (int i = 0; i < rowAmount; i++) {
			List<String> row = fileData.get(i);
			if (row.size() != rowAmount)
				throw new IllegalArgumentException("Error at line " + i + " : row size != row amount");
			pmfMatrix[i] = new float[rowAmount];
			for (int j = 0; j < rowAmount; j++){
				pmfMatrix[i][j] = Float.parseFloat(row.get(j));
			}
		}
		validatePmfConsistency(pmfMatrix);
		return pmfMatrix;
	}
	

	private void validatePmfConsistency(float[][] pmfMatrix) {
		float sum = 0;
		for (int i = 0; i < pmfMatrix.length ; i++){
			for (int j = 0; j < pmfMatrix.length ; j++){
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
		try(BufferedReader reader = new BufferedReader(new FileReader(file))){
			while ((line = reader.readLine()) != null){
				if (isValidForPmfParsing(line)){
					String[] row = line.replace(" ", "").split(ELEMENT_SEPARATOR);
					fileData.add(Arrays.asList(row));
				}else if (lineNumber == 1 && checkAndParseOffets(line)){
					log.info("Initialized offsets {}, {}", pOffset, lOffset);
				}else{
					log.info("Line {} ignored: {}", lineNumber, line);
				}
				lineNumber++;
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

}
