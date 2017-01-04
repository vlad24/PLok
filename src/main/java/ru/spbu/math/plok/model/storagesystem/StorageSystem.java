package ru.spbu.math.plok.model.storagesystem;

import java.util.HashMap;
import java.util.List;

import ru.spbu.math.plok.model.client.Query;
import ru.spbu.math.plok.model.generator.Vector;

public interface StorageSystem {

	public List<Block> serve(Query query) throws Exception;

	public void put(Vector vector);
	
	public HashMap<String, Object> getStatistics();

	public int getBlockCount();

}
