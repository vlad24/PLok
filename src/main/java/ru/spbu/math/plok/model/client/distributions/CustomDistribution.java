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
	private float[][] pdfMatrix;

	@Inject
	public CustomDistribution(@Named("V") String v) throws IOException {
		super(v);
		File matrixFile = new File(Paths.get(v).toAbsolutePath().toString());
		log.debug("Reading ;-separated matrix from {}", matrixFile.getAbsolutePath());
		BufferedReader reader = new BufferedReader(new FileReader(matrixFile));
		List<List<String>> fileData = getFileData(reader);
		log.debug("File data got : {} ", fileData);
		if (!fileData.isEmpty()){
			pdfMatrix = buildPsMatrix(fileData);
		}else{
			throw new IllegalArgumentException("Empty file got!");
		}
		log.debug("Custom distribution successfully constructed");
		log.debug("Probabilities: {} ", getProbabilities());
	}

	private float[][] buildPsMatrix(List<List<String>> fileData) {
		int rowAmount = fileData.size();
		float[][] psMatrix = new float[rowAmount][rowAmount];
		for (int i = 0; i < rowAmount; i++) {
			List<String> row = fileData.get(i);
			if (row.size() != rowAmount)
				throw new IllegalArgumentException("Error at line " + i + " : row size != row amount");
			psMatrix[i] = new float[rowAmount];
			for (int j = 0; j < rowAmount; j++){
				psMatrix[i][j] = Float.parseFloat(row.get(j));
			}
		}
		validateProbabilitiesConsistency(psMatrix);
		return psMatrix;
	}
	
	private void validateProbabilitiesConsistency(float[][] ps) {
		float sum = 0;
		for (int i = 0; i < ps.length - 1 ; i++){
			for (int j = 0; j < ps.length - 1 ; j++){
				if (1 + SMALL_FLOAT < ps[i][j])
					throw new IllegalArgumentException("Error at " + i + " " + j + " matrix element. More than 1.");
				if (ps[i][j] < 0)
					throw new IllegalArgumentException("Error at " + i + " " + j + " matrix element. Less than 0.");
				sum += ps[i][j];
			}
		}
		if (Math.abs(1 - sum) > SMALL_FLOAT)
			throw new IllegalArgumentException("Sum of matrix elements not equal to 1.");
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

	public float[][] getProbabilities() {
		return pdfMatrix;
	}

}
