package ru.spbu.math.plok.model.client;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ru.spbu.math.plok.MapKeyNames;
import ru.spbu.math.plok.NamedProps;
import ru.spbu.math.plok.bench.QueryGenerator;
import ru.spbu.math.plok.model.storagesystem.StorageSystem;

public class Client{
	private static Logger log = LoggerFactory.getLogger(Client.class);

	private final long queriesCount;			// A
	private int vectorSize;						// N
	private long madeQueries;




	@Override
	public String toString() {
		return "CLIENT_[" + queriesCount + "q]";
	}

	@Inject
	public Client(@Named(NamedProps.Q)int queriesCount, @Named(NamedProps.N) int N){
		this.queriesCount = queriesCount;
		this.madeQueries = 0;
		this.vectorSize = N;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, Object> attack(StorageSystem store, HashMap<String, Object> solution, boolean imitating, Long tFrom, Long tTo){
		log.debug("Stating quering {} queries from client", queriesCount);
		QueryGenerator queryGenerator;
		if (imitating){
			queryGenerator = new QueryGenerator((ArrayList<Query>)solution.get(MapKeyNames.QUERIES_KEY));
		}else{
			queryGenerator = new QueryGenerator(vectorSize, solution);
		}
		queryGenerator.setStart(tFrom);
		try{
			long time = tFrom;
			while(madeQueries <= queriesCount) {
				time = Math.min(time + 1, tTo);
				Query q = queryGenerator.nextQuery(time);
				store.serve(q);
				madeQueries++;
				log.debug("Progress = {}%", 100.0 * (float)madeQueries/queriesCount);
			}
			log.debug("Client is over");
			return store.getStatistics();
		}catch(Exception er){
			log.error("Client unexpectedly finished!", er);
			HashMap<String, Object> errorReport = new HashMap<>();
			errorReport.put("error", er);
			return errorReport;
		}
	}
	

}
