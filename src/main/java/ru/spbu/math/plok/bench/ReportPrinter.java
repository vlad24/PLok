package ru.spbu.math.plok.bench;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Joiner;

public class ReportPrinter {

	private static Joiner.MapJoiner mapJoiner = Joiner.on("\n").withKeyValueSeparator("=");


	public static void print(Configurator configurator, HashMap<String, Object> queryReport) throws IOException{
		BufferedWriter outputWriter  = null;
		File report = new File(configurator.getOutput());
		if (!report.exists()){
			report.createNewFile();
		}
		outputWriter = new BufferedWriter(new FileWriter(report));
		outputWriter.write("#Configurator:\n" + configurator.toString() + "\n");
		outputWriter.write("#Results:\n" + mapJoiner.join((Map<?, ?>) queryReport) + "\n");
		outputWriter.flush();
		outputWriter.close();
	}
}
