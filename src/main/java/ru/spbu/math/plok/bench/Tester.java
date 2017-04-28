package ru.spbu.math.plok.bench;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ru.spbu.math.plok.MapKeyNames;
import ru.spbu.math.plok.model.client.Client;
import ru.spbu.math.plok.model.generator.Generator;
import ru.spbu.math.plok.model.storagesystem.StorageSystem;
import ru.spbu.math.plok.solvers.histogramsolver.UserChoice.Policy;

public class Tester {

	private static Logger log = LoggerFactory.getLogger(Tester.class);


	public static void main(String[] args) throws Exception {
		log.debug("Tester started");
		UserConfiguration configuration = new UserConfiguration(args);
		log.info(configuration.toString());
		if (!configuration.isDebugging()){
			AppConfig appConfig              = new AppConfig(configuration);
			Map<String, Object> solution     = appConfig.getSolution();
			Injector injector                = Guice.createInjector(appConfig);
			Generator generator              = injector.getInstance(Generator.class);
			Client client 		             = injector.getInstance(Client.class);
			StorageSystem store              = injector.getInstance(StorageSystem.class);
			log.info("Generator appending {} vectors ", configuration.getVectorAmount());
			generator.fill(store);
			log.debug("Stored {} blocks", store.getBlockCount());
			log.info("Starting client...");
			Map<String, Object> clientReport;
			if (configuration.isTesting()){
				clientReport = client.attack(store, appConfig.getHistoryAnalysisReport().getQueries());
			}else{
				clientReport = client.attack(store,
						(Policy)solution.get(MapKeyNames.I_POLICY_KEY),
						(Policy)solution.get(MapKeyNames.J_POLICY_KEY),
						(Map<String,Object>)  solution.get(MapKeyNames.POLICIES_PARAMS),
						configuration.getReadPeriod()
				);
			}
			log.info("Client has finished!");
			String reportFilePath = ReportPrinter.print(configuration, clientReport);
			log.info("Report has been printed to {}", reportFilePath);
			log.info("Target ratio: {}%", clientReport.get(MapKeyNames.TARGET_RATIO));
		}else{
			log.debug("In debug mode");
			debug(configuration);
		}
		log.info("All done!");
	}


	private static void debug(UserConfiguration configuration) throws Exception {
		AppConfig appConfig              = new AppConfig(configuration);
		Map<String, Object> solution     = appConfig.getSolution();
		log.debug(solution.toString());
	}


}

