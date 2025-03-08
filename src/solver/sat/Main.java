package solver.sat;

import java.util.Map;
import java.util.Set;

/**
 * Usage example: read a given cnf instance file to create 
 * a simple sat instance object and print out its parameter fields. 
 */
public class Main
{  
  public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("Usage: java Main <cnf file>");
			return;
		}

		String input = args[0];

		Timer watch = new Timer();
		watch.start();

		SATInstance instance = DimacsParser.parseCNFFile(input);
		SmarterSolver solver = new SmarterSolver(); // DPLLSolver();
		solver.solve(input); //boolean result = solver.solve(instance);

	}

//	public static void main(String[] args) throws Exception {
//		if (args.length == 0) {
//			System.out.println("Usage: java Main <cnf file>");
//			return;
//		}
//
//		String input = args[0];
//		Path path = Paths.get(input);
//		String filename = path.getFileName().toString();
//
//		Timer watch = new Timer();
//		watch.start();
//
//		SATInstance instance = DimacsParser.parseCNFFile(input);
//		 DPLLSolver dpllSolver = new DPLLSolver();
//
//		boolean result = dpllSolver.solve(instance); // Solve using CDCL
//
//		watch.stop();
//
//		// Get the model map from CDCLSolver
//		Map<Integer, Boolean> modelMap = dpllSolver.getModel();
//		if (result) {
//			StringBuilder solution = new StringBuilder();
//			int count = 0;
//			int size = modelMap.size() - 1; // Exclude the solved status entry
//
//			for (Map.Entry<Integer, Boolean> entry : modelMap.entrySet()) {
//				if (entry.getKey() != -1) { // Skip the solved status entry
//					solution.append(entry.getKey()).append(" ").append(entry.getValue() ? "true" : "false");
//					if (++count < size) {
//						solution.append(" ");
//					}
//				}
//			}
//
//			boolean isValid = validateSolution(modelMap, instance);
//
//			System.out.println("Validated? " + isValid);
//			System.out.printf("{\"Instance\": \"%s\", \"Time\": %.2f, \"Result\": \"SAT\", \"Solution\": \"%s\"}%n",
//					filename, watch.getTime(), solution);
//		} else {
//			System.out.printf("{\"Instance\": \"%s\", \"Time\": %.2f, \"Result\": \"UNSAT\"}%n",
//					filename, watch.getTime());
//		}
//}

	public static boolean validateSolution(Map<Integer, Boolean> modelMap, SATInstance sat) {
		for (Set<Integer> clause : sat.clauses) {
			boolean clauseSatisfied = false;
			for (Integer var : clause) {
				boolean isPositive = var > 0;
				int absVar = Math.abs(var);
				Boolean value = modelMap.get(absVar);
				if (value != null && ((isPositive && value) || (!isPositive && !value))) {
					clauseSatisfied = true;
					break;
				}
			}
			if (!clauseSatisfied) {
				return false;
			}
		}
		return true;
	}
}
