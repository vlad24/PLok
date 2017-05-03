package ru.spbu.math.ais.plok.model.store;

import java.util.List;
import java.util.Map;

import ru.spbu.math.ais.plok.model.client.Query;
import ru.spbu.math.ais.plok.model.generator.Vector;

public interface StorageSystem {
	
	public int getNextId(Block block);

	public List<Block> serve(Query query) throws Exception;

	public void put(Vector vector) throws Exception;
	
	public int getBlockCount();
	
	public Map<String, Object> getStatistics();
	
	public void printState(); 

}
