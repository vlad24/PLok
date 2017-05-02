package ru.spbu.math.plok.model.generator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ru.spbu.math.plok.NamedProps;
import ru.spbu.math.plok.model.storagesystem.StorageSystem;

public class Generator{
	
	private static Logger log = LoggerFactory.getLogger(Generator.class);

	private ArrayList<Vector> vectors;
	private int vectorAmount;
	private final int vectorSize;
	private Random randomizer;
	private volatile boolean isFilling;
	
	@Override
	public String toString() {
		return "GENERATOR[ N=" + vectorSize + ", V=" + vectorAmount;
	}

	@Inject
	public Generator(@Named(NamedProps.N)int vectorSize, @Named(NamedProps.V)int vectorAmount) throws FileNotFoundException {
		super();
		this.vectorSize = vectorSize;
		this.vectorAmount = vectorAmount;
		this.randomizer = new Random(System.currentTimeMillis());
		prepareData();
	}
	
	private void prepareData() {
		log.trace("Preparing {} vectors", vectorAmount);
		this.vectors = new ArrayList<Vector>(vectorAmount); 
		for (int i = 0; i < vectorAmount; i++){
			long time = i;
			float[] vectorData = new float[vectorSize];
			fillFloatArrayRandomly(vectorData);
			vectors.add(new Vector(time, vectorData));
		}
		log.trace("Prepared {} vectors", vectors .size());
		assert (vectors.size() == vectorAmount);
		assert (vectors.get(0).values.length == vectorSize);
	}

	private void fillFloatArrayRandomly(float[] array) {
		for (int j = 0; j < array.length; j++){
			array[j] = randomizer.nextFloat();
		}
	}

	public void fill(StorageSystem store) {
		log.trace("Filling store...");
		try {
			isFilling = true;
			for (int j = 0; j < vectors.size() && isFilling; j++){
				store.put(vectors.get(j));
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Problem {}!", e.getMessage());
		}finally{
			stopFilling();
		}
	}

	public void stopFilling(){
		isFilling = false;
	}
	

}
