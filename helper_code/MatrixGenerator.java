import java.io.File;
import java.io.FileWriter;
import java.util.Random;

public class MatrixGenerator{

	private static final String CSV_SEPARATOR = ",";
	private static final String OFFSETS_SEPARATOR = "/";

	public static void main(String[] args) throws Exception{
		if (args.length >= 4){
			int n = Integer.parseInt(args[0]);
			int m = Integer.parseInt(args[1]);
			float max = Float.parseFloat(args[2]);
			String fileName = args[3];
			String startLine = " (Created at " + System.currentTimeMillis() + ")\n";
			if (args.length >= 5){
				startLine  = args[4] + startLine;
			}
			float sum = 0f;
			Random random = new Random();
			float[][] matrix  = new float[n][m];
			float remainder = 1 - max;
			int iMax = random.nextInt(n);
			int jMax = random.nextInt(m);
			int pOffset= n + random.nextInt(2 * n);
			int lOffset = m + random.nextInt(2 * m);
			matrix[iMax][jMax] = max;
			for (int i = 0; i < n; i++){
				for (int j = 0; j < m; j++){
					if (i != iMax || j != jMax){
						float value =  random.nextFloat();
						matrix[i][j] = remainder * value;
						sum += value;
					}
				}
			}
			for (int i = 0; i < n; i++){
				for (int j = 0; j < m; j++){
					if (i != iMax || j != jMax){
						matrix[i][j] /= sum;
					}
				}
			}
			File output = new File(fileName);
			FileWriter writer = new FileWriter(output);
			writer.write("# " + startLine);
			writer.write(pOffset + OFFSETS_SEPARATOR + lOffset + "\n");
			for (int i = 0; i < n; i++){
				for (int j = 0; j < m; j++){
					writer.write(String.valueOf(matrix[i][j]));
					if (j != m - 1){
						writer.write(CSV_SEPARATOR);
					}
				}
				writer.write("\n");
			}
			writer.flush();
			writer.close();
			 
		}else{
			throw new Exception("No output file specified!");
		}
		System.out.println("Done generating file");
	}

}
