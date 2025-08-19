package at.ac.uibk.dps.pulse.local.assignment;

import at.ac.uibk.dps.pulse.local.graph.ServiceGraph;
import at.ac.uibk.dps.pulse.local.model.Characteristics;
import at.ac.uibk.dps.pulse.local.model.Resource;
import at.ac.uibk.dps.pulse.local.model.Service;
import at.ac.uibk.dps.pulse.local.model.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.objective.Maximize;
import org.moeaframework.core.objective.Minimize;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.problem.AbstractProblem;

/**
 * The Pulse assignment problem.
 */
public class PulseAssignmentProblem extends AbstractProblem {

  public static final int COST_OBJECTIVE = 0;
  public static final int FAIRNESS_OBJECTIVE = 1;
  public static final int NUM_OBJECTIVES = 2;

  private final List<Resource> resources;
  private final List<Service> services;

  private final ServiceGraph serviceGraph;

  private final int m;
  private final int n;

  private final double p;

  private final int[][] f;
  private final double[][] c;

  private final boolean[][] assignments;
  private final double[] resourceMaxUtilization;

  /**
   * Constructs a new Pulse assignment problem, given a global state and a fairness parameter p.
   * @param state the global state
   * @param p the fairness parameter for the Lp-norm
   */
  public PulseAssignmentProblem(State state, double p) {
    super(state.getResources().size(), NUM_OBJECTIVES);
    
    resources = state.getResources();
    services = state.getServices();

    serviceGraph = state.getServiceGraph();

    m = resources.size();
    n = services.size();

    this.p = p;

    f = new int[n][];
    for (int k = 0; k < n; ++k) {
      final var service = services.get(k);
      final var feasible = new ArrayList<Integer>();
      for (int i = 0; i < m; ++i) {
        final var resource = resources.get(i);
        if (service.getRequirements().leq(resource.getCharacteristics())) {
          feasible.add(i);
        }
      }

      final var feasibleArray = new int[feasible.size()];
      for (int j = 0; j < feasible.size(); ++j) {
        feasibleArray[j] = feasible.get(j);
      }
      Arrays.sort(feasibleArray);

      f[k] = feasibleArray;
    }

    c = new double[n][m];

    for (int k = 0; k < n; ++k) {
      final var service = services.get(k);

      final var outEdges = serviceGraph.outgoingEdgesOf(service);
      final var inEdges = serviceGraph.incomingEdgesOf(service);

      for (int i = 0; i < m; ++i) {
        final var resource = resources.get(i);

        var cost =
          resource.getCost().fixed() +
          service.getData() *
          resource.getCost().data();

        for (final var edge : outEdges) {
          cost += edge.dataTransfer() * resource.getCost().out();
        }
        for (final var edge : inEdges) {
          cost += edge.dataTransfer() * resource.getCost().in();
        }

        c[k][i] = cost;
      }
    }

    assignments = new boolean[n][m];
    resourceMaxUtilization = new double[m];
  }

  private static double lpNorm(double[] values, double p) {
    var sum = 0.0;
    for (double v : values) {
      sum += Math.pow(v, p);
    }
    return Math.pow(sum, 1.0 / p);
  }

  private static void adjustCardinality(BinaryVariable variable, int targetCardinality) {
    final var currentCardinality = variable.cardinality();
    if (currentCardinality == targetCardinality) {
      return;
    }

    final var setBits = new ArrayList<Integer>();
    final var clearBits = new ArrayList<Integer>();

    for (int i = 0; i < variable.getNumberOfBits(); ++i) {
      if (variable.get(i)) {
        setBits.add(i);
      } else {
        clearBits.add(i);
      }
    }

    if (currentCardinality > targetCardinality) {
      Collections.shuffle(setBits, PRNG.getRandom());
      for (int i = 0; i < currentCardinality - targetCardinality; ++i) {
        variable.set(setBits.get(i), false);
      }
    } else {
      Collections.shuffle(clearBits, PRNG.getRandom());
      for (int i = 0; i < targetCardinality - currentCardinality; ++i) {
        variable.set(clearBits.get(i), true);
      }
    }
  }

  @Override
  public void evaluate(Solution solution) {
    for (int k = 0; k < n; ++k) {
      Arrays.fill(assignments[k], false);

      final var variable = ((BinaryVariable) solution.getVariable(k));
      adjustCardinality(variable, Math.min(services.get(k).getReplicas(), f[k].length));

      final var bitset = variable.getBitSet();
      for (int x = bitset.nextSetBit(0); x >= 0; x = bitset.nextSetBit(x + 1)) {
        assignments[k][f[k][x]] = true;
      }
    }

    {
      var sum = 0.0;
      for (int k = 0; k < n; ++k) {
        for (int i = 0; i < m; ++i) {
          if (assignments[k][i]) {
            sum += c[k][i];
          }
        }
      }
      solution.setObjectiveValue(COST_OBJECTIVE, sum);
    }

    for (int i = 0; i < m; ++i) {
      final var resource = resources.get(i);

      Characteristics totalUtilization = null;

      for (int k = 0; k < n; ++k) {
        if (assignments[k][i]) {
          final var utilization = services
            .get(k)
            .getRequirements()
            .div(resource.getCharacteristics());

          if (totalUtilization == null) {
            totalUtilization = utilization;
          } else {
            totalUtilization = totalUtilization.add(utilization);
          }
        }
      }

      if (totalUtilization == null) {
        resourceMaxUtilization[i] = 0.0;
      } else {
        resourceMaxUtilization[i] = totalUtilization.max();
      }
    }
    solution.setObjectiveValue(FAIRNESS_OBJECTIVE, lpNorm(resourceMaxUtilization, p));
  }

  /**
   * Returns the feasible resources for each service.
   * @return a 2D array where each row corresponds to a service and contains the indices of feasible resources
   */
  public int[][] getFeasibleResources() {
    return f;
  }

  /**
   * Returns the assignment costs for each service-resource pair.
   * @return a 2D array where each row corresponds to a service and each column corresponds to a resource
   */
  public double[][] getAssignmentCosts() {
    return c;
  }

  /**
   * Returns the services that are part of the assignment problem.
   * @return a list of services
   */
  public List<Service> getServices() {
    return services;
  }

  /**
   * Returns the resources that are part of the assignment problem.
   * @return a list of resources
   */
  public List<Resource> getResources() {
    return resources;
  }

  @Override
  public int getNumberOfConstraints() {
    return 0;
  }

  @Override
  public int getNumberOfObjectives() {
    return NUM_OBJECTIVES;
  }

  @Override
  public int getNumberOfVariables() {
    return n;
  }

  @Override
  public Solution newSolution() {
    final var solution = new Solution(n, NUM_OBJECTIVES, 0);

    // Initialize variables for each resource
    for (int k = 0; k < n; ++k) {
      solution.setVariable(k, new BinaryVariable("Assignment %d".formatted(k), f[k].length));
    }

    // Initialize objectives for cost minimization and fairness maximization
    solution.setObjective(COST_OBJECTIVE, new Minimize("Cost"));
    solution.setObjective(FAIRNESS_OBJECTIVE, new Maximize("Fairness"));

    return solution;
  }
}
