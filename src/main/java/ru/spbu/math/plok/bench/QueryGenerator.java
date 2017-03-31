package ru.spbu.math.plok.bench;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

import ru.spbu.math.plok.model.client.Query;
import ru.spbu.math.plok.solvers.histogramsolver.UserChoice.Policy;
import ru.spbu.math.plok.utils.Stat;

public class QueryGenerator {
	
	private static long TIME_UNKNOWN = 0L;
	
	private final static org.slf4j.Logger log = LoggerFactory.getLogger(QueryGenerator.class);
	private int N;
	private boolean inited = false;
	private boolean imitaing = false;
	private long yielded = 0;
	private ArrayList<Query> userQueriesCopy;
	private HashMap<String, Object> solution;

	private long timeStart;

	private long timeEnd;

	public QueryGenerator(int N, HashMap<String, Object> solution){
		this.N = N;
		this.solution = solution;
		imitaing = false;
	}

	public QueryGenerator(ArrayList<Query> userQueries) {
		this.userQueriesCopy = userQueries;
		imitaing = true;
	}
	
	public Query nextQuery(){
		if (imitaing){
			Query q = userQueriesCopy.get((int) (yielded++ % userQueriesCopy.size()));
			return new Query(TIME_UNKNOWN, q.getI1(), q.getI2(), q.getJ1(), q.getJ2());
		}else{
			long T = Stat.getRandomUniform(timeStart, timeEnd);
			int i1 = 0;
			int i2 = N - 1;
			long j1 = 0;
			long j2 = T;
			Policy iPolicy = (Policy) solution.get("iPolicy");
			Policy jPolicy = (Policy) solution.get("jPolicy");
			if (iPolicy == Policy.FullTrack){
				i1 = (int) Stat.getRandomUniform(0, N - 1);
				i2   = (int) Stat.getRandomUniform(i1, N - 1);
			}else if (iPolicy == Policy.RangeInterest) {
				i1 = (int) Stat.getRandomUniform(0, N - 1);
				i2   = (int) Stat.getRandomUniform(i1, N - 1);
			}
			if (jPolicy == Policy.FullTrack){
				j1   = Stat.getRandomUniform(0, T);
				j2   = Stat.getRandomUniform(j1, T);
			}else if (jPolicy == Policy.LateTracking){
				Long c = (Long) solution.get("jPolicy_last_tracking_c");
				j1 = Stat.getRandomUniform(Math.max(0, T - c), T) ;
				j2 = T;
			}
			//
			//i1 = N-3; i2 = N; j1 = 0; j2 = T/2;
			//
			log.debug("QUERY:({},{},{},{}) // time_delta:{}, index_range:{}", i1, i2, j1, j2, (j2 - j1 + 1) , (i2 - i1 + 1));
			return new Query(T, i1, i2, j1, j2);
		}
	}
	
	public void setStart(long start) {
		this.timeStart = start;
	}

	public void setEnd(long end) {
		this.timeEnd = end;
	}


}
