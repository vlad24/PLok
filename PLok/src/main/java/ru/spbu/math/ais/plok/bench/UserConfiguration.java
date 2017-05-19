package ru.spbu.math.ais.plok.bench;

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
	
	@Option(name="-V",usage="Vectors to write amount", required=true)
	private int 	vectorsAmount = -1;
	
	@Option(name="-C",usage="Cache ratio", required=false)
	private Float 	cacheRatio = 0.25f;
	
	@Option(name="-O",usage="Output file", required=false)
	private String 	outputPath = null;

	@Option(name="--append",usage="Flag, controlling if output should be appended to output", required=false)
	private boolean outputAppended = false;
	
	@Option(name="--verbosity",usage="Verbosity level", required=false)
	private String  verbosity = "info";
	
	@Option(name="--storagePath",usage="Persister file", required=false)
	private String 	storagePath="./storages";
	
	@Option(name="--forceUseHistory",usage="Persister file", required=false)
	private boolean forced2UseHistory = true;
	//////////////////////////////////////////////////////////////////////////////////////////////// TEST MODE
	@Option(name="--test",usage="Test flag", required=false, forbids={"--debug", "--repeatHistory", "--solver", "-r"})
	private boolean testFlag;
	
	@Option(name="-P",usage="Tested P", required=false, depends="--test")
	private Integer testedP;
	
	@Option(name="-L",usage="Tested L", required=false, depends="--test")
	private Integer testedL;
	
	//////////////////////////////////////////////////////////////////////////////////////////////// SOLVE MODE
	@Option(name="--solve",usage="Solve flag", required=false, forbids="--test")
	private boolean solveFlag;
	
	@Option(name="--solver",usage="Solver type", required=false, forbids="--test")
	private String  solverType = "histogram";

	
	
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
					solverType = "mock";
				}
				inited = true;
			} catch (CmdLineException e) {
				log.error(e.toString());
			}
		}
	}

	

	@Override
	public String toString() {
		return new StringBuilder()
				.append("H").append(Reporter.KEY_VALUE_SEPARATOR).append(historyFile).append(Reporter.ELEMENT_SEPARATOR)
				.append("C").append(Reporter.KEY_VALUE_SEPARATOR).append(cacheRatio).append(Reporter.ELEMENT_SEPARATOR)
				.append("V").append(Reporter.KEY_VALUE_SEPARATOR).append(vectorsAmount)
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
	

	public Float getCacheRatio() {
		return cacheRatio;
	}

	public String getOutputFile() {
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

	public boolean isSolving() {
		return solveFlag;
	}

	public boolean isTesting() {
		return testFlag;
	}
	
	public int getTestL() {
		return testedL;
	}

	public int getTestP() {
		return testedP;
	}

	public boolean isOutputAppended() {
		return outputAppended;
	}

	public boolean isForcedToUseHistory() {
		return forced2UseHistory;
	}

}
