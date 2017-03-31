package ru.spbu.math.plok.model.storagesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import ru.spbu.math.plok.model.client.Query;
import ru.spbu.math.plok.model.generator.Vector;

public class PLokerStorage implements StorageSystem{

	private final static Logger log = LoggerFactory.getLogger(PLokerStorage.class);
	
	private final LoadingCache<Integer, Block> cache;
	private final FilePersistentStorage storage;
	private Index index;
	protected long blocksReadFromDisk;
	protected long blocksReadInTotal;

	private List<Block> currentCommonBlocks;
	private Block currentSpecial;
	private AtomicInteger nextId;

	private int N = -1; 
	private int P = -1; 
	private int L = -1; 
	private int L_S;
	private int P_S;


	@Inject
	public PLokerStorage(@Named("N")int N,  @Named("P")int P,  @Named("L")int L, @Named("cacheUnitSize") int cacheSizeInUnits, Provider<Index> indexProvider, Provider<FilePersistentStorage> persStorage) {
		super();
		storage = persStorage.get();
		//TODO specify in which units cache size is calculated
		cache = CacheBuilder.newBuilder()
				.maximumSize(cacheSizeInUnits)
				.build(new CacheLoader<Integer, Block>() {
					@Override
					public Block load(Integer key) throws Exception {
						blocksReadFromDisk++;
						return readFromDisk(key);
					}
				});
		index = indexProvider.get();
		this.P = P;
		this.L = L;
		this.N = N;
		this.L_S = N % L;
		this.P_S = (L_S != 0)? (P * L / L_S) : 0;
		currentCommonBlocks = new ArrayList<>(N / L);
		refreshCommonColumn();
		currentSpecial = new Block(P_S, L_S);
		nextId = new AtomicInteger(0);
		log.info("Inited Ploker storage with P={}, L={}, P_S={}, L_S={}", P, L, P_S, L_S);
	}
	
	private void refreshCommonColumn() {
		currentCommonBlocks.clear();
		for (int i = 0; i < N / L; i++){
			currentCommonBlocks.add(new Block(P, L));
		}
	}

	protected Block readFromDisk(long key) throws IOException {
		return this.storage.get(key);
	}

	public void put(Vector vector) throws IOException {
		putCommonPart(vector);
		putSpecialPart(vector);
	}

	private void putSpecialPart(Vector vector) throws IOException {
		if (L_S != 0){
			if (currentSpecial.tryAdd(vector.cutCopy(vector.getLength() - L_S, vector.getLength() - 1))){
				currentSpecial.autoFillHeader(getNextId(currentSpecial), vector.getLength() - L_S);
				index.put(currentSpecial);
				storage.add(currentSpecial);
				currentSpecial = new Block(P_S, L_S);
			}
		}
	}

	private void putCommonPart(Vector vector) throws IOException {
		boolean commonBlocksFilled = false;
		for (int i = 0; i < N / L; i++){
			int up = L * i;
			int down = L * (i + 1) - 1;
			Block block = currentCommonBlocks.get(i);
			if (block.tryAdd(vector.cutCopy(up, down))){
				commonBlocksFilled = true;
				block.autoFillHeader(getNextId(block), up);
				index.put(block);
				storage.add(block);
			}
		}
		if (commonBlocksFilled){
			refreshCommonColumn();
		}
	}

	public List<Block> serve(Query q) throws Exception{
		List<Integer> ids = index.get(q.getJ1(), q.getJ2(), q.getI1(), q.getI2());
		List<Block> resultBlocks = new ArrayList<>(); 
		for (Integer id: ids){
			blocksReadInTotal++;
			resultBlocks.add(cache.get(id));
		}
		return resultBlocks;		
	}

	@Override
	public HashMap<String, Object> getStatistics() {
		HashMap<String,Object> result = new HashMap<>();
		result.put("d", blocksReadFromDisk);
		result.put("Q", blocksReadInTotal);
		result.put("d/Q", 100 * (float)blocksReadFromDisk / blocksReadInTotal);
		return result;
	}
	
	@Override
	public int getBlockCount() {
		return index.getBlockCount();
	}
	
	@Override
	public int getNextId(Block block) {
		return nextId.getAndIncrement();
	}

}
