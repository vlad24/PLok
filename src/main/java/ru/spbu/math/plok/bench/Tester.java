package ru.spbu.math.plok.bench;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ru.spbu.math.plok.model.client.Client;
import ru.spbu.math.plok.model.generator.Generator;
import ru.spbu.math.plok.model.storagesystem.StorageSystem;

public class Tester {

	private static Logger log = LoggerFactory.getLogger(Tester.class);


	public static void main(String[] args) throws Exception {
		log.debug("Tester started");
		Configuration configuration = new Configuration(args);
		log.info(configuration.toString());
		if (!configuration.isDebugging()){
			AppConfig appConfig              = new AppConfig(configuration);
			HashMap<String, Object> solution = appConfig.getSolution();
			Injector injector                = Guice.createInjector(appConfig);
			Generator generator              = injector.getInstance(Generator.class);
			Client client 		             = injector.getInstance(Client.class);
			StorageSystem store              = injector.getInstance(StorageSystem.class);
			log.info("Letting the generator to attack for {} msec", configuration.getAttackTimeMs());
			HashMap<String, Object> generatorReport = generator.attack(store);
			log.info("Stored {} blocks", store.getBlockCount());
			log.info("Let's have a break for {} msec", configuration.getPhaseBreakMs());
			TimeUnit.MILLISECONDS.sleep(configuration.getPhaseBreakMs());
			log.info("Break is over. Starting client...");
			HashMap<String, Object> queryReport = client.attack(store, solution, 
					configuration.isRepeatingHistory(), (Long)generatorReport.get("tFrom"), (Long)generatorReport.get("tTo"));
			log.info("Client has finished!");
			ReportPrinter.print(configuration, queryReport);
		}else{
			log.debug("In debug mode");
			debug(configuration);
		}
		log.info("All done!");
	}


	private static void debug(Configuration configuration) throws Exception {
		AppConfig appConfig              = new AppConfig(configuration);
		HashMap<String, Object> solution = appConfig.getSolution();
		log.debug(solution.toString());
	}


}

