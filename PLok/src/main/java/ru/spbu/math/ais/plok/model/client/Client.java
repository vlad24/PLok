package ru.spbu.math.ais.plok.model.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ru.spbu.math.ais.plok.NamedProps;
import ru.spbu.math.ais.plok.bench.QueryGenerator;
import ru.spbu.math.ais.plok.model.storagesystem.StorageSystem;
import ru.spbu.math.ais.plok.solvers.histogramsolver.UserChoice.Policy;

public class Client{
	private static Logger log = LoggerFactory.getLogger(Client.class);

	private int vectorSize;						// N

	@Override
	public String toString() {
		return "CLIENT_[" + vectorSize + " lengthed vectors]";
	}

	@Inject
	public Client(@Named(NamedProps.N) int N){
		this.vectorSize   = N;
	}

	public Map<String, Object> attack(StorageSystem store, Policy iPolicy, Policy jPolicy, Map<String, Object> policyParams, int queriesCount, Long timeStep){
		log.debug("Stating performing {} self-made queries", queriesCount);
		QueryGenerator queryGenerator = new QueryGenerator(vectorSize, iPolicy, jPolicy, policyParams);
		try{
			long time = 0;
			int madeQueries = 0;
			while(madeQueries <= queriesCount) {
				time += timeStep;
				Query nextQ = queryGenerator.nextQuery(time);
				log.trace("Query: {}", nextQ.toString());
				store.serve(nextQ);
				madeQueries++;
			}
			return store.getStatistics();
		}catch(Exception er){
			log.error("Client unexpectedly finished!", er);
			HashMap<String, Object> errorReport = new HashMap<>();
			errorReport.put("error", er);
			return errorReport;
		}
	}
	
	public Map<String, Object> attack(StorageSystem store, List<Query> queries){
		log.debug("Stating performing {} queries from file", queries.size());
		try{
			for (Query q : queries){
				store.serve(q);
			}
			return store.getStatistics();
		}catch(Exception er){
			log.error("Client unexpectedly finished!", er);
			HashMap<String, Object> errorReport = new HashMap<>();
			errorReport.put("error", er);
			return errorReport;
		}
	}
	

}
