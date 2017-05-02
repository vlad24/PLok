package ru.spbu.math.ais.plok.model.generator;

import java.util.Arrays;

public class Vector {
	long timestamp;
	float[] values;
	
	public Vector(long timestamp, float[] values) {
		super();
		this.timestamp = timestamp;
		this.values = values;
	}
	

	@Override
	public String toString() {
		return "Vector [timestamp=" + timestamp + ", values=" + Arrays.toString(values) + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		result = prime * result + Arrays.hashCode(values);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector other = (Vector) obj;
		if (timestamp != other.timestamp)
			return false;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public float[] getVector() {
		return values;
	}
	
	public int getLength() {
		return values.length;
	}
	
	public Vector cutCopy(int i1, int i2){
		return new Vector(timestamp, Arrays.copyOfRange(values, i1, i2 + 1));
	}
	
	
	
}
