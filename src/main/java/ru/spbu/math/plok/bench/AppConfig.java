package ru.spbu.math.plok.bench;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import ch.qos.logback.classic.Level;
import ru.spbu.math.plok.MapKeyNames;
import ru.spbu.math.plok.NamedProps;
import ru.spbu.math.plok.model.storagesystem.PLokerStorage;
import ru.spbu.math.plok.model.storagesystem.StorageSystem;
import ru.spbu.math.plok.solvers.HistoryAnalysisReport;
import ru.spbu.math.plok.solvers.HistoryPreprocessor;
import ru.spbu.math.plok.solvers.MockSolver;
import ru.spbu.math.plok.solvers.Solver;
import ru.spbu.math.plok.solvers.histogramsolver.HistogramSolver;

public class AppConfig extends AbstractModule{

	private static final int VECTOR_UNIT_BYTE_SIZE = Float.BYTES;

	private final static Logger log = LoggerFactory.getLogger(AppConfig.class);

	private UserConfiguration   userConfig;
	private HistoryPreprocessor preprocessor;
	private Solver              solver;

	private Map<String, Object>   solution;
	private HistoryAnalysisReport hReport;

	private Integer cacheUnitSize;
	private Integer neededQueriesCount;

	public AppConfig(UserConfiguration configuration) throws Exception {
		userConfig   = configuration;
		((ch.qos.logback.classic.Logger)LoggerFactory.getLogger("ROOT")).setLevel(Level.valueOf(userConfig.getVerbosityLevel()));
		log.info("App configured: {}", configuration);
		preprocessor = new HistoryPreprocessor(userConfig.getHistoryFilePath());
		hReport = preprocessor.analyzeHistory();
		solver   = createSolver();
		solution = solver.solvePLTask();
	}


	@Override
	public void configure() {
		try{
			bind(StorageSystem.class).to(PLokerStorage.class);
			bindConstant().annotatedWith(Names.named(NamedProps.N))                .to(hReport.getN());
			bindConstant().annotatedWith(Names.named(NamedProps.V))                .to(userConfig.getVectorAmount());
			bindConstant().annotatedWith(Names.named(NamedProps.TEST_MODE))        .to(userConfig.isTesting());
			bindConstant().annotatedWith(Names.named(NamedProps.STORAGE_PATH))     .to(userConfig.getStoragePath());
			bindConstant().annotatedWith(Names.named(NamedProps.CACHE_UNIT_SIZE))  .to(getCacheUnitSize());
			bindConstant().annotatedWith(Names.named(NamedProps.P))                .to((Integer)solution.get(MapKeyNames.P_KEY));
			bindConstant().annotatedWith(Names.named(NamedProps.L))                .to((Integer)solution.get(MapKeyNames.L_KEY));
			bindConstant().annotatedWith(Names.named(NamedProps.IS_FILLED_FROM_UP)).to((Boolean)solution.get(MapKeyNames.IS_FILLED_FROM_UP_KEY));
		}catch(Exception e){
			log.error("Error while configuring app: {}", e);
		}
	}

	private Solver createSolver() throws IOException {
		if (userConfig.isTesting()){
			log.debug("Mock Solver set.");
			return new MockSolver(userConfig.getTestP(), userConfig.getTestL());
		}else if (userConfig.getSolverType().equalsIgnoreCase("histogram")){
			log.debug("Histogram Solver set.");
			return new HistogramSolver(hReport, getCacheUnitSize());
		}else {
			return null;
		}

	}

	public Map<String, Object> getSolution() {
		return solution;
	}

	public HistoryAnalysisReport getHistoryAnalysisReport() {
		return hReport;
	}
	
	public int getCacheUnitSize() {
		if (cacheUnitSize == null){
			cacheUnitSize = calculateCacheByteSize() / VECTOR_UNIT_BYTE_SIZE; 
		}
		return cacheUnitSize;  
	}
	
	public int getQueryAmount() {
		if (neededQueriesCount == null){
			if (userConfig.isTesting()){
				neededQueriesCount = hReport.getQueries().size();
			}else{
				neededQueriesCount = (int) (userConfig.getVectorAmount() / hReport.getTimeStep());
			}
		}
		return neededQueriesCount;
	}



	private int calculateCacheByteSize() {
		return (int) (Math.ceil(calculateWriteDataSizeInBytes() * userConfig.getCacheRatio()));
	}

	private int calculateWriteDataSizeInBytes() {
		return userConfig.getVectorAmount() * (VECTOR_UNIT_BYTE_SIZE * hReport.getN());
	}


}
