package ru.spbu.math.plok.bench;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ru.spbu.math.plok.model.client.Client;
import ru.spbu.math.plok.model.generator.Generator;
import ru.spbu.math.plok.model.storagesystem.StorageSystem;

public class Tester {

	private static Logger log = LoggerFactory.getLogger(Tester.class);


	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, ParseException {
		log.debug("Tester started");
		log.debug("Configuring...");
		Configurator configurator = new Configurator();
		configurator.initFromArgs(args);
		System.out.println(configurator);
		if (!configurator.isDebugging()){
			Injector injector = Guice.createInjector(new BuildModule(configurator));
			StorageSystem store = injector.getInstance(StorageSystem.class);
			Generator generator = injector.getInstance(Generator.class);
			Client client 		= injector.getInstance(Client.class);
			log.info("Letting the generator to attack for {} msec", configurator.getT());
			HashMap<String, Object> generatorReport = generator.attack(store);
			log.info("Stored {} blocks", store.getBlockCount());
			log.info("Let's have a break for {} msec", configurator.getPhaseBreak());
			TimeUnit.MILLISECONDS.sleep(configurator.getPhaseBreak());
			log.info("Break is over. Starting client...");
			client.setQueryTimeBounds((Long)generatorReport.get("attackStart"), (Long)generatorReport.get("attackEnd"));
			HashMap<String, Object> queryReport = client.attack(store);
			log.info("Client has finished!");
			ReportPrinter.print(configurator, queryReport);
		}
		log.info("All done!");
	}


}

