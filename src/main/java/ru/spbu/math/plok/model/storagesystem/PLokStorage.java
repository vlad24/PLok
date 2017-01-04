package ru.spbu.math.plok.model.storagesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import ru.spbu.math.plok.model.client.Query;
import ru.spbu.math.plok.model.generator.Vector;

public class PLokStorage implements StorageSystem{

	private final LoadingCache<Long, Block> cache;
	private final FilePersistentStorage storage;
	private Index index;
	protected long cacheMissCount;
	protected long requestCount;
	
	private List<Block> currentCommonBlocks;
	private Block currentSpecial;
	
	private int p = -1; 
	private int N = -1; 
	private int P = -1; 
	private int L = -1; 
	private int L_S;
	private int P_S;
	
	
	@Inject
	public PLokStorage(@Named("N")int N,  @Named("P")int P,  @Named("L")int L, @Named("cacheUnitSize") int cacheUnitSize, Provider<Index> indexProvider, Provider<FilePersistentStorage> persStorage) {
		super();
		storage = persStorage.get();
			cache = CacheBuilder.newBuilder()
					.maximumSize(cacheUnitSize)
					.build(new CacheLoader<Long, Block>() {
						@Override
						public Block load(Long key) throws Exception {
							cacheMissCount++;
							return readFromDisk(key);
						}
					});
			index = indexProvider.get();
			this.P = P;
			this.L = L;
			this.N = N;
			this.L_S = N % L;
			this.P_S = P * L / L_S;
			currentCommonBlocks = new ArrayList<>(N / L);
			refreshCommonColumn();
			currentSpecial = new Block(P_S, L_S);
		}
	private void refreshCommonColumn() {
		currentCommonBlocks = new ArrayList<>();
		//currentCommonBlocks.clear();
		for (int i = 0; i < N / L; i++){
			currentCommonBlocks.add(new Block(P, L));
		}
		
	}
	
	protected Block readFromDisk(long key) throws IOException {
		return this.storage.get(key);
	}


	public void put(Vector vector) {
		putCommonPart(vector);
		putSpecialPart(vector);
	}
	
	private void putSpecialPart(Vector vector) {
		if (L_S != 0){
			if (currentSpecial.tryAdd(vector.cutCopy(vector.getLength() - L_S, vector.getLength() - 1))){
				currentSpecial.pack(System.currentTimeMillis(), vector.getLength() - L_S);
				index.put(currentSpecial);
				currentSpecial = new Block(P_S, L_S);
			}
		}
	}
	
	private void putCommonPart(Vector vector) {
		boolean commonBlocksFilled = false;
		for (int i = 0; i < N / L; i++){
			int up = L * i;
			int down = L * (i + 1) - 1;
			Block block = currentCommonBlocks.get(i);
			if (block.tryAdd(vector.cutCopy(up, down))){
				commonBlocksFilled = true;
				block.pack(System.currentTimeMillis(), up);
				index.put(block);
				System.out.println("Blocks in index:" + index.getBlockCount());
			}
		}
		if (commonBlocksFilled){
			refreshCommonColumn();
		}
	}
	
	public List<Block> serve(Query q) throws Exception{
		List<Long> ids = index.get(q.getTimeStart(), q.getTimeEnd(), q.getIndexStart(), q.getIndexEnd());
		List<Block> blocks = new ArrayList<>(); 
		for (Long id: ids){
			blocks.add(cache.get(id));
		}
		return blocks;		
	}
	
	@Override
	public HashMap<String, Object> getStatistics() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int getBlockCount() {
		return index.getBlockCount();
	}
	

}
