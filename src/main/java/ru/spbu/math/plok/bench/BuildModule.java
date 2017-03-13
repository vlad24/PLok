package ru.spbu.math.plok.bench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import ch.qos.logback.classic.Level;
import ru.spbu.math.plok.model.storagesystem.PLokerStorage;
import ru.spbu.math.plok.model.storagesystem.SQLStorage;
import ru.spbu.math.plok.model.storagesystem.StorageSystem;

public class BuildModule extends AbstractModule {

	private final static Logger log = LoggerFactory.getLogger(BuildModule.class);
	
	private Configurator configs;
	
	public BuildModule(Configurator configurator) {
		configs = configurator;
	}

	public void configure() {
		bindConstant().annotatedWith(Names.named("N")).to(configs.getN());
		bindConstant().annotatedWith(Names.named("T")).to(configs.getT());
		bindConstant().annotatedWith(Names.named("H")).to(configs.getH());
		bindConstant().annotatedWith(Names.named("A")).to(configs.getA());
		bindConstant().annotatedWith(Names.named("p")).to(configs.getPeriod());
		bindConstant().annotatedWith(Names.named("cacheUnitSize")).to(configs.getCacheUnitSize());
		bindConstant().annotatedWith(Names.named("storagePath")).to(configs.getStoragePath());
		initStorage();
		initVerbosity();
	}

	private void initVerbosity() {
		String verbosity = configs.getVerbosity();
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

	private void initStorage() {
		if (configs.getStorage().equalsIgnoreCase("sql"))
			bind(StorageSystem.class).to(SQLStorage.class);
		else {
			bind(StorageSystem.class).to(PLokerStorage.class);
		}
	}

}
