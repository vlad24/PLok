package ru.spbu.math.ais.plok.bench;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import ru.spbu.math.ais.plok.utils.MapsUtils;

public class ReportPrinter {


	private final static Logger log = LoggerFactory.getLogger(ReportPrinter.class);

	private final static String REPORT_DIR = "reports";
	private static final String ELEMENT_SEPARATOR = ",\t";
	private final static String GENERATED_FILENAME_PATTERN = "report_%s.txt";
	private final static SimpleDateFormat GENERATED_FILENAME_ID = new SimpleDateFormat("yyyy_MM_dd--E_kk_mm_ss");
	private static Joiner.MapJoiner mapJoiner = Joiner.on(ELEMENT_SEPARATOR).withKeyValueSeparator("=");
	


	public static String print(String outputFilePath, boolean outputAppended, String reportPrefix, Map<String, Object> queryReport) throws IOException{
		BufferedWriter outputWriter  = null;
		Path targetFilePath = null;
		targetFilePath = (outputFilePath == null) ?  getUniqueReportFilePath() :  Paths.get(outputFilePath).toAbsolutePath(); 
		File reportFile = new File(targetFilePath.toAbsolutePath().toString());
		createWithParentsIfNotExists(reportFile);
		outputWriter = new BufferedWriter(new FileWriter(reportFile, outputAppended));
		outputWriter.write(reportPrefix);
		outputWriter.write(ELEMENT_SEPARATOR);
		if (queryReport != null){
			outputWriter.write(mapJoiner.join((Map<?, ?>) MapsUtils.flatten(queryReport)));
		}else{
			outputWriter.write("#No report generated. Check program logs.");
		}
		outputWriter.newLine();
		outputWriter.flush();
		outputWriter.close();
		return targetFilePath.toString();
	}


	private static void createWithParentsIfNotExists(File reportFile) throws IOException {
		reportFile.getParentFile().mkdirs();
		if (!reportFile.exists()){
			reportFile.createNewFile();
			log.debug("Created new file {}", reportFile.getAbsolutePath());
		}
	}

	private static Path getUniqueReportFilePath() {
		return Paths.get(REPORT_DIR, String.format(GENERATED_FILENAME_PATTERN, GENERATED_FILENAME_ID.format(new Date()))).toAbsolutePath();
	}
}
