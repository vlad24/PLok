package ru.spbu.math.plok.model.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ru.spbu.math.plok.NamedProps;
import ru.spbu.math.plok.bench.QueryGenerator;
import ru.spbu.math.plok.model.storagesystem.StorageSystem;
import ru.spbu.math.plok.solvers.histogramsolver.UserChoice.Policy;

public class Client{
	private static Logger log = LoggerFactory.getLogger(Client.class);

	private final long queriesCount;			// Q
	private int vectorSize;						// N
	private long madeQueries;

	@Override
	public String toString() {
		return "CLIENT_[" + queriesCount + "q]";
	}

	@Inject
	public Client(@Named(NamedProps.Q)int queriesCount, @Named(NamedProps.N) int N){
		this.queriesCount = queriesCount;
		this.madeQueries  = 0;
		this.vectorSize   = N;
	}

	public HashMap<String, Object> attack(StorageSystem store, Policy iPolicy, Policy jPolicy, Map<String, Object> policyParams, Long timeStep){
		log.debug("Stating quering {} queries from client", queriesCount);
		QueryGenerator queryGenerator = new QueryGenerator(vectorSize, iPolicy, jPolicy, policyParams);
		try{
			long time = 0;
			while(madeQueries <= queriesCount) {
				time += timeStep;
				Query nextQ = queryGenerator.nextQuery(time);
				store.serve(nextQ);
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
	
	public HashMap<String, Object> attack(StorageSystem store, List<Query> queries){
		try{
			int i = 0;
			while(madeQueries < queriesCount) {
				Query nextQ = queries.get(i);
				store.serve(nextQ);
				madeQueries++;
				i++;
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
