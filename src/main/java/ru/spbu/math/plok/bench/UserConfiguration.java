package ru.spbu.math.plok.bench;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

public class UserConfiguration{

	private static Logger log = LoggerFactory.getLogger(UserConfiguration.class);
	
	private boolean inited;
	
	//////////////////////////////////////////////////////////////////////////////////////////////// COMMON OPTS
	@Option(name="-H",usage="History file", required=true)
	private String 	historyFile;
	
	@Option(name="-V",usage="Vectors to write amount", required=false)
	private int 	vectorsAmount = -1;
	
	@Option(name="-C",usage="Cache ratio", required=false)
	private Float 	cacheRatio = 0.25f;
	
	@Option(name="-r",usage="idle between two subsequent queries (msec)", required=false)
	private int readPeriod  = 10;
	
	@Option(name="-O",usage="Output", required=false)
	private String 	outputPath = "./reports/";
	
	@Option(name="-v",usage="Verbosity level", required=false)
	private String  verbosity = "info";
	
	@Option(name="--storagePath",usage="Persister file", required=false)
	private String 	storagePath="./storages";
	//////////////////////////////////////////////////////////////////////////////////////////////// TEST MODE
	@Option(name="--test",usage="Test flag", required=false, forbids={"--debug", "--repeatHistory", "--solver", "-r"})
	private boolean test;
	
	@Option(name="-P",usage="Tested P", required=false, depends="--test")
	private Integer testedP;
	
	@Option(name="-L",usage="Tested L", required=false, depends="--test")
	private Integer testedL;
	
	//////////////////////////////////////////////////////////////////////////////////////////////// DEBUG MODE
	@Option(name="--debug",usage="Debug flag", required=false, forbids="--test")
	private boolean debug;
	
	@Option(name="--solver",usage="Solver type", required=false, forbids="--test")
	private String  solverType = "histogram";

	//Computed fields
	private int 	queriesAmount;
	private int 	writeDataSizeBytes;
	private int 	cacheByteSize;
	private int 	cacheUnitSize;

	public UserConfiguration(){
		inited = false;
	}

	public UserConfiguration(String[] args){
		super();
		initConfigs(args);
	}

	private void initConfigs(String[] args){
		if (!inited){
			CmdLineParser parser = new CmdLineParser(this);
			try {
				parser.parseArgument(args);
				if (isTesting()){
					solverType    = "mock";
				}
				inited = true;
			} catch (CmdLineException e) {
				log.error(e.toString());
			}
		}
	}

	

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.omitNullValues()
				.add("H",     historyFile )
				.add("C",     cacheRatio)
				.add("V",     vectorsAmount )
				.add("pr",    readPeriod )
				.add("debug", debug)
				.add("test" , test)
				.toString();
	}

	
	
	/*
	 * Getters and setters go below
	 */
	public boolean isInited() {
		return inited;
	}

	public String getHistoryFilePath() {
		return historyFile;
	}

	public int getVectorAmount() {
		return vectorsAmount;
	}
	
	public long getReadPeriod() {
		return readPeriod;
	}

	public Float getCacheRatio() {
		return cacheRatio;
	}

	public String getOutputPath() {
		return outputPath;
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

	public boolean isTesting() {
		return test;
	}
	
	public int getTestL() {
		return testedL;
	}

	public int getTestP() {
		return testedP;
	}

	

}
