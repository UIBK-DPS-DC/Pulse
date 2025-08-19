package at.ac.uibk.dps.pulse.utils.common;

import at.ac.uibk.dps.pulse.global.model.Candidate;
import at.ac.uibk.dps.pulse.local.assignment.PulseAssignmentProblem;
import java.util.ArrayList;
import java.util.List;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryVariable;

public class Utils {

  public static int roundDownToMultiple(int x, int n) {
    assert n > 0 : "n must be > 0";
    final var rounded = (x / n) * n;
    return Math.max(rounded, n);
  }

  /**
   * Converts a solution to a list of candidates.
   * @param solution the solution to convert
   * @param assignmentProblem the assignment problem
   * @return a list of lists of candidates, where each inner list corresponds to a service
   */
  public static List<List<Candidate>> solutionToCandidates(
    Solution solution,
    PulseAssignmentProblem assignmentProblem
  ) {
    final var candidates = new ArrayList<List<Candidate>>();

    final var assignmentCosts = assignmentProblem.getAssignmentCosts();

    final var n = assignmentProblem.getServices().size();
    final var m = assignmentProblem.getResources().size();

    for (int k = 0; k < n; ++k) {
      if (candidates.size() <= k) {
        candidates.add(new ArrayList<>());
      }
      final var f = assignmentProblem.getFeasibleResources()[k];

      final var assigned = new ArrayList<Integer>();
      final var bitset = ((BinaryVariable) solution.getVariable(k)).getBitSet();
      for (int x = bitset.nextSetBit(0); x >= 0; x = bitset.nextSetBit(x + 1)) {
        assigned.add(f[x]);
      }

      for (int i = 0; i < m; ++i) {
        candidates.get(k).add(new Candidate(assigned.contains(i), assignmentCosts[k][i]));
      }
    }

    return candidates;
  }

  /**
   * Converts a solution to a list of candidates.
   * @param solution the solution to convert
   * @param m the number of resources
   * @param n the number of services
   * @return a list of lists of candidates, where each inner list corresponds to a service
   */
  public static List<List<Candidate>> solutionToCandidates(int[] solution, int m, int n) {
    final var candidates = new ArrayList<List<Candidate>>();
    for (int k = 0; k < n; ++k) {
      candidates.add(new ArrayList<>());

      for (int i = 0; i < m; ++i) {
        candidates.get(k).add(new Candidate(solution[k] == i, 0.0));
      }
    }
    return candidates;
  }
}
