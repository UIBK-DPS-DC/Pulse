package at.ac.uibk.dps.pulse.local.assignment;

import java.util.stream.IntStream;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.initialization.Initialization;
import org.moeaframework.core.variable.BinaryVariable;

/**
 * An initialization strategy for the Pulse assignment problem that assigns replicas to resources in a way that
 * respects the number of replicas required by each service.
 */
public class ReplicaAwareInitialization implements Initialization {

  private final PulseAssignmentProblem problem;

  /**
   * Constructs a new replica aware initialization instance.
   * @param problem the Pulse assignment problem to be solved
   */
  public ReplicaAwareInitialization(PulseAssignmentProblem problem) {
    super();
    
    this.problem = problem;
  }

  @Override
  public Solution[] initialize(int populationSize) {
    Solution[] initialPopulation = new Solution[populationSize];

    for (int p = 0; p < populationSize; ++p) {
      Solution solution = problem.newSolution();

      final var numVariables = solution.getNumberOfVariables();

      for (int j = 0; j < numVariables; ++j) {
        ((BinaryVariable) solution.getVariable(j)).clear();
      }

      for (int k = 0; k < problem.getServices().size(); ++k) {
        final var feasibleResources = problem.getFeasibleResources()[k];
        if (feasibleResources.length == 0) {
          continue;
        }

        for (final var i : IntStream.generate(() -> PRNG.nextInt(feasibleResources.length))
          .limit(problem.getServices().get(k).getReplicas())
          .toArray()) {
          ((BinaryVariable) solution.getVariable(k)).set(i, true);
        }
      }

      initialPopulation[p] = solution;
    }

    return initialPopulation;
  }
}
