package ru.spbu.math.ais.plok.model.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ru.spbu.math.ais.plok.NamedProps;


public class Index {
	private final static Logger log = LoggerFactory.getLogger(Index.class);

	private int p 	=  1; // reserved 
	private int N 	= -1; 
	private int P 	= -1; 
	private int L 	= -1; 
	private int L_S = -1;
	private int P_S = -1;
	private boolean isFFU;
	private long firstBasicTimestamp = -1;
	private long firstSpecialTimestamp = -1;
	private List<List<Long>> grid;
	private List<Long> specialGrid;
	private int blockCount;



	@Inject
	public Index(@Named(NamedProps.N)int N,  @Named(NamedProps.P)int P,  @Named(NamedProps.L)int L, @Named(NamedProps.IS_FILLED_FROM_UP)boolean isFFU) {
		super();
		this.N = N;
		this.P = P;
		this.L = L;
		this.L_S = N % L;
		this.P_S = (L_S != 0) ? (P * L / L_S) : 0;
		this.isFFU = isFFU;
		grid = new ArrayList<>();
		grid.add(new ArrayList<Long>());
		specialGrid = new ArrayList<>();
	}


	@Override
	public String toString() {
		return "Index [p=" + p + ", N=" + N + ", P=" + P + ", L=" + L + ", L_S=" + L_S + ", P_S=" + P_S
				+ ", grid=" + grid.subList(0, Math.min(grid.size(), 5)) 
				+ ", specialGrid=" + specialGrid.subList(0, Math.min(specialGrid.size(), 5)) + "]";
	}

	public String getVisualBlockScheme() {
		List<Character> commonMarkers  = new ArrayList<>(26);
		List<Character> specialMarkers = new ArrayList<>(26);
		for (char c = 'A'; c <= 'Z'; c++){
			commonMarkers.add(c);
			specialMarkers.add(Character.toLowerCase(c));
		}
		StringBuilder builder = new StringBuilder("\n");
		printTimeLine(builder);
		if (isFFU){
			printCommonBlocks(commonMarkers, builder);
			printSpecialBlocks(specialMarkers, builder);
		}else{
			printSpecialBlocks(specialMarkers, builder);
			printCommonBlocks(commonMarkers, builder);
			
		}
		return builder.toString();
	}


	private void printTimeLine(StringBuilder builder) {
		for (long b = 0; b < grid.size(); b++){
			builder.append(b).append("\t");
		}
		builder.append("\n");
	}


	private void printCommonBlocks(List<Character> commonMarkers, StringBuilder builder) {
		for (int i = 1; i < N - L_S; i++){
			builder.append(i).append("\t");
			for (long b = 0; b < grid.size(); b++){
				Character marker = commonMarkers.get((int)((b + i/L) % commonMarkers.size()));
				for (int t = 0; t < P; t += p){
					builder.append(marker).append("\t");
				}
			}
			builder.append("\n");
		}
	}


	private void printSpecialBlocks(List<Character> specialMarkers, StringBuilder builder) {
		for (int i = N - L_S; i <= N; i++){
			builder.append(i).append("\t");
			for (long b = 0; b < specialGrid.size(); b++){
				Character marker = specialMarkers.get((int)(b % specialMarkers.size()));
				for (int t = 0; t < P_S; t += p){
					builder.append(marker).append("\t");
				}
			}
			builder.append("\n");
		}
	}


	public int getBlockCount() {
		return blockCount;
	}


	public void put(Block entry) {
		put(entry.getHeader());
	}


	public void put(BlockHeader entry) {
		if (isSpecial(entry)){
			assert (entry.getiEnd() - entry.getiBeg() + 1 == L_S);
			assert (entry.gettEnd() - entry.gettBeg() + 1 == P_S);
			specialGrid.add(entry.getId());
			log.trace("Put {} as special", entry);
			if (firstSpecialTimestamp == -1){
				firstSpecialTimestamp = entry.gettBeg();
				log.trace("First special timestamp set to {}", firstSpecialTimestamp);
			}
		}else{
			assert (entry.getiEnd() - entry.getiBeg() + 1 == L);
			assert (entry.gettEnd() - entry.gettBeg() + 1 == P);
			int last = grid.size() - 1;
			if (grid.get(last).size() == N / L){
				grid.add(new ArrayList<Long>());
				last++;
			}
			grid.get(last).add(entry.getId());
			log.trace("Put {} as basic", entry);
			if (firstBasicTimestamp == -1){
				firstBasicTimestamp = entry.gettBeg();
				log.trace("First basic timestamp set to {}", firstBasicTimestamp);
			}
		}
		blockCount++;
	}

	private boolean isSpecial(BlockHeader entry) {
		return entry.getiEnd() - entry.getiBeg() + 1 != L;
	}

	public List<Long> get(long startTime, long endTime, int i1, int i2) {
		int commonsIndexStart  = isFFU ? i1                        : Math.max(i1, L_S);
		int commonsIndexEnd    = isFFU ? Math.min(N - L_S - 1, i2) : i2;
		int specialsIndexStart = isFFU ? Math.max(i1, N - L_S)     : 0;
		int specialsIndexEnd   = isFFU ? i2                        : Math.min(L_S - 1, i2);
		List<Long> commons   = getFromBasic  (startTime,   endTime,  commonsIndexStart,  commonsIndexEnd);
		List<Long> specials  = getFromSpecial(startTime,   endTime,  specialsIndexStart, specialsIndexEnd);
		List<Long> result = new ArrayList<>(commons.size() + specials.size());
		result.addAll(commons);
		result.addAll(specials);
		log.trace("Got {} commons and {} specials", commons.size(), specials.size());
		return result;
	}


	private List<Long> getFromBasic(long qTimeStart, long qTimeEnd, int qIndexStart, int qIndexEnd) {
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
		List<Long> result = new ArrayList<>();
		for (int i = leftBlockIndex; i <= rightBlockIndex; i++){
			List<Long> list = grid.get(i);
			result.addAll(list.subList(upBlockIndex, downBlockIndex + 1));
		}
		return result;
	}


	private List<Long> getFromSpecial(long qStartTime, long qEndTime, int qIndexStart, int qIndexEnd) {
		long firstTime = firstSpecialTimestamp;
		long lastTime = firstTime + (specialGrid.size() * P_S) * p;
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
		int rightBlockIndex = (int)  (lastTime      >  qEndTime   ? ((qEndTime   - firstTime) / p) / P_S   : specialGrid.size() - 1);
		return specialGrid.subList(leftBlockIndex, rightBlockIndex + 1);
	}


}