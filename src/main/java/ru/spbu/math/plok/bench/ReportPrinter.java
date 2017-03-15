package ru.spbu.math.plok.bench;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Joiner;

public class ReportPrinter {

	private static Joiner.MapJoiner mapJoiner = Joiner.on("\n").withKeyValueSeparator("=");
	private static String REPORT_FILENAME_PATTERN = "report_%d.txt";


	public static void print(Configuraton configurator, HashMap<String, Object> queryReport) throws IOException{
		BufferedWriter outputWriter  = null;
		Path targetFilePath = Paths.get(configurator.getOutput(), String.format(REPORT_FILENAME_PATTERN, System.currentTimeMillis())).toAbsolutePath();
		File reportFile = new File(targetFilePath.toString());
		reportFile.getParentFile().mkdirs();
		if (!reportFile.exists()){
			reportFile.createNewFile();
		}
		outputWriter = new BufferedWriter(new FileWriter(reportFile));
		outputWriter.write("#Configurator:\n" + configurator.toString() + "\n");
		if (queryReport != null){
			outputWriter.write("#Results:\n" + mapJoiner.join((Map<?, ?>) queryReport) + "\n");
		}else{
			outputWriter.write("#Results:\n No report generated. Check program logs. \n");
		}
		outputWriter.flush();
		outputWriter.close();
	}
}
