package ru.spbu.math.plok.model.client.distributions;

public class ExponentialDistribution extends Distribution{

	public ExponentialDistribution(String v) {
		super(v);
	}

	@Override
	public long getRandomLong(long from, long to) {
		return 0;
	}

	@Override
	public int getRandomInt(int from, int to) {
		return 0;
	}

}
