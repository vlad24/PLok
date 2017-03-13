package ru.spbu.math.plok.bench;

import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import ru.spbu.math.plok.model.client.Query;
import ru.spbu.math.plok.solver.BasicSolver.UserQuery;
import ru.spbu.math.plok.statutils.Stat;

public class QueryGenerator {
	private final static org.slf4j.Logger log = LoggerFactory.getLogger(QueryGenerator.class);
	private int N;
	private long timeStart = -1;
	private long timeEnd = -1;
	private boolean inited = false;
	private boolean imitaing = false;
	private long yielded = 0;
	private ArrayList<UserQuery> userQueriesCopy;

	@Inject
	public QueryGenerator(int N){
		this.N = N;
		imitaing = false;
	}

	public QueryGenerator(ArrayList<UserQuery> userQueries) {
		this.userQueriesCopy = userQueries;
		imitaing = true;
	}

	public Query nextQuery(){
		if (!inited)
			throw new IllegalStateException();
		if (imitaing){
			UserQuery q = userQueriesCopy.get((int) (yielded++ % userQueriesCopy.size()));
			return new Query(q.getI1(), q.getI2(), q.getJ1(), q.getI2());
		}else{
			long qTimeStart = Stat.getRandomUniform(timeStart, timeEnd);
			long qTimeEnd   = Stat.getRandomUniform(qTimeStart, timeEnd);
			int qIndexStart = (int) Stat.getRandomUniform(0, N - 1);
			int qIndexEnd   = (int) Stat.getRandomUniform(qIndexStart, N - 1);
			//		qTimeEnd = timeEnd;
			//		qTimeStart = timeStart;
			//		qIndexStart = N - 1;
			//		qIndexEnd = N -1;
			log.debug("QUERY:({},{},{},{}) // time_delta:{}, index_range:{}", qIndexStart, qIndexEnd, qTimeStart, qTimeEnd,
					(qTimeEnd - qTimeStart + 1) , (qIndexEnd - qIndexStart + 1));
			return new Query(qIndexStart, qIndexEnd, qTimeStart, qTimeEnd);
		}
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
