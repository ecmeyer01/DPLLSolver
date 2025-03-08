package solver.sat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DPLLSolver {
  private Set<Integer> model = new HashSet<>();
  private Set<Integer> vars;
  private List<Set<Integer>> originalClauses;

  public boolean solve(SATInstance instance) {
    this.vars = new HashSet<>(instance.vars);
    this.originalClauses = new ArrayList<>(instance.clauses);
    return dpll(new ArrayList<>(this.originalClauses), new HashSet<>(this.vars), new HashSet<>());
  }

  private boolean dpll(List<Set<Integer>> clauses, Set<Integer> symbols, Set<Integer> model) {
    // Base case 1: if all clauses are satisfied
    if (clauses.isEmpty()) {
      this.model = new HashSet<>(model);
      return true;
    }

    // Base case 2: check for contradiction
    if (clauses.stream().anyMatch(Set::isEmpty)) {
      return false;
    }

    // these will be passed down recursively
    List<Set<Integer>> simplifiedClauses = new ArrayList<>(clauses);
    Set<Integer> remainingSymbols = new HashSet<>(symbols);
    Set<Integer> currentModel = new HashSet<>(model);

    boolean changed;
    do {
      changed = false;

      // eliminate pure symbols
      List<Integer> pureSymbols = findPureSymbols(remainingSymbols, simplifiedClauses);
      if (!pureSymbols.isEmpty()) {
        for (Integer symbol : pureSymbols) {
          simplifiedClauses.removeIf(clause -> clause.contains(symbol));
          remainingSymbols.remove(Math.abs(symbol));
          currentModel.add(symbol);
          changed = true;
        }
      }

      // eliminate unit clauses
      List<Integer> unitClauses = findUnitClauses(simplifiedClauses);
      if (!unitClauses.isEmpty()) {
        for (Integer unit : unitClauses) {
          simplifiedClauses = propagateUnit(simplifiedClauses, unit);
          remainingSymbols.remove(Math.abs(unit));
          currentModel.add(unit);
          changed = true;
        }
      }
    } while (changed);

    // again, if empty, stop because we hit base case 1
    if (simplifiedClauses.isEmpty()) {
      this.model = new HashSet<>(currentModel);
      return true;
    }

    // base case 2
    if (simplifiedClauses.stream().anyMatch(Set::isEmpty)) {
      return false;
    }

    // BRANCHING
    Integer p = remainingSymbols.iterator().next();
    remainingSymbols.remove(p);

    // try branching on p = true
    Set<Integer> modelTrue = new HashSet<>(currentModel);
    modelTrue.add(p);
    List<Set<Integer>> clausesTrue = new ArrayList<>(simplifiedClauses);
    clausesTrue = propagateUnit(clausesTrue, p);
    if (dpll(clausesTrue, new HashSet<>(remainingSymbols), modelTrue)) {
      return true;
    }

    // try branching on p = false
    Set<Integer> modelFalse = new HashSet<>(currentModel);
    modelFalse.add(-p);
    List<Set<Integer>> clausesFalse = new ArrayList<>(simplifiedClauses);
    clausesFalse = propagateUnit(clausesFalse, -p);
    return dpll(clausesFalse, new HashSet<>(remainingSymbols), modelFalse);
  }

  private List<Integer> findPureSymbols(Set<Integer> symbols, List<Set<Integer>> clauses) {
    // every integer to their truth values
    Map<Integer, Set<Boolean>> polarities = new HashMap<>();

    for (Integer symbol : symbols) {
      polarities.put(symbol, new HashSet<>());
    }
    for (Set<Integer> clause : clauses) {
      for (Integer literal : clause) {
        int variable = Math.abs(literal);
        boolean isPositive = literal > 0;
        if (symbols.contains(variable)) {
          polarities.get(variable).add(isPositive);
        }
      }
    }

    // find pure symbols
    List<Integer> pureSymbols = new ArrayList<>();
    for (Map.Entry<Integer, Set<Boolean>> entry : polarities.entrySet()) {
      Set<Boolean> occurrences = entry.getValue();
      // if variable only appears positive or only appears negative
      if (occurrences.size() == 1) {
        Boolean isPositive = occurrences.iterator().next();
        pureSymbols.add(isPositive ? entry.getKey() : -entry.getKey());
      }
    }

    return pureSymbols;
  }

  private List<Integer> findUnitClauses(List<Set<Integer>> clauses) {
    List<Integer> units = new ArrayList<>();
    for (Set<Integer> clause : clauses) {
      if (clause.size() == 1) {
        units.add(clause.iterator().next());
      }
    }
    return units;
  }

  private List<Set<Integer>> propagateUnit(List<Set<Integer>> clauses, Integer unit) {
    List<Set<Integer>> result = new ArrayList<>();
    for (Set<Integer> clause : clauses) {
      if (clause.contains(unit)) {
        continue; // clause is satisfied, no need to pay it any attention or return
      }
      if (clause.contains(-unit)) {
        Set<Integer> newClause = new HashSet<>(clause);
        newClause.remove(-unit);
        result.add(newClause);
      } else {
        result.add(new HashSet<>(clause));
      }
    }
    return result;
  }

  public Map<Integer, Boolean> getModel() {
    Map<Integer, Boolean> modelMap = new HashMap<>();
    for (Integer var : this.model) {
      modelMap.put(Math.abs(var), var > 0);
    }
    return modelMap;
  }
}