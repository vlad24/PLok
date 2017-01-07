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
	private static final float SMALL_FLOAT = 0.0000001f;
	private float[][] matrix;
	
	@Inject
	public CustomDistribution(@Named("V") String v) throws IOException {
		super(v);
		File matrixFile = new File(Paths.get(v).toAbsolutePath().toString());
		log.debug("Reading ;-separated matrix from {}", matrixFile.getAbsolutePath());
		BufferedReader reader = new BufferedReader(new FileReader(matrixFile));
		List<List<String>> fileData = getFileData(reader);
		log.trace("File data got : {} ", fileData);
		if (!fileData.isEmpty()){
			matrix = new float[fileData.size()][];
			toFloatDistributionMatrix(fileData);
		}else{
			throw new IllegalArgumentException("Empty file got!");
		}
		log.debug("Custom distribution successfully constructed");
	}

	private void toFloatDistributionMatrix(List<List<String>> fileData) {
		int rowAmount = fileData.size();
		for (int i = 0; i < rowAmount; i++) {
			List<String> row = fileData.get(i);
			if (row.size() != rowAmount)
				throw new IllegalArgumentException("Error at line " + i + " : row size != row amount");
			matrix[i] = new float[rowAmount];
			for (int j = 0; j < row.size(); j++){
				matrix[i][j] = Float.parseFloat(row.get(j));
			}
		}
		validateConsistency(matrix);
	}

	private void validateConsistency(float[][] distrMatrix) {
		if (Math.abs(distrMatrix[0][0]) > SMALL_FLOAT)
			throw new IllegalArgumentException("Zero-zero element is not equal to zero!");
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

	public float[][] getMatrix() {
		return matrix;
	}

}
