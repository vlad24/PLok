package ru.spbu.math.ais.plok.bench;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ru.spbu.math.ais.plok.MapKeyNames;
import ru.spbu.math.ais.plok.model.client.Client;
import ru.spbu.math.ais.plok.model.generator.Generator;
import ru.spbu.math.ais.plok.model.store.StorageSystem;
import ru.spbu.math.ais.plok.solvers.histogramsolver.UserChoice.Policy;

public class Tester {

	private static final Logger log = LoggerFactory.getLogger(Tester.class);


	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		log.info("Tester started");
		UserConfiguration config = new UserConfiguration(args);
		if (!config.isSolving()){
			AppConfig appConfig              = new AppConfig(config);
			Map<String, Object> solution     = appConfig.getSolution();
			Injector injector                = Guice.createInjector(appConfig);
			Generator generator              = injector.getInstance(Generator.class);
			Client client 		             = injector.getInstance(Client.class);
			StorageSystem store              = injector.getInstance(StorageSystem.class);
			log.info("Generator appending {} vectors...", config.getVectorAmount());
			generator.fill(store);
			log.info("Generator has fifnished.");
			log.debug("Stored blocks: {}", store.getBlockCount());
			log.info("Starting client...");
			Map<String, Object> finalReport;
			if (config.isTesting()){
				log.debug("In test mode...");
				finalReport = client.attack(store, appConfig.getHistoryAnalysisReport().getQueries());
			}else{
				log.debug("In real mode...");
				finalReport = client.attack(store,
						(Policy)solution.get(MapKeyNames.I_POLICY_KEY),
						(Policy)solution.get(MapKeyNames.J_POLICY_KEY),
						(Map<String,Object>)  solution.get(MapKeyNames.POLICIES_PARAMS),
						appConfig.getQueryAmount(),
						appConfig.getHistoryAnalysisReport().getTimeStep()
				);
			}
			log.info("Client has finished.");
			finalReport.putAll(appConfig.getSolution());
			String reportPath = ReportPrinter.print(
					config.getOutputFile(),
					config.isOutputAppended(),
					config.toString(),
					finalReport
				);
			log.info("Report has been printed to {}", reportPath);
			log.info("Target ratio: {}%", finalReport.get(MapKeyNames.TARGET_RATIO));
		}else{
			AppConfig appConfig              = new AppConfig(config);
			Map<String, Object> solution     = appConfig.getSolution();
			log.info("Solution: {}", solution.toString());
		}
		log.info("All done.");
	}


}

