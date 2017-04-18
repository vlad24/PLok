package ru.spbu.math.plok.bench;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import ch.qos.logback.classic.Level;
import ru.spbu.math.plok.MapKeyNames;
import ru.spbu.math.plok.NamedProps;
import ru.spbu.math.plok.model.storagesystem.PLokerStorage;
import ru.spbu.math.plok.model.storagesystem.SQLStorage;
import ru.spbu.math.plok.model.storagesystem.StorageSystem;
import ru.spbu.math.plok.solvers.Solver;
import ru.spbu.math.plok.solvers.histogramsolver.HistogramSolver;

public class AppConfig extends AbstractModule {

	private final static Logger log = LoggerFactory.getLogger(AppConfig.class);

	private Configuration configs;
	private Solver solver;
	private HashMap<String, Object> solution;

	public AppConfig(Configuration configurator) throws Exception {
		configs = configurator;
		solver  = initializeSolver();
		solution = solver.solvePLTask();
	}

	public void configure() {
		bindConstant().annotatedWith(Names.named(NamedProps.N)).to(configs.getVectorLength());
		bindConstant().annotatedWith(Names.named(NamedProps.T)).to(configs.getAttackTimeMs());
		bindConstant().annotatedWith(Names.named(NamedProps.Q)).to(configs.getQueriesAmount());
		bindConstant().annotatedWith(Names.named(NamedProps.P)).to((Integer)solution.get(MapKeyNames.P_KEY));
		bindConstant().annotatedWith(Names.named(NamedProps.L)).to((Integer)solution.get(MapKeyNames.L_KEY));
		bindConstant().annotatedWith(Names.named(NamedProps.IS_FILLED_FROM_UP)).to((Boolean)solution.get(MapKeyNames.IS_FILLED_FROM_UP_KEY));
		bindConstant().annotatedWith(Names.named(NamedProps.PERIOD)).to(configs.getPeriod());
		bindConstant().annotatedWith(Names.named(NamedProps.STORAGE_PATH)).to(configs.getStoragePath());
		bindConstant().annotatedWith(Names.named(NamedProps.CACHE_UNIT_SIZE)).to(configs.getCacheUnitSize());
		bindStorageType();
		bindVerbosityLevel();
	}

	private Solver initializeSolver() {
		log.info("In order to configurate all complonets PL task should be solved");
		if (configs.getSolverType().equalsIgnoreCase("histogram")){
			log.info("Histogram Solver set.");
			return new HistogramSolver(configs.getVectorLength(), configs.getHistoryFilePath(), configs.getCacheUnitSize());
		}else {
			return null;
		}

	}

	private void bindStorageType() {
		if (configs.getStorageType().equalsIgnoreCase("sql"))
			bind(StorageSystem.class).to(SQLStorage.class);
		else {
			bind(StorageSystem.class).to(PLokerStorage.class);
		}
	}

	private void bindVerbosityLevel() {
		String verbosity = configs.getVerbosityLevel();
		ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger("ROOT");

		if(configs.isDebugging()){
			rootLogger.setLevel(Level.DEBUG);
		}
		else if (verbosity.equalsIgnoreCase(Level.DEBUG.toString())){
			rootLogger.setLevel(Level.DEBUG);
		}else if (verbosity.equalsIgnoreCase(Level.INFO.toString())){
			rootLogger.setLevel(Level.INFO);
		}else if (verbosity.equalsIgnoreCase(Level.ALL.toString())){
			rootLogger.setLevel(Level.ALL);
		}else if (verbosity.equalsIgnoreCase(Level.ERROR.toString())){
			rootLogger.setLevel(Level.ERROR);
		}
	}

	
	public HashMap<String, Object> getSolution() {
		return solution;
	}
	

}
