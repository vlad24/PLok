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

import ch.qos.logback.classic.Level;


public class CustomDistribution extends Distribution{
	private final static Logger log = LoggerFactory.getLogger(CustomDistribution.class);
	private static final float SMALL_FLOAT = 0.000001f;
	private float[][] distribution;
	private float[][] probabilites;

	@Inject
	public CustomDistribution(@Named("V") String v) throws IOException {
		super(v);
		File matrixFile = new File(Paths.get(v).toAbsolutePath().toString());
		log.debug("Reading ;-separated matrix from {}", matrixFile.getAbsolutePath());
		BufferedReader reader = new BufferedReader(new FileReader(matrixFile));
		List<List<String>> fileData = getFileData(reader);
		log.debug("File data got : {} ", fileData);
		if (!fileData.isEmpty()){
			distribution = buildDistributionMatrix(fileData);
			probabilites = buildProbabilitiesMatrix(distribution);
		}else{
			throw new IllegalArgumentException("Empty file got!");
		}
		log.debug("Custom distribution successfully constructed");
		log.debug("Distributions: {} ", getDistributionMatrix());
		log.debug("Probabilities: {} ", getProbabilitiesMatrix());
	}

	private float[][] buildDistributionMatrix(List<List<String>> fileData) {
		int rowAmount = fileData.size();
		float[][] distributionMatrix = new float[rowAmount][rowAmount];
		for (int i = 0; i < rowAmount; i++) {
			List<String> row = fileData.get(i);
			if (row.size() != rowAmount)
				throw new IllegalArgumentException("Error at line " + i + " : row size != row amount");
			distributionMatrix[i] = new float[rowAmount];
			for (int j = 0; j < rowAmount; j++){
				distributionMatrix[i][j] = Float.parseFloat(row.get(j));
			}
		}
		validateConsistency(distributionMatrix);
		return distributionMatrix;
	}
	
	private float[][] buildProbabilitiesMatrix(float[][] distributionMatrix){
		for (int i = 0; i < distributionMatrix.length; i++){
			for (int j = 0; j < distributionMatrix.length; j++){
				if (i == 0 && j == 0){
					probabilites[i][j] = distributionMatrix[i][j];  
				}else if (i == 0){
					probabilites[i][j] = distributionMatrix[i][j] - distributionMatrix[i][j - 1];
				}else if (j == 0){
					probabilites[i][j] = distributionMatrix[i][j] - distributionMatrix[i - 1][j];
				}else{
					probabilites[i][j] = distributionMatrix[i][j] - distributionMatrix[i - 1][j] - distributionMatrix[i][j - 1] +  distributionMatrix[i - 1][j - 1];  
				}
				if (Math.abs(probabilites[i][j]) < SMALL_FLOAT)
					probabilites[i][j] = 0f;
			}
		}
		return probabilites;
	}

	private void validateConsistency(float[][] distrMatrix) {
		if (Math.abs(1 - distrMatrix[distrMatrix.length - 1][distrMatrix.length - 1]) > SMALL_FLOAT)
			throw new IllegalArgumentException("Last-last element is not equalt to one!");
		for (int i = 0; i < distrMatrix.length - 1 ; i++){
			for (int j = 0; j < distrMatrix.length - 1 ; j++){
				if (distrMatrix[i][j] > distrMatrix[i + 1][j] || distrMatrix[i][j] > distrMatrix[i][j + 1])
					throw new IllegalArgumentException("Error at " + i + " " + j + " matrix element. Increasing law broken.");
				if (distrMatrix[i][j] < 0)
					throw new IllegalArgumentException("Error at " + i + " " + j + " matrix element. Negative element.");
			}
		}


	}

	private List<List<String>> getFileData(BufferedReader reader) throws IOException {
		String line;
		List<List<String>> fileData = new ArrayList<>();
		while ((line = reader.readLine()) != null){
			String[] row = line.replace(" ", "").split(";");
			fileData.add(Arrays.asList(row));
		}
		return fileData;
	}

	@Override
	public long getRandomLong(long from, long to) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRandomInt(int from, int to) {
		// TODO Auto-generated method stub
		return 0;
	}

	public float[][] getDistributionMatrix() {
		return distribution;
	}

	public float[][] getProbabilitiesMatrix() {
		return probabilites;
	}

}
