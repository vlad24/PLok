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
		Configuraton configuraton = new Configuraton(args);
		log.info(configuraton.toString());
		if (!configuraton.isDebugging()){
			AppConfig appConfig              = new AppConfig(configuraton);
			HashMap<String, Object> solution = appConfig.getSolution();
			Injector injector                = Guice.createInjector(appConfig);
			Generator generator              = injector.getInstance(Generator.class);
			Client client 		             = injector.getInstance(Client.class);
			StorageSystem store              = injector.getInstance(StorageSystem.class);
			log.info("Letting the generator to attack for {} msec", configuraton.getAttackTimeMs());
			HashMap<String, Object> generatorReport = generator.attack(store);
			log.info("Stored {} blocks", store.getBlockCount());
			log.info("Let's have a break for {} msec", configuraton.getPhaseBreakMs());
			TimeUnit.MILLISECONDS.sleep(configuraton.getPhaseBreakMs());
			log.info("Break is over. Starting client...");
			HashMap<String, Object> queryReport = client.attack(store, solution, 
					configuraton.isRepeatingHistory(), (Long)generatorReport.get("tFrom"), (Long)generatorReport.get("tTo"));
			log.info("Client has finished!");
			ReportPrinter.print(configuraton, queryReport);
		}else{
			log.debug("In debug mode");
			debug(configuraton);
		}
		log.info("All done!");
	}


	private static void debug(Configuraton configuration) throws Exception {
		AppConfig appConfig              = new AppConfig(configuration);
		HashMap<String, Object> solution = appConfig.getSolution();
		log.debug(solution.toString());
	}


}

