package ru.spbu.math.plok.model.storagesystem;

import java.util.HashMap;
import java.util.List;

import ru.spbu.math.plok.model.client.Query;
import ru.spbu.math.plok.model.generator.Vector;

public class SQLStorage implements StorageSystem{

	@Override
	public List<Block> serve(Query query) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void put(Vector vector) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HashMap<String, Object> getStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getBlockCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNextId(Block block) {
		// TODO Auto-generated method stub
		return 0;
	}


}
