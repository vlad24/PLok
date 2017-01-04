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
	private long start = -1;
	private long end = -1;
	private boolean inited = false;
	
	@Inject
	public QueryGenerator(@Named("N") int N, Distribution v){
		this.N = N;
		this.v = v;
	}
	
	public Query nextQuery(){
		if (!inited)
			throw new IllegalStateException();
		long qTimeStart = v.getRandomLong(start, end);
		long qTimeEnd   = v.getRandomLong(qTimeStart, end);
		int qIndexStart = v.getRandomInt(0, N - 1);
		int qIndexEnd   = v.getRandomInt(qIndexStart, N);
		log.debug("time_delta:{}, index_range:{}", (qTimeEnd-qTimeStart) , (qIndexEnd-qIndexStart));
		return new Query(qIndexStart, qIndexEnd, qTimeStart, qTimeEnd);
	}

	public void setStart(long start) {
		this.start = start;
		inited = (end != -1);
	}

	public void setEnd(long end) {
		this.end = end;
		inited = (start != -1);
	}


}
