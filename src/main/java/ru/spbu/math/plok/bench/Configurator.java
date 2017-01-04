package ru.spbu.math.plok.bench;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

import ru.spbu.math.plok.model.client.Distribution;

public class Configurator {

	private static Logger log = LoggerFactory.getLogger(Configurator.class);
	private static final String  	DEFAULT_REPORT_OUTPUT 	= "report_" + System.currentTimeMillis() + ".txt";
	private static final String 	DEFAULT_STORAGE_PATH	= "./tmp_file_storage";
	private static final String 	DEFAULT_PHASE_BREAK 	= "2000";
	private static final String 	DEFAULT_S 				= "PLok";
	private static final String 	DEFAULT_C 				= "0.25";
	private static final Integer 	DEFAULT_p 				= 10;
	private static final int     	m 						= Float.BYTES;

	private int 			config_N;
	private Integer 		config_p;
	private Float 			config_C;
	private int 			config_T;
	private String 			config_S;
	private String 			config_O;
	private String 			config_V;
	private Integer 		config_phaseBreak;
	private boolean 		config_debug;
	private int 			calculated_A;

	private Option phaseBreak;
	private Option O; 
	private Option C;      		
	private Option N;     		
	private Option T;    		
	private Option V;    		
	private Option S;
	private Option L;
	private Option P;
	private Option debug;
	private Option storagePath;
	private Options options;
	private CommandLineParser parser;
	private int calculated_SIZE;
	private boolean inited;
	private int calculated_cacheSize;
	private Integer config_P;
	private Integer config_L;
	private String config_storagePath;



	public Configurator(){
		N      			= new Option("N", true, "vector length"); 							N.setRequired(true);
		T      			= new Option("T", true, "write time (msec)");						T.setRequired(true);
		C     	 		= new Option("C", true, "cache ratio");								C.setRequired(false);
		V      			= new Option("V", true, "distribution (exp, norm, uni)");			V.setRequired(true);
		S				= new Option("S", true, "storage type");							S.setRequired(false);
		O				= new Option("O", "output", true, "output");						O.setRequired(false);
		phaseBreak		= new Option("break", true, "break between write and read phases"); phaseBreak.setRequired(false);
		P     	 		= new Option("P", true, "P for block");								P.setRequired(true);
		L     	 		= new Option("L", true, "L for block");								L.setRequired(true);
		storagePath		= new Option("storagePath", true, "persister file");
		debug			= new Option("debug", false, "debug mode flag");
		options = new Options().
				addOption(N).
				addOption(T).
				addOption(C).
				addOption(V).
				addOption(S).
				addOption(O).
				addOption(phaseBreak).
				addOption(debug).
				addOption(storagePath).
				addOption(P).
				addOption(L);
		parser = new PosixParser();
		inited = false;
	}

	public Configurator(String[] args) throws IOException, ParseException {
		super();
		initConfigs(args);
	}

	public void initFromArgs(String[] args) throws IOException, ParseException{
		initConfigs(args);
	}

	private void initConfigs(String[] args) throws IOException, ParseException{
		if (!inited){
			CommandLine line 		= parser.parse(options, args);
			config_debug			= line.hasOption("debug");
			config_phaseBreak    	= Integer.valueOf(line.getOptionValue("break", DEFAULT_PHASE_BREAK));
			config_N 				= Integer.valueOf(line.getOptionValue("N"));
			config_T 				= Integer.valueOf(line.getOptionValue("T"));
			config_C 				= Float.valueOf(line.getOptionValue("C", DEFAULT_C));
			config_P 				= Integer.valueOf(line.getOptionValue("P"));
			config_L 				= Integer.valueOf(line.getOptionValue("L"));
			config_S 				= line.getOptionValue("S", DEFAULT_S);
			config_O				= line.getOptionValue("O", DEFAULT_REPORT_OUTPUT);
			config_storagePath		= line.getOptionValue("storagePath", DEFAULT_STORAGE_PATH);
			config_V 				= line.getOptionValue("V");
			config_p 				= DEFAULT_p;
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
		return 1000 * calculated_SIZE / m;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.omitNullValues()
				.add("storage", config_S)
				.add("N", config_N )
				.add("V", config_V )
				.add("T", config_T )
				.add("p", config_p )
				.add("A", calculated_A )
				.add("C", config_C )
				.add("O", config_O )
				.add("phaseBreak", config_phaseBreak)
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

	public Integer getP() {
		return config_P;
	}
	
	public Integer getL() {
		return config_L;
	}

	public String getV() {
		return config_V;
	}

	public String getStoragePath() {
		return config_storagePath;
	}
}
