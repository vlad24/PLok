package ru.spbu.math.plok.bench;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

public class Configuraton{

	private static Logger log = LoggerFactory.getLogger(Configuraton.class);
	private static final String     DEFAULT_SOLVER          = "basic";
	private static final String  	DEFAULT_REPORT_OUTPUT 	= "./reports/";
	private static final String 	DEFAULT_STORAGE_PATH	= "./storages";
	private static final String 	DEFAULT_PHASE_BREAK 	= "2000";
	private static final String 	DEFAULT_VERBOSITY 		= "info";
	private static final String 	DEFAULT_S 				= "PLok";
	private static final String 	DEFAULT_C 				= "0.25";
	private static final Integer 	DEFAULT_p 				= 10;
	private static final int     	m 						= Float.BYTES;

	private boolean inited;
	private int 			config_N;
	private Integer 		config_p;
	private Float 			config_C;
	private int 			config_T;
	private String 			config_S;
	private String 			config_O;
	private String 			config_H;
	private Integer 		config_phaseBreak;
	private String          config_verbosity;
	private String 			config_storagePath;
	private String config_solver;
	private boolean 		config_debug;
	private int 			calculated_A;
	private int 			calculated_SIZE;
	private int 			calculated_cacheSize;

	private CommandLineParser parser;
	private Option phaseBreak;
	private Option O; 
	private Option C;      		
	private Option N;     		
	private Option T;    		
	private Option H;    		
	private Option S;
	private Option p;
	private Option debug;
	private Option verbosity;
	private Option storagePath;
	private Options options;
	private Option solver;
	private Option repeatHistory;
	private boolean config_repeatH;

	public Configuraton(){
		N      			= new Option("N", true, "vector length"); 							N.setRequired(true);
		T      			= new Option("T", true, "write time (msec)");						T.setRequired(true);
		C     	 		= new Option("C", true, "cache ratio");								C.setRequired(false);
		H      			= new Option("H", true, "history file");			                H.setRequired(true);
		S				= new Option("S", true, "storage type");							S.setRequired(false);
		O				= new Option("O", "output", true, "output");						O.setRequired(false);
		phaseBreak		= new Option("break", true, "break between write and read phases"); phaseBreak.setRequired(false);
		verbosity		= new Option("verbosity", true, "verbosity level (debug, info)");	verbosity.setRequired(false);
		solver          = new Option("storagePath", true, "persister file");                solver.setRequired(false);
		storagePath		= new Option("storagePath", true, "persister file");
		debug			= new Option("debug", false, "debug mode flag");
		repeatHistory   = new Option("repeat_history", false, "if client should attack with history");
		options = new Options().
				addOption(N).
				addOption(T).
				addOption(C).
				addOption(H).
				addOption(S).
				addOption(O).
				addOption(phaseBreak).
				addOption(debug).
				addOption(repeatHistory).
				addOption(storagePath).
				addOption(solver).
				addOption(verbosity);
		parser = new PosixParser();
		inited = false;
	}

	public Configuraton(String[] args) throws IOException, ParseException {
		super();
		initConfigs(args);
	}

	public void initFromArgs(String[] args) throws IOException, ParseException{
		initConfigs(args);
	}

	private void initConfigs(String[] args) throws IOException, ParseException{
		if (!inited){
			CommandLine line 		= parser.parse(options, args);
			config_phaseBreak    	= Integer.valueOf(line.getOptionValue("break", DEFAULT_PHASE_BREAK));
			config_p 				= DEFAULT_p;
			config_C 				= Float.valueOf(line.getOptionValue("C", DEFAULT_C));
			config_N 				= Integer.valueOf(line.getOptionValue("N"));
			config_T 				= Integer.valueOf(line.getOptionValue("T"));
			config_H 				= line.getOptionValue("H");
			config_S 				= line.getOptionValue("S", DEFAULT_S);
			config_O				= line.getOptionValue("O", DEFAULT_REPORT_OUTPUT);
			config_solver			= line.getOptionValue("solver", DEFAULT_SOLVER);
			config_storagePath		= line.getOptionValue("storagePath", DEFAULT_STORAGE_PATH);
			config_verbosity 		= line.getOptionValue("verbosity", DEFAULT_VERBOSITY);
			config_debug			= line.hasOption("debug");
			config_repeatH			= line.hasOption("repeat_history");
			calculated_SIZE 		= calculateSIZE();
			calculated_cacheSize	= calculateCacheSize();
			calculated_A 			= calculateA();
			inited = true;
		}
	}

	private int calculateCacheSize() {
		return (int) (calculated_SIZE * config_C);
	}

	private int calculateSIZE() {
		return m * config_N * config_T / config_p;
	}

	private int calculateA() {
		return 10 * calculated_SIZE / m;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.omitNullValues()
				.add("storage", config_S)
				.add("N", config_N )
				.add("H", config_H )
				.add("T", config_T )
				.add("p", config_p )
				.add("A", calculated_A )
				.add("C", config_C )
				.add("O", config_O )
				.add("phaseBreak", config_phaseBreak)
				.add("debug", config_repeatH)
				.add("debug", config_debug)
				.toString();
	}

	public int getPhaseBreak() {
		return config_phaseBreak;
	}
	
	public int getT() {
		return config_T;
	}
	
	public String getOutput(){
		return config_O;
	}
	
	public String getStorage(){
		return config_S;
	}
	
	public boolean isDebugging(){
		return config_debug;
	}

	public int getN() {
		return config_N;
	}

	public Integer getPeriod() {
		return config_p;
	}

	public int getA() {
		return calculated_A;
	}
	
	public int getSIZE() {
		return calculated_SIZE;
	}
	
	public int getCacheByteSize() {
		return calculated_cacheSize;
	}

	public int getCacheUnitSize() {
		return calculated_cacheSize / m;
	}

	public String getH() {
		return config_H;
	}

	public String getStoragePath() {
		return config_storagePath;
	}
	
	public String getVerbosity() {
		return config_verbosity;
	}
	
	public String getSolverType(){
		return config_solver;
	}
	
	public boolean isRepeatingHistory(){
		return config_repeatH;
	}

}
