package ru.spbu.math.plok.bench;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import ch.qos.logback.classic.Level;
import ru.spbu.math.plok.model.storagesystem.PLokerStorage;
import ru.spbu.math.plok.model.storagesystem.SQLStorage;
import ru.spbu.math.plok.model.storagesystem.StorageSystem;
import ru.spbu.math.plok.solver.BasicSolver;
import ru.spbu.math.plok.solver.Solver;

public class AppConfig extends AbstractModule {

	private final static Logger log = LoggerFactory.getLogger(AppConfig.class);

	private Configuraton configs;
	private Solver solver;
	private HashMap<String, Object> solution;

	public AppConfig(Configuraton configurator) throws Exception {
		configs = configurator;
		solver  = initializeSolver();
		solution = solver.solvePLTask();
	}

	public void configure() {
		bindConstant().annotatedWith(Names.named("N")).to(configs.getVectorLength());
		bindConstant().annotatedWith(Names.named("T")).to(configs.getAttackTimeMs());
		bindConstant().annotatedWith(Names.named("Q")).to(configs.getQueriesAmount());
		bindConstant().annotatedWith(Names.named("P")).to((Integer)solution.get("P"));
		bindConstant().annotatedWith(Names.named("L")).to((Integer)solution.get("L"));
		bindConstant().annotatedWith(Names.named("p")).to(configs.getPeriod());
		bindConstant().annotatedWith(Names.named("storagePath")).to(configs.getStoragePath());
		bindConstant().annotatedWith(Names.named("cacheUnitSize")).to(configs.getCacheUnitSize());
		bindStorageType();
		bindVerbosityLevel();
	}

	private Solver initializeSolver() {
		log.info("In order to configurate all complonets PL task should be solved");
		if (configs.getSolverType().equalsIgnoreCase("basic")){
			log.info("Basic Solver set.");
			return new BasicSolver(configs.getHistoryFilePath());
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

		if      (configs.isDebugging()){
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
