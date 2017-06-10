package ru.spbu.math.ais.plok.model.store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import ru.spbu.math.ais.plok.MapKeyNames;
import ru.spbu.math.ais.plok.NamedProps;
import ru.spbu.math.ais.plok.model.client.Query;
import ru.spbu.math.ais.plok.model.generator.Vector;

public class PLokerStorage implements StorageSystem{

	private final static Logger log = LoggerFactory.getLogger(PLokerStorage.class);
	
	private final LoadingCache<Long, Block> cache;
	private final FilePersistentStorage peristor;
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
	private boolean isFilledFromUp = true;
	private boolean realDiskIO;



	@Inject
	public PLokerStorage(@Named(NamedProps.N)int N,  @Named(NamedProps.P)int P,	@Named(NamedProps.L)int L,
			@Named(NamedProps.IS_FILLED_FROM_UP) boolean isFilledFromUp,
			@Named(NamedProps.FAKE_IO) boolean isInTestMode,
			@Named(NamedProps.CACHE_UNIT_SIZE) int cacheSizeInUnits, 
			Provider<Index> indexProvider, Provider<FilePersistentStorage> persStorage) {
		super();
		if ((P < 1) || (L < 1) || (N < L)){
			throw new IllegalArgumentException("Incorrect P,L,N");
		}
		peristor = persStorage.get();
		int cacheSizeInBlocks = cacheSizeInUnits / (P * L);
		cache = CacheBuilder.newBuilder()
				.maximumSize(cacheSizeInBlocks)
				.build(new CacheLoader<Long, Block>() {
					@Override
					public Block load(Long key) throws Exception {
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
		this.isFilledFromUp = isFilledFromUp;
		this.realDiskIO  = !isInTestMode;
		currentCommonBlocks = new ArrayList<>(N / L);
		refreshCommonColumn();
		currentSpecial = new Block(P_S, L_S);
		nextId = new AtomicInteger(0);
		log.info("Inited Ploker storage with N={} P={}, L={}, P_S={}, L_S={}, realDiskIO={}",N, P, L, P_S, L_S, realDiskIO);
	}
	
	
	private void refreshCommonColumn() {
		currentCommonBlocks.clear();
		for (int i = 0; i < N / L; i++){
			currentCommonBlocks.add(new Block(P, L));
		}
	}

	protected Block readFromDisk(long key) throws IOException {
		if (realDiskIO){
			return this.peristor.get(key);
		}else{
			return new Block(P, L).withHeader(new BlockHeader(key, -1, -1, -1, -1));
		}
	}

	public void put(Vector vector) throws IOException {
		putCommonPart(vector);
		putSpecialPart(vector);
	}

	private void putCommonPart(Vector vector) throws IOException {
		boolean commonBlocksFilled = false;
		int startPoint = (isFilledFromUp)? 0 : L_S;
		for (int i = 0; i < N / L; i++){
			int up   = startPoint + L * i;
			int down = up + L - 1; 
			Block block = currentCommonBlocks.get(i);
			if (block.tryAdd(vector.cutCopy(up, down))){
				commonBlocksFilled = true;
				block.autoFillHeader(getNextId(block), up);
				index.put(block);
				if (realDiskIO){
					peristor.add(block);
				}
			}
		}
		if (commonBlocksFilled){
			refreshCommonColumn();
		}
	}
	
	private void putSpecialPart(Vector vector) throws IOException {
		int startPoint = (isFilledFromUp)? vector.getLength() - L_S : 0;
		if (L_S != 0){
			if (currentSpecial.tryAdd(vector.cutCopy(startPoint, startPoint + L_S - 1))){
				currentSpecial.autoFillHeader(getNextId(currentSpecial), startPoint);
				index.put(currentSpecial);
				if (realDiskIO){
					peristor.add(currentSpecial);
				}
				currentSpecial = new Block(P_S, L_S);
			}
		}
	}

	public List<Block> serve(Query q) throws Exception{
		List<Long> ids = index.get(q.getJ1(), q.getJ2(), q.getI1(), q.getI2());
		List<Block> resultBlocks = new ArrayList<>(); 
		for (Long id: ids){
			blocksReadInTotal++;
			resultBlocks.add(cache.get(id));
		}
		return resultBlocks;		
	}

	@Override
	public Map<String, Object> getStatistics() {
		LinkedHashMap<String,Object> result = new LinkedHashMap<>();
		//result.put(MapKeyNames.BLOCKS_READ_FROM_DISK, blocksReadFromDisk);
		//result.put(MapKeyNames.BLOCKS_READ_IN_TOTAL,  blocksReadInTotal);
		result.put(MapKeyNames.TARGET_RATIO,          String.format("%.3f", 100 * (float)blocksReadFromDisk / blocksReadInTotal));
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

	@Override
	public void printState() {
		//log.debug(index.getVisualBlockScheme());
	}

}
