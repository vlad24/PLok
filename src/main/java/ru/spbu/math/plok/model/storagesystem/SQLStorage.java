package ru.spbu.math.plok.model.storagesystem;

import java.util.HashMap;
import java.util.List;

import ru.spbu.math.plok.model.client.Query;
import ru.spbu.math.plok.model.generator.Vector;

public class SQLStorage implements StorageSystem{

	@Override
	public List<Block> serve(Query query) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void put(Vector vector) {
		throw new UnsupportedOperationException();
	}

	@Override
	public HashMap<String, Object> getStatistics() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getBlockCount() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNextId(Block block) {
		throw new UnsupportedOperationException();
	}


}
