package ru.spbu.math.ais.plok.bench;

import java.util.Map;

import org.slf4j.LoggerFactory;

import ru.spbu.math.ais.plok.MapKeyNames;
import ru.spbu.math.ais.plok.model.client.Query;
import ru.spbu.math.ais.plok.solvers.histogramsolver.UserChoice.Policy;
import ru.spbu.math.ais.plok.utils.Stat;

public class QueryGenerator {
	private static final double HOT_RANGE_CHOICE_THRESHOLD = 0.85;

	private final static org.slf4j.Logger log = LoggerFactory.getLogger(QueryGenerator.class);

	private int N;
	private long timeStart = 0L;
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
			int length = Stat.getRandomUniform(1, N);
			i1 = Stat.getRandomUniform(0, N - length);
			i2 = i1 + length;
		}else if (iPolicy == Policy.HOT_RANGES) {
			if (Double.compare(Stat.getRandomUniformFloat(), HOT_RANGE_CHOICE_THRESHOLD) < 0){
				log.trace("Generating index range based on hot ranges");
				String hrs = (String) policiesParams.get(MapKeyNames.I_POLICY_HR_RANGES_KEY);
				String[] ranges = hrs.split(",");
				String range = ranges[(int) Stat.getRandomUniform(0, ranges.length)];
				log.trace("Chosen range: {}", range);
				String[] points = range.split("-");
				i1 = (int)((Float.parseFloat(points[0]) / 100.0) * (N - 1)); 
				i2 = (int)((Float.parseFloat(points[1]) / 100.0) * (N - 1)); 
			}else{
				log.trace("Hot ranges ignored");
				i1 = (int) Stat.getRandomUniform(0,  N - 1);
				i2 = (int) Stat.getRandomUniform(i1, N - 1);
			}
		}
		if (jPolicy == Policy.FULL_TRACKING){
			long length = Stat.getRandomUniform(1, time);
			j1 = Stat.getRandomUniform(timeStart, timeStart + time - length);
			j2 = j1 + length;
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
