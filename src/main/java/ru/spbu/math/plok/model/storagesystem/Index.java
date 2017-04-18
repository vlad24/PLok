package ru.spbu.math.plok.model.storagesystem;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ru.spbu.math.plok.NamedProps;


public class Index {
	private final static Logger log = LoggerFactory.getLogger(Index.class);
	
	private int p 	= -1; 
	private int N 	= -1; 
	private int P 	= -1; 
	private int L 	= -1; 
	private int L_S = -1;
	private int P_S = -1;
	private long firstBasicTimestamp = -1;
	private long firstSpecialTimestamp = -1;
	private List<List<Integer>> grid;
	private List<Integer> specialGrid;
	private int blockCount;


	@Inject
	public Index(@Named(NamedProps.N)int N,  @Named(NamedProps.P)int P,  @Named(NamedProps.L)int L,  @Named(NamedProps.PERIOD)int period) {
		super();
		this.p = period;
		this.N = N;
		this.P = P;
		this.L = L;
		this.L_S = N % L;
		this.P_S = (L_S != 0) ? (P * L / L_S) : 0;
		grid = new ArrayList<>();
		grid.add(new ArrayList<Integer>());
		specialGrid = new ArrayList<>();
	}

	
	@Override
	public String toString() {
		return "Index [p=" + p + ", N=" + N + ", P=" + P + ", L=" + L + ", L_S=" + L_S + ", P_S=" + P_S
				+ ", grid=" + grid + ", specialGrid=" + specialGrid + "]";
	}

	
	public int getBlockCount() {
		return blockCount;
	}
	
	
	public void put(Block entry) {
		put(entry.getHeader());
	}
	

	public void put(BlockHeader entry) {
		if (isSpecial(entry)){
			specialGrid.add(entry.getId());
			if (firstSpecialTimestamp == -1)
				firstSpecialTimestamp = entry.gettBeg();
		}else{
			int last = grid.size() - 1;
			if (grid.get(last).size() == N / L){
				grid.add(new ArrayList<Integer>());
				last++;
			}
			grid.get(last).add(entry.getId());
			if (firstBasicTimestamp == -1)
				firstBasicTimestamp = entry.gettBeg();
		}
		blockCount++;
	}
	

	private boolean isSpecial(BlockHeader entry) {
		return entry.getiEnd() - entry.getiBeg() + 1 != L;
	}

	public List<Integer> get(long startTime, long endTime, int i1, int i2) {
		List<Integer> commons   = getFromBasic  (startTime,   endTime,  i1,                        Math.min(N - L_S - 1, i2)    );
		List<Integer> specials = getFromSpecial(startTime,   endTime,  Math.max(i1, N - L_S),     i2                           );
		log.debug("Got {} commons and {} specials", commons.size(), specials.size());
		List<Integer> result = new ArrayList<>(commons.size() + specials.size());
		result.addAll(commons);
		result.addAll(specials);
		return result;
	}
	
	
	private List<Integer> getFromBasic(long qTimeStart, long qTimeEnd, int qIndexStart, int qIndexEnd) {
		long firstTime = firstBasicTimestamp;
		long lastTime  = firstTime + grid.size() * P * p - p;
		if (grid.isEmpty() 
				|| qTimeStart  > lastTime
				|| qTimeEnd    < firstTime
				|| qIndexStart > N - 1
				|| qIndexEnd   < 0
				|| qIndexEnd   < qIndexStart
				) {
			return new ArrayList<>();
		}
		int leftBlockIndex  = (int)  (qTimeStart  >= firstTime  ? ((qTimeStart - firstTime) / p) / P   : 0                );
		int rightBlockIndex = (int)  (qTimeEnd    <= lastTime	? ((qTimeEnd   - firstTime) / p) / P   : grid.size() - 1  );
		int upBlockIndex  	= (int)  (qIndexStart >= 0 		    ? qIndexStart / L : 0 );
		int downBlockIndex  = (int)  (qIndexEnd   <= N - 1	    ? qIndexEnd   / L : 0 );
		List<Integer> result = new ArrayList<>();
		for (int i = leftBlockIndex; i <= rightBlockIndex; i++){
			List<Integer> list = grid.get(i);
			result.addAll(list.subList(upBlockIndex, downBlockIndex + 1));
		}
		return result;
	}

	
	private List<Integer> getFromSpecial(long qStartTime, long qEndTime, int qIndexStart, int qIndexEnd) {
		long firstTime = firstSpecialTimestamp;
		long lastTime = firstTime + (specialGrid.size() * P) * p;
		if (specialGrid.isEmpty() 
				|| qStartTime   > lastTime
				|| qEndTime     < firstTime
				|| qIndexStart  > N - 1
				|| qIndexEnd    < 0
				|| qIndexEnd    < qIndexStart
				) {
			return new ArrayList<>();
		}
		int leftBlockIndex  = (int)  (qStartTime    >= firstTime  ? ((qStartTime - firstTime) / p) / P_S   : 0              );
		int rightBlockIndex = (int)  (lastTime      >= qEndTime   ? ((qEndTime   - firstTime) / p) / P_S   : specialGrid.size() - 1);
		return specialGrid.subList(leftBlockIndex, rightBlockIndex + 1);
	}


}