package ru.spbu.math.plok.utils;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import ru.spbu.math.plok.bench.QueryGenerator;
import ru.spbu.math.plok.model.client.Query;
import ru.spbu.math.plok.solvers.Solver;
import ru.spbu.math.plok.solvers.histogramsolver.HParser;
import ru.spbu.math.plok.solvers.histogramsolver.UserChoice.Policy;

public class HistoryFileGenerator{


	/**
	 * Creates dataSet file.
	 * @param args [iPolicy, jPolicy, N, amount, fileName, policyParamsSlashedString]
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		if (args.length >= 4){
			Policy iPolicy      = Policy.valueOf(args[0]);
			Policy jPolicy      = Policy.valueOf(args[1]);
			int N               = Integer.parseInt(args[2]);
			int timeEnd          = Integer.parseInt(args[3]);
			String fileName     = args[4];
			String paramsString = null;
			if (args.length >= 5){
				paramsString = args[5];
			}
			File output = new File(fileName);
			FileWriter writer = new FileWriter(output);
			writer.write("#Created at " + System.currentTimeMillis() + ")\n");
			HashMap<String, Object> qGenParams = packParams(iPolicy, jPolicy, paramsString);
			QueryGenerator queryGenerator = new QueryGenerator(N, qGenParams);
			queryGenerator.setStart(1);
			queryGenerator.setEnd(timeEnd);
			queryGenerator.windUp();
			for (int t = 0; t < timeEnd; t++){
				Query q = queryGenerator.nextQuery();
				writer.write(
					new StringBuilder()
					.append(q.getTime()).append(HParser.ELEMENT_SEPARATOR)
					.append(q.getI1())  .append(HParser.ELEMENT_SEPARATOR)
					.append(q.getI2())  .append(HParser.ELEMENT_SEPARATOR)
					.append(q.getJ1())  .append(HParser.ELEMENT_SEPARATOR)
					.append(q.getJ2())  .append(HParser.ELEMENT_SEPARATOR)
					.append("\n")
					.toString()
				);
			}
			writer.flush();
			writer.close();
			 
		}else{
			throw new Exception("No output file specified!");
		}
		System.out.println("Done generating file");
	}

	private static HashMap<String, Object> packParams(Policy iPolicy, Policy jPolicy, String paramsString) {
		HashMap<String, Object> queryGeneratorParams = new HashMap<>();
		queryGeneratorParams.put(Solver.I_POLICY_KEY, iPolicy.toString());
		queryGeneratorParams.put(Solver.J_POLICY_KEY, jPolicy.toString());
		if (paramsString != null){
			String[] params = paramsString.split(HParser.HINTS_SEPARATOR);
			int currentParam = 0;
			if (iPolicy == Policy.HOT_RANGES){
				queryGeneratorParams.put(Solver.I_POLICY_HR_RANGES_KEY, params[currentParam]);
			}
			if (jPolicy == Policy.RECENT_TRACKING){
				
			}
		}
		return queryGeneratorParams;
	}

}
