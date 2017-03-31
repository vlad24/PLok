package ru.spbu.math.plok.bench;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

public class Configuraton{

	private static Logger log = LoggerFactory.getLogger(Configuraton.class);
	
	private static final int VECTOR_UNIT_BYTE_SIZE = Float.BYTES;
	private static final int QUERY_AMOUNT_FACTOR = 1;

	private boolean inited;
	
	@Option(name="-N",usage="Vector length", required=true)
	private int    vectorLength;
	
	@Option(name="-H",usage="History file", required=true)
	private String 	historyFile;
	
	@Option(name="-T",usage="Attack time", required=true)
	private int 	attackTime;
	
	@Option(name="-C",usage="Cache ratio", required=false)
	private Float 	cacheRatio = 0.25f;
	
	@Option(name="-p",usage="idle between two subsequent writes (msec)", required=false)
	private Integer period = 10;
	
	@Option(name="-S",usage="Storage type", required=false)
	private String 	storageType = "plok";
	
	@Option(name="-O",usage="Output", required=false)
	private String 	outputPath = "./reports/";
	
	@Option(name="-v",usage="Verbosity level", required=false)
	private String  verbosity = "info";
	
	@Option(name="--phaseBreak",usage="Break between write and read phases(ms)", required=false)
	private Integer phaseBreakMs = 2000;
	
	@Option(name="-storagePath",usage="Persister file", required=false)
	private String 	storagePath="./storages";
	
	@Option(name="--solver",usage="Solver type", required=false)
	private String  solverType = "histogram";
	
	@Option(name="--debug",usage="Debug flag", required=false)
	private boolean debug;
	
	@Option(name="--repeatHistory",usage="if client should attack with history", required=false)
	private boolean repeatHistory;
	
	//Computed fields
	private int 	queriesAmount;
	private int 	writeDataSize;
	private int 	cacheByteSize;
	private int 	cacheUnitSize;

	public Configuraton(){
		inited = false;
	}

	public Configuraton(String[] args){
		super();
		initConfigs(args);
	}

	private void initConfigs(String[] args){
		if (!inited){
			CmdLineParser parser = new CmdLineParser(this);
			try {
				parser.parseArgument(args);
				writeDataSize = calculateWriteDataSize();
				cacheByteSize = calculateCacheByteSize();
				cacheUnitSize = calculateCacheUnitSize();
				queriesAmount = calculateQueryAmount();
				inited = true;
			} catch (CmdLineException e) {
				e.printStackTrace();
			}
		}
	}

	private int calculateCacheByteSize() {
		return (int) (Math.ceil(writeDataSize * cacheRatio));
	}

	private int calculateWriteDataSize() {
		// element_size * final_grid_size
		return VECTOR_UNIT_BYTE_SIZE * (vectorLength * attackTime / period);
	}
	
	private int calculateCacheUnitSize() {
		return cacheByteSize / VECTOR_UNIT_BYTE_SIZE;
	}

	private int calculateQueryAmount() {
		//C * final_grid_size
		return QUERY_AMOUNT_FACTOR * (vectorLength * attackTime / period);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.omitNullValues()
				.add("storageType", storageType)
				.add("solverType",  solverType)
				.add("N", vectorLength )
				.add("H", historyFile )
				.add("T", attackTime )
				.add("p", period )
				.add("C", cacheRatio)
				.add("Q", queriesAmount )
				.add("O", outputPath)
				.add("phaseBreak", phaseBreakMs)
				.add("debug", debug)
				.add("repeatHistory", repeatHistory)
				.toString();
	}

	
	
	/*
	 * Getters and setters go below
	 */
	public boolean isInited() {
		return inited;
	}

	public int getVectorLength() {
		return vectorLength;
	}

	public String getHistoryFilePath() {
		return historyFile;
	}

	public int getAttackTimeMs() {
		return attackTime;
	}

	public Float getCacheRatio() {
		return cacheRatio;
	}

	public Integer getPeriod() {
		return period;
	}

	public String getStorageType() {
		return storageType;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public Integer getPhaseBreakMs() {
		return phaseBreakMs;
	}

	public String getVerbosityLevel() {
		return verbosity;
	}

	public String getStoragePath() {
		return storagePath;
	}

	public String getSolverType() {
		return solverType;
	}

	public boolean isDebugging() {
		return debug;
	}

	public boolean isRepeatingHistory() {
		return repeatHistory;
	}

	
	// Calculated fields getters
	public int getQueriesAmount() {
		return queriesAmount;
	}

	public int getWriteDataSize() {
		return writeDataSize;
	}

	public int getCacheByteSize() {
		return cacheByteSize;
	}
	
	public int getCacheUnitSize() {
		return cacheUnitSize;
	}


}
