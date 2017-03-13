package ru.spbu.math.plok.model.client;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ru.spbu.math.plok.bench.QueryGenerator;
import ru.spbu.math.plok.model.storagesystem.StorageSystem;
import ru.spbu.math.plok.solver.BasicSolver.UserQuery;

public class Client{
	private static Logger log = LoggerFactory.getLogger(Client.class);

	private final long queriesCount;			// A
	private long madeQueries;


	@Override
	public String toString() {
		return "CLIENT_[" + queriesCount + "q]";
	}

	@Inject
	public Client(@Named("A")int queriesCount){
		this.queriesCount = queriesCount;
		this.madeQueries = 0;
	}

	public HashMap<String, Object> attack(StorageSystem store, HashMap<String, Object> details,  boolean duplicateHistory){
		log.debug("Stating quering {} queries from client", queriesCount);
		QueryGenerator queryGenerator = null;
		if (duplicateHistory){
			queryGenerator = new QueryGenerator((ArrayList<UserQuery>) details.get("queries"));
		}else{
			
		}
		try{
			while (madeQueries <= queriesCount){
				Query q = queryGenerator.nextQuery();
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
