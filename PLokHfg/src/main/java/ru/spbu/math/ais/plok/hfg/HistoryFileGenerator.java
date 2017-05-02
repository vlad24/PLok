package ru.spbu.math.ais.plok.hfg;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ru.spbu.math.ais.plok.bench.QueryGenerator;
import ru.spbu.math.ais.plok.model.client.Query;
import ru.spbu.math.ais.plok.solvers.HistoryPreprocessor;

public class HistoryFileGenerator{
	
	private final static org.slf4j.Logger log = LoggerFactory.getLogger(QueryGenerator.class);
	
	/**
	 * Creates dataSet file.
	 * @see HFGConfiguration
	 */
	public static void main(String[] args) throws Exception{
		HFGConfiguration config = new HFGConfiguration(args);
		((ch.qos.logback.classic.Logger)LoggerFactory.getLogger("ROOT")).setLevel(Level.valueOf(config.getVerbosity()));
		File output       = new File(config.getHistoryFile());
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		writer.write(
				new StringBuilder()
				.append("#Created by HistoryFileGenerator at ").append(new Date().toString()).append("\n")
				.append("#Parameters: ").append(config.toString())
				.toString()
				);
		writer.newLine();
		writer.write("@" + config.getVectorSize());
		writer.newLine();
		QueryGenerator queryGenerator = new QueryGenerator(config.getVectorSize(), config.getiPolicy(),
				config.getjPolicy(), config.getPoliciesParams());
		long time = 0;
		for (int i = 0; i < config.getQueriesCount(); i++){
			time += config.getTimeStep();
			Query query = queryGenerator.nextQuery(time);
			writer.write(
						new StringBuilder()
						.append(query.getTime()).append(HistoryPreprocessor.ELEMENT_SEPARATOR)
						.append(query.getI1())  .append(HistoryPreprocessor.ELEMENT_SEPARATOR)
						.append(query.getI2())  .append(HistoryPreprocessor.ELEMENT_SEPARATOR)
						.append(query.getJ1())  .append(HistoryPreprocessor.ELEMENT_SEPARATOR)
						.append(query.getJ2())
						.toString()
					);
			writer.newLine();
		}
		writer.flush();
		writer.close();
		log.info("Done generating file {}", config.getHistoryFile());
	}


}
