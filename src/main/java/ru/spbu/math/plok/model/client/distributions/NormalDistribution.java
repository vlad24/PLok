package ru.spbu.math.plok.model.client.distributions;

public class NormalDistribution extends Distribution{

	public NormalDistribution(String v) {
		super(v);
	}

	@Override
	public long getRandomP(long from, long to) {
		return 0;
	}

	@Override
	public int getRandomL(int from, int to) {
		return 0;
	}

}
