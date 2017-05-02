package ru.spbu.math.ais.plok.hfg;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ru.spbu.math.ais.plok.MapKeyNames;
import ru.spbu.math.ais.plok.bench.QueryGenerator;
import ru.spbu.math.ais.plok.model.client.Query;
import ru.spbu.math.ais.plok.solvers.HistoryPreprocessor;
import ru.spbu.math.ais.plok.solvers.histogramsolver.UserChoice.Policy;

public class HistoryFileGenerator{


	/**
	 * Creates dataSet file.
	 * @param args [iPolicy, jPolicy, N, timeStep, count, fileName, policyParamsSlashedString]
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		if (args.length >= 4){
			Policy iPolicy      = Policy.valueOf(args[0]);
			Policy jPolicy      = Policy.valueOf(args[1]);
			int N               = Integer.parseInt(args[2]);
			int timeStep        = Integer.parseInt(args[3]);
			int queriesCount    = Integer.parseInt(args[4]);
			String fileName     = args[5];
			String paramsString = null;
			if (args.length >= 6){
				paramsString = args[6];
			}
			File output = new File(fileName);
			FileWriter writer = new FileWriter(output);
			writer.write(
					new StringBuilder()
					.append("#Created by HistoryFileGenerator at ").append(new Date().toString()).append("\n")
					.append("#Parameters: ").append(Arrays.toString(args)).append("\n")
					.toString()
					);
			HashMap<String, Object> qGenParams = packParams(N, iPolicy, jPolicy, paramsString);
			writer.write("@" + qGenParams.get(MapKeyNames.N_KEY) + "\n");
			QueryGenerator queryGenerator = new QueryGenerator(N, iPolicy, jPolicy, (Map<String, Object>) qGenParams.get(MapKeyNames.POLICIES_PARAMS));
			long t = 0;
			for (int i = 0; i <= queriesCount; i++){
				t += timeStep;
				Query q = queryGenerator.nextQuery(t);
				writer.write(
						new StringBuilder()
						.append(q.getTime()).append(HistoryPreprocessor.ELEMENT_SEPARATOR)
						.append(q.getI1())  .append(HistoryPreprocessor.ELEMENT_SEPARATOR)
						.append(q.getI2())  .append(HistoryPreprocessor.ELEMENT_SEPARATOR)
						.append(q.getJ1())  .append(HistoryPreprocessor.ELEMENT_SEPARATOR)
						.append(q.getJ2())
						.append("\n")
						.toString()
						);
			}
			writer.flush();
			writer.close();
			System.out.println("Done generating file " + fileName);
		}else{
			throw new Exception("No output file specified!");
		}
	}

	private static HashMap<String, Object> packParams(int N, Policy iPolicy, Policy jPolicy, String paramsString) {
		HashMap<String, Object> queryGeneratorParams = new HashMap<>();
		queryGeneratorParams.put(MapKeyNames.N_KEY, N);
		queryGeneratorParams.put(MapKeyNames.I_POLICY_KEY, iPolicy);
		queryGeneratorParams.put(MapKeyNames.J_POLICY_KEY, jPolicy);
		Map<String, Object> policiesParams = new HashMap<>(); 
		if (paramsString != null){
			String[] params = paramsString.split(HistoryPreprocessor.HINTS_SEPARATOR);
			int currentParam = 0;
			if (iPolicy == Policy.HOT_RANGES){
				policiesParams.put(MapKeyNames.I_POLICY_HR_RANGES_KEY, params[currentParam++]);
			}
			if (jPolicy == Policy.RECENT_TRACKING){
				policiesParams.put(MapKeyNames.J_POLICY_RT_WINDOW_KEY, Long.valueOf(params[currentParam++]));
			}
			queryGeneratorParams.put(MapKeyNames.POLICIES_PARAMS, policiesParams);
		}
		return queryGeneratorParams;
	}

}
