package ru.spbu.math.ais.plok.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Stat {

	public enum DISTRIBUTION_TYPE{
		UNIFORM, EXPONENTIAL, NORMAL, OTHER
	}
	
	private static final float SMALL_FLOAT = 0.0001f;

	public static float[][] buildPmfMatrix(List<List<String>> fileData) {
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

	public static void validatePmfConsistency(float[][] pmfMatrix) {
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
	
	public static ArrayList<Float[]> calculateMarginals(float[][] pmfMatrix) {
		ArrayList<Float[]> result = new ArrayList<>(2);
		Float[] marginalL = new Float[pmfMatrix.length];
		Float[] marginalP = new Float[pmfMatrix[0].length];
		for (int i = 0; i < pmfMatrix.length; i++) {
			for (int j = 0; j < pmfMatrix[i].length; j++) {
				marginalL[i] += pmfMatrix[i][j];
				marginalP[j] += pmfMatrix[i][j];
			}
		}
		result.add(marginalP);
		result.add(marginalL);
		return result;
	}
	
	
	public static Float calculateCovariation(float[][] pmf){
		float expectedPL = 0;
		for (int i = 0; i < pmf.length; i++){
			for (int j = 0; j < pmf[i].length; j++){
				expectedPL += i * j * pmf[i][j];
			}
		}
		ArrayList<Float> ms = calculateMeans(pmf);
		return expectedPL - ms.get(0) * ms.get(1);
	}
	
	public static ArrayList<Float> calculateMeans(float[][] pmf) {
		ArrayList<Float> ms = new ArrayList<Float>(2);
		ArrayList<Float[]> marginals = calculateMarginals(pmf);
		Float meanP = 0f;
		Float meanL = 0f;
		for (int i = 0; i < marginals.get(0).length; i++){
			meanP += marginals.get(0)[i] * i;
		}
		for (int j = 0; j < marginals.get(1).length; j++){
			meanL += marginals.get(1)[j] * j;
		}
		return ms;
	}
	
	public static ArrayList<Float> calculateDs(float[][] pmf) {
		Float dL = 0f; 
		Float dP = 0f;
		ArrayList<Float> result = new ArrayList<>(2);
		ArrayList<Float> means = calculateMeans(pmf);
		ArrayList<Float[]> marginals = calculateMarginals(pmf);
		for (int i = 0; i < marginals.get(0).length; i++){
			dP += (float) (marginals.get(0)[i] * Math.pow(i - means.get(0), 2));
		}
		for (int i = 0; i < marginals.get(1).length; i++){
			dL += (float) (marginals.get(1)[i] * Math.pow(i - means.get(1), 2));
		}
		result.add(dP);
		result.add(dL);
		return result;
	}
	
	public static float calculateCorelation(float[][] pmf) {
		ArrayList<Float> ds = calculateDs(pmf);
		return calculateCovariation(pmf) * ds.get(0) * ds.get(1); 
	}
	
	public static long getRandomUniform(long from, long to) {
		float delta = new Random().nextFloat() * (to - from);
		return from + (long)delta;
	}


}
