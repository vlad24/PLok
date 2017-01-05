package ru.spbu.math.plok.model.client;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import ru.spbu.math.plok.bench.QueryGenerator;
import ru.spbu.math.plok.model.storagesystem.StorageSystem;

public class Client{
	private static Logger log = LoggerFactory.getLogger(Client.class);

	private final long queriesCount;			// A
	private long madeQueries;
	private QueryGenerator queryGenerator;


	@Override
	public String toString() {
		return "CLIENT_[" + queriesCount + "q]";
	}

	@Inject
	public Client(@Named("A")int queriesCount, Provider<QueryGenerator> provider){
		this.queriesCount = queriesCount;
		this.madeQueries = 0;
		this.queryGenerator = provider.get();
	}

	public HashMap<String, Object> attack(StorageSystem store){
		log.debug("Stating quering {} queries from client", queriesCount);
		try{
			while (madeQueries <= queriesCount){
				Query q = queryGenerator.nextQuery();
				store.serve(q);
				madeQueries++;
			}
			log.debug("Client {} is over");
		}catch(Exception er){
			log.error("Client unexpectedly finished!", er);
		}
		return store.getStatistics();
	}

	public void setQueryTimeBounds(long start, long end) {
		queryGenerator.setStart(start);
		queryGenerator.setEnd(end);
		
	}




}
