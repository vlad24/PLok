package ru.spbu.math.plok.model.client;

public class Query {

	int indexStart;
	int indexEnd;
	long timeStart;
	long timeEnd;
	
	public Query(int indexStart, int indexEnd, long timeStart, long timeEnd) {
		super();
		this.indexStart = indexStart;
		this.indexEnd = indexEnd;
		this.timeStart = timeStart;
		this.timeEnd = timeEnd;
	}

	public int getIndexStart() {
		return indexStart;
	}

	public void setIndexStart(int indexStart) {
		this.indexStart = indexStart;
	}

	public int getIndexEnd() {
		return indexEnd;
	}

	public void setIndexEnd(int indexEnd) {
		this.indexEnd = indexEnd;
	}

	public long getTimeEnd() {
		return timeEnd;
	}

	public void setTimeEnd(long timeEnd) {
		this.timeEnd = timeEnd;
	}
	
	public long getTimeStart() {
		return timeStart;
	}

	public void setTimeStart(long timeStart) {
		this.timeStart = timeStart;
	}
	

}
