package ru.spbu.math.ais.plok.bench;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import ru.spbu.math.ais.plok.MapKeyNames;
import ru.spbu.math.ais.plok.model.client.Query;
import ru.spbu.math.ais.plok.solvers.histogramsolver.UserChoice.Policy;
import ru.spbu.math.ais.plok.utils.Stat;

public class QueryGenerator {
	private final static org.slf4j.Logger log = LoggerFactory.getLogger(QueryGenerator.class);

	private int N;
	private long timeStart = Long.MIN_VALUE;
	private Map<String, Object> policiesParams;
	private Policy jPolicy;
	private Policy iPolicy;


	public QueryGenerator(int N, Policy iPolicy, Policy jPolicy, Map<String, Object> policyParams){
		this.N = N;
		this.iPolicy = iPolicy;
		this.jPolicy = jPolicy;
		this.policiesParams = policyParams;
	}


	public Query nextQuery(long time) throws IllegalAccessException{
		Query result;
		int i1 = 0;
		int i2 = N - 1;
		long j1 = 0;
		long j2 = time;
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
			Long w = (Long) policiesParams.get(MapKeyNames.J_POLICY_RT_WINDOW_KEY);
			assert (w != null); 
			j2 = time;
			j1 = Math.max(0, j2 - w + 1 - Stat.getRandomUniform(0, 2));
		}
		//i1 = N-3; i2 = N; j1 = 0; j2 = T/2;
		result = new Query(time, i1, i2, j1, j2);
		log.debug("{} // time_delta:{}, index_range:{}", result, (j2 - j1 + 1) , (i2 - i1 + 1));
		return result;
	}

}
