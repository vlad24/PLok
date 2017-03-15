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
import ru.spbu.math.plok.solver.Solver;

public class Tester {

	private static Logger log = LoggerFactory.getLogger(Tester.class);


	public static void main(String[] args) throws Exception {
		log.debug("Tester started");
		log.debug("Configuring and solving...");
		Configuraton configuraton = new Configuraton();
		configuraton.initFromArgs(args);
		System.out.println(configuraton);
		if (!configuraton.isDebugging()){
			AppConfig appConfig = new AppConfig(configuraton);
			Injector injector = Guice.createInjector();
			Solver solver                    = injector.getInstance(Solver.class);
			HashMap<String, Object> solution = solver.solvePLTask();
			appConfig.setPL((Integer) solution.get("P"), (Integer) solution.get("L"));
			Generator generator              = injector.getInstance(Generator.class);
			Client client 		             = injector.getInstance(Client.class);
			StorageSystem store              = injector.getInstance(StorageSystem.class);
			log.info("Letting the generator to attack for {} msec", configuraton.getT());
			HashMap<String, Object> generatorReport = generator.attack(store);
			log.info("Stored {} blocks", store.getBlockCount());
			log.info("Let's have a break for {} msec", configuraton.getPhaseBreak());
			TimeUnit.MILLISECONDS.sleep(configuraton.getPhaseBreak());
			log.info("Break is over. Starting client...");
			HashMap<String, Object> queryReport = client.attack(store, solution, 
					configuraton.isRepeatingHistory(), (Long)generatorReport.get("tFrom"), (Long)generatorReport.get("tTo"));
			log.info("Client has finished!");
			ReportPrinter.print(configuraton, queryReport);
		}
		log.info("All done!");
	}


}

