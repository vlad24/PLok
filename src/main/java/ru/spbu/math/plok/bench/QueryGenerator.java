package ru.spbu.math.plok.bench;

import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.qos.logback.classic.Logger;
import ru.spbu.math.plok.model.client.Distribution;
import ru.spbu.math.plok.model.client.Query;

public class QueryGenerator {
	private final static org.slf4j.Logger log = LoggerFactory.getLogger(QueryGenerator.class);
	private Distribution v;
	private int N;
	private long timeStart = -1;
	private long timeEnd = -1;
	private boolean inited = false;
	
	@Inject
	public QueryGenerator(@Named("N") int N, Distribution v){
		this.N = N;
		this.v = v;
	}
	
	public Query nextQuery(){
		if (!inited)
			throw new IllegalStateException();
		long qTimeStart = v.getRandomLong(timeStart, timeEnd);
		long qTimeEnd   = v.getRandomLong(qTimeStart, timeEnd);
		int qIndexStart = v.getRandomInt(0, N - 1);
		int qIndexEnd   = v.getRandomInt(qIndexStart, N - 1);
//		qTimeEnd = timeEnd;
//		qTimeStart = timeStart;
//		qIndexStart = N - 1;
//		qIndexEnd = N - 1;
		log.debug("QUERY:({},{},{},{}) // time_delta:{}, index_range:{}", qIndexStart, qIndexEnd, qTimeStart, qTimeEnd,
				(qTimeEnd - qTimeStart + 1) , (qIndexEnd - qIndexStart + 1));
		return new Query(qIndexStart, qIndexEnd, qTimeStart, qTimeEnd);
	}

	public void setStart(long start) {
		this.timeStart = start;
		inited = (timeEnd != -1);
	}

	public void setEnd(long end) {
		this.timeEnd = end;
		inited = (timeStart != -1);
	}


}
