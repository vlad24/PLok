package ru.spbu.math.plok.bench;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Joiner;

public class ReportPrinter {

	private static Joiner.MapJoiner mapJoiner = Joiner.on("\n").withKeyValueSeparator("=");
	private static String REPORT_FILENAME_PATTERN = "report_%s.txt";
	private static SimpleDateFormat ft = new SimpleDateFormat ("yyyy_MM_dd---E__hh_mm_ss");


	public static void print(Configuraton configurator, HashMap<String, Object> queryReport) throws IOException{
		BufferedWriter outputWriter  = null;
		Date currentDate = new Date();
		Path targetFilePath = Paths.get(
				configurator.getOutputPath(), 
				String.format(REPORT_FILENAME_PATTERN, ft.format(currentDate)))
				.toAbsolutePath();
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
