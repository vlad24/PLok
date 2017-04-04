package ru.spbu.math.plok.bench;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

import ru.spbu.math.plok.model.client.Query;
import ru.spbu.math.plok.solvers.Solver;
import ru.spbu.math.plok.solvers.histogramsolver.UserChoice.Policy;
import ru.spbu.math.plok.utils.Stat;

public class QueryGenerator {
	private final static long TIME_UNKNOWN = 0L;

	private final static org.slf4j.Logger log = LoggerFactory.getLogger(QueryGenerator.class);

	private int N;
	private boolean imitaing;
	private long timeStart = Long.MIN_VALUE;
	private long yielded = 0;
	private ArrayList<Query> userQueriesCopy;
	private HashMap<String, Object> genParams;


	public QueryGenerator(int N, HashMap<String, Object> genParams){
		this.N = N;
		this.genParams = genParams;
		imitaing = false;
	}

	public QueryGenerator(ArrayList<Query> userQueries) {
		this.userQueriesCopy = userQueries;
		imitaing = true;
	}

	public Query nextQuery(long time) throws IllegalAccessException{
		if (isConsistent()){
			Query result;
			if (imitaing ){
				Query q = userQueriesCopy.get((int) (yielded++ % userQueriesCopy.size()));
				result = new Query(TIME_UNKNOWN, q.getI1(), q.getI2(), q.getJ1(), q.getJ2());
			}else{
				int i1 = 0;
				int i2 = N - 1;
				long j1 = 0;
				long j2 = time;
				Policy iPolicy = (Policy) genParams.get(Solver.I_POLICY_KEY);
				Policy jPolicy = (Policy) genParams.get(Solver.J_POLICY_KEY);
				if (iPolicy == Policy.FULL_TRACKING){
					i1 = (int) Stat.getRandomUniform(0,  N - 1);
					i2 = (int) Stat.getRandomUniform(i1, N - 1);
				}else if (iPolicy == Policy.HOT_RANGES) {
					i1 = (int) Stat.getRandomUniform(0,  N - 1);
					i2 = (int) Stat.getRandomUniform(i1, N - 1);
				}
				if (jPolicy == Policy.FULL_TRACKING){
					j1 = Stat.getRandomUniform(timeStart, time);
					j2 = Stat.getRandomUniform(j1,        time);
				}else if (jPolicy == Policy.RECENT_TRACKING){
					Long w = (Long) genParams.get(Solver.J_POLICY_RT_WINDOW_KEY);
					j1 = Stat.getRandomUniform(Math.max(0, time - w), time) ;
					j2 = time;
				}
				yielded++;
				//i1 = N-3; i2 = N; j1 = 0; j2 = T/2;
				log.debug("QUERY:({},{},{},{}) // time_delta:{}, index_range:{}", i1, i2, j1, j2, (j2 - j1 + 1) , (i2 - i1 + 1));
				result = new Query(time, i1, i2, j1, j2);
			}
			return result;
		}else{
			throw new IllegalAccessException("Query Generator is not correctly set");
		}
	}

	public void setStart(long start) {
		this.timeStart = start;
	}

	public boolean isConsistent() {
		return (timeStart != Long.MIN_VALUE);
	}


}
