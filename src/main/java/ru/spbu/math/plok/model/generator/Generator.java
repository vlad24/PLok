package ru.spbu.math.plok.model.generator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ru.spbu.math.plok.model.storagesystem.StorageSystem;
import ru.spbu.math.plok.solver.BasicSolver;

public class Generator{
	
	private static Logger log = LoggerFactory.getLogger(Generator.class);

	private int delay;
	private ArrayList<Vector> vectors;
	private int writeTime;
	private final int vectorSize;
	private Random rand;
	private volatile boolean attacking;
	
	@Override
	public String toString() {
		return "GENERATOR[ N=" + vectorSize + ", T=" + writeTime + ", p=" + delay + "...]";
	}

	@Inject
	public Generator(@Named("N")int vectorSize, @Named("p")int period, @Named("T")int writeTime) throws FileNotFoundException {
		super();
		this.delay = period;
		this.writeTime = writeTime;
		this.vectorSize = vectorSize;
		this.rand = new Random(System.currentTimeMillis());
		prepareData();
	}
	
	private void prepareData() {
		int expectedVectors = (int)(writeTime / delay);
		int needed = (expectedVectors / vectorSize) * vectorSize;
		this.vectors = new ArrayList<Vector>(expectedVectors); 
		for (int i = 0; i < needed; i++){
			vectors.add(new Vector(System.currentTimeMillis(), getRandomFloatArray(vectorSize)));
		}
		
	}

	private float[] getRandomFloatArray(int N) {
		float[] result = new float[N];
		for (int j = 0; j < N; j++){
			result[j] = rand.nextFloat();
		}
		return result;
	}

	public HashMap<String, Object> attack(StorageSystem store) {
		HashMap<String, Object> report = new HashMap<>();
		try {
			attacking = true;
			long callStart = System.currentTimeMillis();
			for (int j = 0; j < vectors.size() && attacking; j++){
				store.put(vectors.get(j));
				TimeUnit.MILLISECONDS.sleep(delay);
			}
			long callFinish = System.currentTimeMillis();
			log.debug("T is actually: {}", callFinish - callStart);
			report.put("attackStart", callStart);
			report.put("attackEnd", callFinish);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Problem {}!", e.getMessage());
			report.put("error", e.getMessage());
		}finally{
			stopAttack();
			System.out.println("Finally stored:" + store.getBlockCount() + " blocks");
		}
		return report; 
	}

	public void stopAttack(){
		attacking = false;
	}
	

}
