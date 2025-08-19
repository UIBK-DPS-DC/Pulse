package at.ac.uibk.dps.pulse.global.composition;

import at.ac.uibk.dps.pulse.global.graph.ClusterGraph;
import at.ac.uibk.dps.pulse.global.model.Cluster;
import at.ac.uibk.dps.pulse.global.model.State;
import at.ac.uibk.dps.pulse.local.model.Service;
import java.util.ArrayList;
import java.util.List;
import org.moeaframework.core.Solution;
import org.moeaframework.core.constraint.Equal;
import org.moeaframework.core.objective.Minimize;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.problem.AbstractProblem;

/**
 * The Pulse composition problem.
 */
public class PulseCompositionProblem extends AbstractProblem implements CompositionProblem {

  public static final int COST_OBJECTIVE = 0;
  public static final int LATENCY_OBJECTIVE = 1;
  public static final int NUM_OBJECTIVES = 2;

  private final List<Cluster> clusters;
  private final List<Service> services;

  private final ClusterGraph clusterGraph;

  private final int h;
  private final int n;

  private final List<List<ClusterResourceIndex>> clusterResourceIndices;

  /**
   * Construct a new Pulse composition problem, given a global state.
   * @param state the global state
   */
  public PulseCompositionProblem(State state) {
    super(state.getServices().size(), NUM_OBJECTIVES);
    
    clusters = state.getClusters();
    services = state.getServices();

    clusterGraph = state.getClusterGraph();

    h = state.getClusters().size();
    n = state.getServices().size();

    clusterResourceIndices = new ArrayList<>();
    for (int k = 0; k < n; ++k) {
      final var indices = new ArrayList<ClusterResourceIndex>();
      for (int u = 0; u < h; ++u) {
        final var candidates = clusters.get(u).getCandidates().get(k);
        for (int i = 0; i < candidates.size(); ++i) {
          if (candidates.get(i).assigned()) {
            indices.add(new ClusterResourceIndex(u, i));
          }
        }
      }
      clusterResourceIndices.add(indices);
    }
  }

  public boolean isComplete() {
    return clusterResourceIndices.stream().noneMatch(List::isEmpty);
  }

  @Override
  public List<Cluster> getClusters() {
    return clusters;
  }

  @Override
  public List<Service> getServices() {
    return services;
  }

  @Override
  public List<ClusterResourceIndex> getClusterResourceIndices(int k) {
    return clusterResourceIndices.get(k);
  }

  @Override
  public int getNumberOfConstraints() {
    return n;
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
  public void evaluate(Solution solution) {
    final var clusterIndices = new ArrayList<Integer>();

    var cost = 0.0;
    for (int k = 0; k < n; ++k) {
      var assigned = 0;

      final var bitset = ((BinaryVariable) solution.getVariable(k)).getBitSet();
      for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
        final var clusterResource = clusterResourceIndices.get(k).get(i);
        final var candidate = clusters
          .get(clusterResource.u())
          .getCandidates()
          .get(k)
          .get(clusterResource.i());

        assert candidate.assigned();

        cost += candidate.cost();

        if (!clusterIndices.contains(clusterResource.u())) {
          clusterIndices.add(clusterResource.u());
        }

        ++assigned;
      }

      solution.setConstraintValue(k, assigned);
    }

    var latency = 0.0;
    for (final var u : clusterIndices) {
      for (final var v : clusterIndices) {
        latency += clusterGraph.getLatency(clusters.get(u), clusters.get(v)).orElse(0.0);
      }
    }

    solution.setObjectiveValue(COST_OBJECTIVE, cost);
    solution.setObjectiveValue(LATENCY_OBJECTIVE, latency);
  }

  @Override
  public Solution newSolution() {
    final var solution = new Solution(n, NUM_OBJECTIVES, n);

    for (int k = 0; k < n; ++k) {
      solution.setVariable(
        k,
        new BinaryVariable("Assignment %d".formatted(k), clusterResourceIndices.get(k).size())
      );
    }

    solution.setObjective(0, new Minimize("Cost"));
    solution.setObjective(1, new Minimize("Latency"));

    for (int k = 0; k < n; ++k) {
      solution.setConstraint(
        k,
        new Equal("Replicas %d".formatted(k), services.get(k).getReplicas())
      );
    }

    return solution;
  }
}
