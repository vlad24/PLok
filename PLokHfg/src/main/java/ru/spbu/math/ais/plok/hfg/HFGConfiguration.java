package ru.spbu.math.ais.plok.hfg;

import java.util.LinkedHashMap;
import java.util.Map;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

import ru.spbu.math.ais.plok.MapKeyNames;
import ru.spbu.math.ais.plok.solvers.histogramsolver.UserChoice.Policy;

/**
 * @author vlad
 * iPolicy, jPolicy, history file, timeStep, count, vectorSize, verbosity, policiessParams
 * 
 */
public class HFGConfiguration{

	private static Logger log = LoggerFactory.getLogger(HFGConfiguration.class);
	
	private boolean inited;
	
	//////////////////////////////////////////////////////////////////////////////////////////////// COMMON OPTS
	@Option(name="-O",usage="History file", required=true)
	private String 	historyFile;
	
	@Option(name="--iPolicy", usage="i policy", required=true)
	private Policy iPolicy;
	
	@Option(name="--jPolicy", usage="j policy", required=true)
	private Policy jPolicy;
	
	@Option(name="--timeStep",usage="read query avg period", required=true)
	private Integer timeStep;
	
	@Option(name="--vectorSize",usage="vector size(N)", required=true)
	private Integer vectorSize;
	
	@Option(name="--count",usage="amount of queries", required=true)
	private Integer queriesCount;

	@Option(name="--verbosity",usage="Verbosity level", required=false)
	private String  verbosity = "info";
	
	@Option(name="--windowSize",usage="Window size for JPolicy.RecentTracking", required=false)
	private Long    windowSize;
	
	@Option(name="--hotRanges",usage="Hot ranges for IPolicy.HotRanges. EX: 1-2, 33-66, 75-100", required=false)
	private String	hotRanges;
	
	@Option(name="--includeHints",usage="If hints should be included into history file", required=false)
	private boolean includeHints;
	
	
	
	public HFGConfiguration(){
		inited = false;
	}

	public HFGConfiguration(String[] args){
		super();
		initConfigs(args);
	}

	private void initConfigs(String[] args){
		if (!inited){
			CmdLineParser parser = new CmdLineParser(this);
			try {
				parser.parseArgument(args);
				inited = true;
			} catch (CmdLineException e) {
				log.error(e.toString());
			}
		}
	}

	

	@Override
	public String toString() {
		return MoreObjects.toStringHelper("")
				.omitNullValues()
				.add("O",     historyFile)
				.add("N",     vectorSize)
				.add("Q",     queriesCount)
				.add("R",     timeStep)
				.add("W",     windowSize)
				.toString()
				.replace("{", "").replace("}", "");
	}
	
	/*
	 * Getters and setters go below
	 */

	public boolean isInited() {
		return inited;
	}

	public String getHistoryFile() {
		return historyFile;
	}

	public Policy getiPolicy() {
		return iPolicy;
	}

	public Policy getjPolicy() {
		return jPolicy;
	}

	public Integer getTimeStep() {
		return timeStep;
	}

	public Integer getVectorSize() {
		return vectorSize;
	}

	public Integer getQueriesCount() {
		return queriesCount;
	}

	public String getVerbosity() {
		return verbosity;
	}

	public Long getWindowSize() {
		return windowSize;
	}
	
	public String getHotRanges() {
		return hotRanges;
	}
	
	public boolean isIncludeHints() {
		return includeHints;
	}

	public Map<String, Object> getPoliciesParams() {
		LinkedHashMap<String, Object> params = new LinkedHashMap<>();
		if (hotRanges != null){	
			params.put(MapKeyNames.I_POLICY_HR_RANGES_KEY, hotRanges);
		}
		if (windowSize != null){	
			params.put(MapKeyNames.J_POLICY_RT_WINDOW_KEY, windowSize);
		}
		return params;
	}



	
}
