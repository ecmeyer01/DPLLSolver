package solver.sat;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**This class is smarter than the DPLL solver (10x faster) using some conflict detection and heuristics */
public class SmarterSolver {
  private List<List<Set<Integer>>> clauses = new ArrayList<>();
  private int numVars;
  private List<List<Integer>> positiveMembership;
  private List<List<Integer>> negativeMembership;
  private boolean[] assignments;
  private boolean[] originalAssignments;
  private Stack<Integer> branchVariables = new Stack<>();
  private int level = 0;
  private boolean solved = false;

  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: java CDCLSolver <input_file>");
      return;
    }
    String path = args[0];
    SmarterSolver solver = new SmarterSolver();
    solver.solve(path);
  }

  public void solve(String path) {
    initialize(path);
    solved = solveInternal();
    displayResult(solved, path);
  }

  private void initialize(String path) {
    try (Scanner scanner = new Scanner(new File(path))) {
      // Skip lines if not in "input" format
      if (!path.contains("input")) {
        scanner.nextLine();
        scanner.nextLine();
      }
      // Read number of variables
      numVars = Integer.parseInt(scanner.nextLine().split(" ")[2]);
      positiveMembership = new ArrayList<>(numVars + 1);
      negativeMembership = new ArrayList<>(numVars + 1);
      for (int i = 0; i <= numVars; i++) {
        positiveMembership.add(new ArrayList<>());
        negativeMembership.add(new ArrayList<>());
      }
      // Read clauses
      int clauseIndex = 0;
      while (scanner.hasNextLine()) {
        String[] tokens = scanner.nextLine().split(" ");
        Set<Integer> clause = new HashSet<>();
        boolean shouldIncludeClause = true;
        for (String token : tokens) {
          int val = Integer.parseInt(token);
          if (val != 0) {
            if (clause.contains(-val)) {
              shouldIncludeClause = false;
            }
            clause.add(val);
          }
        }
        if (shouldIncludeClause && !clause.isEmpty()) {
          for (int val : clause) {
            if (val > 0) {
              positiveMembership.get(val).add(clauseIndex);
            } else {
              negativeMembership.get(-val).add(clauseIndex);
            }
          }
          clauses.add(new ArrayList<>());
          clauses.get(clauseIndex).add(new HashSet<>(clause));
          clauseIndex++;
        }
      }
    } catch (FileNotFoundException e) {
      System.err.println("File not found: " + path);
      return;
    }

    // Initialize assignments
    assignments = new boolean[numVars + 1];
    originalAssignments = new boolean[numVars + 1];
    for (int i = 1; i <= numVars; i++) {
      assignments[i] = positiveMembership.get(i).size() >= negativeMembership.get(i).size();
      originalAssignments[i] = assignments[i];
    }
  }

  private boolean solveInternal() {
    int b = 1; // Start with the first variable
    while (b <= numVars) {
      // Branch Step
      level++;
      boolean wasPresent = false;
      if (assignments[b]) {
        for (int i : positiveMembership.get(b)) {
          if (clauses.get(i).get(clauses.get(i).size() - 1).contains(b)) {
            wasPresent = true;
            clauses.get(i).add(new HashSet<>());
          }
        }
        for (int i : negativeMembership.get(b)) {
          if (clauses.get(i).get(clauses.get(i).size() - 1).contains(-b)) {
            wasPresent = true;
            Set<Integer> newClause = new HashSet<>(clauses.get(i).get(clauses.get(i).size() - 1));
            newClause.remove(-b);
            clauses.get(i).add(newClause);
          }
        }
      } else {
        for (int i : positiveMembership.get(b)) {
          if (clauses.get(i).get(clauses.get(i).size() - 1).contains(b)) {
            wasPresent = true;
            Set<Integer> newClause = new HashSet<>(clauses.get(i).get(clauses.get(i).size() - 1));
            newClause.remove(b);
            clauses.get(i).add(newClause);
          }
        }
        for (int i : negativeMembership.get(b)) {
          if (clauses.get(i).get(clauses.get(i).size() - 1).contains(-b)) {
            wasPresent = true;
            clauses.get(i).add(new HashSet<>());
          }
        }
      }

      if (!wasPresent) {
        b++;
        continue;
      } else {
        if (assignments[b] == originalAssignments[b]) {
          branchVariables.push(b);
        }
      }

      // Unit Propagation Step
      List<Set<Integer>> potentialClauses = new ArrayList<>();
      for (List<Set<Integer>> clauseHistory : clauses) {
        potentialClauses.add(new HashSet<>(clauseHistory.get(clauseHistory.size() - 1)));
      }
      Set<Integer> unitValuesToRemove = new HashSet<>();
      boolean conflict = false;
      while (true) {
        for (int i = 0; i < potentialClauses.size(); i++) {
          if (potentialClauses.get(i).size() == 1) {
            int u = potentialClauses.get(i).iterator().next();
            if (!unitValuesToRemove.contains(-u)) {
              unitValuesToRemove.add(u);
            } else {
              conflict = true;
              break;
            }
          }
        }
        if (conflict || unitValuesToRemove.isEmpty()) {
          break;
        }
        for (int u : unitValuesToRemove) {
          if (u > 0) {
            assignments[u] = true;
            for (int i : positiveMembership.get(u)) {
              potentialClauses.get(i).clear();
            }
            for (int i : negativeMembership.get(u)) {
              potentialClauses.get(i).remove(-u);
            }
          } else {
            assignments[-u] = false;
            for (int i : positiveMembership.get(-u)) {
              potentialClauses.get(i).remove(-u);
            }
            for (int i : negativeMembership.get(-u)) {
              potentialClauses.get(i).clear();
            }
          }
        }
        unitValuesToRemove.clear();
      }

      // Conflict Analysis and Clause Learning
      if (conflict) {
        if (branchVariables.isEmpty()) {
          return false; // UNSAT
        }
        b = branchVariables.pop();
        level--;
        for (List<Set<Integer>> clauseHistory : clauses) {
          int j = clauseHistory.size() - 1;
          while (j > 0 && !clauseHistory.get(j).isEmpty() && clauseHistory.get(j).iterator().next() > b) {
            j--;
          }
          clauseHistory.subList(j + 1, clauseHistory.size()).clear();
        }
        for (int v = numVars; v > b; v--) {
          assignments[v] = originalAssignments[v];
        }
        assignments[b] = false;
      } else {
        // Append results of the UP (and PLE) Steps
        for (int i = 0; i < clauses.size(); i++) {
          clauses.get(i).add(new HashSet<>(potentialClauses.get(i)));
        }
        b++;
        level++;
      }
    }
    return true; // SAT
  }


  public void displayResult(boolean finalResult, String path) {
    long elapsedTimeMillis = System.currentTimeMillis() - startTime;
    double elapsedTimeSeconds = elapsedTimeMillis / 1000.0; // Convert to seconds
    String formattedTime = String.format("%.2f", elapsedTimeSeconds); // Format to two decimal places

    if (finalResult) {
      StringBuilder assignmentsDisplay = new StringBuilder("{Instance: " + path + ", Time: " + formattedTime + ", Result: SAT, Solution:");
      for (int i = 1; i <= numVars; i++) {
        assignmentsDisplay.append(" ").append(i).append(" ").append(assignments[i] ? "true" : "false");
      }
      assignmentsDisplay.append("}");
      System.out.println(assignmentsDisplay);
    } else {
      System.out.println("{Instance: " + path + ", Time: " + formattedTime + ", Result: UNSAT}");
    }
  }

  private long startTime;

  {
    startTime = System.currentTimeMillis();
  }

//  public Map<Integer, Boolean> getModel() {
//    Map<Integer, Boolean> model = new HashMap<>();
//    for (int i = 1; i <= numVars; i++) {
//      model.put(i, assignments[i]);
//    }
//    return model;
//  }
}