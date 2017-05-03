package ru.spbu.math.ais.plok.hfg;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
		writer.write(HistoryPreprocessor.HEAD_STRING_INDICATOR + config.getVectorSize());
		writer.newLine();
		if (config.isIncludeHints()){
			writeHints(config, writer);
		}
		QueryGenerator queryGenerator = new QueryGenerator(config.getVectorSize(), config.getiPolicy(),
				config.getjPolicy(), config.getPoliciesParams());
		long time = 0;
		for (int i = 0; i < config.getQueriesCount(); i++){
			time += config.getTimeStep();
			Query query = queryGenerator.nextQuery(time);
			writer.write(toFileEntry(query));
			writer.newLine();
		}
		writer.flush();
		writer.close();
		log.info("Done generating file {}", config.getHistoryFile());
	}

	private static String toFileEntry(Query query) {
		return new StringBuilder()
		.append(query.getTime()).append(HistoryPreprocessor.ELEMENT_SEPARATOR)
		.append(query.getI1())  .append(HistoryPreprocessor.ELEMENT_SEPARATOR)
		.append(query.getI2())  .append(HistoryPreprocessor.ELEMENT_SEPARATOR)
		.append(query.getJ1())  .append(HistoryPreprocessor.ELEMENT_SEPARATOR)
		.append(query.getJ2())
		.toString();
	}

	private static void writeHints(HFGConfiguration config, BufferedWriter writer) throws IOException {
		writer.write(HistoryPreprocessor.HINT_STRING_INDICATOR);
		writer.write(config.getiPolicy().toString());
		writer.write(HistoryPreprocessor.HINTS_SEPARATOR);
		writer.write(config.getjPolicy().toString());
		if (config.getWindowSize() != null){
			writer.write(HistoryPreprocessor.HINTS_SEPARATOR);
			writer.write(config.getWindowSize().toString());
		}
		if (config.getHotRanges() != null){
			writer.write(HistoryPreprocessor.HINTS_SEPARATOR);
			writer.write(config.getHotRanges().toString());
		}
		writer.newLine();
	}


}
