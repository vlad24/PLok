package ru.spbu.math.plok.model.client.distributions;

import java.util.Random;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class UniformDistribution extends Distribution{
	

	private Random rand;

	@Inject
	public UniformDistribution(@Named("V") String v) {
		super(v);
		rand = new Random();
	}

	@Override
	public long getRandomP(long from, long to) {
		float delta = rand.nextFloat() * (to - from);
		return from + (long)delta;
	}

	@Override
	public int getRandomL(int from, int to) {
		return from + rand.nextInt(to - from + 1);
	}

}
