package ru.spbu.math.plok.model.storagesystem;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;


public class Index {
	
	private int p = -1; 
	private int N = -1; 
	private int P = -1; 
	private int L = -1; 
	private int L_S;
	private int P_S;
	private long firstBasicTimestamp = -1;
	private long firstSpecialTimestamp = -1;
	private List<List<Long>> grid;
	private List<Long> specialGrid;
	private int blockCount;
	

	@Inject
	public Index(@Named("N")int N,  @Named("P")int P,  @Named("L")int L,  @Named("p")int period) {
		super();
		this.p = period;
		this.P = P;
		this.L = L;
		this.N = N;
		this.L_S = N % L;
		this.P_S = P * L / L_S;
		grid = new ArrayList<>();
		grid.add(new ArrayList<Long>());
		specialGrid = new ArrayList<>();
	}

	@Override
	public String toString() {
		return "Index [p=" + p + ", N=" + N + ", P=" + P + ", L=" + L + ", L_S=" + L_S + ", P_S=" + P_S
				+ ", grid=" + grid + ", specialGrid=" + specialGrid + "]";
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
				grid.add(new ArrayList<Long>());
				last++;
			}
			grid.get(last).add(entry.getId());
			System.out.println("Columns in grid:" + grid.size());
			if (firstBasicTimestamp == -1)
				firstBasicTimestamp = entry.gettBeg();
		}
		blockCount++;
	}

	private boolean isSpecial(BlockHeader entry) {
		return entry.getiEnd() - entry.getiBeg() + 1 != L;
	}

	public List<Long> get(long startTime, long endTime, int i1, int i2) {
		List<Long> basics   = getFromBasic(startTime, endTime, i1, i2);
		List<Long> specials = getFromSpecial(startTime, endTime, i1, i2);
		List<Long> result = new ArrayList<>(basics.size() + specials.size());
		result.addAll(basics);
		result.addAll(specials);
		return result;
	}

	private List<Long> getFromSpecial(long startTime, long endTime, int i1, int i2) {
		long firstTime = firstBasicTimestamp;
		long lastTime = firstTime + specialGrid.size() * P * p;
		if (specialGrid.isEmpty() 
				|| startTime > lastTime
				|| endTime   < firstTime) {
			return new ArrayList<>();
		}
		int leftBlockIndex  = (int)  (startTime    >= firstTime ? ((startTime - firstTime) / p) / P_S   : 0              );
		int rightBlockIndex = (int)  (lastTime     >= endTime   ? ((endTime   - firstTime) / p) / P_S   : specialGrid.size() - 1);
		return specialGrid.subList(leftBlockIndex, rightBlockIndex + 1);
	}

	private List<Long> getFromBasic(long qTimeStart, long qTimeEnd, int qIndexStart, int qIndexEnd) {
		long firstTime = firstBasicTimestamp;
		long lastTime  = firstTime + grid.size() * P * p ;
		if (grid.isEmpty() 
				|| qTimeStart  > lastTime
				|| qTimeEnd    < firstTime
				|| qIndexStart > N
				|| qIndexEnd   < 0 
				) {
			return new ArrayList<>();
		}
		int leftBlockIndex  = (int)  (qTimeStart  >= firstTime  ? ((qTimeStart - firstTime) / p) / P   : 0                );
		int rightBlockIndex = (int)  (qTimeEnd    <= lastTime	? ((qTimeEnd   - firstTime) / p) / P   : grid.size() - 1  );
		int upBlockIndex  	= (int) (qIndexStart  >= 0 			? qIndexStart / L : 0);
		int downBlockIndex  = (int) (qIndexEnd    <= N 			? qIndexEnd / L : 0);
		List<Long> result = new ArrayList<>();
		for (int i = leftBlockIndex; i <= rightBlockIndex; i++){
			 result.addAll(grid.get(i).subList(upBlockIndex, downBlockIndex + 1));
		}
		return result;
	}

	public int getBlockCount() {
		return blockCount;
	}
}
